package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.jerarquia.ModeloDictionaryOccurrenceMarking;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Minería con diccionario y estrategia occurrence marking (los eventos que forman
 * parte de una ocurrencia en un iteración se marcan, y los no marcados se borran
 * en la siguiente iteración).
 *
 * @author vanesa.graino
 *
 */
public class MineDictionaryOccurrenceMarking extends MineDictionary{
   private static final String TIEMPOS_OM = "om";

   private static final Logger LOGGER = Logger.getLogger(MineDictionaryOccurrenceMarking.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int[] numEventosBorrados; //número de eventos borrados en cada iteración por interval marking
   //protected long[] tiemposOM; //tiempo

   //Por cada secuencia tiene un hashmap
   protected boolean esUsado = true;

   {
      associationClassName = "ModeloDictionaryOccurrenceMarking";
      //patternClassName = "PatronDictionaryFinalEvent";
   }

   public MineDictionaryOccurrenceMarking(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_OM);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      numEventosBorrados = new int[tSize];
      //tiemposOM = new long[tSize];
   }


   public int[] getNumEventosBorrados() {
      return numEventosBorrados;
   }

   public long[] getTiemposOM() {
      return registroT.getTiempos(TIEMPOS_OM);
   }

   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
   // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones ocurren en cada ventana.
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            ventanaActual.clear();
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               ((ModeloDictionaryOccurrenceMarking)receptor).actualizaVentana(sid, evento);
               ((ModeloDictionaryOccurrenceMarking)receptor).recibeEvento(sid,evento,savePatternInstances,lista,ventanaActual,esUsado);
            }
         }
         sid++;
      }
      esUsado ^= true;
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      // Después hay indicaciones de qué ocurre en la ventana y se borran
      // los eventos que la anterior iteración no pertenecieron a ninguna
      // ocurrencia de patrón. Para saber cuales son, tenemos que comparar
      // el valor de su campo esUsado con el atributo esUsado de esta clase.
      // Si son iguales, hay borrar el evento ya que no había formaba parte de
      // una ocurrencia.
      Evento evento;
      int borrados = 0;
      ListIterator<Evento> itSecuencia;

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
         itSecuencia = secuencia.listIterator();
         //for(Evento evento : secuencia){
         while(itSecuencia.hasNext()){
            evento = itSecuencia.next();
            List<Patron> ventanaActual = itVentanaActual.next();
            if(evento.isUsado() == esUsado){
               //Borrar evento
               notificarEventoEliminado(evento,sid,tamActual);
               itSecuencia.remove();
               itVentanaActual.remove();
               borrados++;
               continue;
            }

            //long inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, true);
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               ((ModeloDictionaryOccurrenceMarking)receptor).actualizaVentana(sid, evento);
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
                     //ModeloDictionaryOccurrenceMarking posible = (ModeloDictionaryOccurrenceMarking)nodo.getModelo();
                     ModeloDictionaryOccurrenceMarking posible = (ModeloDictionaryOccurrenceMarking)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento,savePatternInstances,extensiones,encontrados,esUsado);
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

      //tiemposOM[tamActual-1] = -1; //TODO registrar tiempos OM
      numEventosBorrados[tamActual-2] = borrados;
      LOGGER.info("Borrados por OM: " + borrados);

      esUsado ^= true;

      //tiemposAnotaciones[tamActual-1] = tiempoA;
      //tiemposVentana[tamActual-1] = tiempoV;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposOM = registroT.getTiempos(TIEMPOS_OM);
      fwp.write("\nTiempos empleados por iteración en occurrence marking:\n");
      for(int i=0;i<tiemposOM.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposOM[i]) + "\n");
      }

      fwp.write("\nEventos eliminados por occurrence marking en cada iteración:\n");
      for(int i=0;i<numEventosBorrados.length;i++){
         fwp.write(nivel(i) + numberFormat(numEventosBorrados[i]) + "\n");
      }
   }


}
