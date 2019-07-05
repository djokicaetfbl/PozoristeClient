package controller;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.dto.AdministrativniRadnik;
import model.dto.Biletar;
import model.dto.Radnik;
import model.dto.Umjetnik;
import util.ProtocolMessages;

public class PregledRadnikaController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TableView radniciTableView;

    @FXML
    private TableColumn<Radnik, String> imeColumn;

    @FXML
    private TableColumn<Radnik, String> prezimeColumn;

    @FXML
    private TableColumn<Radnik, String> zanimanjeColumn;

    @FXML
    private TableColumn<Radnik, String> jmbColumn;

    @FXML
    private TableColumn<Radnik, Boolean> statusRadnikaColumn;

    @FXML
    private TableColumn<Radnik, String> kontaktColumn;

    @FXML
    private TableColumn<Radnik, String> korisnickoImeColumn;

    @FXML
    private Button bDodaj;

    @FXML
    private Button bIzmjeni;

    @FXML
    private Button bPretrazi;

    @FXML
    private TextField tfPretraga;

    @FXML
    private Button bNazad;

    @FXML
    private TextArea taBiografija;

    public static ObservableList<Radnik> radniciObservableList = FXCollections.observableArrayList();

    public static Radnik radnikIzPretrage;
    
    public static boolean dodajRadnika = true;
    public static Radnik izabraniRadnik,radnikIzPretraga;//, radnikIzPretrage;
        public static String tipRadnika = "";

    @FXML
    void dodajRadnikaAction(ActionEvent event) {
        try {
            dodajRadnika = true;
            Parent dodajRadnikaController = FXMLLoader.load(getClass().getResource("/view/DodajRadnika.fxml"));

            Scene dodajRadnikaScene = new Scene(dodajRadnikaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    
    @FXML
    void izmijeniRadnikaAction(ActionEvent event) {
        
            dodajRadnika = false;         
            ObservableList<Radnik> izabranaVrsta,radniciObservableList;
            radniciObservableList = radniciTableView.getItems();
            izabranaVrsta = radniciTableView.getSelectionModel().getSelectedItems();
            izabraniRadnik = (Radnik) izabranaVrsta.get(0);
            tipRadnika = ((Radnik) izabranaVrsta.get(0)).getTipRadnika();
            if(izabraniRadnik != null){
             try {
            Parent dodajRadnikaController = FXMLLoader.load(getClass().getResource("/view/DodajRadnika.fxml"));

            Scene dodajRadnikaScene = new Scene(dodajRadnikaController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
            } else {
                
            }
    }

    @FXML
    void pretraziRadnikaAction(ActionEvent event) {
        String jmbRegex = "\\d+";
        Pattern pattern = Pattern.compile(jmbRegex);
        if (pattern.matcher(tfPretraga.getText()).matches() && (tfPretraga.getText().length() == 13)) {
            radniciTableView.getColumns().clear();
            final String zaPorednje = tfPretraga.getText();
            Optional<Radnik> radnik = radniciObservableList.stream().filter(e -> e.getJmb().equals(zaPorednje)).findFirst();
            ObservableList<Radnik> radnikIzPretrageObservableList = null;
            if (radnik.isPresent()) {
                radnikIzPretrageObservableList = FXCollections.observableArrayList();
                radnikIzPretrageObservableList.add(radnik.get());
                tabelaRadnika(radnikIzPretrageObservableList);
            }
        } else {
            upozorenjePretraga();
            return;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bDodaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_plus.png"))));
        bIzmjeni.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_editproperty_48px.png"))));
        bPretrazi.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/search.png"))));
        bNazad.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
    	try {
    		radniciObservableList.clear();
	        taBiografija.setEditable(false);
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
	        out.writeUTF(ProtocolMessages.UBACI_U_TABELU_RADNIK_ADMIN.getMessage());
	        String response=in.readUTF();
	        if(response.startsWith(ProtocolMessages.UBACI_U_TABELU_RADNIK_ADMIN_RESPONSE.getMessage())) {
	        	//AdministrativniRadnik(rs.getString("Ime"), rs.getString("Prezime"), rs.getString("JMB"), rs.getBoolean("StatusRadnika"), rs.getString("Kontakt"), rs.getString("KorisnickoIme"), rs.getString("HashLozinke"), rs.getString("TipKorisnika"));
                //admin.setIdRadnika(rs.getInt("Id"));
	        	String[] adminiLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
	        	for(int i=0; i<adminiLines.length; i++) {
	        		String[] adminiString=adminiLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
	        		if(i==0) {
	        			boolean status=adminiString[4].equals("true")?true:false;
	        			AdministrativniRadnik ar=new AdministrativniRadnik(adminiString[1], adminiString[2], adminiString[3], status, adminiString[5], adminiString[6], adminiString[7], adminiString[8]);
	        			ar.setIdRadnika(Integer.parseInt(adminiString[9]));
	        			radniciObservableList.add(ar);
	        		}
	        		else {
	        			boolean status=adminiString[3].equals("true")?true:false;
	        			AdministrativniRadnik ar=new AdministrativniRadnik(adminiString[0], adminiString[1], adminiString[2], status, adminiString[4], adminiString[5], adminiString[6], adminiString[7]);
	        			ar.setIdRadnika(Integer.parseInt(adminiString[8]));
	        			radniciObservableList.add(ar);
	        		}
	        	}
	        }
	        out.writeUTF(ProtocolMessages.UBACI_U_TABELU_RADNIK_UMJETNIK.getMessage());
	        response=in.readUTF();
	        if(response.startsWith(ProtocolMessages.UBACI_U_TABELU_RADNIK_UMJETNIK_RESPONSE.getMessage())) {
	        	//umjetnik = new Umjetnik(rs.getString("Ime"), rs.getString("Prezime"), rs.getString("JMB"), rs.getBoolean("StatusRadnika"), rs.getString("Kontakt"), rs.getString("Biografija"));
                //umjetnik.setIdRadnika(rs.getInt("Id"));
	        	String[] umjetniciLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
	        	for(int i=0; i<umjetniciLines.length; i++) {
	        		String[] umjetniciString=umjetniciLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
	        		if(i==0) {
	        			boolean status=umjetniciString[4].equals("true")?true:false;
	        			Umjetnik u=new Umjetnik(umjetniciString[1], umjetniciString[2], umjetniciString[3], status, umjetniciString[5], umjetniciString[6]);
	        			u.setIdRadnika(Integer.parseInt(umjetniciString[7]));
	        			radniciObservableList.add(u);
	        		}
	        		else {
	        			boolean status=umjetniciString[3].equals("true")?true:false;
	        			Umjetnik u=new Umjetnik(umjetniciString[0], umjetniciString[1], umjetniciString[2], status, umjetniciString[4], umjetniciString[5]);
	        			u.setIdRadnika(Integer.parseInt(umjetniciString[6]));
	        			radniciObservableList.add(u);
	        		}
	        	}
	        }
	        out.writeUTF(ProtocolMessages.UBACI_U_TABELU_RADNIK_BILETAR.getMessage());
	        response=in.readUTF();
	        if(response.startsWith(ProtocolMessages.UBACI_U_TABELU_RADNIK_BILETAR_RESPONSE.getMessage())) {
	        	//biletar = new Biletar(rs.getString("Ime") ,rs.getString("Prezime")/, rs.getString("JMB")
                        //,rs.getBoolean("StatusRadnika"), rs.getString("Kontakt"), rs.getString("KorisnickoIme")
                        //,rs.getString("HashLozinke"),rs.getString("TipKorisnika"));
                //biletar.setIdRadnika(rs.getInt("Id"));
	        	String[] biletariLines=response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
	        	for(int i=0; i<biletariLines.length; i++) {
	        		String[] biletariString=biletariLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
	        		if(i==0) {
	        			boolean status=biletariString[4].equals("true")?true:false;
	        			Biletar b=new Biletar(biletariString[1], biletariString[2], biletariString[3], status, biletariString[5], biletariString[6], biletariString[7],biletariString[8]);
	        			b.setIdRadnika(Integer.parseInt(biletariString[9]));
	        			radniciObservableList.add(b);
	        		}
	        		else {
	        			boolean status=biletariString[3].equals("true")?true:false;
	        			Biletar b=new Biletar(biletariString[0], biletariString[1], biletariString[2], status, biletariString[4], biletariString[5], biletariString[6],biletariString[7]);
	        			b.setIdRadnika(Integer.parseInt(biletariString[8]));
	        			radniciObservableList.add(b);
	        		}
	        	}
	        }
	        //AdministratorDAO.ubaciUTabeluRadnik();
	        //BIletarDAO.ubaciUTabeluRadnik();
	        //UmjetnikDAO.ubaciUTabeluRadnik();
	        ubaciKoloneUTabeluRadnik(radniciObservableList);
	        radniciTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
	            if (newSelection != null) {
	                Radnik radnik = (Radnik) radniciTableView.getSelectionModel().getSelectedItem();
	                if (radnik instanceof Umjetnik) {
	                    taBiografija.setText(((Umjetnik) radniciTableView.getSelectionModel().getSelectedItem()).getBiografija());
	                } else {
	                    taBiografija.setText("");
	                }
	            }
	        });
    }catch(IOException e) {
    	e.printStackTrace();
    }

       

    }

    public void tabelaRadnika(ObservableList radnici) {
        ubaciKoloneUTabeluRadnik(radnici);
        radniciTableView.setItems(radnici);       
    }

    private void ubaciKoloneUTabeluRadnik(ObservableList radnici) {
        imeColumn = new TableColumn("Ime");
        imeColumn.setCellValueFactory(new PropertyValueFactory<>("ime"));

        prezimeColumn = new TableColumn("Prezime");
        prezimeColumn.setCellValueFactory(new PropertyValueFactory<>("prezime"));

        jmbColumn = new TableColumn("JMB");
        jmbColumn.setCellValueFactory(new PropertyValueFactory<>("jmb"));

        kontaktColumn = new TableColumn("Kontakt");
        kontaktColumn.setCellValueFactory(new PropertyValueFactory<>("kontakt"));

        korisnickoImeColumn = new TableColumn("Korisnicko Ime");
        korisnickoImeColumn.setCellValueFactory(new PropertyValueFactory<>("korisnickoIme"));
        
        zanimanjeColumn = new TableColumn("Zanimanje");
        zanimanjeColumn.setCellValueFactory(new PropertyValueFactory<>("tipRadnika"));
        
        statusRadnikaColumn = new TableColumn("Status radnika");
        statusRadnikaColumn.setCellValueFactory(new PropertyValueFactory<>("statusRadnika"));

        radniciTableView.setItems(radniciObservableList);
        radniciTableView.getColumns().addAll(jmbColumn,imeColumn, prezimeColumn,zanimanjeColumn,kontaktColumn, korisnickoImeColumn,statusRadnikaColumn);
    }

    @FXML
    void idiNazadNaAdminForm(ActionEvent event) {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/Admin.fxml"));

            Scene dodajRadnikaScene = new Scene(adminController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    private void upozorenjePretraga() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Provjerite polja za pretragu po JMB-u.");
        alert.showAndWait();
    }
    
        private void upozorenjeIzaberi() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom izbora zaposlenog !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite zaposlenog iz tabele !");
        alert.showAndWait();
    }

}
