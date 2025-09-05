package com.gamelife;

import java.io.IOException;

/**
 * Point d'entrée principal de l'application
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("    JEU DE LA VIE - CONWAY       ");
        System.out.println("=================================");
        
        String mode = "web"; // Mode par défaut
        
        // Vérifier les arguments de ligne de commande
        if (args.length > 0) {
            mode = args[0].toLowerCase();
        }
        
        switch (mode) {
            case "console":
            case "cli":
                runConsoleMode();
                break;
                
            case "web":
            case "server":
            default:
                runWebMode();
                break;
        }
    }
    
    /**
     * Lance le mode console interactif
     */
    private static void runConsoleMode() {
        System.out.println("Mode: Console Interactive");
        System.out.println();
        
        GameOfLifeConsole console = new GameOfLifeConsole();
        console.run();
    }
    
    /**
     * Lance le serveur web
     */
    private static void runWebMode() {
        System.out.println("Mode: Serveur Web");
        System.out.println();
        
        GameOfLifeWebServer server = new GameOfLifeWebServer();
        
        try {
            server.start();
            
            // En mode Docker, garder le serveur en vie indéfiniment
            String runtime = System.getProperty("java.runtime.name", "").toLowerCase();
            boolean isInDocker = System.getenv("JAVA_OPTS") != null || 
                               System.getenv("APP_MODE") != null ||
                               System.console() == null;
            
            if (isInDocker) {
                System.out.println("Mode Docker détecté - serveur en fonctionnement continu");
                // Attendre indéfiniment
                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } else {
                // Mode interactif local
                System.out.println("Appuyez sur Entrée pour arrêter le serveur...");
                System.in.read();
            }
            
            server.stop();
            System.out.println("Serveur arrêté.");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Serveur interrompu.");
            Thread.currentThread().interrupt();
        }
    }
}
