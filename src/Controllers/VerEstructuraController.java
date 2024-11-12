/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import Controllers.MenuController;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 */
public class VerEstructuraController implements Initializable {

    private Connection connection;
    private String selectedTable;
    private MenuController menuController;
    private Scene menuScene; // Scene to store the menu scene for easy switching

    @FXML
    private Label label_nombre;
    @FXML
    private TableView<Estructura> tabla_estructura;
    @FXML
    private Button btn_regresar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize TableView columns for table structure
        TableColumn<Estructura, String> fieldCol = new TableColumn<>("Field");
        fieldCol.setCellValueFactory(new PropertyValueFactory<>("field"));

        TableColumn<Estructura, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Estructura, String> isNullCol = new TableColumn<>("Null");
        isNullCol.setCellValueFactory(new PropertyValueFactory<>("isNull"));

        TableColumn<Estructura, String> keyCol = new TableColumn<>("Key");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<Estructura, String> defaultCol = new TableColumn<>("Default");
        defaultCol.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));

        TableColumn<Estructura, String> extraCol = new TableColumn<>("Extra");
        extraCol.setCellValueFactory(new PropertyValueFactory<>("extra"));

        tabla_estructura.getColumns().addAll(fieldCol, typeCol, isNullCol, keyCol, defaultCol, extraCol);
    }

    // Set up connection, selected table, and menu scene reference
    public void setConnectionAndTable(Connection connection, String selectedTable) {
        this.connection = connection;
        this.selectedTable = selectedTable;
        label_nombre.setText("Estructura de la Tabla: " + selectedTable);
        mostrarEstructuraDeTabla();
    }

    // Set the existing MenuController and Menu Scene to go back to
    public void setMenuController(MenuController menuController, Scene menuScene) {
        this.menuController = menuController;
        this.menuScene = menuScene; // Store the menu scene to switch back easily
    }

    // Load table structure data
    private void mostrarEstructuraDeTabla() {
        ObservableList<Estructura> estructuraList = FXCollections.observableArrayList();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("DESCRIBE " + selectedTable);

            while (resultSet.next()) {
                Estructura estructura = new Estructura(
                        resultSet.getString("Field"),
                        resultSet.getString("Type"),
                        resultSet.getString("Null"),
                        resultSet.getString("Key"),
                        resultSet.getString("Default"),
                        resultSet.getString("Extra")
                );
                estructuraList.add(estructura);
            }

            tabla_estructura.setItems(estructuraList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void DoRegresar(ActionEvent event) {
        System.out.println("Regresar button clicked");

        if (menuScene != null) {
            Stage stage = (Stage) btn_regresar.getScene().getWindow();
            stage.setScene(menuScene);  // Go back to the original menu scene
        } else {
            System.out.println("menuScene is null. Ensure setMenuController was called.");
        }
    }

    public static class Estructura {

        private final String field;
        private final String type;
        private final String isNull;
        private final String key;
        private final String defaultValue;
        private final String extra;

        public Estructura(String field, String type, String isNull, String key, String defaultValue, String extra) {
            this.field = field;
            this.type = type;
            this.isNull = isNull;
            this.key = key;
            this.defaultValue = defaultValue;
            this.extra = extra;
        }

        public String getField() {
            return field;
        }

        public String getType() {
            return type;
        }

        public String getIsNull() {
            return isNull;
        }

        public String getKey() {
            return key;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getExtra() {
            return extra;
        }
    }
}
