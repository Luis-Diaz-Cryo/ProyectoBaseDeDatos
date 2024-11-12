package Controllers;

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
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private Scene menuScene;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // No static column initialization as columns are dynamic based on the table selected
    }

    public void setConnectionAndTable(Connection connection, String selectedTable) {
        this.connection = connection;
        this.selectedTable = selectedTable;
        label_nombre.setText("Contenido de la Tabla: " + selectedTable);
        mostrarTablaCompleta();
    }

    public void setMenuScene(Scene menuScene) {
        this.menuScene = menuScene;
    }

    private void mostrarTablaCompleta() {
        tabla_ver.getColumns().clear();  // Clear previous columns if any
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + selectedTable);

            // Get column names and dynamically create TableColumn for each
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                final int columnIndex = i;
                String columnName = metaData.getColumnName(i);

                TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(param -> {
                    if (param.getValue().size() > columnIndex - 1) {
                        return new javafx.beans.property.SimpleStringProperty(param.getValue().get(columnIndex - 1));
                    } else {
                        return new javafx.beans.property.SimpleStringProperty("");
                    }
                });

                tabla_ver.getColumns().add(column);
            }

            // Add rows to the table
            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }

            tabla_ver.setItems(data);

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error al cargar contenido", "No se pudo cargar el contenido de la tabla: " + e.getMessage());
        }
    }

    @FXML
    private void DoRegresar(ActionEvent event) {
        System.out.println("Regresar button clicked");

        if (menuScene != null) {
            Stage stage = (Stage) btn_regresar.getScene().getWindow();
            stage.setScene(menuScene);  // Go back to the original menu scene
        } else {
            System.out.println("menuScene is null. Ensure setMenuScene was called.");
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}
