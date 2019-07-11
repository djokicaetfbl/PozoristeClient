/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.dto.GostujucaPredstava;
import model.dto.Predstava;
import util.ProtocolMessages;

import javax.swing.plaf.synth.Region;

public class PregledPredstavaController implements Initializable {

    @FXML 
    private TableView<Predstava> tablePredstave;

    @FXML 
    private ComboBox<String> comboBoxTip; 

    @FXML 
    private Button buttonDodaj; 

    @FXML 
    private TableView<GostujucaPredstava> tableGostujucePredstave;

    @FXML 
    private TextArea textAreaOpisPredstave; 

    @FXML 
    private TextArea textAreaOpisGostujucePredstave; 

    @FXML 
    private TextArea textAreaGlumciGostujucePredstave; 

    @FXML 
    private Button buttonIzmijeniGostujucuPredstavu; 

    @FXML 
    private Button buttonPregledaj; 

    @FXML 
    private Button buttonIzmijeniPredstavu; 

    @FXML
    private Button buttonNazad;

    @FXML
    private TableColumn<Predstava, String> nazivColumn;
    @FXML
    private TableColumn<Predstava, String> tipColumn;
    @FXML
    private TableColumn<GostujucaPredstava, String> nazivGpColumn;
    @FXML
    private TableColumn<GostujucaPredstava, String> tipGpColumn;
    @FXML
    private TableColumn<GostujucaPredstava, String> pisacColumn;
    @FXML
    private TableColumn<GostujucaPredstava, String> reziserColumn;

    public static ObservableList<Predstava> predstaveObservableList = FXCollections.observableArrayList();
    public static ObservableList<GostujucaPredstava> gostujucePredstaveObservableList = FXCollections.observableArrayList();
    public static ObservableList<String> tipovi = FXCollections.observableArrayList();

