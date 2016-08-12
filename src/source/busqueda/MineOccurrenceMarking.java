package source.busqueda;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.ModeloOccurrenceMarking;
import source.modelo.clustering.IClustering;

/**
 * En esta versión cada evento tiene un nuevo atributo <usado> que se utiliza
 * de forma equivalente a la lista de mapas en la versión 1 (<MineOccurrenceMarkingV1>).
 * <esUsado> almacena el valor para el que se marca como utilizado en la iteración
 * actual un evento que se ha usado en la ocurrencia de un patrón.
 * @author vanesa.graino
 *
 */
public class MineOccurrenceMarking extends Mine {
   private static final Logger LOGGER = Logger.getLogger(MineOccurrenceMarking.class.getName());
   private static final String TIEMPOS_OM = "om";
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int[] numEventosBorrados; //número de eventos borrados en cada iteración por interval marking
   //protected long[] tiemposOM; //tiempo

   //Por cada secuencia tiene un hashmap
   protected transient boolean esUsado = true;

   {
      associationClassName = "ModeloOccurrenceMarking";
      patternClassName = "Patron";
   }

   public MineOccurrenceMarking(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_OM);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      numEventosBorrados = new int[tSize];
      //tiemposOM = new long[tSize];
   }

   /**
    * Versión modificada borra los eventos no utilizados cuando la iteración es 3 o mayor
    */
   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      final int tam = candidatas.get(0).size();
      if(tam < 3){
         super.calcularSoporte(candidatas, coleccion);
         return;
      }

      Evento evento;
      int borrados = 0, sid=0;
      ListIterator<Evento> itSecuencia;

      for(ISecuencia secuencia : coleccion){
         itSecuencia = secuencia.listIterator();
         //for(Evento ev : secuencia){
         while(itSecuencia.hasNext()){
            evento = itSecuencia.next();
            if(evento.isUsado() == esUsado){
               //Borrar evento
               itSecuencia.remove();
               borrados++;
               notificarEventoEliminado(evento, sid, tam);
            }else{
               List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
               for(IAsociacionTemporal receptor : receptores){
                  ((ModeloOccurrenceMarking)receptor).recibeEvento(sid,evento,savePatternInstances,esUsado);
               }
            }
         }
         sid++;
      }
      //tiemposOM[tam-1] = -1; //TODO registrar tiempos OM
      numEventosBorrados[tam-2] = borrados;
      LOGGER.fine("Borrados por OM: " + borrados);

      esUsado ^= true;
   }

   public int[] getNumEventosBorrados() {
      return numEventosBorrados;
   }

   public long[] getTiemposOM() {
      return registroT.getTiempos(TIEMPOS_OM);
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
