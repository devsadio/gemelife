package com.gamelife;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Serveur web simple pour exposer le Jeu de la Vie via API REST
 */
public class GameOfLifeWebServer {
    private GameOfLife game;
    private HttpServer server;
    private static final int PORT = 8080;
    
    public GameOfLifeWebServer() {
        this.game = new GameOfLife();
    }
    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Endpoints API
        server.createContext("/api/game/state", new GameStateHandler());
        server.createContext("/api/game/next", new NextGenerationHandler());
        server.createContext("/api/game/reset", new ResetHandler());
        server.createContext("/api/game/random", new RandomizeHandler());
        server.createContext("/api/game/pattern", new PatternHandler());
        server.createContext("/api/game/cell", new CellHandler());
        
        // Servir les fichiers statiques
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        
        System.out.println("Serveur web démarré sur http://localhost:" + PORT);
        System.out.println("API disponible sur http://localhost:" + PORT + "/api/");
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    /**
     * Handler pour obtenir l'état actuel du jeu
     */
    private class GameStateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                boolean[][] grid = game.getGridState();
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"generation\":").append(game.getGeneration()).append(",");
                json.append("\"liveCells\":").append(game.getLiveCellCount()).append(",");
                json.append("\"grid\":[");
                
                for (int i = 0; i < grid.length; i++) {
                    json.append("[");
                    for (int j = 0; j < grid[i].length; j++) {
                        json.append(grid[i][j]);
                        if (j < grid[i].length - 1) json.append(",");
                    }
                    json.append("]");
                    if (i < grid.length - 1) json.append(",");
                }
                
                json.append("]}");
                
                sendResponse(exchange, 200, json.toString(), "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour passer à la génération suivante
     */
    private class NextGenerationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.nextGeneration();
                sendResponse(exchange, 200, "{\"success\":true,\"generation\":" + game.getGeneration() + "}", "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour reset le jeu
     */
    private class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.reset();
                sendResponse(exchange, 200, "{\"success\":true}", "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour randomiser la grille
     */
    private class RandomizeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.randomizeGrid();
                sendResponse(exchange, 200, "{\"success\":true}", "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour définir le pattern initial
     */
    private class PatternHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.reset();
                game.setInitialPattern();
                sendResponse(exchange, 200, "{\"success\":true}", "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour modifier une cellule
     */
    private class CellHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Parse simple du body JSON (pour éviter les dépendances)
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // Format attendu: {"row":0,"col":1,"alive":true}
                
                try {
                    int row = parseIntFromJson(body, "row");
                    int col = parseIntFromJson(body, "col");
                    boolean alive = parseBooleanFromJson(body, "alive");
                    
                    game.setCellState(row, col, alive);
                    sendResponse(exchange, 200, "{\"success\":true}", "application/json");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}", "application/json");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }
    
    /**
     * Handler pour servir les fichiers statiques
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            // Contenu HTML simple inclus directement
            if ("/index.html".equals(path)) {
                String html = getIndexHtml();
                sendResponse(exchange, 200, html, "text/html");
            } else {
                sendResponse(exchange, 404, "Not Found", "text/plain");
            }
        }
    }
    
    /**
     * Envoie une réponse HTTP
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Parse simple d'un entier depuis JSON
     */
    private int parseIntFromJson(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }
    
    /**
     * Parse simple d'un booléen depuis JSON
     */
    private boolean parseBooleanFromJson(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Boolean.parseBoolean(json.substring(start, end).trim());
    }
    
    /**
     * Retourne le contenu HTML de l'interface web
     */
    private String getIndexHtml() {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Jeu de la Vie</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        max-width: 800px; 
                        margin: 0 auto; 
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background: white;
                        padding: 20px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 { 
                        text-align: center; 
                        color: #333; 
                        margin-bottom: 30px;
                    }
                    .grid { 
                        display: grid; 
                        grid-template-columns: repeat(5, 50px); 
                        gap: 2px; 
                        margin: 20px auto; 
                        justify-content: center;
                    }
                    .cell { 
                        width: 50px; 
                        height: 50px; 
                        border: 2px solid #ddd; 
                        cursor: pointer; 
                        transition: all 0.2s ease;
                        border-radius: 4px;
                    }
                    .alive { 
                        background-color: #2c3e50; 
                        border-color: #34495e;
                    }
                    .dead { 
                        background-color: #ecf0f1; 
                        border-color: #bdc3c7;
                    }
                    .cell:hover {
                        transform: scale(1.1);
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                    }
                    .controls { 
                        text-align: center;
                        margin: 30px 0; 
                    }
                    button { 
                        margin: 5px; 
                        padding: 12px 20px; 
                        font-size: 14px;
                        border: none;
                        border-radius: 6px;
                        cursor: pointer;
                        transition: background-color 0.2s ease;
                        font-weight: 500;
                    }
                    .btn-primary {
                        background-color: #3498db;
                        color: white;
                    }
                    .btn-primary:hover {
                        background-color: #2980b9;
                    }
                    .btn-secondary {
                        background-color: #95a5a6;
                        color: white;
                    }
                    .btn-secondary:hover {
                        background-color: #7f8c8d;
                    }
                    .btn-success {
                        background-color: #27ae60;
                        color: white;
                    }
                    .btn-success:hover {
                        background-color: #229954;
                    }
                    .btn-danger {
                        background-color: #e74c3c;
                        color: white;
                    }
                    .btn-danger:hover {
                        background-color: #c0392b;
                    }
                    .stats { 
                        margin: 20px 0; 
                        padding: 15px; 
                        background: #f8f9fa; 
                        border-radius: 6px;
                        border-left: 4px solid #3498db;
                    }
                    .stats div {
                        margin: 8px 0;
                        font-size: 16px;
                    }
                    .stats strong {
                        color: #2c3e50;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Jeu de la Vie de Conway - 5x5</h1>
                    
                    <div class="controls">
                        <button class="btn-primary" onclick="nextGeneration()">Generation Suivante</button>
                        <button class="btn-success" onclick="autoPlay()">Lecture Auto</button>
                        <button class="btn-danger" onclick="stopAuto()">Arreter</button>
                        <button class="btn-secondary" onclick="reset()">Reset</button>
                        <button class="btn-secondary" onclick="randomize()">Aleatoire</button>
                        <button class="btn-secondary" onclick="setPattern()">Pattern Initial</button>
                    </div>
                    
                    <div class="stats">
                        <div><strong>Generation:</strong> <span id="generation">0</span></div>
                        <div><strong>Cellules vivantes:</strong> <span id="liveCells">0</span>/25</div>
                        <div><strong>Taux de survie:</strong> <span id="survivalRate">0</span>%</div>
                    </div>
                    
                    <div class="grid" id="grid"></div>
                </div>
                
                <script>
                let autoPlayInterval = null;
                
                async function fetchGameState() {
                    const response = await fetch('/api/game/state');
                    return await response.json();
                }
                
                async function updateDisplay() {
                    const state = await fetchGameState();
                    document.getElementById('generation').textContent = state.generation;
                    document.getElementById('liveCells').textContent = state.liveCells;
                    
                    const survivalRate = Math.round((state.liveCells / 25) * 100);
                    document.getElementById('survivalRate').textContent = survivalRate;
                    
                    const grid = document.getElementById('grid');
                    grid.innerHTML = '';
                    
                    for (let i = 0; i < 5; i++) {
                        for (let j = 0; j < 5; j++) {
                            const cell = document.createElement('div');
                            cell.className = 'cell ' + (state.grid[i][j] ? 'alive' : 'dead');
                            cell.onclick = () => toggleCell(i, j);
                            cell.title = `Cellule (${i},${j})`;
                            grid.appendChild(cell);
                        }
                    }
                }
                
                async function nextGeneration() {
                    await fetch('/api/game/next', { method: 'POST' });
                    updateDisplay();
                }
                
                async function reset() {
                    await fetch('/api/game/reset', { method: 'POST' });
                    updateDisplay();
                }
                
                async function randomize() {
                    await fetch('/api/game/random', { method: 'POST' });
                    updateDisplay();
                }
                
                async function setPattern() {
                    await fetch('/api/game/pattern', { method: 'POST' });
                    updateDisplay();
                }
                
                async function toggleCell(row, col) {
                    const state = await fetchGameState();
                    const newState = !state.grid[row][col];
                    
                    await fetch('/api/game/cell', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ row, col, alive: newState })
                    });
                    
                    updateDisplay();
                }
                
                function autoPlay() {
                    if (autoPlayInterval) return;
                    autoPlayInterval = setInterval(nextGeneration, 1000);
                    
                    // Changer le style du bouton
                    const btn = event.target;
                    btn.textContent = 'En cours...';
                    btn.style.backgroundColor = '#f39c12';
                }
                
                function stopAuto() {
                    if (autoPlayInterval) {
                        clearInterval(autoPlayInterval);
                        autoPlayInterval = null;
                        
                        // Restaurer le bouton
                        const autoBtn = document.querySelector('.btn-success');
                        autoBtn.textContent = 'Lecture Auto';
                        autoBtn.style.backgroundColor = '#27ae60';
                    }
                }
                
                // Initialisation
                updateDisplay();
                
                // Mise à jour automatique toutes les 5 secondes si pas en auto-play
                setInterval(() => {
                    if (!autoPlayInterval) {
                        updateDisplay();
                    }
                }, 5000);
                </script>
            </body>
            </html>
            """;
    }
}
