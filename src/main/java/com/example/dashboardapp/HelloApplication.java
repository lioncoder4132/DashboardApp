package com.example.dashboardapp;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HelloApplication extends Application {

    private static final String SAVE_FILE = "dashboard_data.txt";

    // --- FINANCIAL DATA STRUCTURES ---
    public static class Transaction {
        private final SimpleStringProperty type;
        private final SimpleStringProperty description;
        private final SimpleStringProperty amount;

        public Transaction(String type, String description, double amount) {
            this.type = new SimpleStringProperty(type);
            this.description = new SimpleStringProperty(description);
            this.amount = new SimpleStringProperty(String.format("$%.2f", amount));
        }

        public String getType() {
            return type.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getAmount() {
            return amount.get();
        }

        public double getNumericAmount() {
            return Double.parseDouble(amount.get().replace("$", ""));
        }
    }

    // --- TO-DO TASK DATA STRUCTURES ---
    public static class AdvancedTask {
        private final SimpleStringProperty description = new SimpleStringProperty();
        private LocalTime dueTime;
        private final CheckBox checkBox = new CheckBox();

        public AdvancedTask(String description, LocalTime dueTime) {
            this.description.set(description);
            this.dueTime = dueTime;
        }

        public void shiftHours(int hours) {
            this.dueTime = this.dueTime.plusHours(hours);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        @Override
        public String toString() {
            String timeString = String.format("[%02d:%02d]", dueTime.getHour(), dueTime.getMinute());
            return timeString + " " + description.get();
        }
    }

    // --- GLOBAL DATA LISTS ---
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private final ObservableList<AdvancedTask> todoList = FXCollections.observableArrayList();

    // Trackers for actual money contributed
    private double totalTithePaid = 0.0;
    private double totalSavingsPaid = 0.0;

    // UI elements that need refreshing
    private Label budgetSummaryLabel;
    private Label contributionTrackerLabel;

    private TextField tithingPercentInput;
    private TextField savingsPercentInput;

    private TextField todoDescInput;
    private ComboBox<Integer> hourPicker;
    private ComboBox<Integer> minutePicker;
    private ListView<AdvancedTask> todoListView;

    private VBox layoutWrapper;
    private TabPane tabPane;

    // Global layout tracking links to fix the background overrides directly
    private VBox financeLayout;
    private VBox todoLayout;

    @Override
    public void start(Stage primaryStage) {
        tabPane = new TabPane();

        // =================================================================
        // ======================= TAB 1: FINANCE ==========================
        // =================================================================
        Tab financeTab = new Tab("Finance Tracker");
        financeLayout = new VBox(10);
        financeLayout.setStyle("-fx-padding: 15;");

        // Percentage Settings Row
        HBox settingsBox = new HBox(10);
        tithingPercentInput = new TextField("10");
        tithingPercentInput.setPrefWidth(50);
        savingsPercentInput = new TextField("20");
        savingsPercentInput.setPrefWidth(50);
        settingsBox.getChildren().addAll(new Label("Tithing Requirement %:"), tithingPercentInput, new Label("  Savings Target %:"), savingsPercentInput);

        // Transaction Entry Row
        HBox entryBox = new HBox(10);
        ComboBox<String> typeDropdown = new ComboBox<>(FXCollections.observableArrayList("Income", "Expense"));
        typeDropdown.setValue("Expense");
        TextField txDescInput = new TextField();
        txDescInput.setPromptText("Description (e.g. Paycheck, Gas)");
        TextField txAmountInput = new TextField();
        txAmountInput.setPromptText("Amount ($)");
        Button addTxBtn = new Button("Log Entry");
        addTxBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        entryBox.getChildren().addAll(typeDropdown, txDescInput, txAmountInput, addTxBtn);

        // Ledger Table Layout
        VBox tableContainer = new VBox(5);
        TableView<Transaction> transactionTable = new TableView<>(transactionList);
        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().type);
        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> cellData.getValue().description);
        TableColumn<Transaction, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(cellData -> cellData.getValue().amount);
        transactionTable.getColumns().addAll(typeCol, descCol, amtCol);

        // 🔥 FORCE THE TABLE CONTAINER FIXED HEIGHT SO WINDOW CANNOT INFLATE WIDE!
        transactionTable.setPrefHeight(160);
        transactionTable.setMaxHeight(160);
        transactionTable.setMinHeight(160);

        // THE NEW DELETE BUTTON LOGIC
        Button deleteTxBtn = new Button("❌ Delete Selected Ledger Entry");
        deleteTxBtn.setStyle(
                "-fx-background-color: #c0392b; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        deleteTxBtn.setOnAction(e -> {
            Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                transactionList.remove(selected);
                recalculateFinance();
                saveData();
            } else {
                showError("Please click on a row in the table first to select it for deletion!");
            }
        });
        tableContainer.getChildren().addAll(transactionTable, deleteTxBtn);

        // Allocation & Payment Tracking Row
        HBox allocationBox = new HBox(10);
        ComboBox<String> allocDropdown = new ComboBox<>(FXCollections.observableArrayList("Tithe Payment", "Savings Deposit"));
        allocDropdown.setValue("Tithe Payment");
        TextField allocAmountInput = new TextField();
        allocAmountInput.setPromptText("Amount ($)");
        Button addAllocBtn = new Button("Log Contribution");
        addAllocBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );

        Button resetAllocBtn = new Button("🔄 Reset Progress");
        resetAllocBtn.setOnAction(e -> {
            totalTithePaid = 0.0;
            totalSavingsPaid = 0.0;
            recalculateFinance();
            saveData();
        });
        allocationBox.getChildren().addAll(allocDropdown, allocAmountInput, addAllocBtn, resetAllocBtn);

        // Summaries
        budgetSummaryLabel = new Label();
        budgetSummaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #3498db;");

        contributionTrackerLabel = new Label();
        contributionTrackerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #27ae60;");

        // Action Logic for General Entries
        addTxBtn.setOnAction(e -> {
            try {
                String type = typeDropdown.getValue();
                String desc = txDescInput.getText().trim();
                double amount = Double.parseDouble(txAmountInput.getText());
                if (desc.isEmpty()) desc = type;

                transactionList.add(new Transaction(type, desc, amount));
                recalculateFinance();
                txDescInput.clear();
                txAmountInput.clear();
                saveData();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid amount number.");
            }
        });

        // Action Logic for Logging Contributions
        addAllocBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(allocAmountInput.getText());
                if (allocDropdown.getValue().equals("Tithe Payment")) {
                    totalTithePaid += amount;
                } else {
                    totalSavingsPaid += amount;
                }
                recalculateFinance();
                allocAmountInput.clear();
                saveData();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid contribution amount.");
            }
        });

        financeLayout.getChildren().addAll(
                new Label("1. Budget Settings:"), settingsBox,
                new Label("2. Log New Income or Expense item:"), entryBox,
                new Label("3. Ledger History (Click a row to select it):"), tableContainer,
                budgetSummaryLabel,
                new Label("4. Fulfill Targets (Log what money you have actively set aside):"), allocationBox,
                contributionTrackerLabel
        );
        financeTab.setContent(financeLayout);
