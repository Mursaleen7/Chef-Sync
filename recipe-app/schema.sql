PRAGMA foreign_keys = ON;

-- Users Table: Stores user information
CREATE TABLE Users (
    UserID INTEGER PRIMARY KEY AUTOINCREMENT,
    Username TEXT NOT NULL UNIQUE,
    Email TEXT NOT NULL UNIQUE,
    Password TEXT NOT NULL, -- Stores hashed password
    Bio TEXT,
    CreatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE Categories (
    CategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name TEXT NOT NULL UNIQUE,
    Description TEXT
);

-- Ingredients Table: Stores details of individual ingredients
CREATE TABLE Ingredients (
    IngredientID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name TEXT NOT NULL UNIQUE,
    Category TEXT, -- e.g., Dairy, Vegetable, Spice
    Unit TEXT, -- Base unit for general reference, e.g., grams, ml, piece
    NutritionalInfo TEXT -- General nutritional information
);

-- Recipes Table: Stores recipe details
CREATE TABLE Recipes (
    RecipeID INTEGER PRIMARY KEY AUTOINCREMENT,
    UserID INTEGER NOT NULL, -- Foreign key to Users table
    Title TEXT NOT NULL,
    Instructions TEXT NOT NULL,
    PrepTime TEXT, -- e.g., '30 minutes'
    CookingTime TEXT, -- e.g., '1 hour'
    Servings INTEGER,
    Calories REAL, -- Per serving
    Protein REAL, -- Per serving (grams)
    Carbs REAL, -- Per serving (grams)
    Fats REAL, -- Per serving (grams)
    Cuisine TEXT, -- e.g., Italian, Mexican
    DifficultyLevel TEXT, -- e.g., 'Easy', 'Medium', 'Hard'
    IsPublic INTEGER NOT NULL DEFAULT 1, -- 0 for private, 1 for public
    CreatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Tags Table: For categorizing and searching recipes
CREATE TABLE Tags (
    TagID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name TEXT NOT NULL UNIQUE,
    Description TEXT
);

-- Pantry Table: Tracks ingredients a user currently has
CREATE TABLE Pantry (
    PantryItemID INTEGER PRIMARY KEY AUTOINCREMENT,
    UserID INTEGER NOT NULL,
    IngredientID INTEGER NOT NULL,
    Quantity REAL NOT NULL,
    Unit TEXT, -- Unit for this specific quantity (e.g., 2 items, 500 grams)
    PurchaseDate TEXT,
    ExpiryDate TEXT,
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID)
);

-- MealPlans Table: Allows users to plan their meals
CREATE TABLE MealPlans (
    PlanID INTEGER PRIMARY KEY AUTOINCREMENT,
    UserID INTEGER NOT NULL,
    Name TEXT NOT NULL, -- e.g., 'Weekly Fitness Plan'
    Description TEXT,
    StartDate TEXT NOT NULL,
    EndDate TEXT NOT NULL,
    CreatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- RecipeFeedback Table: Stores user feedback for recipes
CREATE TABLE RecipeFeedback (
    FeedbackID INTEGER PRIMARY KEY AUTOINCREMENT,
    RecipeID INTEGER NOT NULL,
    UserID INTEGER NOT NULL,
    Rating INTEGER, -- e.g., 1 to 5 stars
    Comment TEXT,
    ActualCookingTime TEXT, -- User reported cooking time
    DifficultyRating TEXT, -- User reported difficulty
    CreatedAt TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Junction Tables for Many-to-Many Relationships

-- RecipeIngredients Table: Links Recipes with their Ingredients and specifies quantity/unit for the recipe
CREATE TABLE RecipeIngredients (
    RecipeID INTEGER NOT NULL,
    IngredientID INTEGER NOT NULL,
    Quantity REAL, -- Quantity of the ingredient needed for this recipe
    Unit TEXT, -- Unit for the quantity in this recipe context (e.g., 'cups', 'tbsp')
    Notes TEXT, -- Optional notes about the ingredient in this recipe
    PRIMARY KEY (RecipeID, IngredientID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (IngredientID) REFERENCES Ingredients(IngredientID)
);

-- RecipeTags Table: Links Recipes with Tags
CREATE TABLE RecipeTags (
    RecipeID INTEGER NOT NULL,
    TagID INTEGER NOT NULL,
    PRIMARY KEY (RecipeID, TagID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (TagID) REFERENCES Tags(TagID)
);

-- RecipeCategories junction table
CREATE TABLE RecipeCategories (
    RecipeCategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
    RecipeID INTEGER NOT NULL,
    CategoryID INTEGER NOT NULL,
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID),
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    UNIQUE(RecipeID, CategoryID)
);

-- RecipeMealPlans Table: Links Recipes to MealPlans
-- A recipe can be in multiple meal plans, and a meal plan can have multiple recipes.
CREATE TABLE RecipeMealPlans (
    PlanID INTEGER NOT NULL,
    RecipeID INTEGER NOT NULL,
    -- Attributes like ScheduledDate or MealType (Breakfast, Lunch, Dinner) for a recipe within a specific plan
    -- can be added here if more detailed scheduling is needed per recipe instance in a plan.
    -- For a simpler N:M link, these are omitted but can be added.
    -- Example:
    -- ScheduledDate TEXT,
    -- MealType TEXT, 
    PRIMARY KEY (PlanID, RecipeID), -- If adding more attributes, this PK might need adjustment or a surrogate key.
    FOREIGN KEY (PlanID) REFERENCES MealPlans(PlanID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);

-- MealPlanRecipes Table: More detailed table for meal planning with specific dates and meal types
CREATE TABLE MealPlanRecipes (
    PlanID INTEGER NOT NULL,
    RecipeID INTEGER NOT NULL,
    MealDate TEXT NOT NULL, -- Date for this meal (YYYY-MM-DD)
    MealType TEXT NOT NULL, -- Breakfast, Lunch, Dinner, Snack, etc.
    PRIMARY KEY (PlanID, RecipeID, MealDate, MealType),
    FOREIGN KEY (PlanID) REFERENCES MealPlans(PlanID),
    FOREIGN KEY (RecipeID) REFERENCES Recipes(RecipeID)
);

-- Insert default categories
INSERT OR IGNORE INTO Categories (Name, Description) VALUES
('Breakfast', 'Morning meals and dishes'),
('Lunch', 'Midday meals and dishes'),
('Dinner', 'Evening meals and dishes'),
('Appetizer', 'Small dishes served before a meal'),
('Dessert', 'Sweet dishes served after a meal'),
('Snack', 'Small portions of food between meals'),
('Vegetarian', 'Dishes without meat'),
('Vegan', 'Dishes without animal products'),
('Gluten-Free', 'Dishes without gluten'),
('Dairy-Free', 'Dishes without dairy products'),
('Keto', 'Low-carb, high-fat dishes'),
('Paleo', 'Dishes based on foods presumed to be available to paleolithic humans'),
('Low-Carb', 'Dishes with reduced carbohydrate content'),
('High-Protein', 'Dishes with high protein content');

-- Insert default ingredients
INSERT OR IGNORE INTO Ingredients (Name, Category) VALUES
('Salt', 'Spices'),
('Pepper', 'Spices'),
('Olive Oil', 'Oils'),
('Garlic', 'Vegetables'),
('Onion', 'Vegetables'),
('Tomato', 'Vegetables'),
('Chicken Breast', 'Meat'),
('Ground Beef', 'Meat'),
('Rice', 'Grains'),
('Pasta', 'Grains'),
('Flour', 'Baking'),
('Sugar', 'Baking'),
('Eggs', 'Dairy'),
('Milk', 'Dairy'),
('Butter', 'Dairy'),
('Cheese', 'Dairy'); 