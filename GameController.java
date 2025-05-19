import java.util.*;

/**
 * Classe responsable de la gestion des règles et de la logique du jeu
 */
public class GameController {
    private EscampeBoard board;
    
    /**
     * Constructeur
     * @param board Le plateau de jeu
     */
    public GameController(EscampeBoard board) {
        this.board = board;
    }
    
    /**
     * Indique si le coup est valide pour le joueur.
     * @param move le coup à jouer (ex: "B1-D1" ou "C6/A6/B5/D5/E6/F5")
     * @param player le joueur, "noir" ou "blanc"
     * @return true si le coup est valide, false sinon
     */
    public boolean isValidMove(String move, String player) {
        if (move.equals("E")) return true; // Passer son tour
        
        // Placement initial des pièces
        if (move.contains("/")) {
            String[] positions = move.split("/");
            if (positions.length != 6) return false;
            
            // Vérifier que les positions sont valides et sur les deux premières lignes du bord choisi
            boolean isTopBorder = positions[0].charAt(1) == '6' || positions[0].charAt(1) == '5';
            boolean isBottomBorder = positions[0].charAt(1) == '1' || positions[0].charAt(1) == '2';
            
            if (!isTopBorder && !isBottomBorder) return false;
            
            // Vérifier que le joueur noir place ses pièces en premier
            if (player.equals("blanc") && board.getLastPlayer() == null) {
                return false; // Le joueur blanc ne peut pas placer ses pièces en premier
            }
            
            // Vérifier que le joueur blanc place ses pièces sur le bord opposé à celui choisi par le joueur noir
            if (player.equals("blanc")) {
                // Trouver le bord choisi par le joueur noir
                boolean noirTopBorder = false;
                for (int i = 4; i < 6; i++) {
                    for (int j = 0; j < 6; j++) {
                        if (board.getPiece(i, j) == 'N' || board.getPiece(i, j) == 'n') {
                            noirTopBorder = true;
                            break;
                        }
                    }
                    if (noirTopBorder) break;
                }
                
                // Le joueur blanc doit choisir le bord opposé
                if ((noirTopBorder && isTopBorder) || (!noirTopBorder && isBottomBorder)) {
                    return false;
                }
            }
            
            for (String pos : positions) {
                int[] coord = board.parseCoord(pos);
                if (coord == null) return false;
                
                // Vérifier que les positions sont sur les deux premières lignes du bord choisi
                if (isTopBorder && (coord[0] < 4)) return false;
                if (isBottomBorder && (coord[0] > 1)) return false;
                
                // Vérifier que la case est vide
                if (board.getPiece(coord[0], coord[1]) != '-') return false;
            }
            
            return true;
        }
        
        // Déplacement d'une pièce
        String[] parts = move.split("-");
        if (parts.length != 2) return false;
        
        int[] from = board.parseCoord(parts[0]);
        int[] to = board.parseCoord(parts[1]);
        if (from == null || to == null) return false;
        
        // Vérifier que la pièce appartient au joueur
        char piece = board.getPiece(from[0], from[1]);
        if ((player.equals("blanc") && (piece != 'B' && piece != 'b')) ||
            (player.equals("noir") && (piece != 'N' && piece != 'n'))) {
            return false;
        }
        
        // Vérifier que la case d'arrivée est vide ou contient une licorne adverse (uniquement pour les paladins)
        char targetPiece = board.getPiece(to[0], to[1]);
        if (targetPiece != '-') {
            if ((player.equals("blanc") && (piece == 'b' && targetPiece == 'N')) ||
                (player.equals("noir") && (piece == 'n' && targetPiece == 'B'))) {
                // Un paladin peut prendre une licorne adverse
            } else {
                return false; // Mouvement invalide
            }
        }
        
        // Vérifier la contrainte du liseré (sauf pour le premier coup du blanc)
        if (!board.isFirstMove() && board.getLastMove() != null && board.getLastPlayer() != null && !board.getLastPlayer().equals(player)) {
            int lastLisere = board.getLisere(board.getLastMove()[0], board.getLastMove()[1]);
            int currentLisere = board.getLisere(from[0], from[1]);
            
            if (lastLisere != currentLisere) {
                return false; // Le liseré de départ doit être le même que celui d'arrivée du coup précédent
            }
        }
        
        // Vérifier que le mouvement est en ligne droite (pas en diagonale)
        if (from[0] != to[0] && from[1] != to[1]) return false;
        
        // Vérifier la distance de déplacement selon le liseré
        int moveDistance = board.getLisere(from[0], from[1]);
        int actualDistance = Math.abs(from[0] - to[0]) + Math.abs(from[1] - to[1]);
        if (actualDistance != moveDistance) return false;
        
        // Vérifier qu'il n'y a pas d'obstacles sur le chemin
        int stepRow = Integer.compare(to[0], from[0]);
        int stepCol = Integer.compare(to[1], from[1]);
        int curRow = from[0], curCol = from[1];
        
        for (int i = 0; i < actualDistance - 1; i++) {
            curRow += stepRow;
            curCol += stepCol;
            if (board.getPiece(curRow, curCol) != '-') return false;
        }
        
        return true;
    }
    
