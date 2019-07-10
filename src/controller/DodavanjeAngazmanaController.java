package controller;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.Angazman;
import model.dto.Predstava;
import model.dto.Umjetnik;
import model.dto.VrstaAngazmana;
import util.ProtocolMessages;


public class DodavanjeAngazmanaController implements Initializable {

    @FXML
    private ComboBox<String> comboBoxVrstaAngazmana;

    @FXML
    private ComboBox<String> comboBoxUmjetnik;

    @FXML
    private DatePicker datePickerDatumOd;

    @FXML
    private DatePicker datePickerDatumDo;

    @FXML
    private TableView<Angazman> tableAngazmani;

    @FXML
    private Button buttonDodaj;

    @FXML
    private Button buttonOK;

    @FXML
    private Button buttonIzmijeni;

    @FXML
    private Label labelVrstaAngazmana;

    @FXML
    private Label labelDatumDo;

    @FXML
    private Label labelDatumOd;

    @FXML
    private Label labelUmjetnik;

    @FXML
    private Button buttonDodajVrstuAngazmana;

    @FXML
    private TableColumn<Angazman, String> imeColumn;
    @FXML
    private TableColumn<Angazman, String> prezimeColumn;
    @FXML
    private TableColumn<Angazman, String> vrstaAngazmanaColumn;
    @FXML
    private TableColumn<Angazman, Date> datumOdColumn;
    @FXML
    private TableColumn<Angazman, Date> datumDoColumn;

    public static ObservableList<Angazman> angazmani = FXCollections.observableArrayList();
    public static ObservableList<Umjetnik> umjetnici = FXCollections.observableArrayList();
    public static ObservableList<VrstaAngazmana> vrste = FXCollections.observableArrayList();
    private boolean izmjena = false;
    private static Predstava predstava;

    public static void setPredstava(Predstava p) {
        predstava = p;
    }

    private void ubaciKoloneUTabeluAngazmana(ObservableList angazmani) {
        imeColumn = new TableColumn("Ime");
        imeColumn.setCellValueFactory(new PropertyValueFactory<>("ime"));

        prezimeColumn = new TableColumn("Prezime");
        prezimeColumn.setCellValueFactory(new PropertyValueFactory<>("prezime"));

        vrstaAngazmanaColumn = new TableColumn("Vrsta angazmana");
        vrstaAngazmanaColumn.setCellValueFactory(new PropertyValueFactory<>("vrstaAngazmana"));

        datumOdColumn = new TableColumn("Datum od");
        datumOdColumn.setCellValueFactory(new PropertyValueFactory<>("datumOd"));

        datumDoColumn = new TableColumn("Datum do");
        datumDoColumn.setCellValueFactory(new PropertyValueFactory<>("datumDo"));

        tableAngazmani.setItems(angazmani);
        tableAngazmani.getColumns().addAll(imeColumn, prezimeColumn, vrstaAngazmanaColumn, datumOdColumn, datumDoColumn);
    }

    @FXML
    void dodajAction(ActionEvent event) {
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
            if (!izmjena) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePickerDatumOd.getValue().getYear(), datePickerDatumOd.getValue().getMonthValue() - 1, datePickerDatumOd.getValue().getDayOfMonth());

