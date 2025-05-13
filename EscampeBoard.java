import java.io.*;
import java.util.*;

// javac Partie1.java EscampeBoard.java
// java EscampeBoard

public class EscampeBoard implements Partie1 {
    private char[][] board = new char[6][6];
    private int[][] lisere = {
        {3, 2, 2, 1, 3, 2},
        {1, 3, 1, 3, 1, 2},
        {2, 1, 3, 2, 3, 1},
        {2, 3, 1, 2, 1, 3},
        {3, 1, 3, 1, 3, 2},
        {1, 2, 2, 3, 1, 2}
    };

    // Variables pour suivre le dernier coup joué
    private int[] lastMove = null;
    private String lastPlayer = null;
    private boolean isFirstMove = true;
    
    // Constantes pour l'algorithme MinMax
    private static final int MAX_DEPTH = 3; // Profondeur maximale de recherche
    private static final int WIN_SCORE = 1000; // Score pour une victoire
    private static final int LOSE_SCORE = -1000; // Score pour une défaite
    
    public void setFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < 6) {
                if (line.startsWith("%")) continue;
                String content = line.replaceAll("[0-9]", "").trim();
                for (int col = 0; col < 6; col++) {
                    board[5 - row][col] = content.charAt(col);
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
    }

    public void saveToFile(String fileName) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("% ABCDEF");
            for (int i = 5; i >= 0; i--) {
                pw.print(String.format("%02d ", 6 - i));
                for (int j = 0; j < 6; j++) {
                    pw.print(board[i][j]);
                }
                pw.println(" " + String.format("%02d", 6 - i));
            }
            pw.println("% ABCDEF");
        } catch (IOException e) {
            System.err.println("Erreur d'écriture : " + e.getMessage());
        }
    }

    // Méthode pour afficher le plateau dans le terminal
    public void displayBoard() {
        System.out.println("  A B C D E F");
        System.out.println("  -----------");
        for (int i = 5; i >= 0; i--) {
            System.out.print((i + 1) + "|");
            for (int j = 0; j < 6; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("|" + (i + 1));
        }
        System.out.println("  -----------");
        System.out.println("  A B C D E F");
        
        // Afficher les types de liseré pour aider les joueurs
        System.out.println("\nTypes de liseré (1=simple, 2=double, 3=triple):");
        for (int i = 5; i >= 0; i--) {
            System.out.print((i + 1) + "|");
            for (int j = 0; j < 6; j++) {
                System.out.print(lisere[i][j] + " ");
            }
            System.out.println();
        }
    }

    public boolean isValidMove(String move, String player) {
        if (move.equals("E")) return true; // Passer son tour
        
        // Placement initial des pièces
        if (move.contains("/")) {
            if (!isFirstMove && !lastPlayer.equals(player)) return false;
            String[] positions = move.split("/");
            if (positions.length != 6) return false;
            
            // Vérifier que les positions sont valides et sur les deux premières lignes du bord choisi
            boolean isTopBorder = positions[0].charAt(1) == '6' || positions[0].charAt(1) == '5';
            boolean isBottomBorder = positions[0].charAt(1) == '1' || positions[0].charAt(1) == '2';
            
            if (!isTopBorder && !isBottomBorder) return false;
            
            for (String pos : positions) {
                int[] coord = parseCoord(pos);
                if (coord == null) return false;
                
                // Vérifier que les positions sont sur les deux premières lignes du bord choisi
                if (isTopBorder && (coord[0] < 4)) return false;
                if (isBottomBorder && (coord[0] > 1)) return false;
                
                // Vérifier que la case est vide
                if (board[coord[0]][coord[1]] != '-') return false;
            }
            
            return true;
        }
        
        // Déplacement d'une pièce
        String[] parts = move.split("-");
        if (parts.length != 2) return false;
        
        int[] from = parseCoord(parts[0]);
        int[] to = parseCoord(parts[1]);
        if (from == null || to == null) return false;
        
        // Vérifier que la pièce appartient au joueur
        char piece = board[from[0]][from[1]];
        if ((player.equals("blanc") && (piece != 'B' && piece != 'b')) ||
            (player.equals("noir") && (piece != 'N' && piece != 'n'))) {
            return false;
        }
        
        // Vérifier que la case d'arrivée est vide ou contient une licorne adverse (uniquement pour les paladins)
        char targetPiece = board[to[0]][to[1]];
        if (targetPiece != '-') {
            if ((player.equals("blanc") && (piece == 'b' && targetPiece == 'N')) ||
                (player.equals("noir") && (piece == 'n' && targetPiece == 'B'))) {
                // Un paladin peut prendre une licorne adverse
            } else {
                return false; // Mouvement invalide
            }
        }
        
        // Vérifier la contrainte du liseré (sauf pour le premier coup du blanc)
        if (!isFirstMove && lastMove != null && lastPlayer != null && !lastPlayer.equals(player)) {
            int lastLisere = lisere[lastMove[0]][lastMove[1]];
            int currentLisere = lisere[from[0]][from[1]];
            
            if (lastLisere != currentLisere) {
                return false; // Le liseré de départ doit être le même que celui d'arrivée du coup précédent
            }
        }
        
        // Vérifier que le mouvement est en ligne droite (pas en diagonale)
        if (from[0] != to[0] && from[1] != to[1]) return false;
        
        // Vérifier la distance de déplacement selon le liseré
        int moveDistance = lisere[from[0]][from[1]];
        int actualDistance = Math.abs(from[0] - to[0]) + Math.abs(from[1] - to[1]);
        if (actualDistance != moveDistance) return false;
        
        // Vérifier qu'il n'y a pas d'obstacles sur le chemin
        int stepRow = Integer.compare(to[0], from[0]);
        int stepCol = Integer.compare(to[1], from[1]);
        int curRow = from[0], curCol = from[1];
        
        for (int i = 0; i < actualDistance - 1; i++) {
            curRow += stepRow;
            curCol += stepCol;
            if (board[curRow][curCol] != '-') return false;
        }
        
        return true;
    }

    public String[] possiblesMoves(String player) {
        List<String> moves = new ArrayList<>();
        
        // Si aucun coup n'a été joué, le joueur blanc peut jouer n'importe quelle pièce
        if (isFirstMove && player.equals("blanc")) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    char piece = board[i][j];
                    if (piece == 'B' || piece == 'b') {
                        addPossibleMovesForPiece(moves, i, j, player);
                    }
                }
            }
            return moves.toArray(new String[0]);
        }
        
        // Si le joueur précédent n'a pas pu jouer, ce joueur peut jouer n'importe quelle pièce
        if (lastMove == null || lastPlayer.equals(player)) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    char piece = board[i][j];
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
        int targetLisere = lisere[lastMove[0]][lastMove[1]];
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                char piece = board[i][j];
                if ((player.equals("blanc") && (piece == 'B' || piece == 'b')) ||
                    (player.equals("noir") && (piece == 'N' || piece == 'n'))) {
                    
                    if (lisere[i][j] == targetLisere) {
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

    private void addPossibleMovesForPiece(List<String> moves, int row, int col, String player) {
        int moveDistance = lisere[row][col];
        String from = "" + (char)('A' + col) + (row + 1);
        
        // Directions: haut, bas, gauche, droite
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        
        for (int[] d : dirs) {
            for (int dist = 1; dist <= moveDistance; dist++) {
                int ni = row + d[0] * dist;
                int nj = col + d[1] * dist;
                
                if (ni >= 0 && ni < 6 && nj >= 0 && nj < 6) {
                    // Vérifier si le chemin est libre
                    boolean pathClear = true;
                    for (int step = 1; step < dist; step++) {
                        int checkRow = row + d[0] * step;
                        int checkCol = col + d[1] * step;
                        if (board[checkRow][checkCol] != '-') {
                            pathClear = false;
                            break;
                        }
                    }
                    
                    if (!pathClear) continue;
                    
                    // Vérifier si la destination est valide
                    char targetPiece = board[ni][nj];
                    char currentPiece = board[row][col];
                    
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
    }

    public void play(String move, String player) {
        if (move.equals("E")) {
            // Le joueur passe son tour
            lastMove = null;
            lastPlayer = player;
            // Sauvegarder le plateau après le coup
            saveToFile("plateau.txt");
            return;
        }
        
        if (move.contains("/")) {
            // Placement initial des pièces
            String[] tokens = move.split("/");
            placePiece(tokens[0], player.equals("noir") ? 'N' : 'B');
            for (int i = 1; i < tokens.length; i++) {
                placePiece(tokens[i], player.equals("noir") ? 'n' : 'b');
            }
            
            isFirstMove = false;
            lastPlayer = player;
            // Sauvegarder le plateau après le coup
            saveToFile("plateau.txt");
            return;
        }
        
        // Déplacement d'une pièce
        String[] parts = move.split("-");
        int[] from = parseCoord(parts[0]);
        int[] to = parseCoord(parts[1]);
        
        // Déplacer la pièce
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = '-';
        
        // Mettre à jour le dernier coup joué
        lastMove = to;
        lastPlayer = player;
        isFirstMove = false;
        
        // Sauvegarder le plateau après le coup
        saveToFile("plateau.txt");
    }

    public boolean gameOver() {
        boolean foundBlackUnicorn = false;
        boolean foundWhiteUnicorn = false;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == 'N') foundBlackUnicorn = true;
                if (board[i][j] == 'B') foundWhiteUnicorn = true;
            }
        }
        
        return !foundBlackUnicorn || !foundWhiteUnicorn;
    }

    // Retourne le gagnant de la partie
    public String getWinner() {
        boolean foundBlackUnicorn = false;
        boolean foundWhiteUnicorn = false;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (board[i][j] == 'N') foundBlackUnicorn = true;
                if (board[i][j] == 'B') foundWhiteUnicorn = true;
            }
        }
        
        if (!foundBlackUnicorn) return "blanc";
        if (!foundWhiteUnicorn) return "noir";
        return null; // Pas de gagnant
    }

    private void placePiece(String coord, char piece) {
        int[] pos = parseCoord(coord);
        if (pos != null) {
            board[pos[0]][pos[1]] = piece;
        }
    }

    private int[] parseCoord(String s) {
        if (s.length() != 2) return null;
        int col = s.charAt(0) - 'A';
        int row = s.charAt(1) - '1';
        if (col < 0 || col >= 6 || row < 0 || row >= 6) return null;
        return new int[]{row, col};
    }
    
    // Méthodes pour l'algorithme MinMax
    
    /**
     * Fonction d'évaluation (heuristique) pour le jeu Escampe
     * @param player Le joueur pour lequel on évalue la position
     * @return Un score positif si la position est favorable au joueur, négatif sinon
     */
    private int evaluate(String player) {
        // Si la partie est terminée, retourner un score très élevé (positif ou négatif)
        if (gameOver()) {
            String winner = getWinner();
            if (winner != null && winner.equals(player)) {
                return WIN_SCORE;
            } else {
                return LOSE_SCORE;
            }
        }
        
        int score = 0;
        
        // Compter les pièces
        int playerPaladins = 0;
        int opponentPaladins = 0;
        boolean playerHasUnicorn = false;
        boolean opponentHasUnicorn = false;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                char piece = board[i][j];
                
                if (player.equals("blanc")) {
                    if (piece == 'b') playerPaladins++;
                    if (piece == 'B') playerHasUnicorn = true;
                    if (piece == 'n') opponentPaladins++;
                    if (piece == 'N') opponentHasUnicorn = true;
                } else { // player is "noir"
                    if (piece == 'n') playerPaladins++;
                    if (piece == 'N') playerHasUnicorn = true;
                    if (piece == 'b') opponentPaladins++;
                    if (piece == 'B') opponentHasUnicorn = true;
                }
            }
        }
        
        // Vérifier si les licornes sont présentes
        if (!playerHasUnicorn) return LOSE_SCORE;
        if (!opponentHasUnicorn) return WIN_SCORE;
        
        // Évaluer la mobilité (nombre de coups possibles)
        String[] playerMoves = possiblesMoves(player);
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        String[] opponentMoves = possiblesMoves(opponent);
        
        int mobility = playerMoves.length - opponentMoves.length;
        
        // Évaluer les menaces sur les licornes
        int unicornThreat = evaluateUnicornThreat(player);
        
        // Combiner les facteurs
        score = 5 * (playerPaladins - opponentPaladins) + 2 * mobility + 10 * unicornThreat;
        
        return score;
    }
    
    /**
     * Évalue les menaces sur les licornes
     * @param player Le joueur pour lequel on évalue
     * @return Un score positif si la licorne adverse est menacée, négatif si la licorne du joueur est menacée
     */
    private int evaluateUnicornThreat(String player) {
        int threat = 0;
        
        // Trouver les positions des licornes
        int[] playerUnicorn = null;
        int[] opponentUnicorn = null;
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (player.equals("blanc")) {
                    if (board[i][j] == 'B') playerUnicorn = new int[]{i, j};
                    if (board[i][j] == 'N') opponentUnicorn = new int[]{i, j};
                } else {
                    if (board[i][j] == 'N') playerUnicorn = new int[]{i, j};
                    if (board[i][j] == 'B') opponentUnicorn = new int[]{i, j};
                }
            }
        }
        
        if (playerUnicorn == null || opponentUnicorn == null) return 0;
        
        // Vérifier si les licornes sont menacées
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        
        // Simuler tous les coups possibles de l'adversaire pour voir s'il peut prendre la licorne
        String[] opponentMoves = possiblesMoves(opponent);
        for (String move : opponentMoves) {
            if (move.equals("E")) continue;
            
            String[] parts = move.split("-");
            if (parts.length != 2) continue;
            
            int[] to = parseCoord(parts[1]);
            if (to[0] == playerUnicorn[0] && to[1] == playerUnicorn[1]) {
                threat -= 5; // La licorne du joueur est menacée
            }
        }
        
        // Simuler tous les coups possibles du joueur pour voir s'il peut prendre la licorne adverse
        String[] playerMoves = possiblesMoves(player);
        for (String move : playerMoves) {
            if (move.equals("E")) continue;
            
            String[] parts = move.split("-");
            if (parts.length != 2) continue;
            
            int[] to = parseCoord(parts[1]);
            if (to[0] == opponentUnicorn[0] && to[1] == opponentUnicorn[1]) {
                threat += 5; // La licorne adverse est menacée
            }
        }
        
        return threat;
    }
    
    /**
     * Algorithme MinMax pour trouver le meilleur coup
     * @param depth Profondeur actuelle de recherche
     * @param isMaximizingPlayer True si c'est au tour du joueur maximisant
     * @param player Le joueur actuel
     * @param alpha Valeur alpha pour l'élagage alpha-beta
     * @param beta Valeur beta pour l'élagage alpha-beta
     * @return Le score de la meilleure position trouvée
     */
    private int minimax(int depth, boolean isMaximizingPlayer, String player, int alpha, int beta) {
        // Si on a atteint la profondeur maximale ou si la partie est terminée
        if (depth == 0 || gameOver()) {
            return evaluate(player);
        }
        
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        String currentPlayer = isMaximizingPlayer ? player : opponent;
        
        String[] possibleMoves = possiblesMoves(currentPlayer);
        
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (String move : possibleMoves) {
                // Sauvegarder l'état actuel
                char[][] boardCopy = copyBoard();
                int[] lastMoveCopy = lastMove != null ? lastMove.clone() : null;
                String lastPlayerCopy = lastPlayer;
                boolean isFirstMoveCopy = isFirstMove;
                
                // Jouer le coup
                play(move, currentPlayer);
                
                // Évaluer récursivement
                int eval = minimax(depth - 1, false, player, alpha, beta);
                
                // Restaurer l'état
                restoreBoard(boardCopy);
                lastMove = lastMoveCopy;
                lastPlayer = lastPlayerCopy;
                isFirstMove = isFirstMoveCopy;
                
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Élagage beta
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String move : possibleMoves) {
                // Sauvegarder l'état actuel
                char[][] boardCopy = copyBoard();
                int[] lastMoveCopy = lastMove != null ? lastMove.clone() : null;
                String lastPlayerCopy = lastPlayer;
                boolean isFirstMoveCopy = isFirstMove;
                
                // Jouer le coup
                play(move, currentPlayer);
                
                // Évaluer récursivement
                int eval = minimax(depth - 1, true, player, alpha, beta);
                
                // Restaurer l'état
                restoreBoard(boardCopy);
                lastMove = lastMoveCopy;
                lastPlayer = lastPlayerCopy;
                isFirstMove = isFirstMoveCopy;
                
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Élagage alpha
                }
            }
            return minEval;
        }
    }
    
    /**
     * Trouve le meilleur coup à jouer selon l'algorithme MinMax
     * @param player Le joueur qui doit jouer
     * @return Le meilleur coup à jouer
     */
    public String findBestMove(String player) {
        String[] possibleMoves = possiblesMoves(player);
        
        if (possibleMoves.length == 0 || (possibleMoves.length == 1 && possibleMoves[0].equals("E"))) {
            return "E"; // Pas de coup possible, passer son tour
        }
        
        String bestMove = possibleMoves[0];
        int bestValue = Integer.MIN_VALUE;
        
        for (String move : possibleMoves) {
            if (move.equals("E")) continue; // Ignorer le coup "passer son tour" s'il y a d'autres options
            
            // Sauvegarder l'état actuel
            char[][] boardCopy = copyBoard();
            int[] lastMoveCopy = lastMove != null ? lastMove.clone() : null;
            String lastPlayerCopy = lastPlayer;
            boolean isFirstMoveCopy = isFirstMove;
            
            // Jouer le coup
            play(move, player);
            
            // Évaluer avec MinMax
            int moveValue = minimax(MAX_DEPTH, false, player, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            // Restaurer l'état
            restoreBoard(boardCopy);
            lastMove = lastMoveCopy;
            lastPlayer = lastPlayerCopy;
            isFirstMove = isFirstMoveCopy;
            
            // Mettre à jour le meilleur coup
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Copie le plateau de jeu actuel
     * @return Une copie du plateau
     */
    private char[][] copyBoard() {
        char[][] copy = new char[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }
    
    /**
     * Restaure le plateau à partir d'une copie
     * @param copy La copie du plateau à restaurer
     */
    private void restoreBoard(char[][] copy) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                board[i][j] = copy[i][j];
            }
        }
    }

    // Méthode principale pour jouer à deux joueurs via le terminal
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EscampeBoard board = new EscampeBoard();
        
        // Initialiser le plateau vide
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                board.board[i][j] = '-';
            }
        }
        
        // Initialiser le fichier plateau.txt avec le plateau vide
        board.saveToFile("plateau.txt");
        
        System.out.println("=== JEU D'ESCAMPE ===");
        System.out.println("Règles:");
        System.out.println("- Le but du jeu est de prendre la licorne adverse avec un paladin");
        System.out.println("- Les pièces se déplacent en ligne droite (pas en diagonale)");
        System.out.println("- La distance de déplacement dépend du liseré de la case de départ:");
        System.out.println("  * Liseré simple (1): 1 case");
        System.out.println("  * Liseré double (2): 2 cases");
        System.out.println("  * Liseré triple (3): 3 cases");
        System.out.println("- La pièce jouée doit partir d'une case ayant le même liseré que celle");
        System.out.println("  sur laquelle l'autre joueur a posé sa pièce au tour précédent");
        System.out.println("- Si aucun coup n'est possible, entrez 'E' pour passer votre tour");
        System.out.println("- Le plateau est enregistré dans le fichier 'plateau.txt' après chaque coup");
        System.out.println();
        
        // Choix du mode de jeu
        System.out.println("Choisissez le mode de jeu:");
        System.out.println("1. Joueur contre Joueur");
        System.out.println("2. Joueur contre IA (vous êtes Blanc)");
        System.out.println("3. Joueur contre IA (vous êtes Noir)");
        System.out.println("4. IA contre IA (démonstration)");
        
        int gameMode = 0;
        while (gameMode < 1 || gameMode > 4) {
            System.out.print("Votre choix (1-4): ");
            try {
                gameMode = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide. Veuillez entrer un nombre entre 1 et 4.");
            }
        }
        
        // Phase 1: Placement initial des pièces
        System.out.println("=== PHASE 1: PLACEMENT INITIAL DES PIÈCES ===");
        
        // Le joueur noir choisit un bord et place ses pièces
        if (gameMode == 1 || gameMode == 2) {
            System.out.println("Joueur NOIR: Placez vos pièces (format: C6/A6/B5/D5/E6/F5)");
            System.out.println("Le premier emplacement est pour la licorne, les autres pour les paladins");
            System.out.println("Vous pouvez choisir le bord haut (lignes 5-6) ou bas (lignes 1-2)");
            
            String placementNoir;
            do {
                System.out.print("Placement NOIR: ");
                placementNoir = scanner.nextLine().toUpperCase();
                if (!board.isValidMove(placementNoir, "noir")) {
                    System.out.println("Placement invalide! Réessayez.");
                }
            } while (!board.isValidMove(placementNoir, "noir"));
            
            board.play(placementNoir, "noir");
        } else {
            // L'IA place les pièces noires
            String placementNoir = "C6/A6/B5/D5/E6/F5"; // Placement par défaut pour l'IA
            System.out.println("IA (NOIR) place ses pièces: " + placementNoir);
            board.play(placementNoir, "noir");
        }
        
        board.displayBoard();
        
        // Le joueur blanc place ses pièces sur le bord opposé
        if (gameMode == 1 || gameMode == 3) {
            System.out.println("Joueur BLANC: Placez vos pièces (format: C1/A1/B2/D2/E1/F2)");
            System.out.println("Le premier emplacement est pour la licorne, les autres pour les paladins");
            
            String placementBlanc;
            do {
                System.out.print("Placement BLANC: ");
                placementBlanc = scanner.nextLine().toUpperCase();
                if (!board.isValidMove(placementBlanc, "blanc")) {
                    System.out.println("Placement invalide! Réessayez.");
                }
            } while (!board.isValidMove(placementBlanc, "blanc"));
            
            board.play(placementBlanc, "blanc");
        } else {
            // L'IA place les pièces blanches
            String placementBlanc = "C1/A1/B2/D2/E1/F2"; // Placement par défaut pour l'IA
            System.out.println("IA (BLANC) place ses pièces: " + placementBlanc);
            board.play(placementBlanc, "blanc");
        }
        
        board.displayBoard();
        
        // Phase 2: Jeu principal
        System.out.println("=== PHASE 2: JEU PRINCIPAL ===");
        
        String currentPlayer = "blanc"; // Le blanc commence
        
        while (!board.gameOver()) {
            System.out.println("\nTour du joueur " + currentPlayer.toUpperCase());
            
            // Afficher les coups possibles
            String[] possibleMoves = board.possiblesMoves(currentPlayer);
            System.out.println("Coups possibles: " + Arrays.toString(possibleMoves));
            
            String move;
            
            // Déterminer si c'est un joueur humain ou l'IA qui joue
            boolean isHuman = (gameMode == 1) || 
                             (gameMode == 2 && currentPlayer.equals("blanc")) || 
                             (gameMode == 3 && currentPlayer.equals("noir"));
            
            if (isHuman) {
                // Tour du joueur humain
                if (possibleMoves.length == 1 && possibleMoves[0].equals("E")) {
                    System.out.println("Aucun coup possible. Vous passez votre tour (E).");
                    move = "E";
                } else {
                    do {
                        System.out.print("Votre coup (format A1-B1 ou E pour passer): ");
                        move = scanner.nextLine().toUpperCase();
                        if (!board.isValidMove(move, currentPlayer)) {
                            System.out.println("Coup invalide! Réessayez.");
                        }
                    } while (!board.isValidMove(move, currentPlayer));
                }
            } else {
                // Tour de l'IA
                System.out.println("L'IA réfléchit...");
                move = board.findBestMove(currentPlayer);
                System.out.println("L'IA joue: " + move);
            }
            
            // Jouer le coup
            board.play(move, currentPlayer);
            
            // Afficher le plateau après le coup
            board.displayBoard();
            
            // Changer de joueur
            currentPlayer = currentPlayer.equals("blanc") ? "noir" : "blanc";
        }
        
        // Fin de partie
        System.out.println("\n=== FIN DE PARTIE ===");
        String winner = board.getWinner();
        System.out.println("Le joueur " + winner.toUpperCase() + " a gagné!");
        
        scanner.close();
    }
}
