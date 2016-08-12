package source.busqueda.concurrente.semilla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class TestConcurrenteSAHS extends PrincipalTestGeneral{
   @Parameters
   public static Collection<Object[]> data(){
      List<Object[]> configuraciones = new ArrayList<Object[]>();
      for(int win=20;win<=120;win+=20){
         for(Algorithms alg : new Algorithms[]{ Algorithms.ALG_HCON, Algorithms.ALG_CON}){
            configuraciones.add(new Object[]{win, alg, Modes.MODE_FULL, false});
            configuraciones.add(new Object[]{win, alg, Modes.MODE_SEED, false});
         }
      }

      //XXX Fallan CON, seed, apena win =120
      // Falla HCON, seed apnea, 20
      // Falla CON, seed, apnea, 20
      return configuraciones;
   }

   public TestConcurrenteSAHS(int win, Algorithms algorithm, Modes mode, boolean skip){
      super(algorithm,mode,ConfigurationParameters.APNEA_DB, win, skip);
   }

}
