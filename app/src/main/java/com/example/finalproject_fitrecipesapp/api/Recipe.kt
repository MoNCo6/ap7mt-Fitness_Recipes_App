package com.example.finalproject_fitrecipesapp.api

// Trieda dát pre reprezentáciu receptu
data class Recipe(
    val _id: String? = null, // Jedinečný identifikátor receptu
    val name: String = "", // Názov receptu
    val ingredients: List<Ingredient> = emptyList(), // Zoznam ingrediencií
    val instructions: String = "", // Postup prípravy receptu
    val categoryId: String = "", // ID kategórie, do ktorej recept patrí
    val nutrition: Nutrition = Nutrition(), // Nutričné hodnoty receptu
    var isFavorite: Boolean = false // Stav obľúbeného receptu
)

// Trieda dát pre ingrediencie receptu
data class Ingredient(
    val name: String = "", // Názov ingrediencie
    val weight: String? = null, // Váha ingrediencie
    val unit: String = "" // Jednotka ingrediencie
)

// Trieda dát pre nutričné hodnoty receptu
data class Nutrition(
    val perServing: String = "150g", // Veľkosť jednej porcie
    val kcal: Double = 0.0, // Kalórie na porciu
    val carbohydrates: Double = 0.0, // Sacharidy na porciu
    val fats: Double = 0.0, // Tuky na porciu
    val proteins: Double = 0.0 // Bielkoviny na porciu
)
