package escampe;

import java.util.*;

public class MonJoueur implements IJoueur {
    private int couleur; // BLANC (-1) ou NOIR (1)
    private int[][] plateau; // 6x6 board: -1=white,1=black,0=empty
    private static final int BLANC = IJoueur.BLANC;
    private static final int NOIR = IJoueur.NOIR;
    private static final int VIDE = 0;

    // Lisère mapping: 1=simple, 2=double, 3=triple
    private static final int[][] LISERE = {
        {1, 2, 2, 3, 1, 2},
        {3, 1, 3, 1, 3, 2},
        {2, 3, 1, 2, 1, 3},
        {2, 1, 3, 2, 3, 1},
        {1, 3, 1, 3, 1, 2},
        {3, 2, 2, 1, 3, 2}
    };

    private int lastLisere = 0;
    private boolean initialDone = false;

    public MonJoueur() {
        plateau = new int[6][6];
    }

    @Override
    public void initJoueur(int mycolour) {
        couleur = mycolour;
        for (int i = 0; i < 6; i++) {
            Arrays.fill(plateau[i], VIDE);
        }
        lastLisere = 0;
        initialDone = false;
    }

    @Override
    public int getNumJoueur() {
        return couleur;
    }

    @Override
    public String choixMouvement() {
        // -- Placement initial (une seule fois) --
        if (!initialDone) {
            initialDone = true;
            if (couleur == NOIR) {
                // Noir place sur lignes 1–2
                return "A1/A2/B1/B2/C1/C2";
            } else {
                // Blanc place en miroir sur lignes 5–6
                return "A5/A6/B5/B6/C5/C6";
            }
        }

        // -- Phase de mouvement --
        List<String> legal = new ArrayList<>();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (plateau[r][c] == couleur) {
                    // Respecter la lisère si elle est contraignante
                    if (lastLisere > 0 && LISERE[r][c] != lastLisere) {
                        continue;
                    }
                    int max = LISERE[r][c];
                    int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                    for (int[] d : dirs) {
                        int nr = r, nc = c;
                        boolean blocked = false;
                        for (int step = 1; step <= max; step++) {
                            nr += d[0];
                            nc += d[1];
                            // Hors plateau ?
                            if (nr < 0 || nr >= 6 || nc < 0 || nc >= 6) {
                                blocked = true;
                                break;
                            }
                            // Case occupée (sauf capture en dernière case)
                            if (plateau[nr][nc] != VIDE
                                && !(plateau[nr][nc] == -couleur && step == max)) {
                                blocked = true;
                                break;
                            }
                        }
                        if (!blocked) {
                            legal.add(toPos(r, c) + "-" + toPos(nr, nc));
                        }
                    }
                }
            }
        }

        // Si aucun coup possible, on passe
        if (legal.isEmpty()) {
            return "PASSE";
        }
        // Sinon, on choisit un coup aléatoire parmi les légaux
        return legal.get(new Random().nextInt(legal.size()));
    }

    @Override
    public void mouvementEnnemi(String coup) {
        // Remise à zéro si passe
        if ("PASSE".equals(coup)) {
            lastLisere = 0;
            return;
        }
        // Ignorer le placement initial (séparateur '/')
        if (coup.contains("/")) {
            return;
        }
        String[] p = coup.split("-");
        int fr = Integer.parseInt(p[0].substring(1)) - 1;
        int fc = p[0].charAt(0) - 'A';
        int sr = Integer.parseInt(p[1].substring(1)) - 1;
        int sc = p[1].charAt(0) - 'A';
        // Bouger la pièce et libérer l'ancienne case
        plateau[sr][sc] = plateau[fr][fc];
        plateau[fr][fc] = VIDE;
        // Mettre à jour la lisère pour le prochain tour
        lastLisere = LISERE[sr][sc];
    }

    @Override
    public void declareLeVainqueur(int colour) {
        if (colour == couleur) {
            System.out.println("J'ai gagné !");
        } else if (colour == 0) {
            System.out.println("Match nul !");
        } else {
            System.out.println("J'ai perdu !");
        }
    }

    @Override
    public String binoName() {
        return "MonEquipe";
    }

    // Conversion indice → position (ex. 0,0 → "A1")
    private String toPos(int r, int c) {
        return "" + (char)('A' + c) + (r + 1);
    }
}
