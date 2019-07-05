package model.dto;

import java.sql.Date;

public class Kreiranje {
    private Integer id,idPredstave,idRepertoara,idRadnik,idGostujucePredstave;
    private Date datum;

    public Integer getId() {
        return id;
    }

    public Integer getIdPredstave() {
        return idPredstave;
    }

    public Integer getIdRepertoara() {
        return idRepertoara;
    }

    public Integer getIdRadnik() {
        return idRadnik;
    }

    public Integer getIdGostujucePredstave() {
        return idGostujucePredstave;
    }

    public Date getDatum() {
        return datum;
    }

    public Kreiranje(Integer idPredstave, Integer idRepertoara, Integer idGostujucePredstave, Integer idRadnik) {
        this.idPredstave = idPredstave;
        this.idRepertoara = idRepertoara;
        this.idRadnik = idRadnik;
        this.idGostujucePredstave = idGostujucePredstave;
    }
    
}
