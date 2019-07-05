/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dto;

import java.sql.Date;

public class RezervisanoSjediste {

    private Integer idScene;
    private Integer brojSjedista;
    private Integer idRezervacije;
    private Date termin;
    
    
    public RezervisanoSjediste(Integer idScene,Integer brojSjedista,Integer idRezervacije,Date termin){
        this.idScene = idScene;
        this.brojSjedista = brojSjedista;
        this.idRezervacije = idRezervacije;
        this.termin = termin;
    }

    public Integer getIdScene() {
        return idScene;
    }

    public void setIdScene(Integer idScene) {
        this.idScene = idScene;
    }

    public Integer getBrojSjedista() {
        return brojSjedista;
    }

    public void setBrojSjedista(Integer brojSjedista) {
        this.brojSjedista = brojSjedista;
    }

    public Integer getIdRezervacije() {
        return idRezervacije;
    }

    public void setIdRezervacije(Integer idRezervacije) {
        this.idRezervacije = idRezervacije;
    }

    public Date getTermin() {
        return termin;
    }

    public void setTermin(Date termin) {
        this.termin = termin;
    }
    
    
    

}
