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
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private TextArea consultaGeneradaTextArea;
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
        }
    }

    public void setTableRelation(String relatedTable, boolean isRelationEnabled) {
        this.isRelationEnabled = isRelationEnabled;
        this.relatedTable = relatedTable;
        if (isRelationEnabled && relatedTable != null) {
            tabla2ComboBox.getSelectionModel().select(relatedTable);
            cargarCampos(relatedTable, campo2ComboBox);
        } else {
            tabla2ComboBox.setDisable(true);
            campo2ComboBox.setDisable(true);
        }
    }

    private void configurarEventos() {
        agregarCondicionButton.setOnAction(this::agregarCondicion);
        buscarButton.setOnAction(event -> ejecutarConsulta());
        limpiarButton.setOnAction(event -> limpiarConsulta());
        regresarButton.setOnAction(event -> regresar());
        agregarButton.setOnAction(event -> agregarRegistro());
        eliminarButton.setOnAction(event -> eliminarRegistro());
        guardarButton.setOnAction(event -> guardarCambios());
        tabla1ComboBox.setOnAction(event -> cargarCampos(tabla1ComboBox.getValue(), campo1ComboBox));
    }

    private void agregarRegistro() {
        ObservableList<String> nuevaFila = FXCollections.observableArrayList();
        for (TableColumn<ObservableList<String>, ?> columna : resultadoTable.getColumns()) {
            nuevaFila.add("");
        }
        tableData.add(nuevaFila);
        resultadoTable.setItems(tableData);
    }

    private void cargarCampos(String tabla, ComboBox<String> campoComboBox) {
        if (tabla != null && !tabla.isEmpty()) {
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM " + tabla)) {
                ObservableList<String> campos = FXCollections.observableArrayList();
                while (rs.next()) {
                    campos.add(rs.getString("Field"));
                }
                campoComboBox.setItems(campos);
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar campos", e.getMessage());
            }
        }
    }

    private void ejecutarConsulta() {
        String consultaBase = "SELECT * FROM " + currentTable;
        if (isRelationEnabled && relatedTable != null) {
            String campo1 = campo1ComboBox.getValue();
            String campo2 = campo2ComboBox.getValue();
            consultaBase += " JOIN " + relatedTable + " ON " + currentTable + "." + campo1 + " = " + relatedTable + "." + campo2;
        }
        if (consultaGenerada.length() > 0) {
            consultaBase += " WHERE " + consultaGenerada;
        }

        cargarDatosEnTabla(consultaBase);
    }

    private void cargarDatosEnTabla(String consulta) {
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

    private String getPrimaryKeyColumn(String tableName) {
        try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, null, tableName)) {
            if (rs.next()) {
                return rs.getString("COLUMN_NAME");
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al obtener la clave primaria", e.getMessage());
        }
        return null;
    }

    private void eliminarRegistro() {
        ObservableList<String> filaSeleccionada = resultadoTable.getSelectionModel().getSelectedItem();

        if (filaSeleccionada != null) {
            String primaryKeyColumn = getPrimaryKeyColumn(currentTable);

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmación de eliminación");
            confirmacion.setHeaderText("¿Estás seguro de que deseas eliminar este registro?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    String consulta;
                    if (primaryKeyColumn != null) {
                        // tenemos primary key 
                        String id = filaSeleccionada.get(0);
                        consulta = "DELETE FROM " + currentTable + " WHERE " + primaryKeyColumn + " = ?";
                        try (PreparedStatement pstmt = connection.prepareStatement(consulta)) {
                            pstmt.setString(1, id);
                            int filasAfectadas = pstmt.executeUpdate();

                            if (filasAfectadas > 0) {
                                resultadoTable.getItems().remove(filaSeleccionada);
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminación exitosa", "El registro ha sido eliminado correctamente.");
                            } else {
                                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró el registro para eliminar.");
                            }
                        }
                    } else {
                        // No primary key
                        StringBuilder whereClause = new StringBuilder();
                        ObservableList<TableColumn<ObservableList<String>, ?>> columnas = resultadoTable.getColumns();
                        for (int i = 0; i < columnas.size(); i++) {
                            if (i > 0) {
                                whereClause.append(" AND ");
                            }
                            String columnName = columnas.get(i).getText();
                            whereClause.append(columnName).append(" = ?");
                        }

                        consulta = "DELETE FROM " + currentTable + " WHERE " + whereClause;
                        try (PreparedStatement pstmt = connection.prepareStatement(consulta)) {
                            for (int i = 0; i < filaSeleccionada.size(); i++) {
                                pstmt.setString(i + 1, filaSeleccionada.get(i));
                            }
                            int filasAfectadas = pstmt.executeUpdate();

                            if (filasAfectadas > 0) {
                                resultadoTable.getItems().remove(filaSeleccionada);
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminación exitosa", "El registro ha sido eliminado correctamente.");
                            } else {
                                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró el registro para eliminar.");
                            }
                        }
                    }
                } catch (SQLException e) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", e.getMessage());
                }
            }
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Por favor, selecciona un registro para eliminar.");
        }
    }

    private void actualizarCelda(TableColumn.CellEditEvent<ObservableList<String>, String> event) {
        int filaIndex = event.getTablePosition().getRow();
        int columnaIndex = event.getTablePosition().getColumn();
        ObservableList<String> fila = tableData.get(filaIndex);

        fila.set(columnaIndex, event.getNewValue());
        filasTemp.add(fila);
    }

    private void guardarCambios() {
        boolean hayErrores = false;
        for (ObservableList<String> fila : filasTemp) {
            try {
                String primaryKeyColumn = getPrimaryKeyColumn(currentTable);

                StringBuilder consulta = new StringBuilder("UPDATE " + currentTable + " SET ");
                boolean hayModificaciones = false;

                for (int i = 1; i < resultadoTable.getColumns().size(); i++) {
                    TableColumn<ObservableList<String>, ?> columna = resultadoTable.getColumns().get(i);
                    String nombreColumna = columna.getText();
                    String valor = fila.get(i);

                    if (valor != null && !valor.isEmpty()) {
                        if (hayModificaciones) {
                            consulta.append(", ");
                        }
                        consulta.append(nombreColumna).append(" = '").append(valor).append("'");
                        hayModificaciones = true;
                    }
                }

                if (hayModificaciones) {
                    if (primaryKeyColumn != null) {
                        // Use primary key for WHERE clause
                        String id = fila.get(0);
                        consulta.append(" WHERE ").append(primaryKeyColumn).append(" = ?");
                        try (PreparedStatement pstmt = connection.prepareStatement(consulta.toString())) {
                            pstmt.setString(1, id);
                            pstmt.executeUpdate();
                        }
                    } else {
                        // No primary key, construct WHERE clause using all columns
                        StringBuilder whereClause = new StringBuilder();
                        ObservableList<TableColumn<ObservableList<String>, ?>> columnas = resultadoTable.getColumns();
                        for (int i = 0; i < columnas.size(); i++) {
                            if (i > 0) {
                                whereClause.append(" AND ");
                            }
                            String columnName = columnas.get(i).getText();
                            whereClause.append(columnName).append(" = ?");
                        }

                        consulta.append(" WHERE ").append(whereClause);
                        try (PreparedStatement pstmt = connection.prepareStatement(consulta.toString())) {
                            for (int i = 0; i < fila.size(); i++) {
                                pstmt.setString(i + 1, fila.get(i));
                            }
                            pstmt.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                hayErrores = true;
                mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar cambios", e.getMessage());
            }
        }

        if (!hayErrores) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Confirmación de Guardado", "Los cambios se han guardado correctamente.");
        }

        filasTemp.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public void setMenuScene(Scene menuScene) {
        this.menuScene = menuScene;
    }

    private void regresar() {
        if (menuScene != null) {
            Stage stage = (Stage) regresarButton.getScene().getWindow();
            stage.setScene(menuScene);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo regresar al menú. Escena no configurada.");
        }
    }

    private void agregarCondicion(ActionEvent event) {
        String campo = campo1ComboBox.getValue();
        String operador = operadorComboBox.getValue();
        String valor = valorTextField.getText();

        if (campo == null || operador == null || (operador.contains("NULL") || (valor != null && !valor.isEmpty()))) {
            if (consultaGenerada.length() > 0) {
                consultaGenerada.append(" AND ");
            }
            consultaGenerada.append(campo).append(" ").append(operador).append(" ");
            if (!operador.contains("NULL")) {
                consultaGenerada.append("'").append(valor).append("'");
            }
            consultaGeneradaTextArea.setText(consultaGenerada.toString());
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "Condición inválida", "La condición no puede ser vacía o incorrecta.");
        }
    }

    private void limpiarConsulta() {
        consultaGenerada.setLength(0);
        consultaGeneradaTextArea.clear();
    }
}
