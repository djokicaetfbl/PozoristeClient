package controller;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.*;

import util.ProtocolMessages;

public class PregledRepertoaraController implements Initializable {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="scrollPane"
    private ScrollPane scrollPane; // Value injected by FXMLLoader

    @FXML // fx:id="buttonNazad"
    private Button buttonNazad; // Value injected by FXMLLoader

    private static int brojPredstava = 2;

    public static String predstavaSaKojomRadim = "";

    public static Scena scena;

    private static Repertoar repertoarZaPrikaz;

    public static List<Date> listaDatumaRepertoara = new ArrayList<>();

    private static int igranjePodBrojem=0;

    private VBox vBox = null;

    public static boolean igranjeProslo = false;

    @FXML
    private Button bIzlaz;

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
            listaDatumaRepertoara.clear();
        } catch (IOException ex) {
            Logger.getLogger(PregledRepertoaraController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if("Administrator".equals(LogInController.tipKorisnika)){
            bIzlaz.setVisible(false);
        }else {
            bIzlaz.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/rsz_logout.png"))));
        }
        buttonNazad.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/resursi/back.png"))));
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

        if (!"Administrator".equals(LogInController.tipKorisnika)) {
            buttonNazad.setVisible(false);
            out.writeUTF(ProtocolMessages.REPERTOARS.getMessage());
            String response = in.readUTF();

            List<Repertoar> repertoarList = new ArrayList<>();
            String[] repertoarLines = response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<repertoarLines.length; i++) {
            	String[] repertoarString=repertoarLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
            	if(i==0) {
            		java.util.Date datum=null;
					try {
						datum = new SimpleDateFormat("yyyy-MM-dd").parse(repertoarString[2]);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		Date termin=new Date(datum.getTime());
            		Repertoar repertoar=new Repertoar(Integer.parseInt(repertoarString[1]), termin);
            		out.writeUTF(ProtocolMessages.GET_IGRANJA.getMessage()+repertoar.getId());
            		String igranjaResponse=in.readUTF();
            		if(igranjaResponse.startsWith(ProtocolMessages.GET_IGRANJA_RESPONSE.getMessage())) {
	            		LinkedList<Igranje> igranjaList=new LinkedList<Igranje>();
	            		String[] igranjaLines=igranjaResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
	            		for(int j=0; j<igranjaLines.length; j++) {
	            			String[] igranjaString=igranjaLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
	            			if(j==0) {
		   //         			Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
		            			java.util.Date datum1=null;
								try {
									datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[1]);
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
		                		Date termin1=new Date(datum1.getTime());
		            			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[2]), Integer.parseInt(igranjaString[3]),Integer.parseInt(igranjaString[4]), Integer.parseInt(igranjaString[5]));
		            			igranjaList.add(igranje);
	            			}
	            			else {
	            				java.util.Date datum1=null;
								try {
									datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[0]);
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
		                		Date termin1=new Date(datum1.getTime());
		            			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[1]), Integer.parseInt(igranjaString[2]),Integer.parseInt(igranjaString[3]), Integer.parseInt(igranjaString[4]));
		            			igranjaList.add(igranje);
	            			}
	            		}
	            		repertoar.setIgranja(igranjaList);
            		}
            		repertoarList.add(repertoar);
            	}
            	else {
            		java.util.Date datum=null;
					try {
						datum = new SimpleDateFormat("yyyy-MM-dd").parse(repertoarString[1]);
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		Date termin=new Date(datum.getTime());
            		Repertoar repertoar=new Repertoar(Integer.parseInt(repertoarString[0]), termin);
            		out.writeUTF(ProtocolMessages.GET_IGRANJA.getMessage()+repertoar.getId());
            		String igranjaResponse=in.readUTF();
            		if(igranjaResponse.startsWith(ProtocolMessages.GET_IGRANJA_RESPONSE.getMessage())) {
	            		LinkedList<Igranje> igranjaList=new LinkedList<Igranje>();
	            		String[] igranjaLines=igranjaResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
	            		for(int j=0; j<igranjaLines.length; j++) {
	            			String[] igranjaString=igranjaLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
	            			if(j==0) {
		   //         			Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
		            			java.util.Date datum1=null;
								try {
									datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[1]);
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
		                		Date termin1=new Date(datum1.getTime());
		            			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[2]), Integer.parseInt(igranjaString[3]),Integer.parseInt(igranjaString[4]), Integer.parseInt(igranjaString[5]));
		            			igranjaList.add(igranje);
	            			}
	            			else {
	            				java.util.Date datum1=null;
								try {
									datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[0]);
								} catch (ParseException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
		                		Date termin1=new Date(datum1.getTime());
		            			Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[1]), Integer.parseInt(igranjaString[2]),Integer.parseInt(igranjaString[3]), Integer.parseInt(igranjaString[4]));
		            			igranjaList.add(igranje);
	            			}
	            		}
	            		repertoar.setIgranja(igranjaList);
            		}
            		repertoarList.add(repertoar);
            	}
            }
            Optional<Repertoar> op = repertoarList.stream().filter(e -> {
                Calendar kalendar = Calendar.getInstance();
                kalendar.setTime(e.getMjesecIGodina());
                Calendar trenutni = Calendar.getInstance();
                return kalendar.get(Calendar.YEAR) == trenutni.get(Calendar.YEAR) && kalendar.get(Calendar.MONTH) == trenutni.get(Calendar.MONTH);
            }).findAny();
            if (op.isPresent()) {
                repertoarZaPrikaz = op.get();
            } else {
                repertoarZaPrikaz = null;
            }
        }
        buttonNazad.setOnAction(e -> buttonSetAction());

        if (repertoarZaPrikaz != null && !repertoarZaPrikaz.getIgranja().isEmpty()) {
            vBox = new VBox();
            final LinkedList<Predstava> predstave = new LinkedList<>();
            final LinkedList<GostujucaPredstava> gostujuce = new LinkedList<>();
            List<Integer> nadji = repertoarZaPrikaz.getIgranja().stream().mapToInt(e -> (e.getIdPredstave() != 0 ? e.getIdPredstave() : e.getIdGostujucePredstave())).boxed().collect(Collectors.toList());
            out.writeUTF(ProtocolMessages.PREDSTAVE.getMessage());
            String responsePredstave = in.readUTF();
            out.close();
            in.close();
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
            predstavaList.stream().forEach(e -> {
                if (nadji.stream().filter(p -> p == e.getId()).findFirst().isPresent()) {
                    predstave.add(e);
                }
            });
            addr = InetAddress.getByName(Pozoriste.HOST);
            // otvori socket prema drugom racunaru
            sock = new Socket(addr, Pozoriste.PORT);
            // inicijalizuj ulazni stream
            in = new DataInputStream(
                    sock.getInputStream());
            // inicijalizuj izlazni stream
            out = new DataOutputStream(
                    sock.getOutputStream());
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
            gostujucaPredstavaList.stream().forEach(e -> {
                if (nadji.stream().filter(p -> p == e.getId()).findFirst().isPresent()) {
                    gostujuce.add(e);
                }
            });
            repertoarZaPrikaz.getIgranja().sort(Comparator.comparing(e -> e.getTermin()));

            PregledRepertoaraController.listaDatumaRepertoara.clear();
            System.out.println("obrisao");
            for (Integer i = 0; i < repertoarZaPrikaz.getIgranja().size(); i++) {
                HBox hBox = new HBox();
                Label vrijeme = new Label();
                vrijeme.setId(i.toString());
                hBox.setId(i.toString());

                final Igranje igranje = repertoarZaPrikaz.getIgranja().get(i);
                String stringZaPrikaz = "";
                if (igranje.getIdPredstave() != 0) {
                    stringZaPrikaz += predstave.stream().filter(e -> e.getId() == igranje.getIdPredstave()).findFirst().get().getNaziv();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd:hh-mm");
                    //vrijeme.setText(format.format(igranje.getTermin()));
                    Date datumIgranja = igranje.getTermin();
                    listaDatumaRepertoara.add(datumIgranja);
                    LocalDate ld = datumIgranja.toLocalDate();
                    LocalDateTime ldt = LocalDateTime.of(ld.getYear(),ld.getMonthValue(),ld.getDayOfMonth(),20,0);
                    vrijeme.setText(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm")));
                }
                if (igranje.getIdGostujucePredstave() != 0) {
                    stringZaPrikaz += gostujuce.stream().filter(e -> e.getId() == igranje.getIdGostujucePredstave()).findFirst().get().getNaziv();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd:hh-mm");
                    //vrijeme.setText(format.format(igranje.getTermin()));
                    Date datumIgranja = igranje.getTermin();
                    listaDatumaRepertoara.add(datumIgranja);
                    LocalDate ld = datumIgranja.toLocalDate();
                    LocalDateTime ldt = LocalDateTime.of(ld.getYear(),ld.getMonthValue(),ld.getDayOfMonth(),20,0);
                    vrijeme.setText(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm")));
                }
                Label nazivLabel = new Label(stringZaPrikaz);
                nazivLabel.setId(i.toString());
                setLabel(nazivLabel, vrijeme);
                hBox.setMinWidth(900);
                hBox.getChildren().add(nazivLabel);
                hBox.getChildren().add(vrijeme);
                if ("Biletar".equals(LogInController.tipKorisnika)) {
                    labelSetAction(nazivLabel, vrijeme);
                }
                vBox.getChildren().add(hBox);
            }
            vBox.setMaxWidth(747);
            scrollPane.vvalueProperty().bind(vBox.heightProperty());
            scrollPane.setContent(vBox);

        } else {
            if (repertoarZaPrikaz == null) {
                Platform.runLater(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(750);
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Za " + (new SimpleDateFormat("MMM").format(Calendar.getInstance().getTime())) + " nije unjet repertoar", ButtonType.OK);
                        alert.setTitle("Upozorenje");
                        alert.setHeaderText("Upozorenje");
                        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
                        alert.showAndWait();;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PregledRepertoaraController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } else if (repertoarZaPrikaz.getIgranja().isEmpty()) {
                Platform.runLater(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(750);
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Igranja nisu unjeta za ovaj repertoar", ButtonType.OK);
                        alert.setTitle("Upozorenje");
                        alert.setHeaderText("Upozorenje");
                        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
                        alert.showAndWait();;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PregledRepertoaraController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        }
        }catch (Exception exc){
            exc.printStackTrace();
        }
    }

    public static void incijalizacija(Repertoar repertoar) {
        repertoarZaPrikaz = repertoar;
    }

    private void pregledRepertoara(Label label, Label vrijeme) {

        Igranje zeljenoIgranje = null;
        try {
            predstavaSaKojomRadim = label.getText();
            String string = vrijeme.getText();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd:hh-mm", Locale.GERMANY);
            java.util.Date dat=format.parse(string);
            Date date = new Date(dat.getTime());
            igranjePodBrojem=0;
            igranjeProslo = false;
            PregledRepertoaraController.listaDatumaRepertoara.stream().forEach(p -> {
                if(date.toLocalDate().getDayOfMonth() == p.toLocalDate().getDayOfMonth()
                && date.toLocalDate().getYear() == p.toLocalDate().getYear()
                && date.toLocalDate().getMonthValue() == p.toLocalDate().getMonthValue()){
                    if(LocalDate.now().isAfter(date.toLocalDate())){
                        igranjeProslo = true;
                    }
                }else{
                    igranjePodBrojem++;
                }
            });
            zeljenoIgranje = repertoarZaPrikaz.getIgranja().stream().filter(e -> e.getTermin().equals(listaDatumaRepertoara.get(igranjePodBrojem))).findFirst().get();
        } catch (Exception e) {
            Logger.getLogger(PregledPredstavaController.class.getName()).log(Level.SEVERE, null, e);
        }
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
            final Igranje temp = zeljenoIgranje;
            PregledKarataController.scenaZaPrikaz = new Scena(temp.getIdScene(), scenaList.stream().filter(e -> e.getIdScene().equals(temp.getIdScene())).findAny().get().getNazivScene());
            PregledKarataController.terminPredstave = temp.getTermin();
            Parent pregledKarataController = FXMLLoader.load(getClass().getResource("/view/PregledKarata.fxml"));
            Scene scene = new Scene(pregledKarataController);
            Stage stage = (Stage) buttonNazad.getScene().getWindow();
            stage.centerOnScreen();
            stage.getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/drama.png")));
            stage.setScene(scene);
            stage.setTitle("Karte " + "za predstavu " + predstavaSaKojomRadim);
            stage.setResizable(false);
            stage.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
        } catch (Exception ex) {
            //Logger.getLogger(PregledPredstavaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setLabel(Label label, Label vrijeme) {
        if ("Administrator".equals(LogInController.tipKorisnika)) {
            label.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
        }
        label.setMinWidth(665);
        label.setMinHeight(40);
        label.setFont(new Font(16));
        label.setStyle("-fx-font-weight: bold");
        label.setPadding(new Insets(0, 0, 0, 10));

        vrijeme.setMinWidth(230);
        vrijeme.setMinHeight(40);
        vrijeme.setFont(new Font(16));
        vrijeme.setStyle("-fx-font-weight: bold");
        vrijeme.setPadding(new Insets(0, 0, 0, 0));
        if (Integer.parseInt(label.getId()) % 2 == 0) {
            label.setStyle("-fx-background-color: #90c8ff");
        } else {
            label.setStyle("-fx-background-color: #e6e6e6");
        }

        if (Integer.parseInt(vrijeme.getId()) % 2 == 0) {
            vrijeme.setStyle("-fx-background-color: #90c8ff");
        } else {
            vrijeme.setStyle("-fx-background-color: #e6e6e6");
        }

    }

    private void buttonSetAction() {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/Admin.fxml"));
            Scene adminControllerScene = new Scene(adminController);
            Stage window = (Stage) buttonNazad.getScene().getWindow();
            window.centerOnScreen();
            window.setScene(adminControllerScene);
            window.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((primScreenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((primScreenBounds.getHeight() - window.getHeight()) / 2);
        } catch (IOException ex) {
            Logger.getLogger(PregledRepertoaraController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void labelSetAction(Label label, Label vrijeme) {
        label.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (((MouseEvent) event).getClickCount() == 2) {
                    Optional<Node> hOpt = vBox.getChildren().stream().filter(e -> e.getId().equals(label.getId())).findFirst();
                    if (hOpt.isPresent()) {
                        pregledRepertoara((Label) ((HBox) hOpt.get()).getChildren().get(0), (Label) ((HBox) hOpt.get()).getChildren().get(1));
                    }
                }
            }
        });
        label.setOnMouseEntered(event -> {
            label.setStyle("-fx-border-color: #005cb7");
        });
        label.setOnMouseExited(event -> {
            label.setBorder(Border.EMPTY);
            setLabel(label, vrijeme);
        });
    }

}
