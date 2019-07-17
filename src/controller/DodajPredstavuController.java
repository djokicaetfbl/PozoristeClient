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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.Azuriranje;
import model.dto.GostujucaPredstava;
import model.dto.Kreiranje;
import model.dto.Predstava;
import util.ProtocolMessages;

public class DodajPredstavuController implements Initializable {

    @FXML
    private Label labelNaziv;

    @FXML
    private Label labelOpis;

    @FXML
    private Label labelTip;

    @FXML
    private Label labelPisac;

    @FXML
    private Label labelReziser;

    @FXML
    private Label labelGlumci;

    @FXML
    private TextField textFieldNaziv;

    @FXML
    private TextArea textAreaOpis;

    @FXML
    private TextField textFieldTip;

    @FXML
    private TextField textFieldPisac;

    @FXML
    private TextField textFieldReziser;

    @FXML
    private TextArea textAreaGlumci;

    @FXML
    private Button buttonPregledajAngazman;

    @FXML
    private Button buttonOK;

    @FXML
    private Button bNazad;

    @FXML
    private ComboBox<String> cmbTIPPREDSTAVE;

    private static boolean domacaPredstava;
    private static boolean dodavanje;
    private static Object predstava;

    public static void setPredstava(Object p) {
        predstava = p;
    }

    public static void setDomacaPredstava(boolean domacaPr) {
        domacaPredstava = domacaPr;
    }

    public static void setDodavanje(boolean dodaj) {
        dodavanje = dodaj;
    }

    @FXML
    void pregledajAngazmanAction(ActionEvent event) throws  IOException {
        // odredi adresu racunara sa kojim se povezujemo
        // (povezujemo se sa nasim racunarom)
        InetAddress addr = InetAddress.getByName(Pozoriste.HOST);
        // otvori socket prema drugom racunaru
        Socket sock = new Socket(addr, Pozoriste.PORT);
        // inicijalizuj ulazni stream
        DataInputStream in = new DataInputStream(
                sock.getInputStream());
        // inicijalizuj izlazni stream
        DataOutputStream out = new DataOutputStream(
                sock.getOutputStream());
        if (!textFieldNaziv.getText().isEmpty() && !cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem().isEmpty() && !textAreaOpis.getText().isEmpty()) {
            Predstava domaca = (Predstava) predstava;
            domaca.setNaziv(textFieldNaziv.getText());
            domaca.setOpis(textAreaOpis.getText());
            domaca.setTip(cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem());
            if (provjeraPredstava()) {
                //Predstava(String naziv, String opis, String tip)
                
                out.writeUTF(ProtocolMessages.AZURIRAJ_PREDSTAVU.getMessage()+domaca.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+domaca.getOpis()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+domaca.getTip()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+domaca.getId());
                if(in.readUTF().startsWith(ProtocolMessages.AZURIRAJ_PREDSTAVU_OK.getMessage())){
                    System.out.println("Predstava uspjesno azurirana");
                }
                else {
                    System.out.println("Predstava nije azurirana");
                }
                Azuriranje azuriranje = new Azuriranje(domaca.getId(), null, null, LogInController.idLogovanog);
                //Azuriranje(Integer idPredstave, Integer idRepertoara, Integer idGostujucePredstave, Integer idRadnik)
                out.writeUTF(ProtocolMessages.DODAJ_AZURIRANJE.getMessage()+azuriranje.getIdPredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+azuriranje.getIdRepertoara()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+azuriranje.getIdGostujucePredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+azuriranje.getIdRadnik());
                if(in.readUTF().startsWith(ProtocolMessages.DODAJ_AZURIRANJE_OK.getMessage())){
                    System.out.println("Azuriranje uspjesno dodato");
                }
                else {
                    System.out.println("Azuriranje nije dodato");
                }
                DodavanjeAngazmanaController.setPredstava(domaca);
                otvoriAngazmane(event);
            }
        } else {
            upozorenjePoljaSuPrazna();
        }
    }

