package source.evento;

import java.util.List;

import com.google.common.collect.Lists;

public class GuavaChunkedSequence extends SecuenciaSimple{

   private static final int CHUNK_SIZE = 1000;

   /*
    * Constructores
    */

   public GuavaChunkedSequence() {
      super();
   }

   public GuavaChunkedSequence(List<Evento> eventos){
      super(eventos);
   }

   /*
    * Métodos
    */

   @Override
   public boolean contains(Evento ev) {
      return indexOf(ev) != -1;
   }

   @Override
   public int indexOf(Evento ev) {
      for (List<Evento> partition : Lists.partition(eventos, CHUNK_SIZE)) {
        // do something with partition
         if(partition.get(0).getInstante()>ev.getInstante()){
            break;
         }else if(partition.get(partition.size()-1).getInstante()<ev.getInstante()){
            continue;
         }
         int indice = partition.indexOf(ev);
         if(indice>-1){
            return indice;
         }
      }
      return -1;
   }

   @Override
   public int lastIndexOf(Evento ev) {
      return indexOf(ev);
   }

}
