# Utiliser OpenJDK 17 comme image de base
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers source
COPY src/ /app/src/

# Copier les fichiers web
COPY web/ /app/web/

# Créer le répertoire build
RUN mkdir -p /app/build

# Compiler l'application Java
RUN javac -d /app/build -cp /app/src/main/java /app/src/main/java/com/gamelife/*.java

# Créer un JAR exécutable
RUN cd /app/build && \
    echo "Main-Class: com.gamelife.Main" > manifest.txt && \
    jar cfm gamelife.jar manifest.txt com/gamelife/*.class

# Exposer le port 8080
EXPOSE 8080

# Variables d'environnement
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENV APP_MODE="web"

# Commande par défaut
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/build/gamelife.jar $APP_MODE"]

# Métadonnées
LABEL maintainer="Game of Life App"
LABEL version="1.0"
LABEL description="Conway's Game of Life implementation in Java"
