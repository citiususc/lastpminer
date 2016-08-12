package source.modelo.arbol;

import java.util.ArrayList;
import java.util.List;

import source.modelo.IAsociacionTemporal;

/**
 * La lista {@code padresAdoptivos} se rellena en {@link SupernodoAdoptivos} cuando
 * se añaden hijos adoptivos al supernodo. Esta clase también se encarga de borrar
 * los nodos adoptivos cuando se eliminan nodos en el árbol.
 * En la generación de candidatos se fijan con la llamada a
 * {@link SupernodoAdoptivos#fijarNodosAdoptivos}.
 * @author vanesa.graino
 *
 */
public class NodoAntepasados extends Nodo {

   // los padres que no son directamente el padre del nodo
   // pero son subasociaciones del mismo
   protected final List<Nodo> padresAdoptivos = new ArrayList<Nodo>(); //usado por MineAhorro

   /*
    * Constructores
    */

   //Para las hijas
   protected NodoAntepasados(){

   }

   public NodoAntepasados(IAsociacionTemporal modelo){
      //super(modelo);
      this.modelo = modelo;
      this.hijos = new SupernodoAdoptivos(this);
      this.supernodo = null;
   }

   public NodoAntepasados(IAsociacionTemporal modelo, Supernodo supernodo){
      //super(modelo, supernodo);
      this(modelo);
      this.supernodo = supernodo;
   }

   public NodoAntepasados(IAsociacionTemporal modelo, Supernodo hijos, Supernodo supernodo){
      super(modelo, hijos, supernodo);
   }

   /*
    * Métodos
    */

   public List<Nodo> getPadresAdoptivos(){
      return padresAdoptivos;
   }
}
