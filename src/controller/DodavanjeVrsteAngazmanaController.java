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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import static controller.DodavanjeAngazmanaController.vrste;

import model.dto.VrstaAngazmana;
import util.ProtocolMessages;

public class DodavanjeVrsteAngazmanaController implements Initializable {

    @FXML
    private Label labelVrstaAngazmana;

    @FXML
    private TextField textFieldNaziv;

    @FXML
    private Button buttonOk;

    
    
    @FXML
    void okAction(ActionEvent event) {
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
            out.writeUTF(ProtocolMessages.DODAJ_ANGAZMAN_VA.getMessage()+textFieldNaziv.getText());
            if(in.readUTF().startsWith(ProtocolMessages.DODAJ_ANGAZMAN_VA_OK.getMessage())){
                System.out.println("ANGAZMAN VA uspjesno dodan");
            }
            else {
                System.out.println("ANGAZMAN VA nije dodan");
            }
            //VrstaAngazmanaDAO.dodajAngazman(textFieldNaziv.getText());
            ((Stage) buttonOk.getScene().getWindow()).close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
}
