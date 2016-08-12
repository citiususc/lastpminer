package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionArbol;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.NodoAntepasadosAnotado;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.arbol.SupernodoAdoptivosAnotados;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Implementa la idea de Paulo de las anotaciones utilizando el arbol para decidir si se extiende o no una.
 * Necesita las anotaciones de eventos de la ventana para poder hacerlo así que necesita almacenarlas de
 * alguna manera.
 *
 * En esta V3, como en la V2, sólo se considera que un supernodo tiene nodos combinables si por lo menos una anotación pertenece
 * a un nodo suyo. Además, ya no se combinan los nodos en la generación general sino que se utilizan los nodos siguientes
 * guardados en <nivelSiguiente>.
 *
 * @author vanesa.graino
 *
 */
public class MineDictionarySuperMarcarArbol extends MineDictionarySuperModeloES {
   private static final Logger LOGGER = Logger.getLogger(MineDictionarySuperMarcarArbol.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */

   /**
    * Lista de supernodos de la siguiente iteración en donde guarda
    * la función {@link #generarAsociacionesCandidatasSiguientes} los
    * supernodos que crea.
    */
   protected List<Supernodo> nivelSiguiente;

   {
      treeClassName = "SupernodoAdoptivosAnotados";
      associationClassName = "ModeloDFETontoMarcarArbol";
      patternClassName = "PatronMarcado";
   }



   /*
    * Constructores
    */

   public MineDictionarySuperMarcarArbol(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new NodoAntepasadosAnotado(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo){
      return new NodoAntepasadosAnotado(modelo);
   }

   @Override
   public void finSecuencia(){
      for(Supernodo ns : nivelActual){
         ((SupernodoAdoptivosAnotados)ns).resetAnotaciones();
      }
   }

   @Override
   protected void setAnotacionesEvento(List<Patron> encontrados, List<Patron> ventanaActual,
         String evento, int tmp, int tam){
      setAnotacionesEvento(encontrados, ventanaActual, evento, tmp, tam, windowSize, nivelActual, anotacionesEvitadas);
   }

   //@Override
   public static void setAnotacionesEvento(List<Patron> encontrados, List<Patron> ventanaActual,
         String evento, int tmp, int tam, int windowSize, List<Supernodo> nivelActual, long[] anotacionesEvitadas){

      ventanaActual.clear();
      int eSize = encontrados.size();
      if(eSize<tam){
         anotacionesEvitadas[tam-1]+=eSize;
         encontrados.clear();
         return;
      }

      Collections.sort(encontrados);

      //List<Boolean> utiles = new ArrayList<Boolean>(eSize);
      List<Integer> correspondencia = new ArrayList<Integer>(eSize);
      int indice = 0;
      IAsociacionArbol anterior = (IAsociacionArbol)((PatronDictionaryFinalEvent)encontrados.get(0)).getAsociacion();
      List<IAsociacionArbol> allModels = new ArrayList<IAsociacionArbol>(eSize);
      allModels.add(anterior);
      //utiles.add(false);
      correspondencia.add(0);

      // Se marca en el árbol el patrón encontrado para que
      // pueda ser utilizado por la estrategia
      ((IAsociacionArbol)anterior).getNodo().setUtil(false);

      //Lista de supernodos
      List<Supernodo> allSupernodes = new ArrayList<Supernodo>();
      Supernodo snAnterior = anterior.getNodo().getSupernodo();
      allSupernodes.add(snAnterior);

      for(int i=1; i<eSize;i++){
         PatronDictionaryFinalEvent p = (PatronDictionaryFinalEvent)encontrados.get(i);
         if(anterior != p.getAsociacion()){
            indice++;
            anterior = (IAsociacionArbol)p.getAsociacion();
            // Se marca en el árbol el patrón encontrado
            ((IAsociacionArbol)anterior).getNodo().setUtil(false);
            //utiles.add(false);
            allModels.add(anterior);

            //supernodos
            if(snAnterior != anterior.getNodo().getSupernodo()){
               snAnterior = anterior.getNodo().getSupernodo();
               allSupernodes.add(snAnterior);
            }
         }
         correspondencia.add(indice);
      }

      List<NodoAntepasadosAnotado> anotados = new ArrayList<NodoAntepasadosAnotado>();
      //int iSN=nivelActual.indexOf(allModels.get(0).getNodo().getSupernodo());
      //for(; iSN>=0 && iSN<nivelActual.size()-1;iSN++){
      //   SupernodoAdoptivosAnotados ns = (SupernodoAdoptivosAnotados)nivelActual.get(iSN);
      //System.out.println("Total supernodos: " + nivelActual.size() + ", supernodos: " + allSupernodes.size());
      for(Supernodo supernodo : allSupernodes){
         SupernodoAdoptivosAnotados ns = (SupernodoAdoptivosAnotados)supernodo;
         if(ns.nodosMarcados(tmp, windowSize,anotados)){
            combinarNodos(anotados, tmp, windowSize);
         }
      }

      for(int i=0; i<eSize; i++){
         if(!allModels.get(correspondencia.get(i)).getNodo().isUtil()){
            correspondencia.remove(i);
            encontrados.remove(i);
            i--;
            eSize--;
            anotacionesEvitadas[tam-1]++;
         }
      }

      ventanaActual.addAll(encontrados);
      encontrados.clear();
   }

   protected static void combinarNodos(List<NodoAntepasadosAnotado> anotados, int tmp, int windowSize){
      for(int i=0,nSize=anotados.size();i<nSize-1;i++){
         NodoAntepasadosAnotado nodo = anotados.get(i);
         for(int j=i+1; j<nSize; j++){
            NodoAntepasadosAnotado nodoExtendido = (NodoAntepasadosAnotado)nodo.getHijos().getHijo(anotados.get(j).getUltimoTipo());
            //TODO Creo que se puede saltar el primero (cambiaría el 0 por un 1)
            if(nodoExtendido != null && nodoExtendido.asegurarAntepasados(tmp, windowSize,0)){
               nodoExtendido.padresUtiles();
            }
         }
      }
   }

   /**
    * Crea las asociaciones sin patrones de la siguiente iteración a la actual.
    * @param tipos
    * @param tam
    * @param nuevoNivel
    * @throws FactoryInstantiationException
    */
   protected void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      LOGGER.info("Asociaciones siguientes a tam " + (tam-1));
      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_ESTRATEGIA, tam-1, true);
      int i,j,k;
      String[] mod;
      IAsociacionTemporal[] asocBase = new IAsociacionTemporal[tam];
      IAsociacionTemporal modelo;
      Nodo[] nodosBase = new Nodo[tam];
      String[] tiposAdded = new String[tam];
      List<Supernodo> nuevoNivelSiguiente = new ArrayList<Supernodo>();

      for(Supernodo supernodo : nuevoNivel){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            nodosBase[0] = padre;
            asocBase[0] = padre.getModelo();
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               registroT.tiempoAsociaciones(tam-1, true);
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               nodosBase[1] = madre;
               asocBase[1] = madre.getModelo();
               String tipoNuevo = asocBase[1].getTipos()[tam-2];
               tiposAdded[0] = tipoNuevo;
               tiposAdded[1] = asocBase[0].getTipos()[tam-2];
               //mod = new ArrayList<String>(asocBase[0].getTipos());
               //mod.add(mod.size(),tipoNuevo);
               mod = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
               mod[mod.length-1] = tipoNuevo;

               boolean valido=true;

               String tipo;
               int index=2;
               List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
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

               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               //Construir el modelo
               registroT.tiempoModelo(tam-1, true);
               modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloAsociacion", mod, windowSize, numHilos);

               // Añadir el nuevo nodo a la lista de hijos del padre
               NodoAntepasados hijo = (NodoAntepasados)creaNodoFachada(modelo,hijos, tipoNuevo);
               SupernodoAdoptivos.fijarNodosAdoptivos(nodosBase, tiposAdded, hijo);

               registroT.tiempoModelo(tam-1, false);
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

   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      if(tam == 3){
         super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
         //generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
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

            modArray = nodoCandidato.getModelo().getTipos();

            boolean valido = genp.setPadres((NodoAntepasados)nodoCandidato);

            if(!valido){ continue; }

            // Combinar los patrones
            List<Patron> patrones = genp.generarPatrones(modArray);

            // Construir el modelo
            registroT.tiempoModelo(tam-1, true);
            if(!patrones.isEmpty()){
               // Hay: añadir punteros en la tabla hash y a candidatas
               modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize,
                     patrones, supermodelo, numHilos);

               notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);

               //setModeloPatrones(patrones, modelo);
               //El nodo siempre existe en asociaciones de tamaño > 3
               nodoCandidato.setModelo(modelo);

            }// else: No hay patrones candidatos: descartar modelo candidato actual
            registroT.tiempoModelo(tam-1, false);
         } // for nodos
         if(!supernodo.getNodos().isEmpty()){
            nuevoNivel.add(supernodo);
         }else{
            supernodo.getPadre().removeHijos();
         }
      } //for supernodos

      generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);

   }


}


