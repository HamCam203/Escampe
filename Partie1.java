public interface Partie1 {

    /** Initialise un plateau à partir d'un fichier texte.
     * @param fileName le nom du fichier à lire
     */
    public void setFromFile(String fileName);

    /** Sauvegarde la configuration de l'état courant dans un fichier.
     * @param fileName le nom du fichier à sauvegarder
     */
    public void saveToFile(String fileName);

    /** Indique si le coup est valide pour le joueur.
     * @param move le coup à jouer (ex: "B1-D1" ou "C6/A6/B5/D5/E6/F5")
     * @param player le joueur, "noir" ou "blanc"
     * @return true si le coup est valide, false sinon
     */
    public boolean isValidMove(String move, String player);

    /** Retourne les coups possibles pour le joueur.
     * @param player le joueur, "noir" ou "blanc"
     * @return tableau de coups possibles
     */
    public String[] possiblesMoves(String player);

    /** Modifie le plateau en jouant le coup spécifié.
     * @param move le coup à jouer
     * @param player le joueur, "noir" ou "blanc"
     */
    public void play(String move, String player);

    /** Retourne vrai si le jeu est terminé.
     * @return true si fin de partie, false sinon
     */
    public boolean gameOver();
}
