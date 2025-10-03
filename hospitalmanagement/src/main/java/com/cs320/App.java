package com.cs320;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"), 900, 600);
        stage.setTitle("Hospital Management System");
        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
    // Eğer saklanan scene şu anda gösterilen bir stage'e bağlıysa, root'u değiştir.
        boolean applied = false;
        if (scene != null) {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w instanceof Stage) {
                    Stage s = (Stage) w;
                    if (s.getScene() == scene) {
                        scene.setRoot(root);
                        applied = true;
                        break;
                    }
                }
            }
        }

    // Eğer saklanan scene gösterileni değilse (başka bir kod yeni bir Scene oluşturduysa),
    // aktif gösterilen stage için yeni bir Scene oluşturup saklanan scene referansını güncelle.
        if (!applied) {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w instanceof Stage) {
                    Stage s = (Stage) w;
                    if (s.isShowing()) {
                        Scene newScene = new Scene(root, Math.max(800, (int) s.getWidth()), Math.max(600, (int) s.getHeight()));
                        s.setScene(newScene);
                        scene = newScene;
                        applied = true;
                        break;
                    }
                }
            }
        }

    // Yedek: eğer pencere bulunamazsa (headless/test), saklanan scene'in root'unu güncelle veya yeni bir tane oluştur.
        if (!applied) {
            if (scene == null) {
                scene = new Scene(root, 900, 600);
            } else {
                scene.setRoot(root);
            }
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        
        String path = "/com/cs320/" + fxml + ".fxml";
        java.net.URL resource = App.class.getResource(path);
        if (resource == null) {
            throw new IOException("FXML resource not found: " + path + " (looked with App.class.getResource). Make sure the file exists under src/main/resources and is packaged.");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}