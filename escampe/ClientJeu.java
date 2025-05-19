package escampe;

import java.net.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Client réseau permettant de faire circuler les coups sous forme de messages
 * vers et depuis l'arbitre via une connexion réseau.
 */
public class ClientJeu {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private IJoueur joueur;
    
    /**
     * Charge dynamiquement la classe de joueur et établit une connexion avec le serveur
     */
    public ClientJeu(String nomClasseJoueur, String hote, int port) throws Exception {
        // Charger la classe de joueur
        Class<?> classeJoueur = Class.forName(nomClasseJoueur);
        Constructor<?> constructeur = classeJoueur.getConstructor();
        joueur = (IJoueur) constructeur.newInstance();
        
        // Se connecter au serveur
        socket = new Socket(hote, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        System.out.println("Connexion établie avec " + hote + ":" + port);
        System.out.println("Joueur chargé : " + nomClasseJoueur);
    }
    
    /**
     * Boucle principale du client
     */
    public void jouer() throws IOException {
        String message;
        String[] parties;
        String couleur = "";
        String nomAdversaire = "";
        String position = "";
        String coup;
        
        while ((message = in.readLine()) != null) {
            System.out.println("Reçu: " + message);
            
            parties = message.split(" ");
            String commande = parties[0];
            
            switch (commande) {
                case "INIT":
                    // Initialisation du joueur
                    couleur = parties[1];
                    nomAdversaire = parties[2];
                    joueur.initJoueur(couleur);
                    joueur.nomAdversaire(nomAdversaire);
                    System.out.println("Joueur initialisé, couleur: " + couleur + ", adversaire: " + nomAdversaire);
                    break;
                    
                case "JOUER":
                    // Demande de jouer un coup
                    position = parties[1];
                    coup = joueur.jouerCoup(position);
                    out.println(coup);
                    System.out.println("Coup joué: " + coup);
                    break;
                    
                case "COUP":
                    // Notification d'un coup de l'adversaire
                    String coupAdversaire = parties[1];
                    joueur.coupAdversaire(coupAdversaire);
                    System.out.println("Coup adversaire reçu: " + coupAdversaire);
                    break;
                    
                case "FINPARTIE":
                    // Fin de partie
                    String resultat = parties[1];
                    joueur.finPartie(resultat);
                    System.out.println("Partie terminée, résultat: " + resultat);
                    break;
                    
                case "SORTIR":
                    // Demande de sortie
                    System.out.println("Demande de sortie reçue, fermeture de la connexion.");
                    socket.close();
                    return;
                    
                default:
                    System.out.println("Commande inconnue: " + commande);
                    break;
            }
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java escampe.ClientJeu <classeJoueur> <hote> <port>");
            System.out.println("Exemple: java escampe.ClientJeu escampe.MonIA localhost 1234");
            System.exit(1);
        }
        
        String classeJoueur = args[0];
        String hote = args[1];
        int port = Integer.parseInt(args[2]);
        
        try {
            ClientJeu client = new ClientJeu(classeJoueur, hote, port);
            client.jouer();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}