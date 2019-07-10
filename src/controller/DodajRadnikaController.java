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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.AdministrativniRadnik;
import model.dto.Biletar;
import model.dto.Radnik;
import model.dto.Umjetnik;
import util.ProtocolMessages;


public class DodajRadnikaController implements Initializable {

    @FXML
    private TextField tfIme;

    @FXML
    private TextField tfPrezime;

    @FXML
    private TextField tfJmb;

    @FXML
    private TextField tfKontakt;

    @FXML
    private TextField tfKorisnickoIme;
    @FXML
    private TextField tfNovaLozinka;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private TextArea taBiografija;

    @FXML
    private ComboBox cmbTipRadnika;

    @FXML
    private Button bOdustani;

    @FXML
    private Button bPotvrdi;

    @FXML
    private Label lIme;

    @FXML
    private Label lPrezime;

    @FXML
    private Label lJmb;

    @FXML
    private Label lStatusRadnika;

    @FXML
    private Label lKontakt;

    @FXML
    private Label lKorisnickoIme;

    @FXML
    private Label lLozinka;

    @FXML
    private Label lBiografija;
    @FXML
    private Label lNovaLozinka;
    @FXML
    private ComboBox cmbStatusRadnika;

    public static String korisnickoImeIzabranogRadnika = "";

