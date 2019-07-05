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
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.Azuriranje;
import model.dto.Kreiranje;
import model.dto.Repertoar;
import util.ProtocolMessages;

public class DodajRepertoarController implements Initializable {

    @FXML
    private Label lTerminRepertoara;

    @FXML
    private Button bDodajIgranje;

    @FXML
    private ComboBox<Integer> cmbGodina;

    @FXML
    private ComboBox<Integer> cmbMjesec;

    @FXML
    private Button bNazad;

    public static Repertoar repertoar = null;
    public static Integer mjesecRepertoara;
    public static Integer godinaRepertoara;

    private boolean dodajRepertoar() {
    	try {
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
	        if (cmbGodina.getSelectionModel().isEmpty() || cmbMjesec.getSelectionModel().isEmpty()) {
	            upozorenjeTermin();
	            return false;
	        }
	
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Calendar calendar = Calendar.getInstance();
	        try {
	            Integer mjesec = (cmbMjesec.getSelectionModel().getSelectedItem() + 1);
	            Integer godina = cmbGodina.getSelectionModel().getSelectedItem();
	            mjesecRepertoara = mjesec;
	            godinaRepertoara = godina;
	
	            if (!PregledSvihRepertoaraController.izmjenaRepertoara) {
	                repertoar = new Repertoar(0,
	                        new java.sql.Date(sdf.parse(cmbGodina.getSelectionModel().getSelectedItem().toString()
	                                        + "-" + Integer.valueOf(cmbMjesec.getSelectionModel().getSelectedItem()).toString() + "-1").getTime()));
	            } else {
	                repertoar = new Repertoar();
	                repertoar.setId(PregledSvihRepertoaraController.izabraniRepertoar.getId());
	                repertoar.setMjesecIGodina(new java.sql.Date(sdf.parse(cmbGodina.getSelectionModel().getSelectedItem().toString()
	                        + "-" + Integer.valueOf(cmbMjesec.getSelectionModel().getSelectedItem()).toString() + "-1").getTime()));
	                repertoar.setIgranja(PregledSvihRepertoaraController.izabraniRepertoar.getIgranja());
	            }
	        } catch (ParseException ex) {
	            Logger.getLogger(DodajRepertoarController.class.getName()).log(Level.SEVERE, null, ex);
	        }
	
	        final Repertoar simo = repertoar;
	
	        if (!PregledSvihRepertoaraController.izmjenaRepertoara) {
	            if (!PregledSvihRepertoaraController.repertoariObservableList.stream().filter(x -> sdf.format(x.getMjesecIGodina())
	                    .equals(sdf.format(simo.getMjesecIGodina()))).findAny().isPresent()) {
	                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                String strDate = dateFormat.format(repertoar.getMjesecIGodina());
	//              Repertoar(Integer id,Date mjesecIGodina)
	                out.writeUTF(ProtocolMessages.DODAJ_REPERTOAR.getMessage()+repertoar.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate);
	                if(in.readUTF().startsWith(ProtocolMessages.DODAJ_REPERTOAR_OK.getMessage())){
	                    System.out.println("Repertoar uspjesno dodan");
	                }
	                else {
	                    System.out.println("Repertoar nije dodan");
	                }
	                //RepertoarDAO.dodajRepertoar(repertoar);
	                Kreiranje kreiranje = new Kreiranje(null, repertoar.getId(), null, LogInController.idLogovanog);
	                out.writeUTF(ProtocolMessages.DODAJ_KREIRANJE.getMessage()+"null"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+kreiranje.getIdRepertoara()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()
	                        +"null"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+LogInController.idLogovanog);
	                if(in.readUTF().startsWith(ProtocolMessages.DODAJ_KREIRANJE_OK.getMessage())){
	                    System.out.println("Kreiranje uspjesno dodato");
	                }
	                else {
	                    System.out.println("Kreiranje nije dodato");
	                }
	                //KreiranjeDAO.dodajKreiranje(kreiranje);
	                return true;
	            } 
	            else {
	                upozorenjeRepertoar();
	                return false;
	            }
	        } else {
	            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	            String strDate = dateFormat.format(repertoar.getMjesecIGodina());
	            out.writeUTF(ProtocolMessages.IZMIJENI_REPERTOAR.getMessage()+repertoar.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate);
	            if(in.readUTF().startsWith(ProtocolMessages.IZMIJENI_REPERTOAR_OK.getMessage())){
	                System.out.println("Repertoar uspjesno izmijenjen");
	            }
	            else {
	                System.out.println("Repertoar nije izmijenjen");
	            }
	            //RepertoarDAO.izmjeniRepertoar(repertoar);
	            Azuriranje azuriranje = new Azuriranje(null, repertoar.getId(), null, LogInController.idLogovanog);
	            out.writeUTF(ProtocolMessages.DODAJ_AZURIRANJE.getMessage()+"null"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+repertoar.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
	                    "null"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+LogInController.idLogovanog);
	            if(in.readUTF().startsWith(ProtocolMessages.DODAJ_AZURIRANJE_OK.getMessage())){
	                System.out.println("Azuriranje uspjesno dodano");
	            }
	            else {
	                System.out.println("Azuriranje nije dodano");
	            }
	            //AzuriranjeDAO.dodajAzuriranje(azuriranje);
	            return true;
	        }
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
    	return false;
    }

    @FXML
    void dodajIgranjeAction(ActionEvent event) {

        if (dodajRepertoar()) {

            try {
                Parent adminController = FXMLLoader.load(getClass().getResource("/view/DodajIgranje.fxml"));

                Scene dodajRadnikaScene = new Scene(adminController);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(dodajRadnikaScene);
                window.show();
            } catch (IOException ex) {
                Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return;
        }
    }

    @FXML
    void nazadaNaPregledSvihRepertoara(ActionEvent event) {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/PregledSvihRepertoara.fxml"));

            Scene dodajRadnikaScene = new Scene(adminController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(dodajRadnikaScene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbGodina.getItems().add(Calendar.getInstance().get(Calendar.YEAR));
        for (Integer mjesec = (Calendar.getInstance().get(Calendar.MONTH) + 1); mjesec <= 12; mjesec++) {
            cmbMjesec.getItems().add(mjesec);
        }
        if (PregledSvihRepertoaraController.izmjenaRepertoara) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String datum = sdf.format(PregledSvihRepertoaraController.izabraniRepertoar.getMjesecIGodina());
            String godinaIzabranogRepertoara = datum.split("-")[0];
            String mjesecIzabranogRepertoara = datum.split("-")[1];

            cmbGodina.getSelectionModel().select(Integer.valueOf(godinaIzabranogRepertoara));
            cmbMjesec.getSelectionModel().select(Integer.valueOf(mjesecIzabranogRepertoara));
            cmbMjesec.getItems().clear();
            cmbMjesec.getItems().add(Integer.valueOf(mjesecIzabranogRepertoara));
            cmbMjesec.getSelectionModel().select(Integer.valueOf(mjesecIzabranogRepertoara));

        }

    }

    private void upozorenjeRepertoar() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom dodavanja repertoara !");
        alert.setHeaderText(null);
        alert.setContentText("Repertoar postoji u bazi");
        alert.showAndWait();
    }

    private void upozorenjeTermin() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom dodavanja repertoara !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite termin");
        alert.showAndWait();
    }

}
