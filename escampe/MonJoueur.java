package escampe;

public class MonJoueur implements IJoueur {
    private int couleur; // BLANC (-1) ou NOIR (1)
    private int[][] plateau; // représentation du plateau
    
    // Constantes définies dans l'interface IJoueur
    // Vous pouvez les redéfinir ici pour plus de clarté
    private static final int BLANC = IJoueur.BLANC;
    private static final int NOIR = IJoueur.NOIR;
    private static final int VIDE = 0;
    
    public MonJoueur() {
        // Initialisation
        plateau = new int[6][6];
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++)
                plateau[i][j] = VIDE;
    }
    
    @Override
    public void initJoueur(int mycolour) {
        couleur = mycolour;
        // Réinitialiser le plateau
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++)
                plateau[i][j] = VIDE;
    }
    
    @Override
    public int getNumJoueur() {
        return couleur;
    }
    
    @Override
    public String choixMouvement() {
        // Implémentez votre algorithme de décision ici
        // Retournez un coup au format "A1-B2" ou "PASSE"
        
        // Exemple simple pour le placement initial des pièces noires
        if (couleur == NOIR && estPlateauVide()) {
            return "A1-A1"; // Placement des pièces noires
        }
        
        // Exemple simple pour le placement initial des pièces blanches
        if (couleur == BLANC && estPremierCoupBlanc()) {
            return "F6-F6"; // Placement des pièces blanches
        }
        
        // Logique pour les coups suivants
        // ...
        
        return "PASSE"; // Si aucun coup n'est possible
    }
    
    @Override
    public void declareLeVainqueur(int colour) {
        if (colour == couleur)
            System.out.println("J'ai gagné !");
        else if (colour == 0)
            System.out.println("Match nul !");
        else
            System.out.println("J'ai perdu !");
    }
    
    @Override
    public void mouvementEnnemi(String coup) {
        // Mettre à jour votre représentation du plateau
        // en fonction du coup joué par l'adversaire
        
        // Exemple: parser le coup "A1-B2"
        if (!coup.equals("PASSE")) {
            String[] positions = coup.split("-");
            int colDepart = positions[0].charAt(0) - 'A';
            int ligDepart = Integer.parseInt(positions[0].substring(1)) - 1;
            int colArrivee = positions[1].charAt(0) - 'A';
            int ligArrivee = Integer.parseInt(positions[1].substring(1)) - 1;
            
            // Mettre à jour le plateau
            plateau[ligArrivee][colArrivee] = plateau[ligDepart][colDepart];
            plateau[ligDepart][colDepart] = VIDE;
        }
    }
    
    @Override
    public String binoName() {
        return "MonEquipe"; // Remplacez par le nom de votre équipe
    }
    
    // Méthodes utilitaires
    private boolean estPlateauVide() {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++)
                if (plateau[i][j] != VIDE)
                    return false;
        return true;
    }
    
    private boolean estPremierCoupBlanc() {
        // Vérifier si c'est le premier coup du joueur blanc
        int compteurPieces = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (plateau[i][j] != VIDE)
                    compteurPieces++;
            }
        }
        // Si on a exactement 4 pièces (les 4 pièces noires), c'est le premier coup blanc
        return compteurPieces == 4;
    }
}