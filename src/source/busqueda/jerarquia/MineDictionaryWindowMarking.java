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
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Estrategia con window marking
 * Igual podría hacerse algo más específico que sea más eficiente
 * @author vanesa.graino
 *
 */
public class MineDictionaryWindowMarking extends MineDictionary{
   private static final String TIEMPOS_WM = "wm";
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryWindowMarking.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int[] numEventosBorrados; //número de eventos borrados en cada iteración por interval marking
   //protected long[] tiemposWM; //tiempo

   protected List<String> listaTipos;

   public MineDictionaryWindowMarking(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations,
            saveRemovedEvents, clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_WM);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      numEventosBorrados = new int[tSize];
      //tiemposWM = new long[tSize];
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

      LOGGER.info("tamActual=" + tamActual);
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
            verifyPatterns.nuevoEvento();
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
               verifyPatterns.procesarReceptores(evento, receptores, encontrados, tamActual);
            }
            verifyPatterns.comprobarAnotaciones(evento, encontrados, tamActual, listaTipos);
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_WM, tamActual-1, true);
            verifyPatterns.purgarEventosIntervalosNoActivos(evento, tamActual);
            //tiemposWM[tamActual-1] += System.currentTimeMillis()-inicio;
            registroT.tiempo(TIEMPOS_WM, tamActual-1, false);
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

      protected void nuevoEvento(){
         hayOcurrencia=false;
      }

      protected void actualizaUltimaOcurrencia(){
         hayOcurrencia = hayOcurrencia || antes!=despues;
      }

      /**
       * Añade el intervalo del evento en instante tmp si ha habido una ocurrencia del mismo.
       * @param tmp
       */
      protected void addIntervalo(int tmp){
         //if(ultimaIteracion) return;
         if(hayOcurrencia){
            int intervaloActual = tmp-windowSize;
            //if(intervaloActual[1] == tmp) System.out.println("ocurrencias.put(e, Arrays.asList(new int[]{" + intervaloActual[0] + ", " + intervaloActual[1] + "}));");

            // 'intervaloActual' contiene el intervalo más grande de eventos incluidos en una ocurrencia
            // Recorrer la lista de intervalos activos
            boolean algunoActualizado=false;
            //for(int[] intervalo : intervalosActivos){
            int[] intervalo;
            int x=intervalosActivos.size(), i=x-1;
            for(;i>=0;i--){
               intervalo = intervalosActivos.get(i);
               //Solapa el inicio del nuevo intervalo con un intervalo existente: situación más habitual
               if(intervaloActual>=intervalo[0] && intervaloActual<=intervalo[1]){
                  // Actualizar 'intervalo' para que incluya 'intervaloActual'
                  intervalo[1]=tmp;
                  algunoActualizado=true;
                  break;
               }
               if(intervaloActual>intervalo[1]){ break; }
            }
            for(int j=x-1; j>i;j--){ intervalosActivos.remove(j); }
            if(!algunoActualizado){
               // Ningún intervalo fue actualizado, añadir intervalo actual
               //intervaloActual[1] = tmp;
               int[] aux = new int[]{ intervaloActual, tmp};
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
         //if(ultimaIteracion) return 0;
         // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
         // no están en un intervalo de eventos utilizados
         //if(intervalosActivos.size()>2){ System.out.println("Mas de dso"); }
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
      public abstract void secuenciaVacia();
   }

   protected class VerifyPatternsTam2 extends AbstractVerifyPatterns {
      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual, List<String> tiposAmpliar){
         //No hay anotaciones
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam){
         for(IAsociacionTemporal modelo : receptores){
            antes = modelo.getSoporte();
            modelo.recibeEvento(sid, evento, savePatternInstances);
            despues = modelo.getSoporte();
            actualizaUltimaOcurrencia();
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_WM, tam-1, true);
         addIntervalo(evento.getInstante());
         //tiemposWM[tam-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_WM, tam-1, false);
      }

      @Override
      public void borrarEvento() {
         // No hay que hacer nada
      }
   }

   protected class VerifyPatternsTam3 extends AbstractVerifyPatterns {

      protected ListIterator<List<List<Patron>>> itActual = anotaciones.getActual().listIterator();
      protected ListIterator<List<Patron>> itVentanaActual;
      protected List<Patron> ventanaActual;

      @Override
      public void borrarEvento(){
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
      public void nuevoEvento(){
         super.nuevoEvento();
         ventanaActual = itVentanaActual.next();
      }

      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
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
            ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);

            antes = receptor.getSoporte();
            ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,encontrados);
            despues = receptor.getSoporte();

            actualizaUltimaOcurrencia();
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_WM, tam-1, true);
         addIntervalo(evento.getInstante());
         //tiemposWM[tam-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_WM, tam-1, false);
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
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados, int tam){
         for(IAsociacionTemporal aux : receptores){
            ((IAsociacionDiccionario)aux).actualizaVentana(sid, evento);
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
                  //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                  IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                  antes = posible.getSoporte();
                  posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  despues = posible.getSoporte();
                  actualizaUltimaOcurrencia();
                  //addIntervalo(evento.getInstante()); //movido fuera del bucle @vanesa
               }
            }
         }
         //long inicio = System.currentTimeMillis();
         registroT.tiempo(TIEMPOS_WM, tamActual-1, true);
         addIntervalo(evento.getInstante());//movido para aquí @vanesa
         //tiemposWM[tamActual-1] += System.currentTimeMillis() - inicio;
         registroT.tiempo(TIEMPOS_WM, tamActual-1, false);
         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }

   protected class VerifyPatternsGenericLastIteration extends VerifyPatternsGeneric {
      @Override
      protected void actualizaUltimaOcurrencia(){
         //En la ultima iteracion no merece calcular intervalos ya que no se usarán
      }

      @Override
      protected void addIntervalo(int tmp){
         //En la ultima iteracion no merece calcular intervalos ya que no se usarán
      }

      @Override
      public int purgarEventosIntervalosNoActivos(Evento evento, int tamActual){
         return 0;
      }
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposWM = registroT.getTiempos(TIEMPOS_WM);
      fwp.write("\nTiempos empleados por iteración en window marking:\n");
      for(int i=0;i<tiemposWM.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposWM[i]) + "\n");
      }

      fwp.write("\nEventos eliminados por window marking en cada iteración:\n");
      for(int i=0;i<numEventosBorrados.length;i++){
         fwp.write(nivel(i) + numberFormat(numEventosBorrados[i]) + "\n");
      }
   }


   public int[] getNumEventosBorrados() {
      return numEventosBorrados;
   }

   public long[] getTiemposWM() {
      return registroT.getTiempos(TIEMPOS_WM);
   }
}
