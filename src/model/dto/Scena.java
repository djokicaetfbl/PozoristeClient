package model.dto;

/**
 *
 * @author Ognjen
 */
public class Scena {
    
    private Integer idScene;
    
    private String nazivScene;
    
    public Scena(Integer id,String naziv){
        this.nazivScene=naziv;
        this.idScene = id;
    }

    public Integer getIdScene() {
        return idScene;
    }

    public void setIdScene(Integer idScene) {
        this.idScene = idScene;
    }

    public String getNazivScene() {
        return nazivScene;
    }

    public void setNazivScene(String nazivScene) {
        this.nazivScene = nazivScene;
    }

    @Override
    public String toString() {
        return "Scena{" + "idScene=" + idScene + ", nazivScene=" + nazivScene + '}';
    }
    
    
    
}
