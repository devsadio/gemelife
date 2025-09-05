package com.gamelife;

/**
 * Représente une cellule dans le Jeu de la Vie
 */
public class Cell {
    private boolean alive;
    
    public Cell(boolean alive) {
        this.alive = alive;
    }
    
    public Cell() {
        this.alive = false;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public void kill() {
        this.alive = false;
    }
    
    public void revive() {
        this.alive = true;
    }
    
    @Override
    public String toString() {
        return alive ? "█" : "░";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cell cell = (Cell) obj;
        return alive == cell.alive;
    }
    
    @Override
    public int hashCode() {
        return Boolean.hashCode(alive);
    }
}
