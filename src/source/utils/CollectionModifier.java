package source.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.CapsulaEjecucion;
import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;
import source.io.ApneaReader;
import source.io.GenBBDDEpisodios;
import source.io.MalformedFileException;

/**
 * Esta clase permite cargar una colección, modificarla y guardarla.
 * Las modificaciones incluyen borrar secuencias, añadir eventos, etc.
 * @author vanesa.graino
 *
 */
public final class CollectionModifier {

   private static final Logger LOGGER = Logger.getLogger(CollectionModifier.class.getName());

   //NOTE: keep exit at the end
   private static String[] menuOptions = {
      "Add event to collection",
      "Remove event from collection",
      "Remove sequence(s)",
      "Save to file",
      "Print collection info",
      "Exit"
   };

   private static void printMenu(PrintWriter writer){
      //PrintWriter writer = System.console().writer();
      writer.println("These are the menu options:");
      int i=1;
      for(String option: menuOptions){
         writer.println("\t" + i + ". " + option);
         i++;
      }
      writer.flush();
   }

   private static boolean printMessageRetriveYesOrNo(final PrintWriter writer, final BufferedReader reader,/*Scanner reader,*/ String message) throws IOException{
      String answer = printMessageRetrieveString(writer, reader, message + " [yes/NO]");
      if(answer.isEmpty()){
         answer = "no";
      }
      return "yes".equalsIgnoreCase(answer);
   }

   @SuppressWarnings("unused")
   private static long printMessageRetrieveLong(final PrintWriter writer, final BufferedReader reader,
         final String message) throws IOException{
      String value = printMessageRetrieveString(writer, reader, message, null);
      return Long.parseLong(value);
   }

   private static int printMessageRetrieveInt(final PrintWriter writer, final BufferedReader reader,
         final String message) throws IOException{
      String value = printMessageRetrieveString(writer, reader, message, null);
      return Integer.parseInt(value);
   }

   private static String printMessageRetrieveString(final PrintWriter writer, final BufferedReader reader,
         final String message) throws IOException{
      return printMessageRetrieveString(writer, reader, message, null);
   }

   private static String printMessageRetrieveString(final PrintWriter writer, final BufferedReader reader,
         final String message, final String defaultValue) throws IOException{
      writer.println(message + "\n>>");
      writer.flush();
      //String answer = reader.next();
      String answer = reader.readLine();
      if(defaultValue != null && answer.isEmpty()){
         answer = defaultValue;
      }
      return answer;
   }

   private static IColeccion getCollection(String collectionName, List<String> tipos,
         List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios){
      ConfigurationParameters params = new ConfigurationParameters();
      params.setCollection(collectionName);
      if(params.getCollection().startsWith("BD")){
         params.setInputPath(ExecutionParameters.PATH_SINTETICAS + "/" + params.getCollection());
         params.setInputFileName("");
      }else if(params.getCollection().startsWith("/")){
         params.setInputFileName(params.getCollection());
         params.setInputPath("");
         params.setCollection("FileCollection");
      }
      IColeccion coleccion = CapsulaEjecucion.getCollection(params, tipos, episodios, ocurrenciasEpisodios, false);
      //si es apnea ordenar
      if(ConfigurationParameters.APNEA_DB.equals(collectionName)){
         coleccion.sort();
      }
      return coleccion;
   }


   private static void processOption(PrintWriter writer, int a, BufferedReader reader, IColeccion collection,
         String collectionName, List<String> tipos) throws IOException{
      if(a<1 || a>menuOptions.length){
         writer.println("Not a valid option!");
         return;
      }
      writer.println("\nYou have chosen #" + a + ": " + menuOptions[a-1]);
      if(a == menuOptions.length){
         writer.println("See ya!");
         writer.flush();
         //reader.close();
         System.exit(0);
      }
      writer.flush();

      if(a == 1){//Nuevo evento
         int sid = printMessageRetrieveInt(writer, reader, "Sequence id:");
         writer.println("The sequence you have chosen has " + collection.get(sid).size() + " events.");
         int instant = printMessageRetrieveInt(writer, reader, "Event instant:");
         String eventType = printMessageRetrieveString(writer, reader, "Event type:");
         Evento e = new Evento(eventType, instant);
         if(printMessageRetriveYesOrNo(writer, reader, "Add the event " + e + " to the sequence #" + sid + "?" )){
            ISecuencia sequence = collection.get(sid);
            sequence.add(e);
            sequence.sort();
            writer.println("Event added successfully!");
         }else{
            writer.println("Addition aborted :(");
         }
      }else if(a == 2){//Delete an event
         int sid = printMessageRetrieveInt(writer, reader, "Sequence id:");
         int instant = printMessageRetrieveInt(writer, reader, "Event instant:");
         String eventType = printMessageRetrieveString(writer, reader, "Event type:");
         Evento e = new Evento(eventType, instant);
         if(printMessageRetriveYesOrNo(writer, reader, "Proceed removing the event " + e + " from the sequence #" + sid + "?" )){
            ISecuencia sequence = collection.get(sid);
            for(int i=sequence.size()-1; i>=0; i--){
               if(sequence.get(i).equals(e)){
                  sequence.remove(i);
                  writer.println("Event removed!");
                  return;
               }
            }
            writer.println("The sequence #" + sid + " does not have a " + e+ " event");
         }else{
            writer.println("Addition aborted :(");
         }
      }else if(a==3){ //Remove sequence
         String answer = printMessageRetrieveString(writer, reader, "Enter the sequence(s) to be erased (dash is allowed to specify ranges of sequences):");
         writer.println("Processing answer... " + answer);
         writer.flush();
         //Check regular expression
         try{
            List<Integer> toBeRemoved = indexesToBeRemoved(writer, answer, collection.size()-1);
            for(Integer index:toBeRemoved){
               collection.remove(index.intValue());
            }
            //print info
            processOption(writer,5,reader, collection, collectionName, tipos);
         }catch(NumberFormatException nfe){
            writer.println("You entered an invalid string. Only numbers separated by semicolons and dashed ranges are allowed. E.g.: 1,2,3,7-9");
         }
      }else if(a==4){//save
         String folderName = printMessageRetrieveString(writer, reader, "Enter folder path: [" + ExecutionParameters.PROJECT_HOME + "/output/modificadas]",
               ExecutionParameters.PROJECT_HOME + "/output/modificadas" );
         String fileName = printMessageRetrieveString(writer, reader, "Enter file name: [apnea4-166.txt-corrected]", "apnea4-166.txt-corrected");

         if(ConfigurationParameters.APNEA_DB.equalsIgnoreCase(collectionName)){
            ApneaReader.writeFile(collection, folderName + "/" + fileName);
         }else if (Arrays.asList("BD4", "BD5", "BD6", "BD7", "BDR56", "BDR57",
            "BDRoE6", "BDRoE9", "BDRoE11", "BDRoE15").contains(collectionName)){
            GenBBDDEpisodios.writeFile(collection, folderName + "/" + fileName);
            /*if(Arrays.asList("BD4", "BD5", "BD6", "BD7", "BDR56", "BDR57" ).contains(collectionName)){
               GenBBDDEpisodios.writeFile(collection, fileName);
            }else{
               GenBBDDReader.writeFile(collection, fileName);
            }*/
         }
      }else if(a==5){//Print collection info
         writer.println("Collection name: " + collectionName);
         writer.println("Number of sequences: " + collection.size());
         writer.println(tipos.size() + " event types: " + tipos);
      }
   }

