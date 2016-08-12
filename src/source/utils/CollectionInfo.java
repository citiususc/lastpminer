package source.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.CapsulaEjecucion;
import source.configuracion.ConfigurationParameters;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.EventoDeEpisodio;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.InstanciaEpisodio;

/**
 * Sirve para obtener información sobre bases de datos sintéticas generadas con GenBD.
 * Usage:
 * java -cp $HSTP_HOME/target/hstpminer.jar:$HSTP_HOME/lib/* source.utils.CollectionInfo [--sin|--sen] <path-to-collection>
 *
 *
 * @author vanesa.graino
 *
 */
public final class CollectionInfo {

   private CollectionInfo(){
      //Hide constructor
   }

   private static void printUsage(){
      System.out.println("source.utils.CollectionInfo [--sin|--sen] <path-to-collection>");
      System.out.println("--sin,--sen\n\tSi lo que queremos es la información sin relleno.");
   }

   private static void help(){
      System.out.println("source.CollectionInfo help para más info");
   }

   public static final void main(String[] args){
      boolean conRelleno = true;

      if(args.length == 1 && args[0].equalsIgnoreCase("help")){
         printUsage();
         System.exit(0);
      }

      //Necesita un argumento al menos, sino salir
      if(args.length < 1){
         System.err.println("Se necesita por lo menos un argumento. ");
         help();
         System.exit(-1);
      }else if(args.length > 1){
         List<String> argsList = new ArrayList<String>(Arrays.asList(args));
         conRelleno = !argsList.contains("--sin") && !argsList.contains("--sen");
      }
      System.out.println(Arrays.toString(args));
      String pathName = args[args.length-1];

      //Comprobar si existe el path y tiene el fichero de secuencias
      File path = new File(pathName);
      if(!path.exists()){
         System.err.println("No existe el directorio especificado: " + pathName);
         help();
         System.exit(-1);
      }

      String fileName = conRelleno? "Secuencias.txt" : "Secuencias-Sin-Rellenar.txt";
      File file = new File(path, fileName);
      if(!file.exists()){
         System.err.println("No existe el fichero " + fileName + " en el directorio especificado");
         help();
         System.exit(-1);
      }


      //Leer colección
      ConfigurationParameters params = new ConfigurationParameters();
      params.setInputPath(pathName + "/");
      params.setInputFileName(fileName);
      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      List<InstanciaEpisodio> ocurrenciasEpisodios = new ArrayList<InstanciaEpisodio>();
      IColeccion coleccion = CapsulaEjecucion.getCollection(params, tipos, episodios, ocurrenciasEpisodios, true);

      System.out.println(calcularEstadisticas(tipos, episodios, coleccion));

   }

   protected static CollectionInfoDTO calcularEstadisticas(List<String> tipos, List<Episodio> episodios, IColeccion coleccion){
      //Densidad
      int numEvs = 0;
      int tiempoTotal = 0;
      System.out.println("La colección se compone de " + (tipos.size()-episodios.size()*2) + " tipos de eventos (sin duración).");
      System.out.println("Con " + episodios.size() + " tipos de episodios.");
      System.out.println("Consiste en " + coleccion.size() + " secuencias.");
      //for (int i = 0; i < coleccion.size(); i++) {
      int numTransacciones = 0, totalDistancias = 0, totalTam=0;
      int maxDuracionEpisodio = 0, totalEpisodios = 0;
      double avgDuracionEpisodio = 0;
      for(ISecuencia secuencia : coleccion){
         int sSize = secuencia.size();
         numEvs += sSize;
         int tInicial = secuencia.get(0).getInstante();
         int tFinal = secuencia.get(sSize - 1).getInstante();
         tiempoTotal += tFinal - tInicial;
         //double densidad = (double) sSize / (tFinal - tInicial);
         //System.out.println("Secuencia " + i + " eventos: " + sSize + " - densidad: " + densidad);
         int transaccion = 1, tamTransaccion = 1;
         int ultimoInstante = tInicial; //Ultimo instante leido
         int distancias = 0,instanteAnterior = -1;


         if(secuencia.get(0) instanceof EventoDeEpisodio){
            // No se comprueba si es inico o final de episodio y todos se suman dos veces
            // no es un problema para calcular la media de duración ni el máximo.
            int epiDuracion = ((EventoDeEpisodio)secuencia.get(0)).getDuracion();
            if(maxDuracionEpisodio < epiDuracion){
               maxDuracionEpisodio = epiDuracion;
            }
            totalEpisodios++;
            avgDuracionEpisodio += epiDuracion;
         }
         for(Evento ev : secuencia.subList(1, secuencia.size())){
            if(ev.getInstante()!= ultimoInstante){
               tamTransaccion++;
               transaccion++;
               instanteAnterior = ultimoInstante;
               ultimoInstante = ev.getInstante();
               distancias += ultimoInstante - instanteAnterior;
               totalTam += tamTransaccion;
               tamTransaccion =0;
            }else{
               tamTransaccion++;
            }

            if(ev instanceof EventoDeEpisodio){
               // No se comprueba si es inico o final de episodio y todos se suman dos veces
               // no es un problema para calcular la media de duración ni el máximo.
               int epiDuracion = ((EventoDeEpisodio)ev).getDuracion();
               if(maxDuracionEpisodio < epiDuracion){
                  maxDuracionEpisodio = epiDuracion;
               }
               totalEpisodios++;
               avgDuracionEpisodio += epiDuracion;
            }
         }
         totalTam += tamTransaccion;//ultima transaccion
         numTransacciones += transaccion;
         totalDistancias += distancias;
      }
      avgDuracionEpisodio /= totalEpisodios;

      CollectionInfoDTO dto = new CollectionInfoDTO(numEvs, tiempoTotal, numTransacciones, totalTam, totalDistancias,
            numTransacciones, tipos.size(), episodios.size(), coleccion.size(), avgDuracionEpisodio, maxDuracionEpisodio);

      return dto;
   }


}
