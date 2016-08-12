package source.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.EventoDeEpisodio;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;
import source.evento.SecuenciaSimple;
import source.io.MalformedFileException;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
/**
 * GenericCSV para guardar/cargar colecciones con/sin episodios en un formato XML genérico.
 * @author vanesa.graino
 * Formato con episodios:
 *
 *   +============+============+=========+============+
 *   | SequenceId | EventType  | Instant | EpisodeId  |
 *   +============+============+=========+============+
 *   |    1       |     A      |    0    |     1      |
 *   +------------+------------+---------+------------+
 *   |    1       |     B      |    1    |     1      |
 *   +------------+------------+---------+------------+
 *   |    1       |     C      |    2    |     2      |
 *   +------------+------------+---------+------------+
 *   |    1       |     D      |    3    |     2      |
 *   +------------+------------+---------+------------+
 *
 * El formato SIN episodios es igual al anterior omitiendo la columna EpisodeId
 */
public class GenericEpisodesCSVReader {

   /*
    * Constants
    */
   private static final Logger LOGGER = Logger.getLogger(GenericEpisodesCSVReader.class.getName());

   private static final int COLUMN_SEQUENCE_ID = 0;
   private static final int COLUMN_EVENT_TYPE = 1;
   private static final int COLUMN_INSTANT = 2;
   private static final int COLUMN_EPISODE_ID = 3;

   private static final int COLUMN_EPISODE_BEGIN = 0;
   private static final int COLUMN_EPISODE_END = 1;

   private static final String NULL = "null";
   private static final Character DELIMITER = ',';

   private static final boolean skipHeaders = true;
   private static final boolean processHeaders = false;

   /*
    * Constructors
    */
   private GenericEpisodesCSVReader(){
      //Constructor oculto
   }

   /*
    * Methods
    */
   private static IColeccion parseWithEpisodes(CsvReader csv, List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias) throws IOException, MalformedFileException {
      IColeccion collection = new ColeccionSimple();

      Integer sequenceId, episodeId, instant;
      String eventType;
      InstanciaEpisodio ie;
      do{
         sequenceId = Integer.parseInt(csv.get(COLUMN_SEQUENCE_ID));
         episodeId = NULL.equalsIgnoreCase(csv.get(COLUMN_EPISODE_ID))? null : Integer.parseInt(csv.get(COLUMN_EPISODE_ID));
         eventType = csv.get(COLUMN_EVENT_TYPE).intern();
         instant = Integer.parseInt(csv.get(COLUMN_INSTANT));
         //Tipo evento
         if (!tipos.contains(eventType)) {
            tipos.add(eventType);
         }
         Evento e;
         if(episodeId == null){
            e = new Evento(eventType, instant);
         }else{
            EventoDeEpisodio ep = new EventoDeEpisodio(eventType, instant);
            e = ep;
            if (episodeId >= instancias.size()) {
               ie = new InstanciaEpisodio(ep, null);
               instancias.add(ie);
               if (episodeId >= instancias.size()) {
                  throw new MalformedFileException("el id de episodio no es valido");
               }
            } else {
               ie = instancias.get(episodeId);
               ie.setFin(ep);
               // check if its a valid episode instance
               if (!containsEpisodeInstance(ie, episodios)) {
                  checkEpisodeInstance(ie, episodios);
                  episodios.add(new Episodio(ie.getInicio().getTipo(), ie.getFin().getTipo()));
               }
            }
            ep.setInstancia(ie);
         }
         if (sequenceId >= collection.size()) {
            collection.add(new SecuenciaSimple());
            checkSequenceId(sequenceId, collection);
         }
         collection.get(sequenceId).add(e);

      }while(csv.readRecord());

      Collections.sort(tipos);

      for(ISecuencia seq : collection){
         seq.subtractOffsetTime();
      }

      return collection;
   }

