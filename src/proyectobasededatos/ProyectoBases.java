/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyectobasededatos;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProyectoBases extends Application {

    @Override
    public void start(Stage Base) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("LogIn.fxml"));
            Scene scene = new Scene(root);
            Base.setScene(scene);
            Base.setTitle("QueryEase");
            Base.setResizable(false);
            Base.show();
        } catch (IOException e) {
            System.exit(1); // Exit the application
        }
    }
    
    public static void main(String[] args) {                         
        launch(args);                                  
    }
}
