const express = require('express');
const { ObjectId } = require('mongodb');
const { getDB } = require('../db/connection');
const router = express.Router();

// Vytvorenie receptu
router.post('/', async (req, res) => {
  const { name, ingredients, instructions, categoryId, nutrition } = req.body;

  // Overenie povinných polí
  if (!name || !ingredients || !instructions || !categoryId) {
    return res.status(400).json({ error: 'Missing required fields' });
  }

  try {
    const db = getDB();
    const result = await db.collection('recipes').insertOne(req.body); // Vloženie receptu do databázy
    console.log('Recipe created:', result);
    res.status(201).json(result); // Úspešná odpoveď
  } catch (error) {
    console.error('Error creating recipe:', error);
    res.status(500).json({ error: 'Failed to create recipe' });
  }
});

// Získanie všetkých receptov
router.get('/', async (req, res) => {
  try {
    const db = getDB();
    const recipes = await db.collection('recipes').find().toArray(); // Načítanie všetkých receptov
    console.log('Fetched recipes:', recipes);
    res.json(recipes); // Vrátenie zoznamu receptov
  } catch (error) {
    console.error('Error fetching recipes:', error);
    res.status(500).json({ error: 'Failed to fetch recipes' });
  }
});

// Získanie receptu podľa ID
router.get('/:id', async (req, res) => {
  try {
    const db = getDB();
    const recipe = await db.collection('recipes').findOne({ _id: new ObjectId(req.params.id) });
    if (!recipe) {
      return res.status(404).json({ error: 'Recipe not found' });
    }
    console.log('Fetched recipe:', recipe);
    res.json(recipe); // Vrátenie receptu
  } catch (error) {
    console.error('Error fetching recipe by ID:', error);
    res.status(500).json({ error: 'Failed to fetch recipe' });
  }
});

// Aktualizácia receptu
router.put('/:id', async (req, res) => {
  try {
    const { _id, ...updateData } = req.body; // Odstráň _id z aktualizačných dát

    // Overenie povinných polí
    if (!updateData.name || !updateData.ingredients || !updateData.instructions || !updateData.categoryId) {
      return res.status(400).json({ error: 'Missing required fields' });
    }

    const db = getDB();
    const result = await db.collection('recipes').updateOne(
      { _id: new ObjectId(req.params.id) },
      { $set: updateData } // Aktualizácia údajov v databáze
    );

    if (result.matchedCount === 0) {
      return res.status(404).json({ error: 'Recipe not found' });
    }

    res.json({ message: 'Recipe updated successfully' });
  } catch (error) {
    console.error('Error updating recipe:', error);
    res.status(500).json({ error: 'Failed to update recipe' });
  }
});

// Vymazanie receptu podľa ID
router.delete('/:id', async (req, res) => {
  try {
    const db = getDB();
    const result = await db.collection('recipes').deleteOne({ _id: new ObjectId(req.params.id) });
    if (result.deletedCount === 0) {
      return res.status(404).json({ error: 'Recipe not found' });
    }
    console.log('Recipe deleted:', result);
    res.json({ message: 'Recipe deleted successfully' });
  } catch (error) {
    console.error('Error deleting recipe:', error);
    res.status(500).json({ error: 'Failed to delete recipe' });
  }
});

// Vyhľadávanie receptov podľa názvu
router.get('/search', async (req, res) => {
  const { q } = req.query;

  // Overenie parametra vyhľadávania
  if (!q) {
    return res.status(400).json({ error: 'Search query parameter is required' });
  }

  try {
    const db = getDB();
    const recipes = await db.collection('recipes').find({ $text: { $search: q } }).toArray();
    console.log('Search query:', q, 'Results:', recipes);
    res.json(recipes); // Vrátenie výsledkov vyhľadávania
  } catch (error) {
    console.error('Error searching recipes:', error);
    res.status(500).json({ error: 'Failed to search recipes' });
  }
});

module.exports = router;