    /**
     * Retourne les coups possibles pour le joueur.
     * @param player le joueur, "noir" ou "blanc"
     * @return tableau de coups possibles
     */
    public String[] possiblesMoves(String player) {
        List<String> moves = new ArrayList<>();
        
        // Si aucun coup n'a été joué, le joueur blanc peut jouer n'importe quelle pièce
        if (board.isFirstMove() && player.equals("blanc")) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    char piece = board.getPiece(i, j);
                    if (piece == 'B' || piece == 'b') {
                        addPossibleMovesForPiece(moves, i, j, player);
                    }
                }
            }
            return moves.toArray(new String[0]);
        }
        
        // Si le joueur précédent n'a pas pu jouer, ce joueur peut jouer n'importe quelle pièce
        if (board.getLastMove() == null || board.getLastPlayer().equals(player)) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    char piece = board.getPiece(i, j);
                    if ((player.equals("blanc") && (piece == 'B' || piece == 'b')) ||
                        (player.equals("noir") && (piece == 'N' || piece == 'n'))) {
                        addPossibleMovesForPiece(moves, i, j, player);
                    }
                }
            }
            
            // Si aucun coup n'est possible, ajouter le coup "E" (passer son tour)
            if (moves.isEmpty()) {
                moves.add("E");
            }
            
            return moves.toArray(new String[0]);
        }
        
        // Sinon, le joueur doit jouer une pièce sur une case avec le même liseré que la case d'arrivée du coup précédent
        int targetLisere = board.getLisere(board.getLastMove()[0], board.getLastMove()[1]);
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                char piece = board.getPiece(i, j);
                if ((player.equals("blanc") && (piece == 'B' || piece == 'b')) ||
                    (player.equals("noir") && (piece == 'N' || piece == 'n'))) {
                    
                    if (board.getLisere(i, j) == targetLisere) {
                        addPossibleMovesForPiece(moves, i, j, player);
                    }
                }
            }
        }
        
        // Si aucun coup n'est possible, ajouter le coup "E" (passer son tour)
        if (moves.isEmpty()) {
            moves.add("E");
        }
        
        return moves.toArray(new String[0]);
    }
    
    /**
     * Ajoute les coups possibles pour une pièce donnée
     * @param moves Liste des coups possibles
     * @param row Ligne de la pièce
     * @param col Colonne de la pièce
     * @param player Joueur actuel
     */
    private void addPossibleMovesForPiece(List<String> moves, int row, int col, String player) {
        int moveDistance = board.getLisere(row, col);
        String from = "" + (char)('A' + col) + (row + 1);
        
        // Directions: haut, bas, gauche, droite
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        
        for (int[] d : dirs) {
            int ni = row + d[0] * moveDistance;
            int nj = col + d[1] * moveDistance;
            
            if (ni >= 0 && ni < 6 && nj >= 0 && nj < 6) {
                // Vérifier si le chemin est libre
                boolean pathClear = true;
                for (int step = 1; step < moveDistance; step++) {
                    int checkRow = row + d[0] * step;
                    int checkCol = col + d[1] * step;
                    if (checkRow >= 0 && checkRow < 6 && checkCol >= 0 && checkCol < 6 && 
                        board.getPiece(checkRow, checkCol) != '-') {
                        pathClear = false;
                        break;
                    }
                }
                
                if (!pathClear) continue;
                
                // Vérifier si la destination est valide
                char targetPiece = board.getPiece(ni, nj);
                char currentPiece = board.getPiece(row, col);
                
                if (targetPiece == '-' || 
                    (player.equals("blanc") && currentPiece == 'b' && targetPiece == 'N') ||
                    (player.equals("noir") && currentPiece == 'n' && targetPiece == 'B')) {
                    
                    String to = "" + (char)('A' + nj) + (ni + 1);
                    String move = from + "-" + to;
                    
                    // Vérifier si le mouvement est valide selon les règles complètes
                    if (isValidMove(move, player)) {
                        moves.add(move);
                    }
                }
            }
        }
    }
    
    /**
     * Modifie le plateau en jouant le coup spécifié.
     * @param move le coup à jouer
     * @param player le joueur, "noir" ou "blanc"
     */
    public void play(String move, String player) {
        if (move.equals("E")) {
            // Le joueur passe son tour
            board.setLastMove(null);
            board.setLastPlayer(player);
            // Sauvegarder le plateau après le coup
            board.saveToFile("plateau.txt");
            return;
        }
        
        if (move.contains("/")) {
            // Placement initial des pièces
            String[] tokens = move.split("/");
            board.placePiece(tokens[0], player.equals("noir") ? 'N' : 'B');
            for (int i = 1; i < tokens.length; i++) {
                board.placePiece(tokens[i], player.equals("noir") ? 'n' : 'b');
            }
            
            if (player.equals("blanc")) {
                board.setFirstMove(false);
            }
            board.setLastPlayer(player);
            // Sauvegarder le plateau après le coup
            board.saveToFile("plateau.txt");
            return;
        }
        
        // Déplacement d'une pièce
        String[] parts = move.split("-");
        int[] from = board.parseCoord(parts[0]);
        int[] to = board.parseCoord(parts[1]);
        
        // Déplacer la pièce
        board.movePiece(from, to);
        
        // Mettre à jour le dernier coup joué
        board.setLastPlayer(player);
        board.setFirstMove(false);
        
        // Sauvegarder le plateau après le coup
        board.saveToFile("plateau.txt");
    }
    
    /**
     * Retourne vrai si le jeu est terminé.
     * @return true si fin de partie, false sinon
     */
    public boolean gameOver() {
        boolean foundBlackUnicorn = false;
        boolean foundWhiteUnicorn = false;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (board.getPiece(i, j) == 'N') foundBlackUnicorn = true;
                if (board.getPiece(i, j) == 'B') foundWhiteUnicorn = true;
            }
        }
        
        return !foundBlackUnicorn || !foundWhiteUnicorn;
    }
    
    /**
     * Retourne le gagnant de la partie
     * @return "blanc", "noir" ou null si pas de gagnant
     */
    public String getWinner() {
        boolean foundBlackUnicorn = false;
        boolean foundWhiteUnicorn = false;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (board.getPiece(i, j) == 'N') foundBlackUnicorn = true;
                if (board.getPiece(i, j) == 'B') foundWhiteUnicorn = true;
            }
        }
        
        if (!foundBlackUnicorn) return "blanc";
        if (!foundWhiteUnicorn) return "noir";
        return null; // Pas de gagnant
    }
}
