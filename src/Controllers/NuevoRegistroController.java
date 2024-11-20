package Controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class NuevoRegistroController {

    @FXML
    private VBox inputContainer;
    @FXML
    private Button saveButton;

    private Connection connection;
    private String tableName;
    private Runnable onSave;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setTable(String tableName) {
        this.tableName = tableName;
        loadFields();
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null); // Optional: Set a custom header if needed
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void loadFields() {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tableName)) {

            while (rs.next()) {
                String columnName = rs.getString("Field");
                String extra = rs.getString("Extra");

                Label label = new Label(columnName);
                TextField textField = new TextField();
                textField.setPromptText(columnName);

                
                if ("auto_increment".equalsIgnoreCase(extra)) {
                    textField.setDisable(true);
                }

                inputContainer.getChildren().addAll(label, textField);
            }

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los campos: " + e.getMessage());
        }
    }

    @FXML
    private void saveRecord() {
        try {
            if (!validateFields()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor llena todos los campos obligatorios.");
                return;
            }

            StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
            StringBuilder values = new StringBuilder("VALUES (");

            ObservableList<Node> inputs = inputContainer.getChildren();
            for (int i = 0; i < inputs.size(); i += 2) {
                Label label = (Label) inputs.get(i);
                TextField textField = (TextField) inputs.get(i + 1);

                if (!textField.isDisabled()) {
                    if (query.charAt(query.length() - 1) != '(') {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(label.getText());
                    values.append("?");
                }
            }
            query.append(") ").append(values).append(")");

            try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
                int index = 1;
                for (int i = 0; i < inputs.size(); i += 2) {
                    TextField textField = (TextField) inputs.get(i + 1);
                    if (!textField.isDisabled()) {
                        pstmt.setString(index++, textField.getText());
                    }
                }
                pstmt.executeUpdate();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Registro agregado correctamente.");
                if (onSave != null) {
                    onSave.run();
                }
                ((Stage) saveButton.getScene().getWindow()).close();
            }

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", e.getMessage());
        }
    }

    private boolean validateFields() {
        for (Node node : inputContainer.getChildren()) {
            if (node instanceof TextField) {
                TextField textField = (TextField) node; // Explicit cast
                System.out.println(textField.getText());
            }

        }
        return true;
    }
}