    @FXML
    void nazadAction(ActionEvent event) {
         try {
            Parent dodajRadnikaController = FXMLLoader.load(getClass().getResource("/view/Admin.fxml"));

            Scene dodajRadnikaScene = new Scene(dodajRadnikaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.setResizable(false);
            window.setTitle("Administrator");
            window.show();

             Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
             window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
             window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(PregledPredstavaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void dodajAction(ActionEvent event) {
        String temp = comboBoxTip.getValue();
        if(!comboBoxTip.getSelectionModel().isEmpty()) {
            if ("Predstava".equals(temp)) {
                DodajPredstavuController.setDomacaPredstava(true);
                DodajPredstavuController.setDodavanje(true);
                DodajPredstavuController.setPredstava(null);
            } else {
                DodajPredstavuController.setDomacaPredstava(false);
                DodajPredstavuController.setDodavanje(true);
                DodajPredstavuController.setPredstava(null);
            }
            otvoriDodajPredstavu(event);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Izaberite tip predstave!");
            alert.showAndWait();
        }
    }

    @FXML
    void izmijeniGostujucuPredstavuAction(ActionEvent event) {
        ObservableList<GostujucaPredstava> izabranaVrsta, predstaveObservableList;
        predstaveObservableList = tableGostujucePredstave.getItems();
        izabranaVrsta = tableGostujucePredstave.getSelectionModel().getSelectedItems();
        GostujucaPredstava izabranaPredstava = (GostujucaPredstava) izabranaVrsta.get(0);
        if (izabranaPredstava != null) {
            DodajPredstavuController.setDodavanje(false);
            DodajPredstavuController.setDomacaPredstava(false);
            DodajPredstavuController.setPredstava(izabranaPredstava);
            otvoriDodajPredstavu(event);
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Nije selektovana predstava!");
            alert.showAndWait();
        }
    }

    @FXML
    void pregledajAction(ActionEvent event) {
        ObservableList<Predstava> izabranaVrsta, predstaveObservableList;
        predstaveObservableList = tablePredstave.getItems();
        izabranaVrsta = tablePredstave.getSelectionModel().getSelectedItems();
        Predstava izabranaPredstava = (Predstava) izabranaVrsta.get(0);
        if (izabranaPredstava != null) {
            DodavanjeAngazmanaController.setPredstava(izabranaPredstava);
            otvoriAngazmane(event);
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Nije selektovana predstava!");
            alert.showAndWait();
        }
    }

    @FXML
    void izmijeniPredstavuAction(ActionEvent event) {
        ObservableList<Predstava> izabranaVrsta, predstaveObservableList;
        predstaveObservableList = tablePredstave.getItems();
        izabranaVrsta = tablePredstave.getSelectionModel().getSelectedItems();
        Predstava izabranaPredstava = (Predstava) izabranaVrsta.get(0);
        if (izabranaPredstava != null) {
            DodajPredstavuController.setDodavanje(false);
            DodajPredstavuController.setDomacaPredstava(true);
            DodajPredstavuController.setPredstava(izabranaPredstava);
            otvoriDodajPredstavu(event);
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Nije selektovana predstava!");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tablePredstave.setStyle("-fx-selection-bar: #90c8ff; -fx-selection-bar-non-focused: #acfaad;");
        tableGostujucePredstave.setStyle("-fx-selection-bar: #90c8ff; -fx-selection-bar-non-focused: #acfaad;");
        textAreaOpisPredstave.setStyle("-fx-background-color: #acfaad");
        textAreaOpisGostujucePredstave.setStyle("-fx-background-color: #acfaad");
        textAreaGlumciGostujucePredstave.setStyle("-fx-background-color: #acfaad");
        buttonIzmijeniPredstavu.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_editproperty_48px.png"))));
        buttonIzmijeniGostujucuPredstavu.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_editproperty_48px.png"))));
        buttonDodaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_plus.png"))));
        buttonNazad.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
        buttonPregledaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/info.png"))));
        try {
            // odredi adresu racunara sa kojim se povezujemo
            // (povezujemo se sa nasim racunarom)
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            // otvori socket prema drugom racunaru
            Socket sock = new Socket(addr, 9000);
            // inicijalizuj ulazni stream
            DataInputStream in = new DataInputStream(
                    sock.getInputStream());
            // inicijalizuj izlazni stream
            DataOutputStream out = new DataOutputStream(
                    sock.getOutputStream());
            tipovi.clear();
            tipovi.addAll("Predstava", "Gostujuca predstava");
            comboBoxTip.setItems(tipovi);
            textAreaOpisPredstave.setEditable(false);
            textAreaOpisGostujucePredstave.setEditable(false);
            textAreaGlumciGostujucePredstave.setEditable(false);
            gostujucePredstaveObservableList.clear();
            predstaveObservableList.clear();

            out.writeUTF(ProtocolMessages.GOSTUJUCE_PREDSTAVE.getMessage());
            String gostujucePredstaveResponse = in.readUTF();
            List<GostujucaPredstava> gostujucaPredstavaList = new LinkedList<>();
            if (gostujucePredstaveResponse.startsWith(ProtocolMessages.GOSTUJUCE_PREDSTAVE_RESPONSE.getMessage())) {
                String[] predstaveLines = gostujucePredstaveResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < predstaveLines.length; i++) {
                    String[] predstavaString = predstaveLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    GostujucaPredstava predstava = null;
                    if (i == 0) {
//          GostujucaPredstava(rs.getInt("id"), rs.getString("naziv"), rs.getString("opis"), rs.getString("tip"), rs.getString("pisac"), rs.getString("reziser"), rs.getString("glumci"));
                        predstava = new GostujucaPredstava(Integer.parseInt(predstavaString[1]), predstavaString[2], predstavaString[3], predstavaString[4], predstavaString[5], predstavaString[6], predstavaString[7]);
                        gostujucaPredstavaList.add(predstava);
                    } else {
                        predstava = new GostujucaPredstava(Integer.parseInt(predstavaString[0]), predstavaString[1], predstavaString[2], predstavaString[3], predstavaString[4], predstavaString[5], predstavaString[6]);
                        gostujucaPredstavaList.add(predstava);
                    }
                }
            } else {
                System.out.println("Greska pri pristupu Serveru!");
            }
            gostujucePredstaveObservableList.addAll(gostujucaPredstavaList);
            out.writeUTF(ProtocolMessages.PREDSTAVE.getMessage());
            String responsePredstave = in.readUTF();
            List<Predstava> predstavaList = new LinkedList<>();
            if (responsePredstave.startsWith(ProtocolMessages.PREDSTAVE_RESPONSE.getMessage())) {
                String[] predstaveLines = responsePredstave.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < predstaveLines.length; i++) {
                    String[] predstavaString = predstaveLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    Predstava predstava = null;
                    if (i == 0) {
                        //Predstava(String naziv, String opis, String tip)
                        predstava = new Predstava(predstavaString[2], predstavaString[3], predstavaString[4]);
                        predstava.setId(Integer.parseInt(predstavaString[1]));
                        predstavaList.add(predstava);
                    } else {
                        predstava = new Predstava(predstavaString[1], predstavaString[2], predstavaString[3]);
                        predstava.setId(Integer.parseInt(predstavaString[0]));
                        predstavaList.add(predstava);
                    }
                }
            } else {
                System.out.println("Greska pri pristupu Serveru!");
            }
            predstaveObservableList.addAll(predstavaList);
            ubaciKoloneUTabeluGostujucaPredstava(gostujucePredstaveObservableList);
            ubaciKoloneUTabeluPredstava(predstaveObservableList);
            tableGostujucePredstave.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    GostujucaPredstava gp = (GostujucaPredstava) tableGostujucePredstave.getSelectionModel().getSelectedItem();
                    textAreaOpisGostujucePredstave.setText(((GostujucaPredstava) tableGostujucePredstave.getSelectionModel().getSelectedItem()).getOpis());
                    textAreaGlumciGostujucePredstave.setText("Glumci:   " + ((GostujucaPredstava) tableGostujucePredstave.getSelectionModel().getSelectedItem()).getGlumci());
                }
            });
            tablePredstave.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    Predstava p = (Predstava) tablePredstave.getSelectionModel().getSelectedItem();
                    textAreaOpisPredstave.setText(((Predstava) tablePredstave.getSelectionModel().getSelectedItem()).getOpis());

                }
            });
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void ubaciKoloneUTabeluGostujucaPredstava(ObservableList gostujucePredstaveObservableList) {
        nazivGpColumn = new TableColumn("Naziv");
        nazivGpColumn.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        nazivGpColumn.setPrefWidth(210);

        tipGpColumn = new TableColumn("Tip");
        tipGpColumn.setCellValueFactory(new PropertyValueFactory<>("tip"));

        pisacColumn = new TableColumn("Pisac");
        pisacColumn.setCellValueFactory(new PropertyValueFactory<>("pisac"));

        reziserColumn = new TableColumn("Režiser");
        reziserColumn.setCellValueFactory(new PropertyValueFactory<>("reziser"));

        tableGostujucePredstave.setItems(gostujucePredstaveObservableList);
        tableGostujucePredstave.getColumns().addAll(nazivGpColumn, tipGpColumn, pisacColumn, reziserColumn);

    }

    private void ubaciKoloneUTabeluPredstava(ObservableList predstaveObservableList) {
        nazivColumn = new TableColumn("Naziv");
        nazivColumn.setCellValueFactory(new PropertyValueFactory<>("naziv"));
        nazivColumn.setPrefWidth(310);

        tipColumn = new TableColumn("Tip");
        tipColumn.setCellValueFactory(new PropertyValueFactory<>("tip"));
        tipColumn.setPrefWidth(310);

        tablePredstave.setItems(predstaveObservableList);
        tablePredstave.getColumns().addAll(nazivColumn, tipColumn);

    }

    private void otvoriDodajPredstavu(ActionEvent event) {
        try {
            Parent dodajRadnikaController = FXMLLoader.load(getClass().getResource("/view/DodajPredstavu.fxml"));

            Scene dodajRadnikaScene = new Scene(dodajRadnikaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.setResizable(false);
            window.setTitle("Predstava");
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(PregledPredstavaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void otvoriAngazmane(ActionEvent event){
               try {
            Parent dodajRadnikaController = FXMLLoader.load(getClass().getResource("/view/DodavanjeAngazmana.fxml"));
            Scene dodajRadnikaScene = new Scene(dodajRadnikaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.setResizable(false);
            window.setTitle("Angažman");
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(PregledPredstavaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
