package com.example.finalproject_fitrecipesapp.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject_fitrecipesapp.R
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Ingredient
import com.example.finalproject_fitrecipesapp.api.Nutrition
import com.example.finalproject_fitrecipesapp.api.Recipe

// Aktivita zodpovedná za pridanie nového receptu do systému.
class AddRecipeActivity : AppCompatActivity() {

    // Polia pre zadávanie názvu, výživových hodnôt, ingrediencií a inštrukcií.
    private lateinit var etRecipeName: EditText
    private lateinit var etNutritionKcal: EditText
    private lateinit var etNutritionCarbs: EditText
    private lateinit var etNutritionFats: EditText
    private lateinit var etNutritionProteins: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSaveRecipe: Button

    // ViewModel, ktorý sa stará o načítanie kategórií a vytvorenie nového receptu.
    private lateinit var viewModel: AddRecipeViewModel

    // Mapovanie názvov kategórií na ich ID.
    private val categoryMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Inicializácia ViewModelu.
        viewModel = ViewModelProvider(this).get(AddRecipeViewModel::class.java)

        // Pripravenie UI prvkov.
        initializeViews()
        setupBackButton()

        // Sledujeme LiveData s mapou kategórií.
        observeViewModel()

        // Načítame zoznam kategórií (API volanie).
        viewModel.fetchCategories(onError = { msg ->
            showToast(msg)
        })

        // Nastavenie tlačidla "Uložiť recept".
        setupSaveButton()
    }

    // Priradenie jednotlivých UI prvkov k premennej.
    private fun initializeViews() {
        etRecipeName = findViewById(R.id.et_recipe_name)
        etNutritionKcal = findViewById(R.id.et_nutrition_kcal)
        etNutritionCarbs = findViewById(R.id.et_nutrition_carbs)
        etNutritionFats = findViewById(R.id.et_nutrition_fats)
        etNutritionProteins = findViewById(R.id.et_nutrition_proteins)
        etIngredients = findViewById(R.id.et_ingredients)
        etInstructions = findViewById(R.id.et_instructions)
        spinnerCategory = findViewById(R.id.spinner_category)
        btnSaveRecipe = findViewById(R.id.btn_save_recipe)
    }

    // Umožňuje návrat na predchádzajúcu obrazovku.
    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.img_toolbar_btn_back).setOnClickListener { onBackPressed() }
    }

    // Sledujeme stav LiveData z ViewModelu a meníme UI (napr. Spinner) podľa kategórií.
    private fun observeViewModel() {
        viewModel.categoryMap.observe(this, Observer { newMap ->
            categoryMap.clear()
            categoryMap.putAll(newMap)

            // Nastavíme názvy kategórií do spinnera
            val categoryNames = categoryMap.keys.toList()
            val adapter = ArrayAdapter(
                this@AddRecipeActivity,
                android.R.layout.simple_spinner_item,
                categoryNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerCategory.adapter = adapter
        })
    }

    // Spracovanie kliknutia na tlačidlo "Uložiť recept".
    private fun setupSaveButton() {
        btnSaveRecipe.setOnClickListener {
            val recipe = collectRecipeData()
            viewModel.createRecipe(
                recipe = recipe,
                onSuccess = {
                    setResult(RESULT_OK)
                    showToast("Recept úspešne pridaný!")
                    finish()
                },
                onError = { msg ->
                    showToast(msg)
                }
            )
        }
    }

    // Získa údaje z textových polí, vytvorí nový Recipe objekt.
    private fun collectRecipeData(): Recipe {
        val ingredients = parseIngredients(etIngredients.text.toString())
        val nutrition = Nutrition(
            perServing = "100g", // Alebo si môžeš spraviť UI pole
            kcal = etNutritionKcal.text.toString().toDoubleOrNull() ?: 0.0,
            carbohydrates = etNutritionCarbs.text.toString().toDoubleOrNull() ?: 0.0,
            fats = etNutritionFats.text.toString().toDoubleOrNull() ?: 0.0,
            proteins = etNutritionProteins.text.toString().toDoubleOrNull() ?: 0.0
        )
        val selectedCategoryName = spinnerCategory.selectedItem?.toString() ?: ""
        val categoryId = categoryMap[selectedCategoryName] ?: ""

        return Recipe(
            name = etRecipeName.text.toString(),
            ingredients = ingredients,
            instructions = etInstructions.text.toString(),
            categoryId = categoryId,
            nutrition = nutrition
        )
    }

    // Rozdelí reťazec ingrediencií na jednotlivé Ingredient objekty.
    private fun parseIngredients(input: String): List<Ingredient> {
        return input.split(",").mapNotNull { ingredientText ->
            val parts = ingredientText.trim().split(" ")
            if (parts.isNotEmpty()) {
                Ingredient(
                    name = parts[0],
                    weight = parts.getOrNull(1),
                    unit = parts.getOrNull(2) ?: ""
                )
            } else null
        }
    }

    // Zobrazí toast s daným textom.
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
