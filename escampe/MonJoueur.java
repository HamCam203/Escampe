package escampe;

import java.util.*;

/**
 * Classe qui implémente l'intelligence artificielle pour le jeu Escampe
 */
public class MonJoueur implements IJoueur {
    // Paramètres pour la gestion du temps
    private static final long MAX_THINKING_TIME = 4500; // 4.5 secondes max pour réfléchir
    private static final int MAX_DEPTH = 6; // Profondeur maximale absolue
    private static final int MIN_DEPTH = 1; // Profondeur minimale garantie
    
    // Poids des heuristiques pour le placement initial
    private static final int POIDS_LISERE = 10;
    private static final int POIDS_POSITION = 5;
    private static final int POIDS_CENTRE = 2;
    private static final int POIDS_CASES_CONTROLEES = 3;
    
    // Poids des heuristiques pour l'évaluation du plateau
    private static final int POIDS_LISERE_PIECES = 4;
    private static final int POIDS_MOBILITE = 3;
    private static final int POIDS_POSITION_STRATEGIQUE = 3;
    private static final int POIDS_CONTROLE_TERRITOIRE = 5;
    
    // Pour le débogage
    private static final boolean DEBUG = true;
    
    // État du jeu
    private EtatJeu etatJeu;
    private static final Random rand = new Random();

    @Override
    public void initJoueur(int mycolour) {
        etatJeu = new EtatJeu(mycolour);
    }

    @Override
    public int getNumJoueur() {
        return etatJeu.getCouleurJoueur();
    }

    @Override
    public String choixMouvement() {
        try {
            if (!etatJeu.isInitialDone()) {
                // Utiliser un placement intelligent au lieu d'un placement fixe
                String placement = placementIntelligent();
                
                // Mettre à jour le plateau avec notre placement
                etatJeu.appliquerPlacementInitial(placement);
                
                return placement;
            }

            // Générer tous les coups légaux en respectant strictement les règles
            List<String> legalMoves = etatJeu.genererCoupsLegaux();
            
            if (legalMoves.isEmpty()) {
                if (DEBUG) {
                    System.out.println("ALERTE: Aucun coup légal trouvé!");
                    etatJeu.afficherPlateau();
                }
                return "PASSE";
            }
            
            // Si un seul coup est possible, le jouer immédiatement
            if (legalMoves.size() == 1) {
                String move = legalMoves.get(0);
                etatJeu.appliquerCoup(move);
                return move;
            }

            // Utiliser l'approfondissement itératif avec contrôle du temps
            String bestMove = rechercheIterative(legalMoves);
            
            // Vérifier une dernière fois que le coup est légal
            if (!etatJeu.estCoupLegal(bestMove)) {
                System.out.println("ERREUR: Le coup choisi n'est pas légal: " + bestMove);
                // Choisir un coup aléatoire parmi les coups légaux
                bestMove = legalMoves.get(rand.nextInt(legalMoves.size()));
                System.out.println("Choix d'un coup aléatoire à la place: " + bestMove);
            }
            
            if (DEBUG) {
                System.out.println("Coup choisi: " + bestMove);
            }
            
            // Appliquer le meilleur coup trouvé
            etatJeu.appliquerCoup(bestMove);
            return bestMove;

        } catch (Exception e) {
            System.err.println("[ERREUR IA] " + e.getMessage());
            e.printStackTrace();
            
            // En cas d'erreur, essayer de jouer un coup légal
            try {
                List<String> legalMoves = etatJeu.genererCoupsLegaux();
                if (!legalMoves.isEmpty()) {
                    String move = legalMoves.get(rand.nextInt(legalMoves.size()));
                    etatJeu.appliquerCoup(move);
                    return move;
                }
            } catch (Exception ex) {
                System.err.println("[ERREUR FATALE] " + ex.getMessage());
                ex.printStackTrace();
            }
            return "PASSE";
        }
    }
    
