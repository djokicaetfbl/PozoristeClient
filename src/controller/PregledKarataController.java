package controller;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.*;
import util.ProtocolMessages;

public class PregledKarataController implements Initializable {

    @FXML // fx:id="gridPane"
    private GridPane gridPane; // Value injected by FXMLLoader

    @FXML // fx:id="buttonProdaja"
    private Button buttonProdaja; // Value injected by FXMLLoader

    @FXML // fx:id="comboRezervacije"
    private ComboBox<String> comboRezervacije; // Value injected by FXMLLoader

    @FXML // fx:id="buttonNazad"
    private Button buttonNazad; // Value injected by FXMLLoader

    @FXML // fx:id="buttonRezervisi"
    private Button buttonRezervisi; // Value injected by FXMLLoader

    @FXML // fx:id="buttonObrisiRezervaciju"
    private Button buttonObrisiRezervaciju; // Value injected by FXMLLoader

    @FXML // fx:id="buttonStorniraj"
    private Button buttonStorniraj; // Value injected by FXMLLoader

    @FXML // fx:id="comboBoxKarte"
    private ComboBox<Karta> comboBoxKarte; // Value injected by FXMLLoader

    private final Integer RED = 10;

    private final Integer KOLONA = 10;

    public static Scena scenaZaPrikaz;

    public static Date terminPredstave;

    public static Rezervacija rezervacije;

    public static String naKogaGlasiRezervacija;

