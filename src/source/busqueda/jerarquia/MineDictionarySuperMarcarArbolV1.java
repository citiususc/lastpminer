package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import source.modelo.IAsociacionArbol;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasadosAnotado;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivosAnotados;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Implementa la idea de Paulo de las anotaciones utilizando el arbol para decidir si se extiende o no una.
 * Necesita las anotaciones de eventos de la ventana para poder hacerlo así que necesita almacenarlas de
 * alguna manera.
 *
 * @author vanesa.graino
 *
 */
public class MineDictionarySuperMarcarArbolV1 extends MineDictionarySuperModeloES {
   private static final Logger LOGGER = Logger.getLogger(MineDictionarySuperMarcarArbolV1.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */

   {
      treeClassName = "SupernodoAdoptivosAnotados";
      associationClassName = "ModeloDFETontoMarcarArbol";
      patternClassName = "PatronMarcado";
   }

   /*
    * Constructores
    */

   public MineDictionarySuperMarcarArbolV1(String executionId, boolean savePatternInstances,
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

      for(int i=1; i<eSize;i++){
         PatronDictionaryFinalEvent p = (PatronDictionaryFinalEvent)encontrados.get(i);
         if(anterior != p.getAsociacion()){
            indice++;
            anterior = (IAsociacionArbol)p.getAsociacion();
            // Se marca en el árbol el patrón encontrado
            ((IAsociacionArbol)anterior).getNodo().setUtil(false);
            //utiles.add(false);
            allModels.add(anterior);
         }
         correspondencia.add(indice);
      }

      List<NodoAntepasadosAnotado> anotados = new ArrayList<NodoAntepasadosAnotado>();
      for(int i=0; i<nivelActual.size()-1;i++){
         SupernodoAdoptivosAnotados ns = (SupernodoAdoptivosAnotados)nivelActual.get(i);
         if(ns.nodosMarcados(tmp, windowSize,anotados)){// si hay más de un nodo marcado
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

   /**
    * Combina todos los nodos de anotados comprobando si los padres de las
    * superasociaciones resultantes tienen marcas en la ventana, para esto
    * llama al método {@link NodoAntepasadosAnotado.asegurarAntepasados}
    * del nodo resultado de la combinación. Si la extensión es viable llama
    * al método {@link NodoAntepasadosAnotado.padresUtiles}
    * @param anotados
    * @param tmp
    * @param windowSize
    */
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

}
