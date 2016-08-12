package source.configuracion;

import org.junit.Test;


public class ConfigSinteticaTest {

   @Test
   public void testToString(){
      ConfigSintetica cs = new ConfigSintetica("BDPRUEBA", true,
            Modes.MODE_BASIC, new int[]{20, 40, 60, 80});
      System.out.println(cs);

      cs = new ConfigSintetica("BDPRUEBA2", false,
            Modes.MODE_EPISODE, new int[]{20, 40, 60, 80});
      System.out.println(cs);
   }
}
