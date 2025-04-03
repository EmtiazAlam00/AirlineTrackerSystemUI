package airlinetrackersystem.airlinetrackerfx;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Label welcomeText;
    @FXML
    private TableView<AirplanePart> partsTable;
    @FXML
    private TableColumn<AirplanePart, Integer> colId;
    @FXML
    private TableColumn<AirplanePart, String> colName;
    @FXML
    private TableColumn<AirplanePart, String> colType;
    @FXML
    private TableColumn<AirplanePart, Integer> colFHInspect;
    @FXML
    private TableColumn<AirplanePart, Integer> colITInspect;
    @FXML
    private TextField partNameField;
    @FXML
    private TextField partTypeField;
    @FXML
    private TextField fhInspectField;
    @FXML
    private TextField updateFH;
    @FXML
    private TextField updateIT;
    @FXML
    private TextField itInspectField;
    @FXML
    private TextField searchField;

    private ObservableList<AirplanePart> partsList = FXCollections.observableArrayList();
    private Connection connect;
    private PreparedStatement prepare;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colFHInspect.setCellValueFactory(new PropertyValueFactory<>("fhInspect"));
        colITInspect.setCellValueFactory(new PropertyValueFactory<>("itInspect"));
    }

    public ObservableList<AirplanePart> loadPartsData() {
        partsList.clear(); // Prevent duplicate data
        String query = "SELECT * FROM Parts";

        try (Connection connect = Database.connectDb();
             PreparedStatement prepare = connect.prepareStatement(query);
             ResultSet result = prepare.executeQuery()) {

            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String type = result.getString("type");

                // Handle NULL values correctly
                Integer fhInspect = result.getObject("fh_inspect", Integer.class);
                Integer itInspect = result.getObject("it_inspect", Integer.class);

                // Ensure no null value is passed to the constructor
                partsList.add(new AirplanePart(id, name, type,
                        fhInspect != null ? fhInspect : 0,  // Default to 0 if NULL
                        itInspect != null ? itInspect : 0)); // Default to 0 if NULL
            }
            // Update TableView
            partsTable.setItems(partsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void addPart() {
        String sql = "INSERT INTO Parts (name, type, fh_inspect, it_inspect) VALUES (?, ?, ?, ?)";
        connect = Database.connectDb();
        try {
            Alert alert;

            // Check if fields are empty
            if (partNameField.getText().isEmpty()
                    || partTypeField.getText().isEmpty()
                    || fhInspectField.getText().isEmpty()
                    || itInspectField.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setContentText("Please fill all fields!");
                alert.showAndWait();
                return;
            }
            int fhInspect = Integer.parseInt(fhInspectField.getText().trim());
            int itInspect = Integer.parseInt(itInspectField.getText().trim());

            // Insert data into the database
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, partNameField.getText().trim());
            prepare.setString(2, partTypeField.getText().trim());
            prepare.setInt(3, fhInspect);
            prepare.setInt(4, itInspect);
            prepare.executeUpdate();

            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Message");
            alert.setContentText("Successfully added!");
            alert.showAndWait();

            loadPartsData(); // Refresh table
            clearFields();

        } catch (NumberFormatException e) {
            // If the user enters non-numeric values, show an error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setContentText("FH Inspect and IT Inspect must be valid numbers!");
            alert.showAndWait();
        } catch (SQLException e) {
            // Handle database errors
            if (e.getMessage().contains("type")) { // PostgreSQL will complain about type constraints
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setContentText("Invalid Part Type! Allowed values: FH_Part, IT_Part, FHIT_Part.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("An error occurred while inserting data: " + e.getMessage());
                alert.showAndWait();
            }
        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception Error");
            alert.setContentText("An unexpected error occurred: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void deletePart() {
        AirplanePart selectedPart = partsTable.getSelectionModel().getSelectedItem();
        if (selectedPart == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("Please select a part to delete!");
            alert.showAndWait();
            return;
        }
        //confirmation alert
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Are you sure you want to delete " + selectedPart.getName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM Parts WHERE name = ?";
            connect = Database.connectDb();
            try {
                prepare = connect.prepareStatement(sql);
                prepare.setString(1, selectedPart.getName());
                prepare.executeUpdate();

                //remove from tableview
                partsTable.getItems().remove(selectedPart);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Part deleted successfully!");
                alert.showAndWait();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Error deleting part: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void filterFHITParts() {
        // Ensure the table is not empty before filtering
        if (partsList.isEmpty()) {
            loadPartsData(); // Load all parts first
        }
        ObservableList<AirplanePart> filteredParts = FXCollections.observableArrayList();
        for (AirplanePart part : partsList) {
            if ("FHIT_Part".equals(part.getType())) { // Avoids NullPointerException
                filteredParts.add(part);
            }
        }
        partsTable.setItems(filteredParts);
    }

    public void filterFHParts() {
        // Ensure the table is not empty before filtering
        if (partsList.isEmpty()) {
            loadPartsData(); // Load all parts first
        }
        ObservableList<AirplanePart> filteredParts = FXCollections.observableArrayList();
        for (AirplanePart part : partsList) {
            if ("FH_Part".equals(part.getType())) { // Avoids NullPointerException
                filteredParts.add(part);
            }
        }
        partsTable.setItems(filteredParts);
    }

    public void filterITParts() {
        // Ensure the table is not empty before filtering
        if (partsList.isEmpty()) {
            loadPartsData(); // Load all parts first
        }
        ObservableList<AirplanePart> filteredParts = FXCollections.observableArrayList();
        for (AirplanePart part : partsList) {
            if ("IT_Part".equals(part.getType())) { // Avoids NullPointerException
                filteredParts.add(part);
            }
        }
        partsTable.setItems(filteredParts);
    }

    @FXML
    public void updateInspection() {
        AirplanePart selectedPart = partsTable.getSelectionModel().getSelectedItem();
        if (selectedPart == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("Please select a part to update!");
            alert.showAndWait();
            return;
        }
        try {
            int updatedFHInspect = Integer.parseInt(updateFH.getText().trim());
            int updatedITInspect = Integer.parseInt(updateIT.getText().trim());

            //Database connection
            String sql = "UPDATE Parts SET fh_inspect = ?, it_inspect = ? WHERE id = ?";
            connect = Database.connectDb();
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, updatedFHInspect);
            prepare.setInt(2, updatedITInspect);
            prepare.setInt(3, selectedPart.getId());
            int rowsAffected = prepare.executeUpdate();
            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Inspection values updated successfully!");
                alert.showAndWait();

                // Refresh table data
                loadPartsData();
                clearFields();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Update Failed");
                alert.setContentText("No changes were made.");
                alert.showAndWait();
            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("FH Inspect and IT Inspect must be valid numbers!");
            alert.showAndWait();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setContentText("Error updating inspection values: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void searchPart() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            partsTable.setItems(partsList); // Reset to full list if search is empty
            return;
        }
        ObservableList<AirplanePart> filteredList = FXCollections.observableArrayList();
        for (AirplanePart part : partsList) {
            if (part.getName().toLowerCase().contains(searchText)) {
                filteredList.add(part); // Add matching parts to the list
            }
        }
        partsTable.setItems(filteredList);
    }

    // Helper method to clear input fields after insertion
    private void clearFields() {
        partNameField.clear();
        partTypeField.clear();
        fhInspectField.clear();
        itInspectField.clear();
        updateFH.clear();
        updateIT.clear();
    }
}
