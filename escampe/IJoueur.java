package escampe;

/**
 * Interface définissant les méthodes que vous devez implanter pour
 * participer au tournoi Escampe et permettre à ClientJeu de communiquer
 * avec votre IA.
 */
public interface IJoueur {
    
    /**
     * Initialise votre joueur avec la couleur qui lui est attribuée.
     * @param couleur "noir" ou "blanc" selon votre couleur.
     */
    public void initJoueur(String couleur);
    
    /**
     * Indique le nom de l'adversaire.
     * @param nomAdversaire Le nom de votre adversaire.
     */
    public void nomAdversaire(String nomAdversaire);
    
    /**
     * Demande au joueur de jouer un coup.
     * @param etatDuJeu L'état actuel du jeu sous forme de chaîne de caractères.
     * @return Le coup joué sous forme de chaîne de caractères.
     */
    public String jouerCoup(String etatDuJeu);
    
    /**
     * Informe votre joueur du coup joué par l'adversaire.
     * @param coup Le coup joué par l'adversaire.
     */
    public void coupAdversaire(String coup);
    
    /**
     * Informe votre joueur de la fin de la partie.
     * @param result Le résultat de la partie ("gagne", "perd", "nul").
     */
    public void finPartie(String result);
}