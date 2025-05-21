package escampe;

import java.util.*;

/**
 * Classe qui gère l'état du jeu Escampe
 * Contient la représentation du plateau, les règles du jeu, et les fonctions pour vérifier la légalité des coups
 */
public class EtatJeu {
    // Constantes pour les types de pièces
    public static final int BLANC = IJoueur.BLANC;
    public static final int NOIR = IJoueur.NOIR;
    public static final int VIDE = 0;
    
    // Constantes pour distinguer licorne et paladin
    public static final int LICORNE = 2;
    public static final int PALADIN = 1;
    
    // Matrice des liserés du plateau
    public static final int[][] LISERE = {
        {1,2,2,3,1,2},
        {3,1,3,1,3,2},
        {2,3,1,2,1,3},
        {2,1,3,2,3,1},
        {1,3,1,3,1,2},
        {3,2,2,1,3,2}
    };
    
    // Valeurs stratégiques des positions sur le plateau
    public static final int[][] VALEUR_POSITION = {
        {3, 4, 4, 5, 3, 4}, // Ligne 0
        {5, 3, 5, 3, 5, 4}, // Ligne 1
        {4, 5, 3, 4, 3, 5}, // Ligne 2
        {4, 3, 5, 4, 5, 3}, // Ligne 3
        {3, 5, 3, 5, 3, 4}, // Ligne 4
        {5, 4, 4, 3, 5, 4}  // Ligne 5
    };
    
    // État du jeu
    private int[][] plateau;
    private int[][] typePiece;
    private int lastLisere;
    private int couleurJoueur;
    private int[] licorneNoire = new int[2]; // [row, col]
    private int[] licorneBlanche = new int[2]; // [row, col]
    private boolean initialDone;
    
    // Pour le débogage
    private static final boolean DEBUG = true;
    
    /**
     * Constructeur
     */
    public EtatJeu(int couleurJoueur) {
        this.couleurJoueur = couleurJoueur;
        this.plateau = new int[6][6];
        this.typePiece = new int[6][6];
        this.lastLisere = 0;
        this.initialDone = false;
        
        // Initialiser le plateau
        for (int i = 0; i < 6; i++) {
            Arrays.fill(plateau[i], VIDE);
            Arrays.fill(typePiece[i], 0);
        }
    }
    
    /**
     * Réinitialise l'état du jeu
     */
    public void reinitialiser() {
        this.lastLisere = 0;
        this.initialDone = false;
        
        for (int i = 0; i < 6; i++) {
            Arrays.fill(plateau[i], VIDE);
            Arrays.fill(typePiece[i], 0);
        }
    }
    
    /**
     * Applique le placement initial des pièces
     */
    public void appliquerPlacementInitial(String placement) {
        String[] positions = placement.split("/");
        
        // La première position est toujours la licorne
        String licornePos = positions[0];
        int lc = licornePos.charAt(0) - 'A';
        int lr = Integer.parseInt(licornePos.substring(1)) - 1;
        plateau[lr][lc] = couleurJoueur;
        typePiece[lr][lc] = LICORNE;
        
        // Mémoriser la position de notre licorne
        if (couleurJoueur == NOIR) {
            licorneNoire[0] = lr;
            licorneNoire[1] = lc;
        } else {
            licorneBlanche[0] = lr;
            licorneBlanche[1] = lc;
        }
        
        // Les autres positions sont les paladins
        for (int i = 1; i < positions.length; i++) {
            String pos = positions[i];
            int c = pos.charAt(0) - 'A';
            int r = Integer.parseInt(pos.substring(1)) - 1;
            plateau[r][c] = couleurJoueur;
            typePiece[r][c] = PALADIN;
        }
        
        initialDone = true;
        
        if (DEBUG) {
            System.out.println("Placement initial: " + placement);
            afficherPlateau();
        }
    }
    
