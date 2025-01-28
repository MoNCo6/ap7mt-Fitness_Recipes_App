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

// ViewModel pre pridanie nového receptu a načítanie kategórií.
class AddRecipeViewModel : ViewModel() {

    // Uchováva mapu názvov kategórií na ich ID.
    private val _categoryMap = MutableLiveData<Map<String, String>>(emptyMap())
    val categoryMap: LiveData<Map<String, String>> = _categoryMap

    // Zavolá API pre načítanie všetkých kategórií a uloží ich do _categoryMap.
    fun fetchCategories(onError: (String) -> Unit) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .getAllCategories()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                    if (response.isSuccessful) {
                        val categoryList = response.body().orEmpty()
                        // Pretransformujeme zoznam kategórií na mapu (key = categoryName, value = categoryId)
                        val map = categoryList.associate { it.categoryName to it.categoryId }
                        _categoryMap.value = map
                    } else {
                        val msg = "Kategórie sa nepodarilo načítať."
                        onError(msg)
                        logError(msg + " - code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    onError("Chyba pri načítavaní kategórií.")
                    logError("Chyba pri načítavaní kategórií.", t)
                }
            })
    }

    // Zavolá API pre vytvorenie nového receptu a reaguje na výsledok.
    fun createRecipe(recipe: Recipe, onSuccess: () -> Unit, onError: (String) -> Unit) {
        ApiClient.instance.create(RecipeCategoryApi::class.java)
            .createRecipe(recipe)
            .enqueue(object : Callback<Recipe> {
                override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        val msg = "Nepodarilo sa pridať recept."
                        onError(msg)
                        logError(msg + " - code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Recipe>, t: Throwable) {
                    onError("Chyba pri pridávaní receptu.")
                    logError("Chyba pri pridávaní receptu.", t)
                }
            })
    }

    // Zapíše chybovú správu do logu.
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e("AddRecipeViewModel", message, throwable)
    }
}
