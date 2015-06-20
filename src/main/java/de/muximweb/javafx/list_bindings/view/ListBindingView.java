package de.muximweb.javafx.list_bindings.view;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.LinkedList;

/**
 * This is a view class that shows an different bindings between two list properties.
 */
public class ListBindingView extends ViewController {

    private static final String FXML_PATH = "/" + ListBindingView.class.getName().replace(".", "/") + ".fxml";

    @FXML private VBox root;

    @FXML private TextField textFieldNewEntry;
    @FXML private Button buttonAddToList;

    @FXML private TextField textFieldPosition;
    @FXML private TextField textFieldNewValue;
    @FXML private Button buttonReplaceValue;
    @FXML private Button buttonRemoveValue;

    @FXML private RadioButton buttonLeft;
    @FXML private RadioButton buttonRight;

    @FXML private RadioButton buttonUnidirectionalContent;
    @FXML private RadioButton buttonUnidirectional;
    @FXML private RadioButton buttonBidirectionalContent;
    @FXML private RadioButton buttonBidirectional;

    @FXML private ListView<StringProperty> listViewRight;
    @FXML private ListView<StringProperty> listViewLeft;

    @FXML private Label labelInfo;

    private ListProperty<StringProperty> listPropertyLeft;
    private ListProperty<StringProperty> listPropertyRight;

    private Runnable unbindAction;

    // current hideTransition for info label
    private FadeTransition hideTransition;
    private FadeTransition appearTransition;
    private Timeline scheduleHideTransitionTimeline;

    public ListBindingView() {
        super(FXML_PATH);
        initializeView();
    }

