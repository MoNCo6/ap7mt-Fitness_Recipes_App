const express = require('express');
const { ObjectId } = require('mongodb');
const { getDB } = require('../db/connection');
const router = express.Router();

// Vytvorenie novej kategórie
router.post('/', async (req, res) => {
  try {
    const db = getDB();
    const result = await db.collection('categories').insertOne(req.body);

    // Úspešná odpoveď s detailmi o vytvorenej kategórii
    res.status(201).json(result);
  } catch (error) {
    res.status(500).json({ error: 'Failed to create category' });
  }
});

// Získanie všetkých kategórií
router.get('/', async (req, res) => {
  try {
    const db = getDB();
    const categories = await db.collection('categories').find().toArray();

    // Úspešná odpoveď so zoznamom kategórií
    res.json(categories);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch categories' });
  }
});

// Získanie kategórie podľa ID
router.get('/:id', async (req, res) => {
  try {
    const db = getDB();

    // Načítanie kategórie podľa ID z MongoDB
    const category = await db.collection('categories').findOne({ _id: new ObjectId(req.params.id) });

    // Ak kategória neexistuje, vráť chybu 404
    if (!category) {
      return res.status(404).json({ error: 'Category not found' });
    }

    // Úspešná odpoveď s detailmi o kategórii
    res.json(category);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch category' });
  }
});

// Aktualizácia kategórie podľa ID
router.put('/:id', async (req, res) => {
  try {
    const db = getDB();

    // Aktualizácia kategórie s dátami z req.body
    const result = await db.collection('categories').updateOne(
      { _id: new ObjectId(req.params.id) },
      { $set: req.body }
    );

    // Ak kategória neexistuje, vráť chybu 404
    if (result.matchedCount === 0) {
      return res.status(404).json({ error: 'Category not found' });
    }

    // Úspešná odpoveď
    res.json({ message: 'Category updated successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to update category' });
  }
});

// Odstránenie kategórie podľa ID
router.delete('/:id', async (req, res) => {
  try {
    const db = getDB();

    // Odstránenie kategórie podľa ID
    const result = await db.collection('categories').deleteOne({ _id: new ObjectId(req.params.id) });

    // Ak kategória neexistuje, vráť chybu 404
    if (result.deletedCount === 0) {
      return res.status(404).json({ error: 'Category not found' });
    }

    // Úspešná odpoveď
    res.json({ message: 'Category deleted successfully' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to delete category' });
  }
});

module.exports = router;