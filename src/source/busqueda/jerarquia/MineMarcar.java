package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronAnotaciones;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatronMarcado;

/**
 * Implementa la idea de Paulo de las anotaciones utilizando el arbol para decidir si se extiende o no una.
 * Necesita las anotaciones de eventos de la ventana para poder hacerlo así que necesita almacenarlas de
 * alguna manera.
 *
 * @author vanesa.graino
 *
 */
public class MineMarcar extends MineDictionary {
   private static final String TIEMPOS_PURGA_EXTENSIONES = "purgaExt";
   private static final Logger LOGGER = Logger.getLogger(MineMarcar.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   Observable observable = new Observable();
   Integer tamActual = null;
   /*
    * Atributos
    */

   private int[] extensionesEvitadas;
   //private long[] tiemposPurgaExtensiones;
   private int[] asociacionesEvitadas; //eventos evitados completamente

   {
      associationClassName = "ModeloDFEMarcadoPatron";
      patternClassName = "PatronMarcado";
   }

   /*
    * Constructores
    */

   public MineMarcar(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_PURGA_EXTENSIONES);
   }

   @Override
   protected void notificarPatronGeneradoConsistente(Patron p){
      if(tamActual>1) observable.addObserver((Observer)p);
   }

   /*
    * Métodos
    */

   @Override
   protected void iniciarContadores(int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      asociacionesEvitadas = new int[tSize];
      extensionesEvitadas = new int[tSize];
      //tiemposPurgaExtensiones = new long[tSize];
      tamActual=1;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      super.calcularSoporte(candidatas, coleccion);
      tamActual = candidatas.get(0).size();
      LOGGER.info("Extensiones evitadas en la iteracion: " + extensionesEvitadas[tamActual-1]);
      LOGGER.info("Asociaciones evitadas en la iteracion: " + asociacionesEvitadas[tamActual-1]);
      observable.notifyObservers(tamActual);

   }

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
      List<PatronDictionaryFinalEvent> aComprobar = new ArrayList<PatronDictionaryFinalEvent>();
      int sid = 0;

      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
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
                     purgeCandidates(extensiones, aComprobar, sid, evento, tamActual, encontrados);
                     if(aComprobar.isEmpty()){
                        asociacionesEvitadas[tamActual-1]++;
                        continue;
                     }
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)aComprobar.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento,isSavePatternInstances(),aComprobar,encontrados);
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


   protected void purgeCandidates(List<PatronDictionaryFinalEvent> aComprobar,
         List<PatronDictionaryFinalEvent> aComprobarFinal, int sid, Evento evento, int tamActual, List<Patron> encontrados){
      //List<PatronDictionaryFinalEvent> aComprobarFinal = new ArrayList<PatronDictionaryFinalEvent>();
      aComprobarFinal.clear();
      //todo
      String tipo = evento.getTipo();
      for(PatronDictionaryFinalEvent candidato : aComprobar){
         boolean valido = true, masEspecifico = ((PatronAnotaciones)candidato).esMasEspecifico();
         List<PatronDictionaryFinalEvent> padres = ((PatronDictionaryFinalEvent)candidato).getPadres();
         //PatronDictionaryFinalEvent padreEnVentana = null;
         //int inicioOcurrencia;
         for(PatronDictionaryFinalEvent padre : padres){
            if(Arrays.asList(padre.getTipos()).contains(tipo)){
               //Si tiene el patrón del evento tiene que estar anotado en él
               if(!((PatronMarcado)padre).enEvento(sid, evento, windowSize)){
                  valido = false;
                  break;
               }
            }else if(masEspecifico){
               //Si no lo tiene y es más específico buscamos en el resto de la ventana
               if(!((PatronMarcado)padre).enVentana(sid, evento, windowSize)){
                  valido = false;
                  break;
               }
            }
         }
         if(valido){
            aComprobarFinal.add(candidato);

            /*if(!masEspecifico){
               //No hace falta buscarlo hay que indicar que se ha encontrado
               if(!encontrados.contains(candidato)){
                  encontrados.add(candidato);
                  ((PatronMarcado)candidato).encontrado(sid, , evento.getInstante());
               }
            }else{
               aComprobarFinal.add(candidato);
            }*/
         }else{
            extensionesEvitadas[tamActual-1]++;
         }
      }
      //return aComprobarFinal;
   }


   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposPurgaExtensiones = registroT.getTiempos(TIEMPOS_PURGA_EXTENSIONES);
      fwp.write("\nTiempos empleados por iteración en purga de extensiones:");
      for(int i=0;i<tiemposPurgaExtensiones.length;i++){
         fwp.write(nivel(i) + " " + timeFormat(tiemposPurgaExtensiones[i]) + "ms." + "\n");
      }

      fwp.write("\n\nExtensiones evitadas en cada iteración");
      for(int i=0;i<extensionesEvitadas.length;i++){
         fwp.write(nivel(i) + " " + numberFormat(extensionesEvitadas[i]) + "\n");
      }
   }

}