    private ArrayList<Button> rezervisanaSjedista = new ArrayList<>();//sjedista za jednu rezervaciju

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try{
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
        postavi();
        buttonProdaja.setOnAction(e -> prodaj());
        buttonNazad.setOnAction(e -> buttonNazadSetAction());
        buttonObrisiRezervaciju.setOnAction(e -> obrisiRezervacijuButton());
        buttonRezervisi.setOnAction(e -> buttonRezervisi());
        buttonStorniraj.setOnAction(e -> buttonStornirajProdaju());

        comboBoxKarte.getItems().removeAll(comboBoxKarte.getItems());
        List<Karta> kartaList = new ArrayList<>();
        out.writeUTF(ProtocolMessages.KARTE.getMessage());
        String response=in.readUTF();
        if(response.startsWith(ProtocolMessages.KARTE_RESPONSE.getMessage())){
            String[] karteLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<karteLines.length; i++){
                String[] kartaString=karteLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0){
//                  Karta(Integer id,Integer brojReda,Integer brojSjedista,Date termin,Integer idScene)
                    Date termin = null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[4]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }


                    Karta karta=new Karta(Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]), Integer.parseInt(kartaString[3]),termin, Integer.parseInt(kartaString[5]));
                    kartaList.add(karta);
                }
                else {
                    Date termin = null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[3]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }


                    Karta karta=new Karta(Integer.parseInt(kartaString[0]), Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]),termin, Integer.parseInt(kartaString[4]));
                    kartaList.add(karta);
                }
            }
        }
        else {
            System.out.println("Lista karata nije dobijena sa servera!");
        }
        comboBoxKarte.getItems().addAll(kartaList.stream().filter(e -> e.getIdScene() == scenaZaPrikaz.getIdScene() && e.getTermin().equals(terminPredstave)).collect(Collectors.toList()));
        List<Sjediste> sjedisteList=new ArrayList<>();
        out.writeUTF(ProtocolMessages.SJEDISTA.getMessage()+scenaZaPrikaz.getIdScene());
        String resp=in.readUTF();
        if(resp.startsWith(ProtocolMessages.SJEDISTA_RESPONSE.getMessage())){
            String[] sjedistaLines=resp.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<sjedistaLines.length; i++){
//                sjedista+=resultSet.getInt("brojSjedista")+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()
//                        +resultSet.getString("idScene")+ProtocolMessages.LINE_SEPARATOR.getMessage();
                String[] sjedisteString=sjedistaLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0) {
                    Sjediste sjediste = new Sjediste(Integer.parseInt(sjedisteString[1]));
                    sjediste.setIdentifikatorSale(Integer.parseInt(sjedisteString[2]));
                    sjedisteList.add(sjediste);
                }
                else {
                    Sjediste sjediste = new Sjediste(Integer.parseInt(sjedisteString[0]));
                    sjediste.setIdentifikatorSale(Integer.parseInt(sjedisteString[1]));
                    sjedisteList.add(sjediste);
                }
            }
        }

        if (sjedisteList.isEmpty()) {
            for (int i = 0; i < RED; i++) {
                for (int j = 0; j < KOLONA; j++) {
                    out.writeUTF(ProtocolMessages.DODAVANJE_SJEDISTA.getMessage()+scenaZaPrikaz.getIdScene()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+(i * KOLONA + j));
                    if(in.readUTF().startsWith(ProtocolMessages.DODAVANJE_SJEDISTA_OK.getMessage())){
                        System.out.println("Sjediste uspjesno dodano");
                    }
                    else {
                        System.out.println("Sjediste nije dodano");
                    }
                    //SjedisteDAO.dodavanjeSjedista(scenaZaPrikaz.getIdScene(), (i * KOLONA + j));
                }
            }
        }
        comboRezervacije.getItems().removeAll(comboRezervacije.getItems());
            List<Rezervacija> rezervacijaList=new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(terminPredstave);
            out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
            response=null;
            try {
                response=in.readUTF();
            }
            catch (IOException ee){
                ee.printStackTrace();
            }
            if(response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())){
                String[] rezervacijeLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for(int i=0; i<rezervacijeLines.length; i++){
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                    String[] rezervacijaString=rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if(rezervacijaString.length>2) {
                    if(i==0) {
                        Date termin=null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                            termin = new Date(date1.getTime());
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }
                        Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                        rezervacijaList.add(rezervacija);
                    }
                    else {
                        Date termin=null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                            termin = new Date(date1.getTime());
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }
                        Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                        rezervacijaList.add(rezervacija);
                    }
                }
                }
            }
        comboRezervacije.getItems().addAll(rezervacijaList.stream().map(i -> i.getIme()).collect(Collectors.toList()));
        comboRezervacije.getItems().remove("true");

    }catch(IOException ee){
        ee.printStackTrace();
        }
    }

    private void buttonNazadSetAction() {
        try {
            Parent pregledRepertoaraController = FXMLLoader.load(getClass().getResource("/view/PregledRepertoara.fxml"));
            Scene scene = new Scene(pregledRepertoaraController);
            Stage window = (Stage) buttonNazad.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(PregledKarataController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void postavi(){
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

            gridPane.setPadding(new Insets(5));
            gridPane.setHgap(13);
            gridPane.setVgap(13);
            gridPane.getChildren().removeAll(gridPane.getChildren());
            Button buttonMatrix[][] = new Button[RED][KOLONA];
            List<RezervisanoSjediste> rezervisanoSjedisteList = new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(terminPredstave);
            out.writeUTF(ProtocolMessages.SJEDISTA_RS.getMessage() + strDate + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + scenaZaPrikaz.getIdScene());
            String response = in.readUTF();
            if (response.startsWith(ProtocolMessages.SJEDISTA_RS_RESPONSE.getMessage())) {
                String[] sjedistaRSLines = response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < sjedistaRSLines.length; i++) {
                    String[] sjedistaRSString = sjedistaRSLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if (i == 0) {
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[4]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                        RezervisanoSjediste rezervisanoSjediste = new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), Integer.parseInt(sjedistaRSString[3]), termin);
                        rezervisanoSjedisteList.add(rezervisanoSjediste);
                    } else {
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[3]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                        RezervisanoSjediste rezervisanoSjediste = new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[0]), Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), termin);
                        rezervisanoSjedisteList.add(rezervisanoSjediste);
                    }
                }
            }
            List<RezervisanoSjediste> listRezervisanih = rezervisanoSjedisteList;
            List<Karta> kartaList = new ArrayList<>();
            out.writeUTF(ProtocolMessages.KARTE.getMessage());
            response = in.readUTF();
            if (response.startsWith(ProtocolMessages.KARTE_RESPONSE.getMessage())) {
                String[] karteLines = response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < karteLines.length; i++) {
                    String[] kartaString = karteLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if (i == 0) {
//                  Karta(Integer id,Integer brojReda,Integer brojSjedista,Date termin,Integer idScene)
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[4]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }


                        Karta karta = new Karta(Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]), Integer.parseInt(kartaString[3]), termin, Integer.parseInt(kartaString[5]));
                        kartaList.add(karta);
                    } else {
                        Date termin = null;
                        try {
                            java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[3]);
                            termin = new Date(date1.getTime());
                        } catch (java.text.ParseException pe) {
                            pe.printStackTrace();
                        }


                        Karta karta = new Karta(Integer.parseInt(kartaString[0]), Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]), termin, Integer.parseInt(kartaString[4]));
                        kartaList.add(karta);
                    }
                }
            } else {
                System.out.println("Lista karata nije dobijena sa servera!");
            }
            List<Karta> karteProdate = kartaList;
            for (int i = 0; i < RED; i++) {
                for (int j = 0; j < KOLONA; j++) {
                    buttonMatrix[i][j] = new Button();
                    buttonMatrix[i][j].setDisable(false);
                    final Integer brojSjedista = i * KOLONA + j;
                    buttonMatrix[i][j].setId(brojSjedista.toString());

                    if (karteProdate.stream().filter(k -> k.getBrojSjedista() == brojSjedista && k.getTermin().equals(terminPredstave)).findAny().isPresent()
                            && !listRezervisanih.stream().filter(e -> e.getBrojSjedista().equals(brojSjedista)).findAny().isPresent()) {
                        buttonMatrix[i][j].setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/red.png"))));
                        buttonMatrix[i][j].setDisable(true);
                    }

                    if (listRezervisanih.stream().filter(r -> r.getBrojSjedista() == brojSjedista && r.getTermin().equals(terminPredstave) && r.getIdScene() == scenaZaPrikaz.getIdScene())
                            .findAny().isPresent()) {
                        buttonMatrix[i][j].setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/orange.png"))));
                        buttonMatrix[i][j].setDisable(true);
                    }

                    if (!listRezervisanih.stream().filter(e -> e.getBrojSjedista().equals(brojSjedista)).findAny().isPresent()
                            && !karteProdate.stream().filter(k -> k.getBrojSjedista() == brojSjedista && k.getTermin().equals(terminPredstave)).findAny().isPresent()) {
                        buttonMatrix[i][j].setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Green.png"))));
                    } else if (!karteProdate.stream().filter(k -> k.getBrojSjedista() == brojSjedista).findFirst().isPresent()
                            && listRezervisanih.stream().filter(e -> e.getBrojSjedista().equals(brojSjedista)).findAny().isPresent()) {
                        buttonMatrix[i][j].setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/orange.png"))));
                    }
                    buttonMatrix[i][j].setId(new Integer(i * KOLONA + j).toString());
                    
                    buttonMatrix[i][j].setOnMouseClicked(e -> {
                        List<RezervisanoSjediste> rezervisanoSjedisteList1 = new ArrayList<>();
                        final DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                        String strDate1 = dateFormat1.format(terminPredstave);
                        String response1 = null;
                        try {
                            out.writeUTF(ProtocolMessages.SJEDISTA_RS.getMessage() + strDate1 + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + scenaZaPrikaz.getIdScene());
                            response1 = in.readUTF();
                        } catch (IOException ee) {
                            ee.printStackTrace();
                        }
                        if (response1.startsWith(ProtocolMessages.SJEDISTA_RS_RESPONSE.getMessage())) {
                            String[] sjedistaRSLines = response1.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                            for (int k = 0; k < sjedistaRSLines.length; k++) {
                                String[] sjedistaRSString = sjedistaRSLines[k].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                                if (k == 0) {
                                    Date termin = null;
                                    try {
                                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[4]);
                                        termin = new Date(date1.getTime());
                                    } catch (java.text.ParseException pe) {
                                        pe.printStackTrace();
                                    }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                                    RezervisanoSjediste rezervisanoSjediste = new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), Integer.parseInt(sjedistaRSString[3]), termin);
                                    rezervisanoSjedisteList.add(rezervisanoSjediste);
                                } else {
                                    Date termin = null;
                                    try {
                                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[3]);
                                        termin = new Date(date1.getTime());
                                    } catch (java.text.ParseException pe) {
                                        pe.printStackTrace();
                                    }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                                    RezervisanoSjediste rezervisanoSjediste1 = new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[0]), Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), termin);
                                    rezervisanoSjedisteList1.add(rezervisanoSjediste1);
                                }
                            }
                        }
                        if (!rezervisanoSjedisteList1.stream().
                                filter(r -> r.getBrojSjedista() == brojSjedista && r.getTermin().equals(terminPredstave) && r.getIdScene() == scenaZaPrikaz.getIdScene())
                                .findAny().isPresent() && !rezervisanaSjedista.contains(((Button) e.getSource()))) {
                            rezervisanaSjedista.add((Button) e.getSource());
                            ((Button) e.getSource()).setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/orange.png"))));
                            return;
                        } else if (rezervisanaSjedista.contains(((Button) e.getSource()))) {
                            ((Button) e.getSource()).setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/Green.png"))));
                            rezervisanaSjedista.remove(((Button) e.getSource()));
                            return;
                        }
                    });
                    if (gridPane.getChildren().size() > brojSjedista) {
                        gridPane.getChildren().remove(gridPane.getChildren().get(brojSjedista));
                    }
                    gridPane.add(buttonMatrix[i][j], j, i);
                }
            }
            comboRezervacije.getItems().removeAll(comboRezervacije.getItems());
            List<Rezervacija> rezervacijaList = new ArrayList<>();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            strDate = dateFormat.format(terminPredstave);
            out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage() + strDate + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + scenaZaPrikaz.getIdScene());
            response = null;
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
                    if(rezervacijaString.length>2) {
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
            }
            comboRezervacije.getItems().addAll(rezervacijaList.stream().map(i -> i.getIme()).collect(Collectors.toList()));

            comboBoxKarte.getItems().removeAll(comboBoxKarte.getItems());

            comboBoxKarte.getItems().addAll(kartaList.stream().filter(e -> e.getIdScene() == scenaZaPrikaz.getIdScene() && e.getTermin().equals(terminPredstave)).collect(Collectors.toList()));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void buttonRezervisi() {
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
        if (!rezervisanaSjedista.isEmpty()) {
            ((Stage) buttonRezervisi.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(e -> e.setDisable(true));
            DodajRezervacijuController.rezervisanaSjedista = rezervisanaSjedista;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DodajRezervaciju.fxml"));
            DodajRezervacijuController dodajRezervacijuController = null;
            loader.setController(dodajRezervacijuController);
            DodajRezervacijuController.termin = terminPredstave;
            DodajRezervacijuController.idScene = scenaZaPrikaz.getIdScene();
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(PregledKarataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            stage.setTitle("Dodaj rezervaciju");
            stage.getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/drama.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnCloseRequest(e -> {
                comboRezervacije.getItems().removeAll(comboRezervacije.getItems());
                List<Rezervacija> rezervacijaList=new ArrayList<>();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = dateFormat.format(terminPredstave);
                String response=null;
                try {
                    out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
                    response=in.readUTF();
                }
                catch (IOException ee){
                    ee.printStackTrace();
                }
                if(response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())){
                    String[] rezervacijeLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                    for(int i=0; i<rezervacijeLines.length; i++){
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                        String[] rezervacijaString=rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                        if(i==0) {
                            Date termin=null;
                            try {
                                java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                                termin = new Date(date1.getTime());
                            }catch (java.text.ParseException pe){
                                pe.printStackTrace();
                            }
                            Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                            rezervacijaList.add(rezervacija);
                        }
                        else {
                            Date termin=null;
                            try {
                                java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                                termin = new Date(date1.getTime());
                            }catch (java.text.ParseException pe){
                                pe.printStackTrace();
                            }
                            Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                            rezervacijaList.add(rezervacija);
                        }
                    }
                }
                comboRezervacije.getItems().addAll(rezervacijaList.stream().map(i -> i.getIme()).collect(Collectors.toList()));
                ((Stage) buttonRezervisi.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(w -> w.setDisable(false));
                rezervisanaSjedista.removeAll(rezervisanaSjedista);
                    postavi();
            });

            stage.setOnHiding(e -> {
                comboRezervacije.getItems().removeAll(comboRezervacije.getItems());
                List<Rezervacija> rezervacijaList=new ArrayList<>();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = dateFormat.format(terminPredstave);
                String response=null;
                try {
                    out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
                    response=in.readUTF();
                }
                catch (IOException ee){
                    ee.printStackTrace();
                }
                if(response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())){
                    String[] rezervacijeLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                    for(int i=0; i<rezervacijeLines.length; i++){
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                        String[] rezervacijaString=rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                        if(i==0) {
                            Date termin=null;
                            try {
                                java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                                termin = new Date(date1.getTime());
                            }catch (java.text.ParseException pe){
                                pe.printStackTrace();
                            }
                            Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                            rezervacijaList.add(rezervacija);
                        }
                        else {
                            Date termin=null;
                            try {
                                java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                                termin = new Date(date1.getTime());
                            }catch (java.text.ParseException pe){
                                pe.printStackTrace();
                            }
                            Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                            rezervacijaList.add(rezervacija);
                        }
                    }
                }

                comboRezervacije.getItems().addAll(rezervacijaList.stream().map(i -> i.getIme()).collect(Collectors.toList()));
                ((Stage) buttonRezervisi.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(w -> w.setDisable(false));
                rezervisanaSjedista.removeAll(rezervisanaSjedista);
                    postavi();
            });
            stage.show();

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Prvo izaberite sjedista koja zelite rezervisati!", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
            return;
        }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void obrisiRezervacijuButton(){
        try{


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
        List<Rezervacija> rezervacijaList=new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(terminPredstave);
        out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
        String response=null;
        try {
            response=in.readUTF();
        }
        catch (IOException ee){
            ee.printStackTrace();
        }
        if(response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())){
            String[] rezervacijeLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<rezervacijeLines.length; i++){
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                String[] rezervacijaString=rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0) {
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
                    Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                    rezervacijaList.add(rezervacija);
                }
                else {
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
                    Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                    rezervacijaList.add(rezervacija);
                }
            }
        }
        if (!comboRezervacije.getSelectionModel().isEmpty() && rezervacijaList.stream().filter(e -> e.getIme().equals(comboRezervacije.getSelectionModel().getSelectedItem())).findFirst().isPresent()) {
            final String imeZaBrisati = comboRezervacije.getSelectionModel().getSelectedItem();
            final Rezervacija rez = rezervacijaList.stream().filter(e -> e.getIme().equals(imeZaBrisati)).findFirst().get();
//            Rezervacija(Integer id, String ime, Date termin, Integer idScene)
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            String strDate1 = dateFormat1.format(rez.getTermin());
            out.writeUTF(ProtocolMessages.OBRISI_REZERVACIJU.getMessage()+rez.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+rez.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                    strDate1+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+rez.getIdScene());
            if(in.readUTF().startsWith(ProtocolMessages.OBRISI_REZERVACIJU_OK.getMessage())){
                System.out.println("Rezervacija uspjesno obrisana");
            }
            else {
                System.out.println("Rezervacija nije obrisana");
            }
            //RezervacijaDAO.obrisiRezervaciju(rez);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Rezervacija obrisana", ButtonType.OK);
            alert.setTitle("Informacija");
            alert.setHeaderText("Informacija");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/drama.png")));
            alert.showAndWait();
            comboRezervacije.getItems().removeAll(comboRezervacije.getItems());
            comboRezervacije.getItems().addAll(rezervacijaList.stream().map(i -> i.getIme()).collect(Collectors.toList()));
            postavi();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Prvo izaberite rezerevaciju za brisanje", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
        }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void prodaj() {
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
            if (!comboRezervacije.getSelectionModel().isEmpty() && rezervisanaSjedista.isEmpty()) {
                prodajRezervaciju();
                return;
            }

            if (!comboRezervacije.getSelectionModel().isEmpty() && !rezervisanaSjedista.isEmpty()) {
                final String rez = comboRezervacije.getSelectionModel().getSelectedItem();
                ButtonType obicnaProdaja = new ButtonType("Prodaja novih" + System.lineSeparator() + "karata");
                ButtonType prodajaRezervacije = new ButtonType("Prodaja selektovane" + System.lineSeparator() + "rezervacije");
                ButtonType odustani = new ButtonType("Odustani" + System.lineSeparator());
                Alert alert = new Alert(Alert.AlertType.WARNING, "Prodati", odustani, prodajaRezervacije, obicnaProdaja);
                alert.setTitle("Upozorenje");
                alert.setHeaderText("Selektovana je i rezervacija " + rez + " ,pa molimo izaberite tip prodaje");
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == odustani) {
                    rezervisanaSjedista.removeAll(rezervisanaSjedista);
                        postavi();
                } else if (result.get() == prodajaRezervacije) {
                    prodajRezervaciju();
                } else if (result.get() == obicnaProdaja) {
                    rezervisanaSjedista.forEach(e -> {
                        Karta k = new Karta(0, (Integer.valueOf(e.getId())) / 10, Integer.valueOf(e.getId()), terminPredstave, scenaZaPrikaz.getIdScene());
                        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                        String strDate1 = dateFormat1.format(k.getTermin());
                        boolean ress = false;
                        try {
                            out.writeUTF(ProtocolMessages.DODAJ_KARTU.getMessage() + k.getId() + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getBrojReda() + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getBrojSjedista() +
                                    ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + strDate1 + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getIdScene());
                            ress = in.readUTF().startsWith(ProtocolMessages.DODAJ_KARTU_OK.getMessage());
                        } catch (IOException eee) {
                            eee.printStackTrace();
                        }
                        if (ress) {
                            System.out.println("Karta uspjesno dodata");
                        } else {
                            System.out.println("Karta nije dodata");
                        }
                        //KartaDAO.dodajKartu(k);
                    });
                    rezervisanaSjedista.removeAll(rezervisanaSjedista);
                    postavi();
                }

            } else if (comboRezervacije.getSelectionModel().isEmpty() && !rezervisanaSjedista.isEmpty()) {
                rezervisanaSjedista.forEach(e -> {
                    Karta k = new Karta(0, (Integer.valueOf(e.getId())) / 10, Integer.valueOf(e.getId()), terminPredstave, scenaZaPrikaz.getIdScene());
                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                    String strDate1 = dateFormat1.format(k.getTermin());

                    boolean ress = false;
                    try {
                        out.writeUTF(ProtocolMessages.DODAJ_KARTU.getMessage() + k.getId() + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getBrojReda() + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getBrojSjedista() +
                            ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + strDate1 + ProtocolMessages.MESSAGE_SEPARATOR.getMessage() + k.getIdScene());
                        ress = in.readUTF().startsWith(ProtocolMessages.DODAJ_KARTU_OK.getMessage());
                    } catch (IOException eee) {
                        eee.printStackTrace();
                    }
                    if (ress) {
                        System.out.println("Karta uspjesno dodata");
                    } else {
                        System.out.println("Karta nije dodata");
                    }
                    //KartaDAO.dodajKartu(k);
                });
                rezervisanaSjedista.removeAll(rezervisanaSjedista);
                    postavi();

            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void buttonStornirajProdaju() {
        try{
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
        if (comboBoxKarte.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Prvo izaberite kartu za storniranje", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
            return;
        }
        if (!comboBoxKarte.getSelectionModel().isEmpty()) {
            out.writeUTF(ProtocolMessages.OBRISI_KARTU.getMessage()+comboBoxKarte.getSelectionModel().getSelectedItem().getId());
            if(in.readUTF().startsWith(ProtocolMessages.OBRISI_KARTU_OK.getMessage())){
                System.out.println("Karta uspjesno obrisana");
            }
            else {
                System.out.println("Karta nije obrisana");
            }
//          KartaDAO.obrisiKartu(comboBoxKarte.getSelectionModel().getSelectedItem().getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Karta uspjesno stornirana", ButtonType.OK);
            alert.setTitle("Informacija");
            alert.setHeaderText("Informacija");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/drama.png")));
            alert.showAndWait();
                postavi();
        }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void prodajRezervaciju() throws IOException {
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
        List<Rezervacija> rezervacijaList=new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(terminPredstave);
        out.writeUTF(ProtocolMessages.REZERVACIJE.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
        String response=null;
        try {
            response=in.readUTF();
        }
        catch (IOException ee){
            ee.printStackTrace();
        }
        if(response.startsWith(ProtocolMessages.REZERVACIJE_RESPONSE.getMessage())){
            String[] rezervacijeLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<rezervacijeLines.length; i++){
//                      Rezervacija(Integer id, String ime, Date termin, Integer idScene)
                String[] rezervacijaString=rezervacijeLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0) {
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[3]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
                    Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[1]), rezervacijaString[2], termin, Integer.parseInt(rezervacijaString[4]));
                    rezervacijaList.add(rezervacija);
                }
                else {
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(rezervacijaString[2]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
                    Rezervacija rezervacija = new Rezervacija(Integer.parseInt(rezervacijaString[0]), rezervacijaString[1], termin, Integer.parseInt(rezervacijaString[3]));
                    rezervacijaList.add(rezervacija);
                }
            }
        }
        final String zaProdaju = comboRezervacije.getSelectionModel().getSelectedItem();
        final Rezervacija rezervacija = rezervacijaList.stream().filter(e -> e.getIme().equals(zaProdaju)).findFirst().get();
        List<RezervisanoSjediste> rezervisanoSjedisteList=new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        strDate = dateFormat.format(terminPredstave);
        out.writeUTF(ProtocolMessages.SJEDISTA_RS.getMessage()+strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+scenaZaPrikaz.getIdScene());
        response=in.readUTF();
        if(response.startsWith(ProtocolMessages.SJEDISTA_RS_RESPONSE.getMessage())){
            String[] sjedistaRSLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<sjedistaRSLines.length; i++){
                String[] sjedistaRSString=sjedistaRSLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0){
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[4]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                    RezervisanoSjediste rezervisanoSjediste=new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), Integer.parseInt(sjedistaRSString[3]), termin);
                    rezervisanoSjedisteList.add(rezervisanoSjediste);
                }
                else {
                    Date termin=null;
                    try {
                        java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(sjedistaRSString[3]);
                        termin = new Date(date1.getTime());
                    }catch (java.text.ParseException pe){
                        pe.printStackTrace();
                    }
//                    RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
                    RezervisanoSjediste rezervisanoSjediste=new RezervisanoSjediste(Integer.parseInt(sjedistaRSString[0]), Integer.parseInt(sjedistaRSString[1]), Integer.parseInt(sjedistaRSString[2]), termin);
                    rezervisanoSjedisteList.add(rezervisanoSjediste);
                }
            }
        }
        List<RezervisanoSjediste> sjedista = rezervisanoSjedisteList.stream().filter(e -> e.getIdRezervacije() == rezervacija.getId()).collect(Collectors.toList());

        sjedista.forEach(e -> {
        	//Karta(Integer id,Integer brojReda,Integer brojSjedista,Date termin,Integer idScene)
            Karta k = new Karta(0, (int) e.getBrojSjedista() / 10, e.getBrojSjedista(), e.getTermin(), e.getIdScene());
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            String strDate1 = dateFormat1.format(k.getTermin());

            boolean ress=false;
            try{
                out.writeUTF(ProtocolMessages.DODAJ_KARTU.getMessage()+k.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+k.getBrojReda()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+k.getBrojSjedista()+
                    ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate1+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+k.getIdScene());
                ress=in.readUTF().startsWith(ProtocolMessages.DODAJ_KARTU_OK.getMessage());
            }
            catch (IOException eee){
                eee.printStackTrace();
            }
            if(ress){
                System.out.println("Karta uspjesno dodata");
            }
            else {
                System.out.println("Karta nije dodata");
            }
            //KartaDAO.dodajKartu(k);
        });
        sjedista.forEach(e -> {
//            RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin)
            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            String strDate1 = dateFormat1.format(e.getTermin());

            boolean ooo=false;
            try{
                out.writeUTF(ProtocolMessages.OBRISI_REZERVISANO_SJEDISTE.getMessage()+e.getIdScene()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+e.getBrojSjedista()+
                    ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+e.getIdRezervacije()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate1);
                ooo=in.readUTF().startsWith(ProtocolMessages.OBRISI_REZERVISANO_SJEDISTE_OK.getMessage());
            }catch (IOException ie){
                ie.printStackTrace();
            }
            if(ooo){
                System.out.println("Rezervisano sjediste uspjesno obrisano");
            }
            else {
                System.out.println("Rezervisano sjediste nije obrisano");
            }
//            RezervisanoSjedisteDAO.obrisiRezervisanoSjediste(e);
        });
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        String strDate1 = dateFormat1.format(rezervacija.getTermin());
        out.writeUTF(ProtocolMessages.OBRISI_REZERVACIJU.getMessage()+rezervacija.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+rezervacija.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                strDate1+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+rezervacija.getIdScene());
        if(in.readUTF().startsWith(ProtocolMessages.OBRISI_REZERVACIJU_OK.getMessage())){
            System.out.println("Rezervacija uspjesno obrisana");
        }
        else {
            System.out.println("Rezervacija nije obrisana");
        }
        //RezervacijaDAO.obrisiRezervaciju(rezervacija);
            postavi();
    }

}

