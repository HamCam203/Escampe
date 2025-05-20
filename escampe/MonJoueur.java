package escampe;

import java.util.*;

public class MonJoueur implements IJoueur {
    private int couleur;               // BLANC (-1) ou NOIR (1)
    private int[][] plateau;           // 6×6 board: -1=white, 1=black, 0=empty
    private static final int BLANC = IJoueur.BLANC;
    private static final int NOIR  = IJoueur.NOIR;
    private static final int VIDE  = 0;

    // Carte des lisères (portée)
    private static final int[][] LISERE = {
        {1,2,2,3,1,2},
        {3,1,3,1,3,2},
        {2,3,1,2,1,3},
        {2,1,3,2,3,1},
        {1,3,1,3,1,2},
        {3,2,2,1,3,2}
    };

    private int lastLisere      = 0;   // contrainte héritée du coup ennemi
    private boolean initialDone = false;
    private static final Random rand = new Random();

    public MonJoueur() {
        plateau = new int[6][6];
    }

    @Override
    public void initJoueur(int mycolour) {
        couleur      = mycolour;
        lastLisere   = 0;
        initialDone  = false;
        for (int i = 0; i < 6; i++) Arrays.fill(plateau[i], VIDE);
    }

    @Override
    public int getNumJoueur() {
        return couleur;
    }

    @Override
    public String choixMouvement() {
        try {
            // --- Phase de placement initial ---
            if (!initialDone) {
                initialDone = true;
                String placement;
                if (couleur == NOIR) {
                    placement = "A1/A2/B1/B2/C1/C2";
                } else {
                    placement = "A5/A6/B5/B6/C5/C6";
                }
                for (String pos : placement.split("/")) {
                    int c = pos.charAt(0) - 'A';
                    int r = Integer.parseInt(pos.substring(1)) - 1;
                    plateau[r][c] = couleur;
                }
                return placement;
            }

            // --- Phase de mouvement ---
            boolean hasConstrained = false;
            if (lastLisere > 0) {
                outer:
                for (int r = 0; r < 6; r++) {
                    for (int c = 0; c < 6; c++) {
                        if (plateau[r][c] == couleur && LISERE[r][c] == lastLisere) {
                            hasConstrained = true;
                            break outer;
                        }
                    }
                }
            }
            int effectiveLisere = hasConstrained ? lastLisere : 0;

            List<String> legal = new ArrayList<>();
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    if (plateau[r][c] == couleur) {
                        if (effectiveLisere > 0 && LISERE[r][c] != effectiveLisere) continue;
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
                                if (plateau[nr][nc] != VIDE &&
                                    !(plateau[nr][nc] == -couleur && step == max)) {
                                    blocked = true; break;
                                }
                            }
                            if (!blocked) {
                                legal.add(toPos(r, c) + "-" + toPos(nr, nc));
                            }
                        }
                    }
                }
            }

            if (legal.isEmpty()) {
                return "E";  // Pas de coup possible
            } else {
                String coup = legal.get(rand.nextInt(legal.size()));
                String[] p = coup.split("-");
                int fr = Integer.parseInt(p[0].substring(1)) - 1;
                int fc = p[0].charAt(0) - 'A';
                int sr = Integer.parseInt(p[1].substring(1)) - 1;
                int sc = p[1].charAt(0) - 'A';
                plateau[sr][sc] = plateau[fr][fc];
                plateau[fr][fc] = VIDE;
                lastLisere = LISERE[sr][sc];
                return coup;
            }

        } catch (Exception e) {
            System.err.println("[ERREUR] Exception pendant choixMouvement: " + e.getMessage());
            return "E"; // Fail-safe : éviter tout timeout
        }
    }

    @Override
    public void mouvementEnnemi(String coup) {
        if ("E".equals(coup)) {
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
    public void declareLeVainqueur(int colour) {
        if (colour == couleur)      System.out.println("J'ai gagné !");
        else if (colour == 0)       System.out.println("Match nul !");
        else                        System.out.println("J'ai perdu !");
    }

    @Override
    public String binoName() {
        return "MonEquipe";
    }

    private String toPos(int r, int c) {
        return "" + (char)('A' + c) + (r + 1);
    }
}
