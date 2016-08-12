package source.busqueda.episodios;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.IMarcasIntervalos;
import source.modelo.clustering.IClustering;
import source.modelo.episodios.ModeloEpisodiosDFE;
import source.modelo.episodios.ModeloEpisodiosDFEIntervalMarking;
import source.modelo.jerarquia.ModeloDictionaryFinalEvent;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Estrategia de Interval Marking (IM) que consiste en borrar las secciones de las secuencias
 * en las que no hay patrones.
 * Sólo cambia el cálculo de soporte respecto MineCEDFE el cálculo de soporte.
 *
 * TODO: Esta clase no funciona correctamente!!! Idear la estrategia IM cuando hay episodios.
 * Hay que buscar las asociaciones incompletas?
 * @author vanesa.graino
 *
 */
public class MineCEDFEIntervalMarking extends MineCEDFE {
   private static final Logger LOGGER = Logger.getLogger(MineCEDFEIntervalMarking.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */

   {
      associationClassName = "ModeloEpisodiosDFEIntervalMarking";
   }

   /*
    * Constructores
    */

   public MineCEDFEIntervalMarking(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   /*
    * Métodos
    */

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatas.isEmpty()){
         anotaciones.guardarAnotaciones();
         return;
      }

      int tamActual = candidatas.get(0).size();
      if(tamActual == 1){
         super.calcularSoporte(candidatas, coleccion);
         return;
      }

      ListIterator<Evento> finalVentana;

      AbstractVerifyPatterns verifyPatterns = null;
      if(tamActual == 2){
         verifyPatterns = new VerifyPatternsTam2();
      }else if(tamActual == 3){
         verifyPatterns = new VerifyPatternsTam3();
      }else if(tamActual == 4){
         verifyPatterns = new VerifyPatternsTam4();
      }else{
         verifyPatterns = new VerifyPatternsGeneric();
      }

      // En la ultima iteracion no es necesario borrar eventos
      // no habrá otra iteración que tome ventaja de ello
      if(tamActual == listaTipos.size()){
         verifyPatterns.ultimaIteracion = true;
      }

      Evento evento;
      List<Patron> encontrados = new ArrayList<Patron>();

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
               verifyPatterns.procesarReceptores(evento, receptores, encontrados);
            }
            verifyPatterns.comprobarAnotaciones(evento, encontrados, tamActual);
            verifyPatterns.purgarEventosIntervalosNoActivos(evento, tamActual);
         }
      }
      if(tamActual>2){
         anotaciones.guardarAnotaciones();
      }
      imprimirEliminados(LOGGER, verifyPatterns.eliminados, verifyPatterns.restantes);
   }

   protected abstract class AbstractVerifyPatterns {
      protected boolean ultimaIteracion; //false
      protected int[] ultimaOcurrencia;
      protected int[] intervaloActual = {0,0};
      protected List<int[]> intervalosActivos;
      protected boolean hayOcurrencia;
      /**
       * Soporte antes y después de leer el evento para saber si se ha encontrado
       * una instancia de alguno de los patrones
       */
      protected int antes, despues;
      protected int eliminados, restantes;
      protected Evento bv;
      protected int sid = -1;
      protected ListIterator<Evento> inicioVentana;

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
      /*protected void actualizaUltimaOcurrencia(IMarcasIntervalos modelo, int tmp){
         if(antes!=despues){ // Se encontró alguna ocurrencia
            hayOcurrencia=true;
            ultimaOcurrencia = modelo.getUltimaEncontrada();
            //if(modelo.size()>2){
            //LOGGER.info("Nueva ocurrencia de " + modelo.getTipos() + ": "  + intervaloActual[0] + ", " + intervaloActual[1] );
            //}
            if(intervaloActual[0]>ultimaOcurrencia[0]){
               intervaloActual[0]=ultimaOcurrencia[0];
            }
         }
      }*/

      protected void actualizaUltimaOcurrencia(IMarcasIntervalos modelo){
         if(ultimaIteracion){ return; }
         if(antes!=despues){ // Se encontró alguna ocurrencia
            hayOcurrencia=true;
            ultimaOcurrencia = modelo.getUltimaEncontrada();

            //Opcion insegura
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
         if(ultimaIteracion){ return; }
         if(hayOcurrencia){
            // 'intervaloActual' contiene el intervalo más grande de eventos incluidos en una ocurrencia
            // Recorrer la lista de intervalos activos
            boolean algunoActualizado=false;
            for(int[] intervalo : intervalosActivos){
               if(intervaloActual[0]<=intervalo[0] && tmp>=intervalo[1]){
                  algunoActualizado=true;
                  intervalo[0]=intervaloActual[0];
                  intervalo[1]=tmp;
                  break;
               }
               // comprobar si 'intervaloActual' se solapa
               if(intervaloActual[0]>=intervalo[0] && intervaloActual[0]<=intervalo[1]){
                  // Actualizar 'intervalo' para que incluya 'intervaloActual'
                  intervalo[1]=tmp;
                  algunoActualizado=true;
                  break; // los intervalos de 'intervalosActivos' no deben solaparse
               }
            }
            if(!algunoActualizado){
               // Ningún intervalo fue actualizado, añadir intervalo actual
               intervaloActual[1] = tmp;
               int[] aux = new int[]{intervaloActual[0], tmp};
               intervalosActivos.add(aux);
               intervaloActual = new int[2];
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
         if(ultimaIteracion){ return 0; }
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
         while(iteradorIntervalosActivos.hasNext()){
            int[] intervalo = iteradorIntervalosActivos.next();
            if(intervalo[1]<bv.getInstante()){
               iteradorIntervalosActivos.remove();
            }
         }
         return eliminados;
      }

      public abstract void borrarEvento();

      public abstract void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados);
      public abstract void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual);
      public abstract void secuenciaVacia();
   }

   protected class VerifyPatternsTam2 extends AbstractVerifyPatterns {
      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía. Con tamaño 2 no se hace nada");
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual){
         //No hay anotaciones en las primeras iteraciones
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados){
         for(IAsociacionTemporal receptor : receptores){
            IMarcasIntervalos modelo = (IMarcasIntervalos) receptor;
            antes = modelo.getSoporte();
            modelo.recibeEvento(sid, evento, savePatternInstances);
            despues = modelo.getSoporte();
            actualizaUltimaOcurrencia(modelo);
         }
         addIntervalo(evento.getInstante());
      }

      @Override
      public void borrarEvento(){
         //No hay que hacer nada
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
      public void nuevoEvento(int tmp){
         super.nuevoEvento(tmp);
         ventanaActual = itVentanaActual.next();
      }

      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual){
         ventanaActual.clear();
         ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
         encontrados.clear();
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados){
         for(IAsociacionTemporal receptor : receptores){
            List<Patron> aux = receptor.getPatrones();
            List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
            for(Patron patron : aux){
               lista.add((PatronDictionaryFinalEvent)patron);
            }
            ((ModeloDictionaryFinalEvent)receptor).actualizaVentana(sid, evento);

            antes = receptor.getSoporte();
            ((ModeloEpisodiosDFEIntervalMarking)receptor).recibeEvento(sid,evento,isSavePatternInstances(),lista,encontrados);
            despues = receptor.getSoporte();
            actualizaUltimaOcurrencia((IMarcasIntervalos)receptor);
         }
         addIntervalo(evento.getInstante());
      }
   }

   protected class VerifyPatternsTam4 extends VerifyPatternsTam3 {

      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual){
         boolean conservar = false;
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            for(String tipo : listaTipos){
               List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
               if(extensiones != null && !extensiones.isEmpty()){
                  ModeloEpisodiosDFE posible = (ModeloEpisodiosDFE)extensiones.get(0).getAsociacion();
                  /*if(posible==null){ // En rara ocasión se puede dar //TODO como se pode dar isto? @vanesa
                     if(verbose){ LOGGER.info("Posible null"); }
                     continue;
                  }*/
                  //si no es episodio completo no se busca y se conserva para la siguiente iteración
                  if(posible.sonEpisodiosCompletos()){
                     antes = posible.getSoporte();
                     posible.recibeEvento(sid,evento,isSavePatternInstances(),extensiones,encontrados);
                     despues = posible.getSoporte();
                     actualizaUltimaOcurrencia((IMarcasIntervalos)posible);
                  } else{
                     //if(verbose){ LOGGER.info("Contiene episodio incompleto"); }
                     conservar=true;
                  }
               }
            }
            if(conservar){
               // Añadir la vieja anotación a la lista de anotaciones aceptadas
               encontrados.add(patron);
            }
         }
         addIntervalo(evento.getInstante());
         ventanaActual.clear();
         ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
         encontrados.clear();
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados){
         for(IAsociacionTemporal aux : receptores){
            ((ModeloDictionaryFinalEvent)aux).actualizaVentana(sid, evento);
         }

         // Se recorre la lista de la tabla hash
         for(IAsociacionTemporal receptor : receptores){
            //   Procesar únicamente aquellos candidatos que vienen con episodios completos,
            // y vienen por primera vez. (Si hay tipos de eventos que no provienen de episodios
            // en la anterior iteración se pudieron buscar adecuadamente.
            if(((IAsociacionConEpisodios)receptor).sonEpisodiosCompletos()){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               antes = receptor.getSoporte();
               ((ModeloDictionaryFinalEvent)receptor).recibeEvento(sid,evento,isSavePatternInstances(),lista,encontrados);
               despues = receptor.getSoporte();
               actualizaUltimaOcurrencia((IMarcasIntervalos)receptor);
            }
         }
         addIntervalo(evento.getInstante());
      }
   }

   protected class VerifyPatternsGeneric extends VerifyPatternsTam4 {//extends VerifyPatterns {
      @Override
      public void secuenciaVacia(){
         LOGGER.info("Secuencia vacía");
      }

      @Override
      public void procesarReceptores(Evento evento, List<IAsociacionTemporal> receptores, List<Patron> encontrados){
         for(IAsociacionTemporal aux : receptores){
            ((ModeloDictionaryFinalEvent)aux).actualizaVentana(sid, evento);
         }
      }

      @Override
      public void comprobarAnotaciones(Evento evento, List<Patron> encontrados, int tamActual){
         // Calcular las asociaciones temporales a comprobar para el evento actual
         for(Patron aux : ventanaActual){
            PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
            // Comprobar de qué tipo de anotación se trata
            // Caso a: anotación hecha en la anterior iteración
            if(patron.getTipos().length == tamActual-1){
               boolean conservar=false;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     ModeloEpisodiosDFE posible = (ModeloEpisodiosDFE)extensiones.get(0).getAsociacion();
                     //if(posible==null){ continue; }
                     if(posible.sonEpisodiosCompletos()){
                        antes = posible.getSoporte();
                        posible.recibeEvento(sid,evento,isSavePatternInstances(),extensiones,encontrados);
                        despues = posible.getSoporte();
                        actualizaUltimaOcurrencia((IMarcasIntervalos)posible);
                     }else{
                        conservar=true;
                     }
                  }
               }
               if(conservar){
                  // Añadir la vieja anotación a la lista de anotaciones aceptadas
                  encontrados.add(patron);
               }
            }else{
               // Caso b: anotación hecha hace 2 iteraciones
               // Comprobación por episodios.
               for(Episodio episodio : listaEpisodios){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(episodio.getTipoInicio());
                  if(extensiones!=null && !extensiones.isEmpty()){
                     for(PatronDictionaryFinalEvent intermedio : extensiones){
                        List<PatronDictionaryFinalEvent> ext = intermedio.getExtensiones(episodio.getTipoFin());
                        if(ext!=null && !ext.isEmpty()){
                           ModeloEpisodiosDFE posible = (ModeloEpisodiosDFE)ext.get(0).getAsociacion();
                           //if(posible==null){ continue; }
                           if(posible.sonEpisodiosCompletos()){
                              antes = posible.getSoporte();
                              posible.recibeEvento(sid,evento,isSavePatternInstances(),ext,encontrados);
                              despues = posible.getSoporte();
                              actualizaUltimaOcurrencia((IMarcasIntervalos)posible);
                           }/*else{
                              conservar=true;
                           }*/
                        }
                     }
                  }
               }
            }

         }
         addIntervalo(evento.getInstante());
         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }



}
