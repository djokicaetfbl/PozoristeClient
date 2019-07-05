package model.dto;


public class Sjediste {
    
    private Integer identifikatorSale = null;
    
    private Integer redniBroj;
    
    public Sjediste(Integer redBroj){
        this.redniBroj=redBroj;
    }

    public Integer getIdentifikatorSale() {
        return identifikatorSale;
    }

    public void setIdentifikatorSale(Integer identifikatorSale) {
        this.identifikatorSale = identifikatorSale;
    }

    public Integer getRedniBroj() {
        return redniBroj;
    }

    public void setRedniBroj(Integer redniBroj) {
        this.redniBroj = redniBroj;
    }
    
    
    
}
