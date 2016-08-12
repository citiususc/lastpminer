package source.busqueda.negacion;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class NegacionMineEpisodiosTest extends PrincipalTestGeneral{
   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_NEG,  Modes.MODE_EPISODE, "apnea", 80, false}, //0
      });
   }

   public NegacionMineEpisodiosTest(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      //this.tamMaximoPatron = 3; //TODO borrar
      //this.savePatternInstances = true;
      this.writeStatistics = true;
   }
}
