package com.example.finalproject_fitrecipesapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject_fitrecipesapp.R
import com.example.finalproject_fitrecipesapp.api.Recipe

// Detail obrazovka, ktorá zobrazuje podrobnosti o konkrétnom recepte a umožňuje jeho úpravu, mazanie či označenie ako obľúbený.
class DetailActivity : AppCompatActivity() {

    // UI prvky
    private lateinit var tvInstructions: TextView
    private lateinit var tvName: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var tvNutritions: TextView
    private lateinit var tvServing: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDeleteRecipe: Button
    private lateinit var btnEditRecipe: Button
    private lateinit var btnFavorite: ImageButton

    // ID receptu, ktorý sa zobrazuje / upravuje
    private lateinit var recipeId: String

    // ViewModel zabezpečujúci logiku pre túto obrazovku
    private lateinit var viewModel: DetailViewModel

    companion object {
        // Konštanta pre zistenie, či sa vraciame z úpravy receptu
        private const val EDIT_RECIPE_REQUEST_CODE = 100
    }

    // Metóda volaná pri vytváraní obrazovky. Inicializuje ViewModel, UI prvky a nastavuje pozorovanie dát.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Inicializuje ViewModel
        viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)

        // Nastavíme UI prvky a listenery
        initializeViews()
        setupListeners()

        // Získame ID receptu z predchádzajúcej obrazovky (HomeActivity)
        recipeId = intent.getStringExtra("RECIPE_ID").orEmpty()

        // Ak máme platné ID, načítame detaily receptu z ViewModelu
        if (recipeId.isNotBlank()) {
            viewModel.fetchRecipeDetails(recipeId)
        } else {
            showToast("Neplatné ID receptu.")
        }

        // Začneme pozorovať LiveData vo ViewModeli
        observeViewModel()
    }

    //  Inicializácia UI komponentov. Priradí premennej príslušné prvky na obrazovke.
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

    //  Listenery pre UI akcie. Nastavenie kliknutí na rôzne tlačidlá.
    private fun setupListeners() {
        // Tlačidlo 'Späť'
        btnBack.setOnClickListener { onBackPressed() }
        // Tlačidlo 'Zmazať recept'
        btnDeleteRecipe.setOnClickListener { onDeleteRecipeClicked() }
        // Tlačidlo 'Upraviť recept'
        btnEditRecipe.setOnClickListener { openEditRecipeActivity() }
        // Tlačidlo 'Obľúbený'
        btnFavorite.setOnClickListener { toggleFavorite() }
    }

    // Rozhodnutie, či je možné recept vymazať, alebo je obľúbený. Podľa toho buď zobrazí upozornenie, alebo zmaže recept.
    private fun onDeleteRecipeClicked() {
        val currentRecipe = viewModel.recipe.value
        if (currentRecipe?.isFavorite == true) {
            showCannotDeleteFavoriteDialog()
        } else {
            showDeleteConfirmationDialog()
        }
    }

    // Sledovanie ViewModelu - keď sa recept načíta alebo zmení. Keď sa zmení 'recipe', zobrazí ho na obrazovke.
    private fun observeViewModel() {
        viewModel.recipe.observe(this, Observer { recipe ->
            if (recipe != null) {
                bindRecipeDetails(recipe)
                updateFavoriteIcon(recipe.isFavorite)
            } else {
                showToast("Recept sa nenašiel.")
            }
        })
    }

    //  Obsluha stavu obľúbenosti (isFavorite) receptu cez ViewModel. Po úspechu aktualizuje ikonu a odošle informáciu späť do HomeActivity.
    private fun toggleFavorite() {
        viewModel.toggleFavorite(
            onSuccess = { newFavoriteState ->
                // Zmeníme ikonu (vyplnená/nevypnená)
                updateFavoriteIcon(newFavoriteState)
                // Zobrazíme informáciu používateľovi
                val message = if (newFavoriteState) "Pridané medzi obľúbené" else "Odstránené z obľúbených"
                showToast(message)
                // Odošleme do HomeActivity, že recept bol aktualizovaný
                sendFavoriteUpdateResult(recipeId, newFavoriteState)
            },
            onFailure = {
                // Ak sa update nepodarí, oznámime chybu
                showToast("Chyba pri ukladaní do obľúbených.")
            }
        )
    }

    // Upraví ikonku podľa stavu obľúbenosti.
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.fav_fill else R.drawable.fav_unfill
        )
    }

    // Vráti informáciu o zmene obľúbeného stavu späť do HomeActivity.
    private fun sendFavoriteUpdateResult(recipeId: String, isFavorite: Boolean) {
        Intent().apply {
            putExtra("UPDATED_RECIPE_ID", recipeId)
            putExtra("IS_FAVORITE", isFavorite)
        }.also {
            setResult(RESULT_OK, it)
        }
    }

    // Mazanie receptu
    // Dialog oznamujúci, že recept je obľúbený a nemožno ho vymazať.
    private fun showCannotDeleteFavoriteDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Pozor!")
            .setMessage("Tento recept je označený ako obľúbený a nemôže byť vymazaný.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Dialog na potvrdenie vymazania receptu.
    private fun showDeleteConfirmationDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Vymazanie receptu")
            .setMessage("Naozaj chcete vymazať tento recept?")
            .setPositiveButton("Áno") { _, _ -> deleteRecipe() }
            .setNegativeButton("Nie") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // Zavolá ViewModel, aby vymazal recept zo servera. Po úspechu ukončí túto obrazovku.
    private fun deleteRecipe() {
        viewModel.deleteRecipe(recipeId,
            onSuccess = {
                showToast("Recept bol úspešne odstránený!")
                setResultAndFinish()
            },
            onFailure = {
                showToast("Failed to delete recipe.")
            }
        )
    }

    //  Zobrazenie detailov receptu v UI. Vyplní textové polia detailu receptu údajmi.
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

    // Editácia receptu. Otvorí obrazovku na úpravu receptu s ID tohto receptu.
    private fun openEditRecipeActivity() {
        val intent = Intent(this, EditRecipeActivity::class.java).apply {
            putExtra("RECIPE_ID", recipeId)
        }
        startActivityForResult(intent, EDIT_RECIPE_REQUEST_CODE)
    }

    // Spracuje výsledok z EditRecipeActivity. Ak recept upravíme, znovu načítame jeho detaily.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_RECIPE_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedRecipeId = data?.getStringExtra("UPDATED_RECIPE_ID")

            // Vrátime info aj do HomeActivity, že recept bol upravený
            Intent().apply {
                putExtra("UPDATED_RECIPE_ID", updatedRecipeId)
            }.also {
                setResult(RESULT_OK, it)
            }

            // Znova načítame detaily receptu z ViewModelu
            viewModel.fetchRecipeDetails(recipeId)
        }
    }

    //  Dokončenie a návrat do HomeActivity. Nastaví výsledok a ukončí túto obrazovku.
    private fun setResultAndFinish() {
        Intent().apply {
            putExtra("DELETED_RECIPE_ID", recipeId)
        }.also {
            setResult(RESULT_OK, it)
        }
        finish()
    }

    // Zobrazenie Toast správy používateľovi.
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
