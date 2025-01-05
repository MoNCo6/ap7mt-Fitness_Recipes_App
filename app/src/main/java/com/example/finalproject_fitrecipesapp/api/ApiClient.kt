package com.example.finalproject_fitrecipesapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton pre inicializáciu Retrofit klienta
object ApiClient {

    // URL základnej adresy API
    private const val BASE_URL = "http://192.168.0.165:3000/api/"

    // Lazy inicializácia Retrofit inštancie
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Nastaví základnú URL pre všetky API volania
            .addConverterFactory(GsonConverterFactory.create()) // Pridá konvertor pre prácu s JSON pomocou Gson
            .build() // Vytvorí a vráti Retrofit inštanciu
    }
}
