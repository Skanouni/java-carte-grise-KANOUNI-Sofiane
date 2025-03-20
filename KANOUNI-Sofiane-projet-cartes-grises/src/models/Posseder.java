package models;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Classe représentant la table "POSSEDER" de la base de données.
 * Les noms des attributs correspondent aux colonnes de la table.
 */
public class Posseder {
    private int id_proprietaire; // Identifiant unique du propriétaire (clé étrangère)
    private int id_vehicule; // Identifiant du véhicule associé (clé étrangère)
    private String prenom;
    private String nom;
    private String matricule;
    private Date date_debut_propriete; // Date de début de la propriété du véhicule
    private Date date_fin_propriete; // Date de fin de la propriété du véhicule

    // Constructeur
    public Posseder(int id_proprietaire, int id_vehicule, String prenom, String nom,  String matricule, Date date_debut_propriete, Date date_fin_propriete) {
        this.id_proprietaire = id_proprietaire;
        this.id_vehicule = id_vehicule;
        this.prenom = prenom;
        this.nom = nom;
        this.matricule = matricule;
        this.date_debut_propriete = date_debut_propriete;
        this.date_fin_propriete = date_fin_propriete;
    }

    // Getters et Setters
    public int getIdProprietaire() {
        return id_proprietaire;
    }

    public void setIdProprietaire(int id_proprietaire) {
        this.id_proprietaire = id_proprietaire;
    }


    public String getMatricule() {
        return matricule; // Retourne le matricule du véhicule
    }


    public String getPrenom() {
        return prenom;
    }


    public String getNom() {
        return nom;
    }

    public int getIdVehicule() {
        return id_vehicule;
    }

    public void setIdVehicule(int id_vehicule) {
        this.id_vehicule = id_vehicule;
    }

    public Date getDateDebutPropriete() {
        return date_debut_propriete;
    }

    public void setDateDebutPropriete(Date date_debut_propriete) {
        this.date_debut_propriete = date_debut_propriete;
    }

    public Date getDateFinPropriete() {
        return date_fin_propriete;
    }

    public void setDateFinPropriete(Date date_fin_propriete) {
        this.date_fin_propriete = date_fin_propriete;
    }

    // Méthode toString() : Affiche un résumé des informations du propriétaire et du véhicule.
    @Override
    public String toString() {
        return "Propriétaire ID: " + id_proprietaire + ", Véhicule ID: " + id_vehicule;
    }

    // Méthode pour vérifier si la propriété du véhicule est encore valide
    public boolean estProprietaireActuel() {
        Date today = new Date(); // Récupère la date actuelle
        
        // Vérifie si la date actuelle est après la date de début de propriété et avant la date de fin de propriété
        return today.after(date_debut_propriete) && (date_fin_propriete == null || today.before(date_fin_propriete));
    }

    public Map<String, List<String>> getPrenomsAndNoms() {
        return getPrenomsAndNoms(); // Appel à la base de données
    }

    public List<String> getAllMatricules() {
        return getAllMatricules(); // Méthode à implémenter dans le DAO
    }



}
