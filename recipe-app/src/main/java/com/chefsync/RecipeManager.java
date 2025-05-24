package com.chefsync;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class RecipeManager {
    private Connection conn;
    private Integer currentUser;
    private Scanner scanner;
    private final String DB_PATH = "recipe_manager.db";
    
    public RecipeManager() {
        scanner = new Scanner(System.in);
        
        // Check if database exists, initialize if not
        File dbFile = new File(DB_PATH);
        if (!dbFile.exists()) {
            System.out.println("Database not found. Initializing...");
            DBInit.initializeDatabase();
        }
        
        try {
            // Connect to the SQLite database
            String url = "jdbc:sqlite:" + DB_PATH;
            
            // Connect to the database
            conn = DriverManager.getConnection(url);
            
            // Configure the connection
            conn.setAutoCommit(false); // Set auto-commit to false
            
            // Ensure foreign keys are enabled
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void register() {
        try {
            System.out.println("\n--- User Registration ---");
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            
            System.out.print("Confirm password: ");
            String confirmPassword = scanner.nextLine();
            
            System.out.print("Enter a short bio (optional): ");
            String bio = scanner.nextLine();
            
            if (!password.equals(confirmPassword)) {
                System.out.println("Passwords do not match!");
                return;
            }
            
            String hashedPassword = DBInit.hashPassword(password);
            
            String sql = "INSERT INTO Users (Username, Email, Password, Bio) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, bio);
                pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
            }
            
            System.out.println("User registered successfully!");
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Username or email already exists.");
            } else {
                System.out.println("Registration error: " + e.getMessage());
            }
        }
    }
    
    public boolean login() {
        System.out.println("\n--- User Login ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        String hashedPassword = DBInit.hashPassword(password);
        
        String sql = "SELECT UserID, Username FROM Users WHERE Username = ? AND Password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentUser = rs.getInt("UserID");
                String name = rs.getString("Username");
                System.out.println("Welcome, " + name + "!");
                return true;
            } else {
                System.out.println("Invalid credentials.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
        }
    }
    
    public void profileManagement() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        while (true) {
            System.out.println("\n--- Profile Management ---");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Change Password");
            System.out.println("4. Return to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewProfile();
                    break;
                case "2":
                    updateProfile();
                    break;
                case "3":
                    changePassword();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    public void viewProfile() {
        String sql = "SELECT Username, Email, Bio, CreatedAt FROM Users WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n--- Profile Details ---");
                System.out.println("Username: " + rs.getString("Username"));
                System.out.println("Email: " + rs.getString("Email"));
                System.out.println("Bio: " + (rs.getString("Bio") != null ? rs.getString("Bio") : "No bio set"));
                System.out.println("Member Since: " + rs.getString("CreatedAt"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing profile: " + e.getMessage());
        }
    }
    
    public void updateProfile() {
        System.out.print("Enter new email (leave blank to keep current): ");
        String email = scanner.nextLine();
        
        System.out.print("Enter new bio (leave blank to keep current): ");
        String bio = scanner.nextLine();
        
        StringBuilder updates = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        if (!email.isEmpty()) {
            updates.append("Email = ?");
            params.add(email);
        }
        
        if (!bio.isEmpty()) {
            if (updates.length() > 0) {
                updates.append(", ");
            }
            updates.append("Bio = ?");
            params.add(bio);
        }
        
        if (updates.length() > 0) {
            params.add(currentUser);
            String sql = "UPDATE Users SET " + updates.toString() + " WHERE UserID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                
                pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                
                System.out.println("Profile updated successfully!");
            } catch (SQLException e) {
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException rollbackEx) {
                    System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
                System.out.println("Error updating profile: " + e.getMessage());
            }
        }
    }
    
    public void changePassword() {
        System.out.print("Enter current password: ");
        String oldPassword = scanner.nextLine();
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        // Check current password
        String sql = "SELECT Password FROM Users WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String currentHash = rs.getString("Password");
                String oldHash = DBInit.hashPassword(oldPassword);
                
                if (!currentHash.equals(oldHash)) {
                    System.out.println("Current password is incorrect.");
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    System.out.println("New passwords do not match.");
                    return;
                }
                
                // Update password
                String newHash = DBInit.hashPassword(newPassword);
                String updateSql = "UPDATE Users SET Password = ? WHERE UserID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newHash);
                    updateStmt.setInt(2, currentUser);
                    updateStmt.executeUpdate();
                    
                    // Commit the transaction
                    conn.commit();
                    
                    System.out.println("Password changed successfully!");
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error changing password: " + e.getMessage());
        }
    }
    
    public void addRecipe() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        try {
            System.out.println("\n--- Add New Recipe ---");
            System.out.print("Enter recipe title: ");
            String title = scanner.nextLine();
            
            System.out.print("Enter recipe description: ");
            String description = scanner.nextLine();
            
            System.out.print("Enter cuisine type: ");
            String cuisine = scanner.nextLine();
            
            System.out.print("Enter preparation time (minutes): ");
            int prepTime = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter cooking time (minutes): ");
            int cookTime = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter difficulty (Easy/Medium/Hard): ");
            String difficulty = scanner.nextLine();
            
            System.out.print("Enter number of servings: ");
            int servings = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter cooking instructions: ");
            String instructions = scanner.nextLine();
            
            System.out.print("Make recipe public? (yes/no): ");
            boolean isPublic = scanner.nextLine().toLowerCase().equals("yes");
            
            System.out.print("Enter total calories: ");
            int calories = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter protein (g): ");
            float protein = Float.parseFloat(scanner.nextLine());
            
            System.out.print("Enter carbs (g): ");
            float carbs = Float.parseFloat(scanner.nextLine());
            
            System.out.print("Enter fats (g): ");
            float fats = Float.parseFloat(scanner.nextLine());
            
            // Insert the recipe
            String sql = "INSERT INTO Recipes " +
                        "(UserID, Title, Instructions, PrepTime, CookingTime, " +
                        "Servings, Calories, Protein, Carbs, Fats, Cuisine, " + 
                        "DifficultyLevel, IsPublic) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int recipeId;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, currentUser);
                pstmt.setString(2, title);
                pstmt.setString(3, instructions);
                pstmt.setString(4, String.valueOf(prepTime));
                pstmt.setString(5, String.valueOf(cookTime));
                pstmt.setInt(6, servings);
                pstmt.setFloat(7, calories);
                pstmt.setFloat(8, protein);
                pstmt.setFloat(9, carbs);
                pstmt.setFloat(10, fats);
                pstmt.setString(11, cuisine);
                pstmt.setString(12, difficulty);
                pstmt.setInt(13, isPublic ? 1 : 0);
                
                pstmt.executeUpdate();
                
                // Get the generated recipe ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    recipeId = rs.getInt(1);
                } else {
                    throw new SQLException("Creating recipe failed, no ID obtained.");
                }
            }
            
            // Add ingredients
            while (true) {
                System.out.print("Enter ingredient name (or 'done' to finish): ");
                String ingredientName = scanner.nextLine();
                
                if (ingredientName.toLowerCase().equals("done")) {
                    break;
                }
                
                // Check if the ingredient exists
                int ingredientId;
                String ingredientSql = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(ingredientSql)) {
                    pstmt.setString(1, ingredientName);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        ingredientId = rs.getInt("IngredientID");
                    } else {
                        // Create new ingredient
                        System.out.print("Enter category for " + ingredientName + ": ");
                        String category = scanner.nextLine();
                        
                        System.out.print("Enter default unit for " + ingredientName + ": ");
                        String unit = scanner.nextLine();
                        
                        System.out.print("Enter nutritional info (optional): ");
                        String nutritionalInfo = scanner.nextLine();
                        
                        String newIngredientSql = "INSERT INTO Ingredients (Name, Category, Unit, NutritionalInfo) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ingredientStmt = conn.prepareStatement(newIngredientSql, Statement.RETURN_GENERATED_KEYS)) {
                            ingredientStmt.setString(1, ingredientName);
                            ingredientStmt.setString(2, category);
                            ingredientStmt.setString(3, unit);
                            ingredientStmt.setString(4, nutritionalInfo);
                            ingredientStmt.executeUpdate();
                            
                            ResultSet generatedKeys = ingredientStmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                ingredientId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Creating ingredient failed, no ID obtained.");
                            }
                        }
                    }
                }
                
                // Add quantity and unit for this recipe
                System.out.print("Enter quantity of " + ingredientName + ": ");
                float quantity = Float.parseFloat(scanner.nextLine());
                
                System.out.print("Enter unit for " + ingredientName + ": ");
                String unit = scanner.nextLine();
                
                System.out.print("Any notes for this ingredient? (optional): ");
                String notes = scanner.nextLine();
                
                // Insert into RecipeIngredients
                String recipeIngredientSql = "INSERT INTO RecipeIngredients (RecipeID, IngredientID, Quantity, Unit, Notes) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(recipeIngredientSql)) {
                    pstmt.setInt(1, recipeId);
                    pstmt.setInt(2, ingredientId);
                    pstmt.setFloat(3, quantity);
                    pstmt.setString(4, unit);
                    pstmt.setString(5, notes);
                    pstmt.executeUpdate();
                }
            }
            
            // Add tags
            while (true) {
                System.out.print("Enter a tag for this recipe (or 'done' to finish): ");
                String tagName = scanner.nextLine();
                
                if (tagName.toLowerCase().equals("done")) {
                    break;
                }
                
                // Check if the tag exists
                int tagId;
                String tagSql = "SELECT TagID FROM Tags WHERE Name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(tagSql)) {
                    pstmt.setString(1, tagName);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        tagId = rs.getInt("TagID");
                    } else {
                        // Create new tag
                        System.out.print("Enter description for tag " + tagName + ": ");
                        String tagDescription = scanner.nextLine();
                        
                        String newTagSql = "INSERT INTO Tags (Name, Description) VALUES (?, ?)";
                        try (PreparedStatement tagStmt = conn.prepareStatement(newTagSql, Statement.RETURN_GENERATED_KEYS)) {
                            tagStmt.setString(1, tagName);
                            tagStmt.setString(2, tagDescription);
                            tagStmt.executeUpdate();
                            
                            ResultSet generatedKeys = tagStmt.getGeneratedKeys();
                            if (generatedKeys.next()) {
                                tagId = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Creating tag failed, no ID obtained.");
                            }
                        }
                    }
                }
                
                // Insert into RecipeTags
                String recipeTagSql = "INSERT INTO RecipeTags (RecipeID, TagID) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(recipeTagSql)) {
                    pstmt.setInt(1, recipeId);
                    pstmt.setInt(2, tagId);
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit();
            System.out.println("Recipe added successfully!");
            
        } catch (SQLException | NumberFormatException e) {
            try {
                if (conn != null) {
                conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error adding recipe: " + e.getMessage());
        }
    }
    
    public List<Object[]> viewRecipes(String filterType) {
        List<Object[]> recipes = new ArrayList<>();
        
        try {
            StringBuilder query = new StringBuilder(
                "SELECT RecipeID, Title, Cuisine, DifficultyLevel, " +
                "CookingTime, Servings, IsPublic " +
                "FROM Recipes");
            
            List<Object> params = new ArrayList<>();
            
            if (filterType.equals("my_recipes") && currentUser != null) {
                query.append(" WHERE UserID = ?");
                params.add(currentUser);
            } else if (filterType.equals("public")) {
                query.append(" WHERE IsPublic = 1");
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] recipe = new Object[7];
                    recipe[0] = rs.getInt("RecipeID");
                    recipe[1] = rs.getString("Title");
                    recipe[2] = rs.getString("Cuisine");
                    recipe[3] = rs.getString("DifficultyLevel");
                    recipe[4] = rs.getString("CookingTime");
                    recipe[5] = rs.getInt("Servings");
                    recipe[6] = rs.getInt("IsPublic") == 1 ? "Yes" : "No";
                    
                    recipes.add(recipe);
                }
            }
            
            if (recipes.isEmpty()) {
                System.out.println("No recipes found.");
                return recipes;
            }
            
            // Use TableFormatter to display recipes
            String[] headers = {"ID", "Title", "Cuisine", "Difficulty", "Cook Time", "Servings", "Public"};
            int[] maxWidths = {5, 20, 10, 10, 11, 8, 6};
            System.out.println(TableFormatter.formatTable(headers, recipes, maxWidths));
            
        } catch (SQLException e) {
            System.out.println("Error viewing recipes: " + e.getMessage());
        }
        
        return recipes;
    }
    
    private String limitString(String input, int maxLength) {
        if (input == null) return "";
        return input.length() <= maxLength ? input : input.substring(0, maxLength - 3) + "...";
    }
    
    public void viewRecipesMenu() {
        while (true) {
            System.out.println("\n--- View Recipes ---");
            System.out.println("1. View All Recipes");
            System.out.println("2. View My Recipes");
            System.out.println("3. View Recipe Details");
            System.out.println("4. Return to Main Menu");
            
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    viewRecipes("public");
                    break;
                case "2":
                    viewRecipes("my_recipes");
                    break;
                case "3":
                    viewRecipeDetails();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    public void viewRecipeDetails() {
        try {
            // First, view available recipes
            List<Object[]> recipes = viewRecipes("all");
            
            if (recipes.isEmpty()) {
                return;
            }
            
            System.out.print("\nEnter Recipe ID to view details: ");
            int recipeId = Integer.parseInt(scanner.nextLine());
            
            // Retrieve detailed recipe information
            String sql = "SELECT " +
                    "r.RecipeID, r.Title, r.Instructions, r.Cuisine, r.DifficultyLevel, " +
                    "r.PrepTime, r.CookingTime, r.Servings, r.Calories, r.Protein, " +
                    "r.Carbs, r.Fats " +
                    "FROM Recipes r WHERE r.RecipeID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, recipeId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Recipe not found.");
                    return;
                }
                
                System.out.println("\n--- Recipe Details ---");
                System.out.println("=" + "=".repeat(30));
                System.out.println("📜 Title: " + rs.getString("Title"));
                System.out.println("🌍 Cuisine: " + rs.getString("Cuisine"));
                System.out.println("🔥 Difficulty: " + rs.getString("DifficultyLevel"));
                System.out.println("⏰ Prep Time: " + rs.getString("PrepTime") + " minutes");
                System.out.println("🍳 Cooking Time: " + rs.getString("CookingTime") + " minutes");
                System.out.println("👥 Servings: " + rs.getInt("Servings"));
                
                System.out.println("\n🍎 Nutritional Information:");
                System.out.println("    Calories: " + rs.getInt("Calories"));
                System.out.println("    Protein: " + rs.getFloat("Protein") + "g");
                System.out.println("    Carbs: " + rs.getFloat("Carbs") + "g");
                System.out.println("    Fats: " + rs.getFloat("Fats") + "g");
                
                // Get ingredients
                String ingredientsSql = "SELECT i.Name, ri.Quantity, ri.Unit " +
                                     "FROM RecipeIngredients ri " +
                                     "JOIN Ingredients i ON ri.IngredientID = i.IngredientID " +
                                     "WHERE ri.RecipeID = ?";
                
                try (PreparedStatement ingredientsStmt = conn.prepareStatement(ingredientsSql)) {
                    ingredientsStmt.setInt(1, recipeId);
                    ResultSet ingredientsRs = ingredientsStmt.executeQuery();
                    
                    System.out.println("\n🥬 Ingredients:");
                    boolean hasIngredients = false;
                    
                    while (ingredientsRs.next()) {
                        hasIngredients = true;
                        System.out.printf("    - %s: %.2f %s\n", 
                                ingredientsRs.getString("Name"),
                                ingredientsRs.getFloat("Quantity"),
                                ingredientsRs.getString("Unit"));
                    }
                    
                    if (!hasIngredients) {
                        System.out.println("    No ingredients found.");
                    }
                }
                
                // Get tags
                String tagsSql = "SELECT t.Name FROM RecipeTags rt " +
                               "JOIN Tags t ON rt.TagID = t.TagID " +
                               "WHERE rt.RecipeID = ?";
                
                try (PreparedStatement tagsStmt = conn.prepareStatement(tagsSql)) {
                    tagsStmt.setInt(1, recipeId);
                    ResultSet tagsRs = tagsStmt.executeQuery();
                    
                    StringBuilder tags = new StringBuilder();
                    boolean hasTags = false;
                    
                    while (tagsRs.next()) {
                        if (hasTags) {
                            tags.append(", ");
                        }
                        tags.append(tagsRs.getString("Name"));
                        hasTags = true;
                    }
                    
                    if (hasTags) {
                        System.out.println("\n🏷️ Tags: " + tags.toString());
                    }
                }
                
                // Show instructions
                System.out.println("\n📖 Instructions:");
                System.out.println(rs.getString("Instructions"));
                
                // Get feedback
                String feedbackSql = "SELECT u.Username, rf.Rating, rf.DifficultyRating, " +
                                  "rf.ActualCookingTime, rf.Comment, rf.CreatedAt " +
                                  "FROM RecipeFeedback rf " +
                                  "JOIN Users u ON rf.UserID = u.UserID " +
                                  "WHERE rf.RecipeID = ? " +
                                  "ORDER BY rf.CreatedAt DESC";
                
                try (PreparedStatement feedbackStmt = conn.prepareStatement(feedbackSql)) {
                    feedbackStmt.setInt(1, recipeId);
                    ResultSet feedbackRs = feedbackStmt.executeQuery();
                    
                    boolean hasFeedback = false;
                    System.out.println("\n📝 Recipe Feedback:");
                    
                    while (feedbackRs.next()) {
                        hasFeedback = true;
                        System.out.println("\n👤 User: " + feedbackRs.getString("Username"));
                        System.out.println("⭐ Rating: " + feedbackRs.getInt("Rating") + "/5");
                        System.out.println("🔥 Difficulty Rating: " + feedbackRs.getString("DifficultyRating") + "/5");
                        System.out.println("⏰ Actual Cooking Time: " + 
                                      (feedbackRs.getString("ActualCookingTime") != null ? 
                                       feedbackRs.getString("ActualCookingTime") + " minutes" : "Not specified"));
                        System.out.println("💬 Comment: " + 
                                      (feedbackRs.getString("Comment") != null ? 
                                       feedbackRs.getString("Comment") : "No comment"));
                        System.out.println("📅 Submitted: " + feedbackRs.getString("CreatedAt"));
                    }
                    
                    if (!hasFeedback) {
                        System.out.println("No feedback available for this recipe.");
                    }
                }
            }
            
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error viewing recipe details: " + e.getMessage());
        }
    }
    
    public void mealPlanning() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        while (true) {
            System.out.println("\n--- Meal Planning ---");
            System.out.println("1. Create Meal Plan");
            System.out.println("2. View Meal Plans");
            System.out.println("3. View Meal Plan Details");
            System.out.println("4. Add Recipe to Meal Plan");
            System.out.println("5. Return to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createMealPlan();
                    break;
                case "2":
                    viewMealPlans();
                    break;
                case "3":
                    viewMealPlanDetails();
                    break;
                case "4":
                    addRecipeToMealPlan();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    public Integer createMealPlan() {
        try {
            System.out.print("Enter meal plan name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine();
            
            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = scanner.nextLine();
            
            String sql = "INSERT INTO MealPlans (UserID, Name, StartDate, EndDate) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, currentUser);
                pstmt.setString(2, name);
                pstmt.setString(3, startDate);
                pstmt.setString(4, endDate);
                
                pstmt.executeUpdate();
                
                // Get the ID of the created plan
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int planId = rs.getInt(1);
                    
                    // Commit the transaction
                    conn.commit();
                    
                    System.out.println("Meal plan '" + name + "' created successfully!");
                    return planId;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error creating meal plan: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Object[]> viewMealPlans() {
        List<Object[]> plans = new ArrayList<>();
        
        try {
            String sql = "SELECT PlanID, Name, StartDate, EndDate FROM MealPlans WHERE UserID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] plan = new Object[4];
                    plan[0] = rs.getInt("PlanID");
                    plan[1] = rs.getString("Name");
                    plan[2] = rs.getString("StartDate");
                    plan[3] = rs.getString("EndDate");
                    
                    plans.add(plan);
                }
            }
            
            if (plans.isEmpty()) {
                System.out.println("No meal plans found.");
                return plans;
            }
            
            // Use TableFormatter to display meal plans
            String[] headers = {"ID", "Name", "Start Date", "End Date"};
            int[] maxWidths = {5, 20, 10, 10};
            System.out.println(TableFormatter.formatTable(headers, plans, maxWidths));
            
        } catch (SQLException e) {
            System.out.println("Error viewing meal plans: " + e.getMessage());
        }
        
        return plans;
    }
    
    public void viewMealPlanDetails() {
        try {
            // First, view available meal plans
            List<Object[]> plans = viewMealPlans();
            
            if (plans.isEmpty()) {
                return;
            }
            
            System.out.print("\nEnter Meal Plan ID to view details: ");
            int planId = Integer.parseInt(scanner.nextLine());
            
            // Get basic plan information
            String planSql = "SELECT Name, StartDate, EndDate FROM MealPlans WHERE PlanID = ? AND UserID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(planSql)) {
                pstmt.setInt(1, planId);
                pstmt.setInt(2, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Meal plan not found.");
                    return;
                }
                
                String planName = rs.getString("Name");
                String startDate = rs.getString("StartDate");
                String endDate = rs.getString("EndDate");
                
                // Get recipes in this meal plan
                String recipesSql = "SELECT r.Title, r.Calories, r.Protein, r.Carbs, r.Fats, " +
                                   "mpr.MealDate, mpr.MealType " +
                                   "FROM MealPlanRecipes mpr " +
                                   "JOIN Recipes r ON mpr.RecipeID = r.RecipeID " +
                                   "WHERE mpr.PlanID = ? " +
                                   "ORDER BY mpr.MealDate, mpr.MealType";
                
                try (PreparedStatement recipesStmt = conn.prepareStatement(recipesSql)) {
                    recipesStmt.setInt(1, planId);
                    ResultSet recipesRs = recipesStmt.executeQuery();
                    
                    // Organize meal plan by date and meal type
                    Map<String, Map<String, Map<String, Object>>> mealPlanDetails = new HashMap<>();
                    
                    while (recipesRs.next()) {
                        String date = recipesRs.getString("MealDate");
                        String mealType = recipesRs.getString("MealType");
                        
                        // Initialize date in map if it doesn't exist
                        if (!mealPlanDetails.containsKey(date)) {
                            mealPlanDetails.put(date, new HashMap<>());
                        }
                        
                        // Initialize meal type in date map if it doesn't exist
                        Map<String, Map<String, Object>> dateMap = mealPlanDetails.get(date);
                        if (!dateMap.containsKey(mealType)) {
                            Map<String, Object> recipeDetails = new HashMap<>();
                            recipeDetails.put("title", recipesRs.getString("Title"));
                            recipeDetails.put("calories", recipesRs.getInt("Calories"));
                            recipeDetails.put("protein", recipesRs.getFloat("Protein"));
                            recipeDetails.put("carbs", recipesRs.getFloat("Carbs"));
                            recipeDetails.put("fats", recipesRs.getFloat("Fats"));
                            
                            dateMap.put(mealType, recipeDetails);
                        }
                    }
                    
                    // Display the meal plan
                    System.out.println("\n--- Meal Plan: " + planName + " ---");
                    System.out.println("From " + startDate + " to " + endDate + "\n");
                    
                    if (mealPlanDetails.isEmpty()) {
                        System.out.println("No recipes added to this meal plan.");
                        return;
                    }
                    
                    for (String date : mealPlanDetails.keySet()) {
                        System.out.println("Date: " + date);
                        
                        Map<String, Map<String, Object>> mealsForDay = mealPlanDetails.get(date);
                        for (String mealType : mealsForDay.keySet()) {
                            Map<String, Object> recipe = mealsForDay.get(mealType);
                            
                            System.out.println("  " + mealType + ": " + recipe.get("title"));
                            System.out.println("    Calories: " + recipe.get("calories") + 
                                           ", Protein: " + recipe.get("protein") + "g" +
                                           ", Carbs: " + recipe.get("carbs") + "g" +
                                           ", Fats: " + recipe.get("fats") + "g");
                        }
                        
                        System.out.println();
                    }
                }
            }
            
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error viewing meal plan details: " + e.getMessage());
        }
    }
    
    public void addRecipeToMealPlan() {
        try {
            // First, view available meal plans
            List<Object[]> plans = viewMealPlans();
            
            if (plans.isEmpty()) {
                return;
            }
            
            System.out.print("\nEnter Meal Plan ID to add a recipe: ");
            int planId = Integer.parseInt(scanner.nextLine());
            
            // Check if the plan exists and belongs to the current user
            String planSql = "SELECT PlanID FROM MealPlans WHERE PlanID = ? AND UserID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(planSql)) {
                pstmt.setInt(1, planId);
                pstmt.setInt(2, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Meal plan not found or does not belong to you.");
                    return;
                }
            }
            
            // View available recipes
            List<Object[]> recipes = viewRecipes("all");
            
            if (recipes.isEmpty()) {
                return;
            }
            
            System.out.print("\nEnter Recipe ID to add: ");
            int recipeId = Integer.parseInt(scanner.nextLine());
            
            // Check if the recipe exists
            String recipeSql = "SELECT RecipeID FROM Recipes WHERE RecipeID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(recipeSql)) {
                pstmt.setInt(1, recipeId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Recipe not found.");
                    return;
                }
            }
            
            System.out.print("Enter date for this recipe (YYYY-MM-DD): ");
            String mealDate = scanner.nextLine();
            
            System.out.print("Enter meal type (Breakfast/Lunch/Dinner/Snack): ");
            String mealType = scanner.nextLine();
            
            // Add recipe to meal plan
            String sql = "INSERT INTO MealPlanRecipes (PlanID, RecipeID, MealDate, MealType) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, planId);
                pstmt.setInt(2, recipeId);
                pstmt.setString(3, mealDate);
                pstmt.setString(4, mealType);
                
                pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                
                System.out.println("Recipe added to meal plan successfully!");
            }
            
        } catch (SQLException | NumberFormatException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error adding recipe to meal plan: " + e.getMessage());
        }
    }
    
    public void pantryManagement() {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        while (true) {
            System.out.println("\n--- Pantry Management ---");
            System.out.println("1. Add Ingredient to Pantry");
            System.out.println("2. View Pantry");
            System.out.println("3. Remove Ingredient from Pantry");
            System.out.println("4. Return to Main Menu");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addToPantry();
                    break;
                case "2":
                    viewPantry();
                    break;
                case "3":
                    removeFromPantry();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    
    public void addToPantry() {
        try {
            System.out.print("Enter ingredient name: ");
            String ingredientName = scanner.nextLine();
            
            // Check if the ingredient exists
            int ingredientId;
            String ingredientSql = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(ingredientSql)) {
                pstmt.setString(1, ingredientName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    ingredientId = rs.getInt("IngredientID");
                } else {
                    // Create new ingredient
                    System.out.print("Enter category for " + ingredientName + ": ");
                    String category = scanner.nextLine();
                    
                    System.out.print("Enter default unit: ");
                    String unit = scanner.nextLine();
                    
                    String newIngredientSql = "INSERT INTO Ingredients (Name, Category, Unit) VALUES (?, ?, ?)";
                    try (PreparedStatement ingredientStmt = conn.prepareStatement(newIngredientSql, Statement.RETURN_GENERATED_KEYS)) {
                        ingredientStmt.setString(1, ingredientName);
                        ingredientStmt.setString(2, category);
                        ingredientStmt.setString(3, unit);
                        
                        ingredientStmt.executeUpdate();
                        
                        ResultSet generatedKeys = ingredientStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            ingredientId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating ingredient failed, no ID obtained.");
                        }
                    }
                }
            }
            
            System.out.print("Enter quantity: ");
            float quantity = Float.parseFloat(scanner.nextLine());
            
            System.out.print("Enter expiry date (YYYY-MM-DD, optional): ");
            String expiryDate = scanner.nextLine();
            if (expiryDate.isEmpty()) {
                expiryDate = null;
            }
            
            // Current date for purchase date
            LocalDate purchaseDate = LocalDate.now();
            
            // Add to pantry
            String sql = "INSERT OR REPLACE INTO Pantry (UserID, IngredientID, Quantity, ExpiryDate, PurchaseDate) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser);
                pstmt.setInt(2, ingredientId);
                pstmt.setFloat(3, quantity);
                pstmt.setString(4, expiryDate);
                pstmt.setString(5, purchaseDate.toString());
                
                pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                
                System.out.println("Ingredient added to pantry successfully!");
            }
            
        } catch (SQLException | NumberFormatException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error adding to pantry: " + e.getMessage());
        }
    }
    
    public void viewPantry() {
        try {
            String sql = "SELECT i.Name, p.Quantity, i.Unit, p.ExpiryDate " +
                        "FROM Pantry p " +
                        "JOIN Ingredients i ON p.IngredientID = i.IngredientID " +
                        "WHERE p.UserID = ?";
            
            List<Object[]> pantryItems = new ArrayList<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] item = new Object[4];
                    item[0] = rs.getString("Name");
                    item[1] = rs.getFloat("Quantity");
                    item[2] = rs.getString("Unit");
                    item[3] = rs.getString("ExpiryDate") != null ? rs.getString("ExpiryDate") : "N/A";
                    
                    pantryItems.add(item);
                }
            }
            
            if (pantryItems.isEmpty()) {
                System.out.println("Pantry is empty.");
                return;
            }
            
            // Use TableFormatter to display pantry items
            String[] headers = {"Ingredient", "Quantity", "Unit", "Expiry Date"};
            int[] maxWidths = {20, 8, 8, 14};
            System.out.println(TableFormatter.formatTable(headers, pantryItems, maxWidths));
            
        } catch (SQLException e) {
            System.out.println("Error viewing pantry: " + e.getMessage());
        }
    }
    
    public void removeFromPantry() {
        viewPantry();
        
        System.out.print("\nEnter ingredient name to remove: ");
        String ingredientName = scanner.nextLine();
        
        try {
            String sql = "DELETE FROM Pantry " +
                        "WHERE UserID = ? AND IngredientID = (" +
                        "    SELECT IngredientID FROM Ingredients " +
                        "    WHERE Name = ?" +
                        ")";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser);
                pstmt.setString(2, ingredientName);
                
                int rowsAffected = pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                
                if (rowsAffected > 0) {
                    System.out.println("Ingredient removed from pantry successfully!");
                } else {
                    System.out.println("Ingredient not found in pantry.");
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error removing from pantry: " + e.getMessage());
        }
    }
    
    public void advancedRecipeSearch() {
        System.out.println("\n--- Advanced Recipe Search ---");
        
        Map<String, Object> searchCriteria = new HashMap<>();
        
        System.out.print("Enter cuisine (optional): ");
        String cuisine = scanner.nextLine().trim();
        if (!cuisine.isEmpty()) {
            searchCriteria.put("Cuisine", cuisine);
        }
        
        System.out.print("Enter difficulty level (Easy/Medium/Hard, optional): ");
        String difficulty = scanner.nextLine().trim();
        if (!difficulty.isEmpty()) {
            searchCriteria.put("DifficultyLevel", difficulty);
        }
        
        System.out.print("Maximum cooking time (minutes, optional): ");
        String maxCookTime = scanner.nextLine().trim();
        if (!maxCookTime.isEmpty()) {
            try {
                searchCriteria.put("MaxCookTime", Integer.parseInt(maxCookTime));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for cooking time. Ignoring this criterion.");
            }
        }
        
        System.out.print("Dietary tags (comma-separated, optional - e.g., Vegetarian, Gluten-Free): ");
        String dietaryTags = scanner.nextLine().trim();
        List<String> tags = new ArrayList<>();
        
        if (!dietaryTags.isEmpty()) {
            String[] tagArray = dietaryTags.split(",");
            for (String tag : tagArray) {
                tags.add(tag.trim());
            }
        }
        
        try {
            StringBuilder query = new StringBuilder("SELECT DISTINCT r.RecipeID, r.Title, r.Cuisine, r.DifficultyLevel, r.CookingTime, r.Servings FROM Recipes r WHERE 1=1 ");
            List<Object> params = new ArrayList<>();
            
            // Add search criteria to query
            if (searchCriteria.containsKey("Cuisine")) {
                query.append("AND r.Cuisine = ? ");
                params.add(searchCriteria.get("Cuisine"));
            }
            
            if (searchCriteria.containsKey("DifficultyLevel")) {
                query.append("AND r.DifficultyLevel = ? ");
                params.add(searchCriteria.get("DifficultyLevel"));
            }
            
            if (searchCriteria.containsKey("MaxCookTime")) {
                query.append("AND r.CookingTime <= ? ");
                params.add(searchCriteria.get("MaxCookTime"));
            }
            
            // Handle tags with a subquery
            if (!tags.isEmpty()) {
                query.append("AND r.RecipeID IN (SELECT rt.RecipeID FROM RecipeTags rt JOIN Tags t ON rt.TagID = t.TagID WHERE t.Name IN (");
                
                for (int i = 0; i < tags.size(); i++) {
                    if (i > 0) {
                        query.append(", ");
                    }
                    query.append("?");
                    params.add(tags.get(i));
                }
                
                query.append(")) ");
            }
            
            // Only public recipes
            query.append("AND r.IsPublic = 1");
            
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                
                ResultSet rs = pstmt.executeQuery();
                
                List<Object[]> results = new ArrayList<>();
                while (rs.next()) {
                    Object[] recipe = new Object[6];
                    recipe[0] = rs.getInt("RecipeID");
                    recipe[1] = rs.getString("Title");
                    recipe[2] = rs.getString("Cuisine");
                    recipe[3] = rs.getString("DifficultyLevel");
                    recipe[4] = rs.getString("CookingTime");
                    recipe[5] = rs.getInt("Servings");
                    
                    results.add(recipe);
                }
                
                if (results.isEmpty()) {
                    System.out.println("No recipes found matching your criteria.");
                    return;
                }
                
                // Use TableFormatter to display search results
                String[] headers = {"ID", "Title", "Cuisine", "Difficulty", "Cook Time", "Servings"};
                int[] maxWidths = {5, 20, 10, 10, 11, 8};
                System.out.println(TableFormatter.formatTable(headers, results, maxWidths));
            }
            
        } catch (SQLException e) {
            System.out.println("Error performing advanced search: " + e.getMessage());
        }
    }
    
    public void pantryBasedRecommendations() {
        try {
            // Get user's pantry ingredients
            String pantryIngredientsSql = "SELECT IngredientID FROM Pantry WHERE UserID = ?";
            List<Integer> pantryIngredients = new ArrayList<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(pantryIngredientsSql)) {
                pstmt.setInt(1, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    pantryIngredients.add(rs.getInt("IngredientID"));
                }
            }
            
            if (pantryIngredients.isEmpty()) {
                System.out.println("Your pantry is empty. Add some ingredients first.");
                return;
            }
            
            // Create a list to store all recommendations
            List<Object[]> recommendations = new ArrayList<>();
            
            // Find recipes that use these ingredients
            String recipesSql = "SELECT r.RecipeID, r.Title, " +
                              "COUNT(DISTINCT CASE WHEN ri.IngredientID IN (" + String.join(",", Collections.nCopies(pantryIngredients.size(), "?")) + ") THEN ri.IngredientID END) AS IngredientsMatched, " +
                              "COUNT(DISTINCT ri.IngredientID) AS TotalIngredients " +
                              "FROM Recipes r " +
                              "JOIN RecipeIngredients ri ON r.RecipeID = ri.RecipeID " +
                              "GROUP BY r.RecipeID " +
                              "HAVING IngredientsMatched > 0 " +
                              "ORDER BY (IngredientsMatched * 1.0 / TotalIngredients) DESC, IngredientsMatched DESC " +
                              "LIMIT 10";
            
            // Use a forward-only ResultSet (SQLite default)
            try (PreparedStatement pstmt = conn.prepareStatement(recipesSql)) {
                for (int i = 0; i < pantryIngredients.size(); i++) {
                    pstmt.setInt(i + 1, pantryIngredients.get(i));
                }
                
                // Process results directly as we read them
                ResultSet rs = pstmt.executeQuery();
                boolean hasResults = false;
                
                while (rs.next()) {
                    hasResults = true;
                    Object[] recipe = new Object[4];
                    recipe[0] = rs.getInt("RecipeID");
                    recipe[1] = rs.getString("Title");
                    recipe[2] = rs.getInt("IngredientsMatched") + " of " + rs.getInt("TotalIngredients");
                    double matchPercent = (rs.getInt("IngredientsMatched") * 100.0) / rs.getInt("TotalIngredients");
                    recipe[3] = String.format("%.1f%%", matchPercent);
                    
                    recommendations.add(recipe);
                }
                
                if (!hasResults) {
                    System.out.println("No recipes found that match your pantry ingredients.");
                    return;
                }
                
                // Display the results
                System.out.println("\n--- Recipes You Can Make With Your Pantry ---");
                String[] headers = {"ID", "Title", "Ingredients", "Match Percentage"};
                int[] maxWidths = {5, 20, 15, 15};
                System.out.println(TableFormatter.formatTable(headers, recommendations, maxWidths));
                System.out.println("\nUse 'View Recipe Details' to see the full recipe information.");
            }
            
        } catch (SQLException e) {
            System.out.println("Error generating pantry-based recommendations: " + e.getMessage());
        }
    }
    
    public void enhanceRecipeFeedback() {
        try {
            // First, view available recipes
            List<Object[]> recipes = viewRecipes("all");
            
            if (recipes.isEmpty()) {
                return;
            }
            
            System.out.print("\nEnter Recipe ID to provide feedback: ");
            int recipeId = Integer.parseInt(scanner.nextLine());
            
            // Check if the recipe exists
            String recipeSql = "SELECT Title FROM Recipes WHERE RecipeID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(recipeSql)) {
                pstmt.setInt(1, recipeId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Recipe not found.");
                    return;
                }
                
                String recipeTitle = rs.getString("Title");
                System.out.println("\nProviding feedback for: " + recipeTitle);
            }
            
            // Check if user has already given feedback for this recipe
            String checkFeedbackSql = "SELECT FeedbackID FROM RecipeFeedback WHERE RecipeID = ? AND UserID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkFeedbackSql)) {
                pstmt.setInt(1, recipeId);
                pstmt.setInt(2, currentUser);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    System.out.println("You have already provided feedback for this recipe.");
                    System.out.print("Do you want to update your feedback? (yes/no): ");
                    String update = scanner.nextLine().toLowerCase();
                    if (!update.equals("yes")) {
                        return;
                    }
                }
            }
            
            System.out.print("Rate this recipe (1-5 stars): ");
            int rating = Integer.parseInt(scanner.nextLine());
            if (rating < 1 || rating > 5) {
                System.out.println("Rating must be between 1 and 5.");
                return;
            }
            
            System.out.print("Rate the difficulty (1-5, where 1 is very easy and 5 is very difficult): ");
            int difficultyRating = Integer.parseInt(scanner.nextLine());
            if (difficultyRating < 1 || difficultyRating > 5) {
                System.out.println("Difficulty rating must be between 1 and 5.");
                return;
            }
            
            System.out.print("How long did it actually take to cook (minutes)? ");
            String timeEstimate = scanner.nextLine();
            if (timeEstimate.isEmpty()) {
                timeEstimate = null;
            }
            
            System.out.print("Add a comment (optional): ");
            String comment = scanner.nextLine();
            if (comment.isEmpty()) {
                comment = null;
            }
            
            // Insert or update feedback
            String feedbackSql = "INSERT OR REPLACE INTO RecipeFeedback " +
                              "(RecipeID, UserID, Rating, DifficultyRating, ActualCookingTime, Comment) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(feedbackSql)) {
                pstmt.setInt(1, recipeId);
                pstmt.setInt(2, currentUser);
                pstmt.setInt(3, rating);
                pstmt.setInt(4, difficultyRating);
                pstmt.setString(5, timeEstimate);
                pstmt.setString(6, comment);
                
                pstmt.executeUpdate();
                
                // Commit the transaction
                conn.commit();
                
                System.out.println("Feedback submitted successfully!");
            }
            
        } catch (SQLException | NumberFormatException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            System.out.println("Error submitting feedback: " + e.getMessage());
        }
    }
    
    private void printMenuHeader(String title) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BOLD = "\u001B[1m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_GREEN = "\u001B[32m";
        
        // Decorative top border
        System.out.println("\n" + ANSI_YELLOW + "╔" + "═".repeat(title.length() + 4) + "╗" + ANSI_RESET);
        
        // Title with decorative elements
        System.out.println(ANSI_YELLOW + "║ " + ANSI_BOLD + ANSI_CYAN + "✦ " + ANSI_GREEN + title + ANSI_CYAN + " ✦" + ANSI_RESET + ANSI_YELLOW + " ║" + ANSI_RESET);
        
        // Decorative bottom border
        System.out.println(ANSI_YELLOW + "╚" + "═".repeat(title.length() + 4) + "╝" + ANSI_RESET + "\n");
        
        // Add decorative elements based on the menu type
        switch (title) {
            case "ChefSync Recipe Manager":
                System.out.println(ANSI_CYAN + "    🍳  Welcome to your culinary journey!  🥘" + ANSI_RESET + "\n");
                break;
            case "ChefSync Dashboard":
                System.out.println(ANSI_CYAN + "    🥗  Your personal cooking command center  🍽️" + ANSI_RESET + "\n");
                break;
            case "Profile Management":
                System.out.println(ANSI_CYAN + "    👤  Manage your chef profile  👨‍🍳" + ANSI_RESET + "\n");
                break;
            case "Recipe Management":
                System.out.println(ANSI_CYAN + "    📝  Create and manage your recipes  📖" + ANSI_RESET + "\n");
                break;
            case "Meal Planning":
                System.out.println(ANSI_CYAN + "    📅  Plan your meals ahead  🍱" + ANSI_RESET + "\n");
                break;
            case "Pantry Management":
                System.out.println(ANSI_CYAN + "    🥫  Keep track of your ingredients  🥕" + ANSI_RESET + "\n");
                break;
            case "Advanced Recipe Search":
                System.out.println(ANSI_CYAN + "    🔍  Find your perfect recipe  📚" + ANSI_RESET + "\n");
                break;
            case "Recipe Feedback":
                System.out.println(ANSI_CYAN + "    ⭐  Share your cooking experience  💭" + ANSI_RESET + "\n");
                break;
        }
    }

    private void printMenuItem(String number, String text) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        
        // Enhanced menu item display with decorative elements
        System.out.println(ANSI_YELLOW + "  " + ANSI_CYAN + number + ANSI_RESET + ". " + ANSI_GREEN + text + ANSI_RESET);
    }

    // Main application loop
    public void run() {
        while (true) {
            if (currentUser == null) {
                printMenuHeader("ChefSync Recipe Manager");
                printMenuItem("1", "Register");
                printMenuItem("2", "Login");
                printMenuItem("3", "Exit");
                
                System.out.print("\nEnter your choice: ");
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1":
                        register();
                        break;
                    case "2":
                        login();
                        break;
                    case "3":
                        close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } else {
                printMenuHeader("ChefSync Dashboard");
                printMenuItem("1", "Profile Management");
                printMenuItem("2", "Add Recipe");
                printMenuItem("3", "View Recipes");
                printMenuItem("4", "Meal Planning");
                printMenuItem("5", "Pantry Management");
                printMenuItem("6", "Advanced Recipe Search");
                printMenuItem("7", "Pantry-Based Recommendations");
                printMenuItem("8", "Recipe Feedback");
                printMenuItem("9", "Logout");
                
                System.out.print("\nEnter your choice: ");
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1":
                        profileManagement();
                        break;
                    case "2":
                        addRecipe();
                        break;
                    case "3":
                        viewRecipesMenu();
                        break;
                    case "4":
                        mealPlanning();
                        break;
                    case "5":
                        pantryManagement();
                        break;
                    case "6":
                        advancedRecipeSearch();
                        break;
                    case "7":
                        pantryBasedRecommendations();
                        break;
                    case "8":
                        enhanceRecipeFeedback();
                        break;
                    case "9":
                        currentUser = null;
                        System.out.println("Logged out successfully!");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }
    
    private void close() {
        if (conn != null) {
            try {
                // Commit any pending transactions
                try {
                    conn.commit();
                } catch (SQLException e) {
                    System.err.println("Error committing final transactions: " + e.getMessage());
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        System.err.println("Error rolling back: " + rollbackEx.getMessage());
                    }
                }
                
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        if (scanner != null) {
            scanner.close();
        }
    }
}