    @FXML
    void okAction(ActionEvent event) throws IOException{
        // odredi adresu racunara sa kojim se povezujemo
        // (povezujemo se sa nasim racunarom)
        InetAddress addr = InetAddress.getByName(Pozoriste.HOST);
        // otvori socket prema drugom racunaru
        Socket sock = new Socket(addr, Pozoriste.PORT);
        // inicijalizuj ulazni stream
        DataInputStream in = new DataInputStream(
                sock.getInputStream());
        // inicijalizuj izlazni stream
        DataOutputStream out = new DataOutputStream(
                sock.getOutputStream());
        if (dodavanje) {
            if (domacaPredstava) {
                if (!textFieldNaziv.getText().isEmpty() && !cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem().isEmpty() && !textAreaOpis.getText().isEmpty()) {
                    Predstava predstava = new Predstava(textFieldNaziv.getText(), textAreaOpis.getText(), cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem());
                    if (provjeraPredstava()) {
                        out.writeUTF(ProtocolMessages.DODAJ_PREDSTAVU.getMessage()+predstava.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getOpis()+
                                ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getTip());
                        if(in.readUTF().startsWith(ProtocolMessages.DODAJ_PREDSTAVU_OK.getMessage())){
                            System.out.println("Predstava uspjesno dodata");
                        }
                        else {
                            System.out.println("Predstava nije dodata");
                        }
                        Kreiranje kreiranje = new Kreiranje(predstava.getId(), null, null, LogInController.idLogovanog);
                        String idPredst=kreiranje.getIdPredstave()==null?"NULL":String.valueOf(predstava.getId());
                        String idLog=LogInController.idLogovanog==null?"NULL":String.valueOf(LogInController.idLogovanog);
                        out.writeUTF(ProtocolMessages.DODAJ_KREIRANJE.getMessage()+idPredst+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+"NULL"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()
                                +"NULL"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+idLog+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getNaziv());
                        String resp=in.readUTF();
                        if(resp.startsWith(ProtocolMessages.DODAJ_KREIRANJE_OK.getMessage())){
                            System.out.println("Kreiranje uspjesno dodato");
                            predstava.setId(Integer.parseInt(resp.split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1]));
                        }
                        else {
                            System.out.println("Kreiranje nije dodato");
                        }
                        DodavanjeAngazmanaController.setPredstava(predstava);
                        otvoriAngazmane(event);
                    }

                } else {
                    upozorenjePoljaSuPrazna();
                }
            } else {
                if (!textFieldNaziv.getText().isEmpty() && !cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem().isEmpty() && !textAreaOpis.getText().isEmpty() && !textAreaGlumci.getText().isEmpty() && !textFieldPisac.getText().isEmpty() && !textFieldReziser.getText().isEmpty()) {
                    GostujucaPredstava gostujucaPredstava = new GostujucaPredstava(textFieldNaziv.getText(), textAreaOpis.getText(), cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem(), textFieldPisac.getText(), textFieldReziser.getText(), textAreaGlumci.getText());
                    if (provjeraGostujucaPredstava()) {
                        out.writeUTF(ProtocolMessages.DODAJ_GOSTUJUCU_PREDSTAVU.getMessage()+gostujucaPredstava.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujucaPredstava.getOpis()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                                gostujucaPredstava.getTip()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujucaPredstava.getPisac()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujucaPredstava.getReziser()+
                                ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujucaPredstava.getGlumci());
                        if(in.readUTF().startsWith(ProtocolMessages.DODAJ_GOSTUJUCU_PREDSTAVU_OK.getMessage())){
                            System.out.println("GP uspjesno dodata");
                        }
                        else {
                            System.out.println("GP nije dodata");
                        }
                        Kreiranje kreiranje = new Kreiranje(null, null, gostujucaPredstava.getId(), LogInController.idLogovanog);
                        String kreirId=kreiranje.getIdGostujucePredstave()==null?"NULL":String.valueOf(kreiranje.getIdGostujucePredstave());
                        String idLog=LogInController.idLogovanog==null?"NULL":String.valueOf(LogInController.idLogovanog);
                        out.writeUTF(ProtocolMessages.DODAJ_KREIRANJE.getMessage()+"NULL"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+"NULL"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()
                                +kreirId+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+idLog+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujucaPredstava.getNaziv());
                        if(in.readUTF().startsWith(ProtocolMessages.DODAJ_KREIRANJE_OK.getMessage())){
                            System.out.println("Kreiranje uspjesno dodato");
                        }
                        else {
                            System.out.println("Kreiranje nije dodato");
                        }
                        nazadNaPregledPredstava(event);
                    }
                } else {
                    upozorenjePoljaSuPrazna();
                }
            }
        } else {

            if (!textFieldNaziv.getText().isEmpty() && !cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem().isEmpty() && !textAreaOpis.getText().isEmpty() && !textAreaGlumci.getText().isEmpty() && !textFieldPisac.getText().isEmpty() && !textFieldReziser.getText().isEmpty()) {
                GostujucaPredstava gostujuca = (GostujucaPredstava) predstava;
                gostujuca.setNaziv(textFieldNaziv.getText());
                gostujuca.setOpis(textAreaOpis.getText());
                gostujuca.setTip(cmbTIPPREDSTAVE.getSelectionModel().getSelectedItem());
                gostujuca.setGlumci(textAreaGlumci.getText());
                gostujuca.setPisac(textFieldPisac.getText());
                gostujuca.setReziser(textFieldReziser.getText());
                if (provjeraGostujucaPredstava()) {
                    out.writeUTF(ProtocolMessages.AZURIRAJ_GOSTUJUCU_PREDSTAVU.getMessage()+gostujuca.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujuca.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                            gostujuca.getOpis()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujuca.getTip()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujuca.getPisac()+
                            ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujuca.getReziser()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+gostujuca.getGlumci());
                    if(in.readUTF().startsWith(ProtocolMessages.AZURIRAJ_GOSTUJUCU_PREDSTAVU_OK.getMessage())){
                        System.out.println("GP uspjesno azurirana");
                    }
                    else {
                        System.out.println("GP nije azurirana");
                    }
                    Azuriranje azuriranje = new Azuriranje(null, null, gostujuca.getId(), LogInController.idLogovanog);
                    out.writeUTF(ProtocolMessages.DODAJ_AZURIRANJE.getMessage()+"null"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+"null"+
                            ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+azuriranje.getIdGostujucePredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+azuriranje.getIdRadnik());
                    if(in.readUTF().startsWith(ProtocolMessages.DODAJ_AZURIRANJE_OK.getMessage())){
                        System.out.println("Azuriranje uspjesno dodato");
                    }
                    else {
                        System.out.println("Azuriranje nije dodato");
                    }
                    nazadNaPregledPredstava(event);
                }
            } else {
                upozorenjePoljaSuPrazna();
            }
        }
    }


    @FXML
    void nazadNaPregledPredstava(ActionEvent event) {
        try {
            Parent predstavaController = FXMLLoader.load(getClass().getResource("/view/PregledPredstava.fxml"));

            Scene predstavaScene = new Scene(predstavaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(predstavaScene);
            window.setResizable(false);
            window.setTitle("Predstave");
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(!dodavanje){
            buttonPregledajAngazman.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
        }
        cmbTIPPREDSTAVE.getItems().addAll("Drama","Komedija","Mjuzikl");
        cmbTIPPREDSTAVE.getSelectionModel().selectFirst();
        buttonOK.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
        bNazad.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
        if (dodavanje) {
            if (domacaPredstava) {
                labelPisac.setVisible(false);
                labelReziser.setVisible(false);
                labelGlumci.setVisible(false);
                textFieldPisac.setVisible(false);
                textFieldReziser.setVisible(false);
                textAreaGlumci.setVisible(false);
                buttonPregledajAngazman.setVisible(false);
            } else {
                buttonPregledajAngazman.setVisible(false);
            }
        } else {
            if (domacaPredstava) {
                labelPisac.setVisible(false);
                labelReziser.setVisible(false);
                labelGlumci.setVisible(false);
                textFieldPisac.setVisible(false);
                textFieldReziser.setVisible(false);
                textAreaGlumci.setVisible(false);
                buttonOK.setVisible(false);
                Predstava domaca = (Predstava) predstava;
                textFieldNaziv.setText(domaca.getNaziv());
                cmbTIPPREDSTAVE.getSelectionModel().select(domaca.getTip());
                //textFieldTip.setText(domaca.getTip());
                textAreaOpis.setText(domaca.getOpis());

            } else {
                buttonPregledajAngazman.setVisible(false);
                GostujucaPredstava gostujuca = (GostujucaPredstava) predstava;
                textFieldNaziv.setText(gostujuca.getNaziv());
                cmbTIPPREDSTAVE.getSelectionModel().select(gostujuca.getTip());
                //textFieldTip.setText(gostujuca.getTip());
                textAreaOpis.setText(gostujuca.getOpis());
                textFieldPisac.setText(gostujuca.getPisac());
                textFieldReziser.setText(gostujuca.getReziser());
                textAreaGlumci.setText(gostujuca.getGlumci());
            }
        }
    }

    private void upozorenjePoljaSuPrazna() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prazna polja !");
        alert.setHeaderText(null);
        alert.setContentText("Polja su prazna.");
        alert.showAndWait();
    }

    private void upozorenjeUnosDug(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Unos \"" + s + "\" je predugacak!");
        alert.showAndWait();
    }

    private boolean provjeraPredstava() {
        if (textFieldNaziv.getText().length() > 40) {
            upozorenjeUnosDug("Naziv");
            return false;
        }
       /* if (textFieldTip.getText().length() > 40) {
            upozorenjeUnosDug("Tip");
            return false;
        }*/
        return true;
    }

    private boolean provjeraGostujucaPredstava() {
        if (textFieldNaziv.getText().length() > 64) {
            upozorenjeUnosDug("Naziv");
            return false;
        }
       /* if (textFieldTip.getText().length() > 40) {
            upozorenjeUnosDug("Tip");
            return false;
        }*/
        if (textFieldPisac.getText().length() > 40) {
            upozorenjeUnosDug("Pisac");
            return false;
        }
        if (textFieldReziser.getText().length() > 40) {
            upozorenjeUnosDug("Reziser");
            return false;
        }
        return true;
    }

    private void otvoriAngazmane(ActionEvent event) {
        try {
            Parent predstavaController = FXMLLoader.load(getClass().getResource("/view/DodavanjeAngazmana.fxml"));

            Scene predstavaScene = new Scene(predstavaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(predstavaScene);
            window.setResizable(false);
            window.setTitle("Angazman");
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
