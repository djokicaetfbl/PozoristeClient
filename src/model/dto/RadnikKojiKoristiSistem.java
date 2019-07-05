/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dto;

/**
 *
 * @author djord
 */
public class RadnikKojiKoristiSistem extends Radnik{

    public RadnikKojiKoristiSistem() {
    }
    
    

    public RadnikKojiKoristiSistem(String ime, String prezime/*, String opisPosla*/, String jmb, boolean statusRadnika, String kontak) {
        super(ime, prezime/*, opisPosla*/, jmb, statusRadnika, kontak);
    }
    
}