    /**
     * Applique un coup sur le plateau et met à jour les informations nécessaires
     */
    public void appliquerCoup(String move) {
        String[] p = move.split("-");
        int fr = Integer.parseInt(p[0].substring(1)) - 1;
        int fc = p[0].charAt(0) - 'A';
        int sr = Integer.parseInt(p[1].substring(1)) - 1;
        int sc = p[1].charAt(0) - 'A';
        
        // Mettre à jour le type de pièce
        typePiece[sr][sc] = typePiece[fr][fc];
        typePiece[fr][fc] = 0;
        
        // Si c'est une licorne, mettre à jour sa position
        if (typePiece[sr][sc] == LICORNE) {
            if (plateau[fr][fc] == NOIR) {
                licorneNoire[0] = sr;
                licorneNoire[1] = sc;
            } else {
                licorneBlanche[0] = sr;
                licorneBlanche[1] = sc;
            }
        }
        
        // Déplacer la pièce
        plateau[sr][sc] = plateau[fr][fc];
        plateau[fr][fc] = VIDE;
        
        // Mettre à jour le liseré contraint
        lastLisere = LISERE[sr][sc];
        
        if (DEBUG) {
            System.out.println("Coup appliqué: " + move);
            System.out.println("Nouveau liseré contraint: " + lastLisere);
            afficherPlateau();
        }
    }
    
