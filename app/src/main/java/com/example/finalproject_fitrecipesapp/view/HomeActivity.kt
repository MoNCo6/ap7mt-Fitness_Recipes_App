package com.example.finalproject_fitrecipesapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_fitrecipesapp.CategoryAdapter
import com.example.finalproject_fitrecipesapp.R
import com.example.finalproject_fitrecipesapp.RecipeAdapter
import com.example.finalproject_fitrecipesapp.api.Recipe

// Hlavná obrazovka aplikácie, kde sa zobrazujú kategórie a recepty. Umožňuje vyhľadávanie, filtrovanie a preklik na detail.
class HomeActivity : AppCompatActivity() {

    // Adaptery pre zobrazovanie kategórií a receptov v RecyclerView.
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recipeAdapter: RecipeAdapter

    // TextView, ktorý zobrazuje počet filtrovanych / všetkých receptov.
    private lateinit var recipeCountTextView: TextView

    // ViewModel, ktorý drží a spracováva dáta pre túto obrazovku.
    private lateinit var viewModel: HomeViewModel

    companion object {
        // Konštanty pre rôzne kódy návratu z iných aktivít
        private const val DETAIL_ACTIVITY_REQUEST_CODE = 101
        private const val ADD_RECIPE_REQUEST_CODE = 102
    }

    // Metóda, ktorá sa volá pri vytvorení aktivity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializácia ViewModelu
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Nastavenie zobrazenia a komponentov
        initializeViews()
        setupListeners()

        // Nastavenie "pozorovania" dát z ViewModelu
        observeViewModel()

