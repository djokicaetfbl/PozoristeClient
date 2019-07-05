/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdministrativniRadnik extends RadnikKojiKoristiSistem {

    private String hash = "";

    private String korisnickoIme = "";

    private String tipRadnika = "Administrator";
    
    public AdministrativniRadnik(){
        
    }

    public AdministrativniRadnik(String ime, String prezime, String jmb, boolean statusRadnika, String kontak
        ,String korisnickoIme, String hashLozinke, String tipKorisnika) {
        super(ime, prezime, jmb, statusRadnika, kontak);
        this.korisnickoIme = korisnickoIme;
        this.hash = hashLozinke;
        this.tipRadnika = tipKorisnika;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hashhh) {
        this.hash = hashSHA256(hashhh);
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public String getTipRadnika() {
        return tipRadnika;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public void setTipRadnika(String tipKorisnika) {
        this.tipRadnika = tipKorisnika;
    }

    public String hashSHA256(String value) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            //Logger.getLogger(RadnikDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] encodedhash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        String hash = bytesToHex(encodedhash);
        return hash;
    }

    private String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
