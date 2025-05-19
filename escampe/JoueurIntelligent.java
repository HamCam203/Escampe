package escampe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of an intelligent player for the Escampe game.
 * This player uses a minimax algorithm with alpha-beta pruning to choose the best move.
 */
public class JoueurIntelligent implements IJoueur {
    // Constants for the board size
    private static final int BOARD_WIDTH = 6;
    private static final int BOARD_HEIGHT = 6;

    // Constants for the game pieces
    private static final int WHITE_UNICORN = -2;
    private static final int WHITE_PALADIN = -1;
    private static final int EMPTY = 0;
    private static final int BLACK_PALADIN = 1;
    private static final int BLACK_UNICORN = 2;

    // Board representation
    private int[][] board;

    // Player color
    private int myColor;

    // Random number generator for breaking ties
    private Random random;

    // Lisere case values (from Applet.java)
    private static final int[][] lisereCase = {
        {1, 2, 2, 3, 1, 2},
        {3, 1, 3, 1, 3, 2},
        {2, 3, 1, 2, 1, 3},
        {2, 1, 3, 2, 3, 1},
        {1, 3, 1, 3, 1, 2},
        {3, 2, 2, 1, 3, 2}
    };

    // Maximum search depth for minimax algorithm
    private static final int MAX_DEPTH = 4;

    /**
     * Constructor initializes the board and random number generator
     */
    public JoueurIntelligent() {
        this.board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        this.random = new Random();
        initializeBoard();
    }

    /**
     * Initializes the board with empty cells
     */
    private void initializeBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    @Override
    public void initJoueur(int mycolour) {
        this.myColor = mycolour;
        
        // If we're black, we need to place our pieces
        if (myColor == NOIR) {
            // Place BLACK_PALADIN in [0,0] 
            board[0][0] = BLACK_PALADIN;
            // Place BLACK_UNICORN in [0,5]
            board[0][5] = BLACK_UNICORN;
        }
    }

    @Override
    public int getNumJoueur() {
        return myColor;
    }

    @Override
    public String choixMouvement() {
        // If we're playing as black and this is the initial placement
        if (myColor == NOIR && isInitialPlacement()) {
            return "A1-F1";  // Initial placement for black pieces
        }
        
        // If we're playing as white and this is the initial placement
        if (myColor == BLANC && isInitialPlacement()) {
            board[5][0] = WHITE_PALADIN;   // Place WHITE_PALADIN in [5,0]
            board[5][5] = WHITE_UNICORN;   // Place WHITE_UNICORN in [5,5]
            return "A6-F6";  // Initial placement for white pieces
        }
        
        // Use minimax for actual gameplay
        String bestMove = findBestMove();
        if (bestMove != null) {
            // Apply the move to our board representation
            applyMove(bestMove, myColor);
            return bestMove;
        }
        
        // Fallback if no valid moves are found (shouldn't happen)
        return "PASSE";
    }

