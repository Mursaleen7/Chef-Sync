# ChefSync Recipe Manager

A comprehensive Java-based recipe management system designed to streamline meal planning, recipe organization, and pantry management.

## Table of Contents
- [Features](#features)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Technical Details](#technical-details)
- [Troubleshooting](#troubleshooting)
- [Testing](#testing)
- [Future Enhancements](#future-enhancements)
- [License](#license)

## Features

### Core Features
- **Profile Management**: User registration, authentication, and preferences
- **Recipe Management**: Create, store, and organize recipes with detailed instructions
- **Meal Planning**: Create weekly/monthly meal plans and generate shopping lists
- **Pantry Management**: Track ingredients, expiry dates, and quantities
- **Smart Recommendations**: Get recipe suggestions based on available ingredients
- **Advanced Search**: Find recipes by ingredients, cuisine type, cooking time, and more
- **Feedback System**: Rate and review recipes, share modifications

### Detailed Capabilities
- Recipe creation with nutritional information tracking
- Ingredient management and categorization
- Public/private recipe settings
- Weekly/monthly meal planning
- Nutritional goals tracking
- Automatic shopping list generation
- Expiry date tracking for pantry items
- Dietary restrictions filtering
- Difficulty level filtering

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

## Installation

1. **Clone the repository and navigate to the project directory:**
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

## Usage

### Getting Started
1. Register a new account or login with existing credentials
2. Navigate through the main menu to access different features
3. Start by adding recipes or managing your pantry

### Best Practices
- Keep pantry inventory updated for accurate recommendations
- Use tags for better recipe organization
- Regularly update meal plans
- Provide feedback on recipes for better community engagement

## Technical Details

### Architecture
- Java-based application
- SQLite database for data persistence
- Modular architecture for maintainability

### Database Schema
The system uses SQLite with the following main tables:
- Users
- Recipes
- Ingredients
- Pantry
- MealPlans
- RecipeFeedback
- Tags

### Security Features
- Password hashing using SHA-256
- SQL injection prevention
- Input validation
- Transaction management

### Performance Considerations
- Efficient database queries
- Connection pooling
- Transaction management
- Proper resource cleanup

## Troubleshooting

Common issues and solutions:
- **Error: Could not find or load main class com.chefsync.RecipeApp**
  - Ensure you are in the `recipe-app` directory
  - Verify compilation with correct `-d .` flag
- **Missing Database**
  - Database file will be created automatically on first run
- **JDBC Driver Issues**
  - Verify jar file presence in `lib` directory
  - Check classpath configuration

## Testing

A test script is included to verify all features:
```sh
./test_features.sh
```

## Future Enhancements

### Planned Features
- Mobile application interface
- Social sharing capabilities
- Recipe scaling functionality
- Advanced nutritional analysis
- Integration with grocery delivery services

### Technical Improvements
- REST API implementation
- Cloud database integration
- Real-time collaboration features
- Enhanced search algorithms
- Machine learning for better recommendations

## License

This project is available for educational purposes. 
