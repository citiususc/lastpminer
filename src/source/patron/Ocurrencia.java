package source.patron;

import java.util.Arrays;

/*
 *   Representa una ocurrencia de un patrón, encapsulando el identificador de la
 * secuencia en la que se encuentra y los instantes de tiempo en los que ocurre
 * cada uno de los eventos de la ocurrencia del patrón.
 *   Si el patrón representa los tipos de eventos A,B,C ; entonces eventTimes será
 * un array de la forma [instante(A),instante(B),instante(C)].
 */
public class Ocurrencia {
   private final int sequenceID;
   private final int[] eventTimes;

   public Ocurrencia(int sequenceID, int[] eventTimes){
      int etSize = eventTimes.length;
      this.sequenceID = sequenceID;
      this.eventTimes = Arrays.copyOf(eventTimes, etSize);
      //this.eventTimes = new int[etSize];
      //for(int i=0;i<etSize;i++){
      //   this.eventTimes[i]=eventTimes[i];
      //}
   }

   /*
    * Getters && Setters
    */
   public int getSequenceID() {
      return sequenceID;
   }

   public int[] getEventTimes() {
      return eventTimes;
   }

   public String toString(){
      return "< " + sequenceID + ", " + Arrays.toString(eventTimes)+ " >";
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Ocurrencia? equalsTo((Ocurrencia)obj) : false;
   }

   public boolean equalsTo(Ocurrencia ocu){
      if(ocu.sequenceID != sequenceID || ocu.eventTimes.length != eventTimes.length){
         return false;
      }
      for(int i=0;i<eventTimes.length;i++){
         if(eventTimes[i]!=ocu.eventTimes[i]){ return false; }
      }
      return true;
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(eventTimes) + sequenceID * 31;
   }

}
