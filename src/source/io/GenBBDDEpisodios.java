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
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.EventoDeEpisodio;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;
import source.evento.SecuenciaSimple;

public final class GenBBDDEpisodios {
   private static final Logger LOGGER = Logger.getLogger(GenBBDDEpisodios.class.getName());

   private GenBBDDEpisodios(){

   }

   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios, String directory){
      if(directory==null){ return null; }
      BufferedReader dfin = null;
      try{
         dfin = new BufferedReader(new FileReader(directory+"/Secuencias.txt"));
         return parseFiles(tipos, episodios, directory);
      } catch(IOException e) {
         LOGGER.log(Level.WARNING, "Excepción al parsear el fichero de colección sintética", e);
      } finally {
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
   public static IColeccion parseFiles(BufferedReader dfin, List<String> tipos, List<Episodio> episodios){
      return parseFiles(dfin, tipos, null, null);
   }


   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios, String directory){
      return parseFiles(tipos, episodios, ocurrenciasEpisodios, directory, "/Secuencias.txt");
   }

   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios, String directory, String fileName){
      if(directory==null){
         return null;
      }
      BufferedReader dfin = null;
      try{
         dfin = new BufferedReader(new FileReader(directory+"/" + fileName));
         return parseFiles(dfin, tipos, episodios, ocurrenciasEpisodios);
      }catch(IOException e) {
         LOGGER.log(Level.WARNING, "Excepción al parsear el fichero de colección sintética", e);
      } finally {
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

   private static Evento parseEventoSinDuracion(int instante, String tipo, ISecuencia secuencia, List<String> tipos){
      Evento ev = new Evento(tipo,instante);
      secuencia.add(ev);
      // Insertar en los tipos
      int i;
      for(i=0;i<tipos.size();i++){
         if(tipos.get(i)==tipo){
            break;
         }
      }
      if(i==tipos.size()){ tipos.add(tipo); }
      return ev;
   }

   private static Evento parseEventoConDuracion(boolean saveInstances, String tipoInicio, String tipoFin, int instante, int duracion,
         ISecuencia secuencia, List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios){
      InstanciaEpisodio instancia = null;
      Evento ev;
      if(saveInstances){
         ev = new EventoDeEpisodio(tipoInicio,instante);
         instancia = new InstanciaEpisodio();
         instancia.setInicio((EventoDeEpisodio)ev);
         ((EventoDeEpisodio)ev).setInstancia(instancia);
      }else{
         ev = new Evento(tipoInicio,instante);
      }
      secuencia.add(ev);
      // Insertar en tipos
      int i;
      for(i=0;i<tipos.size();i++){
         if(tipos.get(i)==ev.getTipo()){
            break;
         }
      }
      if(i==tipos.size()){
         tipos.add(ev.getTipo());
      }
      // Insertar en episodios
      for(i=0;i<episodios.size();i++){
         if(episodios.get(i).getTipoInicio()==ev.getTipo()){
            break;
         }
      }
      if(i==episodios.size()){
         episodios.add(new Episodio(tipoInicio,tipoFin));
      }
      if(saveInstances){
         ev = new EventoDeEpisodio(tipoFin,instante+duracion);
         ((EventoDeEpisodio)ev).setInstancia(instancia);
         instancia.setFin((EventoDeEpisodio)ev);
         ocurrenciasEpisodios.add(instancia);
      }else{
         ev = new Evento(tipoFin,instante+duracion);
      }
      secuencia.add(ev);
      // Insertar en tipos
      for(i=0;i<tipos.size();i++){
         if(tipos.get(i)==ev.getTipo()){
            break;
         }
      }
      if(i==tipos.size()){
         tipos.add(ev.getTipo());
      }
      return ev;
   }

   private static void parsearTodosTipos(BufferedReader dfin, boolean saveInstances, List<String> tipos,
         List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios, IColeccion coleccion) throws IOException{
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
         while((line!=null) && (!line.startsWith("ID-"))){
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
               token = tokenizer.nextToken(); // tipo,
               //System.out.println(token);
               int duracion = Integer.parseInt(tokenizer.nextToken()); // duracion>
               if(duracion==0){
                  String tipo = token.intern();
                  parseEventoSinDuracion(instante, tipo, secuencia, tipos);

               }else{
                  // Evento con duracion, insertar eventos inicio y fin
                  String tipoInicio = ("b"+token).intern(), tipoFin = ("f"+token).intern();
                  parseEventoConDuracion(saveInstances, tipoInicio, tipoFin, instante, duracion, secuencia, tipos, episodios, ocurrenciasEpisodios);
               }
            }
            // Fin de transacción
            line = dfin.readLine();
         }
         secuencia.sort();
         coleccion.add(secuencia);
      }
   }

   private static void parsearFiltrarTipos(BufferedReader dfin, boolean saveInstances, List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios, IColeccion coleccion) throws IOException{
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
         while((line!=null) && (!line.startsWith("ID-"))){
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
               token = tokenizer.nextToken(); // tipo,
               //System.out.println(token);
               int duracion = Integer.parseInt(tokenizer.nextToken()); // duracion>
               Evento ev;
               InstanciaEpisodio instancia = null;
               // Comprueba si el tipo está entre los interesantes

               if(duracion==0){
                  ev = new Evento(token,instante);
                  if(Collections.binarySearch(tipos, ev.getTipo())>=0){
                  //if(tipos.contains(ev.getTipo())){
                     secuencia.add(ev);
                  }
               }else{
                  // Evento con duracion, insertar eventos inicio y fin
                  String tipoInicio = ("b"+token).intern(), tipoFin = ("f"+token).intern();
                  if(saveInstances){
                     ev = new EventoDeEpisodio(tipoInicio,instante);
                     instancia = new InstanciaEpisodio();
                     instancia.setInicio((EventoDeEpisodio)ev);
                     ((EventoDeEpisodio)ev).setInstancia(instancia);
                  }else{
                     ev = new Evento(tipoInicio,instante);
                  }
                  if(Collections.binarySearch(tipos, ev.getTipo())>=0){
                     secuencia.add(ev);
                  }
                  if(saveInstances){
                     ev = new EventoDeEpisodio(tipoFin,instante+duracion);
                     instancia.setFin((EventoDeEpisodio)ev);
                     ((EventoDeEpisodio)ev).setInstancia(instancia);
                  }else{
                     ev = new Evento(tipoFin,instante+duracion);
                  }
                  if(Collections.binarySearch(tipos, ev.getTipo())>=0){
                     secuencia.add(ev);
                     if(saveInstances ){ ocurrenciasEpisodios.add(instancia);}
                  }

               }
            }
            // Fin de transacción
            line = dfin.readLine();
         }
         secuencia.sort();
         coleccion.add(secuencia);
      }
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
   public static IColeccion parseFiles( BufferedReader dfin, List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios){
      if(dfin==null || tipos == null){
         return null;
      }

      boolean saveInstances = episodios != null;

      if(saveInstances && episodios == null){
         return null;
      }

      try{

         IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>());
         boolean leerTodos = tipos.isEmpty(); // No se proporciona ningún tipo de interés, leer todos.
         if(leerTodos){
            parsearTodosTipos(dfin, leerTodos, tipos, episodios, ocurrenciasEpisodios, coleccion);
            Collections.sort(tipos);
         }else{
            Collections.sort(tipos);
            parsearFiltrarTipos(dfin, saveInstances, tipos, episodios, ocurrenciasEpisodios, coleccion);
         }
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
      Integer duracion;
      String type;
      for(ISecuencia seq : collection){
         bfr.write("ID-" + i++ + ":");
         for(Evento e:seq){
            //Los finales de episodios no se añaden
            if(e.getTipo().startsWith("f")){ continue; }
            if(e.getInstante() != lastInstant){
               lastInstant = e.getInstante();
               bfr.write("\n\tt=" + lastInstant + ":");
            }
            type = e.getTipo();
            duracion = 0;
            if(type.charAt(0)=='b'){
               type = type.substring(1);
               duracion = ((EventoDeEpisodio)e).getDuracion();
            }
            bfr.write("\t<" + type + "," + duracion  + ">");
         }
         bfr.write("\n");
      }
      bfr.flush();
   }
}