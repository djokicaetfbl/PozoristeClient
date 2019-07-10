package main;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import controller.LogInController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Pozoriste extends Application {
    public static final String HOST="127.0.0.1";
    public static final int PORT=9000;
    public static final int AUTH_PORT=9001;

    @Override
    public void start(Stage stage) throws Exception {
        Handler handler = null;
        try {
            handler = new FileHandler("./error.log");
            Logger.getLogger("").addHandler(handler);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Pozoriste.class.getName()).log(Level.SEVERE, null, ex);
        }

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LogIn.fxml"));
        LogInController logInController1 = null;
        loader.setController(logInController1);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Logovanje");
        stage.getIcons().add(new Image(Pozoriste.class.getResourceAsStream("/resursi/drama.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
