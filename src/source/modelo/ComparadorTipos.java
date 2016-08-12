package source.modelo;

import java.util.Comparator;

public class ComparadorTipos implements Comparator<String[]>{

   @Override
   public int compare(String[] tipos, String[] o) {
      if(o.length == tipos.length){
         for(int max=tipos.length, i=0; i<max; i++){
            int comp = tipos[i].compareTo(o[i]);
            if(comp != 0) {
               return comp;
            }
         }
         return 0;
      }
      if(o.length < tipos.length){
         return 1;
      }
      return -1;
   }

}
