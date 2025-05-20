package escampe;

import java.util.*;

public class MonJoueur implements IJoueur {
    private int couleur;
    private int[][] plateau;
    private static final int BLANC = IJoueur.BLANC;
    private static final int NOIR = IJoueur.NOIR;
    private static final int VIDE = 0;

    private static final int[][] LISERE = {
        {1,2,2,3,1,2},
        {3,1,3,1,3,2},
        {2,3,1,2,1,3},
        {2,1,3,2,3,1},
        {1,3,1,3,1,2},
        {3,2,2,1,3,2}
    };

    // Valeurs stratégiques des lisères (plus c'est élevé, mieux c'est)
    private static final int[] VALEUR_LISERE = {0, 1, 2, 3};
    
    // Valeurs stratégiques des positions sur le plateau
    private static final int[][] VALEUR_POSITION = {
        {3, 4, 4, 5, 3, 4}, // Ligne 0
        {5, 3, 5, 3, 5, 4}, // Ligne 1
        {4, 5, 3, 4, 3, 5}, // Ligne 2
        {4, 3, 5, 4, 5, 3}, // Ligne 3
        {3, 5, 3, 5, 3, 4}, // Ligne 4
        {5, 4, 4, 3, 5, 4}  // Ligne 5
    };

    private int lastLisere = 0;
    private boolean initialDone = false;
    private static final Random rand = new Random();

    public MonJoueur() {
        plateau = new int[6][6];
    }

    @Override
    public void initJoueur(int mycolour) {
        couleur = mycolour;
        lastLisere = 0;
        initialDone = false;
        for (int i = 0; i < 6; i++) Arrays.fill(plateau[i], VIDE);
    }

    @Override
    public int getNumJoueur() {
        return couleur;
    }

    @Override
    public String choixMouvement() {
        try {
            if (!initialDone) {
                initialDone = true;
                // Utiliser un placement intelligent au lieu d'un placement fixe
                String placement = placementIntelligent();
                
                // Mettre à jour le plateau avec notre placement
                for (String pos : placement.split("/")) {
                    int c = pos.charAt(0) - 'A';
                    int r = Integer.parseInt(pos.substring(1)) - 1;
                    plateau[r][c] = couleur;
                }
                return placement;
            }

            List<String> legalMoves = genererCoups(couleur, lastLisere, plateau);
            if (legalMoves.isEmpty()) return "PASSE";

            String best = null;
            int bestScore = Integer.MIN_VALUE;
            for (String move : legalMoves) {
                int[][] clone = copierPlateau(plateau);
                appliquer(move, couleur, clone);
                int score = minmax(clone, 1, false, couleur, lastLisere);
                if (score > bestScore) {
                    bestScore = score;
                    best = move;
                }
            }

            appliquer(best, couleur, plateau);
            String[] p = best.split("-");
            int sr = Integer.parseInt(p[1].substring(1)) - 1;
            int sc = p[1].charAt(0) - 'A';
            lastLisere = LISERE[sr][sc];
            return best;

        } catch (Exception e) {
            System.err.println("[ERREUR IA] " + e.getMessage());
            e.printStackTrace();
            return "PASSE";
        }
    }
    
    /**
     * Détermine un placement initial intelligent des pièces
     * @return Une chaîne au format "A1/B2/C3/D4/E5/F6"
     */
    private String placementIntelligent() {
        List<Position> positions = new ArrayList<>();
        int startRow, endRow;
        
        // Déterminer les lignes où nous pouvons placer nos pièces
        if (couleur == NOIR) {
            startRow = 0;
            endRow = 1;
        } else { // BLANC
            startRow = 4;
            endRow = 5;
        }
        
        // Évaluer chaque position possible
        List<PositionEvaluee> evaluations = new ArrayList<>();
        for (int r = startRow; r <= endRow; r++) {
            for (int c = 0; c < 6; c++) {
                int score = evaluerPositionInitiale(r, c);
                evaluations.add(new PositionEvaluee(r, c, score));
            }
        }
        
        // Trier les positions par score décroissant
        Collections.sort(evaluations, (a, b) -> b.score - a.score);
        
        // Sélectionner les 6 meilleures positions
        StringBuilder placement = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            PositionEvaluee pe = evaluations.get(i);
            if (i > 0) placement.append("/");
            placement.append(toPos(pe.row, pe.col));
        }
        
