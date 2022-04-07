/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import Model.Inventory;
import Model.Part;
import Model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/* Main class */

public class MainScreen implements Initializable {

    Inventory inv;

    /**
     * Used to initialize search box for value input.
     * Other fxml components used to build the display table for
     * all parts in the inventory are initiated here.
     */
    @FXML
    private TextField partSearchBox;

    @FXML
    private TextField productSearchBox;

    @FXML
    private TableView<Part> partsTable;

    @FXML
    private TableView<Product> productsTable;
    private ObservableList<Part> partInventory = FXCollections.observableArrayList();
    private ObservableList<Product> productInventory = FXCollections.observableArrayList();
    private ObservableList<Part> partsInventorySearch = FXCollections.observableArrayList();
    private ObservableList<Product> productInventorySearch = FXCollections.observableArrayList();

    public MainScreen(Inventory inv) {
        this.inv = inv;
    }

    /* Initializes the controller class.*/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableParts();
        tableProducts();
    }

    /**
     * Creates a table with a Price column and assigns ALL of the parts in the Inventory..
     */
    private void tableParts() {
        partInventory.setAll(inv.getAllParts());

        TableColumn<Part, Double> priceColumn = formatPrice();
        partsTable.getColumns().addAll(priceColumn);

        partsTable.setItems(partInventory);
        partsTable.refresh();
    }

    /**
     * Generates Price column in the table and assigns ALL the products in the Inventory.
     */
    private void tableProducts() {
        productInventory.setAll(inv.getAllProducts());

        TableColumn<Product, Double> costCol = formatPrice();
        productsTable.getColumns().addAll(costCol);

        productsTable.setItems(productInventory);
        productsTable.refresh();
    }

    @FXML
    private void exitProgram(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void exitProgramButton(MouseEvent event) {
        Platform.exit();
    }

    /* The function of looking for pieces */
    @FXML
    private void searchForPart(MouseEvent event) {
        if (!partSearchBox.getText().trim().isEmpty()) {
            partsTable.setItems(inv.lookUpPart(partSearchBox.getText().trim()));
            partsTable.refresh();
        }
    }

    /**
     This is the traditional method of conducting a search. The Inventory lookupProduct(partName) method isn't used.
    */
    @FXML
    private void searchForProduct(MouseEvent event) {
        if (!productSearchBox.getText().trim().isEmpty()) {
            productInventorySearch.clear();
            for (Product p : inv.getAllProducts()) {
                if (p.getName().contains(productSearchBox.getText().trim())) {
                    productInventorySearch.add(p);
                }
            }
            productsTable.setItems(productInventorySearch);
            productsTable.refresh();
        }
    }

    /**
     * Clears one of the search boxes and displays the entire inventory of components and items.
     */
    @FXML
    void clearText(MouseEvent event) {
        Object source = event.getSource();
        TextField field = (TextField) source;
        field.setText("");
        if (partSearchBox == field) {
            if (partInventory.size() != 0) {
                partsTable.setItems(partInventory);
            }
        }
        if (productSearchBox == field) {
            if (productInventory.size() != 0) {
                productsTable.setItems(productInventory);
            }
        }
    }

    /**
     * To get started, go to the Add Parts screen.
     */
    @FXML
    private void addPart(MouseEvent event
    ) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddPart.fxml"));
            AddPart controller = new AddPart(inv);

            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {

        }
    }

    /**
     * Goes to change parts screen
     */
    @FXML
    private void modifyPart(MouseEvent event
    ) {
        try {
            Part selected = partsTable.getSelectionModel().getSelectedItem();
            if (partInventory.isEmpty()) {
                errorWindow(1);
                return;
            }
            if (!partInventory.isEmpty() && selected == null) {
                errorWindow(2);
                return;
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ModifyPart.fxml"));
                ModifyPart controller = new ModifyPart(inv, selected);

                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        } catch (IOException e) {

        }
    }

    @FXML
    private void deletePart(MouseEvent event
    ) {
        Part removePart = partsTable.getSelectionModel().getSelectedItem();
        if (partInventory.isEmpty()) {
            errorWindow(1);
            return;
        }
        if (!partInventory.isEmpty() && removePart == null) {
            errorWindow(2);
            return;
        } else {
            boolean confirm = confirmationWindow(removePart.getName());
            if (!confirm) {
                return;
            }
            inv.deletePart(removePart);
            partInventory.remove(removePart);
            partsTable.refresh();

        }
    }

    @FXML
    private void deleteProduct(MouseEvent event
    ) {
        boolean deleted = false;
        Product removeProduct = productsTable.getSelectionModel().getSelectedItem();
        if (productInventory.isEmpty()) {
            errorWindow(1);
            return;
        }
        if (!productInventory.isEmpty() && removeProduct == null) {
            errorWindow(2);
            return;
        }
        if (removeProduct.getPartsListSize() == 0) {
            boolean confirm = confirmDelete(removeProduct.getName());
            if (!confirm) {
                return;
            }
        } else {
            infoWindow(removeProduct.getName());
            return;
        }
        inv.removeProduct(removeProduct.getProductID());
        productInventory.remove(removeProduct);
        productsTable.setItems(productInventory);
        productsTable.refresh();
    }

    /**
     * This takes you to the modify parts panel
     */
    @FXML
    private void modifyProduct(MouseEvent event
    ) {
        try {
            Product productSelected = productsTable.getSelectionModel().getSelectedItem();
            if (productInventory.isEmpty()) {
                errorWindow(1);
                return;
            }
            if (!productInventory.isEmpty() && productSelected == null) {
                errorWindow(2);
                return;

            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ModifyProduct.fxml"));
                ModifyProduct controller = new ModifyProduct(inv, productSelected);

                loader.setController(controller);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        } catch (IOException e) {

        }
    }

    @FXML
    private void addProduct(MouseEvent event
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/AddProduct.fxml"));
            AddProduct controller = new AddProduct(inv);

            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {

        }
    }

    private void errorWindow(int code) {
        if (code == 1) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Empty Inventory!");
            alert.setContentText("There's nothing to select!");
            alert.showAndWait();
        }
        if (code == 2) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Selection");
            alert.setContentText("You must select an item!");
            alert.showAndWait();
        }

    }

    private boolean confirmationWindow(String name) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete part");
        alert.setHeaderText("Are you sure you want to delete: " + name);
        alert.setContentText("Click ok to confirm");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    private boolean confirmDelete(String name) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete product");
        alert.setHeaderText("Are you sure you want to delete: " + name );
        alert.setContentText("Click ok to confirm");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    private void infoWindow(String name) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Confirmed");
        alert.setHeaderText(null);
        alert.setContentText(name + " still has parts assigned to it and has NOT been deleted!");
        alert.showAndWait();
    }

    private <T> TableColumn<T, Double> formatPrice() {
        TableColumn<T, Double> costCol = new TableColumn("Price");
        costCol.setCellValueFactory(new PropertyValueFactory<>("Price"));
        costCol.setCellFactory((TableColumn<T, Double> column) -> {
            return new TableCell<T, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    if (!empty) {
                        setText("$" + String.format("%10.2f", item));
                    } else {
                        setText("");
                    }
                }
            };
        });
        return costCol;
    }

}
