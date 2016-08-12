package source.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.ColeccionSimple;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;


public final class GenBBDDReader {
   private static final Logger LOGGER = Logger.getLogger(GenBBDDReader.class.getName());

   private GenBBDDReader(){
   }

   public static IColeccion parseFiles(List<String> tipos,
         String directory){
      if(directory==null){ throw new NullPointerException("directory no puede ser nulo"); }
      BufferedReader dfin = null;
      try{
         dfin = new BufferedReader(new FileReader(directory+"/Secuencias.txt"));
         return parseFiles(tipos, dfin);
      }catch(IOException e){
         LOGGER.log(Level.WARNING, "Excepción al parsear el fichero de colección sintética", e);
      }finally{
         if(dfin != null){
            try{
               dfin.close();
            }catch(IOException e){
               LOGGER.log(Level.WARNING, "", e);
            }
         }
      }
      return null;
   }

   /**
    * The file to be parsed must follow the template:
    * <File> ::= <Sequence_List>
    * <Sequence_List> ::= <Sequence>[<Sequence_List>]
    * <Sequence> ::= ID-<n>\n<Transaction_List>
    * <Transaction_List> ::= <Transaction>\n[<Transaction_List>]
    * <Transaction> ::= \t"t="<n>": "<Event_List>
    * <Event_List> ::= <Event>[\t<Event_List>]
    * <Event> ::= "<"<n>","<n>">"
    * <n> ::= Integer
    *
    * @param tipos List of event types to parse from files. Optionally, an empty list (not null). In this case all event types found in the data, ordered lexicographically, are inserted.
    * @param directory Path to the directory that contains the file 'Secuencias.txt'.
    * @return The list of event sequences contained in the file.
    */
   public static IColeccion parseFiles(List<String> tipos,
         BufferedReader dfin){
      if(tipos==null){ throw new NullPointerException("tipos no puede ser nulo"); }
      try{

         IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>());
         boolean leerTodos = tipos.isEmpty(); // No se proporciona ningún tipo de interés, leer todos.

         // Leer eventos restringidos a los tipos en 'tipos'
         String line = dfin.readLine();
         while(line!=null){
            // Leer identificador de secuencia
            if(!line.startsWith("ID-")){
               // Buscar el siguiente identificador de secuencia
               line = dfin.readLine();
               continue;
            }
            line = dfin.readLine();
            line = line.trim();
            ISecuencia secuencia = new SecuenciaSimple(new ArrayList<Evento>());
            // Leer las transacciones
            while(line!=null && !line.startsWith("ID-")){
               line = line.trim();
               StringTokenizer tokenizer = new StringTokenizer(line,"=:<,>");
               // Leer instante
               String token = tokenizer.nextToken(); // t=
               int instante = Integer.parseInt(tokenizer.nextToken()); // <n>:
               // Leer eventos
               //System.out.println(line);
               while(tokenizer.hasMoreTokens()){
                  token = tokenizer.nextToken(); // \t<
                  if(token.contains("NULL")){ break; }
                  token = tokenizer.nextToken().intern();
                  Evento ev = new Evento(token,instante);
                  // Comprueba si el tipo está entre los interesantes
                  if(leerTodos){
                     secuencia.add(ev);
                     int i;
                     for(i=0;i<tipos.size();i++){
                        if(tipos.get(i)==ev.getTipo()){
                           break;
                        }
                     }
                     if(i==tipos.size()){ tipos.add(ev.getTipo()); }
                  }else{
                     for(int i=0;i<tipos.size();i++){
                        if(tipos.get(i)==ev.getTipo()){
                           //ev.setInstante(ev.getInstante()+base);
                           secuencia.add(ev);
                           break;
                        }
                     }
                  }
               }
               // Fin de transacción
               line = dfin.readLine();
            }
            secuencia.sort();
            coleccion.add(secuencia);
         }

         Collections.sort(tipos);

         return coleccion;
      }catch(IOException e){
         LOGGER.log(Level.WARNING, "Excepción al parsear el fichero de colección sintética", e);
      }finally{
         if(dfin != null){
            try{
               dfin.close();
            }catch(IOException e){
               LOGGER.log(Level.WARNING, "", e);
            }
         }
      }
      return null;
   }


   public static void writeFile(IColeccion collection, String fileName){
      BufferedWriter bfr = null;
      try{
         bfr = new BufferedWriter(new FileWriter(fileName));
         writeFile(collection, bfr);
      }catch(FileNotFoundException e){
         LOGGER.log(Level.WARNING, "Excepción al escribir el fichero de colección sintética", e);
      } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Excepción al escribir el fichero de colección sintética", e);
      }finally{
         if(bfr != null){
            try{
               bfr.close();
            }catch(IOException e){
               LOGGER.log(Level.WARNING, "", e);
            }
         }
      }
   }

   public static void writeFile(IColeccion collection, BufferedWriter bfr) throws IOException{
      int i=1;
      int lastInstant = -1;
      Long duracion;
      String type;
      for(ISecuencia seq : collection){
         bfr.write("ID-" + i++ + ":");
         for(Evento e:seq){
            if(e.getInstante() != lastInstant){
               lastInstant = e.getInstante();
               bfr.write("\n\tt=" + lastInstant + ":");
            }
            type = e.getTipo();
            duracion = 0L;
            bfr.write("\t<" + type + "," + duracion  + ">");
         }
         bfr.write("\n");
      }
      bfr.flush();
   }
}