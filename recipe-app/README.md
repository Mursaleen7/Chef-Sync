# ChefSync Recipe Manager

ChefSync is a Java-based recipe management application that helps users manage recipes, create meal plans, track pantry items, and more.

## Features

1. **Profile Management** - Create and manage user profiles
2. **Recipe Management** - Add, view, and edit recipes
3. **Meal Planning** - Create meal plans and schedule recipes
4. **Pantry Management** - Track ingredients in your pantry
5. **Advanced Recipe Search** - Search for recipes based on various criteria
6. **Pantry-Based Recommendations** - Get recipe recommendations based on what's in your pantry
7. **Recipe Feedback** - Rate and review recipes

## Project Structure

```
recipe-app/
├── lib/                    # Libraries and dependencies
│   └── sqlite-jdbc-3.49.1.0.jar   # SQLite JDBC driver
├── src/                    # Source code
│   └── main/java/com/chefsync/
│       ├── DBInit.java     # Database initialization
│       ├── RecipeApp.java  # Main application class
│       ├── RecipeManager.java # Core functionality
│       └── TableFormatter.java # Utility for formatting tables
├── recipe_manager.db       # SQLite database
├── schema.sql              # Database schema
├── test_features.sh        # Test script
└── README.md               # This file
```

## Requirements

- Java 8 or higher (Java 11+ recommended)
- SQLite JDBC driver (included in the `lib` directory)

## How to Run

1. **Open a terminal and navigate to the `recipe-app` directory:**
   ```sh
   cd recipe-app
   ```

2. **Compile the Java source files:**
   ```sh
   javac -d . src/main/java/com/chefsync/*.java
   ```

3. **Run the application:**
   ```sh
   java -cp ".:lib/sqlite-jdbc-3.49.1.0.jar" com.chefsync.RecipeApp
   ```
   - On Windows, use `;` instead of `:` for the classpath:
     ```sh
     java -cp ".;lib/sqlite-jdbc-3.49.1.0.jar" com.chefsync.RecipeApp
     ```

4. **Follow the on-screen menu to register, login, and use the features.**

## Troubleshooting
- If you see `Error: Could not find or load main class com.chefsync.RecipeApp`, make sure you are in the `recipe-app` directory and compiled with the correct `-d .` flag.
- If the database file is missing, it will be created automatically on first run.
- If you have issues with the JDBC driver, ensure the jar file is present in the `lib` directory and the classpath is set correctly.

## Testing

A test script is included to help verify all features are working correctly:

```sh
./test_features.sh
```

This will guide you through testing all the features of the application.

## Database

The application uses an SQLite database to store all data. The schema is defined in `schema.sql` and the database file is `recipe_manager.db`. If the database file does not exist, it will be created when the application starts.


## License

This project is available for educational purposes. 