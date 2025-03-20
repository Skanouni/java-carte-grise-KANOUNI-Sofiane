package controllers;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import models.Posseder;

/**
 * Contrôleur pour gérer les propriétés (relation entre propriétaires et
 * véhicules) dans la base de données.
 */
public class PossederController {

    // Récupérer toutes les relations propriétaire-véhicule avec jointures
    public List<Posseder> getAllPossessions() {
        List<Posseder> possessions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT p.id_proprietaire, p.prenom, p.nom, v.id_vehicule, v.matricule, pos.date_debut_propriete, pos.date_fin_propriete "
                                +
                                "FROM proprietaire p " +
                                "JOIN posseder pos ON p.id_proprietaire = pos.id_proprietaire " +
                                "JOIN vehicule v ON pos.id_vehicule = v.id_vehicule")) {

            while (rs.next()) {
                Posseder possession = new Posseder(
                        rs.getInt("id_proprietaire"),
                        rs.getInt("id_vehicule"),
                        rs.getString("prenom"),
                        rs.getString("nom"),
                        rs.getString("matricule"),
                        rs.getDate("date_debut_propriete"),
                        rs.getDate("date_fin_propriete"));
                possessions.add(possession);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return possessions;
    }

    // Ajouter une nouvelle relation propriétaire-véhicule avec vérification des
    // doublons
    public void addPossession(String prenom, String nom, String matricule, Date dateDebut) {
        int idProprietaire = getProprietaireIdByName(prenom, nom);
        int idVehicule = getVehiculeIdByMatricule(matricule);


        // Vérifier si la nouvelle relation existe déjà
        if (existVehicule(idVehicule)) {
            JOptionPane.showMessageDialog(null, "Erreur : Ce véhicule existe déjà.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (existsPossession(idProprietaire, idVehicule)) {
            JOptionPane.showMessageDialog(null, "Erreur : Cette relation existe déjà.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO posseder (id_proprietaire, id_vehicule, date_debut_propriete) VALUES (?, ?, ?)")) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);
            ps.setDate(3, dateDebut);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de l'ajout de la propriété.");
        }
    }

    public void updatePossession(
        String newPrenom, String newNom, String newMatricule,
        Date newDateFin, String oldPrenom, String oldNom, String oldMatricule) {

    // Récupérer les anciens ID
    int oldIdProprietaire = getProprietaireIdByName(oldPrenom, oldNom);
    int oldIdVehicule = getVehiculeIdByMatricule(oldMatricule);

    // Vérifier que les anciens IDs existent
    if (oldIdProprietaire == -1 || oldIdVehicule == -1) {
        JOptionPane.showMessageDialog(null, "Ancien propriétaire ou véhicule introuvable.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Récupérer les nouveaux ID automatiquement
    int newIdProprietaire = getProprietaireIdByName(newPrenom, newNom);
    int newIdVehicule = getVehiculeIdByMatricule(newMatricule);

    // Vérifier que les nouveaux IDs existent
    if (newIdProprietaire == -1 || newIdVehicule == -1) {
        JOptionPane.showMessageDialog(null, "Nouveau propriétaire ou véhicule introuvable.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
        return;
    }


        // Vérifier si la nouvelle relation existe déjà
    if (existVehicule(newIdVehicule)) {
        JOptionPane.showMessageDialog(null, "Erreur : Ce véhicule existe déjà.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Vérifier si la nouvelle relation existe déjà
    if (existsPossession(newIdProprietaire, newIdVehicule)) {
        JOptionPane.showMessageDialog(null, "Erreur : Cette relation existe déjà.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
        return;
    }



    try (Connection conn = DatabaseConnection.getConnection()) {

        //  Récupérer la valeur de date_debut_propriete avant modification
        String selectSQL = "SELECT date_debut_propriete FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?";
        Date oldDateDebut = null;
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setInt(1, oldIdProprietaire);
            selectStmt.setInt(2, oldIdVehicule);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                oldDateDebut = rs.getDate("date_debut_propriete"); // Récupérer la date
            }
        }

        // Vérifier si une date de début a été trouvée
        if (oldDateDebut == null) {
            JOptionPane.showMessageDialog(null, "La relation avec ces identifiants n'existe pas.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //  Supprimer l'ancienne entrée
        String deleteSQL = "DELETE FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
            deleteStmt.setInt(1, oldIdProprietaire);
            deleteStmt.setInt(2, oldIdVehicule);
            deleteStmt.executeUpdate();
        }

        // Insertion de la nouvelle entrée avec les nouvelles clés et date de début récupérée
        String insertSQL = "INSERT INTO posseder (id_proprietaire, id_vehicule, date_debut_propriete, date_fin_propriete) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            insertStmt.setInt(1, newIdProprietaire);
            insertStmt.setInt(2, newIdVehicule);
            insertStmt.setDate(3, oldDateDebut); // Réintégrer la date de début
            insertStmt.setDate(4, newDateFin);  // Nouvelle date de fin
            insertStmt.executeUpdate();
        }

        JOptionPane.showMessageDialog(null, "Modification réussie !");

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Une erreur est survenue lors de la mise à jour.", "Erreur",
                JOptionPane.ERROR_MESSAGE);
    }
}

    // Supprimer une relation existante
    public void deletePossession(int idProprietaire, int idVehicule) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("DELETE FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?")) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Possession supprimée avec succès !");
            } else {
                JOptionPane.showMessageDialog(null, "Erreur lors de la suppression.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Vérifier si une relation propriétaire-véhicule existe
    private boolean existsPossession(int idProprietaire, int idVehicule) {
        String query = "SELECT COUNT(*) FROM posseder WHERE id_proprietaire = ? AND id_vehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idProprietaire);
            ps.setInt(2, idVehicule);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean existVehicule(int idVehicule) {
        String query = "SELECT COUNT(*) FROM vehicule WHERE id_vehicule = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
    
            ps.setInt(1, idVehicule);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getProprietaireIdByName(String prenom, String nom) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT id_proprietaire FROM proprietaire WHERE prenom = ? AND nom = ?")) {
            ps.setString(1, prenom);
            ps.setString(2, nom);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_proprietaire");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Si pas trouvé, retourne -1
    }

    public int getVehiculeIdByMatricule(String matricule) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT id_vehicule FROM vehicule WHERE matricule = ?")) {
            ps.setString(1, matricule);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_vehicule");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Si pas trouvé, retourne -1
    }

    private void showAlert(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public Map<String, List<String>> getPrenomsAndNoms() {
        Map<String, List<String>> prenomNomMap = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT prenom, nom FROM proprietaire");
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");

                prenomNomMap.computeIfAbsent(prenom, k -> new ArrayList<>()).add(nom);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prenomNomMap;
    }

    public List<String> getAllMatricules() {
        List<String> matricules = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DISTINCT matricule FROM vehicule")) {
            while (rs.next()) {
                matricules.add(rs.getString("matricule"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matricules;
    }

}