    private boolean dodajAdministrativnogRadnika() throws IOException {
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
        if (PregledRadnikaController.dodajRadnika) {
            if (cmbTipRadnika.getSelectionModel().getSelectedItem() == null) {
                upozorenjeComboBox();
                return false;
            }
        }
        if (!tfJmb.getText().isEmpty() && !tfIme.getText().isEmpty() && !tfPrezime.getText().isEmpty()
                && !tfKontakt.getText().isEmpty() && !tfKorisnickoIme.getText().isEmpty() && !tfPassword.getText().isEmpty()) {

            String jmbRegex = "\\d+";
            Pattern pattern = Pattern.compile(jmbRegex);

            if (!pattern.matcher(tfJmb.getText()).matches() && tfJmb.getText().length() != 13) {
                upozorenjeNeispravanJMB();
                return false;
            } else {
                if (PregledRadnikaController.dodajRadnika) {
                    out.writeUTF(ProtocolMessages.PROVJERI_MATICNI_BROJ_U_BAZI.getMessage()+tfJmb.getText());
                    String res=in.readUTF();
                    boolean provjeriMaticniBrojUBazi=res.split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                    if (provjeriMaticniBrojUBazi) {
                        upoorenjeMaticniBroj();
                        return false;
                    }
                }
            }
            if (tfIme.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }

            if (tfPrezime.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }

            if (tfKorisnickoIme.getText().length() > 20) {
                upozorenjePredugacakUnos();
                return false;
            }

            AdministrativniRadnik admin = new AdministrativniRadnik();
            if (!PregledRadnikaController.dodajRadnika) {
                admin = (AdministrativniRadnik) PregledRadnikaController.izabraniRadnik;
                admin.setIdRadnika(((AdministrativniRadnik) PregledRadnikaController.izabraniRadnik).getIdRadnika());
            }

            admin.setJmb(tfJmb.getText());
            admin.setIme(tfIme.getText());
            admin.setPrezime(tfPrezime.getText());
            String statusRadnika = "";
            if (PregledRadnikaController.dodajRadnika) {
                statusRadnika = "Aktivan";
            } else {
                statusRadnika = cmbStatusRadnika.getSelectionModel().getSelectedItem().toString();
            }
            if (statusRadnika.equals("Aktivan")) {
                admin.setStatusRadnika(true);
            } else {
                admin.setStatusRadnika(false);
            }
            admin.setKorisnickoIme(tfKorisnickoIme.getText());
            admin.setKontak(tfKontakt.getText());

            if (!PregledRadnikaController.dodajRadnika) {

                if (!korisnickoImeIzabranogRadnika.equals(tfKorisnickoIme.getText())) {
                    out.writeUTF(ProtocolMessages.POSTOJI_U_BAZI_KORISNICKO_IME.getMessage()+tfKorisnickoIme.getText());
                    boolean postojiUBaziKorisnickoIme=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                    if (!postojiUBaziKorisnickoIme) {
                        admin.setKorisnickoIme(tfKorisnickoIme.getText());
                    } else {
                        upozorenjeKorisnickoIme();
                        return false;
                    }
                }
            } else {
                out.writeUTF(ProtocolMessages.POSTOJI_U_BAZI_KORISNICKO_IME.getMessage()+tfKorisnickoIme.getText());
                boolean postojiUBaziKorisnickoIme=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1]=="true"?true:false;

                if (!postojiUBaziKorisnickoIme) {
                    admin.setKorisnickoIme(tfKorisnickoIme.getText());
                } else {
                    upozorenjeKorisnickoIme();
                    return false;
                }
            }
            String staraLozinka = tfPassword.getText();
            if (!PregledRadnikaController.dodajRadnika) {
                out.writeUTF(ProtocolMessages.POSOTOJI_U_BAZI_LOZINKA.getMessage()+admin.hashSHA256(staraLozinka));
                boolean postojiUBaziLozinka=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                if (postojiUBaziLozinka) {
                    admin.setHash(tfNovaLozinka.getText());

                } else {
                    upozorenjeStaraLozinkaNijeIspravna();
                    return false;
                }
            } else {
                admin.setHash(tfPassword.getText());
            }
            admin.setTipRadnika("Administrator");
            if (PregledRadnikaController.dodajRadnika) {
                //AdministrativniRadnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak
                //            ,String korisnickoIme, String hashLozinke, String tipKorisnika)
                String stat=admin.getStatusRadnika()==true?"true":"false";
                out.writeUTF(ProtocolMessages.DODAJ_ADMINISTRATIVNOG_RADNIKA.getMessage()+admin.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getJmb()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+stat+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getKorisnickoIme()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getHash()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+"Administrator");
                if(in.readUTF().startsWith(ProtocolMessages.DODAJ_ADMINISTRATIVNOG_RADNIKA_OK.getMessage())){
                    obavjestenjeUspjesnoDodanNalog();
                    System.out.println("Dodan je Admin");
                }
                else {
                    System.out.println("Admin nije dodan");
                }
                //AdministratorDAO.dodajAdministrativnogRadnika(admin);
            } else {
                String stat=admin.getStatusRadnika()==true?"true":"false";
                String adminS=ProtocolMessages.IZMIJENI_ADMINISTRATIVNOG_RADNIKA.getMessage()+admin.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getJmb()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+stat+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getKorisnickoIme()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getHash()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+"Administrator"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+admin.getIdRadnika();
                out.writeUTF(adminS);
                if(in.readUTF().startsWith(ProtocolMessages.IZMIJENI_ADMINISTRATIVNOG_RADNIKA_OK.getMessage())){
                    obavjestenjeAzuriranNalog();
                    System.out.println("Izmijenjen je Admin");
                }
                else {
                    System.out.println("Admin nije izmijenjen");
                }
                //AdministratorDAO.izmjeniAdministratora(admin);
            }

        } else {
            upozorenjePoljaSuPrazna();
            return false;
        }

