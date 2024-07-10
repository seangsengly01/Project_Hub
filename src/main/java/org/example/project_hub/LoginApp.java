package org.example.project_hub;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Load the image
        Image logoImage = new Image("file:src/main/image/logo_login.png");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(100);  // Adjust the size as needed
        logoImageView.setPreserveRatio(true);

        // Create the login form
        VBox loginVBox = new VBox(20);
        loginVBox.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2E2E2E;");
        Label welcomeLabel = new Label("Welcome to the Login Page");
        welcomeLabel.setFont(new Font("Arial", 24));
        welcomeLabel.setTextFill(Color.LIGHTGRAY);
        welcomeLabel.setEffect(new DropShadow(2, 2, 2, Color.GRAY));

        Label userLabel = new Label("Username:");
        userLabel.setTextFill(Color.LIGHTGRAY);
        TextField userTextField = new TextField();
        Label passLabel = new Label("Password:");
        passLabel.setTextFill(Color.LIGHTGRAY);
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);

        GridPane formGrid = new GridPane();
        formGrid.setVgap(10);
        formGrid.setHgap(10);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.add(userLabel, 0, 0);
        formGrid.add(userTextField, 1, 0);
        formGrid.add(passLabel, 0, 1);
        formGrid.add(passField, 1, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        loginVBox.getChildren().addAll(logoImageView, welcomeLabel, formGrid, buttonBox, messageLabel);

        // Add animation for input fields and buttons
        addFadeTransition(userTextField);
        addFadeTransition(passField);
        addFadeTransition(loginButton);
        addFadeTransition(registerButton);

        // Handle login button click
        loginButton.setOnAction(e -> {
            String username = userTextField.getText();
            String password = passField.getText();
            if (validate(username, password)) {
                Dashboard dashboard = new Dashboard(username);  // Pass the username to Dashboard
                dashboard.start(primaryStage);  // Initialize the Dashboard
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        });

        // Handle register button click
        registerButton.setOnAction(e -> {
            Registration registration = new Registration();
            registration.start(primaryStage);
        });

        StackPane rootPane = new StackPane(loginVBox);
        Scene scene = new Scene(rootPane, 800, 600);

        // Open the application in full-screen mode
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private boolean validate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addFadeTransition(TextField textField) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), textField);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private void addFadeTransition(PasswordField passwordField) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), passwordField);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    private void addFadeTransition(Button button) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), button);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
}