// =================================================================
// ================= TAB 2: TO-DO (CUSTOM SCHEDULE) =================
// =================================================================
        Tab todoTab = new Tab("To-Do List");
        todoLayout = new VBox(10);
        todoLayout.setStyle("-fx-padding: 15;");
        HBox todoEntryBox = new HBox(10);
        todoDescInput = new TextField();
        todoDescInput.setPromptText("Task item description");
        todoDescInput.setPrefWidth(160);
        hourPicker = new ComboBox<>();
        for (int h = 0; h < 24; h++) hourPicker.getItems().add(h);
        hourPicker.setValue(LocalTime.now().getHour());
        minutePicker = new ComboBox<>();
        for (int m = 0; m < 60; m++) minutePicker.getItems().add(m);
        minutePicker.setValue(LocalTime.now().getMinute());
        Button addTodoBtn = new Button("Schedule Task");
        todoEntryBox.getChildren().addAll(todoDescInput, new Label("Time:"), hourPicker, new Label(":"), minutePicker, addTodoBtn);
        todoListView = new ListView<>(todoList);
        todoListView.setCellFactory(CheckBoxListCell.forListView(item -> item.getCheckBox().selectedProperty()));
// 🔥 FORCE THE TO-DO LIST CONTAINER FIXED HEIGHT SO IT FITS THE FRAME perfectly
        todoListView.setPrefHeight(180);
        todoListView.setMaxHeight(180);
        addTodoBtn.setOnAction(e -> {
            String desc = todoDescInput.getText().trim();
            if (!desc.isEmpty()) {
                LocalTime customTime = LocalTime.of(hourPicker.getValue(), minutePicker.getValue());
                todoList.add(new AdvancedTask(desc, customTime));
                todoDescInput.clear();
                todoList.sort((t1, t2) -> t1.dueTime.compareTo(t2.dueTime));
                saveData();
            }
        });
        HBox controlBox = new HBox(10);
        Button shiftForwardBtn = new Button("⏩ Shift All +1 Hour");
        shiftForwardBtn.setOnAction(e -> {
            for (AdvancedTask t : todoList) t.shiftHours(1);
            todoList.sort((t1, t2) -> t1.dueTime.compareTo(t2.dueTime));
            todoListView.refresh();
            saveData();
        });
        Button shiftBackwardBtn = new Button("⏪ Shift All -1 Hour");
        shiftBackwardBtn.setOnAction(e -> {
            for (AdvancedTask t : todoList) t.shiftHours(-1);
            todoList.sort((t1, t2) -> t1.dueTime.compareTo(t2.dueTime));
            todoListView.refresh();
            saveData();
        });
        // 🔥 NEW: The 10-Minute Shift Buttons
        Button shiftForward10Btn = new Button("⏩ +10 Mins");
        shiftForward10Btn.setOnAction(e -> {
            for (AdvancedTask t : todoList) {
                t.dueTime = t.dueTime.plusMinutes(10); // Shifts forward by 10 mins
            }
            todoList.sort((t1, t2) -> t1.dueTime.compareTo(t2.dueTime));
            todoListView.refresh();
            saveData();
        });

        Button shiftBackward10Btn = new Button("⏪ -10 Mins");
        shiftBackward10Btn.setOnAction(e -> {
            for (AdvancedTask t : todoList) {
                t.dueTime = t.dueTime.minusMinutes(10); // Shifts backward by 10 mins
            }
            todoList.sort((t1, t2) -> t1.dueTime.compareTo(t2.dueTime));
            todoListView.refresh();
            saveData();
        });
        Button clearCheckedBtn = new Button("🧹 Clear Checked Tasks");
        clearCheckedBtn.setStyle(
                "-fx-background-color: #c0392b; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand;"
        );
        clearCheckedBtn.setOnAction(e -> {
            todoList.removeIf(task -> task.getCheckBox().isSelected());
            todoListView.refresh();
            saveData();
        });
        // Add the two new 10-minute buttons straight into the horizontal wrapper bar!
        controlBox.getChildren().addAll(shiftForwardBtn, shiftBackwardBtn, shiftForward10Btn, shiftBackward10Btn, clearCheckedBtn);
        todoLayout.getChildren().addAll(
                new Label("Add Scheduled Checklist Item:"), todoEntryBox,
                new Label("Today's Timeline:"), todoListView,
                controlBox
        );
        todoTab.setContent(todoLayout);
