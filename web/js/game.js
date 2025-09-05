class GameOfLifeUI {
    constructor() {
        this.autoPlayInterval = null;
        this.currentSpeed = 1000;
        this.previousGrid = null;
        this.isLoading = false;
        
        this.initEventListeners();
        this.updateDisplay();
    }
    
    /**
     * Initialise les événements
     */
    initEventListeners() {
        // Raccourcis clavier
        document.addEventListener('keydown', (e) => {
            if (e.target.tagName === 'INPUT') return; // Ignorer si on tape dans un input
            
            switch(e.code) {
                case 'Space':
                    e.preventDefault();
                    this.nextGeneration();
                    break;
                case 'Enter':
                    e.preventDefault();
                    this.toggleAutoPlay();
                    break;
                case 'KeyR':
                    this.reset();
                    break;
                case 'KeyA':
                    this.randomize();
                    break;
                case 'KeyP':
                    this.setPattern();
                    break;
            }
        });
        
        // Contrôle de vitesse
        const speedSlider = document.getElementById('speedSlider');
        speedSlider.addEventListener('input', (e) => {
            this.currentSpeed = parseInt(e.target.value);
            if (this.autoPlayInterval) {
                this.stopAuto();
                this.startAuto();
            }
        });
        
        // Gestion des erreurs globales
        window.addEventListener('error', (e) => {
            console.error('Erreur JavaScript:', e.error);
            this.showNotification('Erreur inattendue', 'danger');
        });
    }
    
    /**
     * Récupère l'état du jeu depuis l'API
     */
    async fetchGameState() {
        try {
            const response = await fetch('/api/game/state');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error('Erreur lors du fetch:', error);
            this.showNotification('Erreur de connexion à l\'API', 'danger');
            return null;
        }
    }
    
    /**
     * Met à jour l'affichage de l'interface
     */
    async updateDisplay() {
        if (this.isLoading) return;
        
        const state = await this.fetchGameState();
        if (!state) return;
        
        // Mise à jour des statistiques
        this.updateStats(state);
        
        // Mise à jour de la grille avec animations
        this.updateGrid(state);
        
        // Sauvegarder l'état précédent pour les animations
        this.previousGrid = state.grid.map(row => [...row]);
    }
    
    /**
     * Met à jour les statistiques
     */
    updateStats(state) {
        document.getElementById('generation').textContent = state.generation;
        document.getElementById('liveCells').textContent = state.liveCells;
        
        const survivalRate = Math.round((state.liveCells / 25) * 100);
        document.getElementById('survivalRate').textContent = survivalRate + '%';
    }
    
    /**
     * Met à jour la grille avec animations
     */
    updateGrid(state) {
        const grid = document.getElementById('grid');
        
        // Détection des changements pour les animations
        const changes = this.detectChanges(state.grid);
        
        grid.innerHTML = '';
        
        for (let i = 0; i < 5; i++) {
            for (let j = 0; j < 5; j++) {
                const cell = this.createCell(i, j, state.grid[i][j], changes);
                grid.appendChild(cell);
            }
        }
    }
    
    /**
     * Détecte les changements entre l'état précédent et actuel
     */
    detectChanges(currentGrid) {
        const changes = [];
        if (this.previousGrid) {
            for (let i = 0; i < 5; i++) {
                for (let j = 0; j < 5; j++) {
                    if (this.previousGrid[i][j] !== currentGrid[i][j]) {
                        changes.push({
                            row: i, 
                            col: j, 
                            born: currentGrid[i][j]
                        });
                    }
                }
            }
        }
        return changes;
    }
    
    /**
     * Crée un élément cellule
     */
    createCell(row, col, isAlive, changes) {
        const cell = document.createElement('div');
        cell.className = 'cell ' + (isAlive ? 'alive' : 'dead');
        
        // Ajouter animation si changement
        const change = changes.find(c => c.row === row && c.col === col);
        if (change) {
            cell.classList.add(change.born ? 'cell-birth' : 'cell-death');
        }
        
        cell.onclick = () => this.toggleCell(row, col);
        cell.title = `Cellule (${row},${col}) - ${isAlive ? 'Vivante' : 'Morte'}`;
        
        return cell;
    }
    
    /**
     * Passe à la génération suivante
     */
    async nextGeneration() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        const btn = event?.target;
        
        if (btn) {
            btn.disabled = true;
            btn.classList.add('pulse', 'button-press');
        }
        
        try {
            const response = await fetch('/api/game/next', { method: 'POST' });
            if (!response.ok) {
                throw new Error('Erreur lors de la génération suivante');
            }
            
            await this.updateDisplay();
            this.showNotification('Génération calculée', 'success');
        } catch (error) {
            console.error('Erreur:', error);
            this.showNotification('Erreur lors du calcul', 'danger');
        } finally {
            this.isLoading = false;
            if (btn) {
                btn.disabled = false;
                btn.classList.remove('pulse', 'button-press');
            }
        }
    }
    
    /**
     * Remet à zéro le jeu
     */
    async reset() {
        if (this.isLoading) return;
        
        try {
            await fetch('/api/game/reset', { method: 'POST' });
            this.previousGrid = null;
            await this.updateDisplay();
            this.showNotification('Jeu remis à zéro', 'success');
        } catch (error) {
            console.error('Erreur:', error);
            this.showNotification('Erreur lors du reset', 'danger');
        }
    }
    
    /**
     * Génère une grille aléatoire
     */
    async randomize() {
        if (this.isLoading) return;
        
        try {
            await fetch('/api/game/random', { method: 'POST' });
            this.previousGrid = null;
            await this.updateDisplay();
            this.showNotification('Grille aléatoire générée', 'success');
        } catch (error) {
            console.error('Erreur:', error);
            this.showNotification('Erreur lors de la génération aléatoire', 'danger');
        }
    }
    
    /**
     * Définit le pattern initial
     */
    async setPattern() {
        if (this.isLoading) return;
        
        try {
            await fetch('/api/game/pattern', { method: 'POST' });
            this.previousGrid = null;
            await this.updateDisplay();
            this.showNotification('Pattern initial défini', 'success');
        } catch (error) {
            console.error('Erreur:', error);
            this.showNotification('Erreur lors de la définition du pattern', 'danger');
        }
    }
    
    /**
     * Bascule l'état d'une cellule
     */
    async toggleCell(row, col) {
        if (this.isLoading) return;
        
        try {
            const state = await this.fetchGameState();
            if (!state) return;
            
            const newState = !state.grid[row][col];
            
            const response = await fetch('/api/game/cell', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ row, col, alive: newState })
            });
            
            if (!response.ok) {
                throw new Error('Erreur lors de la modification de la cellule');
            }
            
            await this.updateDisplay();
        } catch (error) {
            console.error('Erreur:', error);
            this.showNotification('Erreur lors de la modification', 'danger');
        }
    }
    
    /**
     * Démarre la lecture automatique
     */
    startAuto() {
        if (this.autoPlayInterval) return;
        
        this.autoPlayInterval = setInterval(() => {
            this.nextGeneration();
        }, this.currentSpeed);
        
        const btn = document.getElementById('autoBtn');
        btn.innerHTML = '⏸️ Arrêter';
        btn.className = 'btn-danger';
        
        this.showNotification(`Lecture auto démarrée (${this.currentSpeed}ms)`, 'success');
    }
    
    /**
     * Arrête la lecture automatique
     */
    stopAuto() {
        if (this.autoPlayInterval) {
            clearInterval(this.autoPlayInterval);
            this.autoPlayInterval = null;
            
            const btn = document.getElementById('autoBtn');
            btn.innerHTML = '▶️ Lecture Auto';
            btn.className = 'btn-success';
            
            this.showNotification('Lecture auto arrêtée', 'warning');
        }
    }
    
    /**
     * Bascule la lecture automatique
     */
    toggleAutoPlay() {
        if (this.autoPlayInterval) {
            this.stopAuto();
        } else {
            this.startAuto();
        }
    }
    
    /**
     * Affiche une notification temporaire
     */
    showNotification(message, type = 'info') {
        // Créer la notification
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        
        // Styles inline pour la notification
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '12px 20px',
            borderRadius: '8px',
            color: 'white',
            fontWeight: '600',
            zIndex: '1000',
            opacity: '0',
            transform: 'translateX(100%)',
            transition: 'all 0.3s ease'
        });
        
        // Couleurs selon le type
        const colors = {
            success: '#10b981',
            danger: '#ef4444',
            warning: '#f59e0b',
            info: '#3b82f6'
        };
        
        notification.style.backgroundColor = colors[type] || colors.info;
        
        // Ajouter au DOM
        document.body.appendChild(notification);
        
        // Animation d'entrée
        requestAnimationFrame(() => {
            notification.style.opacity = '1';
            notification.style.transform = 'translateX(0)';
        });
        
        // Supprimer après 3 secondes
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }
}

// Fonctions globales pour les boutons (compatibilité)
let gameUI;

function nextGeneration() {
    gameUI.nextGeneration();
}

function reset() {
    gameUI.reset();
}

function randomize() {
    gameUI.randomize();
}

function setPattern() {
    gameUI.setPattern();
}

function toggleAutoPlay() {
    gameUI.toggleAutoPlay();
}

// Initialisation quand le DOM est chargé
document.addEventListener('DOMContentLoaded', () => {
    gameUI = new GameOfLifeUI();
});

// Fallback si DOMContentLoaded a déjà été déclenché
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        gameUI = new GameOfLifeUI();
    });
} else {
    gameUI = new GameOfLifeUI();
}
