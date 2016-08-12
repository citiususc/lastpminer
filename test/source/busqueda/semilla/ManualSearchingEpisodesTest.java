package source.busqueda.semilla;

import java.util.List;

import org.junit.Test;

import source.CapsulaEjecucion;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;
import source.excepciones.SemillasNoValidasException;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;


public class ManualSearchingEpisodesTest {

   @Test
   public void test() throws SemillasNoValidasException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setAlgorithm(Algorithms.ALG_MAN);
      params.setMode(Modes.MODE_FULL);
      params.setCollection("apnea");
      CapsulaEjecucion capsula = new CapsulaEjecucion(params, null);
      capsula.mineria(0);

      Patron.setPrintID(true);
      List<List<IAsociacionTemporal>> resultado = capsula.resultados;
      System.out.println(resultado.get(0).get(0).getPatron(0));
   }
}
