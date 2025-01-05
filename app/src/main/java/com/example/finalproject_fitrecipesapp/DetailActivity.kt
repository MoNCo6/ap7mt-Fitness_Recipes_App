package com.example.finalproject_fitrecipesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Recipe
import com.example.finalproject_fitrecipesapp.api.RecipeCategoryApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private lateinit var tvInstructions: TextView
    private lateinit var tvName: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var tvNutritions: TextView
    private lateinit var tvServing: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDeleteRecipe: Button
    private lateinit var btnEditRecipe: Button
    private lateinit var btnFavorite: ImageButton
    private lateinit var recipeId: String
    private var recipe: Recipe? = null // Uchováva aktuálny recept

    companion object {
        private const val EDIT_RECIPE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initializeViews() // Inicializácia UI komponentov
        setupListeners()  // Nastavenie listenerov pre akcie používateľa

        recipeId = intent.getStringExtra("RECIPE_ID").orEmpty()

        if (recipeId.isNotBlank()) {
            fetchRecipeDetails() // Načítanie detailov receptu z API
        } else {
            showToast("Neplatné ID receptu.")
        }
    }

    // Inicializácia UI komponentov
    private fun initializeViews() {
        tvInstructions = findViewById(R.id.tv_instructions)
        tvName = findViewById(R.id.tv_recipe_name)
        tvIngredients = findViewById(R.id.tv_ingredients)
        tvNutritions = findViewById(R.id.tv_nutritions)
        tvServing = findViewById(R.id.tv_serving)
        btnBack = findViewById(R.id.img_toolbar_btn_back)
        btnDeleteRecipe = findViewById(R.id.btn_delete_recipe)
        btnEditRecipe = findViewById(R.id.btn_edit_recipe)
        btnFavorite = findViewById(R.id.img_toolbar_btn_fav)
    }

    // Nastavenie listenerov pre tlačidlá
    private fun setupListeners() {
        btnBack.setOnClickListener { onBackPressed() }

        btnDeleteRecipe.setOnClickListener {
            if (recipe?.isFavorite == true) {
                showCannotDeleteFavoriteDialog()
            } else {
                showDeleteConfirmationDialog()
            }
        }

        btnEditRecipe.setOnClickListener { openEditRecipeActivity() }

        btnFavorite.setOnClickListener { toggleFavorite() }
    }

    // Zobrazenie upozornenia pre obľúbené recepty
    private fun showCannotDeleteFavoriteDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Pozor!")
            .setMessage("Tento recept je označený ako obľúbený a nemôže byť vymazaný.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Prepnutie stavu obľúbenosti receptu
    private fun toggleFavorite() {
        recipe?.let {
            it.isFavorite = !it.isFavorite
            updateFavoriteIcon(it.isFavorite)
            saveFavoriteState(it)
            sendFavoriteUpdateResult(it)
        }
    }

    // Aktualizácia ikony pre obľúbenosť
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.fav_fill else R.drawable.fav_unfill
        )
    }

    // Uloženie stavu obľúbenosti na server
    private fun saveFavoriteState(recipe: Recipe) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(recipe._id!!, recipe)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    val message = if (recipe.isFavorite) "Pridané medzi obľúbené" else "Odstránené z obľúbených"
                    showToast(message)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast("Chyba pri ukladaní do obľúbených.")
                }
            })
    }

    // Odoslanie aktualizácie obľúbeného stavu späť
    private fun sendFavoriteUpdateResult(recipe: Recipe) {
        Intent().apply {
            putExtra("UPDATED_RECIPE_ID", recipe._id)
            putExtra("IS_FAVORITE", recipe.isFavorite)
        }.also {
            setResult(RESULT_OK, it)
        }
    }

    // Zobrazenie potvrdenia mazania receptu
    private fun showDeleteConfirmationDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Vymazanie receptu")
            .setMessage("Naozaj chcete vymazať tento recept?")
            .setPositiveButton("Áno") { _, _ -> deleteRecipe() }
            .setNegativeButton("Nie") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Načítanie detailov receptu z API
    private fun fetchRecipeDetails() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getRecipeById(recipeId)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            recipe = it
                            bindRecipeDetails(it)
                            updateFavoriteIcon(it.isFavorite)
                        } ?: showToast("Recept sa nenašiel.")
                    } else {
                        handleError("Failed to load recipe details.")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    handleError("Error loading recipe details.", t)
                }
            })
    }

    // Zobrazenie detailov receptu v UI
    private fun bindRecipeDetails(recipe: Recipe) {
        tvName.text = recipe.name
        tvInstructions.text = recipe.instructions.ifEmpty { "Žiadne inštrukcie." }
        tvIngredients.text = recipe.ingredients.joinToString("\n") {
            "${it.name}: ${it.weight ?: "N/A"} ${it.unit}"
        }.ifEmpty { "Žiadne ingrediencie." }
        tvServing.text = recipe.nutrition.perServing.ifEmpty { "N/A" }
        tvNutritions.text = """
            Kalórie: ${recipe.nutrition.kcal} kcal
            Bielkoviny: ${recipe.nutrition.proteins} g
            Tuky: ${recipe.nutrition.fats} g
            Sacharidy: ${recipe.nutrition.carbohydrates} g
        """.trimIndent()
    }

    // Mazanie receptu
    private fun deleteRecipe() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .deleteRecipeById(recipeId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        showToast("Recept bol úspešne odstránený!")
                        setResultAndFinish()
                    } else {
                        handleError("Failed to delete recipe.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    handleError("Error deleting recipe.", t)
                }
            })
    }

    // Otvorenie aktivity na úpravu receptu
    private fun openEditRecipeActivity() {
        val intent = Intent(this, EditRecipeActivity::class.java).apply {
            putExtra("RECIPE_ID", recipeId)
        }
        startActivityForResult(intent, EDIT_RECIPE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_RECIPE_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedRecipeId = data?.getStringExtra("UPDATED_RECIPE_ID")

            Intent().apply {
                putExtra("UPDATED_RECIPE_ID", updatedRecipeId)
            }.also {
                setResult(RESULT_OK, it)
            }

            fetchRecipeDetails()
        }
    }

    // Nastavenie výsledku pre HomeActivity a ukončenie aktivity
    private fun setResultAndFinish() {
        Intent().apply {
            putExtra("DELETED_RECIPE_ID", recipeId)
        }.also {
            setResult(RESULT_OK, it)
        }
        finish()
    }

    // Zobrazenie správy pre používateľa
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Zaznamenanie chyby
    private fun handleError(message: String, throwable: Throwable? = null) {
        Log.e("DetailActivity", message, throwable)
        showToast(message)
    }
}
