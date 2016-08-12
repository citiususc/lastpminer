package source.busqueda.semilla;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import source.CapsulaEjecucion;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;
import source.excepciones.SemillasNoValidasException;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;
import source.patron.PatronSemilla;


public class ManualSearchingTest {

   @Test
   public void test() throws SemillasNoValidasException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setAlgorithm(Algorithms.ALG_MAN);
      params.setMode(Modes.MODE_SEED);
      params.setCollection("apnea");
      CapsulaEjecucion capsula = new CapsulaEjecucion(params, null);
      capsula.mineria(0);

      Patron.setPrintID(true);
      List<List<IAsociacionTemporal>> resultado = capsula.resultados;
      System.out.println(resultado.get(0).get(0));
   }

   @Ignore
   @Test
   public void test2(){

      PatronSemilla p = new PatronSemilla(new String[]{"fA","fD","fF","fT","iA","iD","iF","iT"}, CapsulaEjecucion.getRIntervalosSemilla(80), true);
      System.out.println("Igual?" + (Integer.MAX_VALUE == -Integer.MIN_VALUE));

      System.out.println(p);
   }
}