        // Načítanie kategórií a receptov z API
        viewModel.fetchData()
    }

    // Pripraví obrazovku – inicializuje adaptéry, RecyclerView a SearchView.
    private fun initializeViews() {
        recipeCountTextView = findViewById(R.id.tv_recipe_count)
        setupAdapters()
        setupRecyclerViews()
        setupSearchView()
    }

    // Vytvorí a pripojí adaptér pre kategórie a adaptér pre recepty.
    private fun setupAdapters() {
        // Adaptér pre kategórie – po kliknutí na kategóriu filtruje recepty podľa categoryId
        categoryAdapter = CategoryAdapter { categoryId ->
            viewModel.filterByCategory(categoryId)
        }
        // Adaptér pre recepty – po kliknutí na recept otvorí detail, menenie obľúbenosti
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipeId -> openDetailActivity(recipeId) },
            onFavoriteClick = { recipe -> toggleFavorite(recipe) }
        )
    }

    // Nastaví RecyclerView, ktoré zobrazujú kategórie a recepty.
    private fun setupRecyclerViews() {
        // RecyclerView pre kategórie
        findViewById<RecyclerView>(R.id.item_category).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        // RecyclerView pre recepty
        findViewById<RecyclerView>(R.id.item_recipes).apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recipeAdapter
        }
    }

    // Nastaví vyhľadávanie receptov podľa zadaného textu (SearchView).
    private fun setupSearchView() {
        findViewById<SearchView>(R.id.search_view).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                // Po potvrdení vyhľadávacieho query
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.filterRecipes(query.orEmpty())
                    return true
                }
                // Po každej zmene textu vo vyhľadávaní
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.filterRecipes(newText.orEmpty())
                    return true
                }
            })
        }
    }

    // Nastaví reakcie na kliknutia tlačidiel.
    private fun setupListeners() {
        // Klik na "Pridať recept"
        findViewById<Button>(R.id.btn_add_recipe).setOnClickListener {
            startActivityForResult(Intent(this, AddRecipeActivity::class.java), ADD_RECIPE_REQUEST_CODE)
        }
        // Klik na "Všetky recepty"
        findViewById<Button>(R.id.btn_all_recipes).setOnClickListener {
            // Zobrazenie všetkých receptov
            viewModel.allRecipes.value?.let {
                recipeAdapter.updateRecipes(it)
                updateRecipeCount(it.size, it.size)
            }
        }
        // Klik na "Obľúbené"
        findViewById<Button>(R.id.btn_favorites).setOnClickListener {
            // Zobrazenie len obľúbených receptov
            val favoriteRecipes = viewModel.allRecipes.value.orEmpty().filter { it.isFavorite }
            recipeAdapter.updateRecipes(favoriteRecipes)
            updateRecipeCount(favoriteRecipes.size, viewModel.allRecipes.value?.size ?: 0)
        }
    }

    // Pozorujeme LiveData z ViewModelu a reagujeme na zmeny.
    private fun observeViewModel() {
        // Aktualizácia kategórií
        viewModel.categories.observe(this) { categories ->
            categoryAdapter.updateCategories(categories)
        }

        // Aktualizácia kompletného zoznamu receptov
        viewModel.allRecipes.observe(this) { all ->
            recipeAdapter.updateRecipes(all)
            updateRecipeCount(all.size, all.size)
        }

        // Aktualizácia filtrovaných receptov (napr. po vyhľadaní alebo zvolení kategórie)
        viewModel.filteredRecipes.observe(this) { filtered ->
            recipeAdapter.updateRecipes(filtered)
            updateRecipeCount(filtered.size, viewModel.allRecipes.value?.size ?: 0)
        }
    }

    // Prepína stav obľúbeného receptu a po úspešnej zmene aktualizuje zobrazenie.
    private fun toggleFavorite(recipe: Recipe) {
        viewModel.toggleFavorite(
            recipe,
            onSuccess = { updatedRecipe ->
                // Ak recept prestal byť obľúbený, zobrazíme len aktuálne obľúbené
                if (!updatedRecipe.isFavorite) {
                    val favoriteRecipes = viewModel.allRecipes.value.orEmpty().filter { it.isFavorite }
                    recipeAdapter.updateRecipes(favoriteRecipes)
                    updateRecipeCount(favoriteRecipes.size, viewModel.allRecipes.value?.size ?: 0)
                } else {
                    // Inak iba aktualizujeme adapter (napr. zmenu ikony)
                    recipeAdapter.notifyDataSetChanged()
                }
            },
            onFailure = {
                // Ak update na serveri zlyhal, vrátime pôvodný stav
                recipe.isFavorite = !recipe.isFavorite
                recipeAdapter.notifyDataSetChanged()
            }
        )
    }

    // Otvorenie obrazovky DetailActivity pre konkrétny recept (podľa ID).
    private fun openDetailActivity(recipeId: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST_CODE)
    }

    // Spracovanie výsledku z iných obrazoviek.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Ak sa vrátime z aktivity na pridávanie receptu
            ADD_RECIPE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    // Znovu načítame všetky recepty (či sa niečo nepridalo)
                    viewModel.fetchData()
                }
            }
            // Ak sa vrátime z DetailActivity
            DETAIL_ACTIVITY_REQUEST_CODE -> {
                handleRecipeUpdate(data, resultCode)
            }
        }
    }

    // Spracovanie prípadného vymazania alebo úpravy obľúbenosti receptu po návrate z DetailActivity.
    private fun handleRecipeUpdate(data: Intent?, resultCode: Int) {
        data?.let {
            // Skontrolujeme, či nebol recept vymazaný
            val deletedRecipeId = it.getStringExtra("DELETED_RECIPE_ID")
            if (deletedRecipeId != null) {
                // Vymažeme ho z ViewModelu
                viewModel.handleDeletedRecipe(deletedRecipeId)
                return
            }

            // Ak nie je vymazaný, pozrieme sa, či sa nezmenila obľúbenosť
            val updatedRecipeId = it.getStringExtra("UPDATED_RECIPE_ID")
            val isFavorite = it.getBooleanExtra("IS_FAVORITE", false)
            updatedRecipeId?.let { recipeId ->
                viewModel.updateFavoriteState(recipeId, isFavorite)
            }

            // Ak bol recept upravený a uložený
            if (resultCode == RESULT_OK) {
                // Refreshni zoznam receptov (napr. zmena ingrediencií)
                viewModel.fetchData()
            }
        }
    }

    // Aktualizuje text, ktorý zobrazuje počet receptov: "filtrované / všetky".
    private fun updateRecipeCount(filteredCount: Int, totalCount: Int) {
        recipeCountTextView.text = "$filteredCount/$totalCount"
    }
}
