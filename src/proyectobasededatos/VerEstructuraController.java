/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package proyectobasededatos;

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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
    
    @FXML
    private Label label_nombre;
    
    @FXML
    private TableView<Estructura> tabla_estructura;
    @FXML
    private Button btn_regresar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización si es necesaria
    }

    public void setConnectionAndTable(Connection connection, String selectedTable) {

    }

    private void mostrarEstructuraDeTabla() {
        
    }

    @FXML
    private void DoRegresar(ActionEvent event) throws IOException {
 
    }

    public static class Estructura {
     

        public Estructura(String field, String type, String isNull, String key, String defaultValue, String extra) {

        }

    }
}

