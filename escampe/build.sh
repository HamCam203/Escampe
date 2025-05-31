#!/usr/bin/env bash
set -e

# 1. Variables
PROF_JAR="escampeobf.jar"
OUT_DIR="out"
JAR_NAME="SkitouCamara.jar"

# 2. Nettoyage & préparation
rm -rf "$OUT_DIR" "$JAR_NAME" mainClass
mkdir -p "$OUT_DIR"

# 3. Compilation de toutes les sources (prof + IA)
javac -cp "$PROF_JAR" -d "$OUT_DIR" *.java

# 4. Création du JAR final ne contenant que VOS classes IA
(
  cd "$OUT_DIR"
  jar cf "../$JAR_NAME" \
    escampe/MonJoueur*.class \
    escampe/Solo*.class
)

# 5. Génération du fichier mainClass pour eCampus
cat <<EOF > mainClass
jar: $JAR_NAME
clientClass: escampe.ClientJeu
mainClass: escampe.MonJoueur
EOF

echo "✔ JAR généré : $JAR_NAME"
echo "✔ Fichier mainClass créé"
