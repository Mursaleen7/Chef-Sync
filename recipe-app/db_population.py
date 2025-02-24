import sqlite3
import random
from datetime import datetime, timedelta
import hashlib
import os

def hash_password(password):
    """Hash password using SHA-256"""
    return hashlib.sha256(password.encode()).hexdigest()

class DatabasePopulator:
    def __init__(self, db_path='recipe_manager.db'):
        self.db_path = db_path
        self.conn = sqlite3.connect(db_path)
        self.cursor = self.conn.cursor()

    def generate_users(self):
        """Generate 15 detailed user profiles"""
        users = [
            {
                'username': 'culinary_artist', 
                'email': 'artist@chefsync.com', 
                'password': 'CulinaryArt2024!', 
                'bio': 'Professional chef exploring global cuisines'
            },
            {
                'username': 'home_cook_hero', 
                'email': 'hero@chefsync.com', 
                'password': 'HomeCooking123!', 
                'bio': 'Family meal enthusiast and weekend baker'
            },
            {
                'username': 'vegan_explorer', 
                'email': 'vegan@chefsync.com', 
                'password': 'PlantPower2024!', 
                'bio': 'Plant-based recipe creator and nutrition advocate'
            },
            {
                'username': 'fitness_foodie', 
                'email': 'fitness@chefsync.com', 
                'password': 'HealthyEats456!', 
                'bio': 'Fitness trainer specializing in nutrition plans'
            },
            {
                'username': 'global_gastronome', 
                'email': 'global@chefsync.com', 
                'password': 'WorldFood789!', 
                'bio': 'Travel and food blogger exploring international cuisines'
            },
            {
                'username': 'budget_chef', 
                'email': 'budget@chefsync.com', 
                'password': 'EconomicalEats2024!', 
                'bio': 'Master of delicious, budget-friendly meals'
            },
            {
                'username': 'dessert_maestro', 
                'email': 'dessert@chefsync.com', 
                'password': 'SweetTreats123!', 
                'bio': 'Pastry chef and dessert recipe innovator'
            },
            {
                'username': 'spice_master', 
                'email': 'spice@chefsync.com', 
                'password': 'SpicyWorld456!', 
                'bio': 'Spice blend expert and international cuisine enthusiast'
            },
            {
                'username': 'quick_meals_queen', 
                'email': 'quickmeals@chefsync.com', 
                'password': 'FastFood789!', 
                'bio': 'Busy professional specializing in 30-minute meals'
            },
            {
                'username': 'organic_gardener', 
                'email': 'organic@chefsync.com', 
                'password': 'FarmToTable2024!', 
                'bio': 'Organic gardener creating farm-fresh recipes'
            },
            {
                'username': 'keto_king', 
                'email': 'keto@chefsync.com', 
                'password': 'KetoLife123!', 
                'bio': 'Low-carb diet expert and recipe developer'
            },
            {
                'username': 'mediterranean_maven', 
                'email': 'mediterranean@chefsync.com', 
                'password': 'HealthyMed456!', 
                'bio': 'Mediterranean diet specialist and nutritionist'
            },
            {
                'username': 'gluten_free_guru', 
                'email': 'glutenfree@chefsync.com', 
                'password': 'GlutenFree789!', 
                'bio': 'Gluten-free lifestyle advocate and recipe creator'
            },
            {
                'username': 'pressure_cooker_pro', 
                'email': 'pressurecooker@chefsync.com', 
                'password': 'QuickCook2024!', 
                'bio': 'Pressure cooker and instant pot recipe specialist'
            },
            {
                'username': 'baking_enthusiast', 
                'email': 'baking@chefsync.com', 
                'password': 'SweetAndSavory123!', 
                'bio': 'Amateur baker exploring complex baking techniques'
            }
        ]

        for user in users:
            try:
                self.cursor.execute('''
                    INSERT INTO Users (Username, Email, Password, Bio) 
                    VALUES (?, ?, ?, ?)
                ''', (
                    user['username'], 
                    user['email'], 
                    hash_password(user['password']), 
                    user['bio']
                ))
            except sqlite3.IntegrityError:
                print(f"User {user['username']} already exists.")

        self.conn.commit()

    def generate_ingredients(self):
        """
        Create a comprehensive and diverse ingredient list.
        Covers multiple food categories with nutritional insights.
        """
        ingredients = [
            # Proteins
            ('Chicken Breast', 'Protein', 'grams', 'Lean protein source, low in fat'),
            ('Salmon', 'Protein', 'grams', 'Rich in omega-3 fatty acids'),
            ('Tofu', 'Protein', 'grams', 'Plant-based complete protein'),
            ('Beef Sirloin', 'Protein', 'grams', 'High-quality lean red meat'),
            
            # Vegetables
            ('Spinach', 'Vegetable', 'cups', 'Iron-rich leafy green'),
            ('Bell Pepper', 'Vegetable', 'pieces', 'High in vitamin C'),
            ('Zucchini', 'Vegetable', 'pieces', 'Low-calorie, high fiber'),
            ('Cauliflower', 'Vegetable', 'grams', 'Versatile cruciferous vegetable'),
            
            # Grains
            ('Quinoa', 'Grain', 'grams', 'Complete protein grain'),
            ('Brown Rice', 'Grain', 'grams', 'Whole grain with complex carbohydrates'),
            ('Whole Wheat Pasta', 'Grain', 'grams', 'High fiber alternative'),
            
            # Dairy
            ('Greek Yogurt', 'Dairy', 'ml', 'Probiotic-rich protein source'),
            ('Feta Cheese', 'Dairy', 'grams', 'Tangy Mediterranean cheese')
        ]

        for ingredient in ingredients:
            try:
                self.cursor.execute('''
                    INSERT INTO Ingredients 
                    (Name, Category, Unit, NutritionalInfo) 
                    VALUES (?, ?, ?, ?)
                ''', ingredient)
            except sqlite3.IntegrityError:
                print(f"Ingredient {ingredient[0]} already exists.")

        self.conn.commit()

    def generate_tags(self):
        """
        Create a diverse set of recipe tags to categorize and filter recipes.
        Represents various dietary preferences and cooking styles.
        """
        tags = [
            ('Healthy', 'Nutritious and balanced recipes'),
            ('Quick Meal', 'Recipes under 30 minutes'),
            ('Vegetarian', 'No meat recipes'),
            ('Gluten-Free', 'Suitable for gluten-sensitive diets'),
            ('High Protein', 'Recipes with substantial protein content'),
            ('Low Carb', 'Reduced carbohydrate recipes'),
            ('Keto', 'Ketogenic diet compatible'),
            ('Mediterranean', 'Following Mediterranean diet principles'),
            ('Vegan', 'Plant-based recipes'),
            ('Dairy-Free', 'No dairy ingredients')
        ]

        for tag in tags:
            try:
                self.cursor.execute('''
                    INSERT INTO Tags (Name, Description) 
                    VALUES (?, ?)
                ''', tag)
            except sqlite3.IntegrityError:
                print(f"Tag {tag[0]} already exists.")

        self.conn.commit()

    def generate_recipes(self):
        
        recipes_data = [
            {
                'username': 'culinary_artist',
                'recipes': [
                    {
                        'title': 'Mediterranean Salmon Bowl',
                        'description': 'Healthy salmon bowl with quinoa and Mediterranean flavors',
                        'cuisine': 'Mediterranean',
                        'difficulty': 'Medium',
                        'prep_time': 15,
                        'cook_time': 20,
                        'servings': 2,
                        'instructions': '''
                        1. Cook quinoa in vegetable broth for enhanced flavor
                        2. Season salmon with olive oil, salt, and herbs
                        3. Grill salmon for 4-5 minutes each side
                        4. Prepare a bed of quinoa, top with salmon
                        5. Garnish with fresh herbs and lemon zest
                        ''',
                        'calories': 450,
                        'protein': 35,
                        'carbs': 30,
                        'fats': 22,
                        'ingredients': [
                            {'name': 'Salmon', 'quantity': 200, 'unit': 'grams'},
                            {'name': 'Quinoa', 'quantity': 100, 'unit': 'grams'},
                            {'name': 'Spinach', 'quantity': 1, 'unit': 'cups'}
                        ],
                        'tags': ['Healthy', 'High Protein', 'Mediterranean'],
                        'is_public': True
                    },
                    {
                        'title': 'Quick Vegetarian Stir-Fry',
                        'description': 'Fast and nutritious plant-based meal',
                        'cuisine': 'Asian Fusion',
                        'difficulty': 'Easy',
                        'prep_time': 10,
                        'cook_time': 15,
                        'servings': 2,
                        'instructions': '''
                        1. Press and cube tofu, season with soy sauce
                        2. Chop vegetables into uniform pieces
                        3. Stir-fry tofu until golden
                        4. Add vegetables and quick stir-fry
                        5. Serve over brown rice
                        ''',
                        'calories': 320,
                        'protein': 25,
                        'carbs': 35,
                        'fats': 15,
                        'ingredients': [
                            {'name': 'Tofu', 'quantity': 250, 'unit': 'grams'},
                            {'name': 'Bell Pepper', 'quantity': 1, 'unit': 'pieces'},
                            {'name': 'Brown Rice', 'quantity': 100, 'unit': 'grams'}
                        ],
                        'tags': ['Vegetarian', 'Quick Meal', 'Low Carb'],
                        'is_public': True
                    }
                ]
            },
            {
                'username': 'fitness_foodie',
                'recipes': [
                    {
                        'title': 'Protein-Packed Chicken Quinoa Bowl',
                        'description': 'Balanced meal for fitness enthusiasts',
                        'cuisine': 'Health Food',
                        'difficulty': 'Medium',
                        'prep_time': 20,
                        'cook_time': 25,
                        'servings': 2,
                        'instructions': '''
                        1. Marinate chicken in herb mixture
                        2. Grill chicken to perfect doneness
                        3. Cook quinoa with turmeric
                        4. Layer quinoa, sliced chicken
                        5. Top with fresh spinach
                        ''',
                        'calories': 480,
                        'protein': 40,
                        'carbs': 40,
                        'fats': 18,
                        'ingredients': [
                            {'name': 'Chicken Breast', 'quantity': 250, 'unit': 'grams'},
                            {'name': 'Quinoa', 'quantity': 150, 'unit': 'grams'},
                            {'name': 'Spinach', 'quantity': 1, 'unit': 'cups'}
                        ],
                        'tags': ['High Protein', 'Healthy', 'Keto'],
                        'is_public': True
                    }
                ]
            }
        ]

        for user_recipe_data in recipes_data:

            self.cursor.execute('SELECT UserID FROM Users WHERE Username = ?', 
                                (user_recipe_data['username'],))
            user_result = self.cursor.fetchone()
            
            if not user_result:
                print(f"User {user_recipe_data['username']} not found. Skipping recipes.")
                continue
            
            user_id = user_result[0]

            for recipe_data in user_recipe_data['recipes']:

                try:
                    self.cursor.execute('''
                        INSERT INTO Recipes 
                        (UserID, Title, Description, Cuisine, DifficultyLevel, 
                        PrepTime, CookingTime, TotalTime, Servings, Instructions, 
                        Calories, Protein, Carbs, Fats, IsPublic) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ''', (
                        user_id, 
                        recipe_data['title'], 
                        recipe_data['description'], 
                        recipe_data['cuisine'], 
                        recipe_data['difficulty'], 
                        recipe_data['prep_time'], 
                        recipe_data['cook_time'], 
                        recipe_data['prep_time'] + recipe_data['cook_time'], 
                        recipe_data['servings'], 
                        recipe_data['instructions'], 
                        recipe_data['calories'], 
                        recipe_data['protein'], 
                        recipe_data['carbs'], 
                        recipe_data['fats'],
                        1  
                    ))
                    
                    recipe_id = self.cursor.lastrowid


                    for ingredient in recipe_data['ingredients']:
                        self.cursor.execute('''
                            SELECT IngredientID FROM Ingredients WHERE Name = ?
                        ''', (ingredient['name'],))
                        ingredient_result = self.cursor.fetchone()
                        
                        if not ingredient_result:
                            print(f"Ingredient {ingredient['name']} not found. Skipping.")
                            continue
                        
                        ingredient_id = ingredient_result[0]

                        self.cursor.execute('''
                            INSERT INTO RecipeIngredients 
                            (RecipeID, IngredientID, Quantity, Unit) 
                            VALUES (?, ?, ?, ?)
                        ''', (
                            recipe_id, 
                            ingredient_id, 
                            ingredient['quantity'], 
                            ingredient['unit']
                        ))


                    for tag_name in recipe_data['tags']:
                        self.cursor.execute('''
                            SELECT TagID FROM Tags WHERE Name = ?
                        ''', (tag_name,))
                        tag_result = self.cursor.fetchone()
                        
                        if not tag_result:
                            print(f"Tag {tag_name} not found. Skipping.")
                            continue
                        
                        tag_id = tag_result[0]

                        self.cursor.execute('''
                            INSERT INTO RecipeTags (RecipeID, TagID) 
                            VALUES (?, ?)
                        ''', (recipe_id, tag_id))

                except sqlite3.Error as e:
                    print(f"Error inserting recipe {recipe_data['title']}: {e}")
                    self.conn.rollback()

        self.conn.commit()


    def run_population(self):
        """
        Orchestrate the database population process with informative logging.
        """
        print("🌱 Starting comprehensive database population...")
        self.generate_users()
        print("👤 User profiles created.")
        self.generate_ingredients()
        print("🥬 Ingredient database populated.")
        self.generate_tags()
        print("🏷️ Recipe tags established.")
        self.generate_recipes()
        print("📜 Diverse recipes added.")
        print("✨ Database population complete!")

    def __del__(self):
        """
        Ensure database connection is properly closed.
        """
        if hasattr(self, 'conn'):
            self.conn.close()

def populate_database(db_path='recipe_manager.db'):
    """
    Main function to initialize and populate the database.
    
    Args:
        db_path (str): Path to the SQLite database file
    """
    from db_init import initialize_database
    

    initialize_database(db_path)
    

    populator = DatabasePopulator(db_path)
    populator.run_population()

if __name__ == "__main__":
    populate_database()