    /**
     * Recherche itérative qui augmente progressivement la profondeur
     * tout en respectant la contrainte de temps
     */
    private String rechercheIterative(List<String> legalMoves) {
        long startTime = System.currentTimeMillis();
        String bestMove = legalMoves.get(0); // Coup par défaut
        
        // Trier les coups pour optimiser l'élagage alpha-beta
        Collections.sort(legalMoves, (a, b) -> {
            String[] pa = a.split("-");
            String[] pb = b.split("-");
            
            // Vérifier que les coups sont au format valide
            if (pa.length != 2 || pb.length != 2) {
                return 0; // Ignorer les coups mal formatés
            }
            
            // Vérifier que les positions sont valides
            if (pa[0].length() < 2 || pa[1].length() < 2 || 
                pb[0].length() < 2 || pb[1].length() < 2) {
                return 0; // Ignorer les positions mal formatées
            }
            
            try {
                int sra = Integer.parseInt(pa[1].substring(1)) - 1;
                int sca = pa[1].charAt(0) - 'A';
                int srb = Integer.parseInt(pb[1].substring(1)) - 1;
                int scb = pb[1].charAt(0) - 'A';
                
                // Vérifier que les indices sont dans les limites du plateau
                if (sra < 0 || sra >= 6 || sca < 0 || sca >= 6 ||
                    srb < 0 || srb >= 6 || scb < 0 || scb >= 6) {
                    return 0; // Ignorer les positions hors limites
                }
                
                int[][] plateau = etatJeu.getPlateau();
                int[][] typePiece = etatJeu.getTypePiece();
                int couleur = etatJeu.getCouleurJoueur();
                
                // Priorité aux captures de licorne
                boolean captureA = plateau[sra][sca] == -couleur && typePiece[sra][sca] == EtatJeu.LICORNE;
                boolean captureB = plateau[srb][scb] == -couleur && typePiece[srb][scb] == EtatJeu.LICORNE;
                
                if (captureA && !captureB) return -1;
                if (!captureA && captureB) return 1;
                
                // Ensuite, priorité aux positions avec une lisère élevée
                int lisereA = EtatJeu.LISERE[sra][sca];
                int lisereB = EtatJeu.LISERE[srb][scb];
                
                if (lisereA > lisereB) return -1;
                if (lisereA < lisereB) return 1;
                
                // Enfin, priorité aux positions stratégiques
                int valeurA = EtatJeu.VALEUR_POSITION[sra][sca];
                int valeurB = EtatJeu.VALEUR_POSITION[srb][scb];
                
                return valeurB - valeurA;
            } catch (Exception e) {
                System.err.println("Erreur lors du tri des coups: " + e.getMessage());
                return 0; // En cas d'erreur, ne pas modifier l'ordre
            }
        });
        
        // Commencer par une profondeur minimale garantie
        int currentDepth = MIN_DEPTH;
        
        while (currentDepth <= MAX_DEPTH) {
            // Vérifier si on a encore du temps
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > MAX_THINKING_TIME) {
                if (DEBUG) System.out.println("Profondeur atteinte: " + (currentDepth - 1));
                break;
            }
            
            int bestScore = Integer.MIN_VALUE;
            String currentBestMove = null;
            
            for (String move : legalMoves) {
                // Vérifier que le coup est légal avant de l'évaluer
                if (!etatJeu.estCoupLegal(move)) {
                    if (DEBUG) System.out.println("Coup illégal ignoré: " + move);
                    continue;
                }
                
                // Créer une copie du plateau et des types de pièces
                int[][] clonePlateau = EtatJeu.copierPlateau(etatJeu.getPlateau());
                int[][] cloneTypePiece = EtatJeu.copierPlateau(etatJeu.getTypePiece());
                
                try {
                    // Appliquer le coup sur les clones
                    String[] p = move.split("-");
                    int fr = Integer.parseInt(p[0].substring(1)) - 1;
                    int fc = p[0].charAt(0) - 'A';
                    int sr = Integer.parseInt(p[1].substring(1)) - 1;
                    int sc = p[1].charAt(0) - 'A';
                    
                    // Vérifier que les indices sont dans les limites du plateau
                    if (fr < 0 || fr >= 6 || fc < 0 || fc >= 6 ||
                        sr < 0 || sr >= 6 || sc < 0 || sc >= 6) {
                        if (DEBUG) System.out.println("Coup hors limites ignoré: " + move);
                        continue;
                    }
                    
                    cloneTypePiece[sr][sc] = cloneTypePiece[fr][fc];
                    cloneTypePiece[fr][fc] = 0;
                    
                    clonePlateau[sr][sc] = clonePlateau[fr][fc];
                    clonePlateau[fr][fc] = 0;
                    
                    int newLisere = EtatJeu.LISERE[sr][sc];
                    
                    // Évaluer avec alpha-beta pruning
                    int score = alphaBeta(clonePlateau, cloneTypePiece, currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false, etatJeu.getCouleurJoueur(), newLisere, startTime);
                    
                    // Si on a dépassé le temps, arrêter la recherche
                    if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME) {
                        break;
                    }
                    
                    if (score > bestScore) {
                        bestScore = score;
                        currentBestMove = move;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'évaluation du coup " + move + ": " + e.getMessage());
                    continue; // Ignorer ce coup en cas d'erreur
                }
            }
            
            // Si on a trouvé un meilleur coup à cette profondeur, le mémoriser
            if (currentBestMove != null) {
                bestMove = currentBestMove;
            }
            
            // Si on a dépassé le temps, arrêter la recherche
            if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME) {
                break;
            }
            
