package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.busqueda.AbstractMine;
import source.busqueda.GeneradorPatrones;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;

/**
 * Utiliza el árbol para comprobar que las subasociaciones existen y, por
 * tanto, fueron frecuentes en la anterior iteración.
 * @author vanesa.graino
 *
 */
public class GeneradorPatronesArbol extends GeneradorPatrones {

   public GeneradorPatronesArbol(int tam, AbstractMine mine) {
      super(tam, mine);
   }

   /*
    * Otros métodos
    */

   public String[] getModArray(){
       // Todos los nodos del supernodo comparten todos los tipos
       // de evento salvo el último.
       String tipoNuevo = asocBase[1].getTipos()[tam-2];
       //mod = new ArrayList<String>(asocBase[0].getTipos());
       //mod.add(tipoNuevo);
       String[] modArray = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
       modArray[modArray.length-1] = tipoNuevo;
       return modArray;
   }

   public String getTipoNuevo(){
      return asocBase[1].getTipos()[tam-2];
   }

   public boolean comprobarSubasociaciones(Supernodo raizArbol, String[] mod){
      String tipo;
      int index=2;
      List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      boolean valido=true;
      for(int k = tam-3; k>=0; k--){
         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         getPatCount()[index] = asocBase[index].getPatrones().size();
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }

   //TODO aprovechar nodos adoptivos
   //public static boolean comprobarSubasociaciones(SupernodoAdoptivos raizArbol, int tam, IAsociacionTemporal[] asocBase,
   //      int[] patCount, List<String> mod){
   public boolean comprobarSubasociaciones(NodoAntepasados nodo, SupernodoAdoptivos raizArbol, List<String> mod){
      String tipo;
      int index=2;
      List<String> modAux = new ArrayList<String>(mod);
      boolean valido=true;
      for(int k = tam-3; k>=0; k--){
         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         patCount[index] = asocBase[index].getPatrones().size();
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }
}

