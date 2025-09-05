# 🎮 Jeu de la Vie de Conway - Java

Le Jeu de la Vie est un automate cellulaire où l'évolution des cellules suit des règles simples :

- **Survie** : Une cellule vivante avec 2 ou 3 voisins vivants reste vivante
- **Naissance** : Une cellule morte avec exactement 3 voisins vivants devient vivante  
- **Mort** : Toutes les autres cellules meurent

Cette implémentation utilise une grille 5x5 et propose deux modes d'interaction.

## 🚀 Démarrage Rapide

### Avec Docker (Recommandé)

```bash
# Cloner et accéder au projet
cd gamelife

# Démarrer l'application web
docker-compose up -d

# Accéder à l'interface web
# http://localhost:8090 (via Nginx)
# http://localhost:8080 (direct Java)

# OU démarrer en mode console interactif
docker-compose down
docker run -it gamelife-gamelife-app console
```

### Sans Docker

```bash
# Compiler
make compile

# Mode console interactif
make console

# Mode serveur web
make web
```

## 🏗️ Architecture

```
gamelife/
├── src/main/java/com/gamelife/     # Code source Java
│   ├── Cell.java                   # Représentation d'une cellule
│   ├── GameOfLife.java            # Logique du jeu
│   ├── GameOfLifeConsole.java     # Interface console
│   ├── GameOfLifeWebServer.java   # Serveur web + API REST
│   └── Main.java                  # Point d'entrée
├── web/                           # Interface utilisateur web
│   ├── index.html                 # Page principale
│   ├── css/
│   │   └── styles.css             # Styles CSS
│   └── js/
│       └── game.js                # Logique JavaScript
├── nginx/
│   └── nginx.conf                 # Configuration Nginx
├── Dockerfile                     # Image Java
├── docker-compose.yml            # Orchestration
├── Makefile                       # Commandes utiles
└── README.md
```

## 🎯 Fonctionnalités

### Mode Console
- Interface interactive en ligne de commande
- Affichage ASCII de la grille (█ = vivant, ░ = mort)
- Contrôles manuels (génération par génération)
- Mode automatique avec évolution continue
- Modification manuelle des cellules
- Statistiques en temps réel
- Patterns prédéfinis (Blinker)

### Mode Web
- Interface graphique intuitive
- API REST complète
- Contrôle en temps réel via navigateur
- Visualisation interactive de la grille
- Modification des cellules par clic

### API REST

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/game/state` | GET | État actuel du jeu |
| `/api/game/next` | POST | Génération suivante |
| `/api/game/reset` | POST | Remise à zéro |
| `/api/game/random` | POST | Grille aléatoire |
| `/api/game/pattern` | POST | Pattern initial |
| `/api/game/cell` | POST | Modifier une cellule |

## 🔧 Commandes Utiles

```bash
# Développement
make help           # Afficher l'aide
make compile        # Compiler le code
make test          # Tester la compilation
make console       # Mode console
make web           # Mode web

# Docker
make build         # Construire les images
make run           # Démarrer l'application
make stop          # Arrêter l'application
make restart       # Redémarrer
make logs          # Voir les logs
make status        # Statut des conteneurs

# Maintenance
make clean         # Nettoyer Docker
make clean-local   # Nettoyer les fichiers locaux
make rebuild       # Reconstruction complète
```

## 🐳 Services Docker

- **gamelife-app** : Application Java (port 8080)
- **gamelife-nginx** : Reverse proxy Nginx (port 8090)
- **gamelife-monitor** : Surveillance de santé

## 🎮 Utilisation

### Interface Console

```bash
# Mode local
make console

# Mode Docker (console interactive)
docker run -it gamelife-gamelife-app console
```

Commandes disponibles :
- `1` ou `afficher` : Afficher la grille
- `2` ou `suivant` : Génération suivante
- `3` ou `auto` : Mode automatique
- `4` ou `pattern` : Pattern initial
- `5` ou `aleatoire` : Grille aléatoire
- `6` ou `modifier` : Modifier une cellule
- `7` ou `reset` : Remise à zéro
- `8` ou `stats` : Statistiques
- `0` ou `quitter` : Quitter

### Interface Web

1. Démarrer l'application : `make run`
2. Ouvrir http://localhost:8090 (ou http://localhost:8080 pour l'accès direct)
3. Utiliser les boutons pour contrôler le jeu
4. Cliquer sur les cellules pour les modifier

## 📊 Patterns Intéressants

Le jeu inclut un pattern initial "Blinker" qui oscille. Vous pouvez aussi :
- Générer des grilles aléatoires
- Créer vos propres patterns manuellement
- Observer l'évolution des populations

## 🔍 Monitoring

Le système inclut :
- Health checks automatiques
- Logs Nginx dans des volumes Docker
- Service de monitoring des conteneurs
- Endpoints de santé (`/health`)

## 🛠️ Développement

### Prérequis
- Java 17+
- Docker & Docker Compose
- Make (optionnel)

### Structure du Code
- **Cell** : Représente l'état d'une cellule (vivante/morte)
- **GameOfLife** : Implémente les règles et la logique de transition
- **GameOfLifeConsole** : Interface utilisateur console
- **GameOfLifeWebServer** : Serveur HTTP avec API REST
- **Main** : Point d'entrée avec sélection du mode

### Tests Manuels

```bash
# Test compilation
make test

# Test console
make console

# Test web
make web
# Puis ouvrir http://localhost:8080

# Test Docker
make run
# Puis ouvrir http://localhost:8090
```

## 📝 Notes Techniques

- Grille fixe 5x5 (configurable dans le code)
- Limites de grille (pas de wrap-around)
- Implémentation efficace du calcul des voisins
- Serveur HTTP natif Java (pas de dépendances externes)
- Configuration Nginx optimisée pour SPA

## 🚀 Déploiement

L'application est prête pour le déploiement avec Docker Compose. Pour la production :

1. Configurer les variables d'environnement
2. Ajuster les limites de ressources
3. Mettre en place le monitoring
4. Configurer HTTPS dans Nginx

