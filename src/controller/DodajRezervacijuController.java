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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.dto.Rezervacija;
import model.dto.RezervisanoSjediste;
import util.ProtocolMessages;
import static controller.PregledKarataController.terminPredstave;
import static controller.PregledKarataController.scenaZaPrikaz;

public class DodajRezervacijuController implements Initializable {

    @FXML // fx:id="textFiled"
    private TextField textFiled; // Value injected by FXMLLoader

    @FXML // fx:id="buttonDodaj"
    private Button buttonDodaj; // Value injected by FXMLLoader

    @FXML // fx:id="buttonOdustani"
    private Button buttonOdustani; // Value injected by FXMLLoader

    public static Date termin;

    public static Integer idScene;

    public static ArrayList<Button> rezervisanaSjedista;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonDodaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_plus.png"))));
        buttonOdustani.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
        buttonOdustani.setOnAction(e -> ((Stage) buttonOdustani.getScene().getWindow()).close());
        buttonDodaj.setOnAction(e -> dodajButton());
    }

    private void dodajButton() {
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
            if (textFiled.getText().length() >= 20) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Unos predugacak", ButtonType.OK);
                alert.setTitle("Upozorenje");
                alert.setHeaderText("Upozorenje");
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
                alert.showAndWait();
                return;
            }
            String ime = textFiled.getText();
            PregledKarataController.naKogaGlasiRezervacija = textFiled.getText();
            PregledKarataController.rezervacije = new Rezervacija(0, ime, termin, idScene);//RezervacijaDAO.addRezervacija(new Rezervacija(0, ime, termin, idScene));
            final String imeRezervacije = PregledKarataController.naKogaGlasiRezervacija;
            Rezervacija unosRezervacija = null;
//      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
//      rezervacije(Date termin, Integer idScene)
            List<Rezervacija> rezervacijaList = new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(terminPredstave);
            
            out.writeUTF(ProtocolMessages.ADD_REZERVACIJA.getMessage()+ PregledKarataController.rezervacije.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage() +PregledKarataController.rezervacije.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+ strDate + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + PregledKarataController.rezervacije.getIdScene());
            if(in.readUTF().startsWith(ProtocolMessages.ADD_REZERVACIJA_OK.getMessage())) {
            	System.out.println("Rezervacija uspjesno dodata");
            }
            out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+terminPredstave+ ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+ scenaZaPrikaz.getIdScene());
            String response = null;
            try {
                response = in.readUTF();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            if (response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())) {
                String[] rezervacijeLines = response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < rezervacijeLines.length; i++) {
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                    String[] rezervacijaString = rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if (i == 0) {
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }
                        Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                        rezervacijaList.add(rezervacija);
                    } else {
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }
                        Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                        rezervacijaList.add(rezervacija);
                    }
                }
            }
            if (!rezervacijaList.stream().filter(e -> e.getIme().equals(imeRezervacije)).findAny().isPresent()) {
                unosRezervacija = new Rezervacija(0, imeRezervacije, terminPredstave, scenaZaPrikaz.getIdScene());
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String strDate1 = dateFormat.format(terminPredstave);
                out.writeUTF(ProtocolMessages.ADD_REZERVACIJA.getMessage() + 0 + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + imeRezervacije + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() +
                        strDate1 + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + scenaZaPrikaz.getIdScene());
                if (in.readUTF().startsWith(ProtocolMessages.ADD_REZERVACIJA_OK.getMessage())) {
                    System.out.println("Rezervacija uspjesno dodata");
                } else {
                    System.out.println("Rezervacija nije dodata");
                }
            } else {
                unosRezervacija = rezervacijaList.stream().filter(e -> e.getIme().equals(imeRezervacije)).findAny().get();
            }
            final Rezervacija temp = unosRezervacija;
            rezervisanaSjedista.forEach(e -> {
                DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                String strDate1 = dateFormat.format(temp.getTermin());
                String reqq = "";
//          new RezervisanoSjediste(scenaZaPrikaz.getIdScene(), Integer.parseInt(e.getId()), temp.getId(), temp.getTermin());
                try {
                    out.writeUTF(ProtocolMessages.ADD_REZERVISANO_SJEDISTE.getMessage() + scenaZaPrikaz.getIdScene() + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + Integer.parseInt(e.getId()) +
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + temp.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + strDate1);
                    reqq=in.readUTF();
                } catch (IOException eg) {
                    eg.printStackTrace();
                }
                if (reqq.startsWith(ProtocolMessages.ADD_REZERVISANO_SJEDISTE_OK.getMessage())) {
                    System.out.println("Rezervisano sjediste uspjesno dodato");
                } else {
                    System.out.println("Rezervisano sjediste nije dodato");
                }
                e.setDisable(true);
            });
            rezervisanaSjedista.removeAll(rezervisanaSjedista);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Rezervisano na ime " + imeRezervacije, ButtonType.OK);
            alert.setTitle("Informacija");
            alert.setHeaderText("Informacija");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/drama.png")));
            alert.showAndWait();

            ((Stage) buttonDodaj.getScene().getWindow()).close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
