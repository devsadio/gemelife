package com.gamelife;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
            
            // Rediriger la racine vers index.html
            if ("/".equals(path)) {
                path = "/index.html";
            }
            
            // Servir les fichiers depuis le dossier web
            String filePath = "web" + path;
            Path file = Paths.get(filePath);
            
            if (Files.exists(file) && !Files.isDirectory(file)) {
                try {
                    byte[] content = Files.readAllBytes(file);
                    String contentType = getContentType(path);
                    
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, content.length);
                    
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(content);
                    }
                } catch (IOException e) {
                    sendResponse(exchange, 500, "Erreur serveur", "text/plain");
                }
            } else {
                // Fichier non trouvé, servir une page 404 simple
                String notFoundHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>404 - Page Non Trouvée</title>
                        <style>
                            body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                            h1 { color: #e74c3c; }
                            a { color: #3498db; text-decoration: none; }
                        </style>
                    </head>
                    <body>
                        <h1>404 - Page Non Trouvée</h1>
                        <p>Le fichier demandé n'existe pas.</p>
                        <a href="/">← Retour au Jeu de la Vie</a>
                    </body>
                    </html>
                    """;
                sendResponse(exchange, 404, notFoundHtml, "text/html");
            }
        }
        
        /**
         * Détermine le type MIME basé sur l'extension du fichier
         */
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (path.endsWith(".json")) return "application/json; charset=utf-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".gif")) return "image/gif";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
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
}
