
package model.dto;

import java.util.Objects;


public class Radnik {

    private String ime;
    private String prezime;
    /*private String opisPosla; */
    private String jmb;
    private boolean statusRadnika;
    private String kontakt;
    protected String tipRadnika = "";
    protected int idRadnika;
    
    public Radnik(){}

    public Radnik(String ime, String prezime /*, String opisPosla */, String jmb, boolean statusRadnika, String kontak) {
        this.ime = ime;
        this.prezime = prezime;
       /* this.opisPosla = opisPosla; */
        this.jmb = jmb;
        this.statusRadnika = statusRadnika;
        this.kontakt = kontak;
    }

    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

   /* public String getOpisPosla() {
        return opisPosla;
    }*/

    public String getJmb() {
        return jmb;
    }

    public boolean isStatusRadnika() {
        return statusRadnika;
    }

    public String getKontakt() {
        return kontakt;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

   /* public void setOpisPosla(String opisPosla) {
        this.opisPosla = opisPosla;
    } */

    public void setJmb(String jmb) {
        this.jmb = jmb;
    }

    public void setStatusRadnika(boolean statusRadnika) {
        this.statusRadnika = statusRadnika;
    }
    public  boolean getStatusRadnika(){return statusRadnika;}
    public void setKontak(String kontak) {
        this.kontakt = kontak;
    }

    @Override
    public String toString() {
        return "Radnik{" + "ime=" + ime + ", prezime=" + prezime + ", jmb=" + jmb + ", statusRadnika=" + statusRadnika + ", kontakt=" + kontakt + ", tipRadnika=" + tipRadnika + ", idRadnika=" + idRadnika + '}';
    }    

    public String getTipRadnika() {
        return tipRadnika;
    }

    public void setTipRadnika(String tipRadnika) {
        this.tipRadnika = tipRadnika;
    }
    
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.jmb);
        return hash;
    }
    
    public void setIdRadnika(int id){
        this.idRadnika = id;
    }
    
    public int getIdRadnika(){
        return this.idRadnika;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Radnik other = (Radnik) obj;

        if (Objects.equals(this.jmb, other.jmb)) {
            return true;
        }
        return false;
    }
    
    
    
    

}
