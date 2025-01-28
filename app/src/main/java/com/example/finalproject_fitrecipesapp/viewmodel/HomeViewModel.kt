package com.example.finalproject_fitrecipesapp.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Category
import com.example.finalproject_fitrecipesapp.api.Recipe
import com.example.finalproject_fitrecipesapp.api.RecipeCategoryApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel zodpovedný za načítanie kategórií, receptov a ich filtrovanie, aktualizáciu, mazanie.
class HomeViewModel : ViewModel() {

    // LiveData s kompletným zoznamom kategórií
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // LiveData so všetkými receptami
    private val _allRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val allRecipes: LiveData<List<Recipe>> = _allRecipes

    // LiveData s filtrovaným zoznamom receptov (napr. podľa názvu alebo kategórie)
    private val _filteredRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val filteredRecipes: LiveData<List<Recipe>> = _filteredRecipes

    // Načíta zoznam kategórií aj receptov.
    fun fetchData() {
        fetchCategories()
        fetchRecipes()
    }

    // Zavolá API pre načítanie kategórií a uloží ich do _categories.
    private fun fetchCategories() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getAllCategories()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(
                    call: Call<List<Category>>,
                    response: Response<List<Category>>
                ) {
                    if (response.isSuccessful) {
                        _categories.value = response.body() ?: emptyList()
                    } else {
                        logError("Failed to fetch categories")
                    }
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    logError("Failed to fetch categories", t)
                }
            })
    }

    // Zavolá API pre načítanie všetkých receptov a uloží ich do _allRecipes, zároveň ich nastaví aj do _filteredRecipes ako default.
    private fun fetchRecipes() {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getAllRecipes()
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(
                    call: Call<List<Recipe>>,
                    response: Response<List<Recipe>>
                ) {
                    if (response.isSuccessful) {
                        val recipes = response.body().orEmpty()
                        _allRecipes.value = recipes
                        _filteredRecipes.value = recipes // Na začiatku zobrazíme všetky
                    } else {
                        logError("Failed to fetch recipes")
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    logError("Failed to fetch recipes", t)
                }
            })
    }

    // Filtrovanie receptov podľa názvu (obsahuje reťazec query).
    fun filterRecipes(query: String) {
        val all = _allRecipes.value.orEmpty()
        val result = all.filter { it.name.contains(query, ignoreCase = true) }
        _filteredRecipes.value = result
    }

    // Filtrovanie receptov podľa ID zvolenej kategórie.
    fun filterByCategory(categoryId: String) {
        val all = _allRecipes.value.orEmpty()
        val filtered = all.filter { it.categoryId == categoryId }
        _filteredRecipes.value = filtered
    }

    // Prepínanie stavu obľúbenosti receptu (isFavorite). Po úspešnej aktualizácii volá onSuccess, inak onFailure.
    fun toggleFavorite(recipe: Recipe, onSuccess: (Recipe) -> Unit, onFailure: () -> Unit) {
        recipe.isFavorite = !recipe.isFavorite

        // Aktualizácia stavu obľúbenosti na serveri
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(recipe._id!!, recipe)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onSuccess(recipe) // Vraciame aktualizovaný recept
                    } else {
                        logError("Failed to update favorite state.")
                        onFailure()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    logError("Error updating favorite state.", t)
                    onFailure()
                }
            })
    }

    // Spracovanie vymazaného receptu: odstráni ho z _allRecipes a tiež z _filteredRecipes, ak sa tam nachádzal.
    fun handleDeletedRecipe(deletedRecipeId: String) {
        val currentList = _allRecipes.value.orEmpty().toMutableList()
        if (currentList.removeAll { it._id == deletedRecipeId }) {
            _allRecipes.value = currentList
            val currentFiltered = _filteredRecipes.value.orEmpty().toMutableList()
            currentFiltered.removeAll { it._id == deletedRecipeId }
            _filteredRecipes.value = currentFiltered
        } else {
            logError("Recipe with ID $deletedRecipeId not found!")
        }
    }

    // Po návrate z DetailActivity môžeme aktualizovať stav obľúbenosti receptu.
    fun updateFavoriteState(recipeId: String, isFavorite: Boolean) {
        val currentList = _allRecipes.value.orEmpty().toMutableList()
        val recipe = currentList.find { it._id == recipeId }
        if (recipe != null) {
            recipe.isFavorite = isFavorite
            _allRecipes.value = currentList

            // Zmeníme aj v _filteredRecipes, ak sa tam aktuálne nachádza
            val filteredList = _filteredRecipes.value.orEmpty().toMutableList()
            val filteredRecipe = filteredList.find { it._id == recipeId }
            if (filteredRecipe != null) {
                filteredRecipe.isFavorite = isFavorite
                _filteredRecipes.value = filteredList
            }
        } else {
            logError("Recipe with ID $recipeId not found!")
        }
    }

    // Zaznamenanie chyby do logu.
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e("HomeViewModel", message, throwable)
    }
}
