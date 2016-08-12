package source.configuracion;

import org.junit.Test;
import static org.junit.Assert.*;


public class ConfigurationParametersTest {

   @Test
   public void testAlgorithmString(){
      ConfigurationParameters config = new ConfigurationParameters();
      config.setAlgorithm(Algorithms.ALG_ANOT);
      System.out.println("ANOT: " + config.getAlgorithmString());
      assertEquals("ANOT", config.getAlgorithmString());

      config.setAlgorithm(Algorithms.ALG_MAN);
      System.out.println("MAN: " + config.getAlgorithmString());
      assertEquals("MAN", config.getAlgorithmString());
   }

   @Test
   public void testProcesarParametros(){
      ConfigurationParameters params = new ConfigurationParameters();
      params.procesarParametros(new String[]{
            "algorithm=hstp",
            "mode=episode",
            "windowSize=20",
            "minFreq=300",
            "inputFileName=",
            "inputPath=\"" + ExecutionParameters.PATH_SINTETICAS + "BDRoE6\"",
            "collection=BDRoE6",
            "iterations=5",
            "resultPath=\"" + ExecutionParameters.PROJECT_HOME + "output/BDRoE6/\""
      });
      assertEquals(Algorithms.ALG_HSTP, params.algorithm);
      assertEquals(Modes.MODE_EPISODE, params.mode);
      assertEquals(20, params.windowSize);
      assertEquals(300, params.minFreq);

      System.out.println("inputPath: " + params.inputPath);
      assertEquals("\"" + ExecutionParameters.PATH_SINTETICAS + "BDRoE6\"", params.inputPath);

      assertEquals("BDRoE6", params.collection);
      assertEquals(5, params.iterations);
      assertEquals("\"" + ExecutionParameters.PROJECT_HOME + "output/BDRoE6/\"", params.resultPath);

      System.out.println("ConfigurationParameters: " + params);
   }
}
