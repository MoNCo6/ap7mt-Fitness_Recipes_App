package com.example.finalproject_fitrecipesapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject_fitrecipesapp.R
import com.example.finalproject_fitrecipesapp.api.Ingredient
import com.example.finalproject_fitrecipesapp.api.Nutrition
import com.example.finalproject_fitrecipesapp.api.Recipe

// Aktivita určená na úpravu existujúceho receptu.
class EditRecipeActivity : AppCompatActivity() {

    // UI prvky na zadávanie názvu, ingrediencií, inštrukcií a nutričných hodnôt.
    private lateinit var etRecipeName: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var etNutritionKcal: EditText
    private lateinit var etNutritionCarbs: EditText
    private lateinit var etNutritionFats: EditText
    private lateinit var etNutritionProteins: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnBackToRecipe: ImageButton

    // ID receptu, ktorý upravujeme.
    private lateinit var recipeId: String

    // Pôvodná kategória receptu a stav obľúbenosti.
    private var originalCategoryId: String = ""
    private var isFavorite: Boolean = false

    // ViewModel pre prácu s receptom a jeho úpravou.
    private lateinit var viewModel: EditRecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Inicializácia ViewModelu.
        viewModel = ViewModelProvider(this).get(EditRecipeViewModel::class.java)

        // Nastavenie UI komponentov.
        initializeViews()

        // Načítame ID receptu z predchádzajúcej aktivity.
        recipeId = intent.getStringExtra("RECIPE_ID").orEmpty()

        // Ak je ID platné, načítame detaily receptu.
        if (recipeId.isNotBlank()) {
            viewModel.fetchRecipeDetails(recipeId, onError = { msg ->
                showToast(msg)
            })
        }

        // Pozorujeme dáta z ViewModelu, aby sme zobrazili načítaný recept.
        observeViewModel()

        // Kliknutie na tlačidlo "Uložiť zmeny".
        btnSaveChanges.setOnClickListener { updateRecipe() }

        // Kliknutie na tlačidlo "Späť".
        btnBackToRecipe.setOnClickListener { finish() }
    }

    // Nastavenie referencií na UI prvky.
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

    // Sledujeme LiveData z ViewModelu, aby sme zobrazili detaily načítaného receptu.
    private fun observeViewModel() {
        viewModel.recipe.observe(this, Observer { loadedRecipe ->
            if (loadedRecipe != null) {
                bindRecipeDetails(loadedRecipe)
                originalCategoryId = loadedRecipe.categoryId
                isFavorite = loadedRecipe.isFavorite
            }
        })
    }

    // Vypĺňame polia obrazovky dátami z receptu.
    private fun bindRecipeDetails(recipe: Recipe) {
        etRecipeName.setText(recipe.name)
        etIngredients.setText(
            recipe.ingredients.joinToString(", ") {
                "${it.name} ${it.weight ?: ""} ${it.unit}"
            }
        )
        etInstructions.setText(recipe.instructions)
        etNutritionKcal.setText(recipe.nutrition.kcal.toString())
        etNutritionCarbs.setText(recipe.nutrition.carbohydrates.toString())
        etNutritionFats.setText(recipe.nutrition.fats.toString())
        etNutritionProteins.setText(recipe.nutrition.proteins.toString())
    }

    // Vytvorí nový Recipe z vyplnených polí a zavolá aktualizáciu vo ViewModeli.
    private fun updateRecipe() {
        val updatedRecipe = Recipe(
            name = etRecipeName.text.toString(),
            ingredients = parseIngredients(etIngredients.text.toString()),
            instructions = etInstructions.text.toString(),
            categoryId = originalCategoryId, // Zachovanie pôvodnej kategórie
            isFavorite = isFavorite,         // Zachovanie stavu obľúbenosti
            nutrition = Nutrition(
                perServing = "100g",
                kcal = etNutritionKcal.text.toString().toDoubleOrNull() ?: 0.0,
                carbohydrates = etNutritionCarbs.text.toString().toDoubleOrNull() ?: 0.0,
                fats = etNutritionFats.text.toString().toDoubleOrNull() ?: 0.0,
                proteins = etNutritionProteins.text.toString().toDoubleOrNull() ?: 0.0
            )
        )

        // Zavoláme update vo ViewModeli a zareagujeme na výsledok
        viewModel.updateRecipe(
            recipeId = recipeId,
            updatedRecipe = updatedRecipe,
            onSuccess = {
                val intent = Intent().apply {
                    putExtra("UPDATED_RECIPE_ID", recipeId)
                }
                setResult(RESULT_OK, intent)
                finish()
            },
            onError = { msg ->
                showToast(msg)
            }
        )
    }

    // Rozdelí reťazec ingrediencií na objekty Ingredient.
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

    // Zobrazí toast s odkazom pre používateľa.
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