    /**
     * Applique un coup de l'adversaire
     */
    public void appliquerCoupAdversaire(String coup) {
        if (DEBUG) {
            System.out.println("Mouvement ennemi reçu: " + coup);
            System.out.println("État du plateau avant application:");
            afficherPlateau();
        }
        
        // Traiter le cas spécial où l'adversaire entre "E" (passer son tour)
        if (coup.equals("E") || coup.equals("PASSE")) {
            if (DEBUG) System.out.println("L'adversaire passe son tour, lisère réinitialisé à 0");
            lastLisere = 0;
            return;
        }
        
        if (coup.contains("/")) {
            int advCol = -couleurJoueur;
            String[] positions = coup.split("/");
            
            // La première position est toujours la licorne
            String licornePos = positions[0];
            int lc = licornePos.charAt(0) - 'A';
            int lr = Integer.parseInt(licornePos.substring(1)) - 1;
            plateau[lr][lc] = advCol;
            typePiece[lr][lc] = LICORNE;
            
            // Mémoriser la position de la licorne adverse
            if (advCol == NOIR) {
                licorneNoire[0] = lr;
                licorneNoire[1] = lc;
            } else {
                licorneBlanche[0] = lr;
                licorneBlanche[1] = lc;
            }
            
            // Les autres positions sont les paladins
            for (int i = 1; i < positions.length; i++) {
                String pos = positions[i];
                int c = pos.charAt(0) - 'A';
                int r = Integer.parseInt(pos.substring(1)) - 1;
                plateau[r][c] = advCol;
                typePiece[r][c] = PALADIN;
            }
            
            if (DEBUG) {
                System.out.println("Placement initial de l'adversaire appliqué");
                afficherPlateau();
            }
            return;
        }
        
        // Vérifier si le coup est au format standard "A1-B2"
        if (!coup.contains("-") || coup.length() != 5) {
            if (DEBUG) System.out.println("Format de coup non standard: " + coup + ", interprété comme PASSE");
            lastLisere = 0;
            return;
        }
    
        String[] p = coup.split("-");
        int fr = Integer.parseInt(p[0].substring(1)) - 1;
        int fc = p[0].charAt(0) - 'A';
        int sr = Integer.parseInt(p[1].substring(1)) - 1;
        int sc = p[1].charAt(0) - 'A';
    
        // Vérifier que la case de départ contient bien une pièce adverse
        if (plateau[fr][fc] != -couleurJoueur && DEBUG) {
            System.out.println("ALERTE: Case départ " + p[0] + " ne contient pas une pièce adverse: " + plateau[fr][fc]);
        }
        
        // Mettre à jour le type de pièce
        typePiece[sr][sc] = typePiece[fr][fc];
        typePiece[fr][fc] = 0;
        
        // Si c'est une licorne, mettre à jour sa position
        if (typePiece[sr][sc] == LICORNE) {
            if (-couleurJoueur == NOIR) {
                licorneNoire[0] = sr;
                licorneNoire[1] = sc;
            } else {
                licorneBlanche[0] = sr;
                licorneBlanche[1] = sc;
            }
        }
    
        plateau[sr][sc] = plateau[fr][fc];
        plateau[fr][fc] = VIDE;
        lastLisere = LISERE[sr][sc];
        if (DEBUG) {
            System.out.println("Nouvelle lisère contrainte: " + lastLisere + " (position " + toPos(sr, sc) + ")");
            // Afficher les pièces qui ont ce liseré
            System.out.println("Pièces avec lisère " + lastLisere + ":");
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (plateau[r][c] == couleurJoueur && LISERE[r][c] == lastLisere) {
                        System.out.println(" - " + toPos(r, c) + (typePiece[r][c] == LICORNE ? " (licorne)" : " (paladin)"));
                    }
                }
            }
        }
    
        if (DEBUG) {
            System.out.println("Mouvement ennemi appliqué: " + coup);
            System.out.println("Nouvelle lisère contrainte: " + lastLisere);
            System.out.println("État du plateau après application:");
            afficherPlateau();
        }
    }
    
    /**
     * Vérifie si un coup est légal selon les règles d'Escampe
     */
    public boolean estCoupLegal(String coup) {
        if (coup == null || coup.isEmpty()) return false;
    
        // Format du coup: "A1-B1"
        if (!coup.contains("-") || coup.length() != 5) return false;
    
        String[] parts = coup.split("-");
        if (parts.length != 2) return false;
    
        String from = parts[0];
        String to = parts[1];
    
        // Vérifier le format des positions
        if (from.length() != 2 || to.length() != 2) return false;
    
        char fromCol = from.charAt(0);
        char toCol = to.charAt(0);
    
        if (fromCol < 'A' || fromCol > 'F' || toCol < 'A' || toCol > 'F') return false;
    
        int fromRow, toRow;
        try {
            fromRow = Integer.parseInt(from.substring(1)) - 1;
            toRow = Integer.parseInt(to.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return false;
        }
    
        if (fromRow < 0 || fromRow >= 6 || toRow < 0 || toRow >= 6) return false;
    
        int fromCol_idx = fromCol - 'A';
        int toCol_idx = toCol - 'A';
    
        // Vérifier que la case de départ contient une pièce du joueur
        if (plateau[fromRow][fromCol_idx] != couleurJoueur) {
            if (DEBUG) System.out.println("Case départ " + from + " ne contient pas une pièce du joueur: " + plateau[fromRow][fromCol_idx]);
            return false;
        }
    
        // Vérifier la contrainte de lisère
        if (lastLisere > 0) {
            boolean hasConstrainedPiece = false;
            List<String> piecesWithConstrainedLisere = new ArrayList<>();
            
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (plateau[r][c] == couleurJoueur && LISERE[r][c] == lastLisere) {
                        hasConstrainedPiece = true;
                        piecesWithConstrainedLisere.add(toPos(r, c));
                    }
                }
            }
    
            if (hasConstrainedPiece && LISERE[fromRow][fromCol_idx] != lastLisere) {
                if (DEBUG) {
                    System.out.println("Contrainte de lisère non respectée: " + lastLisere + " vs " + LISERE[fromRow][fromCol_idx]);
                    System.out.println("Pièces avec le lisère contraint " + lastLisere + ": " + piecesWithConstrainedLisere);
                }
                return false;
            }
        }
    
        // Vérifier que le mouvement est orthogonal (pas diagonal)
        if (fromRow != toRow && fromCol_idx != toCol_idx) {
            if (DEBUG) System.out.println("Mouvement diagonal non autorisé");
            return false;
        }
    
        // Calculer la distance du mouvement
        int distance = Math.abs(fromRow - toRow) + Math.abs(fromCol_idx - toCol_idx);
    
        // Vérifier que la distance est conforme au lisère
        int maxDistance = LISERE[fromRow][fromCol_idx];
        if (distance == 0 || distance > maxDistance) {
            if (DEBUG) System.out.println("Distance non conforme au lisère: " + distance + " vs max " + maxDistance);
            return false;
        }
    
        // Vérifier qu'il n'y a pas d'obstacles sur le chemin
        int dr = Integer.compare(toRow, fromRow);
        int dc = Integer.compare(toCol_idx, fromCol_idx);
    
        int r = fromRow + dr;
        int c = fromCol_idx + dc;
    
        while (r != toRow || c != toCol_idx) {
            if (plateau[r][c] != VIDE) {
                if (DEBUG) System.out.println("Obstacle sur le chemin en " + toPos(r, c) + ": " + plateau[r][c]);
                return false;
            }
            r += dr;
            c += dc;
        }
    
        // Vérifier la case d'arrivée
        if (plateau[toRow][toCol_idx] != VIDE) {
            // Si c'est une pièce adverse
            if (plateau[toRow][toCol_idx] == -couleurJoueur) {
                // Vérifier si c'est une licorne (seule pièce prenable)
                if (typePiece[toRow][toCol_idx] != LICORNE) {
                    if (DEBUG) System.out.println("Impossible de prendre un paladin adverse");
                    return false;
                }
                
                // Vérifier que la pièce qui prend est un paladin
                if (typePiece[fromRow][fromCol_idx] != PALADIN) {
                    if (DEBUG) System.out.println("Seul un paladin peut prendre la licorne adverse");
                    return false;
                }
                
                // Vérifier que la capture se fait à la distance maximale autorisée par le liseré
                if (distance != maxDistance) {
                    if (DEBUG) System.out.println("La capture doit se faire à la distance maximale: " + maxDistance + ", distance actuelle: " + distance);
                    return false;
                }
            } else {
                if (DEBUG) System.out.println("Case d'arrivée " + to + " occupée par: " + plateau[toRow][toCol_idx]);
                return false;
            }
        }
    
        // Afficher l'état du plateau pour débogage
        if (DEBUG) {
            System.out.println("Coup " + coup + " validé");
        }
    
        return true;
    }
    
    /**
     * Génère tous les coups légaux en respectant strictement les règles d'Escampe
     */
    public List<String> genererCoupsLegaux() {
        List<String> legalMoves = new ArrayList<>();
    
        // Vérifier s'il y a des pièces avec le lisère contraint
        boolean hasConstrainedPiece = false;
        List<String> piecesWithConstrainedLisere = new ArrayList<>();
        
        if (lastLisere > 0) {
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (plateau[r][c] == couleurJoueur && LISERE[r][c] == lastLisere) {
                        hasConstrainedPiece = true;
                        piecesWithConstrainedLisere.add(toPos(r, c));
                    }
                }
            }
            
            if (DEBUG && hasConstrainedPiece) {
                System.out.println("Pièces avec lisère contraint " + lastLisere + ": " + piecesWithConstrainedLisere);
            }
        }
    
        // Parcourir toutes les pièces du joueur
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (plateau[r][c] == couleurJoueur) {
                    // Si contrainte de lisère et cette pièce n'a pas le bon lisère, passer
                    if (hasConstrainedPiece && LISERE[r][c] != lastLisere) {
                        if (DEBUG) {
                            System.out.println("Pièce " + toPos(r, c) + " ignorée car lisère " + 
                                              LISERE[r][c] + " ≠ " + lastLisere);
                        }
                        continue;
                    }
        
                    // Déterminer la portée de la pièce selon son lisère
                    int portee = LISERE[r][c];
        
                    // Directions: haut, bas, gauche, droite
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
                    // Explorer chaque direction
                    for (int[] dir : directions) {
                        for (int distance = 1; distance <= portee; distance++) {
                            int nr = r + dir[0] * distance;
                            int nc = c + dir[1] * distance;
                
                            // Vérifier si la position est dans les limites du plateau
                            if (nr < 0 || nr >= 6 || nc < 0 || nc >= 6) {
                                break; // Sortie du plateau
                            }
                
                            // Vérifier s'il y a un obstacle sur le chemin
                            boolean blocked = false;
                            for (int i = 1; i < distance; i++) {
                                int checkR = r + dir[0] * i;
                                int checkC = c + dir[1] * i;
                                if (plateau[checkR][checkC] != VIDE) {
                                    blocked = true;
                                    break;
                                }
                            }
                
                            if (blocked) {
                                break; // Obstacle sur le chemin
                            }
                
                            // Vérifier la case d'arrivée
                            if (plateau[nr][nc] == VIDE) {
                                // Case vide, coup valide
                                String move = toPos(r, c) + "-" + toPos(nr, nc);
                                if (estCoupLegal(move)) {
                                    legalMoves.add(move);
                                }
                            } else if (plateau[nr][nc] == -couleurJoueur) {
                                // Case occupée par une pièce adverse
                                
                                // Vérifier si c'est une licorne (seule pièce prenable)
                                if (typePiece[nr][nc] == LICORNE) {
                                    // Vérifier que la pièce qui prend est un paladin
                                    if (typePiece[r][c] == PALADIN) {
                                        // Vérifier que la distance est exactement égale à la portée
                                        if (distance == portee) {
                                            String move = toPos(r, c) + "-" + toPos(nr, nc);
                                            if (estCoupLegal(move)) {
                                                legalMoves.add(move);
                                            }
                                        } else if (DEBUG) {
                                            System.out.println("Capture non autorisée à distance " + distance + 
                                                             " (doit être exactement " + portee + "): " + 
                                                             toPos(r, c) + "-" + toPos(nr, nc));
                                        }
                                    } else if (DEBUG) {
                                        System.out.println("Seul un paladin peut prendre la licorne adverse: " + 
                                                         toPos(r, c) + "-" + toPos(nr, nc));
                                    }
                                } else if (DEBUG) {
                                    System.out.println("Impossible de prendre un paladin adverse: " + 
                                                     toPos(r, c) + "-" + toPos(nr, nc));
                                }
                                
                                // Dans tous les cas, on ne peut pas aller plus loin dans cette direction
                                break;
                            }
                        }
                    }
                }
            }
        }
    
        if (DEBUG) {
            System.out.println("Coups légaux générés: " + legalMoves.size());
            for (String move : legalMoves) {
                System.out.println(" - " + move);
            }
        }
    
        return legalMoves;
    }
    
    /**
     * Génère les coups possibles pour l'algorithme minimax
     */
    public List<String> genererCoups(int[][] board, int[][] types, int joueur, int lisereContr) {
        List<String> moves = new ArrayList<>();
        boolean hasConstrainedPiece = false;
        if (lisereContr > 0) {
            outer:
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (board[r][c] == joueur && LISERE[r][c] == lisereContr) {
                        hasConstrainedPiece = true;
                        break outer;
                    }
                }
            }
        }
        int lisereEff = hasConstrainedPiece ? lisereContr : 0;

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == joueur) {
                    if (lisereEff > 0 && LISERE[r][c] != lisereEff) continue;
                    int max = LISERE[r][c];
                    int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                    for (int[] d : dirs) {
                        for (int step = 1; step <= max; step++) {
                            int nr = r + d[0] * step;
                            int nc = c + d[1] * step;
                            
                            // Vérifier les limites du plateau
                            if (nr < 0 || nr >= 6 || nc < 0 || nc >= 6) {
                                break;
                            }
                            
                            // Vérifier s'il y a un obstacle sur le chemin
                            boolean blocked = false;
                            for (int i = 1; i < step; i++) {
                                int checkR = r + d[0] * i;
                                int checkC = c + d[1] * i;
                                if (board[checkR][checkC] != VIDE) {
                                    blocked = true;
                                    break;
                                }
                            }
                            
                            if (blocked) {
                                break;
                            }
                            
                            // Vérifier la case d'arrivée
                            if (board[nr][nc] == VIDE) {
                                // Case vide, coup valide
                                moves.add(toPos(r, c) + "-" + toPos(nr, nc));
                            } else if (board[nr][nc] == -joueur) {
                                // Case occupée par une pièce adverse
                                
                                // Vérifier si c'est une licorne (seule pièce prenable)
                                if (types[nr][nc] == LICORNE) {
                                    // Vérifier que la pièce qui prend est un paladin
                                    if (types[r][c] == PALADIN) {
                                        // Vérifier que la distance est exactement égale à la portée
                                        if (step == max) {
                                            moves.add(toPos(r, c) + "-" + toPos(nr, nc));
                                        }
                                    }
                                }
                                
                                // Dans tous les cas, on ne peut pas aller plus loin dans cette direction
                                break;
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }
    
    /**
     * Vérifie si la partie est terminée (licorne capturée)
     */
    public boolean estPartieTerminee() {
        // Vérifier si la licorne noire est encore sur le plateau
        boolean licorneNoireTrouvee = false;
        boolean licorneBlancheTrouvee = false;
        
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (plateau[r][c] == NOIR && typePiece[r][c] == LICORNE) {
                    licorneNoireTrouvee = true;
                }
                if (plateau[r][c] == BLANC && typePiece[r][c] == LICORNE) {
                    licorneBlancheTrouvee = true;
                }
            }
        }
        
        return !licorneNoireTrouvee || !licorneBlancheTrouvee;
    }
    
    /**
     * Affiche l'état du plateau
     */
    public void afficherPlateau() {
        System.out.println("   ABCDEF");
        for (int r = 0; r < 6; r++) {
            System.out.print((r+1) + (r+1 < 10 ? " " : "") + " ");
            for (int c = 0; c < 6; c++) {
                char symbol;
                if (plateau[r][c] == VIDE) {
                    symbol = '-';
                } else if (plateau[r][c] == couleurJoueur) {
                    symbol = typePiece[r][c] == LICORNE ? 'N' : 'n';
                } else {
                    symbol = typePiece[r][c] == LICORNE ? 'B' : 'b';
                }
                System.out.print(symbol);
            }
            System.out.println(" " + (r+1));
        }
        System.out.println("   ABCDEF");
        System.out.println("Lisère contraint: " + lastLisere);
    }
    
    /**
     * Convertit une position (ligne, colonne) en notation algébrique (ex: "A1")
     */
    public static String toPos(int r, int c) {
        return "" + (char)('A' + c) + (r + 1);
    }
    
    /**
     * Crée une copie du plateau
     */
    public static int[][] copierPlateau(int[][] src) {
        int[][] copy = new int[6][6];
        for (int i = 0; i < 6; i++) copy[i] = Arrays.copyOf(src[i], 6);
        return copy;
    }
    
    // Getters et setters
    public int[][] getPlateau() {
        return plateau;
    }
    
    public int[][] getTypePiece() {
        return typePiece;
    }
    
    public int getLastLisere() {
        return lastLisere;
    }
    
    public boolean isInitialDone() {
        return initialDone;
    }
    
    public void setInitialDone(boolean initialDone) {
        this.initialDone = initialDone;
    }
    
    public int getCouleurJoueur() {
        return couleurJoueur;
    }
}
