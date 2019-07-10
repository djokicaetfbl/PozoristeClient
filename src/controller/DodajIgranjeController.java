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
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.GostujucaPredstava;
import model.dto.Igranje;
import model.dto.Predstava;
import model.dto.Scena;
import util.ProtocolMessages;


public class DodajIgranjeController implements Initializable {

    @FXML
    private ComboBox<Object> cmbPredstave;

    @FXML
    private DatePicker dpTerminPredstave;

    @FXML
    private Button bDodaj;

    @FXML
    private Button bZavrsi;

    @FXML
    private Button bUkloni;

    @FXML
    private ComboBox<Object> cmbIgranjaZaRepertoar;
    @FXML
    private Label lUkloniIgranje;

    @FXML
    void ukloniPredstavuAction(ActionEvent event) throws UnknownHostException, IOException {
        if (!cmbIgranjaZaRepertoar.getSelectionModel().isEmpty()) {
//            Predstava predstavaIzCmb = (Predstava) cmbIgranjaZaRepertoar.getSelectionModel().getSelectedItem();
            Object obj= cmbIgranjaZaRepertoar.getSelectionModel().getSelectedItem();
            Predstava predstavaIzCmb=null;
            GostujucaPredstava gostujucaPredstavaIzCmb=null;
            LinkedList<Igranje> igranjeIzCmb = new LinkedList<>();
            if(obj instanceof Predstava) {
                predstavaIzCmb=(Predstava) cmbIgranjaZaRepertoar.getSelectionModel().getSelectedItem();
                Predstava finalPredstavaIzCmb = predstavaIzCmb;
                igranjeIzCmb.addAll(PregledSvihRepertoaraController.izabraniRepertoar.getIgranja().stream().filter(x -> x.getIdPredstave().equals(finalPredstavaIzCmb.getId())).collect(Collectors.toList()));

            }
            else {
                gostujucaPredstavaIzCmb=(GostujucaPredstava)cmbIgranjaZaRepertoar.getSelectionModel().getSelectedItem();
                GostujucaPredstava finalGostujucaPredstavaIzCmb = gostujucaPredstavaIzCmb;
                igranjeIzCmb.addAll(PregledSvihRepertoaraController.izabraniRepertoar.getIgranja().stream().filter(x -> x.getIdGostujucePredstave().equals(finalGostujucaPredstavaIzCmb.getId())).collect(Collectors.toList()));
            }
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
            Igranje igranje=igranjeIzCmb.getFirst();
//            Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
            Date date = igranje.getTermin();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(date);
            String request=strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+igranje.getIdScene()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+igranje.getIdPredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+igranje.getIdGostujucePredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+igranje.getIdRepertoara();
            out.writeUTF(ProtocolMessages.UKLONI_IGRANJE.getMessage()+request);
            System.out.println(in.readUTF());
            obavjestenjePredstavaUspjesnoUklonjena();
        } else {
            upozorenjeIzaberitePredstavuZaBrisati();
        }
    }

    @FXML
    void dodajIgranjeAction(ActionEvent event) throws  IOException{
        if (!dodajIgranje()) {
            return;
        }
    }