   /*
    * Constructores
    */

   private CollectionModifier(){

   }

   /*
    * Métodos protegidos y publicos
    */

   public static List<Integer> indexesToBeRemoved(PrintWriter writer, String answer, int max) {
      StringTokenizer tokenizerDash, tokenizer = new StringTokenizer(answer,",");
      Set<Integer> toBeRemoved = new HashSet<Integer>();
      while(tokenizer.hasMoreTokens()){
         String aux = tokenizer.nextToken();
         writer.println("Aux: " + aux);
         tokenizerDash = new StringTokenizer(aux,"-");
         Integer inicio = Integer.parseInt(tokenizerDash.nextToken().trim());
         if(tokenizerDash.hasMoreTokens()){
            Integer fin = Integer.parseInt(tokenizerDash.nextToken().trim());
            if(fin>max){ fin=max; }
            for(int i=inicio;i<=fin;i++){
               toBeRemoved.add(i);
            }
         }else if(inicio<=max){
            toBeRemoved.add(inicio);
         }
      }
      List<Integer> toBeRemovedList = new ArrayList<Integer>(toBeRemoved);
      Collections.sort(toBeRemovedList);
      Collections.reverse(toBeRemovedList);
      return toBeRemovedList;
   }

   public static void main(String[] args){
      //PrintWriter writer = System.console().writer();
      PrintWriter writer = System.console() == null ? new PrintWriter(System.out) : System.console().writer();
      writer.println("Welcome to the collection modifier!!");
      writer.println("-------------");
      writer.flush();

      String collectionName = null;
      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      IColeccion collection = null;
      List<InstanciaEpisodio> ocurrenciasEpisodios = new ArrayList<InstanciaEpisodio>();
      BufferedReader reader = new BufferedReader( new InputStreamReader(System.in));
      while(collection==null){
         try {
            collectionName = printMessageRetrieveString(writer, reader,"\nFirst of all we need to load a collection so we can work with it. \nEnter collection name [apnea]:");
            if(collectionName.isEmpty()){
               collectionName = ConfigurationParameters.APNEA_DB;
            }
            if(Arrays.asList("apnea", "BD4", "BD5", "BD6", "BD7", "BDR56", "BDR57",
                  "BDRoE6", "BDRoE9", "BDRoE11", "BDRoE15").contains(collectionName)){
               collection = getCollection(collectionName, tipos, episodios, ocurrenciasEpisodios);
            }else if(collectionName.charAt(0) =='/'){
               BufferedReader bfr = new BufferedReader(new FileReader(collectionName));
               collection = ApneaReader.parseFiles(tipos, episodios, ocurrenciasEpisodios, bfr);
            }else{
               writer.println("The collection name is not valid :S");
               writer.flush();
            }
         } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al parsear la colección", e);
         } catch (MalformedFileException e) {
            LOGGER.log(Level.SEVERE, "Error al parsear la colección. El fichero no tiene el formato esperado", e);
         }
      }
      writer.println("Collection '" + collectionName + "' loaded.");

      int a;
      while(true){
         printMenu(writer);
         try{
            a = printMessageRetrieveInt(writer, reader, "Enter option [1-" + menuOptions.length + "]:");
            processOption(writer, a, reader, collection, collectionName, tipos);
         }catch(InputMismatchException e){
            LOGGER.log(Level.FINE, "El usuario ha introducido caracteres no válidos", e);
            writer.println("\nThe option must be an integer from 1 to " + menuOptions.length);
         } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al leer la entrada de usuario", e);
         }
         writer.println("\n\n");
      }
   }



}
