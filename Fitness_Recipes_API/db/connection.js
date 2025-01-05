const { MongoClient } = require('mongodb');

let db; // Premenná na uloženie pripojenia k databáze

// Pripojenie k MongoDB serveru a výber databázy
async function connectToServer() {
  try {
    const client = new MongoClient('mongodb://127.0.0.1:27017'); // Pripojenie na lokálny MongoDB server
    await client.connect();
    db = client.db('FitnessRecipes'); // Výber databázy FitnessRecipes
    console.log('Connected to MongoDB'); // Informácia o úspešnom pripojení
  } catch (error) {
    console.error('Failed to connect to MongoDB:', error); // Chyba pripojenia
    throw error;
  }
}

// Funkcia na získanie pripojenia k databáze
function getDB() {
  if (!db) {
    throw new Error('Database connection not established'); // Chyba, ak nie je pripojenie
  }
  return db; // Vráti pripojenú databázu
}

// Export funkcií na použitie v aplikácii
module.exports = { connectToServer, getDB };
