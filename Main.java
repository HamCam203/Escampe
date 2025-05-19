import java.util.*;

/**
 * Classe principale pour le jeu d'Escampe
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Initialiser les composants du jeu
        EscampeBoard board = new EscampeBoard();
        GameController gameController = new GameController(board);
        AIPlayer aiPlayer = new AIPlayer(board, gameController);
        
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
        System.out.println("2. Joueur contre IA (vous êtes Noir)");
        
        int gameMode = 0;
        while (gameMode < 1 || gameMode > 2) {
            System.out.print("Votre choix (1-2): ");
            try {
                gameMode = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide. Veuillez entrer un nombre entre 1 et 2.");
            }
        }
        
        // Phase 1: Placement initial des pièces
        System.out.println("=== PHASE 1: PLACEMENT INITIAL DES PIÈCES ===");
        
        // Le joueur noir choisit un bord et place ses pièces
        System.out.println("Joueur NOIR: Placez vos pièces (format: C6/A6/B5/D5/E6/F5)");
        System.out.println("Le premier emplacement est pour la licorne, les autres pour les paladins");
        System.out.println("Vous pouvez choisir le bord haut (lignes 5-6) ou bas (lignes 1-2)");
        
        String placementNoir;
        do {
            System.out.print("Placement NOIR: ");
            placementNoir = scanner.nextLine().toUpperCase();
            if (!gameController.isValidMove(placementNoir, "noir")) {
                System.out.println("Placement invalide! Réessayez.");
            }
        } while (!gameController.isValidMove(placementNoir, "noir"));
        
        gameController.play(placementNoir, "noir");
        board.displayBoard();
        
        // Le joueur blanc place ses pièces sur le bord opposé
        if (gameMode == 1) {
            System.out.println("Joueur BLANC: Placez vos pièces (format: C1/A1/B2/D2/E1/F2)");
            System.out.println("Le premier emplacement est pour la licorne, les autres pour les paladins");
            
            String placementBlanc;
            do {
                System.out.print("Placement BLANC: ");
                placementBlanc = scanner.nextLine().toUpperCase();
                if (!gameController.isValidMove(placementBlanc, "blanc")) {
                    System.out.println("Placement invalide! Réessayez.");
                }
            } while (!gameController.isValidMove(placementBlanc, "blanc"));
            
            gameController.play(placementBlanc, "blanc");
        } else {
            // L'IA place les pièces blanches
            // Déterminer le bord choisi par le joueur noir
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
            
            String placementBlanc;
            if (noirTopBorder) {
                placementBlanc = "C1/A1/B2/D2/E1/F2"; // Placement en bas si noir est en haut
            } else {
                placementBlanc = "C6/A6/B5/D5/E6/F5"; // Placement en haut si noir est en bas
            }
            
            System.out.println("IA (BLANC) place ses pièces: " + placementBlanc);
            gameController.play(placementBlanc, "blanc");
        }
        
        board.displayBoard();
        
        // Phase 2: Jeu principal
        System.out.println("=== PHASE 2: JEU PRINCIPAL ===");
        
        String currentPlayer = "blanc"; // Le blanc commence
        
        while (!gameController.gameOver()) {
            System.out.println("\nTour du joueur " + currentPlayer.toUpperCase());
            
            // Afficher les coups possibles
            String[] possibleMoves = gameController.possiblesMoves(currentPlayer);
            System.out.println("Coups possibles: " + Arrays.toString(possibleMoves));
            
            String move;
            
            // Déterminer si c'est un joueur humain ou l'IA qui joue
            boolean isHuman = (gameMode == 1) || (gameMode == 2 && currentPlayer.equals("noir"));
            
            if (isHuman) {
                // Tour du joueur humain
                if (possibleMoves.length == 1 && possibleMoves[0].equals("E")) {
                    System.out.println("Aucun coup possible. Vous passez votre tour (E).");
                    move = "E";
                } else {
                    do {
                        System.out.print("Votre coup (format A1-B1 ou E pour passer): ");
                        move = scanner.nextLine().toUpperCase();
                        if (!gameController.isValidMove(move, currentPlayer)) {
                            System.out.println("Coup invalide! Réessayez.");
                        }
                    } while (!gameController.isValidMove(move, currentPlayer));
                }
            } else {
                // Tour de l'IA
                System.out.println("L'IA réfléchit...");
                move = aiPlayer.findBestMove(currentPlayer);
                System.out.println("L'IA joue: " + move);
            }
            
            // Jouer le coup
            gameController.play(move, currentPlayer);
            
            // Afficher le plateau après le coup
            board.displayBoard();
            
            // Vérifier si la partie est terminée après ce coup
            if (gameController.gameOver()) {
                break;
            }
            
            // Changer de joueur
            currentPlayer = currentPlayer.equals("blanc") ? "noir" : "blanc";
        }
        
        // Fin de partie
        System.out.println("\n=== FIN DE PARTIE ===");
        String winner = gameController.getWinner();
        System.out.println("Le joueur " + winner.toUpperCase() + " a gagné!");
        
        scanner.close();
    }
}