// =================================================================
// =================== BOOTSTRAP DATA RECOVERY =====================
// =================================================================
        loadData();
        recalculateFinance();
        tabPane.getTabs().addAll(financeTab, todoTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
// --- LIVE AUTO-REFRESH CLOCK SYSTEM ---
        Label clockLabel = new Label("System Time: Loading...");
        clockLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3498db; -fx-padding: 5 15 5 15;");
        Timeline clockEngine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime now = LocalTime.now();
            clockLabel.setText(String.format("🕒 Live System Time: %02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond()));
            if (todoListView != null) {
                todoListView.refresh();
            }
        }));
        clockEngine.setCycleCount(Animation.INDEFINITE);
        clockEngine.play();
// 1. Combine layouts together into wrapper
        layoutWrapper = new VBox(clockLabel, tabPane);
// 2. Build explicit locked-bounds Scene window!
        Scene scene = new Scene(layoutWrapper, 600, 680);
// 3. Configure targets
        primaryStage.setTitle("Desktop Productivity Dashboard Engine");
        primaryStage.setResizable(false); // 🔥 PREVENTS WINDOW FROM JUMPING LARGER AUTOMATICALLY!
        primaryStage.setOnCloseRequest(event -> saveData());
// 4. Paint and present frame
        primaryStage.setScene(scene);
        primaryStage.show();
