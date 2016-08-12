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
import source.excepciones.AlgoritmoException;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.clustering.IClustering;
import source.modelo.jerarquia.ModeloDictionaryIntervalMarking;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * @author vanesa.graino
 *
 */
public class MineDictionaryIntervalMarking extends MineDictionary{
   private static final String TIEMPOS_IM ="im";
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryIntervalMarking.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected List<String> listaTipos;
   protected int[] numEventosBorrados; //número de eventos borrados en cada iteración por interval marking
   //protected long[] tiemposIM; //tiempo
   protected boolean soloPares;//false

   {
      associationClassName = "ModeloDictionaryIntervalMarking";
   }

   public MineDictionaryIntervalMarking(String executionId, boolean savePatternInstances, boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean soloPares, boolean removePatterns){
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      this.soloPares = soloPares;
      registroT.addOtrosTiempos(TIEMPOS_IM);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      // TODO Auto-generated method stub
      super.iniciarContadores(tSize, cSize);
      numEventosBorrados = new int[tSize];
      //tiemposIM = new long[tSize];
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      this.listaTipos = tipos;
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int tamActual = candidatas.get(0).size();

      if(tamActual==1){
         super.calcularSoporte(candidatas, coleccion);
         return;
      }
      ListIterator<Evento> finalVentana;

      LOGGER.info("tamActual="+tamActual);
      AbstractVerifyPatterns verifyPatterns = null;
      if(tamActual == 2){
         verifyPatterns = new VerifyPatternsTam2();
      }else if(tamActual == 3){
         verifyPatterns = new VerifyPatternsTam3();
      }else if(tamActual == listaTipos.size()){ //No válido con tipos repetidos
         verifyPatterns = new VerifyPatternsGenericLastIteration();
      }else{
         verifyPatterns = new VerifyPatternsGeneric();
      }

      Evento evento;
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      //long inicio;
      for(ISecuencia secuencia : coleccion){
         if(secuencia.isEmpty()){
            verifyPatterns.secuenciaVacia();
            continue;
         }
         ISecuencia copia = secuencia.clone();
         finalVentana = copia.listIterator();
         verifyPatterns.nuevaSecuencia(secuencia);
         while(finalVentana.hasNext()){
            evento = finalVentana.next();
            verifyPatterns.nuevoEvento(evento.getInstante());
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
               verifyPatterns.procesarReceptores(evento, receptores, encontrados, tamActual);
            }
            verifyPatterns.comprobarAnotaciones(evento, encontrados, tamActual, listaTipos);
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_IM, tamActual-1, true);
            verifyPatterns.purgarEventosIntervalosNoActivos(evento, tamActual);
            //tiemposIM[tamActual-1] += System.currentTimeMillis()-inicio;
            registroT.tiempo(TIEMPOS_IM, tamActual-1, false);
         }
         numEventosBorrados[tamActual-1] += verifyPatterns.eliminados;
      }
      if(tamActual>2){
         anotaciones.guardarAnotaciones();
      }
      imprimirEliminados(LOGGER, numEventosBorrados[tamActual-1], verifyPatterns.restantes);

   }

   @Override
   protected List<String> purgarTiposYEventos(IColeccion coleccion, List<IAsociacionTemporal> actual, List<String> tipos, int tSize){
      super.purgarTiposYEventos(coleccion, actual, tipos, tSize);
      listaTipos = tipos;
      return tipos;
   }

   protected abstract class AbstractVerifyPatterns {
      protected int[] intervaloActual = {0,0};
      protected List<int[]> intervalosActivos;
      protected boolean hayOcurrencia;
      protected int antes, despues;
      protected int eliminados, restantes; //eliminados después de procesar una secuencia y restantes totales de la colección
      protected Evento bv;// Evento de principio de ventana
      protected int sid = -1;
      protected ListIterator<Evento> inicioVentana;
      protected int lastTmp = -1; //para ahorrar comparaciones

      protected void nuevaSecuencia(ISecuencia secuencia){
         inicioVentana = secuencia.listIterator();
         bv = inicioVentana.next(); // Evento de principio de ventana
         eliminados = 0;
         restantes += secuencia.size();
         intervalosActivos = new ArrayList<int[]>();
         sid++;
      }

      protected void nuevoEvento(int tmp){
         hayOcurrencia=false;
         intervaloActual[0]=tmp;
         intervaloActual[1]=tmp;
      }

      protected void actualizaUltimaOcurrencia(IMarcasIntervalos modelo){
         if(antes!=despues){ // Se encontró alguna ocurrencia
            hayOcurrencia=true;
            int[] ultimaOcurrencia = modelo.getUltimaEncontrada();
            if(intervaloActual[0]>ultimaOcurrencia[0]){
               intervaloActual[0]=ultimaOcurrencia[0];
            }
         }
      }

      /**
       * Añade el intervalo del evento en instante tmp si ha habido una ocurrencia del mismo.
       * @param tmp
       */
      protected void addIntervalo(int tmp){
         if(hayOcurrencia){
            // 'intervaloActual' contiene el intervalo más grande de eventos incluidos en una ocurrencia
            // Recorrer la lista de intervalos activos
            boolean algunoActualizado=false;
            int[] intervalo;
            int x=intervalosActivos.size(), i=x-1;
            for(;i>=0;i--){
               intervalo = intervalosActivos.get(i);
               //Solapa el inicio del nuevo intervalo con un intervalo existente: situación más habitual
               if(intervaloActual[0]>=intervalo[0] && intervaloActual[0]<=intervalo[1]){
                  // Actualizar 'intervalo' para que incluya 'intervaloActual'
                  intervalo[1]=tmp;
                  algunoActualizado=true;
                  break;
               }
               if(intervaloActual[0]>intervalo[1]){ break; }
            }
            for(int j=x-1; j>i;j--){ intervalosActivos.remove(j); }
            if(!algunoActualizado){
               // Ningún intervalo fue actualizado, añadir intervalo actual
               //intervaloActual[1] = tmp;
               int[] aux = new int[]{intervaloActual[0], tmp};
               intervalosActivos.add(aux);
               //intervaloActual = new int[2];
            }
         }
      }

      /**
       * Purga los eventos que salen de la ventana y no están en intervalos activos y
       * actualiza los intervalos para que solo permanezcan los intervalos activos en la ventana.
       * @param evento
       * @param tamActual
       * @return Devuelve la cantidad de eventos eliminados
       */
      public int purgarEventosIntervalosNoActivos(Evento evento, int tamActual){

         if(lastTmp==evento.getInstante()){ return 0; }
         // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
         // no están en un intervalo de eventos utilizados
         while(bv.getInstante()<=evento.getInstante()- windowSize - 1){
            // Comprobar si 'bv' pertenece a algún intervalo activo
            boolean estaActivo=false;
            for(int[] intervalo : intervalosActivos){
               int instante = bv.getInstante();
               if(instante>=intervalo[0] && instante<=intervalo[1]){
                  estaActivo=true;
                  break;
               }
            }
            if(!estaActivo){
               //if(VERBOSE)
               //logger.info("El evento (" + bv.getTipo() + ", " + bv.getInstante() + ") de la secuencia "
               // + sid + " se borra ");
               // No pertenece a ningún intervalo activo, eliminar
               notificarEventoEliminado(bv, sid, tamActual);
               borrarEvento();
               inicioVentana.remove();
               restantes--;
               eliminados++;
            }
            bv = inicioVentana.next();
         }

         Iterator<int[]> iteradorIntervalosActivos = intervalosActivos.iterator();
         int instanteVentana = bv.getInstante();
         while(iteradorIntervalosActivos.hasNext()){
            int[] intervalo = iteradorIntervalosActivos.next();
            if(intervalo[1]<instanteVentana){
               iteradorIntervalosActivos.remove();
            }else if(intervalo[1]>instanteVentana){
               break;
            }
         }
         lastTmp = evento.getInstante();
         return eliminados;
      }

      public abstract void borrarEvento();

      public abstract void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam);
      public abstract void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual, List<String> tiposAmpliar);
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
         sid++;
      }
   }

   protected class VerifyPatternsTam2 extends AbstractVerifyPatterns {

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual, List<String> listaTipos){
         //No hay anotaciones que comprobar porque estamos en la iteración 2
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam){
         for(IAsociacionTemporal receptor : receptores){
            IMarcasIntervalos modelo = (IMarcasIntervalos) receptor;
            antes = modelo.getSoporte();
            modelo.recibeEvento(sid, evento, savePatternInstances);
            despues = modelo.getSoporte();
            actualizaUltimaOcurrencia(modelo);
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_IM, tam-1, true);
         addIntervalo(evento.getInstante());
         //tiemposIM[tam-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_IM, tam-1, false);
      }

      @Override
      public void borrarEvento() {
         //nada que hacer
      }
   }

   protected class VerifyPatternsTam3 extends AbstractVerifyPatterns {

      protected ListIterator<List<List<Patron>>> itActual = anotaciones.getActual().listIterator();
      protected ListIterator<List<Patron>> itVentanaActual;
      protected List<Patron> ventanaActual;

      @Override
      public void borrarEvento(){
         //super.borrarEvento();
         //Indice del evento que se borra
         int indexEventoBorrado = inicioVentana.nextIndex()-1;
         //Guardar la posicion del iterador de la secuencia
         int indexEventoActual = itVentanaActual.nextIndex()-1;
         //System.out.println("index: " + indexEventoActual);
         //Borrar las anotaciones/posicion asociadas al evento que se borra
         anotaciones.getActual().get(sid).remove(indexEventoBorrado);
         //Restaurar el iterador de la secuencia
         itVentanaActual = anotaciones.getActual().get(sid).listIterator(indexEventoActual);
      }

      @Override
      public void nuevaSecuencia(ISecuencia secuencia){
         super.nuevaSecuencia(secuencia);
         itVentanaActual = itActual.next().listIterator();
      }

      @Override
      public void nuevoEvento(int tmp){
         super.nuevoEvento(tmp);
         ventanaActual = itVentanaActual.next();
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual, List<String> tiposAmpliar){
         ventanaActual.clear();
         ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
         encontrados.clear();
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam){
         for(IAsociacionTemporal receptor : receptores){
            List<Patron> aux = receptor.getPatrones();
            List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
            for(Patron patron : aux){
               lista.add((PatronDictionaryFinalEvent)patron);
            }
            ((ModeloDictionaryIntervalMarking)receptor).actualizaVentana(sid, evento);

            antes = receptor.getSoporte();
            ((ModeloDictionaryIntervalMarking)receptor).recibeEvento(sid,evento,isSavePatternInstances(),lista,encontrados);
            despues = receptor.getSoporte();
            actualizaUltimaOcurrencia((IMarcasIntervalos)receptor);
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_IM, tam-1, true);
         addIntervalo(evento.getInstante());
         //tiemposIM[tam-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_IM, tam-1, false);
      }
   }

   protected class VerifyPatternsGeneric extends VerifyPatternsTam3 {//extends VerifyPatterns {

      /*protected void actualizaUltimaOcurrencia(IMarcasIntervalos modelo, int tmp){
         if(ultimaIteracion) return;
         if(antes!=despues){ // Se encontró alguna ocurrencia
            hayOcurrencia=true;
            intervaloActual = modelo.getUltimaEncontrada();
            super.addIntervalo(tmp);
         }
      }
      protected void addIntervalo(int tmp){

      }*/

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam){
         for(IAsociacionTemporal aux : receptores){
            ((ModeloDictionaryIntervalMarking)aux).actualizaVentana(sid, evento);
         }
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual, List<String> tiposAmpliar){
         // Calcular las asociaciones temporales a comprobar para el evento actual
         tiposAmpliar = posiblesTiposParaAmpliar(ventanaActual, tiposAmpliar);
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            for(String tipo : tiposAmpliar){
               List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
               if(extensiones!=null && !extensiones.isEmpty()){
                  //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                  //ModeloDictionaryIntervalMarking posible = (ModeloDictionaryIntervalMarking)nodo.getModelo();
                  ModeloDictionaryIntervalMarking posible = (ModeloDictionaryIntervalMarking)extensiones.get(0).getAsociacion();
                  antes = posible.getSoporte();
                  posible.recibeEvento(sid,evento,isSavePatternInstances(),extensiones,encontrados);
                  despues = posible.getSoporte();
                  actualizaUltimaOcurrencia((IMarcasIntervalos)posible);
                  //addIntervalo(evento.getInstante()); //movido fuera del bucle @vanesa
               }
            }
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_IM, tamActual-1, true);
         addIntervalo(evento.getInstante());//movido para aquí @vanesa
         //tiemposIM[tamActual-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_IM, tamActual-1, false);
         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }

   protected class VerifyPatternsGenericLastIteration extends VerifyPatternsGeneric {
      protected void actualizaUltimaOcurrencia(IMarcasIntervalos modelo, int tmp){
         //En la última iteración no es necesario guardar nada ya que no va a utilizarse
      }

      protected void addIntervalo(int tmp){
         //En la última iteración no es necesario guardar nada ya que no va a utilizarse
      }
      public int purgarEventosIntervalosNoActivos(Evento evento, int tamActual){
         return 0;
      }
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposIM = registroT.getTiempos(TIEMPOS_IM);
      fwp.write("\nTiempos empleados por iteración en interval marking:\n");
      for(int i=0;i<tiemposIM.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposIM[i]) + "\n");
      }

      fwp.write("\nEventos eliminados por interval marking en cada iteración:\n");
      for(int i=0;i<numEventosBorrados.length;i++){
         fwp.write(nivel(i) + numberFormat(numEventosBorrados[i]) + "\n");
      }
   }

   public int[] getNumEventosBorrados() {
      return numEventosBorrados;
   }

   public long[] getTiemposIM() {
      return registroT.getTiempos(TIEMPOS_IM);
   }


}
