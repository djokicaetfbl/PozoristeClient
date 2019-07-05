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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.Igranje;
import model.dto.Radnik;
import model.dto.Repertoar;

import util.ProtocolMessages;

public class PregledSvihRepertoaraController implements Initializable {

    @FXML
    private Button bNazad;

    @FXML
    private TableColumn<Radnik, Date> datumColumn;

    public static ObservableList<Repertoar> repertoariObservableList = FXCollections.observableArrayList();

    @FXML
    private Button bDodajRepertoar;

    @FXML
    private TableView sviRepertoariTableView;
    @FXML
    private Button bIzmjeniRepertoar;

    public static boolean izmjenaRepertoara = false;

    public static Repertoar izabraniRepertoar = null;

    @FXML
    void IzmjeniRepertoarAction(ActionEvent event) {
        izmjenaRepertoara = true;
        ObservableList<Repertoar> izabranaVrsta, repertoariObservaleList;
        repertoariObservaleList = sviRepertoariTableView.getItems();
        izabranaVrsta = sviRepertoariTableView.getSelectionModel().getSelectedItems();
        izabraniRepertoar = (Repertoar) izabranaVrsta.get(0);
        if (izabraniRepertoar != null) {

            try {
                Parent adminController = FXMLLoader.load(getClass().getResource("/view/DodajRepertoar.fxml"));

                Scene dodajRadnikaScene = new Scene(adminController);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(dodajRadnikaScene);
                window.show();
            } catch (IOException ex) {
                Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            upozorenjeRepertoar();
            return;
        }
    }

    private void ubaciKoloneUTabeluRadnik(ObservableList repertoari) {
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
	        datumColumn = new TableColumn("Pregled svih repertoara");
	        datumColumn.setCellValueFactory(new PropertyValueFactory<>("mjesecIGodina"));
	
	        sviRepertoariTableView.setItems(repertoariObservableList);
	        sviRepertoariTableView.getColumns().addAll(datumColumn);
	
	        sviRepertoariTableView.setOnMouseClicked(e -> {
	            if (e.getClickCount() == 2 && e.getButton().compareTo(MouseButton.PRIMARY) == 0) {
	                final Repertoar zaPrikaz = (Repertoar) sviRepertoariTableView.getSelectionModel().getSelectedItem();
	                try {
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
	                    Optional<Repertoar> dobaviSvjeze = repertoarList.stream().filter(r -> r.getId() == zaPrikaz.getId()).findAny();
	                    if (dobaviSvjeze.isPresent()) {
	                        PregledRepertoaraController.incijalizacija(dobaviSvjeze.get());
	                    }
	                    Parent adminController = FXMLLoader.load(getClass().getResource("view/PregledRepertoara.fxml"));
	                    Scene pregledRepertoara = new Scene(adminController);
	                    Stage window = (Stage) sviRepertoariTableView.getScene().getWindow();
	                    window.setScene(pregledRepertoara);
	                    window.show();
	                } catch (IOException ex) {
	                    Logger.getLogger(PregledSvihRepertoaraController.class.getName()).log(Level.SEVERE, null, ex);
	                }
	            }
	        });
    	}catch(IOException e) 
    	{
    		e.printStackTrace();
    	}
    }

    @FXML
    void dodajRepertoaraAction(ActionEvent event) {
        izmjenaRepertoara = false;
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/DodajRepertoar.fxml"));

            Scene dodajRadnikaScene = new Scene(adminController);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(dodajRadnikaScene);
            window.show();
        } catch (IOException ex) {
            Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    void nazadNaAdminFormu(ActionEvent event) {
        try {
            Parent adminController = FXMLLoader.load(getClass().getResource("/view/Admin.fxml"));

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
	        repertoariObservableList.removeAll(repertoariObservableList);
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
	        repertoariObservableList.addAll(repertoarList);
	        ubaciKoloneUTabeluRadnik(repertoariObservableList);
	        datumColumn.prefWidthProperty().bind(sviRepertoariTableView.widthProperty().divide(1));
	    }catch(IOException e) {
	    	e.printStackTrace();
	    }
    }

    private void upozorenjeRepertoar() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greska prilikom izbora repertoara !");
        alert.setHeaderText(null);
        alert.setContentText("Izaberite repertoar");
        alert.showAndWait();
    }

}
