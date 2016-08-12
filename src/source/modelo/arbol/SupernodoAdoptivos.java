package source.modelo.arbol;


import java.util.HashMap;
import java.util.Map;

/**
 *
 * TODO documentación
 *
 * @author vanesa.graino
 *
 */
public class SupernodoAdoptivos extends Supernodo {

   /**
    *
    */
   private final Map<String,NodoAntepasados> adoptivos = new HashMap<String,NodoAntepasados>(); //usado por MineAhorro

   protected SupernodoAdoptivos(){
      super();
   }

   protected SupernodoAdoptivos(Nodo padre){
      super(padre);
   }

   /**
    * Busca en los hijos adoptivos
    * @return
    */
   public NodoAntepasados getHijoAdoptivo(String tipo){
      return adoptivos.get(tipo);
   }

   /**
    *
    * @param nodo
    * @param tipo
    */
   public void addHijoAdoptivo(NodoAntepasados nodo, String tipo){
      adoptivos.put(tipo, nodo);
      nodo.getPadresAdoptivos().add(this.padre);
   }

   /**
    * Obtiene el descendiente para el tipo de evento <tipo>
   * sin diferenciar entre hijos e hijos adoptivos.
   * @param tipo - tipo a mayores del descendiente.
   * @return null si no hay descendiente o el descendiente.
   */
   public Nodo getDescendiente(String tipo){
      Nodo hijo = getHijo(tipo);
      if(hijo==null){ hijo = getHijoAdoptivo(tipo); }
      return hijo;
   }


   /**
   *
   * @param tipo
   * @return
   */
   public Nodo removeHijoAdoptivo(String tipo){
      return adoptivos.remove(tipo);
   }

   public Nodo removeHijoAdoptivo(Nodo nodo){
      for(String tipo : adoptivos.keySet()){
         if(nodo == adoptivos.get(tipo)){
            return adoptivos.remove(tipo);
         }
      }
      return null;
   }

   public Nodo getHijo(String tipo, boolean esHijo){
      if(esHijo){ return getHijo(tipo); }
      return getHijoAdoptivo(tipo);
   }

   /**
    * Elimina también los nodos adoptivos
    */
   @Override
   public Nodo eliminarNodoEnArbol(String[] tipos) {
      NodoAntepasados nodo = (NodoAntepasados)super.eliminarNodoEnArbol(tipos);
      if(nodo != null){
         for(Nodo pa : nodo.getPadresAdoptivos()){
            ((SupernodoAdoptivos)pa.getHijos()).removeHijoAdoptivo(nodo);
         }
      }
      return nodo;
   }

   //
   public static void fijarNodosAdoptivos(Nodo[] nodosBase, String[] tiposAdded, NodoAntepasados hijo){
      for(int n=1; n<nodosBase.length;n++){
         ((SupernodoAdoptivos)nodosBase[n].getHijos()).addHijoAdoptivo(hijo, tiposAdded[n]);
      }
   }


}
