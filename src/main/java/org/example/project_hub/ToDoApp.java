package org.example.project_hub;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class ToDoApp extends Application {

    private String username;
    private ObservableList<Task> tasks;
    private TableView<Task> taskTable;
    private TextField searchField;
    private Stage previousStage;

    public ToDoApp(String username, Stage previousStage) {
        this.username = username;
        this.previousStage = previousStage;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("To-Do List");

        tasks = FXCollections.observableArrayList();

        VBox toDoVBox = new VBox(20);
        toDoVBox.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2E2E2E;");
        toDoVBox.setAlignment(Pos.TOP_CENTER);

        // Adding logo image
        Image logoImage = new Image("file:src/main/image/checklist.png");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(100);
        logoImageView.setPreserveRatio(true);

        Label toDoLabel = new Label("To-Do List");
        toDoLabel.setFont(new Font("Arial", 24));
        toDoLabel.setTextFill(Color.LIGHTGRAY);
        toDoLabel.setEffect(new DropShadow(2, 2, 2, Color.GRAY));

        Label searchLabel = new Label("Search Tasks:");
        searchLabel.setTextFill(Color.LIGHTGRAY);
        searchField = new TextField();
        searchField.setPromptText("Search tasks");
        searchField.setPrefWidth(150);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTasks(newValue));

        VBox searchBox = new VBox(5, searchLabel, searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label taskLabel = new Label("Enter new task:");
        taskLabel.setTextFill(Color.LIGHTGRAY);
        TextField taskField = new TextField();
        taskField.setPromptText("Enter a new task");
        taskField.setPrefWidth(300);
        taskField.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white;");

        Button addButton = new Button("Add Task");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(event -> {
            String task = taskField.getText();
            if (!task.isEmpty()) {
                addTask(task);
                taskField.clear();
                loadTasks();
            } else {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Task cannot be empty.");
            }
        });

        Button exportButton = new Button("Export Tasks");
        exportButton.setStyle("-fx-background-color: #0000FF; -fx-text-fill: white;");
        exportButton.setOnAction(event -> exportTasks());

        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: white;");
        backButton.setOnAction(event -> backToDashboard(primaryStage));

        VBox inputBox = new VBox(10, taskLabel, taskField, new HBox(10, addButton, exportButton, backButton));
        inputBox.setAlignment(Pos.CENTER_LEFT);

        taskTable = new TableView<>();
        taskTable.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white;");
        TableColumn<Task, String> taskColumn = new TableColumn<>("Task");
        taskColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        TableColumn<Task, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    if (status.equals("Pending")) {
                        setTextFill(Color.ORANGE);
                    } else if (status.equals("Completed")) {
                        setTextFill(Color.GREEN);
                    } else if (status.equals("Cancelled")) {
                        setTextFill(Color.RED);
                    }
                }
            }
        });

        TableColumn<Task, LocalDateTime> createdAtColumn = new TableColumn<>("Created At");
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());

        TableColumn<Task, LocalDateTime> updatedAtColumn = new TableColumn<>("Updated At");
        updatedAtColumn.setCellValueFactory(cellData -> cellData.getValue().updatedAtProperty());

        TableColumn<Task, Void> editColumn = new TableColumn<>("Edit");
        editColumn.setCellFactory(col -> {
            TableCell<Task, Void> cell = new TableCell<>() {
                private final Button editButton = new Button("Edit");

                {
                    editButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
                    editButton.setOnAction(event -> {
                        Task task = getTableView().getItems().get(getIndex());
                        editTask(task);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(editButton);
                    }
                }
            };
            return cell;
        });

        TableColumn<Task, Void> removeColumn = new TableColumn<>("Remove");
        removeColumn.setCellFactory(col -> {
            TableCell<Task, Void> cell = new TableCell<>() {
                private final Button removeButton = new Button("Remove");

                {
                    removeButton.setStyle("-fx-background-color: #FF0000; -fx-text-fill: white;");
                    removeButton.setOnAction(event -> {
                        Task task = getTableView().getItems().get(getIndex());
                        deleteTask(task.getDescription());
                        loadTasks();
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(removeButton);
                    }
                }
            };
            return cell;
        });

        taskTable.getColumns().addAll(taskColumn, statusColumn, createdAtColumn, updatedAtColumn, editColumn, removeColumn);

        loadTasks();

        toDoVBox.getChildren().addAll(logoImageView, toDoLabel, searchBox, inputBox, taskTable);

        StackPane toDoPane = new StackPane(toDoVBox);
        Scene toDoScene = new Scene(toDoPane, 800, 600);

        // Adding dark mode CSS directly
        String darkModeCSS = """
                .table-view {
                    -fx-background-color: #2E2E2E;
                    -fx-text-fill: white;
                }

                .table-view .column-header-background {
                    -fx-background-color: #3E3E3E;
                }

                .table-view .filler {
                    -fx-background-color: #2E2E2E;
                }

                .table-view .column-header, .table-view .column-header-background .label {
                    -fx-background-color: #3E3E3E;
                    -fx-text-fill: white;
                }

                .table-row-cell {
                    -fx-background-color: #2E2E2E;
                    -fx-text-fill: white;
                }

                .table-row-cell:odd {
                    -fx-background-color: #3E3E3E;
                }

                .table-cell {
                    -fx-text-fill: white;
                }
                """;
        toDoScene.getStylesheets().add("data:text/css," + darkModeCSS.replace("\n", "").replace(" ", "%20"));

        primaryStage.setScene(toDoScene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(false); // Ensure not full screen
        primaryStage.show();
    }

    private void addTask(String task) {
        String sql = "INSERT INTO tasks (username, description, status, createdAt, updatedAt) VALUES (?, ?, 'Pending', NOW(), NOW())";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, task);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add task.");
        }
    }

    private void loadTasks() {
        tasks.clear();
        String sql = "SELECT description, status, createdAt, updatedAt FROM tasks WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String description = resultSet.getString("description");
                String status = resultSet.getString("status");
                LocalDateTime createdAt = resultSet.getTimestamp("createdAt").toLocalDateTime();
                LocalDateTime updatedAt = resultSet.getTimestamp("updatedAt").toLocalDateTime();
                tasks.add(new Task(description, status, createdAt, updatedAt));
            }
            taskTable.setItems(tasks);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load tasks.");
        }
    }

    private void deleteTask(String description) {
        String sql = "DELETE FROM tasks WHERE username = ? AND description = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, description);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete task.");
        }
    }

    private void filterTasks(String query) {
        if (query.isEmpty()) {
            taskTable.setItems(tasks);
        } else {
            ObservableList<Task> filteredTasks = FXCollections.observableArrayList();
            for (Task task : tasks) {
                if (task.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredTasks.add(task);
                }
            }
            taskTable.setItems(filteredTasks);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportTasks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Tasks");
        fileChooser.setInitialFileName("tasks.txt");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Task task : tasks) {
                    writer.write("Task: " + task.getDescription() + "\n");
                    writer.write("Status: " + task.getStatus() + "\n");
                    writer.write("Created At: " + task.getCreatedAt() + "\n");
                    writer.write("Updated At: " + task.getUpdatedAt() + "\n");
                    writer.write("\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Tasks exported successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "File Error", "Could not save tasks to file.");
            }
        }
    }

    private void editTask(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit your task");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        TextField descriptionField = new TextField(task.getDescription());
        ComboBox<String> statusComboBox = new ComboBox<>(FXCollections.observableArrayList("Pending", "Completed", "Cancelled"));
        statusComboBox.setValue(task.getStatus());

        VBox vbox = new VBox(new Label("Description:"), descriptionField, new Label("Status:"), statusComboBox);
        vbox.setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: white;");
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Task(descriptionField.getText(), statusComboBox.getValue(), task.getCreatedAt(), LocalDateTime.now());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newTask -> {
            String sql = "UPDATE tasks SET description = ?, status = ?, updatedAt = NOW() WHERE username = ? AND description = ?";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newTask.getDescription());
                statement.setString(2, newTask.getStatus());
                statement.setString(3, username);
                statement.setString(4, task.getDescription());
                statement.executeUpdate();
                loadTasks();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not edit task.");
            }
        });
    }

    private void backToDashboard(Stage currentStage) {
        previousStage.show(); // Show the previous Dashboard stage
        currentStage.close(); // Close the current To-Do List stage
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Task {
        private final SimpleStringProperty description;
        private final SimpleStringProperty status;
        private final SimpleObjectProperty<LocalDateTime> createdAt;
        private final SimpleObjectProperty<LocalDateTime> updatedAt;

        public Task(String description, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.description = new SimpleStringProperty(description);
            this.status = new SimpleStringProperty(status);
            this.createdAt = new SimpleObjectProperty<>(createdAt);
            this.updatedAt = new SimpleObjectProperty<>(updatedAt);
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt.get();
        }

        public SimpleObjectProperty<LocalDateTime> createdAtProperty() {
            return createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt.get();
        }

        public SimpleObjectProperty<LocalDateTime> updatedAtProperty() {
            return updatedAt;
        }
    }
}
