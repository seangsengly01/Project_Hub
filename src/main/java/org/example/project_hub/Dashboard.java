package org.example.project_hub;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

public class Dashboard extends Application {

    private String username;
    private Stage primaryStage;

    public Dashboard() {
        // Default constructor needed for Application subclass
    }

    public Dashboard(String username) {
        this.username = username;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Dashboard");

        // Load the main dashboard image
        Image dashboardImage = new Image("file:src/main/image/img.png");
        ImageView dashboardImageView = new ImageView(dashboardImage);
        dashboardImageView.setFitWidth(100);  // Adjust the size as needed
        dashboardImageView.setPreserveRatio(true);

        Label welcomeLabel = new Label("Welcome to the Dashboard, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-padding: 20px; -fx-text-fill: lightgray;");

        // Create the mini cases for each project
        VBox toDoAppBox = createProjectBox("To-Do List Management", "file:src/main/image/checklist.png");
        VBox financeTrackerBox = createProjectBox("Mini Personal Finance Tracker", "file:src/main/image/finaceNote.png");
        VBox currencyConverterBox = createProjectBox("Currency Converter", "file:src/main/image/convertCurrency.png");

        HBox projectsBox = new HBox(30, toDoAppBox, financeTrackerBox, currencyConverterBox);
        projectsBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(20, dashboardImageView, welcomeLabel, projectsBox);
        vbox.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2E2E2E;");

        Scene scene = new Scene(vbox, 800, 600);

        primaryStage.setScene(scene);

        // Maximize the window but not full screen
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false); // Ensure not full screen

        primaryStage.show();
    }

    private VBox createProjectBox(String projectName, String imagePath) {
        Image projectImage = new Image(imagePath);
        ImageView projectImageView = new ImageView(projectImage);
        projectImageView.setFitWidth(50);  // Adjust the size as needed
        projectImageView.setPreserveRatio(true);

        Label projectLabel = new Label(projectName);
        projectLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: lightgray;");

        Button openButton = new Button("Open Project");
        openButton.setStyle(
                "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );

        openButton.setOnAction(e -> {
            if (projectName.equals("To-Do List Management")) {
                openToDoApp();
            } else if (projectName.equals("Mini Personal Finance Tracker")) {
                openFinanceTracker(e);
            } else if (projectName.equals("Currency Converter")) {
                openCurrencyConverter(e);
            }
        });

        VBox projectBox = new VBox(10, projectImageView, projectLabel, openButton);
        projectBox.setStyle("-fx-background-color: #3E3E3E; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        projectBox.setAlignment(Pos.CENTER);

        // Add mouse hover animation
        addHoverAnimation(projectBox);

        return projectBox;
    }

    private void addHoverAnimation(VBox projectBox) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), projectBox);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);

        projectBox.setOnMouseEntered(e -> scaleTransition.playFromStart());
        projectBox.setOnMouseExited(e -> {
            scaleTransition.setRate(-1);
            scaleTransition.play();
        });
    }

    private void openToDoApp() {
        primaryStage.hide(); // Hide the previous Dashboard stage
        Stage toDoStage = new Stage();
        ToDoApp toDoApp = new ToDoApp(username, primaryStage);  // Pass the username and previous stage to ToDoApp
        toDoApp.start(toDoStage);
        toDoStage.setMaximized(true);
        toDoStage.setFullScreen(false); // Ensure not full screen
    }

    private void openFinanceTracker(ActionEvent e) {
        Stage newStage = new Stage();
        MiniPersonalFinanceTracker financeTracker = new MiniPersonalFinanceTracker(username, newStage); // Pass the username and new stage
        financeTracker.start(newStage);
        newStage.setMaximized(true); // Ensure the new stage is maximized
        newStage.setFullScreen(false); // Ensure not full screen
        primaryStage.close(); // Close the previous stage
    }

    private void openCurrencyConverter(ActionEvent e) {
        Stage currencyConverterStage = new Stage();
        CurrencyConverter currencyConverter = new CurrencyConverter(username, (Stage) ((Node) e.getSource()).getScene().getWindow()); // Pass the username and the current stage
        currencyConverter.start(currencyConverterStage);
        currencyConverterStage.setMaximized(true);
        currencyConverterStage.setFullScreen(false); // Ensure not full screen
    }

    public static void main(String[] args) {
        launch(args);
    }
}
