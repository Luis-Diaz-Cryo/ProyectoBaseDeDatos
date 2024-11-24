/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 */
public class MenuController implements Initializable {

    @FXML
    private ChoiceBox<String> cb_base;
    @FXML
    private ChoiceBox<String> cb_tabla;
    @FXML
    private ChoiceBox<String> cb_tabla1;
    @FXML
    private Button btn_ver1;
    @FXML
    private Button btn_estruc1;
    @FXML
    private ChoiceBox<String> cb_tabla2;
    @FXML
    private Button btn_ver2;
    @FXML
    private Button btn_estruc2;
    @FXML
    private Button btn_regresar;
    @FXML
    private Button btn_consultas;

    private Connection connection;

    @FXML
    private AnchorPane panel_tabla1;
    @FXML
    private AnchorPane panel_tabla2;

    // Variables to save state
    private String selectedDatabase;
    private String selectedTableOption;
    private String selectedTable1;
    private String selectedTable2;

    // To save the menu scene for later use
    private Scene menuScene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cb_tabla.getItems().addAll("1", "2");

        // Initially disable the table panels
        panel_tabla1.setDisable(true);
        panel_tabla2.setDisable(true);

        // Restore previous selections if they exist
        if (selectedDatabase != null) {
            cb_base.getSelectionModel().select(selectedDatabase);
            cargarTablasDisponibles(selectedDatabase);
        }
        if (selectedTableOption != null) {
            cb_tabla.getSelectionModel().select(selectedTableOption);
            verificarSeleccion();
        }
        if (selectedTable1 != null) {
            cb_tabla1.getSelectionModel().select(selectedTable1);
        }
        if (selectedTable2 != null) {
            cb_tabla2.getSelectionModel().select(selectedTable2);
        }

        // Add listeners for choice boxes to update table options based on selections
        cb_base.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarTablasDisponibles(newValue);
            }
        });

        cb_tabla.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> verificarSeleccion());
        cb_tabla1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> actualizarTablasDisponibles());
        cb_tabla2.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> actualizarTablasDisponibles());
    }

    private void cargarTablasDisponibles(String baseSeleccionada) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("USE " + baseSeleccionada);
            ResultSet resultSet = statement.executeQuery("SHOW TABLES");

            cb_tabla1.getItems().clear();
            cb_tabla2.getItems().clear();

            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                cb_tabla1.getItems().add(tableName);
                cb_tabla2.getItems().add(tableName);
            }

            actualizarTablasDisponibles();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error al cargar tablas", e.getMessage());
        }
    }

    private void actualizarTablasDisponibles() {
        String seleccionada1 = cb_tabla1.getSelectionModel().getSelectedItem();
        String seleccionada2 = cb_tabla2.getSelectionModel().getSelectedItem();

        cb_tabla1.getItems().removeIf(item -> item.equals(seleccionada2));
        cb_tabla2.getItems().removeIf(item -> item.equals(seleccionada1));
    }

    private void verificarSeleccion() {
        String tablaSeleccionada = cb_tabla.getSelectionModel().getSelectedItem();
        if ("1".equals(tablaSeleccionada)) {
            panel_tabla1.setDisable(false);
            panel_tabla2.setDisable(true);
        } else if ("2".equals(tablaSeleccionada)) {
            panel_tabla1.setDisable(false);
            panel_tabla2.setDisable(false);
        } else {
            panel_tabla1.setDisable(true);
            panel_tabla2.setDisable(true);
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        cargarBasesDeDatos();
    }

    private void cargarBasesDeDatos() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");

            cb_base.getItems().clear();
            while (resultSet.next()) {
                String databaseName = resultSet.getString(1);
                cb_base.getItems().add(databaseName);
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error al cargar bases de datos.", e.getMessage());
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void GoVer(ActionEvent event) {
        // Save selections before switching screens
        selectedDatabase = cb_base.getSelectionModel().getSelectedItem();
        selectedTableOption = cb_tabla.getSelectionModel().getSelectedItem();
        selectedTable1 = cb_tabla1.getSelectionModel().getSelectedItem();
        selectedTable2 = cb_tabla2.getSelectionModel().getSelectedItem();

        try {
            ChoiceBox<String> selectedChoiceBox = (event.getSource() == btn_ver1) ? cb_tabla1 : cb_tabla2;
            String selectedTable = selectedChoiceBox.getSelectionModel().getSelectedItem();

            if (selectedTable == null) {
                showAlert(AlertType.WARNING, "Tabla no seleccionada", "Por favor, seleccione una tabla para ver su contenido.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/VerTabla.fxml"));
            Parent root = loader.load();

            VerTablaController tablaController = loader.getController();
            tablaController.setConnectionAndTable(connection, selectedTable);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            menuScene = stage.getScene();  // Save the current scene before switching
            tablaController.setMenuScene(menuScene);  // Pass the saved menuScene to VerTablaController

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la pantalla de la tabla.");
            e.printStackTrace();
        }
    }

    @FXML
    private void GoEstructura(ActionEvent event) {
        // Save selections before switching screens
        selectedDatabase = cb_base.getSelectionModel().getSelectedItem();
        selectedTableOption = cb_tabla.getSelectionModel().getSelectedItem();
        selectedTable1 = cb_tabla1.getSelectionModel().getSelectedItem();
        selectedTable2 = cb_tabla2.getSelectionModel().getSelectedItem();

        try {
            ChoiceBox<String> selectedChoiceBox = (event.getSource() == btn_estruc1) ? cb_tabla1 : cb_tabla2;
            String selectedTable = selectedChoiceBox.getSelectionModel().getSelectedItem();

            if (selectedTable == null) {
                showAlert(AlertType.WARNING, "Tabla no seleccionada", "Por favor, seleccione una tabla para ver su estructura.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/VerEstructura.fxml"));
            Parent root = loader.load();

            // Get the controller instance and set connection, table, and menu scene
            VerEstructuraController estructuraController = loader.getController();
            estructuraController.setConnectionAndTable(connection, selectedTable);

            // Get the current scene to store as menuScene
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            menuScene = stage.getScene();  // Save the current scene
            estructuraController.setMenuController(this, menuScene);  // Pass the saved menuScene to VerEstructuraController

            stage.setScene(new Scene(root));  // Set the new scene
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la pantalla de estructura.");
            e.printStackTrace();
        }
    }

    @FXML
    private void GoRegresar(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/LogIn.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btn_regresar.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void GoConsultas(ActionEvent event) throws IOException {
        String tabla = cb_tabla1.getSelectionModel().getSelectedItem();
        String tabla2 = cb_tabla2.getSelectionModel().getSelectedItem();
        String numTablas = cb_tabla.getSelectionModel().getSelectedItem();
        if (tabla == null) {
            showAlert(Alert.AlertType.WARNING, "Tabla no seleccionada", "Por favor, seleccione una tabla para realizar consultas.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/VentanaPrueba.fxml"));
        Parent root = loader.load();

        VentanaPruebaController escena = loader.getController();
        escena.setConnection(connection);
        escena.setTable(tabla);

        if ("2".equals(numTablas) && tabla2 != null) {
            escena.setTableRelation(tabla2, true);
        } else {
            escena.setTableRelation(null, false);
        }

        // Pass the current menu scene
        Scene currentScene = ((Button) event.getSource()).getScene();
        escena.setMenuScene(currentScene); // Pass menuScene to VentanaPruebaController

        Stage stage = (Stage) btn_consultas.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.show();
    }
}
