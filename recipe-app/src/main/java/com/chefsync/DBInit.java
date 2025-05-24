package com.chefsync;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DBInit {

    private static final String DB_PATH = "recipe_manager.db"; // Relative to recipe-app directory
    private static final String SCHEMA_PATH = "schema.sql"; // Relative to recipe-app directory

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            // In a real app, you might throw a custom exception or handle this more gracefully
            return null;
        }
    }

    public static Connection connect() throws SQLException {
        // SQLite connection string
        // The database file will be created in the recipe-app directory
        String url = "jdbc:sqlite:" + DB_PATH;
        
        // Connect to the database
        Connection conn = DriverManager.getConnection(url);
        
        // Ensure foreign keys are enabled
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        
        return conn;
    }

    public static void initializeDatabase() {
        // Remove existing database if it exists
        try {
            Files.deleteIfExists(Paths.get(DB_PATH));
        } catch (IOException e) {
            System.err.println("Error deleting existing database: " + e.getMessage());
            return; // Stop initialization if we can't delete the old DB
        }

        try (Connection conn = connect()) {
             // Ensure WAL mode is not enabled to prevent readonly issues
             try (Statement stmt = conn.createStatement()) {
                 stmt.execute("PRAGMA journal_mode = DELETE");
             }
             
             try (Statement stmt = conn.createStatement();
                  BufferedReader reader = new BufferedReader(new FileReader(SCHEMA_PATH))) {

                // Read and execute schema from SQL file
                StringBuilder schemaScript = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    schemaScript.append(line).append(System.lineSeparator());
                }

                String[] individualStatements = schemaScript.toString().split(";");

                for (String statement : individualStatements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.executeUpdate(statement.trim() + ";"); // Add back semicolon for execution
                    }
                }
                System.out.println("Database schema created successfully.");

                // Insert sample users with hashed passwords
                String sqlUsers = "INSERT INTO Users (Username, Email, Password, Bio) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtUsers = conn.prepareStatement(sqlUsers)) {
                    String[][] sampleUsers = {
                        {"demo_user", "demo@example.com", hashPassword("password123"), "Demo User Bio"},
                        {"chef_master", "chef@example.com", hashPassword("cookmaster"), "Professional Chef"}
                    };
                    for (String[] user : sampleUsers) {
                        pstmtUsers.setString(1, user[0]);
                        pstmtUsers.setString(2, user[1]);
                        pstmtUsers.setString(3, user[2]);
                        pstmtUsers.setString(4, user[3]);
                        pstmtUsers.executeUpdate();
                    }
                    System.out.println("Sample users inserted successfully.");
                }

                // Insert sample ingredients
                String sqlIngredients = "INSERT INTO Ingredients (Name, Category, Unit, NutritionalInfo) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtIngredients = conn.prepareStatement(sqlIngredients)) {
                    String[][] sampleIngredients = {
                        {"Chicken Breast", "Protein", "grams", "High protein, low fat"},
                        {"Brown Rice", "Carbohydrate", "grams", "Whole grain, nutritious"},
                        {"Olive Oil", "Oil", "ml", "Heart-healthy fat"},
                        {"Tomato", "Vegetable", "pieces", "Rich in vitamins"},
                        {"Garlic", "Herb", "cloves", "Flavor enhancer"}
                    };
                    for (String[] ingredient : sampleIngredients) {
                        pstmtIngredients.setString(1, ingredient[0]);
                        pstmtIngredients.setString(2, ingredient[1]);
                        pstmtIngredients.setString(3, ingredient[2]);
                        pstmtIngredients.setString(4, ingredient[3]);
                        pstmtIngredients.executeUpdate();
                    }
                    System.out.println("Sample ingredients inserted successfully.");
                }

                // Insert sample tags
                String sqlTags = "INSERT INTO Tags (Name, Description) VALUES (?, ?)";
                try (PreparedStatement pstmtTags = conn.prepareStatement(sqlTags)) {
                    String[][] sampleTags = {
                        {"Healthy", "Nutritious recipes"},
                        {"Quick Meal", "Recipes under 30 minutes"},
                        {"Vegetarian", "No meat recipes"},
                        {"Gluten-Free", "Suitable for gluten-sensitive diets"}
                    };
                    for (String[] tag : sampleTags) {
                        pstmtTags.setString(1, tag[0]);
                        pstmtTags.setString(2, tag[1]);
                        pstmtTags.executeUpdate();
                    }
                    System.out.println("Sample tags inserted successfully.");
                }
            }
            
            // Make sure the database is properly closed and committed
            try {
                conn.commit();
            } catch (SQLException e) {
                System.err.println("Error committing changes: " + e.getMessage());
            }
            
            System.out.println("Database " + DB_PATH + " initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Schema file " + SCHEMA_PATH + " not found or error reading file: " + e.getMessage());
        }
        
        // Set read and write permissions for the database file
        try {
            File dbFile = new File(DB_PATH);
            if (dbFile.exists()) {
                dbFile.setReadable(true, false);
                dbFile.setWritable(true, false);
                System.out.println("Database file permissions set successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error setting database file permissions: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // To run this, you need the SQLite JDBC driver in your classpath.
        // Example: java -cp .:sqlite-jdbc-3.43.0.0.jar com.chefsync.DBInit
        // Download the SQLite JDBC driver (e.g., from https://github.com/xerial/sqlite-jdbc/releases)
        // and place it in a 'lib' directory, then adjust classpath.
        // For now, this main method allows direct execution.
        // We'll also need to create a lib directory and download the SQLite JDBC driver.
        System.out.println("Initializing database...");
        initializeDatabase();
    }
} 