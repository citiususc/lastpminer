package source;

import org.junit.Ignore;
import org.junit.Test;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;

public class SAHSTest {
   @Test
   @Ignore
   public void testMaxTime(){
      AllThatYouNeedSAHS capsula = new AllThatYouNeedSAHS();
      IColeccion coleccion = capsula.coleccion;
      long maxTime = 0;
      for(ISecuencia seq : coleccion){
         for(Evento ev : seq ) {
            if(ev.getInstante()>maxTime){
               maxTime = ev.getInstante();
            }
         }
      }
      System.out.println("Max time: " + maxTime);
   }

   @Test
   public void testMaxTransactionSize(){
      AllThatYouNeedSAHS capsula = new AllThatYouNeedSAHS();
      IColeccion coleccion = capsula.coleccion;
      int counter = 0, maxCounter = 0;
      long lastTime=-1;
      for(ISecuencia seq : coleccion){
         counter = 0;

         for(Evento ev : seq ) {
            if(lastTime == ev.getInstante()){
               counter++;
               if(counter>maxCounter){
                  maxCounter = counter;
               }
            }else{
               counter=0;
               lastTime = ev.getInstante();
            }
         }
      }
      System.out.println("Max transaction size: " + maxCounter);
   }

}
