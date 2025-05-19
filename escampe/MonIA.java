package escampe;

import java.util.*;

/**
 * Implémentation d'une IA simple pour le jeu Escampe
 */
public class MonIA implements IJoueur {
    private String maCouleur;
    private String couleurAdversaire;
    private String dernierCoup;
    private Random random;
    
    public MonIA() {
        random = new Random();
    }
    
    @Override
    public void initJoueur(String couleur) {
        maCouleur = couleur;
        couleurAdversaire = (couleur.equals("blanc")) ? "noir" : "blanc";
        System.out.println("Initialisation du joueur " + maCouleur);
    }
    
    @Override
    public void nomAdversaire(String nomAdversaire) {
        System.out.println("Adversaire: " + nomAdversaire);
    }
    
    @Override
    public String jouerCoup(String etatDuJeu) {
        System.out.println("État actuel: " + etatDuJeu);
        
        // Analyser l'état du jeu
        List<String> coupsPossibles = genererCoupsPossibles(etatDuJeu);
        
        if (coupsPossibles.isEmpty()) {
            System.out.println("Aucun coup possible !");
            return "";
        }
        
        // Pour cette IA simple, on choisit un coup aléatoire
        String coup = coupsPossibles.get(random.nextInt(coupsPossibles.size()));
        dernierCoup = coup;
        
        System.out.println("Je joue: " + coup);
        return coup;
    }
    
    @Override
    public void coupAdversaire(String coup) {
        System.out.println("L'adversaire a joué: " + coup);
    }
    
    @Override
    public void finPartie(String result) {
        System.out.println("Fin de partie: " + result);
    }
    
    /**
     * Génère la liste des coups possibles pour le joueur actuel
     */
    private List<String> genererCoupsPossibles(String etatDuJeu) {
        List<String> coups = new ArrayList<>();
        
        // Tableau pour la position des pièces
        Map<String, String> pieces = new HashMap<>();
        String[] elements = etatDuJeu.split("_");
        
        // Extraire les pièces et leurs positions
        for (int i = 0; i < elements.length; i += 2) {
            if (i + 1 < elements.length) {
                String piece = elements[i];
                String position = elements[i + 1];
                pieces.put(position, piece);
            }
        }
        
        // Pour chaque pièce de ma couleur
        for (Map.Entry<String, String> entry : pieces.entrySet()) {
            String position = entry.getKey();
            String piece = entry.getValue();
            
            // Vérifier si c'est ma pièce
            boolean maPiece = (maCouleur.equals("blanc") && piece.startsWith("B")) ||
                              (maCouleur.equals("noir") && piece.startsWith("N"));
            
            if (maPiece) {
                // Générer les déplacements possibles selon le type de pièce
                List<String> destinations = getDestinationsPossibles(position, piece, pieces);
                
                for (String destination : destinations) {
                    coups.add(position + "-" + destination);
                }
            }
        }
        
        return coups;
    }
    
    /**
     * Détermine les destinations possibles pour une pièce
     */
    private List<String> getDestinationsPossibles(String position, String piece, Map<String, String> pieces) {
        List<String> destinations = new ArrayList<>();
        char colonne = position.charAt(0);
        int ligne = Character.getNumericValue(position.charAt(1));
        boolean estRoi = piece.endsWith("R");
        
        // Directions possibles (simplifiées - à adapter selon les règles d'Escampe)
        int[][] directions;
        
        if (estRoi) {
            // Le Roi peut se déplacer dans toutes les directions
            directions = new int[][] {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // Orthogonales
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonales
            };
        } else {
            // Le Fou ne peut se déplacer qu'en diagonale
            directions = new int[][] {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
            };
        }
        
        // Explorer les directions
        for (int[] dir : directions) {
            int deltaCol = dir[0];
            int deltaLigne = dir[1];
            
            char nouvelleColonne = (char) (colonne + deltaCol);
            int nouvelleLigne = ligne + deltaLigne;
            
            // Vérifier si la nouvelle position est dans le plateau (a-f, 1-6)
            if (nouvelleColonne >= 'a' && nouvelleColonne <= 'f' && 
                nouvelleLigne >= 1 && nouvelleLigne <= 6) {
                
                String nouvellePosition = nouvelleColonne + "" + nouvelleLigne;
                
                // Vérifier si la case est occupée
                if (pieces.containsKey(nouvellePosition)) {
                    String pieceDestination = pieces.get(nouvellePosition);
                    boolean estPieceAdversaire = (maCouleur.equals("blanc") && pieceDestination.startsWith("N")) ||
                                               (maCouleur.equals("noir") && pieceDestination.startsWith("B"));
                    
                    // On peut capturer une pièce adverse
                    if (estPieceAdversaire) {
                        destinations.add(nouvellePosition);
                    }
                } else {
                    // Case vide, on peut s'y déplacer
                    destinations.add(nouvellePosition);
                }
            }
        }
        
        return destinations;
    }
}