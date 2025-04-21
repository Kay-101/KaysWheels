package com.example.kayswheelsrental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyJDBC {
    // ✅ Added static variables
    private static Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle_rental";
    private static final String USER = "root";
    private static final String PASSWORD = "1234567890";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {  // ✅ Fixed typo
                Class.forName("com.mysql.cj.jdbc.Driver");  // ✅ Fixed syntax
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("JavaFX Connected to MySQL successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");  // ✅ Fixed typo
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");  // ✅ Fixed typo
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Failed to close the connection!");
                e.printStackTrace();
            }
        }
    }
}