
package controller;
import java.io.*;

import java.net.InetAddress;
import java.net.Socket;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.Pozoriste;
import model.dto.*;
import util.ProtocolMessages;


public class IzvjestajProdatihKarataController {

    // File file = new File(System.getProperty("user.dir") + File.separator + "izvjestaj.pdf");
    private File file;
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

    public IzvjestajProdatihKarataController(File file) {
        File temp = null;
        try {
            temp = new File(file.getAbsolutePath() + File.separator + "Izvjestaj" + new SimpleDateFormat("yyyy:MM:dd_hh:mm:ss").format(Calendar.getInstance().getTime()).replace(".", "_").replace(":", "_") + ".pdf");
            if (!temp.exists()) {
                temp.createNewFile();
                this.file = temp;
            } else {
                this.file = temp;
            }
        } catch (Exception ex) {
            Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void metoda() {
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Fajl ne moze biti kreiran", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
            return;
        }

        if (file == null || !file.exists() || !file.renameTo(file)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Fajl je otvoren od strane drugog programa", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
            return;
        }
        if (file != null && file.exists()) {
            Document document = new Document();
            try {
                file.createNewFile();
                PdfWriter.getInstance(document, new FileOutputStream(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            document.open();
            addMetaData(document);
            try {
                addContent(document);
            } catch (DocumentException ex) {
                Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            document.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Fajl se ne moze sacuvati u folderu, molimo izaberite drugi folder", ButtonType.OK);
            alert.setTitle("Upozorenje");
            alert.setHeaderText("Upozorenje");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PregledKarataController.class.getResourceAsStream("/resursi/warning.png")));
            alert.showAndWait();
        }

    }

    public void addMetaData(Document document) {
        document.addTitle("Izvjestaj prodatih karata");
        document.addSubject("Using iText");
        document.addKeywords("PDF, Izvjestaj");
        document.addAuthor("Pozoriste");
        document.addCreator("Pozoriste");
    }

    public void addContent(Document document) throws DocumentException {
        try {
            // odredi adresu racunara sa kojim se povezujemo
            // (povezujemo se sa nasim racunarom)
            InetAddress addr = InetAddress.getByName(Pozoriste.HOST);
            // otvori socket prema drugom racunaru
            Socket sock = new Socket(addr, Pozoriste.PORT);
            // inicijalizuj ulazni stream
            DataInputStream in = new DataInputStream(
                    sock.getInputStream());
            // inicijalizuj izlazni stream
            DataOutputStream out = new DataOutputStream(
                    sock.getOutputStream());
            BaseFont baseFont = null;
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.EMBEDDED);
            } catch (IOException ex) {
                Logger.getLogger(IzvjestajProdatihKarataController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Font font = new Font(baseFont);

            // Paragraph subPara = new Paragraph("Subcategory 1    ŠŠŠŠ Đ ĐĐ Š Č Ć ŠŠŽŽ  đ ć ", font);

            out.writeUTF(ProtocolMessages.REPERTOARS.getMessage());
            String response = in.readUTF();

            List<Repertoar> repertoarList = new ArrayList<>();
            String[] repertoarLines = response.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
            for(int i=0; i<repertoarLines.length; i++) {
                String[] repertoarString=repertoarLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                if(i==0) {
                    java.util.Date datum=null;
                    try {
                        datum = new SimpleDateFormat("yyyy-MM-dd").parse(repertoarString[2]);
                    } catch (ParseException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    Date termin=new Date(datum.getTime());
                    Repertoar repertoar=new Repertoar(Integer.parseInt(repertoarString[1]), termin);
                    out.writeUTF(ProtocolMessages.GET_IGRANJA.getMessage()+repertoar.getId());
                    String igranjaResponse=in.readUTF();
                    if(igranjaResponse.startsWith(ProtocolMessages.GET_IGRANJA_RESPONSE.getMessage())) {
                        LinkedList<Igranje> igranjaList=new LinkedList<Igranje>();
                        String[] igranjaLines=igranjaResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                        for(int j=0; j<igranjaLines.length; j++) {
                            String[] igranjaString=igranjaLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                            if(j==0) {
                                //         			Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
                                java.util.Date datum1=null;
                                try {
                                    datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[1]);
                                } catch (ParseException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                Date termin1=new Date(datum1.getTime());
                                Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[2]), Integer.parseInt(igranjaString[3]),Integer.parseInt(igranjaString[4]), Integer.parseInt(igranjaString[5]));
                                igranjaList.add(igranje);
                            }
                            else {
                                java.util.Date datum1=null;
                                try {
                                    datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[0]);
                                } catch (ParseException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                Date termin1=new Date(datum1.getTime());
                                Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[1]), Integer.parseInt(igranjaString[2]),Integer.parseInt(igranjaString[3]), Integer.parseInt(igranjaString[4]));
                                igranjaList.add(igranje);
                            }
                        }
                        repertoar.setIgranja(igranjaList);
                    }
                    repertoarList.add(repertoar);
                }
                else {
                    java.util.Date datum=null;
                    try {
                        datum = new SimpleDateFormat("yyyy-MM-dd").parse(repertoarString[1]);
                    } catch (ParseException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    Date termin=new Date(datum.getTime());
                    Repertoar repertoar=new Repertoar(Integer.parseInt(repertoarString[0]), termin);
                    out.writeUTF(ProtocolMessages.GET_IGRANJA.getMessage()+repertoar.getId());
                    String igranjaResponse=in.readUTF();
                    if(igranjaResponse.startsWith(ProtocolMessages.GET_IGRANJA_RESPONSE.getMessage())) {
                        LinkedList<Igranje> igranjaList=new LinkedList<Igranje>();
                        String[] igranjaLines=igranjaResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                        for(int j=0; j<igranjaLines.length; j++) {
                            String[] igranjaString=igranjaLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                            if(j==0) {
                                //         			Igranje(Date termin, Integer idScene, Integer idPredstave, Integer idGostujucePredstave, Integer idRepertoara)
                                java.util.Date datum1=null;
                                try {
                                    datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[1]);
                                } catch (ParseException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                Date termin1=new Date(datum1.getTime());
                                Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[2]), Integer.parseInt(igranjaString[3]),Integer.parseInt(igranjaString[4]), Integer.parseInt(igranjaString[5]));
                                igranjaList.add(igranje);
                            }
                            else {
                                java.util.Date datum1=null;
                                try {
                                    datum1 = new SimpleDateFormat("yyyy-MM-dd").parse(igranjaString[0]);
                                } catch (ParseException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                                Date termin1=new Date(datum1.getTime());
                                Igranje igranje=new Igranje(termin1, Integer.parseInt(igranjaString[1]), Integer.parseInt(igranjaString[2]),Integer.parseInt(igranjaString[3]), Integer.parseInt(igranjaString[4]));
                                igranjaList.add(igranje);
                            }
                        }
                        repertoar.setIgranja(igranjaList);
                    }
                    repertoarList.add(repertoar);
                }
            }


            List<Repertoar> listaRepertoara = repertoarList.stream().sorted(Comparator.comparing(e -> e.getMjesecIGodina())).collect(Collectors.toList());

            out.writeUTF(ProtocolMessages.PREDSTAVE.getMessage());
            String responsePredstave = in.readUTF();
            List<Predstava> predstavaList = new LinkedList<>();
            if (responsePredstave.startsWith(ProtocolMessages.PREDSTAVE_RESPONSE.getMessage())) {
                String[] predstaveLines = responsePredstave.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < predstaveLines.length; i++) {
                    String[] predstavaString = predstaveLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    Predstava predstava = null;
                    if (i == 0) {
                        //Predstava(String naziv, String opis, String tip)
                        predstava = new Predstava(predstavaString[2], predstavaString[3], predstavaString[4]);
                        predstava.setId(Integer.parseInt(predstavaString[1]));
                        predstavaList.add(predstava);
                    } else {
                        predstava = new Predstava(predstavaString[1], predstavaString[2], predstavaString[3]);
                        predstava.setId(Integer.parseInt(predstavaString[0]));
                        predstavaList.add(predstava);
                    }
                }
            } else {
                System.out.println("Greska pri pristupu Serveru!");
            }
            List<Predstava> listaPredstava = predstavaList;

            out.writeUTF(ProtocolMessages.GOSTUJUCE_PREDSTAVE.getMessage());
            String gostujucePredstaveResponse = in.readUTF();
            List<GostujucaPredstava> gostujucaPredstavaList = new LinkedList<>();
            if (gostujucePredstaveResponse.startsWith(ProtocolMessages.GOSTUJUCE_PREDSTAVE_RESPONSE.getMessage())) {
                String[] predstaveLines = gostujucePredstaveResponse.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                for (int i = 0; i < predstaveLines.length; i++) {
                    String[] predstavaString = predstaveLines[i].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                    GostujucaPredstava predstava = null;
                    if (i == 0) {
//          GostujucaPredstava(rs.getInt("id"), rs.getString("naziv"), rs.getString("opis"), rs.getString("tip"), rs.getString("pisac"), rs.getString("reziser"), rs.getString("glumci"));
                        predstava = new GostujucaPredstava(Integer.parseInt(predstavaString[1]), predstavaString[2], predstavaString[3], predstavaString[4], predstavaString[5], predstavaString[6], predstavaString[7]);
                        gostujucaPredstavaList.add(predstava);
                    } else {
                        predstava = new GostujucaPredstava(Integer.parseInt(predstavaString[0]), predstavaString[1], predstavaString[2], predstavaString[3], predstavaString[4], predstavaString[5], predstavaString[6]);
                        gostujucaPredstavaList.add(predstava);
                    }
                }
            } else {
                System.out.println("Greska pri pristupu Serveru!");
            }
            List<GostujucaPredstava> gostujucePredstava = gostujucaPredstavaList;

            Font fontNaziva = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
            Paragraph nazivIzvjestaja = new Paragraph("STATISTIKA PRODATIH KARATA", fontNaziva);
            nazivIzvjestaja.setAlignment(Paragraph.ALIGN_CENTER);
            document.newPage();
            for (int i = 0; i < 15; i++) {
                document.add(new Paragraph(" "));
            }
            document.add(nazivIzvjestaja);

            PdfPTable table = new PdfPTable(5);//broj kolona imace 4 kolone

            // the cell object
            PdfPCell celija1 = new PdfPCell(new Phrase("Statistika prodatih karata po repertoarima", font));
            celija1.setColspan(5);
            celija1.setRowspan(1);
            celija1.setCalculatedHeight(10);
            celija1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija1.setVerticalAlignment(Element.ALIGN_CENTER);
            celija1.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(celija1);
            document.newPage();
            Consumer<Repertoar> r = p -> {

                for (Integer i = 0; i < p.getIgranja().size(); i++) {

                    Igranje igranje = p.getIgranja().get(i);
                    String naziv = "";
                    Optional<Predstava> optPredtsva = Optional.ofNullable(null);
                    Optional<GostujucaPredstava> optGostujuca = Optional.ofNullable(null);
                    List<Karta> kartaList = new ArrayList<>();
                    String response1="";
                    try{
                        out.writeUTF(ProtocolMessages.KARTE.getMessage());
                        response1=in.readUTF();
                    }catch (IOException ee){
                        ee.printStackTrace();
                    }
                    if(response1.startsWith(ProtocolMessages.KARTE_RESPONSE.getMessage())){
                        String[] karteLines=response1.split(ProtocolMessages.LINE_SEPARATOR.getMessage());
                        for(int j=0; j<karteLines.length; j++){
                            String[] kartaString=karteLines[j].split(ProtocolMessages.MESSAGE_SEPARATOR.getMessage());
                            if(j==0){
//                  Karta(Integer id,Integer brojReda,Integer brojSjedista,Date termin,Integer idScene)
                                Date termin = null;
                                try {
                                    java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[4]);
                                    termin = new Date(date1.getTime());
                                }catch (java.text.ParseException pe){
                                    pe.printStackTrace();
                                }


                                Karta karta=new Karta(Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]), Integer.parseInt(kartaString[3]),termin, Integer.parseInt(kartaString[5]));
                                kartaList.add(karta);
                            }
                            else {
                                Date termin = null;
                                try {
                                    java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(kartaString[3]);
                                    termin = new Date(date1.getTime());
                                }catch (java.text.ParseException pe){
                                    pe.printStackTrace();
                                }


                                Karta karta=new Karta(Integer.parseInt(kartaString[0]), Integer.parseInt(kartaString[1]), Integer.parseInt(kartaString[2]),termin, Integer.parseInt(kartaString[4]));
                                kartaList.add(karta);
                            }
                        }
                    }
                    else {
                        System.out.println("Lista karata nije dobijena sa servera!");
                    }
                    List<Karta> karte = kartaList.stream().filter(e -> e.getTermin().equals(igranje.getTermin()) && e.getIdScene() == igranje.getIdScene()).collect(Collectors.toList());
                    if (igranje.getIdPredstave() != 0) {
                        optPredtsva = listaPredstava.stream().filter(e -> e.getId() == igranje.getIdPredstave()).findFirst();
                        if (optPredtsva.isPresent()) {
                            naziv = optPredtsva.get().getNaziv();
                        }
                    } else if (igranje.getIdGostujucePredstave() != 0) {
                        optGostujuca = gostujucePredstava.stream().filter(e -> e.getId() == igranje.getIdGostujucePredstave()).findFirst();
                        if (optGostujuca.isPresent()) {
                            naziv = optGostujuca.get().getNaziv();
                        }
                    }
                    PdfPCell celija2 = new PdfPCell(new Phrase("Repertoar " + p.getMjesecIGodina().toString(), font));
                    celija2.setRowspan(1);
                    celija2.setCalculatedHeight(10);
                    celija2.setColspan(1);
                    celija2.setVerticalAlignment(Element.ALIGN_CENTER);
                    celija2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(celija2);

