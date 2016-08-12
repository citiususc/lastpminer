package source.modelo;

import java.util.Comparator;

public class ComparadorAsociaciones implements Comparator<IAsociacionTemporal> {

   public ComparadorAsociaciones() {
      //Vacio
   }

   @Override
   public int compare(IAsociacionTemporal o1, IAsociacionTemporal o) {
      ///Comparar tipos
      if(o.getTipos().length == o1.getTipos().length){
         for(int max=o1.getTipos().length, i=0; i<max; i++){
            int comp = o1.getTipos()[i].compareTo(o.getTipos()[i]);
            if(comp != 0) {
               return comp;
            }
         }
         return 0;
      }
      if(o.getTipos().length < o1.getTipos().length){
         return 1;
      }
      return -1;
   }

}
