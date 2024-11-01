/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package proyectobases;

/**
 * FXML Controller class
 *
 * @author marim
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

public class VerTablaController implements Initializable {

    @FXML
    private Label label_nombre;
    @FXML
    private TableView<ObservableList<String>> tabla_ver;
    @FXML
    private Button btn_regresar;

    private Connection connection;
    private String selectedTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización si es necesaria
    }

    public void setConnectionAndTable(Connection connection, String selectedTable) {
        
    }

    private void mostrarTablaCompleta() {
        
    }

    @FXML
    private void DoRegresar(ActionEvent event) throws IOException {

    }

    private void showAlert(AlertType alertType, String title, String message) {

    }
}

