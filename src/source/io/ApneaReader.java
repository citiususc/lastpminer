package source.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.EventoDeEpisodio;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;
import source.evento.SecuenciaSimple;

public final class ApneaReader {
   private static final Logger LOGGER = Logger.getLogger(ApneaReader.class.getName());

   private ApneaReader(){

   }

   /**
    * Sólo se guardan episodios completos en <ocurrenciasEpisodios>
    * @param tipos
    * @param episodios
    * @param ocurrenciasEpisodios
    * @param bfr
    * @return
    * @throws MalformedFileException
    */
   public static IColeccion parseFiles(List<String> tipos, List<Episodio> episodios,
         List<InstanciaEpisodio> ocurrenciasEpisodios, BufferedReader bfr) throws MalformedFileException{
      if(tipos == null){ return null; }
      boolean saveOcus = ocurrenciasEpisodios != null;

      //if(tipos==null || episodios==null || ocurrenciasEpisodios==null){ return null; }
      if(episodios == null && saveOcus){ return null; }

      StringTokenizer tkn;
      String cadena;
      ISecuencia secuencia = new SecuenciaSimple();
      ColeccionSimple coleccion = new ColeccionSimple();

      tipos.addAll(Arrays.asList("iA", "iD", "iF", "iT", "fA", "fD", "fF", "fT"));
      Collections.sort(tipos);

      if(episodios != null){
         episodios.addAll(Arrays.asList(new Episodio("iA", "fA"), new Episodio("iD", "fD"),
            new Episodio("iF", "fF"), new Episodio("iT", "fT")));
      }

      // Colisiones para asociar inicio y fin de episodios se resuelven FCFS.
      List<InstanciaEpisodio> f = new ArrayList<InstanciaEpisodio>();
      List<InstanciaEpisodio> t = new ArrayList<InstanciaEpisodio>();
      List<InstanciaEpisodio> a = new ArrayList<InstanciaEpisodio>();
      List<InstanciaEpisodio> d = new ArrayList<InstanciaEpisodio>();

//      Level level = Level.INFO;

      try{
         //Abre el fichero de datos y lo lee
         //Parsea los eventos del fichero de datos
         cadena = bfr.readLine();

         int tBase = 0;
//         int sid = -1;
         int maxDuracion = 0;

         EventoDeEpisodio ee;
         List<InstanciaEpisodio> l;
         InstanciaEpisodio ie;
         char letra;

         coleccion = new ColeccionSimple();

         while(cadena != null){
//            sid++;
//            LOGGER.log(level, "Secuencia #" + sid);
            tkn = new StringTokenizer(cadena,";");
            while(tkn != null && tkn.hasMoreTokens()){
               String str = tkn.nextToken();
               ee = new EventoDeEpisodio(str);
               ee.setInstante(ee.getInstante() + tBase);
               secuencia.add(ee);

               String tipo = ee.getTipo().intern();

               // Componer episodio
               letra = tipo.charAt(1);
               switch(letra){
                  case 'A':
                     l = a;
                     break;
                  case 'T':
                     l = t;
                     break;
                  case 'D':
                     l = d;
                     break;
                  case 'F':
                     l = f;
                     break;
                  default:
                     throw new MalformedFileException("Tipo no válido para la colección: " + tipo);
               }
               // inicio
               if(tipo.charAt(0) == 'i'){
                  ie = new InstanciaEpisodio();
                  ie.setInicio(ee);
                  ee.setInstancia(ie);
//                  if(!l.isEmpty()){
//                     LOGGER.log(level, "Se solapa episodio tipo " + letra + ":" + ie + ", había: " + l);
//                  }
                  l.add(ie);
               }
               // Fin de episodio
               else if(tipo.charAt(0) == 'f'){
                  if(l.isEmpty()){
//                     LOGGER.log(level, "Evento de fin sin evento de inicio: " + ee);
                     continue;

                  }
                  ie = l.remove(0);
                  ie.setFin(ee);
                  ee.setInstancia(ie);
//                  if(ee.getDuracion()>320){//if(ee.getDuracion()>120){
//                     LOGGER.log(level, "episodio largo: " + ie + " (" + ee.getDuracion() + ")");
//                  }
                  maxDuracion = Integer.max(maxDuracion, ee.getDuracion());
                  if(saveOcus){ ocurrenciasEpisodios.add(ie); }
               }else{
                  throw new MalformedFileException("Tipo no válido para la colección: " + tipo);
               }

            }
            //Informar de episodios sin cerrar

            //for(InstanciaEpisodio ie2 : new ArrayList<InstanciaEpisodio>() { { addAll(a); addAll(t); addAll(d); addAll(f); } }){
            a.addAll(t); a.addAll(d); a.addAll(f);
//            for(InstanciaEpisodio ie2 :  a){
//               LOGGER.log(level, "Episodio incompleto: " + ie2);
//            }
            a.clear();
            t.clear();
            d.clear();
            f.clear();
            secuencia.sort();
            coleccion.add(secuencia);
//          LOGGER.log(level, "sequence size: " + secuencia.size());

            secuencia = new SecuenciaSimple();//new ArrayList<Evento>();
            cadena = bfr.readLine();
         }
//         LOGGER.log(level, "maxima duracion episodios: " + maxDuracion);
      }catch(FileNotFoundException e){
         LOGGER.log(Level.SEVERE,"No se puede parsear el fichero de apnea porque no se encuentra", e);
      }catch(IOException e){
         LOGGER.log(Level.SEVERE,"Fallo de IO al parsear el fichero de SAHS", e);
      }catch(MalformedFileException mfe){
         LOGGER.log(Level.SEVERE,"El fichero no tiene el formato esperado por el parser de SAHS", mfe);
         throw mfe;
      }finally{
         try{
            if(bfr!=null){ bfr.close(); }
         }catch(IOException e){
            LOGGER.log(Level.WARNING,"Excepción cerrando el buffer cuando se parse el fichero de apnea", e);
         }
      }

      return coleccion;
   }


