import sqlite3
import getpass
import hashlib
import os
import sys
from datetime import datetime, date
from tabulate import tabulate


sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from db_init import initialize_database, hash_password

class RecipeManager:
    def __init__(self, db_path='recipe_manager.db'):

        if not os.path.exists(db_path):
            print("Database not found. Initializing...")
            initialize_database(db_path)
        
        try:
            self.conn = sqlite3.connect(db_path)
            self.cursor = self.conn.cursor()
            self.current_user = None
        except sqlite3.Error as e:
            print(f"Database connection error: {e}")
            sys.exit(1)

    def register(self):
        try:
            print("\n--- User Registration ---")
            username = input("Enter username: ")
            email = input("Enter email: ")
            password = getpass.getpass("Enter password: ")
            confirm_password = getpass.getpass("Confirm password: ")
            bio = input("Enter a short bio (optional): ")
            
         
            if password != confirm_password:
                print("Passwords do not match!")
                return
            
            hashed_password = hash_password(password)
            
            self.cursor.execute('''
                INSERT INTO Users (Username, Email, Password, Bio) 
                VALUES (?, ?, ?, ?)
            ''', (username, email, hashed_password, bio))
            
            self.conn.commit()
            print("User registered successfully!")
        
        except sqlite3.IntegrityError:
            print("Username or email already exists.")
        except sqlite3.Error as e:
            print(f"Registration error: {e}")

    def login(self):
        print("\n--- User Login ---")
        username = input("Enter username: ")
        password = getpass.getpass("Enter password: ")
        
        hashed_password = hash_password(password)
        
        self.cursor.execute('''
            SELECT UserID, Username FROM Users 
            WHERE Username = ? AND Password = ?
        ''', (username, hashed_password))
        
        user = self.cursor.fetchone()
        
        if user:
            self.current_user = user[0]
            print(f"Welcome, {user[1]}!")
            return True
        else:
            print("Invalid credentials.")
            return False

    def profile_management(self):
        if not self.current_user:
            print("Please login first.")
            return

        while True:
            print("\n--- Profile Management ---")
            print("1. View Profile")
            print("2. Update Profile")
            print("3. Change Password")
            print("4. Return to Main Menu")

            choice = input("Enter your choice: ")

            if choice == '1':
                self.view_profile()
            elif choice == '2':
                self.update_profile()
            elif choice == '3':
                self.change_password()
            elif choice == '4':
                break
            else:
                print("Invalid choice. Try again.")

    def view_profile(self):
        self.cursor.execute('''
            SELECT Username, Email, Bio, CreatedAt 
            FROM Users WHERE UserID = ?
        ''', (self.current_user,))
        profile = self.cursor.fetchone()
        
        if profile:
            print("\n--- Profile Details ---")
            print(f"Username: {profile[0]}")
            print(f"Email: {profile[1]}")
            print(f"Bio: {profile[2] or 'No bio set'}")
            print(f"Member Since: {profile[3]}")

    def update_profile(self):
        email = input("Enter new email (leave blank to keep current): ")
        bio = input("Enter new bio (leave blank to keep current): ")

        updates = []
        params = []

        if email:
            updates.append("Email = ?")
            params.append(email)
        
        if bio:
            updates.append("Bio = ?")
            params.append(bio)
        
        if updates:
            query = f"UPDATE Users SET {', '.join(updates)} WHERE UserID = ?"
            params.append(self.current_user)
            
            try:
                self.cursor.execute(query, tuple(params))
                self.conn.commit()
                print("Profile updated successfully!")
            except sqlite3.Error as e:
                print(f"Error updating profile: {e}")
                self.conn.rollback()

    def change_password(self):
        old_password = getpass.getpass("Enter current password: ")
        new_password = getpass.getpass("Enter new password: ")
        confirm_password = getpass.getpass("Confirm new password: ")


        self.cursor.execute('''
            SELECT Password FROM Users 
            WHERE UserID = ?
        ''', (self.current_user,))
        current_hash = self.cursor.fetchone()[0]

        if current_hash != hash_password(old_password):
            print("Current password is incorrect.")
            return

        if new_password != confirm_password:
            print("New passwords do not match.")
            return

        try:
            new_hash = hash_password(new_password)
            self.cursor.execute('''
                UPDATE Users SET Password = ? 
                WHERE UserID = ?
            ''', (new_hash, self.current_user))
            self.conn.commit()
            print("Password changed successfully!")
        except sqlite3.Error as e:
            print(f"Error changing password: {e}")
            self.conn.rollback()

    def add_recipe(self):
        if not self.current_user:
            print("Please login first.")
            return

        try:
            print("\n--- Add New Recipe ---")
            title = input("Enter recipe title: ")
            description = input("Enter recipe description: ")
            cuisine = input("Enter cuisine type: ")
            prep_time = int(input("Enter preparation time (minutes): "))
            cook_time = int(input("Enter cooking time (minutes): "))
            difficulty = input("Enter difficulty (Easy/Medium/Hard): ").capitalize()
            servings = int(input("Enter number of servings: "))
            instructions = input("Enter cooking instructions: ")
            is_public = input("Make recipe public? (yes/no): ").lower() == 'yes'


            calories = int(input("Enter total calories: "))
            protein = float(input("Enter protein (g): "))
            carbs = float(input("Enter carbs (g): "))
            fats = float(input("Enter fats (g): "))


            self.cursor.execute('''
                INSERT INTO Recipes 
                (UserID, Title, Description, Cuisine, PrepTime, CookingTime, TotalTime, 
                DifficultyLevel, Servings, Instructions, Calories, Protein, Carbs, Fats, IsPublic) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (self.current_user, title, description, cuisine, prep_time, cook_time, 
                  prep_time + cook_time, difficulty, servings, instructions, calories, 
                  protein, carbs, fats, 1 if is_public else 0))
            
            recipe_id = self.cursor.lastrowid


            while True:
                ingredient_name = input("Enter ingredient name (or 'done' to finish): ")
                if ingredient_name.lower() == 'done':
                    break


                self.cursor.execute('''
                    SELECT IngredientID FROM Ingredients 
                    WHERE Name = ?
                ''', (ingredient_name,))
                ingredient = self.cursor.fetchone()
                
                if not ingredient:

                    category = input(f"Enter category for {ingredient_name}: ")
                    unit = input(f"Enter default unit for {ingredient_name}: ")
                    nutritional_info = input(f"Enter nutritional info (optional): ")
                    
                    self.cursor.execute('''
                        INSERT INTO Ingredients 
                        (Name, Category, Unit, NutritionalInfo) 
                        VALUES (?, ?, ?, ?)
                    ''', (ingredient_name, category, unit, nutritional_info))
                    ingredient_id = self.cursor.lastrowid
                else:
                    ingredient_id = ingredient[0]


                quantity = float(input(f"Enter quantity of {ingredient_name}: "))
                unit = input(f"Enter unit for {ingredient_name}: ")
                notes = input(f"Any notes for this ingredient? (optional): ")

                self.cursor.execute('''
                    INSERT INTO RecipeIngredients 
                    (RecipeID, IngredientID, Quantity, Unit, Notes) 
                    VALUES (?, ?, ?, ?, ?)
                ''', (recipe_id, ingredient_id, quantity, unit, notes))


            while True:
                tag_name = input("Enter a tag for this recipe (or 'done' to finish): ")
                if tag_name.lower() == 'done':
                    break


                self.cursor.execute('SELECT TagID FROM Tags WHERE Name = ?', (tag_name,))
                tag = self.cursor.fetchone()
                
                if not tag:

                    description = input(f"Enter description for tag {tag_name}: ")
                    self.cursor.execute('''
                        INSERT INTO Tags (Name, Description) 
                        VALUES (?, ?)
                    ''', (tag_name, description))
                    tag_id = self.cursor.lastrowid
                else:
                    tag_id = tag[0]


                self.cursor.execute('''
                    INSERT INTO RecipeTags 
                    (RecipeID, TagID) 
                    VALUES (?, ?)
                ''', (recipe_id, tag_id))

            self.conn.commit()
            print("Recipe added successfully!")

        except (sqlite3.Error, ValueError) as e:
            self.conn.rollback()
            print(f"Error adding recipe: {e}")

    def view_recipes(self, filter_type='all'):
        try:
            query = '''
                SELECT RecipeID, Title, Cuisine, DifficultyLevel, 
                       CookingTime, Servings, IsPublic 
                FROM Recipes
            '''
            params = []

            if filter_type == 'my_recipes':
                query += ' WHERE UserID = ?'
                params.append(self.current_user)
            elif filter_type == 'public':
                query += ' WHERE IsPublic = 1'

            self.cursor.execute(query, params)
            recipes = self.cursor.fetchall()
            
            if not recipes:
                print("No recipes found.")
                return
            
            headers = ["ID", "Title", "Cuisine", "Difficulty", 
                       "Cooking Time", "Servings", "Public"]
            
            print(tabulate(recipes, headers=headers, tablefmt="grid"))
            return recipes
        
        except sqlite3.Error as e:
            print(f"Error viewing recipes: {e}")

    def view_recipe_details(self):
        try:
            # First, view available recipes
            recipes = self.view_recipes('all')
            
            if not recipes:
                return

            # Prompt user to select a recipe
            recipe_id = int(input("Enter Recipe ID to view details: "))


            self.cursor.execute('''
                SELECT 
                    r.RecipeID, 
                    r.Title, 
                    r.Description,
                    r.Cuisine,
                    r.DifficultyLevel,
                    r.PrepTime,
                    r.CookingTime,
                    r.Servings,
                    r.Instructions,
                    r.Calories,
                    r.Protein,
                    r.Carbs,
                    r.Fats,
                    (SELECT GROUP_CONCAT(t.Name, ', ') 
                     FROM RecipeTags rt 
                     JOIN Tags t ON rt.TagID = t.TagID 
                     WHERE rt.RecipeID = r.RecipeID) as Tags,
                    (SELECT GROUP_CONCAT(i.Name || ': ' || ri.Quantity || ' ' || ri.Unit, ', ') 
                     FROM RecipeIngredients ri 
                     JOIN Ingredients i ON ri.IngredientID = i.IngredientID 
                     WHERE ri.RecipeID = r.RecipeID) as Ingredients
                FROM Recipes r
                WHERE r.RecipeID = ?
            ''', (recipe_id,))
            
            recipe = self.cursor.fetchone()

            if not recipe:
                print("Recipe not found.")
                return


            (recipe_id, title, description, cuisine, difficulty, 
             prep_time, cook_time, servings, instructions, 
             calories, protein, carbs, fats, tags, ingredients) = recipe


            self.cursor.execute('''
                SELECT 
                    u.Username, 
                    rf.Rating, 
                    rf.DifficultyRating, 
                    rf.ActualCookingTime, 
                    rf.Comment, 
                    rf.CreatedAt
                FROM RecipeFeedback rf
                JOIN Users u ON rf.UserID = u.UserID
                WHERE rf.RecipeID = ?
                ORDER BY rf.CreatedAt DESC
            ''', (recipe_id,))
            
            feedbacks = self.cursor.fetchall()


            print("\n--- Recipe Details ---")
            print("=" * 30)
            print(f"📜 Title: {title}")
            print(f"📝 Description: {description}")
            print(f"🌍 Cuisine: {cuisine}")
            print(f"🔥 Difficulty: {difficulty}")
            print(f"⏰ Prep Time: {prep_time} minutes")
            print(f"🍳 Cooking Time: {cook_time} minutes")
            print(f"👥 Servings: {servings}")


            print("\n🍎 Nutritional Information:")
            print(f"    Calories: {calories}")
            print(f"    Protein: {protein}g")
            print(f"    Carbs: {carbs}g")
            print(f"    Fats: {fats}g")

 
            print("\n🥬 Ingredients:")
            if ingredients:
                for ingredient in ingredients.split(', '):
                    print(f"    - {ingredient}")
            else:
                print("    No ingredients found.")


            if tags:
                print(f"\n🏷️ Tags: {tags}")


            print("\n📖 Instructions:")
            print(instructions)


            if feedbacks:
                print("\n📝 Recipe Feedback:")
                for feedback in feedbacks:
                    username, rating, difficulty_rating, cook_time, comment, created_at = feedback
                    print(f"\n👤 User: {username}")
                    print(f"⭐ Rating: {rating}/5")
                    print(f"🔥 Difficulty Rating: {difficulty_rating}/5")
                    print(f"⏰ Actual Cooking Time: {cook_time or 'Not specified'} minutes")
                    print(f"💬 Comment: {comment or 'No comment'}")
                    print(f"📅 Submitted: {created_at}")
            else:
                print("\n📝 No feedback available for this recipe.")

        except (sqlite3.Error, ValueError) as e:
            print(f"Error viewing recipe details: {e}")


    def view_meal_plan_details(self):
        try:

            plans = self.view_meal_plans()
            if not plans:
                return


            plan_id = int(input("Enter Meal Plan ID to view details: "))


            self.cursor.execute('''
                SELECT Name, StartDate, EndDate 
                FROM MealPlans 
                WHERE PlanID = ? AND UserID = ?
            ''', (plan_id, self.current_user))
            plan_info = self.cursor.fetchone()

            if not plan_info:
                print("Meal plan not found.")
                return


            self.cursor.execute('''
                SELECT 
                    r.Title, 
                    r.Calories,
                    r.Protein,
                    r.Carbs,
                    r.Fats,
                    mpr.MealDate, 
                    mpr.MealType
                FROM MealPlanRecipes mpr
                JOIN Recipes r ON mpr.RecipeID = r.RecipeID
                WHERE mpr.PlanID = ?
                ORDER BY mpr.MealDate, mpr.MealType
            ''', (plan_id,))
            recipes = self.cursor.fetchall()

            if not recipes:
                print("No recipes added to this meal plan.")
                return


            meal_plan_details = {}
            for recipe in recipes:
                date = recipe[5]
                meal_type = recipe[6]

                if date not in meal_plan_details:
                    meal_plan_details[date] = {}

                meal_plan_details[date][meal_type] = {
                    'title': recipe[0],
                    'calories': recipe[1],
                    'protein': recipe[2],
                    'carbs': recipe[3],
                    'fats': recipe[4]
                }


            print(f"\n--- Meal Plan: {plan_info[0]} ---")
            print(f"From {plan_info[1]} to {plan_info[2]}\n")


            header_format = "| {:<10} | {:<15} | {:<25} | {:<10} | {:<10} | {:<10} | {:<10} |"
            separator = "+------------+" + "-" * 15 + "+" + "-" * 25 + "+" + "-" * 10 + "+" + "-" * 10 + "+" + "-" * 10 + "+" + "-" * 10 + "+"

            print(separator)
            print(header_format.format("Date", "Day", "Recipe", "Meal Type", "Calories", "Protein", "Carbs", "Fats"))
            print(separator)


            meal_types = ['Breakfast', 'Lunch', 'Dinner', 'Snack']


            sorted_dates = sorted(meal_plan_details.keys())

            for date in sorted_dates:
                day_of_week = datetime.strptime(date, "%Y-%m-%d").strftime("%A")

                for meal_type in meal_types:
                    row_data = [date, day_of_week]

                    if meal_type in meal_plan_details[date]:
                        recipe = meal_plan_details[date][meal_type]
                        row_data.extend([
                            recipe['title'][:25], 
                            meal_type, 
                            recipe['calories'], 
                            f"{recipe['protein']}g", 
                            f"{recipe['carbs']}g", 
                            f"{recipe['fats']}g"
                        ])
                    else:
                        row_data.extend(['-', meal_type, '-', '-', '-', '-'])


                    print(header_format.format(*row_data))

                print(separator)

        except (sqlite3.Error, ValueError) as e:
            print(f"Error viewing meal plan details: {e}")






    def meal_planning(self):
        if not self.current_user:
            print("Please login first.")
            return

        while True:
            print("\n--- Meal Planning ---")
            print("1. Create Meal Plan")
            print("2. View Meal Plans")
            print("3. View Meal Plan Details")  
            print("4. Add Recipe to Meal Plan")
            print("5. Return to Main Menu")

            choice = input("Enter your choice: ")

            if choice == '1':
                self.create_meal_plan()
            elif choice == '2':
                self.view_meal_plans()
            elif choice == '3':
                self.view_meal_plan_details()  
            elif choice == '4':
                self.add_recipe_to_meal_plan()
            elif choice == '5':
                break
            else:
                print("Invalid choice. Try again.")

    def create_meal_plan(self):
        try:
            name = input("Enter meal plan name: ")
            start_date = input("Enter start date (YYYY-MM-DD): ")
            end_date = input("Enter end date (YYYY-MM-DD): ")

            self.cursor.execute('''
                INSERT INTO MealPlans 
                (UserID, Name, StartDate, EndDate) 
                VALUES (?, ?, ?, ?)
            ''', (self.current_user, name, start_date, end_date))
            
            plan_id = self.cursor.lastrowid
            self.conn.commit()
            print(f"Meal plan '{name}' created successfully!")
            return plan_id
        
        except sqlite3.Error as e:
            print(f"Error creating meal plan: {e}")
            self.conn.rollback()

    def view_meal_plans(self):
        try:
            self.cursor.execute('''
                SELECT PlanID, Name, StartDate, EndDate 
                FROM MealPlans 
                WHERE UserID = ?
            ''', (self.current_user,))
            
            plans = self.cursor.fetchall()
            
            if not plans:
                print("No meal plans found.")
                return
            
            headers = ["ID", "Name", "Start Date", "End Date"]
            print(tabulate(plans, headers=headers, tablefmt="grid"))
            return plans
        
        except sqlite3.Error as e:
            print(f"Error viewing meal plans: {e}")

    def add_recipe_to_meal_plan(self):

        plans = self.view_meal_plans()
        
        if not plans:
            return


        plan_id = int(input("Enter Meal Plan ID to add a recipe: "))
        

        recipes = self.view_recipes('all')
        
        if not recipes:
            return


        recipe_id = int(input("Enter Recipe ID to add: "))
        

        meal_date = input("Enter date for this recipe (YYYY-MM-DD): ")
        meal_type = input("Enter meal type (Breakfast/Lunch/Dinner/Snack): ").capitalize()

        try:
            self.cursor.execute('''
                INSERT INTO MealPlanRecipes 
                (PlanID, RecipeID, MealDate, MealType) 
                VALUES (?, ?, ?, ?)
            ''', (plan_id, recipe_id, meal_date, meal_type))
            
            self.conn.commit()
            print("Recipe added to meal plan successfully!")
        
        except sqlite3.Error as e:
            print(f"Error adding recipe to meal plan: {e}")
            self.conn.rollback()

    def pantry_management(self):
        if not self.current_user:
            print("Please login first.")
            return

        while True:
            print("\n--- Pantry Management ---")
            print("1. Add Ingredient to Pantry")
            print("2. View Pantry")
            print("3. Remove Ingredient from Pantry")
            print("4. Return to Main Menu")

            choice = input("Enter your choice: ")

            if choice == '1':
                self.add_to_pantry()
            elif choice == '2':
                self.view_pantry()
            elif choice == '3':
                self.remove_from_pantry()
            elif choice == '4':
                break
            else:
                print("Invalid choice. Try again.")
        

    def advanced_recipe_search(self):
        print("\n--- Advanced Recipe Search ---")
        search_criteria = {}


        cuisine = input("Enter cuisine (optional): ").strip()
        if cuisine:
            search_criteria['Cuisine'] = cuisine

        difficulty = input("Enter difficulty level (Easy/Medium/Hard, optional): ").strip()
        if difficulty:
            search_criteria['DifficultyLevel'] = difficulty

        max_cook_time = input("Maximum cooking time (minutes, optional): ").strip()
        if max_cook_time:
            search_criteria['MaxCookTime'] = int(max_cook_time)

        dietary_tags = input("Dietary tags (comma-separated, optional - e.g., Vegetarian, Gluten-Free): ").strip()
        

        query = "SELECT * FROM Recipes WHERE 1=1 "
        params = []

        for key, value in search_criteria.items():
            if key == 'Cuisine':
                query += " AND Cuisine = ?"
                params.append(value)
            elif key == 'DifficultyLevel':
                query += " AND DifficultyLevel = ?"
                params.append(value)
            elif key == 'MaxCookTime':
                query += " AND CookingTime <= ?"
                params.append(value)

        if dietary_tags:
            tags = [tag.strip() for tag in dietary_tags.split(',')]
            query += " AND RecipeID IN (SELECT RecipeID FROM RecipeTags rt JOIN Tags t ON rt.TagID = t.TagID WHERE t.Name IN ({}))"
            query = query.format(','.join(['?'] * len(tags)))
            params.extend(tags)

        query += " AND IsPublic = 1"  

        self.cursor.execute(query, params)
        recipes = self.cursor.fetchall()

        if recipes:
            headers = ["ID", "Title", "Cuisine", "Difficulty", "Cook Time", "Servings"]
            table_data = [(r[0], r[2], r[4], r[8], r[6], r[9]) for r in recipes]
            print(tabulate(table_data, headers=headers, tablefmt="grid"))
        else:
            print("No recipes found matching your criteria.")

    def pantry_based_recommendations(self):

        self.cursor.execute('''
            SELECT IngredientID FROM Pantry 
            WHERE UserID = ?
        ''', (self.current_user,))
        pantry_ingredients = [str(i[0]) for i in self.cursor.fetchall()]

        if not pantry_ingredients:
            print("Your pantry is empty. Add ingredients to get recommendations!")
            return


        query = f'''
            SELECT r.RecipeID, r.Title, 
                   COUNT(DISTINCT ri.IngredientID) as matched_ingredients,
                   COUNT(DISTINCT req.IngredientID) as total_ingredients
            FROM Recipes r
            JOIN RecipeIngredients ri ON r.RecipeID = ri.RecipeID
            JOIN (
                SELECT RecipeID, IngredientID 
                FROM RecipeIngredients 
                WHERE IngredientID IN ({','.join(pantry_ingredients)})
            ) matched ON r.RecipeID = matched.RecipeID
            JOIN RecipeIngredients req ON r.RecipeID = req.RecipeID
            WHERE r.IsPublic = 1
            GROUP BY r.RecipeID, r.Title
            ORDER BY matched_ingredients DESC, total_ingredients ASC
            LIMIT 5
        '''

        self.cursor.execute(query)
        recommendations = self.cursor.fetchall()

        if recommendations:
            print("\n--- Recommended Recipes Based on Your Pantry ---")
            headers = ["ID", "Recipe", "Matched Ingredients", "Total Ingredients"]
            print(tabulate(recommendations, headers=headers, tablefmt="grid"))
        else:
            print("No recipe recommendations found.")


    def enhance_recipe_feedback(self):

        print("\n--- Available Recipes for Feedback ---")
        recipes = self.view_recipes('all')
        
        if not recipes:
            print("No recipes available to provide feedback.")
            return


        try:
            recipe_id = int(input("Enter Recipe ID to provide feedback: "))
            

            self.cursor.execute('SELECT Title FROM Recipes WHERE RecipeID = ?', (recipe_id,))
            recipe = self.cursor.fetchone()
            
            if not recipe:
                print("Invalid Recipe ID.")
                return


            rating = int(input("Rate the recipe (1-5): "))
            difficulty_rating = int(input("How difficult was this recipe to make? (1-5): "))
            time_estimate = input("Actual cooking time (minutes): ")
            comment = input("Additional comments: ")

            # Validate input ranges
            if not (1 <= rating <= 5 and 1 <= difficulty_rating <= 5):
                print("Ratings must be between 1 and 5.")
                return

            try:
                self.cursor.execute('''
                    INSERT INTO RecipeFeedback 
                    (RecipeID, UserID, Rating, DifficultyRating, 
                     ActualCookingTime, Comment) 
                    VALUES (?, ?, ?, ?, ?, ?)
                ''', (recipe_id, self.current_user, rating, 
                      difficulty_rating, time_estimate, comment))
                
                self.conn.commit()
                print("Feedback submitted successfully!")
            
            except sqlite3.Error as e:
                print(f"Error submitting feedback: {e}")
                self.conn.rollback()

        except ValueError:
            print("Please enter a valid numeric Recipe ID.")

    def add_to_pantry(self):

        ingredient_name = input("Enter ingredient name: ")
        

        self.cursor.execute('''
            SELECT IngredientID FROM Ingredients 
            WHERE Name = ?
        ''', (ingredient_name,))
        ingredient = self.cursor.fetchone()
        
        if not ingredient:

            category = input(f"Enter category for {ingredient_name}: ")
            unit = input(f"Enter default unit: ")
            self.cursor.execute('''
                INSERT INTO Ingredients (Name, Category, Unit) 
                VALUES (?, ?, ?)
            ''', (ingredient_name, category, unit))
            ingredient_id = self.cursor.lastrowid
        else:
            ingredient_id = ingredient[0]

        quantity = float(input("Enter quantity: "))
        expiry_date = input("Enter expiry date (YYYY-MM-DD, optional): ") or None

        try:
            self.cursor.execute('''
                INSERT OR REPLACE INTO Pantry 
                (UserID, IngredientID, Quantity, ExpiryDate, PurchaseDate) 
                VALUES (?, ?, ?, ?, ?)
            ''', (self.current_user, ingredient_id, quantity, 
                  expiry_date, date.today()))
            
            self.conn.commit()
            print("Ingredient added to pantry successfully!")
        
        except sqlite3.Error as e:
            print(f"Error adding to pantry: {e}")
            self.conn.rollback()

    def view_pantry(self):
        try:
            self.cursor.execute('''
                SELECT i.Name, p.Quantity, i.Unit, p.ExpiryDate 
                FROM Pantry p
                JOIN Ingredients i ON p.IngredientID = i.IngredientID
                WHERE p.UserID = ?
            ''', (self.current_user,))
            
            pantry_items = self.cursor.fetchall()
            
            if not pantry_items:
                print("Pantry is empty.")
                return
            
            headers = ["Ingredient", "Quantity", "Unit", "Expiry Date"]
            print(tabulate(pantry_items, headers=headers, tablefmt="grid"))
        
        except sqlite3.Error as e:
            print(f"Error viewing pantry: {e}")

    def remove_from_pantry(self):
        self.view_pantry()
        ingredient_name = input("Enter ingredient name to remove: ")
        
        try:
            self.cursor.execute('''
                DELETE FROM Pantry 
                WHERE UserID = ? AND IngredientID = (
                    SELECT IngredientID FROM Ingredients 
                    WHERE Name = ?
                )
            ''', (self.current_user, ingredient_name))
            
            self.conn.commit()
            print("Ingredient removed from pantry successfully!")
        
        except sqlite3.Error as e:
            print(f"Error removing from pantry: {e}")
            self.conn.rollback()

    def run(self):
        while True:
            if not self.current_user:
                print("\n--- ChefSync Recipe Manager ---")
                print("1. Register")
                print("2. Login")
                print("3. Exit")

                choice = input("Enter your choice: ")

                if choice == '1':
                    self.register()
                elif choice == '2':
                    self.login()
                elif choice == '3':
                    break
                else:
                    print("Invalid choice. Try again.")
            
            else:

                print("\n--- ChefSync Dashboard ---")
                print("1. Profile Management")
                print("2. Add Recipe")
                print("3. View Recipes")
                print("4. Meal Planning")
                print("5. Pantry Management")
                print("6. Advanced Recipe Search")
                print("7. Pantry-Based Recommendations")
                print("8. Recipe Feedback")
                print("9. Logout")

                choice = input("Enter your choice: ")

                if choice == '1':
                    self.profile_management()
                elif choice == '2':
                    self.add_recipe()
                elif choice == '3':
                  while True:
                      print("\n--- View Recipes ---")
                      print("1. View All Recipes")
                      print("2. View My Recipes")
                      print("3. View Recipe Details")
                      print("4. Return to Main Menu")
                    
                      view_choice = input("Enter your choice: ")
                    
                      if view_choice == '1':
                        self.view_recipes('public')
                      elif view_choice == '2':
                        self.view_recipes('my_recipes')
                      elif view_choice == '3':
                        self.view_recipe_details()
                      elif view_choice == '4':
                        break
                      else:
                        print("Invalid choice. Try again.")
            
                elif choice == '4':
                # Meal Planning
                    self.meal_planning()
            
                elif choice == '5':
                # Pantry Management
                    self.pantry_management()
            
                elif choice == '6':
                # Advanced Recipe Search
                    self.advanced_recipe_search()
            
                elif choice == '7':
                # Pantry-Based Recommendations
                    self.pantry_based_recommendations()
            
                elif choice == '8':
                # Recipe Feedback
                    self.enhance_recipe_feedback()
            
                elif choice == '9':
                # Logout
                    self.current_user = None
                    print("Logged out successfully!")
            
                else:
                    print("Invalid choice. Try again.")

    def __del__(self):
        if hasattr(self, 'conn'):
            self.conn.close()

def main():
    recipe_manager = RecipeManager()
    recipe_manager.run()

if __name__ == "__main__":
    main()