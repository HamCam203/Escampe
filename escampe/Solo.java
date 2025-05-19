package escampe;

import java.lang.reflect.*;

/**
 * Classe pour mettre en relation deux classes IJoueur
 * pour qu'elles jouent l'une contre l'autre.
 * Utile pour tester votre IA en conditions similaires au tournoi.
 */
public class Solo {
    private IJoueur joueurBlanc;
    private IJoueur joueurNoir;
    private String etatDuJeu;
    private static final int MAX_COUPS = 200; // Pour éviter les parties infinies
    
    /**
     * Initialise la partie avec les deux joueurs
     */
    public Solo(String classeJoueurBlanc, String classeJoueurNoir) throws Exception {
        // Charger les classes des joueurs dynamiquement
        Class<?> classeBlanc = Class.forName(classeJoueurBlanc);
        Class<?> classeNoir = Class.forName(classeJoueurNoir);
        
        Constructor<?> constructeurBlanc = classeBlanc.getConstructor();
        Constructor<?> constructeurNoir = classeNoir.getConstructor();
        
        joueurBlanc = (IJoueur) constructeurBlanc.newInstance();
        joueurNoir = (IJoueur) constructeurNoir.newInstance();
        
        // Initialiser les joueurs
        joueurBlanc.initJoueur("blanc");
        joueurNoir.initJoueur("noir");
        
        joueurBlanc.nomAdversaire(classeJoueurNoir);
        joueurNoir.nomAdversaire(classeJoueurBlanc);
        
        // État initial du jeu (format: pièce_position...)
        etatDuJeu = "NR_a1_NR_a3_NR_a5_NF_b1_NF_b3_NF_b5_BR_f2_BR_f4_BR_f6_BF_e2_BF_e4_BF_e6";
    }
    
    /**
     * Exécute une partie complète
     */
    public void jouerPartie() {
        int compteurCoups = 0;
        String coup;
        boolean jeuTermine = false;
        IJoueur joueurActuel = joueurBlanc; // Les blancs commencent
        
        System.out.println("=== Début de la partie ===");
        System.out.println("Position initiale: " + etatDuJeu);
        
        try {
            // Tant que la partie n'est pas terminée et qu'on n'a pas atteint le nombre max de coups
            while (!jeuTermine && compteurCoups < MAX_COUPS) {
                // Joueur actuel joue un coup
                coup = joueurActuel.jouerCoup(etatDuJeu);
                System.out.println((joueurActuel == joueurBlanc ? "Blanc" : "Noir") + " joue: " + coup);
                
                // Vérifier la validité du coup (à implémenter)
                if (!coupValide(coup)) {
                    System.out.println("Coup invalide! Partie terminée.");
                    joueurActuel.finPartie("perd");
                    (joueurActuel == joueurBlanc ? joueurNoir : joueurBlanc).finPartie("gagne");
                    return;
                }
                
                // Mettre à jour l'état du jeu
                etatDuJeu = appliquerCoup(etatDuJeu, coup);
                System.out.println("Nouvel état: " + etatDuJeu);
                
                // Envoyer le coup à l'adversaire
                if (joueurActuel == joueurBlanc) {
                    joueurNoir.coupAdversaire(coup);
                } else {
                    joueurBlanc.coupAdversaire(coup);
                }
                
                // Vérifier si la partie est terminée
                jeuTermine = partieTerminee();
                
                // Passer au joueur suivant
                joueurActuel = (joueurActuel == joueurBlanc) ? joueurNoir : joueurBlanc;
                compteurCoups++;
            }
            
            // Déterminer le résultat de la partie
            if (jeuTermine) {
                String vainqueur = determinerVainqueur();
                if (vainqueur.equals("blanc")) {
                    joueurBlanc.finPartie("gagne");
                    joueurNoir.finPartie("perd");
                    System.out.println("Les blancs ont gagné!");
                } else if (vainqueur.equals("noir")) {
                    joueurBlanc.finPartie("perd");
                    joueurNoir.finPartie("gagne");
                    System.out.println("Les noirs ont gagné!");
                } else {
                    joueurBlanc.finPartie("nul");
                    joueurNoir.finPartie("nul");
                    System.out.println("Match nul!");
                }
            } else {
                joueurBlanc.finPartie("nul");
                joueurNoir.finPartie("nul");
                System.out.println("Partie interrompue - nombre maximum de coups atteint");
            }
        } catch (Exception e) {
            System.err.println("Erreur pendant la partie: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si un coup est valide
     * (Implémentation simplifiée - à compléter)
     */
    private boolean coupValide(String coup) {
        // Vérification de base - à améliorer selon les règles d'Escampe
        return coup != null && !coup.isEmpty();
    }
    
    /**
     * Applique un coup à l'état du jeu
     * (Implémentation simplifiée - à compléter)
     */
    private String appliquerCoup(String etat, String coup) {
        // Implémentation simplifiée - à adapter selon les règles d'Escampe
        // Format de coup attendu: depart-arrivee
        String[] positions = coup.split("-");
        if (positions.length != 2) {
            return etat;
        }
        
        String depart = positions[0];
        String arrivee = positions[1];
        
        // Convertir l'état en tableau pour manipulation
        String[] elements = etat.split("_");
        StringBuilder nouvelEtat = new StringBuilder();
        
        boolean pieceDeplacee = false;
        
        // Parcourir les éléments (pièce_position) et modifier selon le coup
        for (int i = 0; i < elements.length; i += 2) {
            String piece = elements[i];
            String position = (i + 1 < elements.length) ? elements[i + 1] : "";
            
            if (position.equals(depart) && !pieceDeplacee) {
                // Déplacer la pièce à la nouvelle position
                if (nouvelEtat.length() > 0) {
                    nouvelEtat.append("_");
                }
                nouvelEtat.append(piece).append("_").append(arrivee);
                pieceDeplacee = true;
            } else if (!position.equals(arrivee)) {
                // Garder les autres pièces inchangées (sauf celle qui est "mangée")
                if (nouvelEtat.length() > 0) {
                    nouvelEtat.append("_");
                }
                nouvelEtat.append(piece).append("_").append(position);
            }
        }
        
        return nouvelEtat.toString();
    }
    
    /**
     * Vérifie si la partie est terminée
     * (Implémentation simplifiée - à compléter)
     */
    private boolean partieTerminee() {
        // Vérifier si un joueur a perdu toutes ses pièces
        return !etatDuJeu.contains("NR") || !etatDuJeu.contains("BR");
    }
    
    /**
     * Détermine le vainqueur de la partie
     * (Implémentation simplifiée - à compléter)
     */
    private String determinerVainqueur() {
        if (!etatDuJeu.contains("NR")) {
            return "blanc";
        } else if (!etatDuJeu.contains("BR")) {
            return "noir";
        } else {
            return "nul";
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java escampe.Solo <classeJoueurBlanc> <classeJoueurNoir>");
            System.out.println("Exemple: java escampe.Solo escampe.MonIA escampe.JoueurAleatoire");
            System.exit(1);
        }
        
        String classeJoueurBlanc = args[0];
        String classeJoueurNoir = args[1];
        
        try {
            Solo partie = new Solo(classeJoueurBlanc, classeJoueurNoir);
            partie.jouerPartie();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}