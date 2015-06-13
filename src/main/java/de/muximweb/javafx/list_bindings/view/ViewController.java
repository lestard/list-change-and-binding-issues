package de.muximweb.javafx.list_bindings.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * An universal view controller (code behind view) class that contains the logic for loading view from FXML file. The
 * sub class can then simply use the @FXML annotation to get view references injected.
 */
public abstract class ViewController {

    private Parent root;

    /**
     * Loads the given FXML and sets this instance as the responsible view controller class.
     *
     * @param fxmlResourcePath A valid resource path (in the classpath) to the FXML file to be loaded.
     */
    public ViewController(String fxmlResourcePath) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlResourcePath));
        fxmlLoader.setController(this);

        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Parent getRoot() {
        return root;
    }
}
