package source.io.report;

import org.junit.Test;

import source.configuracion.ConfigurationParameters;


public class TXTOneTimeReportTest {
   @Test
   public void test(){
      ConfigurationParameters params = new ConfigurationParameters();

      new TXTOneTimeReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(0, 200);
      new TXTOneTimeReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(1, 2030);
      new TXTOneTimeReport("/tmp/hstpminer/probaTxt/", params).escribirTiempo(4, 2320);
   }
}
