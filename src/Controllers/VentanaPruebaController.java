package Controllers;

import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaPruebaController implements Initializable {

    @FXML
    private ComboBox<String> tabla1ComboBox;
    @FXML
    private ComboBox<String> campo1ComboBox;
    @FXML
    private ComboBox<String> tabla2ComboBox;
    @FXML
    private ComboBox<String> campo2ComboBox;
    @FXML
    private ComboBox<String> operadorComboBox;
    @FXML
    private TextField valorTextField;
    @FXML
    private Button agregarCondicionButton;
    @FXML
    private Button buscarButton;
    @FXML
    private Button limpiarButton;
    @FXML
    private Button regresarButton;
    @FXML
    private Button agregarButton;
    @FXML
    private Button eliminarButton;
    @FXML
    private Button guardarButton;
    @FXML
    private VBox columnasContainer;
    @FXML
    private Button agregarColumnaButton;
    @FXML
    private TextArea consultaGeneradaTextArea;
    @FXML
    private ComboBox<String> consultaCampoComboBox;
    @FXML
    private TableView<ObservableList<String>> resultadoTable;

    private boolean isRelationEnabled = false;
    private StringBuilder consultaGenerada = new StringBuilder();
    private Connection connection;
    private String currentTable;
    private String relatedTable;
    private ObservableList<ObservableList<String>> tableData;
    private Set<ObservableList<String>> filasEditadas = new HashSet<>();
    private ObservableList<ObservableList<String>> filasTemp = FXCollections.observableArrayList();
    private Scene menuScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        operadorComboBox.setItems(FXCollections.observableArrayList("=", ">", "<", ">=", "<=", "LIKE", "NOT LIKE", "IS NULL", "IS NOT NULL"));
        configurarEventos();
        resultadoTable.setEditable(true);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setTable(String table) {
        if (table != null) {
            this.currentTable = table;
            tabla1ComboBox.getSelectionModel().select(table);
            cargarCampos(table, campo1ComboBox);
            cargarCampos(table, consultaCampoComboBox);

        }
    }

    public void setTableRelation(String relatedTable, boolean isRelationEnabled) {
        this.isRelationEnabled = isRelationEnabled;
        this.relatedTable = relatedTable;

        if (isRelationEnabled) {
            
            if (currentTable != null) {
                tabla1ComboBox.setDisable(false); // Ensure it's enabled
                campo1ComboBox.setDisable(false); // Ensure it's enabled
                tabla1ComboBox.getSelectionModel().select(currentTable);
                cargarCampos(currentTable, campo1ComboBox);
            }

            
            if (relatedTable != null) {
                tabla2ComboBox.setDisable(false); // Ensure it's enabled
                campo2ComboBox.setDisable(false); // Ensure it's enabled
                tabla2ComboBox.getSelectionModel().select(relatedTable);
                cargarCampos(relatedTable, campo2ComboBox);
            }
        } else {
            
            tabla2ComboBox.setDisable(true);
            campo2ComboBox.setDisable(true);
        }
    }

    private void configurarEventos() {
        agregarColumnaButton.setOnAction(event -> agregarColumna());
        agregarCondicionButton.setOnAction(this::agregarCondicion);
        buscarButton.setOnAction(event -> ejecutarConsulta());
        limpiarButton.setOnAction(event -> limpiarConsulta());
        regresarButton.setOnAction(event -> regresar());
        agregarButton.setOnAction(event -> agregarRegistro());
        eliminarButton.setOnAction(event -> eliminarRegistro());
        guardarButton.setOnAction(event -> guardarCambios());
        tabla1ComboBox.setOnAction(event -> cargarCampos(tabla1ComboBox.getValue(), campo1ComboBox));
    }

    public void agregarColumna() {
        ComboBox<String> nuevaColumna = new ComboBox<>();
        nuevaColumna.setPromptText("Seleccionar columna");
        nuevaColumna.setPrefWidth(200);
        cargarCampos(currentTable, nuevaColumna);
        columnasContainer.getChildren().add(nuevaColumna);
    }

    public void agregarRegistro() {
        if (currentTable == null || currentTable.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Tabla no seleccionada", "Por favor selecciona una tabla primero.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vista/nuevoRegistro.fxml"));
            Parent root = loader.load();

            NuevoRegistroController controller = loader.getController();
            controller.setConnection(connection);
            controller.setTable(currentTable);
            controller.setOnSave(() -> ejecutarConsulta());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Nuevo Registro");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Log detailed error
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo abrir la ventana de nuevo registro. Verifica la ubicación del archivo FXML.");
        }
    }

    public void cargarCampos(String tabla, ComboBox<String> campoComboBox) {
        System.out.println("Loading columns for table: " + tabla);

        if (tabla != null && !tabla.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tabla)) {

                ObservableList<String> campos = FXCollections.observableArrayList();
                while (rs.next()) {
                    campos.add(rs.getString("Field"));
                }
                campoComboBox.setItems(campos);
                System.out.println("Columns loaded: " + campos);
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar campos", e.getMessage());
            }
        } else {
            System.out.println("No table selected to load columns");
        }
    }

    public void ejecutarConsulta() {
        StringBuilder consultaBase = new StringBuilder("SELECT ");
        List<String> selectedColumns = getSelectedColumns();
        consultaBase.append(selectedColumns.isEmpty() ? "*" : String.join(", ", selectedColumns));
        consultaBase.append(" FROM ").append(currentTable);

        if (isRelationEnabled && relatedTable != null) {
            String campo1 = campo1ComboBox.getValue();
            String campo2 = campo2ComboBox.getValue();

            consultaBase.append(", ").append(relatedTable).append(" WHERE ");
            consultaBase.append(currentTable).append(".").append(campo1).append(" = ").append(relatedTable).append(".").append(campo2);

            if (consultaGenerada.length() > 0) {
                consultaBase.append(" AND ").append(consultaGenerada);
            }
        } else if (consultaGenerada.length() > 0) {
            consultaBase.append(" WHERE ").append(consultaGenerada);
        }

        consultaGeneradaTextArea.setText(consultaBase.toString());
        cargarDatosEnTabla(consultaBase.toString());
    }

    public void actualizarCelda(TableColumn.CellEditEvent<ObservableList<String>, String> event) {
        int filaIndex = event.getTablePosition().getRow();
        int columnaIndex = event.getTablePosition().getColumn();
        ObservableList<String> fila = tableData.get(filaIndex);

        fila.set(columnaIndex, event.getNewValue());
        filasTemp.add(fila);
    }

    public void cargarDatosEnTabla(String consulta) {
        resultadoTable.getColumns().clear();
        tableData = FXCollections.observableArrayList();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(consulta)) {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                final int index = i - 1;
                TableColumn<ObservableList<String>, String> columna = new TableColumn<>(rs.getMetaData().getColumnName(i));
                columna.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(index)));
                columna.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
                columna.setOnEditCommit(this::actualizarCelda);
                resultadoTable.getColumns().add(columna);
            }

            while (rs.next()) {
                ObservableList<String> fila = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    fila.add(rs.getString(i));
                }
                tableData.add(fila);
            }

            resultadoTable.setItems(tableData);

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", e.getMessage());
        }
    }

    public String getPrimaryKeyColumn(String tableName) {
        System.out.println("Fetching primary key for table: " + tableName);
        String primaryKey = null;

        // Try fetching primary key from metadata
        try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, null, tableName)) {
            if (rs.next()) {
                primaryKey = rs.getString("COLUMN_NAME");
                System.out.println("Primary key retrieved from metadata: " + primaryKey);
                return primaryKey;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching primary key from metadata: " + e.getMessage());
        }

        // Fallback: Use SHOW KEYS query
        String fallbackQuery = "SHOW KEYS FROM " + tableName + " WHERE Key_name = 'PRIMARY'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(fallbackQuery)) {
            if (rs.next()) {
                primaryKey = rs.getString("Column_name");
                System.out.println("Primary key retrieved from fallback query: " + primaryKey);
                return primaryKey;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching primary key from fallback query: " + e.getMessage());
        }

        System.err.println("No primary key found for table: " + tableName);
        return primaryKey;
    }

    public List<String> getSelectedColumns() {
        List<String> selectedColumns = new ArrayList<>();
        for (Node node : columnasContainer.getChildren()) {
            if (node instanceof ComboBox) {
                String column = ((ComboBox<String>) node).getValue();
                if (column != null && !column.isEmpty()) {
                    selectedColumns.add(column);
                }
            }
        }
        return selectedColumns;
    }

    public void eliminarRegistro() {
        ObservableList<String> filaSeleccionada = resultadoTable.getSelectionModel().getSelectedItem();

        if (filaSeleccionada != null) {
            String primaryKeyColumn = getPrimaryKeyColumn(currentTable);
            if (primaryKeyColumn == null) {
                System.err.println("No primary key found for table " + currentTable); // Debug log
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmación de eliminación");
            confirmacion.setHeaderText("¿Estás seguro de que deseas eliminar este registro?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    String id = filaSeleccionada.get(0); // Assuming the primary key is in the first column
                    String consulta = "DELETE FROM " + currentTable + " WHERE " + primaryKeyColumn + " = ?";
                    System.out.println("Generated SQL Query for Delete: " + consulta); // Debug log
                    System.out.println("Primary key value: " + id); // Debug log

                    try (PreparedStatement pstmt = connection.prepareStatement(consulta)) {
                        pstmt.setString(1, id);
                        int filasAfectadas = pstmt.executeUpdate();
                        System.out.println("Rows deleted: " + filasAfectadas); // Debug log

                        if (filasAfectadas > 0) {
                            mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminación exitosa", "El registro ha sido eliminado.");
                            ejecutarConsulta(); // Refresh the table
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error during delete: " + e.getMessage()); // Debug log
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", e.getMessage());
                }
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Por favor, selecciona un registro para eliminar.");
        }
    }

    public void guardarCambios() {
        boolean hayErrores = false;

        for (ObservableList<String> fila : filasTemp) {
            try {
                String primaryKeyColumn = getPrimaryKeyColumn(currentTable);
                if (primaryKeyColumn == null) {
                    System.err.println("No primary key found for table " + currentTable + ". Skipping this row.");
                    continue;
                }

                // Perform UPDATE for existing rows
                StringBuilder updateQuery = new StringBuilder("UPDATE ").append(currentTable).append(" SET ");
                for (int i = 1; i < fila.size(); i++) {
                    if (i > 1) {
                        updateQuery.append(", ");
                    }
                    updateQuery.append(resultadoTable.getColumns().get(i).getText()).append(" = ?");
                }
                updateQuery.append(" WHERE ").append(primaryKeyColumn).append(" = ?");

                System.out.println("Generated SQL Query Template for Update: " + updateQuery); // Debug log

                try (PreparedStatement pstmt = connection.prepareStatement(updateQuery.toString())) {
                    for (int i = 1; i < fila.size(); i++) {
                        pstmt.setString(i, fila.get(i));
                    }
                    pstmt.setString(fila.size(), fila.get(0));
                    int filasAfectadas = pstmt.executeUpdate();
                    System.out.println("Rows updated: " + filasAfectadas); // Debug log
                }

            } catch (SQLException e) {
                hayErrores = true;
                System.err.println("Error during update: " + e.getMessage()); // Debug log
                mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar cambios", e.getMessage());
            }
        }

        if (!hayErrores) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado Exitoso", "Los cambios se han guardado correctamente.");
            ejecutarConsulta(); // Refresh the table
        }
        filasTemp.clear();
    }

    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void setMenuScene(Scene menuScene) {
        this.menuScene = menuScene;
    }

    public void regresar() {
        if (menuScene != null) {
            Stage stage = (Stage) regresarButton.getScene().getWindow();
            stage.setScene(menuScene);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo regresar al menú. Escena no configurada.");
        }
    }

    public void agregarCondicion(ActionEvent event) {
        String consultaCampo = consultaCampoComboBox.getValue();
        String operador = operadorComboBox.getValue();
        String valor = valorTextField.getText();

        if (consultaCampo == null || operador == null || (valor == null && !operador.contains("NULL"))) {
            mostrarAlerta(Alert.AlertType.WARNING, "Condición inválida", "Por favor selecciona un campo, operador y valor válidos.");
            return;
        }

        if (consultaGenerada.length() > 0) {
            consultaGenerada.append(" AND ");
        }
        consultaGenerada.append(consultaCampo).append(" ").append(operador).append(" ");
        if (!operador.contains("NULL")) {
            consultaGenerada.append("'").append(valor).append("'");
        }

        consultaGeneradaTextArea.setText(consultaGenerada.toString());
    }

    public void limpiarConsulta() {
        consultaGenerada.setLength(0);
        consultaGeneradaTextArea.clear();
        columnasContainer.getChildren().clear();
    }
}
