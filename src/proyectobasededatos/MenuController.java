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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Alert;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cb_tabla.getItems().addAll("1", "2");

        panel_tabla1.setDisable(true);
        panel_tabla2.setDisable(true);

        cb_base.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarTablasDisponibles(newValue);
            }
        });

        cb_tabla.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            verificarSeleccion();
        });

        cb_tabla1.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            actualizarTablasDisponibles();
        });

        cb_tabla2.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            actualizarTablasDisponibles();
        });
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
        String baseSeleccionada = cb_base.getSelectionModel().getSelectedItem();
        String tablaSeleccionada = cb_tabla.getSelectionModel().getSelectedItem();

        if (baseSeleccionada != null && tablaSeleccionada != null) {
            if (tablaSeleccionada.equals("1")) {
                panel_tabla1.setDisable(false);
                panel_tabla2.setDisable(true);
            } else if (tablaSeleccionada.equals("2")) {
                panel_tabla1.setDisable(false);
                panel_tabla2.setDisable(false);
            }
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
        try {
            ChoiceBox<String> selectedChoiceBox = (event.getSource() == btn_ver1) ? cb_tabla1 : cb_tabla2;
            String selectedTable = selectedChoiceBox.getSelectionModel().getSelectedItem();

            if (selectedTable == null) {
                showAlert(AlertType.WARNING, "Tabla no seleccionada", "Por favor, seleccione una tabla para ver su contenido.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("VerTabla.fxml"));
            Parent root = loader.load();

            VerTablaController tablaController = loader.getController();
            tablaController.setConnectionAndTable(connection, selectedTable);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la pantalla de la tabla.");
            e.printStackTrace();
        }
    }


    @FXML
    private void GoEstructura(ActionEvent event) {
        try {
            ChoiceBox<String> selectedChoiceBox = (event.getSource() == btn_estruc1) ? cb_tabla1 : cb_tabla2;
            String selectedTable = selectedChoiceBox.getSelectionModel().getSelectedItem();

            if (selectedTable == null) {
                showAlert(AlertType.WARNING, "Tabla no seleccionada", "Por favor, seleccione una tabla para ver su estructura.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("VerEstructura.fxml"));
            Parent root = loader.load();

            VerEstructuraController estructuraController = loader.getController();
            estructuraController.setConnectionAndTable(connection, selectedTable);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error de Carga", "No se pudo cargar la pantalla de estructura.");
            e.printStackTrace();
        }
    }

    @FXML
    private void GoRegresar(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LogIn.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btn_regresar.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void GoConsultas(ActionEvent event) {
        // Lógica para manejar el botón Consultas
    }
}