   private static IColeccion parseWithoutEpisodes(CsvReader csv, List<String> tipos) throws IOException, MalformedFileException {
      IColeccion collection = new ColeccionSimple();

      Integer sequenceId, instant;
      String eventType;
      //csv.setComment('#');
      //csv.setSkipEmptyRecords(true);
      //csv.setUseComments(true);
      //csv.setDelimiter(',');
      do{
         sequenceId = Integer.parseInt(csv.get(COLUMN_SEQUENCE_ID));
         eventType = csv.get(COLUMN_EVENT_TYPE).intern();
         instant = Integer.parseInt(csv.get(COLUMN_INSTANT));
         //Tipo evento
         if (!tipos.contains(eventType)) {
            tipos.add(eventType);
         }
         Evento e = new Evento(eventType, instant);
         if (sequenceId >= collection.size()) {
            collection.add(new SecuenciaSimple());
            checkSequenceId(sequenceId, collection);
         }
         collection.get(sequenceId).add(e);
      } while(csv.readRecord());

      Collections.sort(tipos);
      for(ISecuencia seq : collection){
         seq.subtractOffsetTime();
      }
      return collection;
   }

   private static IColeccion parseWithEpisodesDefinition(CsvReader csv, List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias) throws NumberFormatException, IOException, MalformedFileException{
      IColeccion collection = new ColeccionSimple();


      List<InstanciaEpisodio> instanciasAbiertas = new ArrayList<InstanciaEpisodio>();
      List<String> tiposInicio = new ArrayList<String>(), tiposFin = new ArrayList<String>();
      for(Episodio ep : episodios){
         instanciasAbiertas.add(null);
         tiposInicio.add(ep.getTipoInicio());
         tiposFin.add(ep.getTipoFin());
      }

      Integer sequenceId, instant;
      String eventType;
      do{
         sequenceId = Integer.parseInt(csv.get(COLUMN_SEQUENCE_ID));
         eventType = csv.get(COLUMN_EVENT_TYPE).intern();
         instant = Integer.parseInt(csv.get(COLUMN_INSTANT));
         //Tipo evento
         Evento e;
         if (!tipos.contains(eventType)) {
            tipos.add(eventType);
         }
         if(tiposInicio.contains(eventType)){
            EventoDeEpisodio ee = new EventoDeEpisodio(eventType, instant);
            e = ee;
            int indice = tiposInicio.indexOf(eventType);
            if(instanciasAbiertas.get(indice) != null){
               //LOGGER.warning("Se inicia un episodio con " + e + " cuando otro no había acabado "
               //      + instanciasAbiertas.get(indice).getInicio() + " en la secuencia #" + sequenceId);
               LOGGER.warning("Seq. #" + sequenceId  + ". Evento de inicio " + e + ", con inicio abierto " + instanciasAbiertas.get(indice).getInicio());
            }
            InstanciaEpisodio ie = new InstanciaEpisodio(ee, null);
            instanciasAbiertas.set(indice, ie);
            instancias.add(ie);
            ee.setInstancia(ie);
         }else if(tiposFin.contains(eventType)){
            EventoDeEpisodio ee = new EventoDeEpisodio(eventType, instant);
            e = ee;
            int indice = tiposFin.indexOf(eventType);
            if(instanciasAbiertas.get(indice) == null){
               //LOGGER.warning("Se ha encontrado un fin de episodio " + e + " que no tiene inicio en la secuencia #" + sequenceId);
               LOGGER.warning("Seq. #" + sequenceId  + ". Evento de fin " + e + " sin inicio");
               InstanciaEpisodio ie = new InstanciaEpisodio(null, ee);
               instancias.add(ie);
               ee.setInstancia(ie);
            }else{
               InstanciaEpisodio ie = instanciasAbiertas.get(indice);
               ie.setFin(ee);
               ee.setInstancia(ie);
               instanciasAbiertas.set(indice,null);
            }
         }else{
            e = new Evento(eventType, instant);
         }

         if (sequenceId >= collection.size()) {
            collection.add(new SecuenciaSimple());
            checkSequenceId(sequenceId, collection);
         }
         collection.get(sequenceId).add(e);
      } while(csv.readRecord());

      Collections.sort(tipos);
      for(ISecuencia seq : collection){
         seq.subtractOffsetTime();
      }
      return collection;
   }

