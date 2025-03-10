package controllers;

import database.DatabaseConnection;
import models.Posseder;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Modifier une relation existante
    public void updatePossessionEndDate(int idProprietaire, int idVehicule, Date newDateFin) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE posseder SET date_fin_propriete = ? " +
                        "WHERE id_proprietaire = ? AND id_vehicule = ?")) {

            ps.setDate(1, newDateFin);
            ps.setInt(2, idProprietaire);
            ps.setInt(3, idVehicule);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Date de fin modifiée avec succès !");
            } else {
                JOptionPane.showMessageDialog(null, "Erreur lors de la modification.", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

    // Vérifier si un propriétaire existe
    private boolean existsProprietaire(String prenom, String nom) {
        return existsInTable("proprietaire", "prenom", "nom");
    }

    // Vérifier si un véhicule existe
    private boolean existsVehicule(String matricule) {
        return existsInTable2("vehicule", "matricule");
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

    public int getProprietaireIdByName(String prenom, String nom) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT id_proprietaire FROM proprietaire WHERE prenom = ? AND nom = ?")) {

            ps.setString(1, prenom);
            ps.setString(2, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_proprietaire");
                } else {
                    return -1;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getVehiculeIdByMatricule(String matricule) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT id_vehicule FROM vehicule WHERE matricule = ?")) {

            ps.setString(1, matricule);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_vehicule");
                } else {
                    return -1;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Vérification générale dans une table
    private boolean existsInTable(String tableName, String columnName, String columnName2) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE "
                        + columnName + " = ?" + " AND " + columnName2 + " = ?")) {

            ps.setString(1, columnName);
            ps.setString(2, columnName2);

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

    // Vérification générale dans une table
    private boolean existsInTable2(String tableName, String columnName) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?")) {

            ps.setString(1, tableName);
            ps.setString(2, columnName);

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

    private boolean isDuplicate(Connection conn, String nom, String prenom, String matricule) {
        String query = "SELECT COUNT(*) FROM posseder WHERE prenom = ? AND nom = ? AND matricule = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, matricule);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retourne true si un doublon existe
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // Afficher une boîte de confirmation
    private int showConfirmation(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
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




