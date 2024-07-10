package org.example.project_hub;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MiniPersonalFinanceTracker extends Application {

    private TableView<FinanceRecord> tableView;
    private DatePicker datePicker;
    private TextField descriptionField;
    private TextField amountField;
    private Label errorMessageLabel;
    private Label totalAmountLabel;
    private TextField searchField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private FilteredList<FinanceRecord> filteredRecords;
    private ObservableList<FinanceRecord> records;
    private String username;
    private FinanceRecord selectedRecord = null;
    private Stage primaryStage;

    public MiniPersonalFinanceTracker(String username, Stage primaryStage) {
        this.username = username;
        this.primaryStage = primaryStage;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;  // Set the primaryStage
        primaryStage.setTitle("Mini Personal Finance Tracker");

        // UI elements
        Image logoImage = new Image("file:src/main/image/finaceNote.png");
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(100);
        logoImageView.setPreserveRatio(true);

        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-padding: 10px; -fx-text-fill: #FFD700;");

        datePicker = new DatePicker();
        datePicker.setEditable(false); // Prevent manual input of the date
        descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        amountField = new TextField();
        amountField.setPromptText("Amount");

        // Error message label
        errorMessageLabel = new Label();
        errorMessageLabel.setStyle("-fx-text-fill: red;");
        errorMessageLabel.setVisible(false);

        totalAmountLabel = new Label();
        totalAmountLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #FFD700;");

        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> addRecord());

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteRecord());

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        editButton.setOnAction(e -> editRecord());

        Button exportButton = new Button("Export to Excel");
        exportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        exportButton.setOnAction(e -> exportToExcel(primaryStage));

        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        backButton.setOnAction(e -> backToDashboard());

        searchField = new TextField();
        searchField.setPromptText("Search by Description");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterRecords());

        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        Button filterButton = new Button("Filter by Date");
        filterButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white;");
        filterButton.setOnAction(e -> filterRecords());

        Button clearFilterButton = new Button("Clear Filter");
        clearFilterButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        clearFilterButton.setOnAction(e -> clearFilter());

        HBox inputBox = new HBox(10, datePicker, descriptionField, amountField, addButton, deleteButton, editButton, exportButton, backButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));

        HBox filterBox = new HBox(10, searchField, startDatePicker, endDatePicker, filterButton, clearFilterButton);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(10));

        tableView = new TableView<>();
        records = FXCollections.observableArrayList();
        tableView.setItems(records);
        tableView.setPrefHeight(400);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<FinanceRecord, Number> numberColumn = new TableColumn<>("No");
        numberColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(cellData.getValue()) + 1));
        numberColumn.setSortable(false);

        TableColumn<FinanceRecord, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        TableColumn<FinanceRecord, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        TableColumn<FinanceRecord, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());

        tableView.getColumns().addAll(numberColumn, dateColumn, descriptionColumn, amountColumn);

        filteredRecords = new FilteredList<>(records, p -> true);

        VBox vbox = new VBox(10, logoImageView, welcomeLabel, inputBox, filterBox, errorMessageLabel, tableView, totalAmountLabel);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: #2E2E2E;");

        Scene scene = new Scene(vbox, 600, 500);
        scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm()); // Apply the CSS file

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Open in full window but not full screen
        primaryStage.setFullScreen(false); // Ensure not full screen
        primaryStage.show();

        loadRecords();
        updateTotalAmount();
    }

    private void backToDashboard() {
        Dashboard dashboard = new Dashboard(username);
        Stage dashboardStage = new Stage();
        dashboard.start(dashboardStage);
        dashboardStage.setMaximized(true);
        dashboardStage.setFullScreen(false); // Ensure not full screen
        primaryStage.close(); // Close the current stage
    }

    private void addRecord() {
        LocalDate date = datePicker.getValue();
        String description = descriptionField.getText();
        double amount;

        if (date == null || description.isEmpty() || amountField.getText().isEmpty()) {
            showError("All fields are required");
            return;
        }

        try {
            amount = parseCurrency(amountField.getText());
            if (amount == 0) return;
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number");
            return;
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO mini_personal_finance (date, description, amount, username) VALUES (?, ?, ?, ?)")) {

            statement.setDate(1, Date.valueOf(date));
            statement.setString(2, description);
            statement.setDouble(3, amount);
            statement.setString(4, username);
            statement.executeUpdate();

            FinanceRecord newRecord = new FinanceRecord(date, description, amount);
            records.add(newRecord);
            tableView.setItems(filteredRecords);
            tableView.refresh(); // Refresh the table to show new data
            clearInputFields();
            updateTotalAmount();
            filterRecords(); // Update filter

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error adding record");
        }
    }

    private double parseCurrency(String currencyInput) {
        try {
            // Assuming the input might have non-digit characters like currency codes or symbols
            String numericPart = currencyInput.replaceAll("[^\\d.]", ""); // Remove non-digits except the decimal point
            return Double.parseDouble(numericPart);
        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
            return 0; // Return 0 or handle as needed
        }
    }

    private void deleteRecord() {
        FinanceRecord selectedRecord = tableView.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showError("No record selected");
            return;
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM mini_personal_finance WHERE id = ?")) {
            statement.setInt(1, selectedRecord.getId());
            statement.executeUpdate();

            records.remove(selectedRecord);
            tableView.setItems(filteredRecords); // Re-set the items to ensure the view is updated
            tableView.refresh(); // Force the table to refresh and show updated data
            updateTotalAmount();
            filterRecords(); // Update filter

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error deleting record");
        }
    }

    private void editRecord() {
        selectedRecord = tableView.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showError("No record selected");
            return;
        }

        datePicker.setValue(selectedRecord.getDate());
        descriptionField.setText(selectedRecord.getDescription());
        amountField.setText(String.valueOf(selectedRecord.getAmount()));

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        updateButton.setOnAction(e -> updateRecord());

        HBox inputBox = new HBox(10, datePicker, descriptionField, amountField, updateButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));

        ((VBox) tableView.getParent()).getChildren().set(3, inputBox);
    }

    private void updateRecord() {
        LocalDate date = datePicker.getValue();
        String description = descriptionField.getText();
        double amount;

        if (date == null || description.isEmpty() || amountField.getText().isEmpty()) {
            showError("All fields are required");
            return;
        }

        try {
            amount = parseCurrency(amountField.getText());
            if (amount == 0) return;
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number");
            return;
        }

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE mini_personal_finance SET date = ?, description = ?, amount = ? WHERE id = ?")) {

            statement.setDate(1, Date.valueOf(date));
            statement.setString(2, description);
            statement.setDouble(3, amount);
            statement.setInt(4, selectedRecord.getId());
            statement.executeUpdate();

            // Reload the application after updating the record
            reloadApplication();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error updating record");
        }
    }

    private void reloadApplication() {
        primaryStage.close(); // Close the current stage
        // Recreate and start the application
        MiniPersonalFinanceTracker newApp = new MiniPersonalFinanceTracker(username, new Stage());
        newApp.start(new Stage());
    }

    private void exportToExcel(Window window) {
        List<FinanceRecord> recordsList = new ArrayList<>(tableView.getItems());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Finance Records");

                // Header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Date");
                headerRow.createCell(2).setCellValue("Description");
                headerRow.createCell(3).setCellValue("Amount");

                // Data rows
                int rowNum = 1;
                for (FinanceRecord record : recordsList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(record.getId());
                    row.createCell(1).setCellValue(record.getDate().toString());
                    row.createCell(2).setCellValue(record.getDescription());
                    row.createCell(3).setCellValue(record.getAmount());
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                showAlert(AlertType.INFORMATION, "Export Successful", "Records have been exported to " + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error exporting records to Excel");
            }
        }
    }

    private void loadRecords() {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM mini_personal_finance WHERE username = ?")) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            records.clear();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                LocalDate date = resultSet.getDate("date").toLocalDate();
                String description = resultSet.getString("description");
                double amount = resultSet.getDouble("amount");

                FinanceRecord record = new FinanceRecord(id, date, description, amount);
                records.add(record);
            }
            tableView.setItems(filteredRecords);
            tableView.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading records");
        }
    }

    private void clearInputFields() {
        datePicker.setValue(null);
        descriptionField.clear();
        amountField.clear();
        errorMessageLabel.setVisible(false);
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTotalAmount() {
        double total = filteredRecords.stream().mapToDouble(FinanceRecord::getAmount).sum();
        totalAmountLabel.setText("Total Amount: $" + String.format("%.2f", total));
    }

    private void filterRecords() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredRecords.setPredicate(record -> {
            boolean matchesDescription = record.getDescription().toLowerCase().contains(searchText);
            boolean matchesDate = true;

            if (startDate != null && endDate != null) {
                matchesDate = (record.getDate().isEqual(startDate) || record.getDate().isAfter(startDate)) &&
                        (record.getDate().isEqual(endDate) || record.getDate().isBefore(endDate));
            } else if (startDate != null) {
                matchesDate = record.getDate().isEqual(startDate) || record.getDate().isAfter(startDate);
            } else if (endDate != null) {
                matchesDate = record.getDate().isEqual(endDate) || record.getDate().isBefore(endDate);
            }

            return matchesDescription && matchesDate;
        });

        tableView.setItems(filteredRecords);
        updateTotalAmount();
    }

    private void clearFilter() {
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        filterRecords();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