        return true;
    }

    private boolean dodajBiletara() throws IOException {
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
        if (PregledRadnikaController.dodajRadnika) {
            if (cmbTipRadnika.getSelectionModel().getSelectedItem() == null) {
                upozorenjeComboBox();
                return false;
            }
        }
        if (!tfJmb.getText().isEmpty() && !tfIme.getText().isEmpty() && !tfPrezime.getText().isEmpty()
                && !tfKontakt.getText().isEmpty() && !tfKorisnickoIme.getText().isEmpty() && !tfPassword.getText().isEmpty()) {

            String jmbRegex = "\\d+";
            Pattern pattern = Pattern.compile(jmbRegex);

            if (!pattern.matcher(tfJmb.getText()).matches() && tfJmb.getText().length() != 13) {
                upozorenjeNeispravanJMB();
                return false;
            } else {
                if (PregledRadnikaController.dodajRadnika) {
                    out.writeUTF(ProtocolMessages.PROVJERI_MATICNI_BROJ_U_BAZI.getMessage()+tfJmb.getText());
                    String res=in.readUTF();
                    String value=res.split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1];
                    boolean provjeriMaticniBrojUBazi=(value.equals("true")?true:false);
                    if (provjeriMaticniBrojUBazi) {
                        upoorenjeMaticniBroj();
                        return false;
                    }
                }
            }

            if (tfIme.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }

            if (tfPrezime.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }

            if (tfKorisnickoIme.getText().length() > 20) {
                upozorenjePredugacakUnos();
                return false;
            }

            String brojTelefona = "\\d+";
            pattern = Pattern.compile(brojTelefona);
            if (!pattern.matcher(tfKontakt.getText()).matches()) {
                upozorenjeBrjTelefona();
                return false;
            }

            Biletar biletar = new Biletar();
            if (!PregledRadnikaController.dodajRadnika) {
                biletar = (Biletar) PregledRadnikaController.izabraniRadnik;
                biletar.setIdRadnika(((Biletar) PregledRadnikaController.izabraniRadnik).getIdRadnika());
            }

            biletar.setJmb(tfJmb.getText());
            biletar.setIme(tfIme.getText());
            biletar.setPrezime(tfPrezime.getText());
            String statusRadnika = "";
            if (PregledRadnikaController.dodajRadnika) {
                statusRadnika = "Aktivan";
            } else {
                statusRadnika = cmbStatusRadnika.getSelectionModel().getSelectedItem().toString();
            }
            if (statusRadnika.equals("Aktivan")) {
                biletar.setStatusRadnika(true);
            } else {
                biletar.setStatusRadnika(false);
            }
            biletar.setKorisnickoIme(tfKorisnickoIme.getText());
            biletar.setKontak(tfKontakt.getText());
            if (!PregledRadnikaController.dodajRadnika) {

                if (!korisnickoImeIzabranogRadnika.equals(tfKorisnickoIme.getText())) {
                    out.writeUTF(ProtocolMessages.POSTOJI_U_BAZI_KORISNICKO_IME.getMessage()+tfKorisnickoIme.getText());
                    boolean postojiUBaziKorisnickoIme=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                    if (!postojiUBaziKorisnickoIme) {
                        biletar.setKorisnickoIme(tfKorisnickoIme.getText());
                    } else {
                        upozorenjeKorisnickoIme();
                        return false;
                    }
                }
            } else {
                out.writeUTF(ProtocolMessages.POSTOJI_U_BAZI_KORISNICKO_IME.getMessage()+tfKorisnickoIme.getText());
                boolean postojiUBaziKorisnickoIme=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                if (!postojiUBaziKorisnickoIme) {
                    biletar.setKorisnickoIme(tfKorisnickoIme.getText());
                } else {
                    upozorenjeKorisnickoIme();
                    return false;
                }
            }
            String staraLozinka = tfPassword.getText();
            if (!PregledRadnikaController.dodajRadnika) {
                out.writeUTF(ProtocolMessages.POSOTOJI_U_BAZI_LOZINKA.getMessage()+biletar.hashSHA256(staraLozinka));
                boolean postojiUBaziLozinka=in.readUTF().split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1].equals("true")?true:false;
                if (postojiUBaziLozinka) {
                    biletar.setHash(tfNovaLozinka.getText());

                } else {
                    upozorenjeStaraLozinkaNijeIspravna();
                    return false;
                }
            } else {
                biletar.setHash(tfPassword.getText());
            }
            biletar.setTipRadnika("Biletar");
            if (PregledRadnikaController.dodajRadnika) {
//                Biletar(String ime, String prezime, String jmb, boolean statusRadnika, String kontakt,
//                        String korisnickoIme, String hashLozinke, String tipKorisnika)
                String statusRadnikaB=biletar.isStatusRadnika()==true?"true":"false";
                out.writeUTF(ProtocolMessages.DODAJ_BILETARA.getMessage()+biletar.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                		biletar.getJmb()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+statusRadnikaB+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getKorisnickoIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                        biletar.getHash()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+ biletar.getTipRadnika());
                String resss=in.readUTF();
                if(resss.startsWith(ProtocolMessages.DODAJ_BILETARA_OK.getMessage())){
                    obavjestenjeUspjesnoDodanNalog();
                }
                else {
                    System.out.println("Biletar nije dodan");
                }
            } else {
                String statusRadnikaB=biletar.isStatusRadnika()==true?"true":"false";
                String requestic=ProtocolMessages.IZMIJENI_BILETARA.getMessage()+biletar.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                        biletar.getJmb()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+statusRadnikaB+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getKorisnickoIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                        biletar.getHash()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+ "Biletar"+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+biletar.getIdRadnika();
                out.writeUTF(requestic);
                if(in.readUTF().startsWith(ProtocolMessages.IZMIJENI_BILETARA_OK.getMessage())){
                    obavjestenjeAzuriranNalog();
                }
                else {
                    System.out.println("Biletar nije izmijenjen");
                }
            }

        } else {
            upozorenjePoljaSuPrazna();
            return false;
        }

        return true;
    }

    private boolean dodajUmjetnika() throws IOException {
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
        if (PregledRadnikaController.dodajRadnika) {
            if (cmbTipRadnika.getSelectionModel().getSelectedItem() == null) {
                upozorenjeComboBox();
                return false;
            }
        }
        if (!tfJmb.getText().isEmpty() && !tfIme.getText().isEmpty() && !tfPrezime.getText().isEmpty()
                && !tfKontakt.getText().isEmpty() && !taBiografija.getText().isEmpty()) {

            String jmbRegex = "\\d+";
            Pattern pattern = Pattern.compile(jmbRegex);

            if (!pattern.matcher(tfJmb.getText()).matches() || tfJmb.getText().length() != 13) {
                upozorenjeNeispravanJMB();
                return false;
            } else {
                if (PregledRadnikaController.dodajRadnika) {
                	out.writeUTF(ProtocolMessages.PROVJERI_MATICNI_BROJ_U_BAZI.getMessage()+tfJmb.getText());
                    String res=in.readUTF();
                    String value=res.split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage())[1];
                    boolean provjeriMaticniBrojUBazi=(value.equals("true")?true:false);
                    if (provjeriMaticniBrojUBazi) {
                        upoorenjeMaticniBroj();
                        return false;
                    }
                }
            }

            if (tfIme.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }

            if (tfPrezime.getText().length() > 40) {
                upozorenjePredugacakUnos();
                return false;
            }
            String brojTelefona = "\\d+";
            pattern = Pattern.compile(brojTelefona);
            if (!pattern.matcher(tfKontakt.getText()).matches()) {
                upozorenjeBrjTelefona();
                return false;
            }

            Umjetnik umjetnik = new Umjetnik();
            if (!PregledRadnikaController.dodajRadnika) {
                umjetnik = (Umjetnik) PregledRadnikaController.izabraniRadnik;
            }

            umjetnik.setJmb(tfJmb.getText());
            umjetnik.setIme(tfIme.getText());
            umjetnik.setPrezime(tfPrezime.getText());
            String statusRadnika = "";
            if (PregledRadnikaController.dodajRadnika) {
                statusRadnika = "Aktivan";
            } else {
                statusRadnika = cmbStatusRadnika.getSelectionModel().getSelectedItem().toString();
            }
            if (statusRadnika.equals("Aktivan")) {
                umjetnik.setStatusRadnika(true);
            } else {
                umjetnik.setStatusRadnika(false);
            }
            umjetnik.setKontak(tfKontakt.getText());
            umjetnik.setBiografija(taBiografija.getText());

            if (PregledRadnikaController.dodajRadnika) {
                String statusRadnikaB=umjetnik.isStatusRadnika()==true?"true":"false";
//                Umjetnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak, String biografija)
                out.writeUTF(ProtocolMessages.DODAJ_UMJETNIKA.getMessage()+umjetnik.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getJmb()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+statusRadnikaB+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                        umjetnik.getBiografija());
                String resss=in.readUTF();
                if(resss.startsWith(ProtocolMessages.DODAJ_UMJETNIKA_OK.getMessage())){
                    obavjestenjeUspjesnoDodanUmjetnik();
                }
                else {
                    System.out.println("Umjetnik nije dodan");
                }
            } else {
                String statusRadnikaB=umjetnik.isStatusRadnika()==true?"true":"false";
//                Umjetnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak, String biografija)
                String request=ProtocolMessages.IZMIJENI_UMJETNIKA.getMessage()+umjetnik.getIme()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getPrezime()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getJmb()+
                        ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+statusRadnikaB+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getKontakt()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+
                        umjetnik.getBiografija()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+umjetnik.getIdRadnika();
                out.writeUTF(request);
                String ress=in.readUTF();
                if(ress.startsWith(ProtocolMessages.IZMIJENI_UMJETNIKA_OK.getMessage())){
                    obavjestenjeAzuriranNalog();
                }
                else {
                    System.out.println("Umjetnik nije azuriran");
                }
            }
        } else {
            upozorenjePoljaSuPrazna();
            return false;
        }
        return true;
    }

    public boolean unesiZaposlenog() throws  IOException{
        if (PregledRadnikaController.dodajRadnika) {
            if (cmbTipRadnika.getValue().equals("Biletar")) {
                if (dodajBiletara()) {
                    return true;
                } else {
                    return false;
                }
            }
            if (cmbTipRadnika.getValue().equals("Umjetnik")) {
                if (dodajUmjetnika()) {
                    return true;
                } else {
                    return false;
                }
            }
            if (cmbTipRadnika.getValue().equals("Administrativni radnik")) {
                if (dodajAdministrativnogRadnika()) {
                    return true;
                } else {
                    return false;
                }
            }

        } else if (!PregledRadnikaController.dodajRadnika)  {
            Radnik radnik = PregledRadnikaController.izabraniRadnik;
            if (radnik.getTipRadnika().equals("Biletar")) {
                if (dodajBiletara()) {
                    return true;
                } else {
                    return false;
                }
            } else if (radnik.getTipRadnika().equals("Umjetnik")) {
                if (dodajUmjetnika()) {
                    return true;
                } else {
                    return false;
                }
            } else if (radnik.getTipRadnika().equals("Administrator")) {
                if (dodajAdministrativnogRadnika()) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @FXML
    void potvrdiUnosRadnikaAction(ActionEvent event) throws IOException{
        if (PregledRadnikaController.dodajRadnika) {
            if (cmbTipRadnika.getSelectionModel().isEmpty()) {
                upozorenjeComboBox();
                return;
            }
        }
        if (unesiZaposlenog()) {
            Parent dodajZaposlenogView;
            try {
                dodajZaposlenogView = FXMLLoader.load(getClass().getResource("/view/PregledRadnika.fxml"));

                Scene dodajZaposlenogScene = new Scene(dodajZaposlenogView);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(dodajZaposlenogScene);
                window.show();

                Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
                window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
            } catch (IOException ex) {
                Logger.getLogger(DodajRadnikaController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            return;
        }
    }

    @FXML
    void odustaniOdUnosaRadnikaAction(ActionEvent event) {
        Parent dodajZaposlenogView;
        try {
            dodajZaposlenogView = FXMLLoader.load(getClass().getResource("/view/PregledRadnika.fxml"));

            Scene dodajZaposlenogScene = new Scene(dodajZaposlenogView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(dodajZaposlenogScene);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(DodajRadnikaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sakriPolja() {
        tfIme.setVisible(false);
        tfPrezime.setVisible(false);
        tfJmb.setVisible(false);
        tfKontakt.setVisible(false);
        tfKorisnickoIme.setVisible(false);
        tfPassword.setVisible(false);
        taBiografija.setVisible(false);
        tfNovaLozinka.setVisible(false);

        lIme.setVisible(false);
        lPrezime.setVisible(false);
        lJmb.setVisible(false);
        lStatusRadnika.setVisible(false);
        lKontakt.setVisible(false);
        lKorisnickoIme.setVisible(false);
        lLozinka.setVisible(false);
        lBiografija.setVisible(false);
        lNovaLozinka.setVisible(false);
    }

    private void prikazRadnikaKojiKoristiSistem() {
        if (PregledRadnikaController.dodajRadnika) {
            cmbStatusRadnika.setVisible(false);
        } else {
            cmbStatusRadnika.setVisible(true);
        }
        tfIme.clear();
        tfIme.setVisible(true);
        tfPrezime.clear();
        tfPrezime.setVisible(true);
        tfJmb.clear();
        tfJmb.setVisible(true);
        tfKontakt.clear();
        tfKontakt.setVisible(true);
        tfKorisnickoIme.clear();
        tfKorisnickoIme.setVisible(true);
        tfPassword.clear();
        tfPassword.setVisible(true);
        if (PregledRadnikaController.dodajRadnika) {
            tfNovaLozinka.setVisible(false);
        } else {
            tfNovaLozinka.setVisible(true);
        }

        lIme.setVisible(true);
        lPrezime.setVisible(true);
        lJmb.setVisible(true);
        if (PregledRadnikaController.dodajRadnika) {
            lStatusRadnika.setVisible(false);
        } else {
            lStatusRadnika.setVisible(true);
        }
        lKontakt.setVisible(true);
        lKorisnickoIme.setVisible(true);
        lLozinka.setVisible(true);
        if (!PregledRadnikaController.dodajRadnika) {
            lLozinka.setText("Stara lozinka");
        }
        if (PregledRadnikaController.dodajRadnika) {
            lNovaLozinka.setVisible(false);
        } else {
            lNovaLozinka.setVisible(true);
        }
    }

    private void prikazUmjetnika() {
        if (PregledRadnikaController.dodajRadnika) {
            cmbStatusRadnika.setVisible(false);
        } else {
            cmbStatusRadnika.setVisible(true);
        }
        tfIme.setVisible(true);
        tfPrezime.setVisible(true);
        tfJmb.setVisible(true);
        tfKontakt.setVisible(true);
        taBiografija.setVisible(true);

        lIme.setVisible(true);
        lPrezime.setVisible(true);
        lJmb.setVisible(true);
        if (PregledRadnikaController.dodajRadnika) {
            lStatusRadnika.setVisible(false);
        } else {
            lStatusRadnika.setVisible(true);
        }
        lKontakt.setVisible(true);
        lBiografija.setVisible(true);
    }

    @FXML
    private void prikazPoljaNaOsnovuTipaRadnikaIzKomboBoksa() {
        sakriPolja();
        if (cmbTipRadnika.getValue().toString().equals("Biletar")) {
            prikazRadnikaKojiKoristiSistem();
        } else if (cmbTipRadnika.getValue().toString().equals("Administrativni radnik")) {
            prikazRadnikaKojiKoristiSistem();
        } else if (cmbTipRadnika.getValue().toString().equals("Umjetnik")) {
            prikazUmjetnika();
        }
    }

    private void postavljanjeTextFieldovaZaUmjetnika() {
        tfIme.setText(PregledRadnikaController.izabraniRadnik.getIme());
        tfPrezime.setText(PregledRadnikaController.izabraniRadnik.getPrezime());
        tfJmb.setText(PregledRadnikaController.izabraniRadnik.getJmb());
        tfKontakt.setText(PregledRadnikaController.izabraniRadnik.getKontakt());
        taBiografija.setText(((Umjetnik) PregledRadnikaController.izabraniRadnik).getBiografija());
        tfJmb.setEditable(false);
    }

    private void postavljanjeTextFieldovaZaRadnikaKojiKoristiSistem() {
        tfIme.setText(PregledRadnikaController.izabraniRadnik.getIme());
        tfPrezime.setText(PregledRadnikaController.izabraniRadnik.getPrezime());
        tfJmb.setText(PregledRadnikaController.izabraniRadnik.getJmb());
        tfKontakt.setText(PregledRadnikaController.izabraniRadnik.getKontakt());
        if (PregledRadnikaController.izabraniRadnik instanceof Biletar) {
            tfKorisnickoIme.setText(((Biletar) PregledRadnikaController.izabraniRadnik).getKorisnickoIme());
        } else {
            tfKorisnickoIme.setText(((AdministrativniRadnik) PregledRadnikaController.izabraniRadnik).getKorisnickoIme());
        }
        tfPassword.setText("");
        tfJmb.setEditable(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bPotvrdi.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
        bOdustani.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
        sakriPolja();
        cmbStatusRadnika.getItems().addAll("Aktivan", "Neaktivan");
        cmbTipRadnika.getItems().addAll("Biletar", "Umjetnik", "Administrativni radnik");
        cmbStatusRadnika.setVisible(false);
        cmbStatusRadnika.getSelectionModel().selectFirst();
        if (!PregledRadnikaController.dodajRadnika) {

            tfJmb.setEditable(false);
            cmbTipRadnika.setVisible(false);
            if (PregledRadnikaController.tipRadnika.equals("Biletar")) {
                korisnickoImeIzabranogRadnika = ((Biletar) PregledRadnikaController.izabraniRadnik).getKorisnickoIme();
                if (PregledRadnikaController.izabraniRadnik.isStatusRadnika()) {
                    cmbStatusRadnika.getSelectionModel().selectFirst();
                } else {
                    cmbStatusRadnika.getSelectionModel().selectLast();
                }
                cmbStatusRadnika.setVisible(true);
                prikazRadnikaKojiKoristiSistem();
                postavljanjeTextFieldovaZaRadnikaKojiKoristiSistem();

            } else if (PregledRadnikaController.tipRadnika.equals("Umjetnik")) {
                if (PregledRadnikaController.izabraniRadnik.isStatusRadnika()) {
                    cmbStatusRadnika.getSelectionModel().selectFirst();
                } else {
                    cmbStatusRadnika.getSelectionModel().selectLast();
                }
                cmbStatusRadnika.setVisible(true);
                prikazUmjetnika();
                postavljanjeTextFieldovaZaUmjetnika();

            } else if (PregledRadnikaController.tipRadnika.equals("Administrator")) {
                korisnickoImeIzabranogRadnika = ((AdministrativniRadnik) PregledRadnikaController.izabraniRadnik).getKorisnickoIme();
                if (PregledRadnikaController.izabraniRadnik.isStatusRadnika()) {
                    cmbStatusRadnika.getSelectionModel().selectFirst();
                } else {
                    cmbStatusRadnika.getSelectionModel().selectLast();
                }
                cmbStatusRadnika.setVisible(true);
                prikazRadnikaKojiKoristiSistem();
                postavljanjeTextFieldovaZaRadnikaKojiKoristiSistem();
                tfJmb.setEditable(false);
            }
        }
    }

    private void obavjestenjeUspjesnoDodanNalog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspjesno dodan nalog!");
        alert.setHeaderText(null);
        alert.setContentText("Uspjesno dodan nalog!");
        alert.showAndWait();
    }
    private void obavjestenjeAzuriranNalog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspjesno azuriran nalog!");
        alert.setHeaderText(null);
        alert.setContentText("Uspjesno azuriran nalog!");
        alert.showAndWait();
    }

    private void obavjestenjeUspjesnoDodanUmjetnik() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspjesno dodan umjetnik!");
        alert.setHeaderText(null);
        alert.setContentText("Uspjesno dodan umjetnik!");
        alert.showAndWait();
    }

    private void upozorenjeNeispravanJMB() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa JMB !");
        alert.setHeaderText(null);
        alert.setContentText("Provjerite da li JMB ima 13 karaktera.");
        alert.showAndWait();
    }

    private void upoorenjeMaticniBroj() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Uneseni JMB vec postoji u bazi podataka.");
        alert.showAndWait();
    }

    private void upozorenjePoljaSuPrazna() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Polja su prazna.");
        alert.showAndWait();
    }

    private void upozorenjeComboBox() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom odabira u combobox-u !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite iz padajuceg menija u combobox-u");
        alert.showAndWait();
    }

    private void upozorenjePredugacakUnos() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Predugacak unos!");
        alert.showAndWait();
    }

    private void upozorenjeBrjTelefona() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa podataka !");
        alert.setHeaderText(null);
        alert.setContentText("Provjerite broj telefona!");
        alert.showAndWait();
    }

    private void upozorenjeKorisnickoIme() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa korisnickog imena!");
        alert.setHeaderText(null);
        alert.setContentText("Korisnicko ime vec postoji u bazi!");
        alert.showAndWait();
    }

    private void upozorenjeLozinka() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa lozinka!");
        alert.setHeaderText(null);
        alert.setContentText("Lozinka vec postoji u bazi!");
        alert.showAndWait();
    }

    private void upozorenjeStaraLozinkaNijeIspravna() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom unosa lozinka!");
        alert.setHeaderText(null);
        alert.setContentText("Neispravna stara lozinka!");
        alert.showAndWait();
    }

}
