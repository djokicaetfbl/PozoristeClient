/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dto;

import java.util.logging.Logger;

/**
 *
 * @author Mina
 */
public class VrstaAngazmana {
    Integer id;
    String naziv;

    public VrstaAngazmana(Integer id, String naziv){
        this.id=id;
        this.naziv=naziv;
    }
    
    
    public void setId(Integer id) {
        this.id = id;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Integer getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }
}
