package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.Modelo;
import source.modelo.arbol.DictionaryUtils;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 * Incorpora la generación de asociaciones temporales candidatas en tiempo de cálculo de frecuencia.
 * Está fallando ya que no genera los mismos resultados que MineDictionary
 * @author vanesa.graino
 *
 */
public class MineDictionaryLazy3 extends MineDictionaryLazy2 {
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryLazy3.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected Map<String,List<IAsociacionTemporal>> mapaFuturas;
   protected List<Supernodo> nivelSiguiente;

   //{
   //   associationClassName = "ModeloDictionary";
   //   patternClassName = "PatronDictionaryFinalEvent";
   //}

   /*
    * Constructores
    */

   public MineDictionaryLazy3(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
   }

   /**
    * @param nuevasAsociaciones - las asociaciones calculadas en base a las anotaciones de un evento
    */
   @Override
   protected boolean insertarAsociacion(List<String> nuevaAsociacion){
      if(raizArbol == null){ return false; }

      List<String> padre = nuevaAsociacion.subList(0, nuevaAsociacion.size()-1);
      Nodo nodoPadre = raizArbol.obtenerNodoEnArbol(padre);
      if(nodoPadre == null){
         return false;
      }

      Supernodo supernodo = nodoPadre.getHijos(); //supernodo en el que vamos
      if(supernodo == null){ return false; }
      String tipoExtra = nuevaAsociacion.get(nuevaAsociacion.size()-1);
      Nodo nuevo = supernodo.getHijo(tipoExtra);
      if(nuevo == null){ return false; }
      Modelo m = (Modelo)nuevo.getModelo();
      m.incrementarSoporte();
      //LOGGER.info("La asociacion " + nuevaAsociacion + " tiene soporte " + m.getSoporte());

      return true;
   }


   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      minFreq = supmin;
      // Continuar normalmente
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   private void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      int i=0,j=0,l;
      String[] mod;
      IAsociacionTemporal[] asocBase = new IAsociacionTemporal[tam];
      IAsociacionTemporal modelo;
      List<Supernodo> nuevoNivelSiguiente = new ArrayList<Supernodo>();

      // Inicializar mapa
      Map<String,List<IAsociacionTemporal>> nuevoMapaFuturas = new HashMap<String,List<IAsociacionTemporal>>(tipos.size());
      for(String tipo : tipos){
         nuevoMapaFuturas.put(tipo, new ArrayList<IAsociacionTemporal>());
      }

      for(Supernodo supernodo : nuevoNivel){

         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            asocBase[0] = padre.getModelo();
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               asocBase[1] = madre.getModelo();
               String tipoNuevo = asocBase[1].getTipos()[tam-2];
               //mod = new ArrayList<String>(asocBase[0].getTipos());
               //mod.add(tipoNuevo);
               mod = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
               mod[mod.length-1] = tipoNuevo;

               boolean valido= DictionaryUtils.comprobarSubasociaciones(raizArbol, tam, asocBase, mod);

               if(!valido){ continue; }

               modelo = AssociationFactory.getInstance().getAssociationInstance("Modelo", mod, windowSize, numHilos);

               nuevoMapaFuturas.get(mod[0]).add(modelo);
               for(l=1;l<tam;l++){
                  if(mod[l] != mod[l-1]){
                     nuevoMapaFuturas.get(mod[l]).add(modelo);
                  }
               }

               // Añadir el Nodo al nuevo
               creaNodoFachada(modelo, hijos, tipoNuevo);
            }
            if(!hijos.getNodos().isEmpty()){
               nuevoNivelSiguiente.add(hijos);
            }
         }
      }

      mapaFuturas = nuevoMapaFuturas;
      nivelSiguiente = nuevoNivelSiguiente;
   }

   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      if(tam == 3){
         super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
         generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
         return;
      }
      //tam>3
      int i=0,j=0;
      IAsociacionTemporal modelo;
      String[] modArray;
      GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

      // Inicializar mapa
      resetMapas(tipos);

      Nodo nodoAux = null;

      for(Supernodo supernodo : nivelActual){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            genp.setPadre(padre.getModelo(), 0);
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               registroT.tiempoAsociaciones(tam-1, true);
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               genp.setPadre(madre.getModelo(), 1);
               modArray = genp.getModArray();

               boolean valido=true;
               nodoAux = raizArbol.obtenerNodoEnArbol(modArray);
               //valido = nodoAux != null && nodoAux.getModelo().getSoporte()>=minFreq;
               if(nodoAux == null || nodoAux.getModelo().getSoporte()<minFreq){
                  valido = false;
                  asociacionesDescartadasLazy[tam-1]++;
                  patronesDescartadosLazy[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
                  patronesNoGeneradosNivel[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
               }else{
                  // Comprobar que las subasociaciones temporales son frecuentes
                  valido = genp.comprobarSubasociaciones(raizArbol, modArray);
               }

               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               // Combinar los patrones
               List<Patron> patrones = genp.generarPatrones(modArray);

               // Construir el modelo si hay patrones
               registroT.tiempoModelo(tam-1, true);
               if(!patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas
                  modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize, patrones,
                        numHilos);

                  notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);

                  //setModeloPatrones(patrones, modelo);

                  //El nodo siempre existe en asociaciones de tamaño > 3
                  nodoAux.setModelo(modelo);
               }// else: No hay patrones candidatos: descartar modelo candidato actual
               registroT.tiempoModelo(tam-1, false);
            } // for j
            if(!hijos.getNodos().isEmpty()){
               nuevoNivel.add(hijos);
            }
         } // for i
      } //for supernodos

      LOGGER.fine("Descartados por lazy. Asociaciones: " + asociacionesDescartadasLazy[tam-1]
            + ", patrones: " + patronesDescartadosLazy[tam-1]);

      generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);

   }


}
