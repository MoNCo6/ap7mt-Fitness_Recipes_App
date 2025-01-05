package com.example.finalproject_fitrecipesapp.api

import retrofit2.Call
import retrofit2.http.*

// Rozhranie definujúce API volania pre kategórie a recepty
interface RecipeCategoryApi {

    // Získa všetky kategórie
    @GET("categories")
    fun getAllCategories(): Call<List<Category>>

    // Získa všetky recepty
    @GET("recipes")
    fun getAllRecipes(): Call<List<Recipe>>

    // Získa recept podľa ID
    @GET("recipes/{id}")
    fun getRecipeById(@Path("id") id: String): Call<Recipe>

    // Vytvorí nový recept
    @POST("recipes")
    fun createRecipe(@Body recipe: Recipe): Call<Recipe>

    // Zmaže recept podľa ID
    @DELETE("recipes/{id}")
    fun deleteRecipeById(@Path("id") id: String): Call<Void>

    // Aktualizuje recept podľa ID
    @PUT("recipes/{id}")
    fun updateRecipe(@Path("id") recipeId: String, @Body updatedRecipe: Recipe): Call<Void>
}