    private void initializeView() {

        listPropertyLeft = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
        listPropertyRight = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));

        listViewLeft.setItems(listPropertyLeft);
        listViewRight.setItems(listPropertyRight);

        // initial binding
        listPropertyLeft.bind(listPropertyRight);
        unbindAction = listPropertyLeft::unbind;

        initializeListeners();

        hideInfoLabel(false);
    }

    private void initializeListeners() {

        // modification actions
        buttonAddToList.setOnAction(e -> {
            addToList(textFieldNewEntry.getText());
            textFieldNewEntry.clear();
        });
        buttonReplaceValue.setOnAction(e -> {
            replaceValueAt(textFieldNewValue.getText(), textFieldPosition.getText());
            textFieldPosition.clear();
            textFieldNewValue.clear();
        });
        buttonRemoveValue.setOnAction(e -> {
            removeValueAt(textFieldPosition.getText());
            textFieldPosition.clear();
        });

        // binding type changes
        buttonUnidirectional.setOnAction(e -> {
            showInfoLabel("Set unidirectional binding type and cleared lists.");
            unbind();
            Platform.runLater(() -> listPropertyLeft.bind(listPropertyRight));
            unbindAction = listPropertyLeft::unbind;
        });
        buttonUnidirectionalContent.setOnAction(e -> {
            showInfoLabel("Set unidirectional content binding type and cleared lists.");
            unbind();
            Platform.runLater(() -> listPropertyLeft.bindContent(listPropertyRight));
            unbindAction = () -> listPropertyLeft.unbindContent(listPropertyRight);
        });
        buttonBidirectional.setOnAction(e -> {
            showInfoLabel("Set bidirectional binding type and cleared lists.");
            unbind();
            Platform.runLater(() -> listPropertyLeft.bindBidirectional(listPropertyRight));
            unbindAction = () -> listPropertyLeft.unbindBidirectional(listPropertyRight);
        });
        buttonBidirectionalContent.setOnAction(e -> {
            showInfoLabel("Set bidirectional content binding type and cleared lists.");
            unbind();
            Platform.runLater(() -> listPropertyLeft.bindContentBidirectional(listPropertyRight));
            unbindAction = () -> listPropertyLeft.unbindContentBidirectional(listPropertyRight);
        });


        // notification about list changes
        ListChangeListener<StringProperty> listChangeListener = change -> {
            while (change.next()) {
                if (change.wasPermutated()) {
                    showInfoLabel("List value was permutated.");
                } else if (change.wasReplaced()) {
                    showInfoLabel("List value was replaced.");
                } else if (change.wasRemoved()) {
                    showInfoLabel("List value was removed.");
                } else if (change.wasAdded()) {
                    showInfoLabel("List value was added.");
                }
            }
        };
        listPropertyLeft.addListener(listChangeListener);

        buttonLeft.setOnAction(e -> {
            listPropertyRight.removeListener(listChangeListener);
            listPropertyLeft.addListener(listChangeListener);
        });
        buttonRight.setOnAction(e -> {
            listPropertyLeft.removeListener(listChangeListener);
            listPropertyRight.addListener(listChangeListener);
        });
    }

    private void removeValueAt(String positionAsString) {
        try {
            int position = parsePosition(positionAsString);
            ListProperty<StringProperty> listProperty = getActiveListProperty();

            if (position < listProperty.size()) {
                listProperty.remove(position);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            alertError("Error", "Please enter a number in the 'Position' text field which is greater than 0 but " +
                    "maximum equal to the list size.");
        }
    }

    private void replaceValueAt(String text, String positionAsString) {
        ListProperty<StringProperty> listProperty = getActiveListProperty();
        changeValue(text, positionAsString, () -> listProperty.set(parsePosition(positionAsString), new SimpleStringProperty(text)));
    }

    private int parsePosition(String positionAsString) {
        return Integer.parseInt(positionAsString);
    }

    private void changeValue(String text, String positionAsString, Runnable changeOperation) {
        try {
            int position = parsePosition(positionAsString);
            ListProperty<StringProperty> listProperty = getActiveListProperty();

            if (position < listProperty.size()) {
                ifTextValueIsValidExecute(text, changeOperation);
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            alertError("Error", "Please enter a number in the 'Position' text field which is greater than 0 but " +
                    "maximum equal to the list size.");
        }
    }

    private void unbind() {
        Platform.runLater(unbindAction);
        Platform.runLater(() -> {
            listPropertyLeft.set(FXCollections.observableArrayList());
            listPropertyRight.set(FXCollections.observableArrayList());
        });
    }

    private void addToList(String text) {
        ifTextValueIsValidExecute(text, () -> getActiveListProperty().add(new SimpleStringProperty(text)));
    }

    private void ifTextValueIsValidExecute(final String text, Runnable modificationAction) {
        if (text != null && !text.trim().isEmpty()) {
            Platform.runLater(modificationAction);
        } else {
            alertError("Error", "Please enter a text as list item value for your action.");
        }
    }

    private ListProperty<StringProperty> getActiveListProperty() {
        if (buttonLeft.selectedProperty().get()) {
            return listPropertyLeft;
        } else {
            return listPropertyRight;
        }
    }

    private void alertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void hideInfoLabel(boolean withFade) {
        if (withFade) {

            if (hideTransition == null) {
                hideTransition = new FadeTransition(Duration.seconds(1));
                hideTransition.setNode(labelInfo);
                hideTransition.setFromValue(1);
                hideTransition.setToValue(0);
                hideTransition.setOnFinished(e -> labelInfo.setVisible(false));
            }
            hideTransition.playFromStart();
        } else {
            labelInfo.setVisible(false);
        }
    }

    private void showInfoLabel(String infoText) {
        labelInfo.setText(infoText);
        labelInfo.setVisible(true);

        if (appearTransition == null) {
            appearTransition = new FadeTransition(Duration.seconds(1), labelInfo);
            appearTransition.setFromValue(0);
            appearTransition.setToValue(1);
        }
        // cancel all other transitions / scheduled transitions
        else {
            appearTransition.stop();
        }
        if (hideTransition != null) {
            hideTransition.stop();
            scheduleHideTransitionTimeline.stop();
        }

        appearTransition.playFromStart();
        scheduleHideTransitionTimeline = new Timeline();
        scheduleHideTransitionTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5), null));
        scheduleHideTransitionTimeline.setOnFinished(e -> hideInfoLabel(true));
        scheduleHideTransitionTimeline.play();
    }
}
