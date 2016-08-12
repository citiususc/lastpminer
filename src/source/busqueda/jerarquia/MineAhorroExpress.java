package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

//TODO
/**
 * Combinación de las estrategias ahorro {@link MineAhorro} y express {@link MineDictionaryExpress}
 * @author vanesa.graino
 *
 */
public class MineAhorroExpress extends MineAhorro{
   private static final String TIEMPOS_PURGA_EXTENSIONES = "purgaExt";
   private static final Logger LOGGER = Logger.getLogger(MineAhorroExpress.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   private int[] extensionesEvitadas;
   //private long[] tiemposPurgaExtensiones;

   public MineAhorroExpress(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_PURGA_EXTENSIONES);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      extensionesEvitadas = new int[tSize];
      //tiemposPurgaExtensiones = new long[tSize];
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual) {
      // Caso general.
      // Se actualizan las ventanas de los posibles receptores.
      //
      // Se tiene calculado qué asociaciones podrían ocurrir en cada
      // posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      // de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      // se podrían encontrar en la siguiente iteración en base a las encontradas.
      //long tiempoV=0, tiempoA=0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();

            List<Integer> patternIDs = new ArrayList<Integer>();
            for(Patron anotacion: ventanaActual){
               patternIDs.add(anotacion.getID());
            }
            //No hace falta ordenar porque ya se guardan ordenados
            //Collections.sort(patternIDs); //esto es necesario
            //long inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, true);
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
            }
            //tiempoV += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, false);
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_ANOTACIONES, tamActual-1, true);
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     extensiones = purgaAnotaciones(extensiones, patternIDs, tamActual);
                     if(extensiones.isEmpty()){ continue; }
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  }
               }
            }
            //tiempoA += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_ANOTACIONES, tamActual-1, false);

            boolean antesHabia = !encontrados.isEmpty();
            setAnotacionesEvento(encontrados, ventanaActual, evento.getTipo(),  evento.getInstante(),  tamActual);
            if(antesHabia && ventanaActual.isEmpty()){
               eventosSinAnotaciones[tamActual-1]++;
            }
         }
         sid++;
      }
      //tiemposAnotaciones[tamActual-1] = tiempoA;
      //tiemposVentana[tamActual-1] = tiempoV;
      LOGGER.info("Extensiones evitadas: " + extensionesEvitadas[tamActual-1]);
      LOGGER.info("Eventos que dejan de tener anotaciones: " + eventosSinAnotaciones[tamActual-1]);
   }

   /**
    * Copiado y pegado de {@link MineDictionaryExpress}
    */
   @Override
   protected List<PatronDictionaryFinalEvent> purgaAnotaciones(List<PatronDictionaryFinalEvent> aComprobar, List<Integer> patternIDs, int tam){
      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_PURGA_EXTENSIONES, tam-1, true);

      List<PatronDictionaryFinalEvent> aComprobarFinal = new ArrayList<PatronDictionaryFinalEvent>();
      for(PatronDictionaryFinalEvent candidato : aComprobar){
         List<PatronDictionaryFinalEvent> padres = ((PatronDictionaryFinalEvent)candidato).getPadres();
         int existen = 0;
         for(int i=0,x=padres.size();i<x && existen<(tam-1);i++){
            //if(patternIDs.contains(padres.get(i).getID())){
            if(Collections.binarySearch(patternIDs, padres.get(i).getID()) >= 0){
               existen++;
            }
         }
         if(existen == tam-1){
            aComprobarFinal.add(candidato);
         }else{
            extensionesEvitadas[tam-1]++;
         }
      }

      //tiemposPurgaExtensiones[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_PURGA_EXTENSIONES, tam-1, false);
      return aComprobarFinal;

   }


   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposPurgaExtensiones = registroT.getTiempos(TIEMPOS_PURGA_EXTENSIONES);
      fwp.write("\nTiempos empleados por iteración en purga de extensiones:\n");
      for(int i=0;i<tiemposPurgaExtensiones.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposPurgaExtensiones[i]) + "\n");
      }

      fwp.write("\nExtensiones evitadas en cada iteración:\n");
      for(int i=0;i<extensionesEvitadas.length;i++){
         fwp.write(nivel(i) + numberFormat(extensionesEvitadas[i]) + "\n");
      }
   }
}

