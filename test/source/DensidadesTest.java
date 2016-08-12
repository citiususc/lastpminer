package source;

import org.junit.Test;

import source.configuracion.ConfigSintetica;
import source.configuracion.ExecutionParameters;

public class DensidadesTest {

   @Test
   public void densidadesTest() {
      //for(String bbdd : ConfigurationParameters.BBDDS){
      for(ConfigSintetica cs: ExecutionParameters.BBDD){
         String bbdd = cs.nombre;
         if(bbdd.equals("apnea")) continue;
         System.out.println("\n\n" + bbdd);
         //AllThatYouNeed capsula = new AllThatYouNeedSinteticas(bbdd);

      }

   }

}
