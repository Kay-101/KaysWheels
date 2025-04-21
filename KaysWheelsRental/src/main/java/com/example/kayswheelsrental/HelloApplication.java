package com.example.kayswheelsrental;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HelloApplication extends Application {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_rental";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890";

    private Connection connection;
    private String currentUserRole = "";
    private String currentUserName = "";

    // UI Components shared across tabs
    private TableView<Map<String, String>> vehicleTable = new TableView<>();
    private TableView<Map<String, String>> customerTable = new TableView<>();
    private TableView<Map<String, String>> bookingTable = new TableView<>();
    private TableView<Map<String, String>> paymentTable = new TableView<>();

    // For reports
    private PieChart availabilityChart;
    private BarChart<String, Number> revenueChart;
    private LineChart<String, Number> rentalTrendChart;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialize database connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();

            // Show main menu first
            showMainMenu(primaryStage);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMainMenu(Stage primaryStage) {
        // Create main container with background image
        StackPane root = new StackPane();
        root.setStyle("-fx-background-image: url('/back.jpeg'); " +
                "-fx-background-size: cover;");

        // Semi-transparent overlay
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        root.getChildren().add(overlay);

        // Main content container
        HBox mainContent = new HBox();
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(20));
        mainContent.setSpacing(50);

        // Welcome message on left
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER_LEFT);
        welcomeBox.setPadding(new Insets(50));
        welcomeBox.setMaxWidth(500);

        Text welcomeText1 = new Text("Welcome to Kay's Wheels Rental");
        welcomeText1.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        welcomeText1.setFill(Color.WHITE);

        Text welcomeText2 = new Text("Your trusted partner for all vehicle rental needs. Choose from our wide range of vehicles at affordable prices.");
        welcomeText2.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        welcomeText2.setFill(Color.WHITE);
        welcomeText2.setWrappingWidth(450);

        welcomeBox.getChildren().addAll(welcomeText1, welcomeText2);

        // Login container on right
        VBox loginContainer = new VBox(20);
        loginContainer.setStyle("-fx-background-color: rgba(255,255,255,0.9); " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 30;");
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setMaxWidth(350);

        // Login form
        Text loginTitle = new Text("Login to Your Account");
        loginTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        CheckBox rememberMeCheck = new CheckBox("Remember me");

        HBox signupBox = new HBox(5);
        signupBox.setAlignment(Pos.CENTER);
        Text signupPrompt = new Text("Not registered yet?");
        Hyperlink signupLink = new Hyperlink("Sign up");
        signupBox.getChildren().addAll(signupPrompt, signupLink);

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        loginBtn.setPrefWidth(250);
        loginBtn.setOnAction(e -> {
            if (validateLogin(usernameField.getText(), passwordField.getText())) {
                showMainApplication(primaryStage);
            } else {
                showAlert("Login Failed", "Invalid username or password");
            }
        });

        signupLink.setOnAction(e -> showRegistrationForm(primaryStage));

        loginContainer.getChildren().addAll(
                loginTitle,
                new Label("Username:"),
                usernameField,
                new Label("Password:"),
                passwordField,
                rememberMeCheck,
                loginBtn,
                signupBox
        );

        mainContent.getChildren().addAll(welcomeBox, loginContainer);
        root.getChildren().add(mainContent);

        // Set up scene
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Kay's Wheels Rental");
        primaryStage.show();
    }

    private void showLoginForm(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-image: url('/back.jpeg'); -fx-background-size: cover;");

        // Semi-transparent overlay
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        root.getChildren().add(overlay);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setSpacing(10);

        Text logo = new Text("KW");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        logo.setFill(Color.WHITE);

        Text title = new Text("Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.WHITE);

        header.getChildren().addAll(logo, title);
        root.setTop(header);

        // Login form
        GridPane form = new GridPane();
        form.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 20; -fx-background-radius: 10;");
        form.setAlignment(Pos.CENTER);
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);

        CheckBox rememberMeCheck = new CheckBox("Remember me");

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(200);
        loginBtn.setOnAction(e -> {
            if (validateLogin(usernameField.getText(), passwordField.getText())) {
                showMainApplication(primaryStage);
            } else {
                showAlert("Login Failed", "Invalid username or password");
            }
        });

        HBox signupBox = new HBox(5);
        signupBox.setAlignment(Pos.CENTER);
        Text signupPrompt = new Text("Not registered yet?");
        Hyperlink signupLink = new Hyperlink("Sign up");
        signupBox.getChildren().addAll(signupPrompt, signupLink);

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setPrefWidth(200);
        backBtn.setOnAction(e -> showMainMenu(primaryStage));

        signupLink.setOnAction(e -> showRegistrationForm(primaryStage));

        form.add(new Label("Username:"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Password:"), 0, 1);
        form.add(passwordField, 1, 1);
        form.add(rememberMeCheck, 1, 2);
        form.add(loginBtn, 1, 3);
        form.add(signupBox, 1, 4);
        form.add(backBtn, 1, 5);

        root.setCenter(form);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
    }

    private void showRegistrationForm(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-image: url('/back.jpeg'); -fx-background-size: cover;");

        // Semi-transparent overlay
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        root.getChildren().add(overlay);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setSpacing(10);

        Text logo = new Text("KW");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        logo.setFill(Color.WHITE);

        Text title = new Text("Register New Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.WHITE);

        header.getChildren().addAll(logo, title);
        root.setTop(header);

        // Registration form
        GridPane form = new GridPane();
        form.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding: 20; -fx-background-radius: 10;");
        form.setAlignment(Pos.CENTER);
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(200);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefWidth(200);

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        nameField.setPrefWidth(200);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Admin", "Employee");
        roleCombo.setPromptText("Select Role");
        roleCombo.setPrefWidth(200);

        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(200);
        registerBtn.setOnAction(e -> {
            if (validateRegistration(usernameField.getText(), passwordField.getText(),
                    confirmPasswordField.getText(), nameField.getText())) {
                if (registerUser(usernameField.getText(), passwordField.getText(),
                        nameField.getText(), roleCombo.getValue())) {
                    showAlert("Registration Success", "Account created successfully. Please login.");
                    showLoginForm(primaryStage);
                }
            }
        });

        Button backBtn = new Button("Back to Main Menu");
        backBtn.setPrefWidth(200);
        backBtn.setOnAction(e -> showMainMenu(primaryStage));

        form.add(new Label("Username:"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Password:"), 0, 1);
        form.add(passwordField, 1, 1);
        form.add(new Label("Confirm Password:"), 0, 2);
        form.add(confirmPasswordField, 1, 2);
        form.add(new Label("Full Name:"), 0, 3);
        form.add(nameField, 1, 3);
        form.add(new Label("Role:"), 0, 4);
        form.add(roleCombo, 1, 4);
        form.add(registerBtn, 1, 5);
        form.add(backBtn, 1, 6);

        root.setCenter(form);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
    }

    private boolean validateLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Username and password are required");
            return false;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentUserRole = rs.getString("role");
                currentUserName = rs.getString("name");
                return true;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to verify login: " + e.getMessage());
        }
        return false;
    }

    private boolean validateRegistration(String username, String password, String confirmPassword, String name) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            showAlert("Validation Error", "All fields are required");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match");
            return false;
        }

        if (password.length() < 6) {
            showAlert("Validation Error", "Password must be at least 6 characters");
            return false;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showAlert("Validation Error", "Username already exists");
                return false;
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to check username: " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean registerUser(String username, String password, String name, String role) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (username, password, role, name) VALUES (?, ?, ?, ?)"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, name);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to register user: " + e.getMessage());
            return false;
        }
    }

    private void showMainApplication(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Header with logo and title
        HBox header = createHeader(primaryStage);
        root.setTop(header);

        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Create tabs based on user role
        if (currentUserRole.equals("Admin")) {
            // Admin has access to all tabs
            Tab vehicleTab = createVehicleManagementTab();
            Tab customerTab = createCustomerManagementTab();
            Tab bookingTab = createBookingSystemTab();
            Tab paymentTab = createPaymentBillingTab();
            Tab reportTab = createReportsTab();

            tabPane.getTabs().addAll(vehicleTab, customerTab, bookingTab, paymentTab, reportTab);
        } else {
            // Employee only has access to booking and payment tabs
            Tab bookingTab = createBookingSystemTab();
            Tab paymentTab = createPaymentBillingTab();

            tabPane.getTabs().addAll(bookingTab, paymentTab);
        }

        root.setCenter(tabPane);

        // Create scene
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Kay's Wheels Rental System - " + currentUserName + " (" + currentUserRole + ")");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeader(Stage primaryStage) {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15px;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Logo (using text as placeholder)
        Text logo = new Text("KW");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        logo.setFill(Color.WHITE);

        // Title
        Text title = new Text("Kay's Wheels Rental");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setFill(Color.WHITE);

        // User info
        HBox userBox = new HBox();
        userBox.setAlignment(Pos.CENTER_RIGHT);
        userBox.setSpacing(10);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        Label userLabel = new Label("Logged in as: " + currentUserName + " (" + currentUserRole + ")");
        userLabel.setTextFill(Color.WHITE);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            currentUserRole = "";
            currentUserName = "";
            showMainMenu(primaryStage);
        });

        userBox.getChildren().addAll(userLabel, logoutBtn);
        header.getChildren().addAll(logo, title, userBox);
        return header;
    }

    private Tab createVehicleManagementTab() {
        Tab tab = new Tab("Vehicle Management");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // Title
        Text title = new Text("Vehicle Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Form for adding/updating vehicles
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);

        // Form fields
        TextField vehicleIdField = new TextField();
        vehicleIdField.setPromptText("Auto-generated");
        vehicleIdField.setDisable(true);

        TextField brandField = new TextField();
        brandField.setPromptText("Brand");

        TextField modelField = new TextField();
        modelField.setPromptText("Model");

        // Category radio buttons
        ToggleGroup categoryGroup = new ToggleGroup();
        HBox categoryBox = new HBox(10);
        categoryBox.setAlignment(Pos.CENTER_LEFT);

        RadioButton carRadio = new RadioButton("Car");
        carRadio.setToggleGroup(categoryGroup);
        carRadio.setSelected(true);

        RadioButton bikeRadio = new RadioButton("Bike");
        bikeRadio.setToggleGroup(categoryGroup);

        RadioButton vanRadio = new RadioButton("Van");
        vanRadio.setToggleGroup(categoryGroup);

        RadioButton truckRadio = new RadioButton("Truck");
        truckRadio.setToggleGroup(categoryGroup);

        categoryBox.getChildren().addAll(
                new Label("Category:"), carRadio, bikeRadio, vanRadio, truckRadio
        );

        // Type combobox
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setPromptText("Select Type");

        // Update types based on category selection
        categoryGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == carRadio) {
                typeCombo.getItems().setAll("Sedan", "SUV", "Hatchback", "Convertible", "Coupe");
            } else if (newVal == bikeRadio) {
                typeCombo.getItems().setAll("Sport", "Cruiser", "Touring", "Off-road", "Scooter");
            } else if (newVal == vanRadio) {
                typeCombo.getItems().setAll("Passenger", "Cargo", "Mini", "Full-size");
            } else if (newVal == truckRadio) {
                typeCombo.getItems().setAll("Pickup", "Box Truck", "Flatbed", "Dump Truck");
            }
            typeCombo.getSelectionModel().selectFirst();
        });
        categoryGroup.selectToggle(carRadio); // Trigger initial update

        TextField priceField = new TextField();
        priceField.setPromptText("Price per Day (M)");

        CheckBox availableCheck = new CheckBox("Available");
        availableCheck.setSelected(true);

        // Add form fields to grid
        form.add(new Label("Vehicle ID:"), 0, 0);
        form.add(vehicleIdField, 1, 0);
        form.add(new Label("Brand:"), 0, 1);
        form.add(brandField, 1, 1);
        form.add(new Label("Model:"), 0, 2);
        form.add(modelField, 1, 2);
        form.add(categoryBox, 0, 3, 2, 1);
        form.add(new Label("Type:"), 0, 4);
        form.add(typeCombo, 1, 4);
        form.add(new Label("Price per Day (M):"), 0, 5);
        form.add(priceField, 1, 5);
        form.add(new Label("Availability:"), 0, 6);
        form.add(availableCheck, 1, 6);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            if (validateVehicleForm(brandField, modelField, typeCombo, priceField)) {
                addVehicle(
                        brandField.getText(),
                        modelField.getText(),
                        ((RadioButton)categoryGroup.getSelectedToggle()).getText(),
                        typeCombo.getValue(),
                        priceField.getText(),
                        availableCheck.isSelected()
                );
                clearVehicleForm(vehicleIdField, brandField, modelField, priceField, availableCheck);
                refreshVehicleTable();
            }
        });

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            if (vehicleTable.getSelectionModel().getSelectedItem() != null) {
                if (validateVehicleForm(brandField, modelField, typeCombo, priceField)) {
                    updateVehicle(
                            vehicleTable.getSelectionModel().getSelectedItem().get("id"),
                            brandField.getText(),
                            modelField.getText(),
                            ((RadioButton)categoryGroup.getSelectedToggle()).getText(),
                            typeCombo.getValue(),
                            priceField.getText(),
                            availableCheck.isSelected()
                    );
                    clearVehicleForm(vehicleIdField, brandField, modelField, priceField, availableCheck);
                    refreshVehicleTable();
                }
            } else {
                showAlert("No Selection", "Please select a vehicle to update.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            if (vehicleTable.getSelectionModel().getSelectedItem() != null) {
                deleteVehicle(vehicleTable.getSelectionModel().getSelectedItem().get("id"));
                clearVehicleForm(vehicleIdField, brandField, modelField, priceField, availableCheck);
                refreshVehicleTable();
            } else {
                showAlert("No Selection", "Please select a vehicle to delete.");
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearVehicleForm(vehicleIdField, brandField, modelField, priceField, availableCheck));

        buttonBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);

        // Search
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search vehicles...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchVehicles(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Vehicle table
        setupVehicleTable();

        // Populate table
        refreshVehicleTable();

        // Add selection listener to populate form
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                vehicleIdField.setText(newSelection.get("id"));
                brandField.setText(newSelection.get("brand"));
                modelField.setText(newSelection.get("model"));

                // Set category radio button
                String category = newSelection.get("category");
                if (category.equals("Car")) carRadio.setSelected(true);
                else if (category.equals("Bike")) bikeRadio.setSelected(true);
                else if (category.equals("Van")) vanRadio.setSelected(true);
                else if (category.equals("Truck")) truckRadio.setSelected(true);

                // Set type
                typeCombo.setValue(newSelection.get("type"));

                priceField.setText(newSelection.get("price"));
                availableCheck.setSelected(newSelection.get("available").equals("Yes"));
            }
        });

        vbox.getChildren().addAll(title, form, buttonBox, searchBox, vehicleTable);
        tab.setContent(vbox);
        return tab;
    }

    private boolean validateVehicleForm(TextField brand, TextField model, ComboBox<String> type, TextField price) {
        if (brand.getText().isEmpty()) {
            showAlert("Validation Error", "Brand is required.");
            return false;
        }
        if (model.getText().isEmpty()) {
            showAlert("Validation Error", "Model is required.");
            return false;
        }
        if (type.getValue() == null) {
            showAlert("Validation Error", "Type is required.");
            return false;
        }
        if (price.getText().isEmpty() || !price.getText().matches("\\d+(\\.\\d+)?")) {
            showAlert("Validation Error", "Valid price is required.");
            return false;
        }
        return true;
    }

    private void clearVehicleForm(TextField id, TextField brand, TextField model, TextField price, CheckBox available) {
        id.clear();
        brand.clear();
        model.clear();
        price.clear();
        available.setSelected(true);
        vehicleTable.getSelectionModel().clearSelection();
    }

    private void setupVehicleTable() {
        vehicleTable.getColumns().clear();

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id")));

        TableColumn<Map<String, String>, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("brand")));

        TableColumn<Map<String, String>, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("model")));

        TableColumn<Map<String, String>, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category")));

        TableColumn<Map<String, String>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("type")));

        TableColumn<Map<String, String>, String> priceCol = new TableColumn<>("Price/Day (M)");
        priceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("price")));

        TableColumn<Map<String, String>, String> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("available")));

        vehicleTable.getColumns().addAll(idCol, brandCol, modelCol, categoryCol, typeCol, priceCol, availableCol);
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshVehicleTable() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM vehicles");

            vehicleTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> vehicle = new HashMap<>();
                vehicle.put("id", rs.getString("id"));
                vehicle.put("brand", rs.getString("brand"));
                vehicle.put("model", rs.getString("model"));
                vehicle.put("category", rs.getString("category"));
                vehicle.put("type", rs.getString("type"));
                vehicle.put("price", String.format("M%.2f", rs.getDouble("price_per_day")));
                vehicle.put("available", rs.getBoolean("available") ? "Yes" : "No");
                vehicleTable.getItems().add(vehicle);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchVehicles(String query) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM vehicles WHERE brand LIKE ? OR model LIKE ? OR category LIKE ? OR type LIKE ?"
            );
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            stmt.setString(4, "%" + query + "%");

            ResultSet rs = stmt.executeQuery();

            vehicleTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> vehicle = new HashMap<>();
                vehicle.put("id", rs.getString("id"));
                vehicle.put("brand", rs.getString("brand"));
                vehicle.put("model", rs.getString("model"));
                vehicle.put("category", rs.getString("category"));
                vehicle.put("type", rs.getString("type"));
                vehicle.put("price", String.format("M%.2f", rs.getDouble("price_per_day")));
                vehicle.put("available", rs.getBoolean("available") ? "Yes" : "No");
                vehicleTable.getItems().add(vehicle);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to search vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addVehicle(String brand, String model, String category, String type, String price, boolean available) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO vehicles (brand, model, category, type, price_per_day, available) VALUES (?, ?, ?, ?, ?, ?)"
            );
            stmt.setString(1, brand);
            stmt.setString(2, model);
            stmt.setString(3, category);
            stmt.setString(4, type);
            stmt.setDouble(5, Double.parseDouble(price));
            stmt.setBoolean(6, available);

            stmt.executeUpdate();
            showAlert("Success", "Vehicle added successfully.");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateVehicle(String id, String brand, String model, String category, String type, String price, boolean available) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE vehicles SET brand=?, model=?, category=?, type=?, price_per_day=?, available=? WHERE id=?"
            );
            stmt.setString(1, brand);
            stmt.setString(2, model);
            stmt.setString(3, category);
            stmt.setString(4, type);
            stmt.setDouble(5, Double.parseDouble(price));
            stmt.setBoolean(6, available);
            stmt.setInt(7, Integer.parseInt(id));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Vehicle updated successfully.");
            } else {
                showAlert("Error", "No vehicle found with ID: " + id);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteVehicle(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Vehicle");
        alert.setContentText("Are you sure you want to delete this vehicle?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                PreparedStatement stmt = connection.prepareStatement("DELETE FROM vehicles WHERE id=?");
                stmt.setInt(1, Integer.parseInt(id));

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Success", "Vehicle deleted successfully.");
                } else {
                    showAlert("Error", "No vehicle found with ID: " + id);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete vehicle: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Tab createCustomerManagementTab() {
        Tab tab = new Tab("Customer Management");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // Title
        Text title = new Text("Customer Management");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Form for adding/updating customers
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);

        // Form fields
        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Auto-generated");
        customerIdField.setDisable(true);

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact (8 digits)");

        TextField licenseField = new TextField();
        licenseField.setPromptText("Driving License (12 characters)");

        // Add form fields to grid
        form.add(new Label("Customer ID:"), 0, 0);
        form.add(customerIdField, 1, 0);
        form.add(new Label("Name:"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Contact:"), 0, 2);
        form.add(contactField, 1, 2);
        form.add(new Label("Driving License:"), 0, 3);
        form.add(licenseField, 1, 3);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            if (validateCustomerForm(nameField, contactField, licenseField)) {
                addCustomer(
                        nameField.getText(),
                        contactField.getText(),
                        licenseField.getText()
                );
                clearCustomerForm(customerIdField, nameField, contactField, licenseField);
                refreshCustomerTable();
            }
        });

        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            if (customerTable.getSelectionModel().getSelectedItem() != null) {
                if (validateCustomerForm(nameField, contactField, licenseField)) {
                    updateCustomer(
                            customerTable.getSelectionModel().getSelectedItem().get("id"),
                            nameField.getText(),
                            contactField.getText(),
                            licenseField.getText()
                    );
                    clearCustomerForm(customerIdField, nameField, contactField, licenseField);
                    refreshCustomerTable();
                }
            } else {
                showAlert("No Selection", "Please select a customer to update.");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            if (customerTable.getSelectionModel().getSelectedItem() != null) {
                deleteCustomer(customerTable.getSelectionModel().getSelectedItem().get("id"));
                clearCustomerForm(customerIdField, nameField, contactField, licenseField);
                refreshCustomerTable();
            } else {
                showAlert("No Selection", "Please select a customer to delete.");
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearCustomerForm(customerIdField, nameField, contactField, licenseField));

        buttonBox.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);

        // Search
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search customers...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchCustomers(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Customer table
        setupCustomerTable();

        // Populate table
        refreshCustomerTable();

        // Add selection listener to populate form
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                customerIdField.setText(newSelection.get("id"));
                nameField.setText(newSelection.get("name"));
                contactField.setText(newSelection.get("contact"));
                licenseField.setText(newSelection.get("license"));
            }
        });

        vbox.getChildren().addAll(title, form, buttonBox, searchBox, customerTable);
        tab.setContent(vbox);
        return tab;
    }

    private boolean validateCustomerForm(TextField name, TextField contact, TextField license) {
        if (name.getText().isEmpty()) {
            showAlert("Validation Error", "Name is required.");
            return false;
        }
        if (contact.getText().isEmpty() || !contact.getText().matches("\\d{8}")) {
            showAlert("Validation Error", "Valid 8-digit contact number is required.");
            return false;
        }
        if (license.getText().isEmpty() || license.getText().length() != 12) {
            showAlert("Validation Error", "Valid 12-character driving license is required.");
            return false;
        }
        return true;
    }

    private void clearCustomerForm(TextField id, TextField name, TextField contact, TextField license) {
        id.clear();
        name.clear();
        contact.clear();
        license.clear();
        customerTable.getSelectionModel().clearSelection();
    }

    private void setupCustomerTable() {
        customerTable.getColumns().clear();

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id")));

        TableColumn<Map<String, String>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("name")));

        TableColumn<Map<String, String>, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("contact")));

        TableColumn<Map<String, String>, String> licenseCol = new TableColumn<>("Driving License");
        licenseCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("license")));

        customerTable.getColumns().addAll(idCol, nameCol, contactCol, licenseCol);
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshCustomerTable() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM customers");

            customerTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> customer = new HashMap<>();
                customer.put("id", rs.getString("id"));
                customer.put("name", rs.getString("name"));
                customer.put("contact", rs.getString("contact"));
                customer.put("license", rs.getString("driving_license"));
                customerTable.getItems().add(customer);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchCustomers(String query) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM customers WHERE name LIKE ? OR contact LIKE ? OR driving_license LIKE ?"
            );
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");

            ResultSet rs = stmt.executeQuery();

            customerTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> customer = new HashMap<>();
                customer.put("id", rs.getString("id"));
                customer.put("name", rs.getString("name"));
                customer.put("contact", rs.getString("contact"));
                customer.put("license", rs.getString("driving_license"));
                customerTable.getItems().add(customer);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to search customers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addCustomer(String name, String contact, String license) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO customers (name, contact, driving_license) VALUES (?, ?, ?)"
            );
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, license);

            stmt.executeUpdate();
            showAlert("Success", "Customer added successfully.");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCustomer(String id, String name, String contact, String license) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE customers SET name=?, contact=?, driving_license=? WHERE id=?"
            );
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, license);
            stmt.setInt(4, Integer.parseInt(id));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Customer updated successfully.");
            } else {
                showAlert("Error", "No customer found with ID: " + id);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCustomer(String id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete this customer?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                PreparedStatement stmt = connection.prepareStatement("DELETE FROM customers WHERE id=?");
                stmt.setInt(1, Integer.parseInt(id));

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Success", "Customer deleted successfully.");
                } else {
                    showAlert("Error", "No customer found with ID: " + id);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete customer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Tab createBookingSystemTab() {
        Tab tab = new Tab("Booking System");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // Title
        Text title = new Text("Booking System");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Form for creating/modifying bookings
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);

        // Form fields
        TextField bookingIdField = new TextField();
        bookingIdField.setPromptText("Auto-generated");
        bookingIdField.setDisable(true);

        // Customer combo
        ComboBox<String> customerCombo = new ComboBox<>();
        customerCombo.setPromptText("Select Customer");
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM customers");
            while (rs.next()) {
                customerCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load customers: " + e.getMessage());
            e.printStackTrace();
        }

        // Vehicle combo (only available vehicles)
        ComboBox<String> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, brand, model FROM vehicles WHERE available=true");
            while (rs.next()) {
                vehicleCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("brand") + " " + rs.getString("model"));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load vehicles: " + e.getMessage());
            e.printStackTrace();
        }

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now().plusDays(1));

        TextField statusField = new TextField();
        statusField.setPromptText("Status");
        statusField.setDisable(true);

        // Add form fields to grid
        form.add(new Label("Booking ID:"), 0, 0);
        form.add(bookingIdField, 1, 0);
        form.add(new Label("Customer:"), 0, 1);
        form.add(customerCombo, 1, 1);
        form.add(new Label("Vehicle:"), 0, 2);
        form.add(vehicleCombo, 1, 2);
        form.add(new Label("Start Date:"), 0, 3);
        form.add(startDatePicker, 1, 3);
        form.add(new Label("End Date:"), 0, 4);
        form.add(endDatePicker, 1, 4);
        form.add(new Label("Status:"), 0, 5);
        form.add(statusField, 1, 5);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button bookBtn = new Button("Book");
        bookBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        bookBtn.setOnAction(e -> {
            if (validateBookingForm(customerCombo, vehicleCombo, startDatePicker, endDatePicker)) {
                bookVehicle(
                        customerCombo.getValue().split(" - ")[0],
                        vehicleCombo.getValue().split(" - ")[0],
                        startDatePicker.getValue(),
                        endDatePicker.getValue()
                );
                clearBookingForm(bookingIdField, customerCombo, vehicleCombo, startDatePicker, endDatePicker, statusField);
                refreshBookingTable();
                refreshVehicleTable(); // Update availability
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        cancelBtn.setOnAction(e -> {
            if (bookingTable.getSelectionModel().getSelectedItem() != null) {
                cancelBooking(bookingTable.getSelectionModel().getSelectedItem().get("id"));
                clearBookingForm(bookingIdField, customerCombo, vehicleCombo, startDatePicker, endDatePicker, statusField);
                refreshBookingTable();
                refreshVehicleTable(); // Update availability
            } else {
                showAlert("No Selection", "Please select a booking to cancel.");
            }
        });

        Button completeBtn = new Button("Complete");
        completeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        completeBtn.setOnAction(e -> {
            if (bookingTable.getSelectionModel().getSelectedItem() != null) {
                completeBooking(bookingTable.getSelectionModel().getSelectedItem().get("id"));
                clearBookingForm(bookingIdField, customerCombo, vehicleCombo, startDatePicker, endDatePicker, statusField);
                refreshBookingTable();
                refreshVehicleTable(); // Update availability
            } else {
                showAlert("No Selection", "Please select a booking to complete.");
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearBookingForm(bookingIdField, customerCombo, vehicleCombo, startDatePicker, endDatePicker, statusField));

        buttonBox.getChildren().addAll(bookBtn, cancelBtn, completeBtn, clearBtn);

        // Search
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search bookings...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchBookings(searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Booking table
        setupBookingTable();

        // Populate table
        refreshBookingTable();

        // Add selection listener to populate form
        bookingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                bookingIdField.setText(newSelection.get("id"));

                // Set customer
                for (String item : customerCombo.getItems()) {
                    if (item.startsWith(newSelection.get("customer_id") + " - ")) {
                        customerCombo.setValue(item);
                        break;
                    }
                }

                // Set vehicle
                for (String item : vehicleCombo.getItems()) {
                    if (item.startsWith(newSelection.get("vehicle_id") + " - ")) {
                        vehicleCombo.setValue(item);
                        break;
                    }
                }

                startDatePicker.setValue(LocalDate.parse(newSelection.get("start_date")));
                endDatePicker.setValue(LocalDate.parse(newSelection.get("end_date")));
                statusField.setText(newSelection.get("status"));
            }
        });

        vbox.getChildren().addAll(title, form, buttonBox, searchBox, bookingTable);
        tab.setContent(vbox);
        return tab;
    }

    private boolean validateBookingForm(ComboBox<String> customer, ComboBox<String> vehicle, DatePicker startDate, DatePicker endDate) {
        if (customer.getValue() == null) {
            showAlert("Validation Error", "Customer is required.");
            return false;
        }
        if (vehicle.getValue() == null) {
            showAlert("Validation Error", "Vehicle is required.");
            return false;
        }
        if (startDate.getValue() == null || endDate.getValue() == null ||
                startDate.getValue().isAfter(endDate.getValue())) {
            showAlert("Validation Error", "Valid start and end dates are required.");
            return false;
        }
        return true;
    }

    private void clearBookingForm(TextField id, ComboBox<String> customer, ComboBox<String> vehicle,
                                  DatePicker startDate, DatePicker endDate, TextField status) {
        id.clear();
        customer.getSelectionModel().clearSelection();
        vehicle.getSelectionModel().clearSelection();
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now().plusDays(1));
        status.clear();
        bookingTable.getSelectionModel().clearSelection();
    }

    private void setupBookingTable() {
        bookingTable.getColumns().clear();

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id")));

        TableColumn<Map<String, String>, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("customer")));

        TableColumn<Map<String, String>, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("vehicle")));

        TableColumn<Map<String, String>, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("start_date")));

        TableColumn<Map<String, String>, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("end_date")));

        TableColumn<Map<String, String>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("status")));

        bookingTable.getColumns().addAll(idCol, customerCol, vehicleCol, startDateCol, endDateCol, statusCol);
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshBookingTable() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT b.id, c.name as customer, CONCAT(v.brand,' ', v.model) as vehicle, " +
                            "b.start_date, b.end_date, b.status, b.customer_id, b.vehicle_id " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id"
            );

            bookingTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> booking = new HashMap<>();
                booking.put("id", rs.getString("id"));
                booking.put("customer", rs.getString("customer"));
                booking.put("customer_id", rs.getString("customer_id"));
                booking.put("vehicle", rs.getString("vehicle"));
                booking.put("vehicle_id", rs.getString("vehicle_id"));
                booking.put("start_date", rs.getString("start_date"));
                booking.put("end_date", rs.getString("end_date"));
                booking.put("status", rs.getString("status"));
                bookingTable.getItems().add(booking);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchBookings(String query) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT b.id, c.name as customer, v.brand || ' ' || v.model as vehicle, " +
                            "b.start_date, b.end_date, b.status, b.customer_id, b.vehicle_id " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "WHERE c.name LIKE ? OR v.brand LIKE ? OR v.model LIKE ? OR b.status LIKE ?"
            );
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            stmt.setString(4, "%" + query + "%");

            ResultSet rs = stmt.executeQuery();

            bookingTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> booking = new HashMap<>();
                booking.put("id", rs.getString("id"));
                booking.put("customer", rs.getString("customer"));
                booking.put("customer_id", rs.getString("customer_id"));
                booking.put("vehicle", rs.getString("vehicle"));
                booking.put("vehicle_id", rs.getString("vehicle_id"));
                booking.put("start_date", rs.getString("start_date"));
                booking.put("end_date", rs.getString("end_date"));
                booking.put("status", rs.getString("status"));
                bookingTable.getItems().add(booking);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to search bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bookVehicle(String customerId, String vehicleId, LocalDate startDate, LocalDate endDate) {
        try {
            // Check if vehicle is available
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT available FROM vehicles WHERE id=?"
            );
            checkStmt.setInt(1, Integer.parseInt(vehicleId));
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getBoolean("available")) {
                // Start transaction
                connection.setAutoCommit(false);

                try {
                    // Create booking
                    PreparedStatement bookingStmt = connection.prepareStatement(
                            "INSERT INTO bookings (customer_id, vehicle_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    bookingStmt.setInt(1, Integer.parseInt(customerId));
                    bookingStmt.setInt(2, Integer.parseInt(vehicleId));
                    bookingStmt.setDate(3, Date.valueOf(startDate));
                    bookingStmt.setDate(4, Date.valueOf(endDate));
                    bookingStmt.setString(5, "Booked");
                    bookingStmt.executeUpdate();

                    // Update vehicle availability
                    PreparedStatement vehicleStmt = connection.prepareStatement(
                            "UPDATE vehicles SET available=false WHERE id=?"
                    );
                    vehicleStmt.setInt(1, Integer.parseInt(vehicleId));
                    vehicleStmt.executeUpdate();

                    // Commit transaction
                    connection.commit();
                    showAlert("Success", "Vehicle booked successfully.");
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            } else {
                showAlert("Error", "Selected vehicle is not available.");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to book vehicle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cancelBooking(String bookingId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancellation");
        alert.setHeaderText("Cancel Booking");
        alert.setContentText("Are you sure you want to cancel this booking?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Start transaction
                connection.setAutoCommit(false);

                try {
                    // Get vehicle ID from booking
                    PreparedStatement getStmt = connection.prepareStatement(
                            "SELECT vehicle_id FROM bookings WHERE id=?"
                    );
                    getStmt.setInt(1, Integer.parseInt(bookingId));
                    ResultSet rs = getStmt.executeQuery();

                    if (rs.next()) {
                        int vehicleId = rs.getInt("vehicle_id");

                        // Update booking status
                        PreparedStatement bookingStmt = connection.prepareStatement(
                                "UPDATE bookings SET status='Cancelled' WHERE id=?"
                        );
                        bookingStmt.setInt(1, Integer.parseInt(bookingId));
                        bookingStmt.executeUpdate();

                        // Update vehicle availability
                        PreparedStatement vehicleStmt = connection.prepareStatement(
                                "UPDATE vehicles SET available=true WHERE id=?"
                        );
                        vehicleStmt.setInt(1, vehicleId);
                        vehicleStmt.executeUpdate();

                        // Commit transaction
                        connection.commit();
                        showAlert("Success", "Booking cancelled successfully.");
                    } else {
                        connection.rollback();
                        showAlert("Error", "No booking found with ID: " + bookingId);
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to cancel booking: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void completeBooking(String bookingId) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            try {
                // Get booking details
                PreparedStatement getStmt = connection.prepareStatement(
                        "SELECT vehicle_id FROM bookings WHERE id=?"
                );
                getStmt.setInt(1, Integer.parseInt(bookingId));
                ResultSet rs = getStmt.executeQuery();

                if (rs.next()) {
                    int vehicleId = rs.getInt("vehicle_id");

                    // Update booking status
                    PreparedStatement bookingStmt = connection.prepareStatement(
                            "UPDATE bookings SET status='Completed' WHERE id=?"
                    );
                    bookingStmt.setInt(1, Integer.parseInt(bookingId));
                    bookingStmt.executeUpdate();

                    // Update vehicle availability
                    PreparedStatement vehicleStmt = connection.prepareStatement(
                            "UPDATE vehicles SET available=true WHERE id=?"
                    );
                    vehicleStmt.setInt(1, vehicleId);
                    vehicleStmt.executeUpdate();

                    // Commit transaction
                    connection.commit();
                    showAlert("Success", "Booking completed successfully.");
                } else {
                    connection.rollback();
                    showAlert("Error", "No booking found with ID: " + bookingId);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to complete booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Tab createPaymentBillingTab() {
        Tab tab = new Tab("Payment & Billing");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // Title
        Text title = new Text("Payment & Billing");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Form for payments
        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);

        // Form fields
        TextField paymentIdField = new TextField();
        paymentIdField.setPromptText("Auto-generated");
        paymentIdField.setDisable(true);

        // Booking combo (only active bookings)
        ComboBox<String> bookingCombo = new ComboBox<>();
        bookingCombo.setPromptText("Select Booking");
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT b.id, c.name as customer, v.brand || ' ' || v.model as vehicle " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "WHERE b.status = 'Booked'"
            );
            while (rs.next()) {
                bookingCombo.getItems().add(rs.getInt("id") + " - " + rs.getString("customer") + " - " + rs.getString("vehicle"));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load bookings: " + e.getMessage());
            e.printStackTrace();
        }

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (M)");

        DatePicker paymentDatePicker = new DatePicker();
        paymentDatePicker.setValue(LocalDate.now());

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("Cash", "Credit Card", "Online");
        methodCombo.setValue("Cash");

        TextField statusField = new TextField();
        statusField.setPromptText("Status");
        statusField.setDisable(true);

        // Add form fields to grid
        form.add(new Label("Payment ID:"), 0, 0);
        form.add(paymentIdField, 1, 0);
        form.add(new Label("Booking:"), 0, 1);
        form.add(bookingCombo, 1, 1);
        form.add(new Label("Amount (M):"), 0, 2);
        form.add(amountField, 1, 2);
        form.add(new Label("Payment Date:"), 0, 3);
        form.add(paymentDatePicker, 1, 3);
        form.add(new Label("Method:"), 0, 4);
        form.add(methodCombo, 1, 4);
        form.add(new Label("Status:"), 0, 5);
        form.add(statusField, 1, 5);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button calculateBtn = new Button("Calculate");
        calculateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        calculateBtn.setOnAction(e -> {
            if (bookingCombo.getValue() != null) {
                calculateAmount(bookingCombo.getValue().split(" - ")[0], amountField);
            } else {
                showAlert("No Selection", "Please select a booking to calculate.");
            }
        });

        Button payBtn = new Button("Pay");
        payBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        payBtn.setOnAction(e -> {
            if (validatePaymentForm(bookingCombo, amountField)) {
                processPayment(
                        bookingCombo.getValue().split(" - ")[0],
                        amountField.getText(),
                        paymentDatePicker.getValue(),
                        methodCombo.getValue()
                );
                clearPaymentForm(paymentIdField, bookingCombo, amountField, paymentDatePicker, methodCombo, statusField);
                refreshPaymentTable();
            }
        });

        Button invoiceBtn = new Button("Generate Invoice");
        invoiceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        invoiceBtn.setOnAction(e -> {
            Map<String, String> selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
            if (selectedPayment != null) {
                generateInvoice(selectedPayment.get("id"));
            } else {
                showAlert("No Selection", "Please select a payment to generate invoice.");
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> clearPaymentForm(paymentIdField, bookingCombo, amountField, paymentDatePicker, methodCombo, statusField));

        buttonBox.getChildren().addAll(calculateBtn, payBtn, invoiceBtn, clearBtn);

        // Payment table
        setupPaymentTable();

        // Populate table
        refreshPaymentTable();

        // Add selection listener to populate form
        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                paymentIdField.setText(newSelection.get("id"));

                // Set booking
                for (String item : bookingCombo.getItems()) {
                    if (item.startsWith(newSelection.get("booking_id") + " - ")) {
                        bookingCombo.setValue(item);
                        break;
                    }
                }

                amountField.setText(newSelection.get("amount"));
                paymentDatePicker.setValue(LocalDate.parse(newSelection.get("payment_date")));
                methodCombo.setValue(newSelection.get("method"));
                statusField.setText(newSelection.get("status"));
            }
        });

        vbox.getChildren().addAll(title, form, buttonBox, paymentTable);
        tab.setContent(vbox);
        return tab;
    }

    private boolean validatePaymentForm(ComboBox<String> booking, TextField amount) {
        if (booking.getValue() == null) {
            showAlert("Validation Error", "Booking is required.");
            return false;
        }
        if (amount.getText().isEmpty() || !amount.getText().matches("\\d+(\\.\\d+)?")) {
            showAlert("Validation Error", "Valid amount is required.");
            return false;
        }
        return true;
    }

    private void calculateAmount(String bookingId, TextField amountField) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT b.start_date, b.end_date, v.price_per_day " +
                            "FROM bookings b " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "WHERE b.id=?"
            );
            stmt.setInt(1, Integer.parseInt(bookingId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double pricePerDay = rs.getDouble("price_per_day");

                long days = ChronoUnit.DAYS.between(startDate, endDate);
                double total = days * pricePerDay;

                amountField.setText(String.format("%.2f", total));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to calculate amount: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearPaymentForm(TextField id, ComboBox<String> booking, TextField amount,
                                  DatePicker paymentDate, ComboBox<String> method, TextField status) {
        id.clear();
        booking.getSelectionModel().clearSelection();
        amount.clear();
        paymentDate.setValue(LocalDate.now());
        method.setValue("Cash");
        status.clear();
        paymentTable.getSelectionModel().clearSelection();
    }

    private void setupPaymentTable() {
        paymentTable.getColumns().clear();

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id")));

        TableColumn<Map<String, String>, String> bookingCol = new TableColumn<>("Booking");
        bookingCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("booking")));

        TableColumn<Map<String, String>, String> amountCol = new TableColumn<>("Amount (M)");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("amount")));

        TableColumn<Map<String, String>, String> dateCol = new TableColumn<>("Payment Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("payment_date")));

        TableColumn<Map<String, String>, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("method")));

        TableColumn<Map<String, String>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("status")));

        paymentTable.getColumns().addAll(idCol, bookingCol, amountCol, dateCol, methodCol, statusCol);
        paymentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshPaymentTable() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT p.id, b.id as booking_id, c.name || ' - ' || v.brand || ' ' || v.model as booking, " +
                            "p.amount, p.payment_date, p.method, p.status " +
                            "FROM payments p " +
                            "JOIN bookings b ON p.booking_id = b.id " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id"
            );

            paymentTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> payment = new HashMap<>();
                payment.put("id", rs.getString("id"));
                payment.put("booking_id", rs.getString("booking_id"));
                payment.put("booking", rs.getString("booking"));
                payment.put("amount", String.format("M%.2f", rs.getDouble("amount")));
                payment.put("payment_date", rs.getString("payment_date"));
                payment.put("method", rs.getString("method"));
                payment.put("status", rs.getString("status"));
                paymentTable.getItems().add(payment);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processPayment(String bookingId, String amount, LocalDate paymentDate, String method) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            try {
                // Create payment
                PreparedStatement paymentStmt = connection.prepareStatement(
                        "INSERT INTO payments (booking_id, amount, payment_date, method, status) VALUES (?, ?, ?, ?, ?)"
                );
                paymentStmt.setInt(1, Integer.parseInt(bookingId));
                paymentStmt.setDouble(2, Double.parseDouble(amount));
                paymentStmt.setDate(3, Date.valueOf(paymentDate));
                paymentStmt.setString(4, method);
                paymentStmt.setString(5, "Paid");
                paymentStmt.executeUpdate();

                // Update booking status
                PreparedStatement bookingStmt = connection.prepareStatement(
                        "UPDATE bookings SET status='Paid' WHERE id=?"
                );
                bookingStmt.setInt(1, Integer.parseInt(bookingId));
                bookingStmt.executeUpdate();

                // Commit transaction
                connection.commit();
                showAlert("Success", "Payment processed successfully.");
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to process payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateInvoice(String paymentId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT p.id, p.amount, p.payment_date, p.method, " +
                            "c.name as customer, c.contact, " +
                            "v.brand, v.model, v.price_per_day, " +
                            "b.start_date, b.end_date " +
                            "FROM payments p " +
                            "JOIN bookings b ON p.booking_id = b.id " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "WHERE p.id=?"
            );
            stmt.setInt(1, Integer.parseInt(paymentId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Create invoice dialog
                Dialog<Void> invoiceDialog = new Dialog<>();
                invoiceDialog.setTitle("Invoice");
                invoiceDialog.setHeaderText("Kay's Wheels Rental - Invoice #" + paymentId);

                // Create invoice content
                VBox content = new VBox(10);
                content.setPadding(new Insets(20));

                // Logo and header
                HBox header = new HBox();
                header.setAlignment(Pos.CENTER);
                header.setSpacing(10);

                Text logo = new Text("KW");
                logo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                logo.setFill(Color.DARKBLUE);

                Text invoiceTitle = new Text("INVOICE");
                invoiceTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                invoiceTitle.setFill(Color.DARKBLUE);

                header.getChildren().addAll(logo, invoiceTitle);

                // Invoice details
                GridPane details = new GridPane();
                details.setHgap(10);
                details.setVgap(5);

                details.add(new Label("Invoice #:"), 0, 0);
                details.add(new Label(rs.getString("id")), 1, 0);
                details.add(new Label("Date:"), 0, 1);
                details.add(new Label(rs.getDate("payment_date").toString()), 1, 1);
                details.add(new Label("Customer:"), 0, 2);
                details.add(new Label(rs.getString("customer")), 1, 2);
                details.add(new Label("Contact:"), 0, 3);
                details.add(new Label(rs.getString("contact")), 1, 3);

                // Rental details
                GridPane rentalDetails = new GridPane();
                rentalDetails.setHgap(10);
                rentalDetails.setVgap(5);
                rentalDetails.setPadding(new Insets(20, 0, 0, 0));

                rentalDetails.add(new Label("Vehicle:"), 0, 0);
                rentalDetails.add(new Label(rs.getString("brand") + " " + rs.getString("model")), 1, 0);
                rentalDetails.add(new Label("Rental Period:"), 0, 1);
                rentalDetails.add(new Label(rs.getDate("start_date") + " to " + rs.getDate("end_date")), 1, 1);
                rentalDetails.add(new Label("Daily Rate:"), 0, 2);
                rentalDetails.add(new Label("M" + rs.getDouble("price_per_day")), 1, 2);

                // Payment summary
                GridPane paymentSummary = new GridPane();
                paymentSummary.setHgap(10);
                paymentSummary.setVgap(5);
                paymentSummary.setPadding(new Insets(20, 0, 0, 0));

                paymentSummary.add(new Label("Payment Method:"), 0, 0);
                paymentSummary.add(new Label(rs.getString("method")), 1, 0);
                paymentSummary.add(new Label("Total Amount:"), 0, 1);
                paymentSummary.add(new Label("M" + rs.getDouble("amount")), 1, 1);

                // Thank you message
                Text thankYou = new Text("Thank you for choosing Kay's Wheels Rental!");
                thankYou.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                content.getChildren().addAll(header, details, rentalDetails, paymentSummary, thankYou);

                // Print button
                ButtonType printBtn = new ButtonType("Print", ButtonBar.ButtonData.OK_DONE);
                invoiceDialog.getDialogPane().getButtonTypes().add(printBtn);

                invoiceDialog.getDialogPane().setContent(content);
                invoiceDialog.showAndWait();
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to generate invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Tab createReportsTab() {
        Tab tab = new Tab("Reports");
        tab.setClosable(false);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        // Title
        Text title = new Text("Reports & Analytics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Tab pane for different reports
        TabPane reportTabPane = new TabPane();

        // Available Vehicles Report
        Tab availableTab = new Tab("Available Vehicles");
        availableTab.setContent(createAvailableVehiclesReport());

        // Rental History Report
        Tab historyTab = new Tab("Rental History");
        historyTab.setContent(createRentalHistoryReport());

        // Revenue Report
        Tab revenueTab = new Tab("Revenue");
        revenueTab.setContent(createRevenueReport());

        reportTabPane.getTabs().addAll(availableTab, historyTab, revenueTab);

        vbox.getChildren().addAll(title, reportTabPane);
        tab.setContent(vbox);
        return tab;
    }

    private VBox createAvailableVehiclesReport() {
        VBox vbox = new VBox(10);

        // Table for available vehicles
        TableView<Map<String, String>> availableTable = new TableView<>();

        TableColumn<Map<String, String>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("id")));

        TableColumn<Map<String, String>, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("brand")));

        TableColumn<Map<String, String>, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("model")));

        TableColumn<Map<String, String>, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category")));

        TableColumn<Map<String, String>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("type")));

        TableColumn<Map<String, String>, String> priceCol = new TableColumn<>("Price/Day (M)");
        priceCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("price")));

        availableTable.getColumns().addAll(idCol, brandCol, modelCol, categoryCol, typeCol, priceCol);
        availableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Pie chart for availability
        availabilityChart = new PieChart();
        updateAvailabilityChart();

        // Export button
        Button exportBtn = new Button("Export to CSV");
        exportBtn.setOnAction(e -> exportAvailableVehiclesToCSV());

        HBox chartBox = new HBox(20);
        chartBox.getChildren().addAll(availabilityChart);
        chartBox.setAlignment(Pos.CENTER);

        // Load data
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM vehicles WHERE available=true");

            availableTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> vehicle = new HashMap<>();
                vehicle.put("id", rs.getString("id"));
                vehicle.put("brand", rs.getString("brand"));
                vehicle.put("model", rs.getString("model"));
                vehicle.put("category", rs.getString("category"));
                vehicle.put("type", rs.getString("type"));
                vehicle.put("price", String.format("M%.2f", rs.getDouble("price_per_day")));
                availableTable.getItems().add(vehicle);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load available vehicles: " + e.getMessage());
            e.printStackTrace();
        }

        vbox.getChildren().addAll(availableTable, chartBox, exportBtn);
        return vbox;
    }

    private void updateAvailabilityChart() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT " +
                            "SUM(CASE WHEN available=true THEN 1 ELSE 0 END) as available, " +
                            "SUM(CASE WHEN available=false THEN 1 ELSE 0 END) as rented " +
                            "FROM vehicles"
            );

            if (rs.next()) {
                int available = rs.getInt("available");
                int rented = rs.getInt("rented");

                availabilityChart.getData().clear();
                availabilityChart.getData().add(new PieChart.Data("Available (" + available + ")", available));
                availabilityChart.getData().add(new PieChart.Data("Rented (" + rented + ")", rented));
                availabilityChart.setTitle("Vehicle Availability");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load availability data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportAvailableVehiclesToCSV() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM vehicles WHERE available=true");

            StringBuilder csvData = new StringBuilder();
            csvData.append("ID,Brand,Model,Category,Type,Price Per Day\n");

            while (rs.next()) {
                csvData.append(rs.getInt("id")).append(",")
                        .append(rs.getString("brand")).append(",")
                        .append(rs.getString("model")).append(",")
                        .append(rs.getString("category")).append(",")
                        .append(rs.getString("type")).append(",")
                        .append(rs.getDouble("price_per_day")).append("\n");
            }

            // In a real application, you would save this to a file
            showAlert("CSV Export", "Available vehicles data exported to CSV:\n\n" + csvData.toString());
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to export available vehicles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createRentalHistoryReport() {
        VBox vbox = new VBox(10);

        // Table for rental history
        TableView<Map<String, String>> historyTable = new TableView<>();

        TableColumn<Map<String, String>, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("customer")));

        TableColumn<Map<String, String>, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("vehicle")));

        TableColumn<Map<String, String>, String> datesCol = new TableColumn<>("Rental Period");
        datesCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("dates")));

        TableColumn<Map<String, String>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("status")));

        TableColumn<Map<String, String>, String> amountCol = new TableColumn<>("Amount (M)");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("amount")));

        historyTable.getColumns().addAll(customerCol, vehicleCol, datesCol, statusCol, amountCol);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Bar chart for rentals by customer
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> rentalsByCustomerChart = new BarChart<>(xAxis, yAxis);
        rentalsByCustomerChart.setTitle("Rentals by Customer");
        updateRentalsByCustomerChart(rentalsByCustomerChart);

        // Export button
        Button exportBtn = new Button("Export to CSV");
        exportBtn.setOnAction(e -> exportRentalHistoryToCSV());

        // Load data
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT c.name as customer, v.brand || ' ' || v.model as vehicle, " +
                            "b.start_date || ' to ' || b.end_date as dates, b.status, " +
                            "COALESCE(p.amount, 0) as amount " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "LEFT JOIN payments p ON b.id = p.booking_id " +
                            "ORDER BY b.start_date DESC"
            );

            historyTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> rental = new HashMap<>();
                rental.put("customer", rs.getString("customer"));
                rental.put("vehicle", rs.getString("vehicle"));
                rental.put("dates", rs.getString("dates"));
                rental.put("status", rs.getString("status"));
                rental.put("amount", rs.getDouble("amount") > 0 ? String.format("M%.2f", rs.getDouble("amount")) : "N/A");
                historyTable.getItems().add(rental);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load rental history: " + e.getMessage());
            e.printStackTrace();
        }

        vbox.getChildren().addAll(historyTable, rentalsByCustomerChart, exportBtn);
        return vbox;
    }

    private void updateRentalsByCustomerChart(BarChart<String, Number> chart) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT c.name as customer, COUNT(b.id) as rentals " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "GROUP BY c.name " +
                            "ORDER BY rentals DESC LIMIT 5"
            );

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Rentals");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("customer"), rs.getInt("rentals")));
            }

            chart.getData().clear();
            chart.getData().add(series);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load rentals by customer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportRentalHistoryToCSV() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT c.name as customer, v.brand || ' ' || v.model as vehicle, " +
                            "b.start_date || ' to ' || b.end_date as dates, b.status, " +
                            "COALESCE(p.amount, 0) as amount " +
                            "FROM bookings b " +
                            "JOIN customers c ON b.customer_id = c.id " +
                            "JOIN vehicles v ON b.vehicle_id = v.id " +
                            "LEFT JOIN payments p ON b.id = p.booking_id " +
                            "ORDER BY b.start_date DESC"
            );

            StringBuilder csvData = new StringBuilder();
            csvData.append("Customer,Vehicle,Rental Period,Status,Amount\n");

            while (rs.next()) {
                csvData.append(rs.getString("customer")).append(",")
                        .append(rs.getString("vehicle")).append(",")
                        .append(rs.getString("dates")).append(",")
                        .append(rs.getString("status")).append(",")
                        .append(rs.getDouble("amount")).append("\n");
            }

            // In a real application, you would save this to a file
            showAlert("CSV Export", "Rental history data exported to CSV:\n\n" + csvData.toString());
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to export rental history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createRevenueReport() {
        VBox vbox = new VBox(10);

        // Table for revenue
        TableView<Map<String, String>> revenueTable = new TableView<>();

        TableColumn<Map<String, String>, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("month")));

        TableColumn<Map<String, String>, String> revenueCol = new TableColumn<>("Revenue (M)");
        revenueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("revenue")));

        TableColumn<Map<String, String>, String> bookingsCol = new TableColumn<>("Bookings");
        bookingsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("bookings")));

        revenueTable.getColumns().addAll(monthCol, revenueCol, bookingsCol);
        revenueTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Line chart for revenue trend
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        rentalTrendChart = new LineChart<>(xAxis, yAxis);
        rentalTrendChart.setTitle("Monthly Revenue Trend");
        updateRevenueTrendChart();

        // Export button
        Button exportBtn = new Button("Export to CSV");
        exportBtn.setOnAction(e -> exportRevenueToCSV());

        // Load data
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT " +
                            "DATE_FORMAT(p.payment_date, '%Y-%m') as month, " +
                            "SUM(p.amount) as revenue, " +
                            "COUNT(p.id) as bookings " +
                            "FROM payments p " +
                            "GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m') " +
                            "ORDER BY month DESC"
            );

            revenueTable.getItems().clear();

            while (rs.next()) {
                Map<String, String> revenue = new HashMap<>();
                revenue.put("month", rs.getString("month"));
                revenue.put("revenue", String.format("M%.2f", rs.getDouble("revenue")));
                revenue.put("bookings", rs.getString("bookings"));
                revenueTable.getItems().add(revenue);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load revenue data: " + e.getMessage());
            e.printStackTrace();
        }

        vbox.getChildren().addAll(revenueTable, rentalTrendChart, exportBtn);
        return vbox;
    }

    private void updateRevenueTrendChart() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT " +
                            "DATE_FORMAT(p.payment_date, '%Y-%m') as month, " +
                            "SUM(p.amount) as revenue " +
                            "FROM payments p " +
                            "GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m') " +
                            "ORDER BY month"
            );

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Revenue");

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("month"), rs.getDouble("revenue")));
            }

            rentalTrendChart.getData().clear();
            rentalTrendChart.getData().add(series);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load revenue trend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportRevenueToCSV() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT " +
                            "DATE_FORMAT(p.payment_date, '%Y-%m') as month, " +
                            "SUM(p.amount) as revenue, " +
                            "COUNT(p.id) as bookings " +
                            "FROM payments p " +
                            "GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m') " +
                            "ORDER BY month"
            );

            StringBuilder csvData = new StringBuilder();
            csvData.append("Month,Revenue,Bookings\n");

            while (rs.next()) {
                csvData.append(rs.getString("month")).append(",")
                        .append(rs.getDouble("revenue")).append(",")
                        .append(rs.getInt("bookings")).append("\n");
            }

            // In a real application, you would save this to a file
            showAlert("CSV Export", "Revenue data exported to CSV:\n\n" + csvData.toString());
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to export revenue data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        // Create tables if they don't exist
        Statement stmt = connection.createStatement();

        // Users table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "role VARCHAR(20) NOT NULL, " +
                        "name VARCHAR(100) NOT NULL)"
        );

        // Vehicles table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS vehicles (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "brand VARCHAR(50) NOT NULL, " +
                        "model VARCHAR(50) NOT NULL, " +
                        "category VARCHAR(20) NOT NULL, " +
                        "type VARCHAR(30) NOT NULL, " +
                        "price_per_day DECIMAL(10,2) NOT NULL, " +
                        "available BOOLEAN DEFAULT true)"
        );

        // Customers table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS customers (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "contact VARCHAR(20) NOT NULL, " +
                        "driving_license VARCHAR(20) NOT NULL)"
        );

        // Bookings table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "customer_id INTEGER NOT NULL, " +
                        "vehicle_id INTEGER NOT NULL, " +
                        "start_date DATE NOT NULL, " +
                        "end_date DATE NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'Booked', " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(id), " +
                        "FOREIGN KEY (vehicle_id) REFERENCES vehicles(id))"
        );

        // Payments table
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS payments (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "booking_id INTEGER NOT NULL, " +
                        "amount DECIMAL(10,2) NOT NULL, " +
                        "payment_date DATE NOT NULL, " +
                        "method VARCHAR(20) NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'Paid', " +
                        "FOREIGN KEY (booking_id) REFERENCES bookings(id))"
        );

        // Add sample data if tables are empty
        if (isTableEmpty("users")) {
            stmt.execute(
                    "INSERT INTO users (username, password, role, name) VALUES " +
                            "('admin', 'admin123', 'Admin', 'System Administrator'), " +
                            "('staff', 'staff123', 'Employee', 'Regular Staff')"
            );
        }

        if (isTableEmpty("vehicles")) {
            stmt.execute(
                    "INSERT INTO vehicles (brand, model, category, type, price_per_day, available) VALUES " +
                            "('Toyota', 'Corolla', 'Car', 'Sedan', 500.00, true), " +
                            "('Honda', 'CR-V', 'Car', 'SUV', 700.00, true), " +
                            "('Ford', 'Ranger', 'Truck', 'Pickup', 800.00, true), " +
                            "('Yamaha', 'YZF-R1', 'Bike', 'Sport', 400.00, true), " +
                            "('Mercedes', 'Sprinter', 'Van', 'Passenger', 900.00, false)"
            );
        }

        if (isTableEmpty("customers")) {
            stmt.execute(
                    "INSERT INTO customers (name, contact, driving_license) VALUES " +
                            "('Mary Nko', '52345678', 'DL1234567890'), " +
                            "('Trey Mohale', '57654321', 'DL0987654321'), " +
                            "('Tlotliso Mporo', '51223344', 'DL1122334455')"
            );
        }

        if (isTableEmpty("bookings")) {
            stmt.execute(
                    "INSERT INTO bookings (customer_id, vehicle_id, start_date, end_date, status) VALUES " +
                            "(1, 5, '2023-01-01', '2023-01-05', 'Completed'), " +
                            "(2, 3, '2023-02-10', '2023-02-15', 'Paid'), " +
                            "(3, 1, '2023-03-01', '2023-03-03', 'Booked')"
            );
        }

        if (isTableEmpty("payments")) {
            stmt.execute(
                    "INSERT INTO payments (booking_id, amount, payment_date, method, status) VALUES " +
                            "(1, 4500.00, '2023-01-05', 'Credit Card', 'Paid'), " +
                            "(2, 4000.00, '2023-02-15', 'Cash', 'Paid')"
            );
        }
    }

    private boolean isTableEmpty(String tableName) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
        return rs.next() && rs.getInt(1) == 0;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}