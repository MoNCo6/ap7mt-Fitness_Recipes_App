package com.example.finalproject_fitrecipesapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Ingredient
import com.example.finalproject_fitrecipesapp.api.Nutrition
import com.example.finalproject_fitrecipesapp.api.Recipe
import com.example.finalproject_fitrecipesapp.api.RecipeCategoryApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etNutritionKcal: EditText
    private lateinit var etNutritionCarbs: EditText
    private lateinit var etNutritionFats: EditText
    private lateinit var etNutritionProteins: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnBackToRecipe: ImageButton
    private lateinit var recipeId: String
    private var originalCategoryId: String = ""
    private var isFavorite: Boolean = false // Uchovanie stavu obľúbenosti

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        initializeViews() // Inicializácia UI komponentov

        recipeId = intent.getStringExtra("RECIPE_ID").orEmpty()

        if (recipeId.isNotBlank()) fetchRecipeDetails() // Načítanie detailov receptu

        btnSaveChanges.setOnClickListener { updateRecipe() } // Uloženie zmien
        btnBackToRecipe.setOnClickListener { finish() } // Návrat späť
    }

    // Inicializuje UI komponenty
    private fun initializeViews() {
        etRecipeName = findViewById(R.id.et_recipe_name)
        etIngredients = findViewById(R.id.et_ingredients)
        etInstructions = findViewById(R.id.et_instructions)
        etNutritionKcal = findViewById(R.id.et_nutrition_kcal)
        etNutritionCarbs = findViewById(R.id.et_nutrition_carbs)
        etNutritionFats = findViewById(R.id.et_nutrition_fats)
        etNutritionProteins = findViewById(R.id.et_nutrition_proteins)
        btnSaveChanges = findViewById(R.id.btn_save_changes)
        btnBackToRecipe = findViewById(R.id.btn_back_to_recipe)
    }

    // Načíta detaily receptu zo servera
    private fun fetchRecipeDetails() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getRecipeById(recipeId)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        response.body()?.let { recipe ->
                            bindRecipeDetails(recipe) // Zobrazenie detailov receptu
                            originalCategoryId = recipe.categoryId
                            isFavorite = recipe.isFavorite  // Uloženie stavu obľúbenosti
                        } ?: showToast("Recept sa nenašiel.")
                    } else {
                        showToast("Nepodarilo sa načítať recept.")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    showToast("Chyba pri načítavaní receptu.")
                }
            })
    }

    // Zobrazuje načítané detaily receptu na UI
    private fun bindRecipeDetails(recipe: Recipe) {
        etRecipeName.setText(recipe.name)
        etIngredients.setText(recipe.ingredients.joinToString(", ") {
            "${it.name} ${it.weight ?: ""} ${it.unit}"
        })
        etInstructions.setText(recipe.instructions)
        etNutritionKcal.setText(recipe.nutrition.kcal.toString())
        etNutritionCarbs.setText(recipe.nutrition.carbohydrates.toString())
        etNutritionFats.setText(recipe.nutrition.fats.toString())
        etNutritionProteins.setText(recipe.nutrition.proteins.toString())
    }

    // Aktualizuje recept a uloží ho na server
    private fun updateRecipe() {
        val updatedRecipe = Recipe(
            name = etRecipeName.text.toString(),
            ingredients = parseIngredients(etIngredients.text.toString()),
            instructions = etInstructions.text.toString(),
            categoryId = originalCategoryId, // Zachovanie pôvodnej kategórie
            isFavorite = isFavorite, // Zachovanie stavu obľúbenosti
            nutrition = Nutrition(
                perServing = "100g",
                kcal = etNutritionKcal.text.toString().toDoubleOrNull() ?: 0.0,
                carbohydrates = etNutritionCarbs.text.toString().toDoubleOrNull() ?: 0.0,
                fats = etNutritionFats.text.toString().toDoubleOrNull() ?: 0.0,
                proteins = etNutritionProteins.text.toString().toDoubleOrNull() ?: 0.0
            )
        )

        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(recipeId, updatedRecipe)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        val intent = Intent().apply {
                            putExtra("UPDATED_RECIPE_ID", recipeId)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        showToast("Recept sa nepodarilo aktualizovať.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast("Chyba pri aktualizácii receptu.")
                }
            })
    }

    // Rozdelí vstupný reťazec ingrediencií na zoznam objektov `Ingredient`
    private fun parseIngredients(input: String): List<Ingredient> {
        return input.split(",").mapNotNull { ing ->
            val parts = ing.trim().split(" ")
            if (parts.isNotEmpty()) {
                Ingredient(
                    name = parts[0],
                    weight = parts.getOrNull(1),
                    unit = parts.getOrNull(2) ?: ""
                )
            } else null
        }
    }

    // Zobrazí toast so správou
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}