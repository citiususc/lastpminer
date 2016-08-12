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

/**
 * Versión de express equivalente a {@link MineDictionaryExpress} utilizando
 * el SuperModelo que gestiona la ventana.
 */
public class MineDictionarySuperModeloExpress extends MineDictionarySuperModelo {
   protected static final String TIEMPOS_PURGA_EXTENSIONES = "purgaExtensiones";
   private static final Logger LOGGER = Logger.getLogger(MineDictionarySuperModeloExpress.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   protected int[] extensionesEvitadas;
   //protected long[] tiemposPurgaExtensiones;

   public MineDictionarySuperModeloExpress(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations,
            saveRemovedEvents, clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_PURGA_EXTENSIONES);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      extensionesEvitadas = new int[tSize];
      //tiemposPurgaExtensiones = new long[tSize];
   }

   /*
    * Se añade que se calcula la lista de IDS de lo patrones anotados en cada evento
    * y se purgan las asociaciones
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionarySuperModelo#calcularSoporteGeneral(java.util.List, int)
    */
   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      // Caso general. Se tiene calculado qué asociaciones podrían ocurrir en cada
      //posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      //de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      //se podrían encontrar en la siguiente iteración en base a las encontradas.
      //long tiempoV=0, tiempoA=0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            //long inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, true);
            List<Patron> ventanaActual = itVentanaActual.next();
            List<Integer> patternIDs = new ArrayList<Integer>();
            for(Patron anotacion: ventanaActual){
               patternIDs.add(anotacion.getID());
            }
            Collections.sort(patternIDs); //esto es necesario
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            //List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            //for(IAsociacionTemporal receptor : receptores){
            //   ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
            //}
            supermodelo.actualizaVentana(sid,evento);
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
                     extensiones = MineDictionaryExpress.purgaAnotaciones(extensiones, patternIDs,
                           tamActual, extensionesEvitadas, registroT /*tiemposPurgaExtensiones*/);
                     if(extensiones.isEmpty()){ continue; }
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  }
               }
            }
            //tiempoA += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_ANOTACIONES, tamActual-1, false);
            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
      //tiemposAnotaciones[tamActual-1] = tiempoA;
      //tiemposVentana[tamActual-1] = tiempoV;
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
