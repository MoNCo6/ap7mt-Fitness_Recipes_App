package com.example.finalproject_fitrecipesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_fitrecipesapp.api.Recipe

// Adapter pre zobrazenie zoznamu receptov v RecyclerView
class RecipeAdapter(
    private val onRecipeClick: (String) -> Unit, // Callback pre kliknutie na recept
    private val onFavoriteClick: (Recipe) -> Unit // Callback pre kliknutie na ikonu obľúbených
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val recipes = mutableListOf<Recipe>() // Uchováva aktuálny zoznam receptov

    // Aktualizuje zoznam receptov a notifikuje RecyclerView o zmene dát
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes.run {
            clear() // Vyčistí staré
            addAll(newRecipes) // Pridá nové
        }
        notifyDataSetChanged() // Informuje RecyclerView o zmene dát
    }

    // Vytvára nové ViewHoldery
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipes, parent, false) // Inflatuje layout pre položky receptov
        return RecipeViewHolder(view, onRecipeClick, onFavoriteClick) // Vytvorí ViewHolder
    }

    // Prepojí dáta s ViewHolderom
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position]) // Posiela recept na pozícii do ViewHoldera
    }

    // Vráti počet receptov
    override fun getItemCount(): Int = recipes.size

    // ViewHolder pre zobrazenie jedného receptu
    class RecipeViewHolder(
        itemView: View,
        private val onRecipeClick: (String) -> Unit, // Callback pre kliknutie na recept
        private val onFavoriteClick: (Recipe) -> Unit // Callback pre kliknutie na ikonu obľúbených
    ) : RecyclerView.ViewHolder(itemView) {

        private val recipeName: TextView = itemView.findViewById(R.id.tv_recipe_name) // TextView pre názov receptu
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.img_favorite_icon) // Ikona obľúbených

        // Prepojí dáta receptu s pohľadom
        fun bind(recipe: Recipe) {
            recipeName.text = recipe.name // Nastaví názov receptu

            // Nastaví stav ikony obľúbených podľa receptu
            favoriteIcon.setImageResource(
                if (recipe.isFavorite) R.drawable.fav_fill else R.drawable.fav_unfill
            )

            // Nastaví kliknutie na celý item pre otvorenie detailu receptu
            itemView.setOnClickListener {
                recipe._id?.let { onRecipeClick(it) }
            }

            // Nastaví kliknutie na ikonu obľúbených pre zmenu stavu
            favoriteIcon.setOnClickListener {
                onFavoriteClick(recipe)
            }
        }
    }
}
