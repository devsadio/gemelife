# Makefile pour le Jeu de la Vie
.PHONY: help build run stop clean logs test compile console web

# Variables
APP_NAME = gamelife
DOCKER_COMPOSE = docker-compose
JAVA_SRC = src/com/gamelife

# Aide par défaut
help:
	@echo "=== JEU DE LA VIE - COMMANDES ==="
	@echo ""
	@echo "Compilation:"
	@echo "  make compile    - Compiler le code Java localement"
	@echo "  make test       - Tester la compilation"
	@echo ""
	@echo "Exécution locale:"
	@echo "  make console    - Lancer en mode console"
	@echo "  make web        - Lancer en mode web (localhost:8080)"
	@echo ""
	@echo "Docker:"
	@echo "  make build      - Construire les images Docker"
	@echo "  make run        - Démarrer l'application avec Docker"
	@echo "  make stop       - Arrêter l'application"
	@echo "  make restart    - Redémarrer l'application"
	@echo "  make logs       - Voir les logs"
	@echo ""
	@echo "Maintenance:"
	@echo "  make clean      - Nettoyer les conteneurs et images"
	@echo "  make status     - Statut des conteneurs"
	@echo ""

# Compilation Java locale
compile:
	@echo "Compilation du code Java..."
	@mkdir -p build
	javac -d build -cp $(JAVA_SRC) $(JAVA_SRC)/*.java
	@echo "✓ Compilation terminée"

# Test de compilation
test: compile
	@echo "Test de l'application..."
	cd build && echo "quit" | java com.gamelife.Main console
	@echo "✓ Test réussi"

# Exécution en mode console
console: compile
	@echo "Lancement en mode console..."
	cd build && java com.gamelife.Main console

# Exécution en mode web
web: compile
	@echo "Lancement en mode web sur http://localhost:8080"
	cd build && java com.gamelife.Main web

# Construction des images Docker
build:
	@echo "Construction des images Docker..."
	$(DOCKER_COMPOSE) build
	@echo "✓ Images construites"

# Démarrage de l'application
run:
	@echo "Démarrage de l'application..."
	$(DOCKER_COMPOSE) up -d
	@echo "✓ Application démarrée"
	@echo "Interface web: http://localhost"
	@echo "API directe: http://localhost:8080"

# Arrêt de l'application
stop:
	@echo "Arrêt de l'application..."
	$(DOCKER_COMPOSE) down
	@echo "✓ Application arrêtée"

# Redémarrage
restart: stop run

# Affichage des logs
logs:
	$(DOCKER_COMPOSE) logs -f

# Logs d'un service spécifique
logs-app:
	$(DOCKER_COMPOSE) logs -f gamelife-app

logs-nginx:
	$(DOCKER_COMPOSE) logs -f gamelife-nginx

# Statut des conteneurs
status:
	@echo "=== STATUT DES CONTENEURS ==="
	$(DOCKER_COMPOSE) ps

# Nettoyage
clean:
	@echo "Nettoyage des conteneurs et images..."
	$(DOCKER_COMPOSE) down --rmi all --volumes --remove-orphans
	docker system prune -f
	@echo "✓ Nettoyage terminé"

# Nettoyage local
clean-local:
	@echo "Nettoyage des fichiers compilés..."
	rm -rf build
	@echo "✓ Nettoyage local terminé"

# Shell dans le conteneur de l'application
shell-app:
	$(DOCKER_COMPOSE) exec gamelife-app /bin/bash

# Shell dans le conteneur nginx
shell-nginx:
	$(DOCKER_COMPOSE) exec gamelife-nginx /bin/sh

# Reconstruction et redémarrage
rebuild: clean build run

# Installation des dépendances de développement
dev-setup:
	@echo "Configuration de l'environnement de développement..."
	@echo "Vérification de Java..."
	java -version
	javac -version
	@echo "Vérification de Docker..."
	docker --version
	docker-compose --version
	@echo "✓ Environnement prêt"
