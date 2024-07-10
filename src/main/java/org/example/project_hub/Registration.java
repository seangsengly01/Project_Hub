package org.example.project_hub;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
import java.sql.SQLException;

public class Registration extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Registration");

        // Initial setup
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Show the registration form
        showRegistrationForm(primaryStage, root);
    }

    private void showRegistrationForm(Stage primaryStage, StackPane root) {
        // Welcome Title
        Label welcomeLabel = new Label("Welcome to User Registration");
        welcomeLabel.setFont(new Font("Arial", 24));
        welcomeLabel.setTextFill(Color.LIGHTGRAY);
        welcomeLabel.setEffect(new DropShadow(2, 2, 2, Color.BLACK));

        // Logo Image
        ImageView logoImageView = new ImageView(new Image("file:src/main/image/logo_login.png"));
        logoImageView.setFitWidth(100);
        logoImageView.setPreserveRatio(true);
        logoImageView.setEffect(new DropShadow(2, 2, 2, Color.BLACK));

        // User Registration Form
        GridPane formGrid = new GridPane();
        formGrid.setVgap(10);
        formGrid.setHgap(10);
        formGrid.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username:");
        userLabel.setTextFill(Color.LIGHTGRAY);
        TextField userField = new TextField();
        userField.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white;");
        Label passLabel = new Label("Password:");
        passLabel.setTextFill(Color.LIGHTGRAY);
        PasswordField passField = new PasswordField();
        passField.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white;");
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        registerButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and Password cannot be empty.");
            } else if (username.length() < 3 || password.length() < 3) {
                errorLabel.setText("Username and Password must be at least 3 characters long.");
            } else {
                boolean success = registerUser(username, password);

                if (success) {
                    showSuccessScreen(primaryStage, root);
                } else {
                    errorLabel.setText("User registration failed.");
                }
            }
        });

        backButton.setOnAction(e -> {
            LoginApp loginApp = new LoginApp();
            loginApp.start(primaryStage);
        });

        formGrid.add(userLabel, 0, 0);
        formGrid.add(userField, 1, 0);
        formGrid.add(passLabel, 0, 1);
        formGrid.add(passField, 1, 1);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(backButton, registerButton);

        VBox registrationVBox = new VBox(20);
        registrationVBox.getChildren().addAll(logoImageView, welcomeLabel, formGrid, buttonBox, errorLabel);
        registrationVBox.setAlignment(Pos.CENTER);
        registrationVBox.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2E2E2E;");

        // Animation for TextField and Button
        addFadeTransition(userField);
        addFadeTransition(passField);
        addFadeTransition(registerButton);
        addFadeTransition(backButton);

        root.getChildren().clear();
        root.getChildren().add(registrationVBox);
    }

    private boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                return false;
            }

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showSuccessScreen(Stage primaryStage, StackPane root) {
        Label successLabel = new Label("Registration Successful!");
        successLabel.setFont(new Font("Arial", 24));
        successLabel.setTextFill(Color.GREEN);
        successLabel.setEffect(new DropShadow(2, 2, 2, Color.GRAY));

        StackPane successPane = new StackPane(successLabel);
        successPane.setAlignment(Pos.CENTER);

        root.getChildren().clear();
        root.getChildren().add(successPane);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> showRegistrationForm(primaryStage, root));
        pause.play();
    }

    private void addFadeTransition(TextField textField) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), textField);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void addFadeTransition(PasswordField passwordField) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), passwordField);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void addFadeTransition(Button button) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1500), button);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
