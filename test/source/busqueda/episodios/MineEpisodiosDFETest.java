package source.busqueda.episodios;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class MineEpisodiosDFETest extends PrincipalTestGeneral {

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, false}, //0
            {"BD4", 20, false}, //1
            {"BD5", 20, false}, //1
      });
   }

   public MineEpisodiosDFETest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_HSTP, Modes.MODE_EPISODE, collection, window, skip);
   }


}
