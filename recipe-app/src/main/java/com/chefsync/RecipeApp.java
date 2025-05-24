package com.chefsync;

import java.sql.Connection;
import java.sql.DriverManager;

public class RecipeApp {
    
    private static void printWelcomeScreen() {
        // Clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        // ANSI color codes
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_BOLD = "\u001B[1m";
        
        // Welcome message
        String[] welcomeMessage = {
            "",
            ANSI_YELLOW + "╔════════════════════════════════════════════════════════════╗" + ANSI_RESET,
            ANSI_YELLOW + "║" + ANSI_BOLD + ANSI_GREEN + "              Welcome to ChefSync Recipe Manager!              " + ANSI_RESET + ANSI_YELLOW + "║" + ANSI_RESET,
            ANSI_YELLOW + "╚════════════════════════════════════════════════════════════╝" + ANSI_RESET,
            "",
            ANSI_PURPLE + "Your Ultimate Recipe Management Solution" + ANSI_RESET,
            "",
            ANSI_BLUE + "Features:" + ANSI_RESET,
            ANSI_GREEN + "• Recipe Management" + ANSI_RESET,
            ANSI_GREEN + "• Meal Planning" + ANSI_RESET,
            ANSI_GREEN + "• Pantry Management" + ANSI_RESET,
            ANSI_GREEN + "• Recipe Recommendations" + ANSI_RESET,
            ANSI_GREEN + "• User Feedback System" + ANSI_RESET,
            "",
            ANSI_YELLOW + "══════════════════════════════════════════════════════════════" + ANSI_RESET,
            "",
            // Decorative elements
            ANSI_CYAN + "    🍳  " + ANSI_RESET + ANSI_YELLOW + "  Cooking Made Easy  " + ANSI_RESET + ANSI_CYAN + "  🥘" + ANSI_RESET,
            ANSI_CYAN + "    🥗  " + ANSI_RESET + ANSI_YELLOW + "  Plan Your Meals    " + ANSI_RESET + ANSI_CYAN + "  🍽️" + ANSI_RESET,
            ANSI_CYAN + "    🥕  " + ANSI_RESET + ANSI_YELLOW + "  Manage Your Pantry " + ANSI_RESET + ANSI_CYAN + "  🥬" + ANSI_RESET,
            "",
            ANSI_YELLOW + "╔════════════════════════════════════════════════════════════╗" + ANSI_RESET,
            ANSI_YELLOW + "║" + ANSI_BOLD + ANSI_GREEN + "                    Let's Get Cooking!                    " + ANSI_RESET + ANSI_YELLOW + "║" + ANSI_RESET,
            ANSI_YELLOW + "╚════════════════════════════════════════════════════════════╝" + ANSI_RESET,
            ""
        };
        
        // Print welcome message
        for (String line : welcomeMessage) {
            System.out.println(line);
        }
        
        // Add a small delay for dramatic effect
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) {
        printWelcomeScreen();
        
        try {
        // Create and run the recipe manager
        RecipeManager manager = new RecipeManager();
        manager.run();
        } catch (Exception e) {
            System.err.println("Error running ChefSync Recipe Manager: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\nThank you for using ChefSync Recipe Manager!");
        }
    }
} 