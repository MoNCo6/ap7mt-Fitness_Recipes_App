package com.example.finalproject_fitrecipesapp.api

// Trieda dát pre reprezentáciu kategórie
data class Category(
    val categoryId: String, // Jedinečný identifikátor kategórie
    val categoryName: String, // Názov kategórie
    val description: String = "" // Popis kategórie
)