    private boolean dodajIgranje() throws  IOException{

    	if (cmbPredstave.getSelectionModel().isEmpty()) {
            upozorenjeIzaberitePredstavu();
            return false;
        }

        if (dpTerminPredstave.getValue() == null) {
            upozorenjeTermin();
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar trenutni = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dpTerminPredstave.getValue().getYear(), dpTerminPredstave.getValue().getMonthValue() - 1, dpTerminPredstave.getValue().getDayOfMonth());


        Calendar kalendarRepertoar = Calendar.getInstance();
        if (dpTerminPredstave.getValue().getYear() != DodajRepertoarController.godinaRepertoara
                || ((dpTerminPredstave.getValue().getMonthValue()) != (DodajRepertoarController.mjesecRepertoara - 1))) {
            upozorenjeTerminPredstave();
            return false;
        }

        try {
            if (sdf.parse(sdf.format(calendar.getTime())).before(sdf.parse(sdf.format(trenutni.getTime())))) {
                upozorenjeTermin();
                return false;
            }
        } catch (ParseException ex) {
            Logger.getLogger(DodajIgranjeController.class.getName()).log(Level.SEVERE, null, ex);
        }

        sdf.format(new Date(calendar.getInstance().getTimeInMillis()));
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
        out.writeUTF(ProtocolMessages.SCENE.getMessage());
        String sceneResponse=in.readUTF();
        List<Scena> scenaList=new ArrayList<>();
        if(sceneResponse.startsWith(ProtocolMessages.SCENE_RESPONSE.getMessage())){
            String[] sceneLines=sceneResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<sceneLines.length; i++){
                String[] sceneString=sceneLines[0].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0){
                    //Scena(Integer id,String naziv)
                    Scena scena=new Scena(Integer.parseInt(sceneString[1]), sceneString[2]);
                    scenaList.add(scena);
                }
                else{
                    Scena scena=new Scena(Integer.parseInt(sceneString[0]), sceneString[1]);
                    scenaList.add(scena);
                }
            }
        }
        List<Scena> scena = scenaList;

        out.writeUTF(ProtocolMessages.GET_IGRANJA.getMessage()+DodajRepertoarController.repertoar.getId());
		String igranjaResponse=in.readUTF();
        Igranje novoIgranje = new Igranje(new Date(calendar.getTimeInMillis()), scena.get(0).getIdScene(), (cmbPredstave.getSelectionModel().getSelectedItem() instanceof Predstava) ? ((Predstava) cmbPredstave.getSelectionModel().getSelectedItem()).getId() : null, (cmbPredstave.getSelectionModel().getSelectedItem() instanceof GostujucaPredstava) ? ((GostujucaPredstava) cmbPredstave.getSelectionModel().getSelectedItem()).getId() : null, DodajRepertoarController.repertoar.getId());
        LinkedList<Igranje> svaIgranja=new LinkedList<Igranje>();
		if(igranjaResponse.startsWith(ProtocolMessages.GET_IGRANJA_RESPONSE.getMessage())) {
    		String[] igranjaLines=igranjaResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
    		for(int j=0; j<igranjaLines.length; j++) {
    			String[] igranjaString=igranjaLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
    			if(j==0) {
//         			Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
        			java.util.Date datum1=null;
					try {
						datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[1]);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		Date termin1=new Date(datum1.getTime());
        			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[2]), Integer.parseInt(igranjaString[3]),Integer.parseInt(igranjaString[4]), Integer.parseInt(igranjaString[5]));
        			svaIgranja.add(igranje);
    			}
    			else {
    				java.util.Date datum1=null;
					try {
						datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[0]);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		Date termin1=new Date(datum1.getTime());
        			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[1]), Integer.parseInt(igranjaString[2]),Integer.parseInt(igranjaString[3]), Integer.parseInt(igranjaString[4]));
        			svaIgranja.add(igranje);
    			}
    		}
		}
        Date date = novoIgranje.getTermin();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date);
        String novoIgranjeString=strDate+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+novoIgranje.getIdScene()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+novoIgranje.getIdPredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+novoIgranje.getIdGostujucePredstave()+ProtocolMessages.MESSAGE_SEPARATOR.getMessage()+novoIgranje.getIdRepertoara();
        if (!svaIgranja.stream().filter(x -> sdf.format(x.getTermin()).equals(sdf.format(novoIgranje.getTermin()))).findAny().isPresent()) {
            out.writeUTF(ProtocolMessages.DODAJ_IGRANJE.getMessage()+novoIgranjeString);
            if(in.readUTF().startsWith(ProtocolMessages.DODAJ_IGRANJE_OK.getMessage())){
                obavjestenjePredstavaUspjesnoDodata();
            }
            else{
                System.out.println("Igranje nije dodato, server NOT_OK");
            }
            return true;

        } else {
            upozorenjePredstavaSeVecIgraNaTajDan();
            return false;
        }
    }

    @FXML
    void nazadNaDodajRepertoar(ActionEvent event) {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/DodajRepertoar.fxml"));

            Scene dodajRadnikaScene = new Scene(adminController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void zavrsiDodavanjeRepertoara(ActionEvent event) {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/PregledSvihRepertoara.fxml"));

            Scene dodajRadnikaScene = new Scene(adminController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.centerOnScreen();
            window.setScene(dodajRadnikaScene);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(DodajIgranjeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ubaciUCMBPredstave() throws  IOException {
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
        cmbPredstave.getItems().addAll(predstavaList);

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
        cmbPredstave.getItems().addAll(gostujucaPredstavaList);
    }

    private void ubaciUCMBIgranjaZaRepertoar() throws  IOException {
        final LinkedList<Predstava> predstave = new LinkedList<>();
        final LinkedList<GostujucaPredstava> gostujuce = new LinkedList<>();
        List<Integer> nadjiOBicne = PregledSvihRepertoaraController.izabraniRepertoar.getIgranja().stream().mapToInt(e -> (e.getIdPredstave() != 0 ? e.getIdPredstave() : 0)).boxed().collect(Collectors.toList());
        List<Integer> nadjiGostujuce = PregledSvihRepertoaraController.izabraniRepertoar.getIgranja().stream().mapToInt(e -> (e.getIdGostujucePredstave() != 0 ? e.getIdGostujucePredstave() : 0)).boxed().collect(Collectors.toList());
        Long obicneBroj = nadjiOBicne.stream().count();
        Long gostujuceBroj = nadjiGostujuce.stream().count();
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
       nadjiOBicne.forEach(e -> {
            Optional<Predstava> op = predstavaList.stream().filter(p -> p.getId() == e).findFirst();
            if (op.isPresent()) {
                predstave.add(op.get());
            }
        });

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
        nadjiGostujuce.forEach(e -> {
            Optional<GostujucaPredstava> op = gostujucaPredstavaList.stream().filter(p -> p.getId() == e).findFirst();
            if (op.isPresent()) {
                gostujuce.add(op.get());
            }
        });
        cmbIgranjaZaRepertoar.getItems().addAll(predstave);
        cmbIgranjaZaRepertoar.getItems().addAll(gostujuce);

    }

    private void obavjestenjePredstavaUspjesnoDodata() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspjesno uklanjanje predstave!");
        alert.setHeaderText(null);
        alert.setContentText("Uspjesno dodana predstave!");
        alert.showAndWait();
    }

    private void obavjestenjePredstavaUspjesnoUklonjena() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Uspjesno uklanjanje predstave!");
        alert.setHeaderText(null);
        alert.setContentText("Uspjesno uklanjanje predstave!");
        alert.showAndWait();
    }

    private void upozorenjePredstavaSeVecIgraNaTajDan() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom dodavanja predstave !");
        alert.setHeaderText(null);
        alert.setContentText("Termin popunjen izaberite drugi!");
        alert.showAndWait();
    }

    private void upozorenjeTerminPredstave() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom dodavanja predstave !");
        alert.setHeaderText(null);
        alert.setContentText("Pogresan termin , prilagodite igranje predstave odgovarajucem repertoaru!");
        alert.showAndWait();
    }

    private void upozorenjeTermin() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom izbora termina !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite termin");
        alert.showAndWait();
    }

    private void upozorenjeIzaberitePredstavu() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom izbora predstave !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite predstavu!");
        alert.showAndWait();
    }

    private void upozorenjeIzaberitePredstavuZaBrisati() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom izbora predstave !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite predstavu za brisanje!");
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bDodaj.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_plus.png"))));
        bZavrsi.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/accept.png"))));
        bUkloni.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/minus.png"))));
        try {
            ubaciUCMBPredstave();
            lUkloniIgranje.setVisible(false);
            cmbIgranjaZaRepertoar.setVisible(false);
            bUkloni.setVisible(false);
            if (PregledSvihRepertoaraController.izmjenaRepertoara) {
                lUkloniIgranje.setVisible(true);
                cmbIgranjaZaRepertoar.setVisible(true);
                bUkloni.setVisible(true);
                ubaciUCMBIgranjaZaRepertoar();
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
