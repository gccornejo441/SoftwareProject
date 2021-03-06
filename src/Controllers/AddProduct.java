/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import Model.Inventory;
import Model.Part;
import Model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class AddProduct implements Initializable {

    Inventory inv;

    @FXML
    private TextField id;
    @FXML
    private TextField name;
    @FXML
    private TextField price;
    @FXML
    private TextField count;
    @FXML
    private TextField min;
    @FXML
    private TextField max;
    @FXML
    private TextField search;
    @FXML
    private TableView<Part> partSearchTable;
    @FXML
    private TableView<Part> assocPartsTable;
    private ObservableList<Part> partsInventory = FXCollections.observableArrayList();
    private ObservableList<Part> partsInventorySearch = FXCollections.observableArrayList();
    private ObservableList<Part> assocPartList = FXCollections.observableArrayList();

    public AddProduct(Inventory inv) {
        this.inv = inv;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        generateProductID();
        populateSearchTable();
    }

    private void generateProductID() {
        boolean match;
        Random randomNum = new Random();
        Integer num = randomNum.nextInt(999);


        if (inv.productListSize() == 0) {
            id.setText(num.toString());

        }
        if (inv.productListSize() == 999) {
            Alert.errorProduct(3, null);
        } else {
            match = generateNum(num);

            if (match == false) {
                id.setText(num.toString());
            } else {
                generateProductID();
            }
        }

        id.setText(num.toString());
    }

    private boolean generateNum(Integer num) {
        Part match = inv.lookUpPart (num);
        return match != null;
    }


    private void populateSearchTable() {
        partsInventory.setAll(inv.getAllParts());

        TableColumn<Part, Double> costCol = formatPrice();
        partSearchTable.getColumns().addAll(costCol);

        partSearchTable.setItems(partsInventory);
        partSearchTable.refresh();
    }

    @FXML
    private void clearTextField(MouseEvent event) {
        Object source = event.getSource();
        TextField field = (TextField) source;
        field.setText("");
    }

    @FXML
    private void searchForPart(MouseEvent event) {
        if (!search.getText().trim().isEmpty()) {
            partsInventory.clear();
            partSearchTable.setItems(inv.lookUpPart(search.getText().trim()));
            partSearchTable.refresh();
        }
    }

    @FXML
    private void addPart(MouseEvent event) {
        Part addPart = partSearchTable.getSelectionModel().getSelectedItem();
        boolean repeatedItem = false;

        if (addPart != null) {
            int id = addPart.getId();
            for (int i = 0; i < assocPartList.size(); i++) {
                if (assocPartList.get(i).getId() == id) {
                    Alert.errorProduct(2, null);
                    repeatedItem = true;
                }
            }

            if (!repeatedItem) {
                assocPartList.add(addPart);

            }

            TableColumn<Part, Double> costCol = formatPrice();
            assocPartsTable.getColumns().addAll(costCol);

            assocPartsTable.setItems(assocPartList);
        }
    }

    @FXML
    private void deletePart(MouseEvent event
    ) {
        Part removePart = assocPartsTable.getSelectionModel().getSelectedItem();
        boolean deleted = false;
        if (removePart != null) {
            boolean remove = Alert.confirmationWindow(removePart.getName());
            if (remove) {
                assocPartList.remove(removePart);
                assocPartsTable.refresh();
            }
        } else {
            return;
        }
        if (deleted) {
            Alert.infoWindow(1, removePart.getName());
        } else {
            Alert.infoWindow(2, "");
        }

    }


    @FXML
    private void cancelAddProduct(MouseEvent event
    ) {
        boolean cancel = Alert.cancel();
        if (cancel) {
            mainScreen(event);
        }
    }

    @FXML
    private void saveAddProduct(MouseEvent event
    ) {
        resetFieldsStyle();
        boolean end = false;
        TextField[] fieldCount = {count, price, min, max};
        double minCost = 0;
        for (int i = 0; i < assocPartList.size(); i++) {
            minCost += assocPartList.get(i).getPrice();
        }
        if (name.getText().trim().isEmpty() || name.getText().trim().toLowerCase().equals("part name")) {
            Alert.errorProduct(4, name);
            return;
        }
        for (TextField fieldCount1 : fieldCount) {
            boolean valueError = checkValue(fieldCount1);
            if (valueError) {
                end = true;
                break;
            }
            boolean typeError = checkType(fieldCount1);
            if (typeError) {
                end = true;
                break;
            }
        }
        if (Integer.parseInt(min.getText().trim()) > Integer.parseInt(max.getText().trim())) {
            Alert.errorProduct(10, min);
            return;
        }
        if (Integer.parseInt(count.getText().trim()) < Integer.parseInt(min.getText().trim())) {
            Alert.errorProduct(8, count);
            return;
        }
        if (Integer.parseInt(count.getText().trim()) > Integer.parseInt(max.getText().trim())) {
            Alert.errorProduct(9, count);
            return;
        }
        if (Double.parseDouble(price.getText().trim()) < minCost) {
            Alert.errorProduct(6, price);
            return;
        }
        if (assocPartList.isEmpty()) {
            Alert.errorProduct(7, null);
            return;
        }

        saveProduct();
        mainScreen(event);

    }

    private void fieldError(TextField field) {
        if (field != null) {
            field.setStyle("-fx-border-color: red");
        }
    }

    private void saveProduct() {
        Product product = new Product(Integer.parseInt(id.getText().trim()), name.getText().trim(), Double.parseDouble(price.getText().trim()),
                Integer.parseInt(count.getText().trim()), Integer.parseInt(min.getText().trim()), Integer.parseInt(max.getText().trim()));
        for (int i = 0; i < assocPartList.size(); i++) {
            product.addAssociatedPart(assocPartList.get(i));
        }

        inv.addProduct(product);

    }

    private void resetFieldsStyle() {
        name.setStyle("-fx-border-color: lightgray");
        count.setStyle("-fx-border-color: lightgray");
        price.setStyle("-fx-border-color: lightgray");
        min.setStyle("-fx-border-color: lightgray");
        max.setStyle("-fx-border-color: lightgray");

    }

    @FXML
    void clearField(MouseEvent event) {
        search.setText("");
        if (!partsInventory.isEmpty()) {
            partSearchTable.setItems(partsInventory);
        }

    }

    private void mainScreen(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/MainScreen.fxml"));
            MainScreen controller = new MainScreen(inv);

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

    private boolean checkValue(TextField field) {
        boolean error = false;
        try {
            if (field.getText().trim().isEmpty() || field.getText().trim() == null) {
                Alert.errorProduct(1, field);
                return true;
            }
            if (field == price && Double.parseDouble(field.getText().trim()) < 0) {
                Alert.errorProduct(5, field);
                error = true;
            }
        } catch (NumberFormatException e) {
            error = true;
            Alert.errorProduct(3, field);
            System.out.println(e);

        }
        return error;
    }

    private boolean checkType(TextField field) {

        if (field == price & !field.getText().trim().matches("\\d+(\\.\\d+)?")) {
            Alert.errorProduct(3, field);
            return true;
        }
        if (field != price & !field.getText().trim().matches("[0-9]*")) {
            Alert.errorProduct(3, field);
            return true;
        }
        return false;

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
                    }
                    else{
                        setText("");
                    }
                }
            };
        });
        return costCol;
    }

}
