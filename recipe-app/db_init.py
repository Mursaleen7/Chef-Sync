import sqlite3
import os
import hashlib

def hash_password(password):
    """
    Hash password using SHA-256 algorithm.
    
    Args:
        password (str): Plain text password to be hashed
    
    Returns:
        str: Hashed password as a hexadecimal string
    """
    return hashlib.sha256(password.encode()).hexdigest()

def initialize_database(db_path='recipe_manager.db', schema_path='schema.sql'):
    """
    Initialize the SQLite database by:
    1. Removing existing database if it exists
    2. Creating new database with schema from SQL file
    3. Inserting sample data
    
    Args:
        db_path (str): Path to the database file
        schema_path (str): Path to the SQL schema file
    """
    try:
        # Remove existing database if it exists
        if os.path.exists(db_path):
            os.remove(db_path)
        
        # Connect to the database
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # Read and execute schema from SQL file
        with open(schema_path, 'r') as schema_file:
            schema_script = schema_file.read()
            cursor.executescript(schema_script)
        
        # Insert sample users with hashed passwords
        sample_users = [
            ('demo_user', 'demo@example.com', hash_password('password123'), 'Demo User Bio'),
            ('chef_master', 'chef@example.com', hash_password('cookmaster'), 'Professional Chef')
        ]
        cursor.executemany('''
            INSERT INTO Users (Username, Email, Password, Bio) 
            VALUES (?, ?, ?, ?)
        ''', sample_users)
        
        # Insert sample ingredients
        sample_ingredients = [
            ('Chicken Breast', 'Protein', 'grams', 'High protein, low fat'),
            ('Brown Rice', 'Carbohydrate', 'grams', 'Whole grain, nutritious'),
            ('Olive Oil', 'Oil', 'ml', 'Heart-healthy fat'),
            ('Tomato', 'Vegetable', 'pieces', 'Rich in vitamins'),
            ('Garlic', 'Herb', 'cloves', 'Flavor enhancer')
        ]
        cursor.executemany('''
            INSERT INTO Ingredients (Name, Category, Unit, NutritionalInfo) 
            VALUES (?, ?, ?, ?)
        ''', sample_ingredients)
        
        # Insert sample tags
        sample_tags = [
            ('Healthy', 'Nutritious recipes'),
            ('Quick Meal', 'Recipes under 30 minutes'),
            ('Vegetarian', 'No meat recipes'),
            ('Gluten-Free', 'Suitable for gluten-sensitive diets')
        ]
        cursor.executemany('''
            INSERT INTO Tags (Name, Description) 
            VALUES (?, ?)
        ''', sample_tags)
        
        # Commit changes and close connection
        conn.commit()
        conn.close()
        
        print(f"Database {db_path} initialized successfully!")
    
    except sqlite3.Error as e:
        print(f"Error initializing database: {e}")
    except FileNotFoundError:
        print(f"Schema file {schema_path} not found. Please check the file path.")

def main():
    """
    Main function to execute database initialization.
    Can be used to run the script directly.
    """
    initialize_database()

if __name__ == "__main__":
    main()