package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.SuperModelo;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Implementa HSTP utilizando un supermodelo que gestiona la ventana (inserta y
 * elimina los eventos) para todos los eventos. Los demás modelos utilizan la ventana
 * de este supermodelo (sólo los tipos que les interesan) y comprueban las ocurrencias
 * de sus respectivos patrones.
 *
 * TODO: si un tipo de evento deja de ser frecuente lo seguimos actualizando.
 * Implementar una lista de omitidos en SuperModelo y actualizarla desde aquí.
 *
 *
 * @author vanesa.graino
 *
 */
public class MineDictionarySuperModelo extends MineDictionary {
   private static final Logger LOGGER = Logger.getLogger(MineDictionarySuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /**
    * Es el único modelo que va a recibir eventos. Los demas
    * utilizarán sus estructuras de control.
    */
   protected SuperModelo supermodelo;

   {
      associationClassName = "ModeloDictionaryFinalEvent";
      //patternClassName = "PatronDictionaryFinalEvent";
   }

   public MineDictionarySuperModelo(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize) throws FactoryInstantiationException {
      super.inicializaEstructuras(tipos, actual, win, cSize);
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos,
         List<IAsociacionTemporal> modelosBase, int win, int cSize)
         throws FactoryInstantiationException {
      super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize);
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
   }


   /* Sólo cambia que se llama a la fábrica de asociaciones con el supermodelo como parámetro
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionary#crearModelo(java.util.List)
    */
   @Override
   protected IAsociacionTemporal crearModelo(String[] mod) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            mod, windowSize, getClustering(), supermodelo, numHilos);
   }

   /*
    * Sólo cambia que se llama a la fábrica de asociaciones con el supermodelo como parámetro
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineDictionary#crearModelo(java.util.List, java.util.List)
    */
   @Override
   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, windowSize, patrones,
            supermodelo, numHilos);
   }

   /**
   *
   * @param actual
   * @return
   * TODO igual se puede hacer mejor esto: mantenar una lista en SuperModelo y utilizarla por ejemplo.
   */
  public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
     //return anotaciones.posiblesTiposParaAmpliar(actual);
     int[] tam = supermodelo.getTam();
     String[] tipos = supermodelo.getTipos();
     tiposAmpliar.clear();
     for(int i=0; i< tam.length; i++){
        if(tam[i]>0){
           tiposAmpliar.add(tipos[i]);
        }
     }
     return tiposAmpliar;
  }

   //@Override
   protected void calcularSoporteTam2(IColeccion coleccion){
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         for(Evento evento : secuencia){
            supermodelo.actualizaVentana(sid, evento);
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               receptor.recibeEvento(sid, evento, savePatternInstances);
            }
         }
         sid++;
      }
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
            supermodelo.actualizaVentana(sid,evento);
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               //((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,ventanaActual);
            }
         }
         sid++;
      }
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
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            //long inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_VENTANA, tamActual-1, true);
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
      if(tamActual==2){
         calcularSoporteTam2(coleccion);
         return;
      }
      super.calcularSoporte(candidatas, coleccion);
   }

}
