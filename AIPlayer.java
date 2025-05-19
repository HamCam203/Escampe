/**
 * Classe contenant la logique de l'IA pour le jeu d'Escampe
 */
public class AIPlayer {
    private EscampeBoard board;
    private GameController gameController;
    
    // Constantes pour l'algorithme MinMax
    private static final int MAX_DEPTH = 3; // Profondeur maximale de recherche
    private static final int WIN_SCORE = 1000; // Score pour une victoire
    private static final int LOSE_SCORE = -1000; // Score pour une défaite
    
    /**
     * Constructeur
     * @param board Le plateau de jeu
     * @param gameController Le contrôleur de jeu
     */
    public AIPlayer(EscampeBoard board, GameController gameController) {
        this.board = board;
        this.gameController = gameController;
    }
    
    /**
     * Trouve le meilleur coup à jouer selon l'algorithme MinMax
     * @param player Le joueur qui doit jouer
     * @return Le meilleur coup à jouer
     */
    public String findBestMove(String player) {
        String[] possibleMoves = gameController.possiblesMoves(player);
        
        if (possibleMoves.length == 0 || (possibleMoves.length == 1 && possibleMoves[0].equals("E"))) {
            return "E"; // Pas de coup possible, passer son tour
        }
        
        String bestMove = possibleMoves[0];
        int bestValue = Integer.MIN_VALUE;
        
        for (String move : possibleMoves) {
            if (move.equals("E")) continue; // Ignorer le coup "passer son tour" s'il y a d'autres options
            
            // Sauvegarder l'état actuel
            char[][] boardCopy = board.copyBoard();
            int[] lastMoveCopy = board.getLastMove() != null ? board.getLastMove().clone() : null;
            String lastPlayerCopy = board.getLastPlayer();
            boolean isFirstMoveCopy = board.isFirstMove();
            
            // Jouer le coup
            gameController.play(move, player);
            
            // Évaluer avec MinMax
            int moveValue = minimax(MAX_DEPTH, false, player, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            // Restaurer l'état
            board.restoreBoard(boardCopy);
            board.setLastMove(lastMoveCopy);
            board.setLastPlayer(lastPlayerCopy);
            board.setFirstMove(isFirstMoveCopy);
            
            // Mettre à jour le meilleur coup
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }
        
        return bestMove;
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
        if (depth == 0 || gameController.gameOver()) {
            return evaluate(player);
        }
        
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        String currentPlayer = isMaximizingPlayer ? player : opponent;
        
        String[] possibleMoves = gameController.possiblesMoves(currentPlayer);
        
        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (String move : possibleMoves) {
                // Sauvegarder l'état actuel
                char[][] boardCopy = board.copyBoard();
                int[] lastMoveCopy = board.getLastMove() != null ? board.getLastMove().clone() : null;
                String lastPlayerCopy = board.getLastPlayer();
                boolean isFirstMoveCopy = board.isFirstMove();
                
                // Jouer le coup
                gameController.play(move, currentPlayer);
                
                // Évaluer récursivement
                int eval = minimax(depth - 1, false, player, alpha, beta);
                
                // Restaurer l'état
                board.restoreBoard(boardCopy);
                board.setLastMove(lastMoveCopy);
                board.setLastPlayer(lastPlayerCopy);
                board.setFirstMove(isFirstMoveCopy);
                
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
                char[][] boardCopy = board.copyBoard();
                int[] lastMoveCopy = board.getLastMove() != null ? board.getLastMove().clone() : null;
                String lastPlayerCopy = board.getLastPlayer();
                boolean isFirstMoveCopy = board.isFirstMove();
                
                // Jouer le coup
                gameController.play(move, currentPlayer);
                
                // Évaluer récursivement
                int eval = minimax(depth - 1, true, player, alpha, beta);
                
                // Restaurer l'état
                board.restoreBoard(boardCopy);
                board.setLastMove(lastMoveCopy);
                board.setLastPlayer(lastPlayerCopy);
                board.setFirstMove(isFirstMoveCopy);
                
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
     * Fonction d'évaluation (heuristique) pour le jeu Escampe
     * @param player Le joueur pour lequel on évalue la position
     * @return Un score positif si la position est favorable au joueur, négatif sinon
     */
    private int evaluate(String player) {
        // Si la partie est terminée, retourner un score très élevé (positif ou négatif)
        if (gameController.gameOver()) {
            String winner = gameController.getWinner();
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
                char piece = board.getPiece(i, j);
                
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
        String[] playerMoves = gameController.possiblesMoves(player);
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        String[] opponentMoves = gameController.possiblesMoves(opponent);
        
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
                    if (board.getPiece(i, j) == 'B') playerUnicorn = new int[]{i, j};
                    if (board.getPiece(i, j) == 'N') opponentUnicorn = new int[]{i, j};
                } else {
                    if (board.getPiece(i, j) == 'N') playerUnicorn = new int[]{i, j};
                    if (board.getPiece(i, j) == 'B') opponentUnicorn = new int[]{i, j};
                }
            }
        }
        
        if (playerUnicorn == null || opponentUnicorn == null) return 0;
        
        // Vérifier si les licornes sont menacées
        String opponent = player.equals("blanc") ? "noir" : "blanc";
        
        // Simuler tous les coups possibles de l'adversaire pour voir s'il peut prendre la licorne
        String[] opponentMoves = gameController.possiblesMoves(opponent);
        for (String move : opponentMoves) {
            if (move.equals("E")) continue;
            
            String[] parts = move.split("-");
            if (parts.length != 2) continue;
            
            int[] to = board.parseCoord(parts[1]);
            if (to != null && to[0] == playerUnicorn[0] && to[1] == playerUnicorn[1]) {
                threat -= 5; // La licorne du joueur est menacée
            }
        }
        
        // Simuler tous les coups possibles du joueur pour voir s'il peut prendre la licorne adverse
        String[] playerMoves = gameController.possiblesMoves(player);
        for (String move : playerMoves) {
            if (move.equals("E")) continue;
            
            String[] parts = move.split("-");
            if (parts.length != 2) continue;
            
            int[] to = board.parseCoord(parts[1]);
            if (to != null && to[0] == opponentUnicorn[0] && to[1] == opponentUnicorn[1]) {
                threat += 5; // La licorne adverse est menacée
            }
        }
        
        return threat;
    }
}
