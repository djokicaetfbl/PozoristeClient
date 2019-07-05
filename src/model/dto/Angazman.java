package model.dto;

import java.sql.Date;

public class Angazman {
    String ime,prezime,vrstaAngazmana;
    Date datumOd,datumDo;

    
    public Angazman(String ime,String prezime,String vrstaAngazmana,Date datumOd,Date datumDo){
        this.ime=ime;
        this.prezime=prezime;
        this.vrstaAngazmana=vrstaAngazmana;
        this.datumDo=datumDo;
        this.datumOd=datumOd;
    }
    public void setIme(String ime) {
        this.ime = ime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public void setVrstaAngazmana(String vrstaAngazmana) {
        this.vrstaAngazmana = vrstaAngazmana;
    }

    public void setDatumOd(Date datumOd) {
        this.datumOd = datumOd;
    }

    public void setDatumDo(Date datumDo) {
        this.datumDo = datumDo;
    }

    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public String getVrstaAngazmana() {
        return vrstaAngazmana;
    }

    public Date getDatumOd() {
        return datumOd;
    }

    public Date getDatumDo() {
        return datumDo;
    }
    
    
}
