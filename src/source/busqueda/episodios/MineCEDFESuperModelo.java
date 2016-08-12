package source.busqueda.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.episodios.SuperModeloEpisodios;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Versión con SuperModelo y episodios
 * En las asociaciones de tamaño 2 no se está utilizando supermodelo.
 * @author vanesa.graino
 *
 */
public class MineCEDFESuperModelo extends MineCEDFE{
   private static final Logger LOGGER = Logger.getLogger(MineCEDFESuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   /*
    * Atributos propios
    */

   protected SuperModeloEpisodios supermodelo;

   {
      associationClassName = "ModeloDictionaryFinalEvent";
      //patternClassName = "PatronDictionaryFinalEvent";
   }

   public MineCEDFESuperModelo(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos,
         IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) {
      supermodelo = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), episodios, win);
      return super.calcularDistribuciones(tipos, coleccion, supmin, win, episodios);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) {
      supermodelo = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), episodios, win);
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win) throws ModelosBaseNoValidosException {
      supermodelo = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), Collections.<Episodio> emptyList(), win);
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win, List<Episodio> episodios)
         throws ModelosBaseNoValidosException {
      supermodelo = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), episodios, win);
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win,
            episodios);
   }


   /*
    * Sobreescrito para pasar a la fábrica de asociaciones el supermodelo como parámetro
    * En las asociaciones de tamaño 2 no se está utilizando
    * (non-Javadoc)
    */
   @Override
   protected IAsociacionTemporal crearModelo(String[] mod,
         List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(mod), genp.getAsociacionesBase());
      if(eps.isEmpty()){
         return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
               mod, windowSize, patrones, supermodelo, numHilos);
      }else{
         return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
               mod, windowSize, patrones, eps, supermodelo, numHilos);
      }
   }

   @Override
   public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return supermodelo.eventosActivos(tiposAmpliar);
   }

   @Override
   public List<String> posiblesTiposParaAmpliarNoEpisodios(List<Patron> actual, List<String> tiposAmpliar, Evento evento){
      return supermodelo.eventosActivosNoEpisodios(tiposAmpliar, evento);
   }

   @Override
   public List<Episodio> posiblesEpisodiosParaAmpliar(List<Episodio> episodiosAmpliar, Evento evento){
      return supermodelo.episodiosActivos(episodiosAmpliar, true);
   }

   /*
    * Los métodos de calculo de soporte se sobreescriben para utilizar el
    * supermodelo
    *
    */
   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones *sin* episodios ocurren en cada ventana.
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
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,ventanaActual);
            }
         }
         sid++;
      }
   }

   @Override
   protected void calcularSoporteTam4(IColeccion coleccion){
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      //List<String> listaTiposAmpliar = new ArrayList<String>();
      List<Episodio> listaEpisodiosAmpliar = new ArrayList<Episodio>();

      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            supermodelo.actualizaVentana(sid,evento);
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
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
                     ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,encontrados);
                  }
               }
            }
            // Se comprueban las anotaciones que haya de la iteración anterior
            // para guardar las que pueden extenderse a patrones incompletos de tamaño 4
            //listaTiposAmpliar = posiblesTiposParaAmpliar(ventanaActual, listaTiposAmpliar);
            listaEpisodiosAmpliar = posiblesEpisodiosParaAmpliar(listaEpisodiosAmpliar, evento);
            boolean conservar = false;
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               /*for(String tipo : listaTiposAmpliar){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     //if(posible==null){ continue; } // En rara ocasión se puede dar
                     //si no es episodio completo no se busca
                     if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                        //posible.recibeEvento(sid,evento,savePatternInstances,extensiones,encontrados);
                     }else{
                        conservar=true;
                     }
                  }
               }*/
               for(int i=0; !conservar && i<listaEpisodiosAmpliar.size(); i++){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(listaEpisodiosAmpliar.get(i).getTipoInicio());
                  if(extensiones!=null && !extensiones.isEmpty()){
                     conservar = true;
                  }
               }
               if(conservar){
                  // Añadir la vieja anotación a la lista de anotaciones aceptadas
                  //if(!encontrados.contains(patron)) encontrados.add(patron);
                  encontrados.add(patron);
               }
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
            encontrados.clear();
         }
         sid++;
      }
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      //List<String> listaTiposAmpliar = new ArrayList<String>();
      List<String> listaTiposAmpliarNoEpisodios = new ArrayList<String>();
      List<Episodio> listaEpisodiosAmpliar = new ArrayList<Episodio>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            supermodelo.actualizaVentana(sid,evento);
            //listaTiposAmpliar = posiblesTiposParaAmpliar(ventanaActual, listaTiposAmpliar);
            listaTiposAmpliarNoEpisodios = posiblesTiposParaAmpliarNoEpisodios(ventanaActual, listaTiposAmpliarNoEpisodios, evento);
            listaEpisodiosAmpliar = posiblesEpisodiosParaAmpliar(listaEpisodiosAmpliar, evento);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               // Comprobar de qué tipo de anotación se trata
               // Caso a: anotación hecha en la anterior iteración
               if(patron.getTipos().length == tamActual-1){
                  boolean conservar=false;
                  //for(String tipo : listaTiposAmpliar){
                  //Siempre van a ser asociaciones completas en este primer bucle
                  for(String tipo : listaTiposAmpliarNoEpisodios){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                     if(extensiones!=null && !extensiones.isEmpty()){
                        IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                        //if(posible==null){ continue; }
                        //if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                           posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                        //}else{
                        //  conservar=true;
                        //}
                     }
                  }
                  // En esta parte sólo pueden crearse asociaciones incompletas por lo que si existe
                  // alguna se guarda la anotación original de tamaño i-1
                  for(int i=0; !conservar && i<listaEpisodiosAmpliar.size(); i++){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(listaEpisodiosAmpliar.get(i).getTipoInicio());
                     if(extensiones!=null && !extensiones.isEmpty()){
                        conservar = true;
                     }
                  }
                  if(conservar){
                     // Añadir la vieja anotación a la lista de anotaciones aceptadas
                     encontrados.add(patron);
                  }
               }else{
                  // Caso b: anotación hecha hace 2 iteraciones
                  // Comprobación por episodios.
                  for(Episodio episodio : listaEpisodiosAmpliar){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(episodio.getTipoInicio());
                     if(extensiones!=null && !extensiones.isEmpty()){
                        for(PatronDictionaryFinalEvent intermedio : extensiones){
                           List<PatronDictionaryFinalEvent> ext = intermedio.getExtensiones(episodio.getTipoFin());
                           if(ext!=null && !ext.isEmpty()){
                              IAsociacionDiccionario posible = (IAsociacionDiccionario)ext.get(0).getAsociacion();
                              //if(posible==null){ continue; }
                              //Siempre van a ser completos!
                              //if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                                 posible.recibeEvento(sid,evento, savePatternInstances,ext,encontrados);
                              //}
                           }
                        }
                     }
                  }
               }//en caso b
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
   }

}
