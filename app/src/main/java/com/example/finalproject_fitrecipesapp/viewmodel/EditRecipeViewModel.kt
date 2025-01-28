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

// ViewModel pre úpravu receptu a načítanie jeho detailov.
class EditRecipeViewModel : ViewModel() {

    // Uchováva načítaný recept, ktorý budeme upravovať.
    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> get() = _recipe

    // Načíta detaily receptu podľa ID a uloží ich do LiveData.
    fun fetchRecipeDetails(recipeId: String, onError: (String) -> Unit) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getRecipeById(recipeId)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        _recipe.value = response.body()
                        if (_recipe.value == null) {
                            onError("Recept sa nenašiel.")
                        }
                    } else {
                        onError("Nepodarilo sa načítať recept.")
                        logError("Nepodarilo sa načítať recept. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    onError("Chyba pri načítavaní receptu.")
                    logError("Chyba pri načítavaní receptu.", t)
                }
            })
    }

    // Aktualizuje recept na serveri a reaguje na úspech alebo chybu.
    fun updateRecipe(
        recipeId: String,
        updatedRecipe: Recipe,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .updateRecipe(recipeId, updatedRecipe)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Recept sa nepodarilo aktualizovať.")
                        logError("Recept sa nepodarilo aktualizovať. Response code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    onError("Chyba pri aktualizácii receptu.")
                    logError("Chyba pri aktualizácii receptu.", t)
                }
            })
    }

    // Zapíše chybovú správu do logu.
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e("EditRecipeViewModel", message, throwable)
    }
}
