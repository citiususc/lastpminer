package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 * Versión con Ahorro en la que se utilizan los nodos generados
 * por generarCandidatosSiguientes en la iteración actual en lugar
 * de vovler a hacer el mismo trabajo. Se mantiene una lista de
 * supernodos <nivelSiguiente> para esto.
 * @author vanesa.graino
 *
 */
public class MineAhorroV4 extends MineAhorro {
   private static final Logger LOGGER = Logger.getLogger(MineAhorro.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /**
    * Lista de supernodos de la siguiente iteración en donde guarda
    * la función {@link #generarAsociacionesCandidatasSiguientes} los
    * supernodos que crea.
    */
   protected List<Supernodo> nivelSiguiente;


   public MineAhorroV4(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations,
            saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Sólo cambia respecto a la función que sobreescribe en que guarda los supernodos en una lista
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineAhorro#generarAsociacionesCandidatasSiguientes(java.util.List, int, java.util.List)
    */
   @Override
   protected void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam,
         List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      LOGGER.info("Asociaciones siguientes a tam " + (tam-1));
      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, true);
      int i,j,k;
      String[] modArray;
      IAsociacionTemporal[] asocBase = new IAsociacionTemporal[tam];
      IAsociacionTemporal modelo;
      List<Supernodo> nuevoNivelSiguiente = new ArrayList<Supernodo>();

      Nodo[] nodosBase = new Nodo[tam];
      String[] tiposAdded = new String[tam];

      for(Supernodo supernodo : nuevoNivel){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();

         for(i=0;i<nSize;i++){


            Nodo padre = nodos.get(i);
            //if(tam>3) System.out.println("Padre: " + Arrays.toString(padre.getModelo().getTipos()));
            nodosBase[0] = padre;
            asocBase[0] = padre.getModelo();
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               //if(tam>3) System.out.println("Madre: " + Arrays.toString(madre.getModelo().getTipos()));
               nodosBase[1] = madre;
               asocBase[1] = madre.getModelo();
               String tipoNuevo = asocBase[1].getTipos()[tam-2];
               tiposAdded[0] = tipoNuevo;
               tiposAdded[1] = asocBase[0].getTipos()[tam-2];
               //mod = new ArrayList<String>(asocBase[0].getTipos());
               //mod.add(mod.size(),tipoNuevo);
               modArray = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
               modArray[modArray.length-1] = tipoNuevo;

               boolean valido=true;

               String tipo;
               int index=2;
               List<String> modAux = new ArrayList<String>(Arrays.asList(modArray));
               for(k=tam-3;k>=0;k--){
                  tipo = modAux.remove(k);
                  Nodo subnodoAux = raizArbol.obtenerNodoEnArbol(modAux);
                  if(subnodoAux==null){
                     valido=false;
                     break;
                  }
                  asocBase[index] = subnodoAux.getModelo();
                  nodosBase[index] = subnodoAux;
                  tiposAdded[index] = tipo;
                  index++;
                  modAux.add(k, tipo);
               }

               if(!valido){ continue; }

               modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloAsociacion", modArray, windowSize,
                     numHilos);

               // Añadir el nuevo nodo a la lista de hijos del padre
               NodoAntepasados hijo = (NodoAntepasados)creaNodoFachada(modelo,hijos, tipoNuevo);
               SupernodoAdoptivos.fijarNodosAdoptivos(nodosBase, tiposAdded, hijo);
            }
            if(!hijos.getNodos().isEmpty()){
               nuevoNivelSiguiente.add(hijos);
            }
         }
      }

      //mapaFuturas = nuevoMapa;
      //nivelSiguiente = nuevoNivel;
      nivelSiguiente = nuevoNivelSiguiente;
      //tiemposEstrategia[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, false);
   }

   /*
    * En lugar de recorrer los supernodos y emparejar pares de nodos de cada uno
    * utiliza los nodos que creó generarAsociacionesCandidatasSiguientes en la
    * anterior iteración usando las funciones de padres/nodos adoptivos
    * para obtener los patrones a combinar.
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineAhorro#generarCandidatasGeneral(java.util.List, java.util.List, int, java.util.List)
    */
   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       if(tam == 3){
          super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
          //generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel); //Ya se llama en la clase padre
          return;
       }
      //tam>=3

      IAsociacionTemporal modelo;
      String[] modArray;

      GeneradorPatronesArbolSiguientes genp = new GeneradorPatronesArbolSiguientes(tam, this);

      // Inicializar mapa
      resetMapas(tipos);
      for(Supernodo supernodo : nivelSiguiente){
         for(Nodo nodoCandidato : supernodo.getListaNodos()){

            Nodo padre = supernodo.getPadre();
            genp.setPadre(padre.getModelo(), 0);

            registroT.tiempoAsociaciones(tam-1, true);
            modArray = nodoCandidato.getModelo().getTipos();

            boolean valido = genp.setPadres((NodoAntepasados)nodoCandidato);

            registroT.tiempoAsociaciones(tam-1, false);

            if(!valido){ continue; }

            // Combinar los patrones
            List<Patron> patrones = genp.generarPatrones(modArray);

            // Construir el modelo
            registroT.tiempoModelo(tam-1, true);
            if(!patrones.isEmpty()){
               // Hay: añadir punteros en la tabla hash y a candidatas
               modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize, patrones,
                     numHilos);

               notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);
               //setModeloPatrones(patrones, modelo);
               //El nodo siempre existe en asociaciones de tamaño > 3
               nodoCandidato.setModelo(modelo);

            }// else: No hay patrones candidatos: descartar modelo candidato actual
            registroT.tiempoModelo(tam-1, false);



         } // for nodo
         if(!supernodo.getNodos().isEmpty()){
            nuevoNivel.add(supernodo);
         }else{
            supernodo.getPadre().removeHijos();
         }
      } //for supernodos

      generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
   }

}
