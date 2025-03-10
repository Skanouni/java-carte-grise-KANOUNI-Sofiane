package views;

import controllers.PossederController;
import models.Posseder;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PossederView extends JFrame {
    private PossederController controller;

    public PossederView() {
        controller = new PossederController();

        setTitle("Gestion des Possessions");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Disposition verticale

        // Charger les possessions
        List<Posseder> possessions = controller.getAllPossessions();
        for (Posseder possession : possessions) {
            // Créer un panel pour chaque possession
            JPanel possessionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel possessionLabel = new JLabel(
                    "Propriétaire: " + possession.getPrenom() + " " + possession.getNom() +
                    ", Véhicule: " + possession.getMatricule() +
                    ", Début: " + possession.getDateDebutPropriete() +
                    ", Fin: " + (possession.getDateFinPropriete() != null ? possession.getDateFinPropriete() : "Actuel")
            );






            // Bouton Modifier la date de fin
            JButton modifyButton = new JButton("Modifier la date de fin");
            modifyButton.addActionListener(e -> {
                JTextField dateFinField = new JTextField(possession.getDateFinPropriete() != null ? possession.getDateFinPropriete().toString() : "");
                Object[] message = {
                        "Date de fin (YYYY-MM-DD) :", dateFinField
                };

                int option = JOptionPane.showConfirmDialog(this, message, "Modifier la date de fin", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        Date newDateFin = Date.valueOf(dateFinField.getText());
                        controller.updatePossessionEndDate(possession.getIdProprietaire(), possession.getIdVehicule(), newDateFin);
                        refreshView();
                    } catch (Exception ex) {
                        showErrorMessage("Format de date invalide ou erreur lors de la mise à jour.");
                    }
                }
            });

            // Bouton Supprimer
            JButton deleteButton = new JButton("Supprimer");
            deleteButton.addActionListener(e -> {
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Êtes-vous sûr de vouloir supprimer cette possession ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        controller.deletePossession(possession.getIdProprietaire(), possession.getIdVehicule());
                        refreshView();
                    } catch (Exception ex) {
                        showErrorMessage("Erreur lors de la suppression de la possession.");
                    }
                }
            });

            // Ajouter les composants au panel
            possessionPanel.add(possessionLabel);
            possessionPanel.add(modifyButton);
            possessionPanel.add(deleteButton);
            panel.add(possessionPanel);
        }

        // Panel pour les boutons "Ajouter" et "Retour"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Bouton "Ajouter une Possession"
        JButton addButton = new JButton("Ajouter une Possession");
        addButton.addActionListener(e -> {
    // Récupérer les prénoms et noms liés depuis le contrôleur
            Map<String, List<String>> prenomNomMap = controller.getPrenomsAndNoms(); // Nouvelle méthode dans le contrôleur
            List<String> prenoms = new ArrayList<>(prenomNomMap.keySet()); // Liste des prénoms

            // Création des listes déroulantes
            JComboBox<String> prenomBox = new JComboBox<>(prenoms.toArray(new String[0]));
            JComboBox<String> nomBox = new JComboBox<>(); // Vide au départ

            // Remplissage initial du champ nom selon le prénom sélectionné
            if (!prenoms.isEmpty()) {
                String selectedPrenom = prenoms.get(0);
                nomBox.setModel(new DefaultComboBoxModel<>(prenomNomMap.get(selectedPrenom).toArray(new String[0])));
            }

            // Mise à jour du champ nom lorsqu'on change le prénom
            prenomBox.addActionListener(event -> {
                String selectedPrenom = (String) prenomBox.getSelectedItem();
                if (selectedPrenom != null) {
                    nomBox.setModel(new DefaultComboBoxModel<>(prenomNomMap.get(selectedPrenom).toArray(new String[0])));
                }
            });
            List<String> matricules = controller.getAllMatricules(); // Méthode à implémenter dans le contrôleur
        
            JComboBox<String> matriculeBox = new JComboBox<>(matricules.toArray(new String[0]));
            JTextField dateDebutField = new JTextField(); // Toujours un champ texte pour la date
        
            // Message de la boîte de dialogue
            Object[] message = {
                    "Prénom du propriétaire :", prenomBox,
                    "Nom du propriétaire :", nomBox,
                    "Matricule du véhicule :", matriculeBox,
                    "Date de début (YYYY-MM-DD) :", dateDebutField
            };
        
            // Affichage de la boîte de dialogue
            int option = JOptionPane.showConfirmDialog(this, message, "Ajouter une Possession", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    // Récupération des valeurs sélectionnées
                    String prenom = (String) prenomBox.getSelectedItem();
                    String nom = (String) nomBox.getSelectedItem();
                    String matricule = (String) matriculeBox.getSelectedItem();
                    Date dateDebut = Date.valueOf(dateDebutField.getText().trim());
        
                    // Vérification des entrées
                    if (prenom == null || nom == null || matricule == null || dateDebutField.getText().trim().isEmpty()) {
                        showErrorMessage("Tous les champs doivent être remplis.");
                        return;
                    }
                    controller.addPossession(prenom, nom, matricule, dateDebut);
                    
                    refreshView();
                } catch (Exception ex) {
                    showErrorMessage("Données invalides ou erreur lors de l'ajout.");
                }
            }
        });

        // Bouton "Retour"
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> dispose());

        // Ajouter les boutons au panel
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);

        // Ajouter le panel des boutons au panneau principal
        panel.add(buttonPanel);

        // Ajouter un JScrollPane pour la barre de défilement
        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane);

        // Rendre visible
        setVisible(true);
    }

    // Méthode pour rafraîchir la vue
    private void refreshView() {
        dispose();
        new PossederView(); // Recharger la vue avec les nouvelles possessions
    }

    // Afficher un message d'erreur
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        new PossederView();
    }
}