                    PdfPCell celija3 = new PdfPCell(new Phrase(naziv, font));
                    celija3.setRowspan(1);
                    celija3.setCalculatedHeight(10);
                    celija3.setColspan(1);
                    celija3.setVerticalAlignment(Element.ALIGN_CENTER);
                    celija3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(celija3);

                    String cijenaPojedinacno = "";
                    if (!karte.isEmpty()) {
                        cijenaPojedinacno = String.format("%.2f", karte.get(0).getIznos());
                    } else {
                        cijenaPojedinacno = "--";
                    }
                    PdfPCell celija4 = new PdfPCell(new Phrase(cijenaPojedinacno, font));
                    celija4.setRowspan(1);
                    celija4.setCalculatedHeight(10);
                    celija4.setColspan(1);
                    celija4.setVerticalAlignment(Element.ALIGN_CENTER);
                    celija4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(celija4);

                    Long brojProdatihKarata = new Long(0);
                    if (!karte.isEmpty()) {
                        brojProdatihKarata = karte.stream().count();
                    }
                    PdfPCell celija6 = new PdfPCell(new Phrase(brojProdatihKarata.toString(), font));
                    celija6.setRowspan(1);
                    celija6.setCalculatedHeight(10);
                    celija6.setColspan(1);
                    celija6.setVerticalAlignment(Element.ALIGN_CENTER);
                    celija6.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(celija6);

