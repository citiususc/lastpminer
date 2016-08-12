package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import source.modelo.arbol.Nodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.clustering.IClustering;

/**
 *
 * Misma idea de {@link MineAhorro} solo que se recorren de forma diferente los candidatos
 * a ser buscados en la secuencia.
 *
 * @author vanesa.graino
 *
 */
public class MineAhorroV1 extends MineAhorro{
   private static final Logger LOGGER = Logger.getLogger(MineAhorroV1.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   public MineAhorroV1(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   //TODO arreglar que ahora que está como estática no se puede sobreescribir
   //@Override
   protected List<Boolean> getAnotacionesEventoUtiles(List<List<String>> allModels, List<Boolean> utiles,
         List<String> listaEventos, String evento, int tam){

      SupernodoAdoptivos hijos;
      Nodo nodo = null;
      for(int i=0, allModelsSize=allModels.size(); i<allModelsSize-tam+1; i++){
         List<String> m = allModels.get(i);
         nodo = raizArbol.obtenerNodoEnArbol(m);
         hijos = (SupernodoAdoptivos)nodo.getHijos();
         List<String> mExt = new ArrayList<String>(m);
         List<Integer> indices = new ArrayList<Integer>();
         for(int j=i+1;j<listaEventos.size();j++){
            indices.clear();
            String e = listaEventos.get(j);
            if(hijos.getDescendiente(e) == null ){ continue; }

            int insertionPoint = Collections.binarySearch(mExt, e);

            //La asociacion ya tiene este evento, pasamos
            //if(insertionPoint > -1){ continue; } //no es necesario
            insertionPoint = -insertionPoint - 1;
            mExt.add(insertionPoint, e); //se inserta ordenadamente

            //if(hijos.getHijo(e, insertionPoint == mExt.size()-1) != null ){
               for(int k=tam;k>=0;k--){
                  if(k == insertionPoint){ continue; }
                  String borrado = mExt.remove(k);
                  // Si el borrado es del evento anotado, no nos sirve
                  if(borrado.equals(evento)){
                     mExt.add(k,borrado);
                     continue;
                  }
                  //Buscamos la anotacion en las anotaciones
                  for(int l=0; l<allModels.size();l++){
                     if(i==l){ continue; }
                     if(allModels.get(l).equals(mExt)){
                        indices.add(l);
                        break;
                     }
                  }

                  //Volvemos a meter el evento
                  mExt.add(k,borrado);
               }
               if(indices.size() >= tam-1){
                  utiles.set(i,true);
                  for(int iUtil : indices){
                     utiles.set(iUtil, true);
                  }
               }
            //}//Fin de busqueda de subasociaciones para mExt

            mExt.remove(insertionPoint);
         }
      }

      return utiles;
   }

}
