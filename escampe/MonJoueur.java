package escampe;

public class MonJoueur implements IJoueur {
    private int couleur;
    private int[][] plateau = new int[6][6];
    
    public MonJoueur() {
        // Initialisation
    }
    
    @Override
    public void initJoueur(int mycolour) {
        couleur = mycolour;
        // Réinitialiser le plateau
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++)
                plateau[i][j] = 0;
    }
    
    @Override
    public int getNumJoueur() {
        return couleur;
    }
    
    @Override
    public String choixMouvement() {
        // Logique simple pour tester
        return "A1-A2";
    }
    
    @Override
    public void declareLeVainqueur(int colour) {
        // Afficher le vainqueur
    }
    
    @Override
    public void mouvementEnnemi(String coup) {
        // Traiter le coup ennemi
    }
    
    @Override
    public String binoName() {
        return "MonEquipe";
    }
}