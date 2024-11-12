/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 */
public class LogInController implements Initializable {

    @FXML
    private TextField input_maquina;
    @FXML
    private TextField input_puerto;
    @FXML
    private TextField input_user;
    @FXML
    private PasswordField input_password;
    @FXML
    private Button btn_log;

    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización si es necesaria
    }

    @FXML
    private void DoLogin(ActionEvent event) {
        // Obtener los valores ingresados por el usuario
        String maquina = input_maquina.getText();
        String puerto = input_puerto.getText();
        String usuario = input_user.getText();
        String password = input_password.getText();

        // Verificar si algún campo está vacío
        if (maquina.isEmpty() || puerto.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, complete todos los campos.");
            return;
        }

        // Construir la URL de conexión
        String url = "jdbc:mysql://" + maquina + ":" + puerto + "/";

        try {
            // Intentar establecer la conexión a la base de datos
            connection = DriverManager.getConnection(url, usuario, password);
            // Si la conexión fue exitosa, mostrar un mensaje
            showAlert(Alert.AlertType.INFORMATION, "Conexión Exitosa", "¡Se ha conectado correctamente!");

            // Cambiar de escena al menú
            goToMenu();

        } catch (SQLException e) {
             e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Conexión", "Por favor, revisa tus credenciales.");
        }
    }

    private void goToMenu() {
        try {
            // Cargar el archivo FXML del menú
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/Menu.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del menú
            MenuController menuController = loader.getController();

            // Pasar la conexión al controlador del menú
            menuController.setConnection(connection);

            // Obtener el stage actual usando cualquier componente de la escena (btn_log en este caso)
            Stage stage = (Stage) btn_log.getScene().getWindow();
            // Configurar la nueva escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la pantalla de menú.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    // Método para obtener la conexión en otros controladores, si se necesita
    public Connection getConnection() {
        return connection;
    }
}