   public static IColeccion parse(List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias,
         CsvReader csv) throws IOException, MalformedFileException {
      csv.setComment('#');
      csv.setSkipEmptyRecords(true);
      csv.setUseComments(true);
      csv.setDelimiter(',');
      if(csv.readRecord()){
         int columns = csv.getColumnCount();
         if(columns == 4){
            return parseWithEpisodes(csv, tipos, episodios, instancias);
         }else if(columns == 3 && episodios != null && episodios.size()>0){
            return parseWithEpisodesDefinition(csv, tipos, episodios, instancias);
         }
         return parseWithoutEpisodes(csv, tipos);
      }
      return null;
   }

   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias, String directory,
         String fileName, boolean episodesFile) {
      try{
         if(episodesFile){
            String episodesFileName = fileName.substring(0, fileName.length()-".csv".length()) + "Episodios.csv";
            CsvReader csv = new CsvReader(new FileReader(new File(directory, episodesFileName)), DELIMITER);
            while(csv.readRecord()){
               String begin = csv.get(COLUMN_EPISODE_BEGIN).intern();
               String end = csv.get(COLUMN_EPISODE_END).intern();
               episodios.add(new Episodio(begin, end));
            }
            csv.close();
         }
         return parseFiles(tipos, episodios, instancias, new File(directory, fileName));
      }catch(IOException e){
         e.printStackTrace();
      } catch (MalformedFileException e) {
         e.printStackTrace();
      }
      return null;
   }

   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias, String directory, String fileName) throws MalformedFileException {
      return parseFiles(tipos, episodios, instancias, new File(directory, fileName));
   }

   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> instancias, File f) throws MalformedFileException {
      FileReader fr = null;
      try {
         fr = new FileReader(f);
         CsvReader csv = new CsvReader(fr, DELIMITER);
         if(!skipHeaders){
            if (csv.readHeaders()) {
               LOGGER.info("Headers read successfully");
               if(processHeaders){
                LOGGER.info("Headers: " + Arrays.asList(csv.getHeaders()));
               }
            }else {
               LOGGER.info("Headers did NOT read successfully");
            }
         }
         return parse(tipos, episodios, instancias, csv);
      } catch (IOException ioe) {
         LOGGER.log(Level.WARNING, "IOException parsing file: " + ioe.getLocalizedMessage(), ioe);
         return null;
      } finally {
         try {
            if (fr != null) {
               fr.close();
            }
         } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
         }
      }
   }

   public static boolean toFileWithEpisodes(IColeccion collection, List<InstanciaEpisodio> instanciasEpisodio, CsvWriter csv) throws IOException {
      int i = 0;
      EventoDeEpisodio ee = null;
      for (ISecuencia sequence : collection) {
         for (Evento e : sequence) {
            ee = (EventoDeEpisodio) e;
            csv.write(Integer.toString(i));
            csv.write(ee.getTipo());
            csv.write(Long.toString(ee.getInstante()));
            csv.write(Integer.toString(instanciasEpisodio.indexOf(ee.getInstancia())));
            csv.endRecord();
         }
         i++;
      }
      return true;
   }

   public static boolean toFileWithoutEpisodes(IColeccion coleccion, CsvWriter csv) throws IOException {
      int i = 0;
      for (ISecuencia sequence : coleccion) {
         for (Evento e : sequence) {
            csv.write(Integer.toString(i));
            csv.write(e.getTipo());
            csv.write(Long.toString(e.getInstante()));
            csv.endRecord();
         }
         i++;
      }
      return true;
   }

   /**
    * Comprueba que el id de secuencia no es mayor que el tamaño de la colección (si esto sucede
    * el fichero se ha saltado un identificador de secuencia probablemente)
    * @param sequenceId
    * @param collection
    * @throws MalformedFileException
    */
   private static void checkSequenceId(Integer sequenceId, IColeccion collection) throws MalformedFileException{
      if (sequenceId >= collection.size()) {
         throw new MalformedFileException("el id de secuencia no es valido");
      }
   }

   private static void checkEpisodeInstance(InstanciaEpisodio ie, List<Episodio> episodios) throws MalformedFileException{
      if (!isValidEpisodeInstance(ie, episodios)) {
         throw new MalformedFileException("la instancia de episodio no es valida");
      }
   }

   private static boolean isValidEpisodeInstance(InstanciaEpisodio ie, List<Episodio> episodios) {
      return !episodios.contains(new Episodio(ie.getFin().getTipo(), ie.getInicio().getTipo()));
   }

   private static boolean containsEpisodeInstance(InstanciaEpisodio ie, List<Episodio> episodios) {
      return episodios.contains(new Episodio(ie.getInicio().getTipo(), ie.getFin().getTipo()));
   }

}
