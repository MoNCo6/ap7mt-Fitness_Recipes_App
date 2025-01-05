package com.example.finalproject_fitrecipesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_fitrecipesapp.api.Category

// Adapter pre zobrazenie zoznamu kategórií v RecyclerView
class CategoryAdapter(
    private val onCategoryClick: (String) -> Unit // Callback pre kliknutie na kategóriu
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = mutableListOf<Category>() // Uchováva aktuálny zoznam kategórií

    // Aktualizuje zoznam kategórií a notifikuje RecyclerView o zmene dát
    fun updateCategories(newCategories: List<Category>) {
        categories.run {
            clear() // Vyčistí staré
            addAll(newCategories) // Pridá nové
        }
        notifyDataSetChanged() // Informuje RecyclerView o zmene dát
    }

    // Vytvára nové ViewHoldery
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false) // Inflatuje layout pre jednotlivé položky
        return CategoryViewHolder(view, onCategoryClick) // Vytvorí ViewHolder s callbackom
    }

    // Prepojí dáta s ViewHolderom
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position]) // Posiela kategóriu na pozícii do ViewHoldera
    }

    // Vráti počet kategórií
    override fun getItemCount(): Int = categories.size

    // ViewHolder pre zobrazenie jednej kategórie
    class CategoryViewHolder(
        itemView: View,
        private val onCategoryClick: (String) -> Unit // Callback pre kliknutie na kategóriu
    ) : RecyclerView.ViewHolder(itemView) {

        private val categoryName: TextView = itemView.findViewById(R.id.tv_category_name) // TextView pre názov kategórie

        // Prepojí dáta kategórie s pohľadom
        fun bind(category: Category) {
            categoryName.text = category.categoryName // Nastaví text názvu kategórie
            itemView.setOnClickListener { onCategoryClick(category.categoryId) } // Nastaví kliknutie na položku
        }
    }
}
