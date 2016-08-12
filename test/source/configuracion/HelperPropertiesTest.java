package source.configuracion;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;


public class HelperPropertiesTest {

   @Test
   public void testBBDD(){
      //HelperProperties helper = new HelperProperties(new File("/home/remoto/vanesa.graino/workspace/hstpminer/resources/config.properties"));
      HelperProperties helper = new HelperProperties();
      System.out.println(helper.getPathSinteticas());
      Assert.assertNotNull(helper.getPathSinteticas());

      System.out.println(helper.getProjectHome());
      Assert.assertNotNull(helper.getProjectHome());

      ConfigSintetica[] sts = helper.getSinteticas();
      System.out.println(Arrays.toString(sts));

      Assert.assertEquals("No se han procesado todas las bases de datos", 30, sts.length);
   }
}

