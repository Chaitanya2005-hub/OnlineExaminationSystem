package com.stark.exam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Define the path to the FXML file
            // Note: The leading '/' is required to look from the classpath root (resources folder)
            String fxmlPath = "/fxml/login.fxml";

            // 2. Debug: Print where we are looking
            System.out.println("Attempting to load FXML from: " + fxmlPath);

            // 3. Get the URL resource
            URL fxmlUrl = getClass().getResource(fxmlPath);

            // 4. Check if file was found
            if (fxmlUrl == null) {
                System.err.println("\nCRITICAL ERROR: login.fxml not found!");
                System.err.println("Please check the following:");
                System.err.println("1. Does 'src/main/resources/fxml/login.fxml' exist?");
                System.err.println("2. Is the 'resources' folder marked as a Resources Root in IntelliJ?");
                System.err.println("3. Did you Rebuild the project? (Build -> Rebuild Project)\n");
                throw new RuntimeException("FXML file missing: " + fxmlPath);
            }

            // 5. Load the file
            Parent root = FXMLLoader.load(fxmlUrl);

            // 6. Show the Scene
            Scene scene = new Scene(root);
            primaryStage.setTitle("Stark Exam System - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace(); // Print full error to console
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