                if (((comboBoxUmjetnik.getSelectionModel().getSelectedIndex()) < 0) || ((comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()) < 0)) {
                    upozorenjeSelekcije();
                } else {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String strDate = dateFormat.format(new Date(calendar.getTimeInMillis()));
                    Integer umjetnikId=umjetnici.get(comboBoxUmjetnik.getSelectionModel().getSelectedIndex()).getIdRadnika();
                    out.writeUTF(ProtocolMessages.DODAJ_ANGAZMAN.getMessage()+predstava.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnikId+
                            ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+vrste.get(comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()).getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate);
//                    AngazmanDAO.dodajAngazman(predstava.getId(), umjetnici.get(comboBoxUmjetnik.getSelectionModel().getSelectedIndex()).getIdRadnika(),
//                            vrste.get(comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()).getId(), new Date(calendar.getTimeInMillis()));
                    if(in.readUTF().startsWith(ProtocolMessages.DODAJ_ANGAZMAN_OK.getMessage())){
                        System.out.println("Angazman uspjesno dodan");
                    }
                    else {
                        System.out.println("Angazman nije dodan");
                    }
                    osvjeziTabelu();

                }
            } else {
                Calendar calendar = Calendar.getInstance();
                Calendar calendar1 = Calendar.getInstance();
                calendar.set(datePickerDatumOd.getValue().getYear(), datePickerDatumOd.getValue().getMonthValue() - 1, datePickerDatumOd.getValue().getDayOfMonth());
                calendar1.set(datePickerDatumDo.getValue().getYear(), datePickerDatumDo.getValue().getMonthValue() - 1, datePickerDatumDo.getValue().getDayOfMonth());
                if (calendar1.after(calendar)) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String strDate = dateFormat.format(new Date(calendar.getTimeInMillis()));
                    out.writeUTF(ProtocolMessages.DODAJ_ANGAZMAN.getMessage()+predstava.getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnici.get(comboBoxUmjetnik.getSelectionModel().getSelectedIndex()).getIdRadnika()+
                            ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+vrste.get(comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()).getId()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+strDate);
//                    AngazmanDAO.dodajAngazman(predstava.getId(), umjetnici.get(comboBoxUmjetnik.getSelectionModel().getSelectedIndex()).getIdRadnika(),
//                            vrste.get(comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()).getId(), new Date(calendar.getTimeInMillis()));
                    if(in.readUTF().startsWith(ProtocolMessages.DODAJ_ANGAZMAN.getMessage())){
                        System.out.println("Angazman uspjesno dodan");
                    }
                    else {
                        System.out.println("Angazman nije dodan");
                    }
//                    AngazmanDAO.azurirajAngazman(predstava.getId(), umjetnici.get(comboBoxUmjetnik.getSelectionModel().getSelectedIndex()).getIdRadnika(),
//                            vrste.get(comboBoxVrstaAngazmana.getSelectionModel().getSelectedIndex()).getId(), new Date(calendar.getTimeInMillis()), new Date(calendar1.getTimeInMillis()));
                    osvjeziTabelu();
                    izmjena = false;
                    datePickerDatumDo.setVisible(false);
                    buttonIzmijeni.setVisible(false);
                    tableAngazmani.setDisable(false);
                    comboBoxUmjetnik.setDisable(false);
                    comboBoxVrstaAngazmana.setDisable(false);
                    datePickerDatumOd.setDisable(false);
                } else {
                    upozorenjeDate();
                }

            }
        }catch (IOException ee){
            ee.printStackTrace();
        }
    }

    @FXML
    void izmijeniAction(ActionEvent event) {
        ObservableList<Angazman> izabranaVrsta, angazmaniObservableList;
        angazmaniObservableList = tableAngazmani.getItems();
        izabranaVrsta = tableAngazmani.getSelectionModel().getSelectedItems();
        Angazman izabraniAngazman = (Angazman) izabranaVrsta.get(0);
        if (izabraniAngazman != null) {
            izmjena = true;
            tableAngazmani.setDisable(true);
            comboBoxUmjetnik.setValue(izabraniAngazman.getIme() + " " + izabraniAngazman.getPrezime());
            comboBoxUmjetnik.setDisable(true);
            comboBoxVrstaAngazmana.setValue(izabraniAngazman.getVrstaAngazmana());
            comboBoxVrstaAngazmana.setDisable(true);
            LocalDate l = izabraniAngazman.getDatumOd().toLocalDate();
            datePickerDatumOd.setValue(l);
            datePickerDatumOd.setDisable(true);

            datePickerDatumDo.setVisible(true);
            buttonIzmijeni.setVisible(false);

        } else {
            upozorenjeSelekcijeTabele();
        }

    }

    @FXML
    void dodajVrstuAngazmanaAction(ActionEvent event) {
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
            ((Stage) tableAngazmani.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(e -> e.setDisable(true));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DodavanjeVrsteAngazmana.fxml"));
            DodavanjeVrsteAngazmanaController dodajVrstuAngazmana = null;
            loader.setController(dodajVrstuAngazmana);
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(DodavanjeAngazmanaController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Dodaj vrstu angazmana");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setOnCloseRequest(e -> {
                ((Stage) tableAngazmani.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(k -> k.setDisable(false));
                vrste.clear();
                List<VrstaAngazmana> vrstaAngazmanaList = new ArrayList<>();

                String res="";
                try{
                    out.writeUTF(ProtocolMessages.VRSTE_ANGAZMANA.getMessage());
                    res=in.readUTF();
                }catch (IOException eee){
                    eee.printStackTrace();
                }
                if(res.startsWith(ProtocolMessages.VRSTE_ANGAZMANA_RESPONSE.getMessage())){
                    String[] vrsteAngazmanaLines=res.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                    for(int i=0; i<vrsteAngazmanaLines.length; i++){
                        String[] vrsteAngazmanaString=vrsteAngazmanaLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                        if(i==0){
//                            VrstaAngazmana(Integer id, String naziv)
                            VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[1]), vrsteAngazmanaString[2]);
                            vrstaAngazmanaList.add(vrstaAngazmana);
                        }
                        else {
                            VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[0]), vrsteAngazmanaString[1]);
                            vrstaAngazmanaList.add(vrstaAngazmana);
                        }
                    }
                }
                vrste.addAll(vrstaAngazmanaList);
                ObservableList<String> pomocni = FXCollections.observableArrayList();
                vrste.forEach((v) -> {
                    pomocni.add(v.getNaziv());
                });
                comboBoxVrstaAngazmana.getItems().removeAll(comboBoxVrstaAngazmana.getItems());
                comboBoxVrstaAngazmana.setItems(pomocni);
            });
            stage.setOnHiding(e -> {
                ((Stage) tableAngazmani.getScene().getWindow()).getScene().getRoot().getChildrenUnmodifiable().forEach(k -> k.setDisable(false));
                vrste.clear();
                List<VrstaAngazmana> vrstaAngazmanaList = new ArrayList<>();
                String res="";
                try{
                    out.writeUTF(ProtocolMessages.VRSTE_ANGAZMANA.getMessage());
                    res=in.readUTF();
                }catch (IOException eee){
                    eee.printStackTrace();
                }
                if(res.startsWith(ProtocolMessages.VRSTE_ANGAZMANA_RESPONSE.getMessage())){
                    String[] vrsteAngazmanaLines=res.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                    for(int i=0; i<vrsteAngazmanaLines.length; i++){
                        String[] vrsteAngazmanaString=vrsteAngazmanaLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                        if(i==0){
//                            VrstaAngazmana(Integer id, String naziv)
                            VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[1]), vrsteAngazmanaString[2]);
                            vrstaAngazmanaList.add(vrstaAngazmana);
                        }
                        else {
                            VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[0]), vrsteAngazmanaString[1]);
                            vrstaAngazmanaList.add(vrstaAngazmana);
                        }
                    }
                }
                vrste.addAll(vrstaAngazmanaList);
                ObservableList<String> pomocni = FXCollections.observableArrayList();
                vrste.forEach((v) -> {
                    pomocni.add(v.getNaziv());
                });
                comboBoxVrstaAngazmana.getItems().removeAll(comboBoxVrstaAngazmana.getItems());
                comboBoxVrstaAngazmana.setItems(pomocni);
            });
            stage.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
        }catch(IOException ee){
            ee.printStackTrace();
        }
    }

    @FXML
    void okAction(ActionEvent event) {
        try {
            Parent predstavaController = FXMLLoader.load(getClass().getResource("/view/PregledPredstava.fxml"));

            Scene predstavaScene = new Scene(predstavaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(predstavaScene);
            window.setResizable(false);
            window.setTitle("Vrsta angazmana");
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(DodavanjeAngazmanaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buttonDodaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
        buttonOK.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
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
            labelDatumDo.setVisible(false);
            datePickerDatumDo.setVisible(false);

            umjetnici.clear();
            List<Umjetnik> umjetnikList = new ArrayList<>();
            out.writeUTF(ProtocolMessages.UMJETNICI.getMessage());
            String res="";
            try{
                res=in.readUTF();
            }catch (IOException eee){
                eee.printStackTrace();
            }
            if(res.startsWith(ProtocolMessages.UMJETNICI_RESPONSE.getMessage())){
                String[] umjetniciLines=res.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for(int i=0; i<umjetniciLines.length; i++){
                    String[] umjetniciString=umjetniciLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if(i==0){
                        boolean status=umjetniciString[4]=="true"?true:false;
//                        Umjetnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak, String biografija)
                        Umjetnik umjetnik=new Umjetnik(umjetniciString[1], umjetniciString[2], umjetniciString[3], status, umjetniciString[5], umjetniciString[6]);
                        umjetnik.setIdRadnika(Integer.parseInt(umjetniciString[7]));
                        umjetnikList.add(umjetnik);
                    }
                    else {
                        boolean status=umjetniciString[3]=="true"?true:false;
//                        Umjetnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak, String biografija)
                        Umjetnik umjetnik=new Umjetnik(umjetniciString[0], umjetniciString[1], umjetniciString[2], status, umjetniciString[4], umjetniciString[5]);
                        umjetnik.setIdRadnika(Integer.parseInt(umjetniciString[6]));
                        umjetnikList.add(umjetnik);
                    }
                }
            }
            umjetnici.addAll(umjetnikList);
            ObservableList<String> pomocno = FXCollections.observableArrayList();
            umjetnici.stream().map((u) -> u.getIme() + " " + u.getPrezime()).forEachOrdered((pom) -> {
                pomocno.add(pom);
            });
            comboBoxUmjetnik.getItems().removeAll(comboBoxUmjetnik.getItems());
            comboBoxUmjetnik.setItems(pomocno);
         // odredi adresu racunara sa kojim se povezujemo
            // (povezujemo se sa nasim racunarom)
            addr = InetAddress.getByName(Pozoriste.HOST);
            // otvori socket prema drugom racunaru
            sock = new Socket(addr, Pozoriste.PORT);
            // inicijalizuj ulazni stream
            in = new DataInputStream(
                    sock.getInputStream());
            // inicijalizuj izlazni stream
            out = new DataOutputStream(
                    sock.getOutputStream());
            vrste.clear();
            List<VrstaAngazmana> vrstaAngazmanaList = new ArrayList<>();
            out.writeUTF(ProtocolMessages.VRSTE_ANGAZMANA.getMessage());
            String respo="";
            try{
                respo=in.readUTF();
            }catch (IOException eee){
                eee.printStackTrace();
            }
            if(respo.startsWith(ProtocolMessages.VRSTE_ANGAZMANA_RESPONSE.getMessage())){
                String[] vrsteAngazmanaLines=respo.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for(int i=0; i<vrsteAngazmanaLines.length; i++){
                    String[] vrsteAngazmanaString=vrsteAngazmanaLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if(i==0){
//                        VrstaAngazmana(Integer id, String naziv)
                        VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[1]), vrsteAngazmanaString[2]);
                        vrstaAngazmanaList.add(vrstaAngazmana);
                    }
                    else {
                        VrstaAngazmana vrstaAngazmana=new VrstaAngazmana(Integer.parseInt(vrsteAngazmanaString[0]), vrsteAngazmanaString[1]);
                        vrstaAngazmanaList.add(vrstaAngazmana);
                    }
                }
            }
            vrste.addAll(vrstaAngazmanaList);
            ObservableList<String> pomocni = FXCollections.observableArrayList();
            vrste.forEach((v) -> {
                pomocni.add(v.getNaziv());
            });
            comboBoxVrstaAngazmana.getItems().removeAll(comboBoxVrstaAngazmana.getItems());
            comboBoxVrstaAngazmana.setItems(pomocni);

            angazmani.clear();
            List<Angazman> angazmanList=new ArrayList<>();
//          Predstava(String naziv, String opis, String tip)
            out.writeUTF(ProtocolMessages.ANGAZMANI.getMessage()+predstava.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getOpis()+
                    ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getTip());
            String response=in.readUTF();
            //Angazman(String ime,String prezime,String vrstaAngazmana,Date datumOd,Date datumDo)
            if(response.startsWith(ProtocolMessages.ANGAZMANI_RESPONSE.getMessage())){
                String[] angazmaniLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for(int i=0; i<angazmaniLines.length; i++){
                    String[] angazmaniString=angazmaniLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if(i==0){
                        String sDate1=angazmaniString[4];
                        String sDate2=angazmaniString[5];
                        Date terminOD=null;
                        Date terminDO=null;
                        try {
                        	java.util.Date terminOd=null;
                            if(!sDate1.equals("null")) {
                            	terminOd=new SimpleDateFormat("yyyy-MM-dd").parse(sDate1);
                            	terminOD=new Date(terminOd.getTime());
                            }
                            java.util.Date terminDo=null;
                            if(!sDate2.equals("null")) {
                            	terminDo=new SimpleDateFormat("yyyy-MM-dd").parse(sDate2);
                            	terminDO=new Date(terminDo.getTime());
                            }
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }

                        Angazman angazman=new Angazman(angazmaniString[1], angazmaniString[2], angazmaniString[3], terminOD, terminDO);
                       angazmanList.add(angazman);
                    }
                    else {
                    	String sDate1=angazmaniString[3];
                        String sDate2=angazmaniString[4];
                        Date terminOD=null;
                        Date terminDO=null;
                        try {
                        	java.util.Date terminOd=null;
                            if(!sDate1.equals("null")) {
                            	terminOd=new SimpleDateFormat("yyyy-MM-dd").parse(sDate1);
                            	terminOD=new Date(terminOd.getTime());
                            }
                            java.util.Date terminDo=null;
                            if(!sDate2.equals("null")) {
                            	terminDo=new SimpleDateFormat("yyyy-MM-dd").parse(sDate2);
                            	terminDO=new Date(terminDo.getTime());
                            }
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }

                        Angazman angazman=new Angazman(angazmaniString[0], angazmaniString[1], angazmaniString[2], terminOD, terminDO);
                       angazmanList.add(angazman);
                    }
                }
            }
            angazmani.addAll(angazmanList);
            ubaciKoloneUTabeluAngazmana(angazmani);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void osvjeziTabelu() {
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

            angazmani.clear();
            List<Angazman> angazmanList=new ArrayList<>();
//          Predstava(String naziv, String opis, String tip)
            out.writeUTF(ProtocolMessages.ANGAZMANI.getMessage()+predstava.getNaziv()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getOpis()+
                    ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+predstava.getTip());
            String response=in.readUTF();
            //Angazman(String ime,String prezime,String vrstaAngazmana,Date datumOd,Date datumDo)
            if(response.startsWith(ProtocolMessages.ANGAZMANI_RESPONSE.getMessage())){
                String[] angazmaniLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for(int i=0; i<angazmaniLines.length; i++){
                    String[] angazmaniString=angazmaniLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    if(i==0){
                        String sDate1=angazmaniString[4];
                        String sDate2=angazmaniString[5];
                        Date terminOD=null;
                        Date terminDO=null;
                        try {
                        	java.util.Date terminOd=null;
                        	if(!sDate1.equals("null")) {
                        		terminOd=new SimpleDateFormat("yyyy-MM-dd").parse(sDate1);
                        		terminOD=new Date(terminOd.getTime());
                        	}
                        	java.util.Date terminDo=null;
                        	if(!sDate2.equals("null")) {
                        		terminDo=new SimpleDateFormat("yyyy-MM-dd").parse(sDate2);
                            	terminDO=new Date(terminDo.getTime());
                        	}
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }

                        Angazman angazman=new Angazman(angazmaniString[1], angazmaniString[2], angazmaniString[3], terminOD, terminDO);
                        angazmanList.add(angazman);
                    }
                    else {
                    	String sDate1=angazmaniString[3];
                        String sDate2=angazmaniString[4];
                        Date terminOD=null;
                        Date terminDO=null;
                        try {
                        	java.util.Date terminOd=null;
                            if(!sDate1.equals("null")) {
                            	terminOd=new SimpleDateFormat("yyyy-MM-dd").parse(sDate1);
                            	terminOD=new Date(terminOd.getTime());
                            }
                            java.util.Date terminDo=null;
                            if(!sDate2.equals("null")) {
                            	terminDo=new SimpleDateFormat("yyyy-MM-dd").parse(sDate2);
                            	terminDO=new Date(terminDo.getTime());
                            }
                        }catch (java.text.ParseException pe){
                            pe.printStackTrace();
                        }

                        Angazman angazman=new Angazman(angazmaniString[0], angazmaniString[1], angazmaniString[2], terminOD, terminDO);
                       angazmanList.add(angazman);
                    }
                }
            }
            angazmani.addAll(angazmanList);
            tableAngazmani.setItems(angazmani);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void upozorenjeSelekcije() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText("Izaberite umjetnika i vrstu angazmana!");
        alert.showAndWait();
    }
    private void upozorenjeDate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText("Izabrani datum nije tacan!");
        alert.showAndWait();
    }
    private void upozorenjeSelekcijeTabele() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText("Selektujte angazman!");
        alert.showAndWait();
    }

}
