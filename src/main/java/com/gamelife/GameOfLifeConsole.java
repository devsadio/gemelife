package com.gamelife;

import java.util.Scanner;

/**
 * Classe principale pour exécuter le Jeu de la Vie en mode console
 */
public class GameOfLifeConsole {
    private GameOfLife game;
    private Scanner scanner;
    
    public GameOfLifeConsole() {
        this.game = new GameOfLife();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Lance l'interface console interactive
     */
    public void run() {
        System.out.println("=================================");
        System.out.println("   JEU DE LA VIE - CONSOLE       ");
        System.out.println("=================================");
        System.out.println();
        
        showMenu();
        
        while (true) {
            System.out.print("Votre choix: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice.toLowerCase()) {
                case "1":
                case "afficher":
                    game.display();
                    break;
                    
                case "2":
                case "suivant":
                    game.nextGeneration();
                    System.out.println("Generation suivante calculee !");
                    game.display();
                    break;
                    
                case "3":
                case "auto":
                    runAutoMode();
                    break;
                    
                case "4":
                case "pattern":
                    game.reset();
                    game.setInitialPattern();
                    System.out.println("Pattern initial defini !");
                    game.display();
                    break;
                    
                case "5":
                case "aleatoire":
                    game.reset();
                    game.randomizeGrid();
                    System.out.println("Grille randomisee !");
                    game.display();
                    break;
                    
                case "6":
                case "modifier":
                    modifyCell();
                    break;
                    
                case "7":
                case "reset":
                    game.reset();
                    System.out.println("Jeu remis a zero !");
                    game.display();
                    break;
                    
                case "8":
                case "stats":
                    showStats();
                    break;
                    
                case "9":
                case "aide":
                    showMenu();
                    break;
                    
                case "0":
                case "quitter":
                case "exit":
                    System.out.println("Au revoir !");
                    return;
                    
                default:
                    System.out.println("Choix invalide. Tapez '9' pour l'aide.");
            }
            
            System.out.println();
        }
    }
    
    /**
     * Affiche le menu des options
     */
    private void showMenu() {
        System.out.println("Options disponibles:");
        System.out.println("1. Afficher la grille");
        System.out.println("2. Generation suivante");
        System.out.println("3. Mode automatique");
        System.out.println("4. Pattern initial");
        System.out.println("5. Grille aleatoire");
        System.out.println("6. Modifier une cellule");
        System.out.println("7. Reset");
        System.out.println("8. Statistiques");
        System.out.println("9. Aide");
        System.out.println("0. Quitter");
        System.out.println();
    }
    
    /**
     * Mode automatique - évolution continue
     */
    private void runAutoMode() {
        System.out.print("Nombre de generations a simuler (0 pour infini): ");
        try {
            int generations = Integer.parseInt(scanner.nextLine().trim());
            
            if (generations == 0) {
                System.out.println("Mode automatique infini. Appuyez sur Entree pour arreter...");
                // TODO: Implementer l'arret par input en mode non-bloquant
                for (int i = 0; i < 50; i++) { // Limite a 50 pour la demo
                    game.nextGeneration();
                    game.display();
                    
                    if (game.isEmpty()) {
                        System.out.println("Toutes les cellules sont mortes. Arret automatique.");
                        break;
                    }
                    
                    try {
                        Thread.sleep(1000); // Pause d'1 seconde
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } else {
                for (int i = 0; i < generations; i++) {
                    game.nextGeneration();
                    game.display();
                    
                    if (game.isEmpty()) {
                        System.out.println("Toutes les cellules sont mortes a la generation " + game.getGeneration());
                        break;
                    }
                    
                    try {
                        Thread.sleep(500); // Pause d'0.5 seconde
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Nombre invalide.");
        }
    }
    
    /**
     * Permet de modifier l'état d'une cellule
     */
    private void modifyCell() {
        game.display();
        
        try {
            System.out.print("Ligne (0-4): ");
            int row = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Colonne (0-4): ");
            int col = Integer.parseInt(scanner.nextLine().trim());
            
            if (row < 0 || row >= 5 || col < 0 || col >= 5) {
                System.out.println("Coordonnees invalides. Utilisez 0-4.");
                return;
            }
            
            System.out.print("Nouvelle etat (vivant/mort ou true/false): ");
            String state = scanner.nextLine().trim().toLowerCase();
            
            boolean alive;
            if (state.equals("vivant") || state.equals("true") || state.equals("1")) {
                alive = true;
            } else if (state.equals("mort") || state.equals("false") || state.equals("0")) {
                alive = false;
            } else {
                System.out.println("Etat invalide.");
                return;
            }
            
            game.setCellState(row, col, alive);
            System.out.println("Cellule modifiee !");
            game.display();
            
        } catch (NumberFormatException e) {
            System.out.println("Coordonnees invalides.");
        }
    }
    
    /**
     * Affiche les statistiques du jeu
     */
    private void showStats() {
        System.out.println("=== STATISTIQUES ===");
        System.out.println("Generation actuelle: " + game.getGeneration());
        System.out.println("Cellules vivantes: " + game.getLiveCellCount() + "/25");
        System.out.println("Taux de survie: " + String.format("%.1f", (game.getLiveCellCount() / 25.0) * 100) + "%");
        
        if (game.isEmpty()) {
            System.out.println("Etat: Extinction complete");
        } else if (game.getLiveCellCount() == 25) {
            System.out.println("Etat: Population maximale");
        } else {
            System.out.println("Etat: Population active");
        }
    }
}
