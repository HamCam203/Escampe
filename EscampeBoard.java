import java.io.*;

/**
 * Classe représentant le plateau de jeu d'Escampe
 * Gère la représentation du plateau et les opérations de base
 */
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
    
    /**
     * Constructeur par défaut
     * Initialise un plateau vide
     */
    public EscampeBoard() {
        // Initialiser le plateau vide
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                board[i][j] = '-';
            }
        }
    }
    
    /**
     * Initialise un plateau à partir d'un fichier texte.
     * @param fileName le nom du fichier à lire
     */
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

    /**
     * Sauvegarde la configuration de l'état courant dans un fichier.
     * @param fileName le nom du fichier à sauvegarder
     */
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

    /**
     * Affiche le plateau dans le terminal
     */
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

    /**
     * Place une pièce sur le plateau
     * @param coord Coordonnées (ex: "A1")
     * @param piece Type de pièce ('B', 'b', 'N', 'n')
     */
    public void placePiece(String coord, char piece) {
        int[] pos = parseCoord(coord);
        if (pos != null) {
            board[pos[0]][pos[1]] = piece;
        }
    }

    /**
     * Déplace une pièce sur le plateau
     * @param from Coordonnées de départ
     * @param to Coordonnées d'arrivée
     */
    public void movePiece(int[] from, int[] to) {
        board[to[0]][to[1]] = board[from[0]][from[1]];
        board[from[0]][from[1]] = '-';
        lastMove = to;
    }

    /**
     * Convertit des coordonnées textuelles en indices de tableau
     * @param s Coordonnées (ex: "A1")
     * @return Tableau d'indices [row, col]
     */
    public int[] parseCoord(String s) {
        if (s.length() != 2) return null;
        int col = s.charAt(0) - 'A';
        int row = s.charAt(1) - '1';
        if (col < 0 || col >= 6 || row < 0 || row >= 6) return null;
        return new int[]{row, col};
    }

    /**
     * Crée une copie du plateau actuel
     * @return Une copie du plateau
     */
    public char[][] copyBoard() {
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
    public void restoreBoard(char[][] copy) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                board[i][j] = copy[i][j];
            }
        }
    }

    // Getters et setters
    public char getPiece(int row, int col) {
        return board[row][col];
    }
    
    public int getLisere(int row, int col) {
        return lisere[row][col];
    }
    
    public int[] getLastMove() {
        return lastMove;
    }
    
    public void setLastMove(int[] lastMove) {
        this.lastMove = lastMove;
    }
    
    public String getLastPlayer() {
        return lastPlayer;
    }
    
    public void setLastPlayer(String lastPlayer) {
        this.lastPlayer = lastPlayer;
    }
    
    public boolean isFirstMove() {
        return isFirstMove;
    }
    
    public void setFirstMove(boolean isFirstMove) {
        this.isFirstMove = isFirstMove;
    }

    // Méthodes de l'interface Partie1 qui délèguent à GameController
    @Override
    public boolean isValidMove(String move, String player) {
        // Cette méthode sera implémentée dans GameController
        return false;
    }

    @Override
    public String[] possiblesMoves(String player) {
        // Cette méthode sera implémentée dans GameController
        return new String[0];
    }

    @Override
    public void play(String move, String player) {
        // Cette méthode sera implémentée dans GameController
    }

    @Override
    public boolean gameOver() {
        // Cette méthode sera implémentée dans GameController
        return false;
    }
}