            currentDepth++;
        }
        
        // Vérifier une dernière fois que le coup est légal
        if (!etatJeu.estCoupLegal(bestMove) && !legalMoves.isEmpty()) {
            System.out.println("ALERTE: Le meilleur coup trouvé n'est pas légal: " + bestMove);
            // Choisir un coup aléatoire parmi les coups légaux
            bestMove = legalMoves.get(rand.nextInt(legalMoves.size()));
            System.out.println("Choix d'un coup aléatoire à la place: " + bestMove);
        }
        
        return bestMove;
    }
    
    /**
     * Algorithme Alpha-Beta avec contrôle du temps
     */
    private int alphaBeta(int[][] board, int[][] types, int depth, int alpha, int beta, boolean maximizing, 
                          int player, int lisere, long startTime) {
        // Vérifier si on a dépassé le temps alloué
        if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME) {
            return maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        
        // Vérifier si la partie est terminée
        boolean licorneNoireTrouvee = false;
        boolean licorneBlancheTrouvee = false;
        
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == EtatJeu.NOIR && types[r][c] == EtatJeu.LICORNE) {
                    licorneNoireTrouvee = true;
                }
                if (board[r][c] == EtatJeu.BLANC && types[r][c] == EtatJeu.LICORNE) {
                    licorneBlancheTrouvee = true;
                }
            }
        }
        
        if (!licorneNoireTrouvee) {
            return player == EtatJeu.BLANC ? 10000 : -10000;
        }
        
        if (!licorneBlancheTrouvee) {
            return player == EtatJeu.NOIR ? 10000 : -10000;
        }
        
        // Cas de base: profondeur atteinte
        if (depth == 0) {
            return heuristique(board, types, player);
        }
        
        // Générer les coups légaux pour le joueur actuel
        List<String> coups = genererCoupsSecurise(board, types, maximizing ? player : -player, lisere);
        if (coups.isEmpty()) {
            // Si aucun coup n'est possible, c'est mauvais pour le joueur actuel
            return 0; // Valeur neutre car le joueur passe son tour
        }
        
        if (maximizing) {
            int value = Integer.MIN_VALUE;
            for (String move : coups) {
                try {
                    // Créer une copie du plateau et des types de pièces
                    int[][] clonePlateau = EtatJeu.copierPlateau(board);
                    int[][] cloneTypePiece = EtatJeu.copierPlateau(types);
                    
                    // Appliquer le coup sur les clones
                    String[] p = move.split("-");
                    int fr = Integer.parseInt(p[0].substring(1)) - 1;
                    int fc = p[0].charAt(0) - 'A';
                    int sr = Integer.parseInt(p[1].substring(1)) - 1;
                    int sc = p[1].charAt(0) - 'A';
                    
                    cloneTypePiece[sr][sc] = cloneTypePiece[fr][fc];
                    cloneTypePiece[fr][fc] = 0;
                    
                    clonePlateau[sr][sc] = clonePlateau[fr][fc];
                    clonePlateau[fr][fc] = 0;
                    
                    int newLisere = EtatJeu.LISERE[sr][sc];
                    
                    value = Math.max(value, alphaBeta(clonePlateau, cloneTypePiece, depth - 1, alpha, beta, false, player, newLisere, startTime));
                    alpha = Math.max(alpha, value);
                    
                    // Élagage alpha-beta
                    if (beta <= alpha) break;
                    
                    // Vérifier le temps
                    if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME) {
                        break;
                    }
                } catch (Exception e) {
                    // Ignorer les coups qui causent des erreurs
                    continue;
                }
            }
            return value;
        } else {
            int value = Integer.MAX_VALUE;
            for (String move : coups) {
                try {
                    // Créer une copie du plateau et des types de pièces
                    int[][] clonePlateau = EtatJeu.copierPlateau(board);
                    int[][] cloneTypePiece = EtatJeu.copierPlateau(types);
                    
                    // Appliquer le coup sur les clones
                    String[] p = move.split("-");
                    int fr = Integer.parseInt(p[0].substring(1)) - 1;
                    int fc = p[0].charAt(0) - 'A';
                    int sr = Integer.parseInt(p[1].substring(1)) - 1;
                    int sc = p[1].charAt(0) - 'A';
                    
                    cloneTypePiece[sr][sc] = cloneTypePiece[fr][fc];
                    cloneTypePiece[fr][fc] = 0;
                    
                    clonePlateau[sr][sc] = clonePlateau[fr][fc];
                    clonePlateau[fr][fc] = 0;
                    
                    int newLisere = EtatJeu.LISERE[sr][sc];
                    
                    value = Math.min(value, alphaBeta(clonePlateau, cloneTypePiece, depth - 1, alpha, beta, true, player, newLisere, startTime));
                    beta = Math.min(beta, value);
                    
                    // Élagage alpha-beta
                    if (beta <= alpha) break;
                    
                    // Vérifier le temps
                    if (System.currentTimeMillis() - startTime > MAX_THINKING_TIME) {
                        break;
                    }
                } catch (Exception e) {
                    // Ignorer les coups qui causent des erreurs
                    continue;
                }
            }
            return value;
        }
    }
    
    /**
     * Version sécurisée de la génération de coups pour éviter les erreurs
     */
    private List<String> genererCoupsSecurise(int[][] board, int[][] types, int joueur, int lisereContr) {
        List<String> moves = new ArrayList<>();
        try {
            boolean hasConstrainedPiece = false;
            if (lisereContr > 0) {
                outer:
                for (int r = 0; r < 6; r++) {
                    for (int c = 0; c < 6; c++) {
                        if (board[r][c] == joueur && EtatJeu.LISERE[r][c] == lisereContr) {
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
                        if (lisereEff > 0 && EtatJeu.LISERE[r][c] != lisereEff) continue;
                        int max = EtatJeu.LISERE[r][c];
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
                                    if (board[checkR][checkC] != EtatJeu.VIDE) {
                                        blocked = true;
                                        break;
                                    }
                                }
                                
                                if (blocked) {
                                    break;
                                }
                                
                                // Vérifier la case d'arrivée
                                if (board[nr][nc] == EtatJeu.VIDE) {
                                    // Case vide, coup valide
                                    moves.add(EtatJeu.toPos(r, c) + "-" + EtatJeu.toPos(nr, nc));
                                } else if (board[nr][nc] == -joueur) {
                                    // Case occupée par une pièce adverse
                                    
                                    // Vérifier si c'est une licorne (seule pièce prenable)
                                    if (types[nr][nc] == EtatJeu.LICORNE) {
                                        // Vérifier que la pièce qui prend est un paladin
                                        if (types[r][c] == EtatJeu.PALADIN) {
                                            // Vérifier que la distance est exactement égale à la portée
                                            if (step == max) {
                                                moves.add(EtatJeu.toPos(r, c) + "-" + EtatJeu.toPos(nr, nc));
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
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération des coups: " + e.getMessage());
            e.printStackTrace();
        }
        return moves;
    }
    
    /**
     * Détermine un placement initial intelligent des pièces
     * @return Une chaîne au format "A1/B2/C3/D4/E5/F6"
     */
    private String placementIntelligent() {
        int startRow, endRow;
        int couleur = etatJeu.getCouleurJoueur();
        
        // Déterminer les lignes où nous pouvons placer nos pièces
        if (couleur == EtatJeu.NOIR) {
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
        
        // La meilleure position est pour la licorne
        PositionEvaluee licornePos = evaluations.get(0);
        
        // Sélectionner les 6 meilleures positions (licorne + 5 paladins)
        StringBuilder placement = new StringBuilder();
        
        // Placer la licorne en premier
        placement.append(EtatJeu.toPos(licornePos.row, licornePos.col));
        
        // Placer les paladins ensuite
        for (int i = 1; i < 6; i++) {
            PositionEvaluee pe = evaluations.get(i);
            placement.append("/").append(EtatJeu.toPos(pe.row, pe.col));
        }
        
        return placement.toString();
    }
    
    /**
     * Évalue la valeur stratégique d'une position pour le placement initial
     */
    private int evaluerPositionInitiale(int row, int col) {
        int score = 0;
        
        // 1. Valeur de la lisère (poids x10)
        // Plus la lisère est élevée, plus la pièce peut se déplacer loin
        score += EtatJeu.LISERE[row][col] * POIDS_LISERE;
        
        // 2. Position stratégique (poids x5)
        // Utilise la matrice de valeurs prédéfinies
        score += EtatJeu.VALEUR_POSITION[row][col] * POIDS_POSITION;
        
        // 3. Proximité du centre (poids x2)
        // Le centre du plateau est à (2.5, 2.5), donc on calcule la distance
        double distanceCentreX = Math.abs(col - 2.5);
        double distanceCentreY = Math.abs(row - 2.5);
        double distanceCentre = Math.sqrt(distanceCentreX*distanceCentreX + distanceCentreY*distanceCentreY);
        // Convertir la distance en score (plus c'est proche, plus le score est élevé)
        score += (4 - distanceCentre) * POIDS_CENTRE;
        
        // 4. Cases contrôlées (poids x3)
        // Nombre de cases que la pièce peut atteindre
        score += calculerCasesControlees(row, col) * POIDS_CASES_CONTROLEES;
        
        return score;
    }
    
    /**
     * Calcule combien de cases une pièce peut contrôler depuis cette position
     */
    private int calculerCasesControlees(int row, int col) {
        int count = 0;
        int max = EtatJeu.LISERE[row][col];
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

    /**
     * Fonction d'évaluation heuristique pour un état de plateau
     */
    private int heuristique(int[][] board, int[][] types, int player) {
        int score = 0;
        
        // Bonus très élevé si la licorne adverse est capturée
        boolean licorneNoireTrouvee = false;
        boolean licorneBlancheTrouvee = false;
        
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == EtatJeu.NOIR && types[r][c] == EtatJeu.LICORNE) {
                    licorneNoireTrouvee = true;
                }
                if (board[r][c] == EtatJeu.BLANC && types[r][c] == EtatJeu.LICORNE) {
                    licorneBlancheTrouvee = true;
                }
            }
        }
        
        if (!licorneNoireTrouvee && player == EtatJeu.BLANC) return 10000;
        if (!licorneBlancheTrouvee && player == EtatJeu.NOIR) return 10000;
        if (!licorneNoireTrouvee && player == EtatJeu.NOIR) return -10000;
        if (!licorneBlancheTrouvee && player == EtatJeu.BLANC) return -10000;
        
        // Variables pour le contrôle du territoire
        int controleSelf = 0;
        int controleOpponent = 0;
        
        // Carte de chaleur pour le contrôle du territoire
        int[][] controleMap = new int[6][6];
        
        // Évaluer chaque pièce sur le plateau
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (board[r][c] == player) {
                    // 1. Bonus pour les pièces avec une lisère élevée
                    score += EtatJeu.LISERE[r][c] * POIDS_LISERE_PIECES;
                    
                    // 2. Bonus pour la position stratégique
                    score += EtatJeu.VALEUR_POSITION[r][c] * POIDS_POSITION_STRATEGIQUE;
                    
                    // 3. Bonus supplémentaire pour la licorne
                    if (types[r][c] == EtatJeu.LICORNE) {
                        score += 50; // Valeur élevée pour protéger la licorne
                    }
                    
                    // 4. Calculer la mobilité et le contrôle du territoire
                    int casesControlees = 0;
                    int max = EtatJeu.LISERE[r][c];
                    int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                    
                    for (int[] d : dirs) {
                        int nr = r, nc = c;
                        for (int step = 1; step <= max; step++) {
                            nr += d[0];
                            nc += d[1];
                            if (nr >= 0 && nr < 6 && nc >= 0 && nc < 6) {
                                casesControlees++;
                                // Marquer cette case comme contrôlée par le joueur
                                controleMap[nr][nc]++;
                                
                                // Bonus si un paladin peut capturer la licorne adverse
                                if (types[r][c] == EtatJeu.PALADIN && 
                                    board[nr][nc] == -player && 
                                    types[nr][nc] == EtatJeu.LICORNE && 
                                    step == max) {
                                    score += 500; // Très forte valeur pour la capture potentielle
                                }
                            }
                        }
                    }
                    
                    controleSelf += casesControlees;
                    score += casesControlees * POIDS_MOBILITE;
                } 
                else if (board[r][c] == -player) {
                    // Évaluer le contrôle de l'adversaire
                    int casesControlees = 0;
                    int max = EtatJeu.LISERE[r][c];
                    int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                    
                    for (int[] d : dirs) {
                        int nr = r, nc = c;
                        for (int step = 1; step <= max; step++) {
                            nr += d[0];
                            nc += d[1];
                            if (nr >= 0 && nr < 6 && nc >= 0 && nc < 6) {
                                casesControlees++;
                                // Marquer cette case comme contrôlée par l'adversaire
                                controleMap[nr][nc]--;
                                
                                // Malus si un paladin adverse peut capturer notre licorne
                                if (types[r][c] == EtatJeu.PALADIN && 
                                    board[nr][nc] == player && 
                                    types[nr][nc] == EtatJeu.LICORNE && 
                                    step == max) {
                                    score -= 500; // Très forte pénalité pour la capture potentielle
                                }
                            }
                        }
                    }
                    
                    controleOpponent += casesControlees;
                }
            }
        }
        
        // 5. Évaluer le contrôle global du territoire
        int controleTerritory = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (controleMap[r][c] > 0) {
                    // Case dominée par le joueur
                    controleTerritory += controleMap[r][c];
                } else if (controleMap[r][c] < 0) {
                    // Case dominée par l'adversaire
                    controleTerritory += controleMap[r][c];
                }
            }
        }
        
        // Ajouter le score de contrôle du territoire
        score += controleTerritory * POIDS_CONTROLE_TERRITOIRE;
        
        // 6. Différence de mobilité
        score += (controleSelf - controleOpponent) * POIDS_MOBILITE;
        
        return score;
    }

    @Override
    public void mouvementEnnemi(String coup) {
        etatJeu.appliquerCoupAdversaire(coup);
    }

    @Override
    public void declareLeVainqueur(int couleurGagnant) {
        if (couleurGagnant == etatJeu.getCouleurJoueur()) System.out.println("J'ai gagné !");
        else if (couleurGagnant == 0) System.out.println("Match nul !");
        else System.out.println("J'ai perdu !");
    }

    @Override
    public String binoName() {
        return "MonEquipe";
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