package org.example.project_hub;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LoginApp loginApp = new LoginApp();
        loginApp.start(primaryStage);
    }
}
