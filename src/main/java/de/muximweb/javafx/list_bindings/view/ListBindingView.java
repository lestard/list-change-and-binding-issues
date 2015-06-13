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
    @FXML private Button buttonUpdateValue;

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

    public ListBindingView() {
        super(FXML_PATH);
        initializeView();
    }

    private void initializeView() {

        listPropertyLeft = new SimpleListProperty<>(FXCollections.observableArrayList());
        listPropertyRight = new SimpleListProperty<>(FXCollections.observableArrayList());

        listViewLeft.setItems(listPropertyLeft);
        listViewRight.setItems(listPropertyRight);

        // initial binding
        listPropertyLeft.bind(listPropertyRight);
        unbindAction = listPropertyLeft::unbind;

        initializeListeners();

        hideInfoLabel(false);
    }

    private void initializeListeners() {
        buttonAddToList.setOnAction(e -> {
            addToList(textFieldNewEntry.getText());
            textFieldNewEntry.clear();
        });
        buttonReplaceValue.setOnAction(e -> {
            replaceValueAt(textFieldNewValue.getText(), textFieldPosition.getText());
            textFieldPosition.clear();
            textFieldNewValue.clear();
        });
        buttonUpdateValue.setOnAction(e -> {
            updateValueAt(textFieldNewValue.getText(), textFieldPosition.getText());
            textFieldPosition.clear();
            textFieldNewValue.clear();
        });
        buttonUnidirectional.setOnAction(e -> {
            unbind();
            listPropertyLeft.bind(listPropertyRight);
            unbindAction = listPropertyLeft::unbind;
        });
        buttonUnidirectionalContent.setOnAction(e -> {
            unbind();
            listPropertyLeft.bindContent(listPropertyRight);
            unbindAction = () -> listPropertyLeft.unbindContent(listPropertyRight);
        });
        buttonBidirectional.setOnAction(e -> {
            unbind();
            listPropertyLeft.bindBidirectional(listPropertyRight);
            unbindAction = () -> listPropertyLeft.unbindBidirectional(listPropertyRight);
        });
        buttonBidirectionalContent.setOnAction(e -> {
            unbind();
            listPropertyLeft.bindContentBidirectional(listPropertyRight);
            unbindAction = () -> listPropertyLeft.unbindContentBidirectional(listPropertyRight);
        });


        ListChangeListener<StringProperty> listChangeListener = change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    showInfoLabel("List value was updated.");
                } else if (change.wasReplaced()) {
                    showInfoLabel("List value was replaced.");
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

    private void updateValueAt(String text, String positionAsString) {

        // Currently not works, some ideas to this problem:
        // http://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes
        // https://community.oracle.com/thread/2244635

        ListProperty<StringProperty> listProperty = getActiveListProperty();
        changeValue(text, positionAsString, () -> {

            // just for debugging purpose in separate steps
            // result of debugging: property change ist set and it gets the updated value, but the ListChangeListener
            // does not fire an update event.
            int position = parsePosition(positionAsString);
            StringProperty property = listProperty.get(position);
            property.setValue(text);
        });
    }

    private void replaceValueAt(String text, String positionAsString) {
        ListProperty<StringProperty> listProperty = getActiveListProperty();
        changeValue(text, positionAsString, () -> listProperty.set(parsePosition(positionAsString), new SimpleStringProperty(text)));
    }

    private int parsePosition(String positionAsString) {
        return Integer.parseInt(positionAsString) - 1;
    }

    private void changeValue(String text, String positionAsString, Runnable changeOperation) {
        try {
            int position = parsePosition(positionAsString);
            ListProperty<StringProperty> listProperty = getActiveListProperty();

            if (position < listProperty.size()) {
                ifInputIsValidExecute(text, changeOperation);
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
        ifInputIsValidExecute(text, () -> getActiveListProperty().add(new SimpleStringProperty(text)));
    }

    private void ifInputIsValidExecute(final String text, Runnable modificationAction) {
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
            FadeTransition transition = new FadeTransition(Duration.seconds(1));
            transition.setNode(labelInfo);
            transition.setFromValue(1);
            transition.setToValue(0);
            transition.setOnFinished(e -> labelInfo.setVisible(false));
            transition.play();
        } else {
            labelInfo.setVisible(false);
        }
    }

    private void showInfoLabel(String infoText) {
        labelInfo.setText(infoText);
        labelInfo.setVisible(true);

        FadeTransition transition = new FadeTransition(Duration.seconds(1), labelInfo);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5), null));
        timeline.setOnFinished(e -> hideInfoLabel(true));
        timeline.play();
    }
}
