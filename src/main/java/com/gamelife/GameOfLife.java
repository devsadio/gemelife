package com.gamelife;

import java.util.Random;

/**
 * Implémentation du Jeu de la Vie de Conway
 * Grille 5x5 comme spécifié dans les exigences
 */
public class GameOfLife {
    private static final int GRID_SIZE = 5;
    private Cell[][] grid;
    private int generation;
    
    public GameOfLife() {
        this.grid = new Cell[GRID_SIZE][GRID_SIZE];
        this.generation = 0;
        initializeGrid();
    }
    
    /**
     * Initialise la grille avec des cellules mortes
     */
    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell(false);
            }
        }
    }
    
    /**
     * Initialise la grille avec un pattern aléatoire
     */
    public void randomizeGrid() {
        Random random = new Random();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j].setAlive(random.nextBoolean());
            }
        }
    }
    
    /**
     * Définit un pattern initial prédéfini (ex: glider pattern adapté à 5x5)
     */
    public void setInitialPattern() {
        // Pattern "Blinker" adapté
        grid[2][1].setAlive(true);
        grid[2][2].setAlive(true);
        grid[2][3].setAlive(true);
        
        // Quelques cellules additionnelles pour rendre intéressant
        grid[1][2].setAlive(true);
        grid[3][2].setAlive(true);
    }
    
    /**
     * Compte le nombre de voisins vivants pour une cellule donnée
     */
    private int countLiveNeighbors(int row, int col) {
        int count = 0;
        
        // Vérifier les 8 directions autour de la cellule
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip la cellule elle-même
                
                int newRow = row + i;
                int newCol = col + j;
                
                // Vérifier les limites de la grille
                if (newRow >= 0 && newRow < GRID_SIZE && 
                    newCol >= 0 && newCol < GRID_SIZE) {
                    if (grid[newRow][newCol].isAlive()) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * Calcule la prochaine génération selon les règles du Jeu de la Vie
     */
    public void nextGeneration() {
        Cell[][] newGrid = new Cell[GRID_SIZE][GRID_SIZE];
        
        // Créer la nouvelle grille basée sur les règles
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int liveNeighbors = countLiveNeighbors(i, j);
                boolean currentlyAlive = grid[i][j].isAlive();
                
                newGrid[i][j] = new Cell();
                
                // Appliquer les règles du Jeu de la Vie
                if (currentlyAlive) {
                    // Cellule vivante : survit avec 2 ou 3 voisins
                    if (liveNeighbors == 2 || liveNeighbors == 3) {
                        newGrid[i][j].setAlive(true);
                    }
                } else {
                    // Cellule morte : naît avec exactement 3 voisins
                    if (liveNeighbors == 3) {
                        newGrid[i][j].setAlive(true);
                    }
                }
            }
        }
        
        // Remplacer l'ancienne grille par la nouvelle
        this.grid = newGrid;
        this.generation++;
    }
    
    /**
     * Affiche la grille actuelle en console
     */
    public void display() {
        System.out.println("=== Generation " + generation + " ===");
        System.out.println("  0 1 2 3 4");
        
        for (int i = 0; i < GRID_SIZE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    /**
     * Vérifie si la grille est vide (toutes cellules mortes)
     */
    public boolean isEmpty() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j].isAlive()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Compte le nombre total de cellules vivantes
     */
    public int getLiveCellCount() {
        int count = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j].isAlive()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Retourne la génération actuelle
     */
    public int getGeneration() {
        return generation;
    }
    
    /**
     * Retourne une copie de la grille pour l'API web
     */
    public boolean[][] getGridState() {
        boolean[][] state = new boolean[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                state[i][j] = grid[i][j].isAlive();
            }
        }
        return state;
    }
    
    /**
     * Définit l'état d'une cellule spécifique
     */
    public void setCellState(int row, int col, boolean alive) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            grid[row][col].setAlive(alive);
        }
    }
    
    /**
     * Remet à zéro le jeu
     */
    public void reset() {
        this.generation = 0;
        initializeGrid();
    }
}
