package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.IBusquedaAnotaciones;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 *
 *	....
 *
 * La parte de borrado de patrones de iteraciones anteriores se implementa
 * al final de {@code generarCandidatas}. Se mantiene una referencia en nivelAnterior
 * (el nivelActual de la iteración anterior) y se recorre cada modelo de esta
 * iteración anterior, y cada patrón del modelo borrando sus padres (para eliminar
 * las referencias de la jerarquía de patrones),después se purgan los propios modelos
 * aunque se mantienen por ser necesarios para el árbol.
 * En los resultados generales se encarga {@code Mine} de que sean sobreescritos.
 *
 * @author vanesa.graino
 *
 */
public class MineDictionary extends MineArbol implements IBusquedaAnotaciones {
   protected static final String TIEMPOS_VENTANA = "ventana";
   protected static final String TIEMPOS_ANOTACIONES ="anotaciones";
   private static final Logger LOGGER = Logger.getLogger(MineDictionary.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   protected Anotaciones anotaciones;

   //protected long[] tiemposVentana;
   //protected long[] tiemposAnotaciones;


   {
      associationClassName = "ModeloDictionary";
      patternClassName = "PatronDictionaryFinalEvent";
      //treeClassName = "Supernodo";
   }


   /*
    * Constructores
    */

   public MineDictionary(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.anotaciones = new Anotaciones(saveAllAnnotations);
      registroT.addOtrosTiempos(TIEMPOS_ANOTACIONES,TIEMPOS_VENTANA);
   }

   @Override
   protected void borrarPatrones(){
      for(Supernodo sn : nivelAnterior){
         for(Nodo n : sn.getListaNodos()){
            IAsociacionTemporal modelo = n.getModelo();
            for(Patron p : modelo.getPatrones()){
               ((PatronDictionaryFinalEvent)p).getPadres().clear();
            }
            modelo.getPatrones().clear();
         }
      }
   }

   /**
    *
    * @param actual
    * @return
    * XXX tiene que devolver los tipos ordenados?
    */
   public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return anotaciones.posiblesTiposParaAmpliar(actual, tiposAmpliar);
   }

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
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,ventanaActual);
            }
         }
         sid++;
      }
   }

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
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int tamActual = candidatas.get(0).size();
      if(tamActual<3){
         super.calcularSoporte(candidatas, coleccion);
         return;
      }else if(tamActual==3){
         calcularSoporteTam3(coleccion);
      }else{
         calcularSoporteGeneral(coleccion, tamActual);
      }
      anotaciones.guardarAnotaciones();
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      //tiemposAnotaciones = new long[tSize];
      //tiemposVentana = new long[tSize];
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      anotaciones.generarEstructuraAnotaciones(coleccion,1);
      // Continuar normalmente
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   /*
    * Se asume que los modelos tienen tamaño 2.
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin,
         int win) throws ModelosBaseNoValidosException {
      anotaciones.generarEstructuraAnotaciones(coleccion, 2);
      // El resto del comportamiento debería ser el mismo
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }


   public Iterator<List<Patron>> getActualIterator(int sid) {
      return anotaciones.getActual().get(sid).iterator();
   }

   @Override
   public Anotaciones getAnotaciones(){
      return anotaciones;
   }

   @Override
   public void setSaveAllAnnotations(boolean saveAllAnnotations) {
      anotaciones.setSaveAllAnnotations(saveAllAnnotations);
   }


}