    /**
     * Checks if this is the initial placement phase
     */
    private boolean isInitialPlacement() {
        int pieces = 0;
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] != EMPTY) {
                    pieces++;
                }
            }
        }
        return pieces <= 2;
    }

    /**
     * Finds the best move using minimax algorithm with alpha-beta pruning
     */
    private String findBestMove() {
        List<String> validMoves = generateValidMoves(myColor);
        
        if (validMoves.isEmpty()) {
            return null;
        }
        
        String bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        
        // Create a copy of the board for simulation
        int[][] boardCopy = copyBoard(board);
        
        for (String move : validMoves) {
            // Apply move on temporary board
            applyMove(move, myColor);
            
            // Calculate move value using minimax
            int score = minimax(MAX_DEPTH - 1, -myColor, alpha, beta, false);
            
            // Restore board
            restoreBoard(boardCopy);
            
            if (score > bestScore || (score == bestScore && random.nextBoolean())) {
                bestScore = score;
                bestMove = move;
            }
            
            alpha = Math.max(alpha, bestScore);
        }
        
        return bestMove;
    }

    /**
     * Minimax algorithm with alpha-beta pruning
     */
    private int minimax(int depth, int player, int alpha, int beta, boolean maximizingPlayer) {
        // Check for terminal states
        if (depth == 0 || isGameOver()) {
            return evaluateBoard();
        }
        
        List<String> validMoves = generateValidMoves(player);
        
        if (validMoves.isEmpty()) {
            return evaluateBoard();
        }
        
        int[][] boardCopy = copyBoard(board);
        
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (String move : validMoves) {
                applyMove(move, player);
                int eval = minimax(depth - 1, -player, alpha, beta, false);
                restoreBoard(boardCopy);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String move : validMoves) {
                applyMove(move, player);
                int eval = minimax(depth - 1, -player, alpha, beta, true);
                restoreBoard(boardCopy);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    /**
     * Checks if the game is over (one player has no pieces)
     */
    private boolean isGameOver() {
        boolean hasWhite = false;
        boolean hasBlack = false;
        
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == WHITE_PALADIN || board[i][j] == WHITE_UNICORN) {
                    hasWhite = true;
                } else if (board[i][j] == BLACK_PALADIN || board[i][j] == BLACK_UNICORN) {
                    hasBlack = true;
                }
                
                if (hasWhite && hasBlack) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Evaluates the current board position
     */
    private int evaluateBoard() {
        int score = 0;
        
        // Material value
        int whiteMaterial = 0;
        int blackMaterial = 0;
        
        // Position value
        int whitePosition = 0;
        int blackPosition = 0;
        
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                switch (board[i][j]) {
                    case WHITE_PALADIN:
                        whiteMaterial += 100;
                        whitePosition += positionValue(i, j, WHITE_PALADIN);
                        break;
                    case WHITE_UNICORN:
                        whiteMaterial += 120;
                        whitePosition += positionValue(i, j, WHITE_UNICORN);
                        break;
                    case BLACK_PALADIN:
                        blackMaterial += 100;
                        blackPosition += positionValue(i, j, BLACK_PALADIN);
                        break;
                    case BLACK_UNICORN:
                        blackMaterial += 120;
                        blackPosition += positionValue(i, j, BLACK_UNICORN);
                        break;
                }
            }
        }
        
        int materialScore = blackMaterial - whiteMaterial;
        int positionScore = blackPosition - whitePosition;
        
        score = materialScore + positionScore;
        
        return myColor == NOIR ? score : -score;
    }

    /**
     * Calculates position value based on piece type and position
     */
    private int positionValue(int row, int col, int pieceType) {
        int value = 0;
        
        // Lisere value (higher lisere values are better)
        value += lisereCase[row][col] * 5;
        
        // Center control is valuable
        int centerDistance = Math.abs(row - 2) + Math.abs(col - 2);
        value += (4 - centerDistance) * 2;
        
        // For black pieces, advancing toward the opponent's side is good
        if (pieceType == BLACK_PALADIN || pieceType == BLACK_UNICORN) {
            value += row * 2;  // More points for advancing
        }
        
        // For white pieces, advancing toward the opponent's side is good
        if (pieceType == WHITE_PALADIN || pieceType == WHITE_UNICORN) {
            value += (5 - row) * 2;  // More points for advancing
        }
        
        return value;
    }

    /**
     * Generates all valid moves for the given player
     */
    private List<String> generateValidMoves(int player) {
        List<String> validMoves = new ArrayList<>();
        
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                // If the cell contains a piece of the player's color
                if ((player == BLANC && (board[i][j] == WHITE_PALADIN || board[i][j] == WHITE_UNICORN)) ||
                    (player == NOIR && (board[i][j] == BLACK_PALADIN || board[i][j] == BLACK_UNICORN))) {
                    
                    int pieceType = board[i][j];
                    List<int[]> destinations = getValidDestinations(i, j, pieceType);
                    
                    for (int[] dest : destinations) {
                        int destRow = dest[0];
                        int destCol = dest[1];
                        
                        String move = coordinatesToString(j, i) + "-" + coordinatesToString(destCol, destRow);
                        validMoves.add(move);
                    }
                }
            }
        }
        
        return validMoves;
    }

    /**
     * Gets valid destinations for a piece at the specified position
     */
    private List<int[]> getValidDestinations(int row, int col, int pieceType) {
        List<int[]> destinations = new ArrayList<>();
        
        // Get the lisere value of the current cell
        int lisere = lisereCase[row][col];
        
        // Determine moves based on piece type
        if (pieceType == WHITE_PALADIN || pieceType == BLACK_PALADIN) {
            // Paladin moves to adjacent cells with same lisere value
            addPaladinMoves(row, col, lisere, destinations);
        } else if (pieceType == WHITE_UNICORN || pieceType == BLACK_UNICORN) {
            // Unicorn moves to cells with different lisere values
            addUnicornMoves(row, col, lisere, destinations);
        }
        
        return destinations;
    }

    /**
     * Adds valid moves for a paladin (moves to adjacent cells with same lisere value)
     */
    private void addPaladinMoves(int row, int col, int lisere, List<int[]> destinations) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (isValidPosition(newRow, newCol) && 
                lisereCase[newRow][newCol] == lisere &&
                !isOccupiedByFriendly(newRow, newCol)) {
                destinations.add(new int[]{newRow, newCol});
            }
        }
    }

    /**
     * Adds valid moves for a unicorn (moves to cells with different lisere values)
     */
    private void addUnicornMoves(int row, int col, int lisere, List<int[]> destinations) {
        // Unicorn can move in 8 directions until it hits an obstacle or board edge
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            while (isValidPosition(newRow, newCol)) {
                if (board[newRow][newCol] != EMPTY) {
                    // If the cell is occupied by an enemy piece, we can capture it
                    if (!isOccupiedByFriendly(newRow, newCol)) {
                        destinations.add(new int[]{newRow, newCol});
                    }
                    break; // Stop in this direction after an occupied cell
                }
                
                // Check if the new cell has a different lisere value
                if (lisereCase[newRow][newCol] != lisere) {
                    destinations.add(new int[]{newRow, newCol});
                }
                
                // Continue in the same direction
                newRow += dir[0];
                newCol += dir[1];
            }
        }
    }

    /**
     * Checks if the position is valid (within board bounds)
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH;
    }

    /**
     * Checks if the position is occupied by a friendly piece
     */
    private boolean isOccupiedByFriendly(int row, int col) {
        if (myColor == BLANC) {
            return board[row][col] == WHITE_PALADIN || board[row][col] == WHITE_UNICORN;
        } else {
            return board[row][col] == BLACK_PALADIN || board[row][col] == BLACK_UNICORN;
        }
    }

    /**
     * Converts board coordinates to string notation (e.g., A1, B2, etc.)
     */
    private String coordinatesToString(int col, int row) {
        char colChar = (char) ('A' + col);
        return "" + colChar + (row + 1);
    }

    /**
     * Parses string notation to board coordinates
     */
    private int[] stringToCoordinates(String position) {
        int col = position.charAt(0) - 'A';
        int row = Integer.parseInt(position.substring(1)) - 1;
        return new int[]{row, col};
    }

    /**
     * Applies a move to the board
     */
    private void applyMove(String move, int player) {
        if (move.equals("PASSE")) {
            return;
        }
        
        String[] positions = move.split("-");
        int[] from = stringToCoordinates(positions[0]);
        int[] to = stringToCoordinates(positions[1]);
        
        int fromRow = from[0];
        int fromCol = from[1];
        int toRow = to[0];
        int toCol = to[1];
        
        // Move the piece
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = EMPTY;
    }

    /**
     * Copies the current board
     */
    private int[][] copyBoard(int[][] original) {
        int[][] copy = new int[BOARD_HEIGHT][BOARD_WIDTH];
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, BOARD_WIDTH);
        }
        return copy;
    }

    /**
     * Restores the board from a copy
     */
    private void restoreBoard(int[][] copy) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            System.arraycopy(copy[i], 0, board[i], 0, BOARD_WIDTH);
        }
    }

    @Override
    public void declareLeVainqueur(int colour) {
        if (colour == myColor) {
            System.out.println("J'ai gagné! JoueurIntelligent est victorieux!");
        } else if (colour == -myColor) {
            System.out.println("J'ai perdu. JoueurIntelligent s'incline.");
        } else {
            System.out.println("Match nul.");
        }
    }

    @Override
    public void mouvementEnnemi(String coup) {
        // Update our board representation with the enemy's move
        applyMove(coup, -myColor);
    }

    @Override
    public String binoName() {
        return "JoueurIntelligent";
    }
}
