package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class AdminController implements Initializable {

    @FXML
    private Button bPregledPredstave;

    @FXML
    private Button bPregledRadnika;

    @FXML
    private Button bPregledRepertoara;

    @FXML 
    private Button buttonStatistika;

    @FXML
    private Button bIzlaz;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //ConnectionPool.getInstance();//da otvori odmah konekciju ka bazi
        buttonStatistika.setOnAction(e -> izaberiFolderZaStatistiku());
        bPregledPredstave.setStyle("-fx-background-color: #90c8ff");
        bPregledRepertoara.setStyle("-fx-background-color: #e6e6e6");
        bPregledRadnika.setStyle("-fx-background-color: #90c8ff");
        
        buttonStatistika.setStyle("-fx-background-color: #e6e6e6");
        bPregledPredstave.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Theatre Mask_48px.png"))));
        buttonStatistika.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Statistics_48px.png"))));
        bPregledRepertoara.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Report Card_48px.png"))));
        bPregledRadnika.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Find User Male_48px.png"))));
        bIzlaz.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_logout.png"))));
    }

    private void izaberiFolderZaStatistiku() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setTitle("Izaberite lokaciju izvjestaja");
        File folder = directoryChooser.showDialog((Stage) buttonStatistika.getScene().getWindow());
        if (folder != null) {
            IzvjestajProdatihKarataController k = new IzvjestajProdatihKarataController(folder);
            k.metoda();
        }
    }

    @FXML
    void nazadNaLoginFormu(ActionEvent event) {
        try {
            Parent loginController = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene loginScene = new Scene(loginController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setTitle("Logovanje");
            window.setScene(loginScene);
            window.show();
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pregledRadnikaAction(ActionEvent event) {
        try {
            Parent radnikController = FXMLLoader.load(getClass().getResource("/view/PregledRadnika.fxml"));
            Scene radnikScene = new Scene(radnikController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setTitle("Radnici");
            window.setScene(radnikScene);
            window.show();
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pregledRepertoaraAction(ActionEvent event) {

        try {
            Parent repertoarController = FXMLLoader.load(getClass().getResource("/view/PregledSvihRepertoara.fxml"));

            Scene repertoarScene = new Scene(repertoarController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setTitle("Repertoar");
            window.setScene(repertoarScene);
            window.setResizable(false);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pregledPredstavaAction(ActionEvent event) {
        try {
            Parent predstavaController = FXMLLoader.load(getClass().getResource("/view/PregledPredstava.fxml"));

            Scene predstavaScene = new Scene(predstavaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setTitle("Predstave");
            window.setScene(predstavaScene);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
