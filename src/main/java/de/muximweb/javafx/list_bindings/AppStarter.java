package de.muximweb.javafx.list_bindings;

import de.muximweb.javafx.list_bindings.view.ListBindingView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The starter for the list-bindings application.
 */
public class AppStarter extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(new ListBindingView().getRoot());
        primaryStage.setTitle("List Bindings App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
