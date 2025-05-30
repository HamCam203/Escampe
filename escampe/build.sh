#!/usr/bin/env bash
set -e

# 1. Variables
PROF_JAR="escampeobf.jar"
OUT_DIR="out"
JAR_NAME="SkitouCamara.jar"

# 2. Nettoyage & préparation
rm -rf "$OUT_DIR" "$JAR_NAME" mainClass
mkdir -p "$OUT_DIR"

# 3. Compilation de **tous** les .java du répertoire courant
javac -cp "$PROF_JAR" -d "$OUT_DIR" *.java

# 4. Création du JAR avec tout ce qui a été compilé
#    (cela inclut à la fois le package escampe et d'éventuelles classes hors package)
jar cf "$JAR_NAME" -C "$OUT_DIR" .

# 5. Génération du fichier mainClass
cat <<EOF > mainClass
jar: $JAR_NAME
clientClass: escampe.ClientJeu
mainClass: escampe.MonJoueur
EOF

echo "✔ Jar généré : $JAR_NAME"
echo "✔ Fichier mainClass créé"
