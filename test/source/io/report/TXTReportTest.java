package source.io.report;

import org.junit.Test;

import source.configuracion.ConfigurationParameters;


public class TXTReportTest {
   @Test
   public void test(){
      ConfigurationParameters params = new ConfigurationParameters();

      new TXTReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(true, 200);
      new TXTReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(false, 2030);
      new TXTReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(false, 2320);
   }

}
