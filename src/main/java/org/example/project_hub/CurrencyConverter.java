package org.example.project_hub;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyConverter extends Application {

    private ObservableList<String> currencyCodes;
    private ComboBox<String> fromCurrency;
    private ComboBox<String> toCurrency;
    private TextField amountField;
    private Label resultLabel;
    private Label exchangeRateLabel;
    private Label lastUpdateLabel;
    private Label validationLabel;
    private TableView<ExchangeRate> table;
    private FilteredList<ExchangeRate> filteredRates;
    private String username;
    private Stage dashboardStage;

    public CurrencyConverter(String username, Stage dashboardStage) {
        this.username = username;
        this.dashboardStage = dashboardStage;
    }

    public CurrencyConverter() {
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Currency Converter");

        loadCurrencyCodes();

        fromCurrency = new ComboBox<>(currencyCodes);
        fromCurrency.setOnAction(e -> filterExchangeRates());

        toCurrency = new ComboBox<>(currencyCodes);

        amountField = new TextField();
        amountField.setPromptText("Amount");

        Button convertButton = new Button("Convert");
        convertButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        convertButton.setOnAction(e -> convertCurrency());

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshButton.setOnAction(e -> refreshFilters());

        resultLabel = new Label("Result: ");
        resultLabel.setStyle("-fx-text-fill: white;");
        resultLabel.setOnMouseClicked(e -> copyToClipboard(resultLabel.getText()));

        exchangeRateLabel = new Label("Exchange Rate: ");
        exchangeRateLabel.setStyle("-fx-text-fill: white;");
        exchangeRateLabel.setOnMouseClicked(e -> copyToClipboard(exchangeRateLabel.getText()));

        lastUpdateLabel = new Label();
        lastUpdateLabel.setStyle("-fx-text-fill: white;");
        updateLastUpdateLabel();

        validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setVisible(false);

        // Back button
        Button backButton = new Button("Back to Dashboard");
        backButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> {
            primaryStage.close();
            dashboardStage.show();
        });

        // Settings button for updating exchange rates
        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");
        settingsButton.setOnAction(e -> openLoginWindow());

        // Load the image
        Image speedometerImage = new Image("file:src/main/image/convertCurrency.png");
        ImageView speedometerImageView = new ImageView(speedometerImage);
        speedometerImageView.setFitWidth(100);  // Adjust the size as needed
        speedometerImageView.setPreserveRatio(true);

        // Welcome message
        Label welcomeLabel = new Label("Welcome to Currency Converter, " + username + "!");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        HBox welcomeBox = new HBox(welcomeLabel);
        welcomeBox.setAlignment(Pos.CENTER);

        HBox inputBox = new HBox(10, new Label("From:"), fromCurrency, new Label("To:"), toCurrency, amountField, convertButton, settingsButton, refreshButton, backButton);
        inputBox.setAlignment(Pos.CENTER);

        // Create a VBox to hold all elements
        VBox vbox = new VBox(20, speedometerImageView, welcomeBox, inputBox, validationLabel, exchangeRateLabel, resultLabel, lastUpdateLabel);
        vbox.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #2E2E2E;");

        // Table to display exchange rates
        table = new TableView<>();
        filteredRates = new FilteredList<>(loadExchangeRates(), p -> true);
        table.setItems(filteredRates);

        TableColumn<ExchangeRate, String> fromCurrencyColumn = new TableColumn<>("From Currency");
        fromCurrencyColumn.setCellValueFactory(new PropertyValueFactory<>("fromCurrency"));

        TableColumn<ExchangeRate, String> toCurrencyColumn = new TableColumn<>("To Currency");
        toCurrencyColumn.setCellValueFactory(new PropertyValueFactory<>("toCurrency"));

        TableColumn<ExchangeRate, Double> exchangeRateColumn = new TableColumn<>("Exchange Rate");
        exchangeRateColumn.setCellValueFactory(new PropertyValueFactory<>("exchangeRate"));

        table.getColumns().addAll(fromCurrencyColumn, toCurrencyColumn, exchangeRateColumn);

        VBox tableBox = new VBox(table);
        tableBox.setStyle("-fx-padding: 20; -fx-background-color: #2E2E2E;");
        vbox.getChildren().add(tableBox);

        Scene scene = new Scene(vbox, 800, 600);

        // Load and apply the CSS file
        scene.getStylesheets().add(getClass().getResource("/dark-mode.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadCurrencyCodes() {
        currencyCodes = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT from_currency FROM exchange_rates";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                currencyCodes.add(resultSet.getString("from_currency"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load currency codes.");
        }
    }

    private ObservableList<ExchangeRate> loadExchangeRates() {
        ObservableList<ExchangeRate> rates = FXCollections.observableArrayList();
        String sql = "SELECT from_currency, to_currency, exchange_rate FROM exchange_rates";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String fromCurrency = resultSet.getString("from_currency");
                String toCurrency = resultSet.getString("to_currency");
                double exchangeRate = resultSet.getDouble("exchange_rate");
                rates.add(new ExchangeRate(fromCurrency, toCurrency, exchangeRate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load exchange rates.");
        }
        return rates;
    }

    private void filterExchangeRates() {
        String selectedFromCurrency = fromCurrency.getValue();
        if (selectedFromCurrency != null) {
            filteredRates.setPredicate(exchangeRate -> exchangeRate.getFromCurrency().equals(selectedFromCurrency));
        } else {
            filteredRates.setPredicate(exchangeRate -> true);
        }
    }

    private void refreshFilters() {
        fromCurrency.setValue(null);
        toCurrency.setValue(null);
        amountField.clear();
        filteredRates.setPredicate(exchangeRate -> true);
        validationLabel.setVisible(false);
        resultLabel.setText("Result: ");
        exchangeRateLabel.setText("Exchange Rate: ");
    }

    private void convertCurrency() {
        validationLabel.setVisible(false);
        String from = fromCurrency.getValue();
        String to = toCurrency.getValue();
        String amountText = amountField.getText();

        if (from == null || to == null) {
            validationLabel.setText("Please select both currencies.");
            validationLabel.setVisible(true);
            return;
        }

        if (from.equals(to)) {
            validationLabel.setText("The same currency cannot be converted.");
            validationLabel.setVisible(true);
            return;
        }

        if (amountText.isEmpty()) {
            validationLabel.setText("Amount for conversion cannot be blank.");
            validationLabel.setVisible(true);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            validationLabel.setText("Invalid amount. Please enter a valid number.");
            validationLabel.setVisible(true);
            return;
        }

        if (amount <= 0) {
            validationLabel.setText("Amount must be greater than zero.");
            validationLabel.setVisible(true);
            return;
        }

        double exchangeRate = getExchangeRate(from, to);

        if (exchangeRate == 0.0) {
            double toUSD = getExchangeRate(from, "USD");
            double fromUSD = getExchangeRate("USD", to);

            if (toUSD > 0.0 && fromUSD > 0.0) {
                exchangeRate = toUSD * fromUSD;
            }
        }

        if (exchangeRate > 0.0) {
            exchangeRateLabel.setText(String.format("Exchange Rate: %.4f", exchangeRate));
            double convertedAmount = amount * exchangeRate;
            resultLabel.setText(String.format("Result: %.4f %s", convertedAmount, to));
        } else {
            showAlert(Alert.AlertType.ERROR, "Conversion Error", "No valid exchange rate found for the selected currencies.");
        }
    }

    private double getExchangeRate(String from, String to) {
        String sql = "SELECT exchange_rate FROM exchange_rates WHERE from_currency = ? AND to_currency = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, from);
            statement.setString(2, to);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("exchange_rate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve exchange rate.");
        }
        return 0.0;
    }

    private void updateLastUpdateLabel() {
        String sql = "SELECT MAX(updated_at) AS last_update FROM exchange_rates";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String lastUpdate = resultSet.getString("last_update");
                lastUpdateLabel.setText("Last Update: " + lastUpdate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not retrieve last update time.");
        }
    }

    private void openLoginWindow() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Admin Login");

        Label userIdLabel = new Label("User ID:");
        userIdLabel.setStyle("-fx-text-fill: white;");
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        loginButton.setOnAction(e -> {
            String userId = userIdField.getText();
            String password = passwordField.getText();
            if (authenticateAdmin(userId, password)) {
                loginStage.close();
                openSettingsWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid User ID or Password.");
            }
        });

        VBox vbox = new VBox(10, userIdLabel, userIdField, passwordLabel, passwordField, loginButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #2E2E2E;");
        Scene scene = new Scene(vbox, 300, 200);

        loginStage.setScene(scene);
        loginStage.show();
    }

    private boolean authenticateAdmin(String userId, String password) {
        return "001122".equals(userId) && "123".equals(password);
    }

    private void openSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Exchange Rate Settings");

        TableView<ExchangeRate> settingsTable = new TableView<>();
        settingsTable.setItems(loadExchangeRates());

        TableColumn<ExchangeRate, String> fromCurrencyColumn = new TableColumn<>("From Currency");
        fromCurrencyColumn.setCellValueFactory(new PropertyValueFactory<>("fromCurrency"));

        TableColumn<ExchangeRate, String> toCurrencyColumn = new TableColumn<>("To Currency");
        toCurrencyColumn.setCellValueFactory(new PropertyValueFactory<>("toCurrency"));

        TableColumn<ExchangeRate, Double> exchangeRateColumn = new TableColumn<>("Exchange Rate");
        exchangeRateColumn.setCellValueFactory(new PropertyValueFactory<>("exchangeRate"));

        settingsTable.getColumns().addAll(fromCurrencyColumn, toCurrencyColumn, exchangeRateColumn);

        // Add, Edit, Delete buttons
        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> showAddExchangeRateDialog(settingsTable));

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-font-weight: bold;");
        editButton.setOnAction(e -> showEditExchangeRateDialog(settingsTable));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setOnAction(e -> deleteExchangeRate(settingsTable));

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(10, settingsTable, buttonBox);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #2E2E2E;");
        Scene scene = new Scene(vbox, 600, 400);

        settingsStage.setScene(scene);
        settingsStage.show();
    }

    private void showAddExchangeRateDialog(TableView<ExchangeRate> table) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Exchange Rate");

        ComboBox<String> fromCurrencyField = new ComboBox<>(currencyCodes);
        ComboBox<String> toCurrencyField = new ComboBox<>(currencyCodes);
        TextField exchangeRateField = new TextField();
        exchangeRateField.setPromptText("Exchange Rate");

        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> {
            String fromCurrency = fromCurrencyField.getValue();
            String toCurrency = toCurrencyField.getValue();
            double exchangeRate = Double.parseDouble(exchangeRateField.getText());
            if (fromCurrency == null || toCurrency == null || exchangeRate <= 0) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields.");
                return;
            }

            String sql = "INSERT INTO exchange_rates (from_currency, to_currency, exchange_rate, updated_at) VALUES (?, ?, ?, NOW())";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, fromCurrency);
                statement.setString(2, toCurrency);
                statement.setDouble(3, exchangeRate);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    table.setItems(loadExchangeRates());
                    updateLastUpdateLabel();
                    dialogStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Add Failed", "Failed to add exchange rate.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not add exchange rate.");
            }
        });

        VBox vbox = new VBox(10, new Label("From Currency:"), fromCurrencyField, new Label("To Currency:"), toCurrencyField, new Label("Exchange Rate:"), exchangeRateField, addButton);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #2E2E2E;");
        Scene scene = new Scene(vbox, 300, 250);

        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void showEditExchangeRateDialog(TableView<ExchangeRate> table) {
        ExchangeRate selectedRate = table.getSelectionModel().getSelectedItem();
        if (selectedRate == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an exchange rate to edit.");
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Exchange Rate");

        ComboBox<String> fromCurrencyField = new ComboBox<>(currencyCodes);
        fromCurrencyField.setValue(selectedRate.getFromCurrency());
        ComboBox<String> toCurrencyField = new ComboBox<>(currencyCodes);
        toCurrencyField.setValue(selectedRate.getToCurrency());
        TextField exchangeRateField = new TextField(String.valueOf(selectedRate.getExchangeRate()));
        exchangeRateField.setPromptText("Exchange Rate");

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-font-weight: bold;");
        updateButton.setOnAction(e -> {
            String fromCurrency = fromCurrencyField.getValue();
            String toCurrency = toCurrencyField.getValue();
            double exchangeRate = Double.parseDouble(exchangeRateField.getText());
            if (fromCurrency == null || toCurrency == null || exchangeRate <= 0) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields.");
                return;
            }

            String sql = "UPDATE exchange_rates SET exchange_rate = ?, updated_at = NOW() WHERE from_currency = ? AND to_currency = ?";
            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, exchangeRate);
                statement.setString(2, fromCurrency);
                statement.setString(3, toCurrency);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    table.setItems(loadExchangeRates());
                    updateLastUpdateLabel();
                    dialogStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update exchange rate.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update exchange rate.");
            }
        });

        VBox vbox = new VBox(10, new Label("From Currency:"), fromCurrencyField, new Label("To Currency:"), toCurrencyField, new Label("Exchange Rate:"), exchangeRateField, updateButton);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: #2E2E2E;");
        Scene scene = new Scene(vbox, 300, 250);

        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void deleteExchangeRate(TableView<ExchangeRate> table) {
        ExchangeRate selectedRate = table.getSelectionModel().getSelectedItem();
        if (selectedRate == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select an exchange rate to delete.");
            return;
        }

        String sql = "DELETE FROM exchange_rates WHERE from_currency = ? AND to_currency = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, selectedRate.getFromCurrency());
            statement.setString(2, selectedRate.getToCurrency());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                table.setItems(loadExchangeRates());
                updateLastUpdateLabel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Delete Failed", "Failed to delete exchange rate.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete exchange rate.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
        showAlert(Alert.AlertType.INFORMATION, "Copied to Clipboard", "Text has been copied to clipboard.");
    }

    public static class ExchangeRate {
        private String fromCurrency;
        private String toCurrency;
        private double exchangeRate;

        public ExchangeRate(String fromCurrency, String toCurrency, double exchangeRate) {
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.exchangeRate = exchangeRate;
        }

        public String getFromCurrency() {
            return fromCurrency;
        }

        public String getToCurrency() {
            return toCurrency;
        }

        public double getExchangeRate() {
            return exchangeRate;
        }
    }
}
