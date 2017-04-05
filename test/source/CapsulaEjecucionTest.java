package source;

import org.junit.Assert;
import org.junit.Test;

import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;


public class CapsulaEjecucionTest {
   /**
    * Valida con un ejemplo si funciona correctamente el método getFicheroValidacion de Principal
    */
   @Test public void testFicheroValidacion(){
      ConfigurationParameters params = new ConfigurationParameters();
      params.setCollection("BD7");
      params.setMode(Modes.MODE_EPISODE);
      params.setWindowSize(80);
      String fileName = CapsulaEjecucion.getFicheroValidacion(params);
      Assert.assertEquals("No se calcula correctamente el fichero de validacion",
            ExecutionParameters.PROJECT_HOME + "/output/referencias/BD7-episode-80-cp-0.25-mp-0.45.txt", fileName);
   }
}