   /*
    * Para crear instancias de EventoDeEpisodio e InstanciaEpisodio
    */

   public static IColeccion parseFiles(List<String> tipos, ConfigurationParameters params) throws MalformedFileException{
      return parseFiles(params, tipos, null, null);
   }

   /*
    * Para crear instancias de EventoDeEpisodio e InstanciaEpisodio
    */
   public static IColeccion parseFiles(ConfigurationParameters params, List<String> tipos, List<Episodio> episodios, List<InstanciaEpisodio> ocurrenciasEpisodios) throws MalformedFileException{
      BufferedReader bfr = null;
      String entrada = params.getInputPath(ExecutionParameters.PROJECT_HOME, ExecutionParameters.PATH_SINTETICAS) + params.getInputFileName();
      try {
         //Abre el fichero de datos y lo lee
         //Parsea los eventos del fichero de datos

         bfr = new BufferedReader(new FileReader(entrada));
         return parseFiles(tipos, episodios, ocurrenciasEpisodios, bfr);
      }catch(FileNotFoundException e){
         LOGGER.log(Level.SEVERE,"No se puede parsear el fichero de apnea porque no se encuentra", e);
      }
      return null;
   }

   public static void writeFile(IColeccion collection, String fileName){
      BufferedWriter bfr = null;
      try{
         bfr = new BufferedWriter(new FileWriter(fileName));
         for(ISecuencia seq : collection){
            for(Evento e:seq){
               bfr.write(e + ";");
            }
            bfr.write("\n");
         }
         bfr.flush();
      }catch(FileNotFoundException e){
         LOGGER.log(Level.SEVERE,"No se encuentra el fichero en el que escribir la colección", e);
      } catch (IOException e) {
         LOGGER.log(Level.SEVERE,"Fallo de IO al escribir el fichero de SAHS", e);
      }finally{
         try{
            if(bfr != null){ bfr.close(); }
         }catch(IOException e){
            LOGGER.log(Level.WARNING,"Excepción cerrando el buffer cuando se parse el fichero de apnea", e);
         }
      }
   }
}