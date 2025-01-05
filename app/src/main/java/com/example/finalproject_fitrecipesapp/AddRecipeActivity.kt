package com.example.finalproject_fitrecipesapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject_fitrecipesapp.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRecipeActivity : AppCompatActivity() {

    private val categoryMap = mutableMapOf<String, String>() // Mapovanie názvov kategórií na ich ID

    private lateinit var etRecipeName: EditText
    private lateinit var etNutritionKcal: EditText
    private lateinit var etNutritionCarbs: EditText
    private lateinit var etNutritionFats: EditText
    private lateinit var etNutritionProteins: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSaveRecipe: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        initializeViews() // Inicializácia UI komponentov
        setupBackButton() // Nastavenie tlačidla na návrat späť
        fetchCategories() // Načítanie dostupných kategórií
        setupSaveButton() // Nastavenie tlačidla na uloženie receptu
    }

    // Inicializuje všetky potrebné UI komponenty
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

    // Nastavenie tlačidla na návrat späť
    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.img_toolbar_btn_back).setOnClickListener { onBackPressed() }
    }

    // Načíta kategórie z API a nastaví ich do spinneru
    private fun fetchCategories() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getAllCategories()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { categoryList ->
                            // Vymazanie existujúcich a pridanie nových kategórií
                            categoryMap.clear()
                            categoryList.forEach { category ->
                                categoryMap[category.categoryName] = category.categoryId
                            }

                            // Nastavenie kategórií do spinneru
                            spinnerCategory.adapter = ArrayAdapter(
                                this@AddRecipeActivity,
                                android.R.layout.simple_spinner_item,
                                categoryMap.keys.toList()
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                        }
                    } else {
                        showToast("Kategórie sa nepodarilo načítať.")
                    }
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    showToast("Chyba pri načítavaní kategórií.")
                }
            })
    }

    // Nastavenie tlačidla na uloženie receptu
    private fun setupSaveButton() {
        btnSaveRecipe.setOnClickListener {
            val recipe = collectRecipeData() // Zber údajov z formulára
            saveRecipe(recipe) // Uloženie receptu na server
        }
    }

    // Zber údajov z formulára a vytvorenie objektu receptu
    private fun collectRecipeData(): Recipe {
        val ingredients = etIngredients.text.toString().split(",").mapNotNull { ingredientText ->
            val parts = ingredientText.trim().split(" ")
            if (parts.isNotEmpty()) {
                Ingredient(
                    name = parts[0],
                    weight = parts.getOrNull(1),
                    unit = parts.getOrNull(2) ?: ""
                )
            } else null
        }
        val nutrition = Nutrition(
            perServing = "100g",
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

    // Uloženie receptu na server
    private fun saveRecipe(recipe: Recipe) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .createRecipe(recipe)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        showToast("Recept úspešne pridaný!")
                        finish() // Návrat na predchádzajúcu obrazovku
                    } else {
                        showToast("Nepodarilo sa pridať recept.")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    showToast("Chyba pri pridávaní receptu.")
                }
            })
    }

    // Zobrazí toast so správou
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}