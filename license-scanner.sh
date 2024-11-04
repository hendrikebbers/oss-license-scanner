#!/bin/bash

# Prüfen, ob eine .env-Datei existiert
if [ -f .env ]; then
    # Alle Variablen aus der .env-Datei laden
    export $(grep -v '^#' .env | xargs)
fi

./mvnw verify
java -jar target/license-scanner.jar "$@"