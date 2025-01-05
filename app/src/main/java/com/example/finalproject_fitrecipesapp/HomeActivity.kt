package com.example.finalproject_fitrecipesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Recipe
import com.example.finalproject_fitrecipesapp.api.RecipeCategoryApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeCountTextView: TextView
    private val allRecipes = mutableListOf<Recipe>()
    private val filteredRecipes = mutableListOf<Recipe>()

    companion object {
        private const val DETAIL_ACTIVITY_REQUEST_CODE = 101
        private const val ADD_RECIPE_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initializeViews() // Inicializácia UI komponentov
        setupListeners()  // Nastavenie listenerov pre interakcie používateľa
        fetchData()        // Načítanie údajov z API
    }

    // Inicializuje UI komponenty.
    private fun initializeViews() {
        recipeCountTextView = findViewById(R.id.tv_recipe_count)
        setupAdapters()     // Nastavenie adaptérov
        setupRecyclerViews() // Inicializácia RecyclerView
        setupSearchView()    // Nastavenie SearchView pre vyhľadávanie
    }

    //Nastaví adaptéry pre kategórie a recepty.
    private fun setupAdapters() {
        categoryAdapter = CategoryAdapter { categoryId -> filterByCategory(categoryId) }
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipeId -> openDetailActivity(recipeId) },
            onFavoriteClick = { recipe -> toggleFavorite(recipe) }
        )
    }

    //Inicializuje RecyclerView pre kategórie a recepty.
    private fun setupRecyclerViews() {
        findViewById<RecyclerView>(R.id.item_category).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        findViewById<RecyclerView>(R.id.item_recipes).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recipeAdapter
        }
    }

    //Nastaví SearchView pre vyhľadávanie receptov podľa mena.
    private fun setupSearchView() {
        findViewById<SearchView>(R.id.search_view).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterRecipes(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterRecipes(newText.orEmpty())
                    return true
                }
            })
        }
    }

    //Nastaví listenery na akcie používateľa.
    private fun setupListeners() {
        findViewById<Button>(R.id.btn_add_recipe).setOnClickListener {
            startActivityForResult(Intent(this, AddRecipeActivity::class.java), ADD_RECIPE_REQUEST_CODE)
        }
        findViewById<Button>(R.id.btn_all_recipes).setOnClickListener {
            recipeAdapter.updateRecipes(allRecipes)
            updateRecipeCount(allRecipes.size, allRecipes.size)
        }
        findViewById<Button>(R.id.btn_favorites).setOnClickListener {
            showFavorites()
        }
    }

    //Zobrazuje len obľúbené recepty.
    private fun showFavorites() {
        val favoriteRecipes = allRecipes.filter { it.isFavorite }
        recipeAdapter.updateRecipes(favoriteRecipes)
        updateRecipeCount(favoriteRecipes.size, allRecipes.size)
    }

    // Prepína stav receptu medzi obľúbeným a neobľúbeným.
    private fun toggleFavorite(recipe: Recipe) {
        recipe.isFavorite = !recipe.isFavorite

        // Aktualizácia stavu obľúbenosti na serveri
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(recipe._id!!, recipe)
            .enqueue(object : Callback<Void> {
                // Spracovanie odpovede zo servera
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) {
                        Log.e("HomeActivity", "Failed to update favorite state.")
                        return
                    }

                    // Ak recept už nie je obľúbený, aktualizujeme zoznam obľúbených receptov
                    if (!recipe.isFavorite) {
                        val favoriteRecipes = allRecipes.filter { it.isFavorite }
                        recipeAdapter.updateRecipes(favoriteRecipes) // Aktualizácia zobrazenia
                        updateRecipeCount(favoriteRecipes.size, allRecipes.size) // Aktualizácia počítadla
                    } else {
                        recipeAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("HomeActivity", "Error updating favorite state.", t)
                }
            })
    }


    //Načíta kategórie a recepty z API.
    private fun fetchData() {
        fetchCategories()
        fetchRecipes()
    }

    //Načíta kategórie z API a nastaví ich do adaptéra.
    private fun fetchCategories() {
        ApiClient.instance.create(RecipeCategoryApi::class.java).getAllCategories()
            .enqueue(apiCallback(
                onSuccess = { categories -> categoryAdapter.updateCategories(categories) },
                onError = { logError("Failed to fetch categories") }
            ))
    }

    //Načíta recepty z API a nastaví ich do adaptéra.
    private fun fetchRecipes() {
        ApiClient.instance.create(RecipeCategoryApi::class.java).getAllRecipes()
            .enqueue(apiCallback(
                onSuccess = { recipes ->
                    allRecipes.apply {
                        clear()
                        addAll(recipes)
                    }
                    recipeAdapter.updateRecipes(allRecipes)
                    updateRecipeCount(allRecipes.size, allRecipes.size)
                },
                onError = { logError("Failed to fetch recipes") }
            ))
    }

    //Filtrovanie receptov podľa mena.
    private fun filterRecipes(query: String) {
        filteredRecipes.apply {
            clear()
            addAll(allRecipes.filter { it.name.contains(query, ignoreCase = true) })
        }
        recipeAdapter.updateRecipes(filteredRecipes)
        updateRecipeCount(filteredRecipes.size, allRecipes.size)
    }

    //Filtrovanie receptov podľa kategórie.
    private fun filterByCategory(categoryId: String) {
        val filtered = allRecipes.filter { it.categoryId == categoryId }
        recipeAdapter.updateRecipes(filtered)
        updateRecipeCount(filtered.size, allRecipes.size)
    }

    //Otvorí detail receptu.
    private fun openDetailActivity(recipeId: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ADD_RECIPE_REQUEST_CODE -> if (resultCode == RESULT_OK) fetchRecipes()
            DETAIL_ACTIVITY_REQUEST_CODE -> handleRecipeUpdate(data, resultCode)
        }
    }

    //Spracovanie zmien v stave obľúbenosti alebo odstránenia receptu.
    private fun handleRecipeUpdate(data: Intent?, resultCode: Int) {
        data?.let {
            val deletedRecipeId = it.getStringExtra("DELETED_RECIPE_ID")
            if (deletedRecipeId != null) {
                handleDeletedRecipe(deletedRecipeId)
                return
            }

            val updatedRecipeId = it.getStringExtra("UPDATED_RECIPE_ID")
            val isFavorite = it.getBooleanExtra("IS_FAVORITE", false)

            updatedRecipeId?.let { recipeId ->
                updateFavoriteState(recipeId, isFavorite)
            }

            if (resultCode == RESULT_OK) {
                fetchRecipes() // Aktualizuje zoznam receptov po úprave
            }
        }
    }

   //Spracovanie odstráneného receptu.
    private fun handleDeletedRecipe(deletedRecipeId: String) {
        if (allRecipes.removeAll { it._id == deletedRecipeId }) {
            recipeAdapter.updateRecipes(allRecipes)
            updateRecipeCount(allRecipes.size, allRecipes.size)
        } else {
            Log.e("HomeActivity", "Recipe with ID $deletedRecipeId not found!")
        }
    }

    //Aktualizácia stavu obľúbenosti receptu.
    private fun updateFavoriteState(recipeId: String, isFavorite: Boolean) {
        val recipe = allRecipes.find { it._id == recipeId }
        if (recipe != null) {
            recipe.isFavorite = isFavorite
            recipeAdapter.notifyDataSetChanged()
        } else {
            Log.e("HomeActivity", "Recipe with ID $recipeId not found!")
        }
    }

    //Aktualizácia počítadla receptov.
    private fun updateRecipeCount(filteredCount: Int, totalCount: Int) {
        recipeCountTextView.text = "$filteredCount/$totalCount"
    }

    //Zaznamenáva chyby do logu.
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e("HomeActivity", message, throwable)
    }

    //Vytvára Callback pre API volania.
    private fun <T> apiCallback(onSuccess: (T) -> Unit, onError: () -> Unit): Callback<T> {
        return object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError()
                } else onError()
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                logError("API call failed", t)
                onError()
            }
        }
    }
}
