package source.patron;

public class GeneradorID {

   private int idGenerator=1;

   public GeneradorID() {

   }

   public int nextID(){
      return idGenerator++;
   }

   public void assureID(int id){
      if(idGenerator<=id){
         idGenerator = id +1;
      }
   }

   public void resetGenerator(){
      idGenerator = 1;
   }
}
