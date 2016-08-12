package source.modelo.arbol;

import java.util.ArrayList;
import java.util.List;

import source.modelo.IAsociacionTemporal;


/**
 * Versión del nodo del árbol para HSTP paralelo.
 * Se crea la lista de modelosHilos cuando se añaden los modelos de cada hilo.
 * @author vanesa.graino
 *
 */
public class NodoHilos extends Nodo {

   /*
    * Atributos propios
    */

   private final List<IAsociacionTemporal> modelosHilos = new ArrayList<IAsociacionTemporal>();

   /*
    * Constructores
    */

   public NodoHilos(IAsociacionTemporal modelo, Supernodo hijos,
         Supernodo supernodo) {
      super(modelo, hijos, supernodo);
   }

   public NodoHilos(IAsociacionTemporal modelo, Supernodo supernodo) {
      super(modelo, supernodo);
   }

   public NodoHilos(IAsociacionTemporal modelo) {
      super(modelo);
   }

   /*
    * Métodos
    */

   private void ensureCapacity(int hilos){
      if(modelosHilos.size()<hilos){
         for(int i=modelosHilos.size();i<hilos;i++){
            modelosHilos.add(null);
         }
      }
   }

   public void setModelosHilo(int hilo, IAsociacionTemporal modelo){
      ensureCapacity(hilo+1);
      modelosHilos.set(hilo, modelo);
   }

   public IAsociacionTemporal getModelo(int hilo){
      return modelosHilos.get(hilo);
   }
}
