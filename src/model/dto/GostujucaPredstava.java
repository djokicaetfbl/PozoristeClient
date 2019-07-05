
package model.dto;


public class GostujucaPredstava {
    private Integer id;
    private String naziv,opis,tip,pisac,reziser,glumci;

    public GostujucaPredstava(Integer id,String naziv,String opis,String tip,String pisac,String reziser,String glumci){
        this.glumci=glumci;
        this.naziv=naziv;
        this.opis=opis;
        this.tip=tip;
        this.pisac=pisac;
        this.reziser=reziser;
        this.id=id;
    }
    public GostujucaPredstava(String naziv,String opis,String tip,String pisac,String reziser,String glumci){
        this.glumci=glumci;
        this.naziv=naziv;
        this.opis=opis;
        this.tip=tip;
        this.pisac=pisac;
        this.reziser=reziser;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getPisac() {
        return pisac;
    }

    public void setPisac(String pisac) {
        this.pisac = pisac;
    }

    public String getReziser() {
        return reziser;
    }

    public void setReziser(String reziser) {
        this.reziser = reziser;
    }

    public String getGlumci() {
        return glumci;
    }

    public void setGlumci(String glumci) {
        this.glumci = glumci;
    }
    
    public String toString(){
        return this.naziv +" ";
    }
    
    
    
}
