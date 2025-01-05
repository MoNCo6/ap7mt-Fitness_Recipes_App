const express = require('express');
const { connectToServer } = require('./db/connection');
const recipeRoutes = require('./routes/recipes');
const categoryRoutes = require('./routes/categories');

const app = express();
const PORT = 3000;

// Middleware pre parsovanie JSON požiadaviek
app.use(express.json());

// Registrácia API endpoints
app.use('/api/recipes', recipeRoutes);
app.use('/api/categories', categoryRoutes);

// Hlavná (root) cesta pre overenie, či server beží
app.get('/', (req, res) => {
  res.send('Server je spustený a pripravený na prijímanie požiadaviek!');
});

// Spustenie servera a pripojenie k databáze
app.listen(PORT, '0.0.0.0', async () => {
  try {
    // Pokus o pripojenie k databáze
    await connectToServer();
    console.log(`Server beží na adrese http://0.0.0.0:${PORT}`);
  } catch (error) {
    console.error('Chyba pri spustení servera:', error);
  }
});
