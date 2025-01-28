package com.example.finalproject_fitrecipesapp.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finalproject_fitrecipesapp.api.ApiClient
import com.example.finalproject_fitrecipesapp.api.Recipe
import com.example.finalproject_fitrecipesapp.api.RecipeCategoryApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel pre DetailActivity na načítanie, úpravu a mazanie receptu.
class DetailViewModel : ViewModel() {

    // Uchováva aktuálny recept (zobrazený v DetailActivity).
    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> get() = _recipe

    // Načíta detail receptu z API podľa recipeId a uloží ho do _recipe.
    fun fetchRecipeDetails(recipeId: String) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getRecipeById(recipeId)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        _recipe.value = response.body()
                    } else {
                        logError("Failed to load recipe details.")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    logError("Error loading recipe details.", t)
                }
            })
    }

    // Prepne stav obľúbenosti receptu a spracuje odpoveď servera.
    fun toggleFavorite(onSuccess: (Boolean) -> Unit, onFailure: () -> Unit) {
        val currentRecipe = _recipe.value ?: return
        val newFavoriteState = !currentRecipe.isFavorite
        currentRecipe.isFavorite = newFavoriteState

        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(currentRecipe._id!!, currentRecipe)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onSuccess(newFavoriteState)
                    } else {
                        logError("Failed to update favorite state.")
                        currentRecipe.isFavorite = !newFavoriteState
                        onFailure()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    logError("Error updating favorite state.", t)
                    currentRecipe.isFavorite = !newFavoriteState
                    onFailure()
                }
            })
    }

    // Zmaže recept so zadaným ID na serveri a spracuje odpoveď.
    fun deleteRecipe(recipeId: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .deleteRecipeById(recipeId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        logError("Failed to delete recipe.")
                        onFailure()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    logError("Error deleting recipe.", t)
                    onFailure()
                }
            })
    }

    // Zapíše chybu do logu.
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e("DetailViewModel", message, throwable)
    }
}