                    String cijenaUkupno = "";
                    if (!karte.isEmpty()) {
                        cijenaUkupno = String.format("%.2f", karte.stream().mapToDouble(e -> (double) e.getIznos()).sum());
                    } else {
                        cijenaUkupno = "--";
                    }
                    PdfPCell celija5 = new PdfPCell(new Phrase(cijenaUkupno, font));
                    celija5.setRowspan(1);
                    celija5.setCalculatedHeight(10);
                    celija5.setColspan(1);
                    celija5.setVerticalAlignment(Element.ALIGN_CENTER);
                    celija5.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(celija5);
                }
            };

            PdfPCell celija7 = new PdfPCell(new Phrase("Repertoar", font));
            celija7.setColspan(1);
            celija7.setRowspan(1);
            celija7.setCalculatedHeight(10);
            celija7.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija7.setVerticalAlignment(Element.ALIGN_CENTER);
            celija7.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(celija7);

            PdfPCell celija8 = new PdfPCell(new Phrase("Naziv predstave", font));
            celija8.setColspan(1);
            celija8.setRowspan(1);
            celija8.setCalculatedHeight(10);
            celija8.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija8.setVerticalAlignment(Element.ALIGN_CENTER);
            celija8.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(celija8);

            PdfPCell celija9 = new PdfPCell(new Phrase("Cijena karte", font));
            celija9.setColspan(1);
            celija9.setRowspan(1);
            celija9.setCalculatedHeight(10);
            celija9.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija9.setVerticalAlignment(Element.ALIGN_CENTER);
            celija9.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(celija9);

            PdfPCell celija10 = new PdfPCell(new Phrase("Broj prodatih karata", font));
            celija10.setColspan(1);
            celija10.setRowspan(1);
            celija10.setCalculatedHeight(10);
            celija10.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija10.setVerticalAlignment(Element.ALIGN_CENTER);
            celija10.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(celija10);

            PdfPCell celija11 = new PdfPCell(new Phrase("Ukupan iznos", font));
            celija11.setColspan(1);
            celija11.setRowspan(1);
            celija11.setCalculatedHeight(10);
            celija11.setBackgroundColor(BaseColor.LIGHT_GRAY);
            celija11.setVerticalAlignment(Element.ALIGN_CENTER);
            celija11.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(celija11);

            listaRepertoara.forEach(r);
            document.add(table);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