        return placement.toString();
    }
    
    /**
     * Évalue la valeur stratégique d'une position pour le placement initial
     */
    private int evaluerPositionInitiale(int row, int col) {
        int score = 0;
        
        // Valeur de la lisère (plus c'est élevé, mieux c'est)
        score += VALEUR_LISERE[LISERE[row][col]] * 10;
        
        // Valeur stratégique de la position
        score += VALEUR_POSITION[row][col] * 5;
        
        // Bonus pour les positions centrales
        int distanceCentreX = Math.abs(col - 2);
        int distanceCentreY = Math.abs(row - 2);
        score += (6 - distanceCentreX - distanceCentreY) * 2;
        
        // Bonus pour les positions qui contrôlent plus de cases
        score += calculerCasesControlees(row, col) * 3;
        
        return score;
    }
    
    /**
     * Calcule combien de cases une pièce peut contrôler depuis cette position
     */
    private int calculerCasesControlees(int row, int col) {
        int count = 0;
        int max = LISERE[row][col];
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        
        for (int[] d : dirs) {
            int nr = row, nc = col;
            for (int step = 1; step <= max; step++) {
                nr += d[0];
                nc += d[1];
                if (nr >= 0 && nr < 6 && nc >= 0 && nc < 6) {
                    count++;
                }
            }
        }
        
        return count;
    }

    private int minmax(int[][] board, int depth, boolean maximizing, int player, int lisere) {
        if (depth == 0) return heuristique(board, player);

        List<String> coups = genererCoups(maximizing ? player : -player, lisere, board);
        if (coups.isEmpty()) return heuristique(board, player);

        int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (String move : coups) {
            int[][] clone = copierPlateau(board);
            appliquer(move, maximizing ? player : -player, clone);
            int sr = Integer.parseInt(move.split("-")[1].substring(1)) - 1;
            int sc = move.split("-")[1].charAt(0) - 'A';
            int lisereSuivant = LISERE[sr][sc];
            int score = minmax(clone, depth - 1, !maximizing, player, lisereSuivant);
            best = maximizing ? Math.max(best, score) : Math.min(best, score);
        }
        return best;
    }

    private int heuristique(int[][] board, int player) {
        int score = 0;
        
        // Compter les pièces
        int piecesSelf = 0;
        int piecesOpponent = 0;
        
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == player) {
                    piecesSelf++;
                    // Bonus pour les pièces avec une lisère élevée
                    score += VALEUR_LISERE[LISERE[r][c]] * 2;
                    // Bonus pour la mobilité (nombre de cases contrôlées)
                    score += calculerCasesControlees(r, c);
                } 
                else if (board[r][c] == -player) {
                    piecesOpponent++;
                }
            }
        }
        
        // Différence de pièces (facteur le plus important)
        score += (piecesSelf - piecesOpponent) * 10;
        
        return score;
    }

    private int[][] copierPlateau(int[][] src) {
        int[][] copy = new int[6][6];
        for (int i = 0; i < 6; i++) copy[i] = Arrays.copyOf(src[i], 6);
        return copy;
    }

    private void appliquer(String move, int joueur, int[][] board) {
        String[] p = move.split("-");
        int fr = Integer.parseInt(p[0].substring(1)) - 1;
        int fc = p[0].charAt(0) - 'A';
        int sr = Integer.parseInt(p[1].substring(1)) - 1;
        int sc = p[1].charAt(0) - 'A';
        board[sr][sc] = board[fr][fc];
        board[fr][fc] = VIDE;
    }

    private List<String> genererCoups(int joueur, int lisereContr, int[][] board) {
        List<String> moves = new ArrayList<>();
        boolean hasConstrained = false;
        if (lisereContr > 0) {
            outer:
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (board[r][c] == joueur && LISERE[r][c] == lisereContr) {
                        hasConstrained = true;
                        break outer;
                    }
                }
            }
        }
        int lisereEff = hasConstrained ? lisereContr : 0;

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == joueur) {
                    if (lisereEff > 0 && LISERE[r][c] != lisereEff) continue;
                    int max = LISERE[r][c];
                    int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                    for (int[] d : dirs) {
                        int nr = r, nc = c;
                        boolean blocked = false;
                        for (int step = 1; step <= max; step++) {
                            nr += d[0];
                            nc += d[1];
                            if (nr < 0 || nr >= 6 || nc < 0 || nc >= 6) {
                                blocked = true; break;
                            }
                            if (board[nr][nc] != VIDE &&
                                !(board[nr][nc] == -joueur && step == max)) {
                                blocked = true; break;
                            }
                        }
                        if (!blocked) {
                            moves.add(toPos(r, c) + "-" + toPos(nr, nc));
                        }
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        if ("PASSE".equals(coup)) {
            lastLisere = 0;
            return;
        }
        if (coup.contains("/")) {
            int advCol = -couleur;
            for (String pos : coup.split("/")) {
                int c = pos.charAt(0) - 'A';
                int r = Integer.parseInt(pos.substring(1)) - 1;
                plateau[r][c] = advCol;
            }
            return;
        }
        String[] p = coup.split("-");
        int fr = Integer.parseInt(p[0].substring(1)) - 1;
        int fc = p[0].charAt(0) - 'A';
        int sr = Integer.parseInt(p[1].substring(1)) - 1;
        int sc = p[1].charAt(0) - 'A';
        plateau[sr][sc] = plateau[fr][fc];
        plateau[fr][fc] = VIDE;
        lastLisere = LISERE[sr][sc];
    }

    @Override
    public void declareLeVainqueur(int couleurGagnant) {
        if (couleurGagnant == couleur) System.out.println("J'ai gagné !");
        else if (couleurGagnant == 0) System.out.println("Match nul !");
        else System.out.println("J'ai perdu !");
    }

    @Override
    public String binoName() {
        return "MonEquipe";
    }

    private String toPos(int r, int c) {
        return "" + (char)('A' + c) + (r + 1);
    }
    
    // Classes utilitaires pour le placement intelligent
    private static class Position {
        final int row;
        final int col;
        
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    
    private static class PositionEvaluee extends Position {
        final int score;
        
        PositionEvaluee(int row, int col, int score) {
            super(row, col);
            this.score = score;
        }
    }
}