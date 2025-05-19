package escampe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Classe permettant l'affichage graphique du plateau de jeu Escampe
 */
public class Applet extends JPanel {
    private static final long serialVersionUID = 1L;
    
    // Dimensions du plateau
    private static final int BOARD_SIZE = 6;
    private static final int SQUARE_SIZE = 60;
    
    // Couleurs du plateau
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color HIGHLIGHT = new Color(255, 255, 0, 128);
    
    private String[][] board;
    private String lastMove = "";
    private String currentPlayer = "";
    
    public Applet() {
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));
        board = new String[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Initialiser un plateau vide
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = "";
            }
        }
    }
    
    public void updateBoard(String position, String currentPlayer) {
        this.currentPlayer = currentPlayer;
        
        // Réinitialiser le plateau
        initializeBoard();
        
        // Format de position attendu: pièces_position_pièces_position...
        String[] parts = position.split("_");
        for (int i = 0; i < parts.length; i += 2) {
            String piece = parts[i];
            if (i + 1 < parts.length) {
                String pos = parts[i + 1];
                int row = pos.charAt(0) - 'a';
                int col = pos.charAt(1) - '1';
                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                    board[row][col] = piece;
                }
            }
        }
        
        repaint();
    }
    
    public void setLastMove(String move) {
        this.lastMove = move;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Dessiner le plateau
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Alternance des couleurs pour les cases
                if ((row + col) % 2 == 0) {
                    g2d.setColor(LIGHT_SQUARE);
                } else {
                    g2d.setColor(DARK_SQUARE);
                }
                
                g2d.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                
                // Dessiner les coordonnées
                if (row == BOARD_SIZE - 1) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.valueOf((char)('a' + col)), 
                                  col * SQUARE_SIZE + SQUARE_SIZE - 15, 
                                  row * SQUARE_SIZE + SQUARE_SIZE - 5);
                }
                if (col == 0) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(String.valueOf(row + 1), 
                                  col * SQUARE_SIZE + 5, 
                                  row * SQUARE_SIZE + 15);
                }
                
                // Dessiner les pièces
                if (!board[row][col].isEmpty()) {
                    drawPiece(g2d, row, col, board[row][col]);
                }
            }
        }
        
        // Afficher le joueur actuel
        g2d.setColor(Color.BLACK);
        g2d.drawString("Joueur actuel: " + currentPlayer, 10, BOARD_SIZE * SQUARE_SIZE + 20);
    }
    
    private void drawPiece(Graphics2D g2d, int row, int col, String piece) {
        int x = col * SQUARE_SIZE;
        int y = row * SQUARE_SIZE;
        
        if (piece.startsWith("N")) {
            g2d.setColor(Color.BLACK);
        } else {
            g2d.setColor(Color.WHITE);
        }
        
        // Dessiner un cercle pour représenter la pièce
        g2d.fillOval(x + 10, y + 10, SQUARE_SIZE - 20, SQUARE_SIZE - 20);
        g2d.setColor(Color.RED);
        g2d.drawOval(x + 10, y + 10, SQUARE_SIZE - 20, SQUARE_SIZE - 20);
        
        // Afficher le type de pièce
        g2d.setColor(piece.startsWith("N") ? Color.WHITE : Color.BLACK);
        
        String type = piece.substring(1, 2);
        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);
        
        // Centrer le texte
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textX = x + (SQUARE_SIZE - metrics.stringWidth(type)) / 2;
        int textY = y + ((SQUARE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
        
        g2d.drawString(type, textX, textY);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Escampe");
        Applet applet = new Applet();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(applet);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}