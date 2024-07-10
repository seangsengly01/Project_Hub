package org.example.project_hub;

import javafx.beans.property.*;

import java.time.LocalDate;

public class FinanceRecord {

    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty description;
    private final DoubleProperty amount;

    public FinanceRecord(int id, LocalDate date, String description, double amount) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleObjectProperty<>(date);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public FinanceRecord(LocalDate date, String description, double amount) {
        this(0, date, description, amount);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public double getAmount() {
        return amount.get();
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }
}