// 5. 🔥 RUN COMPACT VARIABLE OVERRIDES!
        applySleekStyle(scene);
    }

    private void recalculateFinance() {
        double totalIncome = 0;
        double totalExpenses = 0;
        for (Transaction t : transactionList) {
            if (t.getType().equals("Income")) totalIncome += t.getNumericAmount();
            if (t.getType().equals("Expense")) totalExpenses += t.getNumericAmount();
        }
        double titheRate = 0.10;
        double savingsRate = 0.20;
        try {
            titheRate = Double.parseDouble(tithingPercentInput.getText()) / 100.0;
            savingsRate = Double.parseDouble(savingsPercentInput.getText()) / 100.0;
        } catch (Exception ignored) {
        }
        double titheTarget = totalIncome * titheRate;
        double savingsTarget = totalIncome * savingsRate;
        double netRemaining = totalIncome - totalExpenses - titheTarget - savingsTarget;
        if (budgetSummaryLabel != null) {
            budgetSummaryLabel.setText(String.format(
                    "Total Income: $%.2f | Total Expenses: $%.2f\nTithe Goal: $%.2f | Savings Goal: $%.2f\nNet Target Remaining: $%.2f",
                    totalIncome, totalExpenses, titheTarget, savingsTarget, netRemaining
            ));
        }
        if (contributionTrackerLabel != null) {
            contributionTrackerLabel.setText(String.format(
                    "ACTUAL TARGET PROGRESS:\n• Tithe Paid: $%.2f / Target: $%.2f\n• Savings Deposited: $%.2f / Target: $%.2f",
                    totalTithePaid, titheTarget, totalSavingsPaid, savingsTarget
            ));
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            writer.println(tithingPercentInput.getText() + "," + savingsPercentInput.getText());
            writer.println(totalTithePaid + "," + totalSavingsPaid);
            writer.println(transactionList.size());
            for (Transaction t : transactionList) {
                writer.println(t.getType() + "," + t.getDescription() + "," + t.getNumericAmount());
            }
            writer.println(todoList.size());
            for (AdvancedTask task : todoList) {
                writer.println(task.description.get() + "," + task.dueTime.toString() + "," + task.getCheckBox().isSelected());
            }
        } catch (IOException e) {
            System.out.println("Error saving dashboard data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String settingsLine = reader.readLine();
            if (settingsLine != null && settingsLine.contains(",")) {
                String[] parts = settingsLine.split(",");
                tithingPercentInput.setText(parts[0]);
                savingsPercentInput.setText(parts[1]);
            }
            String contributionLine = reader.readLine();
            if (contributionLine != null && contributionLine.contains(",")) {
                String[] parts = contributionLine.split(",");
                totalTithePaid = Double.parseDouble(parts[0]);
                totalSavingsPaid = Double.parseDouble(parts[1]);
            }
            String txCountLine = reader.readLine();
            if (txCountLine != null) {
                int txCount = Integer.parseInt(txCountLine);
                for (int i = 0; i < txCount; i++) {
                    String[] parts = reader.readLine().split(",");
                    transactionList.add(new Transaction(parts[0], parts[1], Double.parseDouble(parts[2])));
                }
            }
            String taskCountLine = reader.readLine();
            if (taskCountLine != null) {
                int taskCount = Integer.parseInt(taskCountLine);
                for (int i = 0; i < taskCount; i++) {
                    String[] parts = reader.readLine().split(",");
                    AdvancedTask task = new AdvancedTask(parts[0], LocalTime.parse(parts[1]));
                    task.getCheckBox().setSelected(Boolean.parseBoolean(parts[2]));
                    todoList.add(task);
                }
            }
        } catch (Exception e) {
            System.out.println("No previous profile found or file read error, loading fresh workspace.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 🔥 PRECISE DIRECT VARIABLE METHOD OVERRIDE FOR STABLE SKINNING NATIVELY
    private void applySleekStyle(Scene scene) {
        String darkBackground = "-fx-background-color: #1e1e24; -fx-base: #1e1e24; -fx-control-inner-background: #2a2a35; -fx-text-base-color: #f0f0f5;";
// Paint backgrounds explicitly via variable connections to dodge Gradle's lookup latency
        if (layoutWrapper != null) layoutWrapper.setStyle(darkBackground);
        if (financeLayout != null) financeLayout.setStyle("-fx-background-color: #1e1e24; -fx-padding: 15;");
        if (todoLayout != null) todoLayout.setStyle("-fx-background-color: #1e1e24; -fx-padding: 15;");
        if (tabPane != null) tabPane.setStyle("-fx-background-color: #1e1e24;");
// Force label font structures cleanly
        for (Node n : scene.getRoot().lookupAll(".label")) {
            if (n instanceof Label) {
                Label l = (Label) n;
                if (!l.getStyle().contains("-fx-text-fill")) {
                    l.setStyle("-fx-text-fill: #f0f0f5;");
                }
            }
        }
    }
}