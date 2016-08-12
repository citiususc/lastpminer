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
public class MineEpisodiosArbolTest extends PrincipalTestGeneral {

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0
            {"BD4", 20, PASADO}, //1
            {"BD5", 20, PASADO}, //1
      });
   }

   public MineEpisodiosArbolTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_TSTP, Modes.MODE_EPISODE, collection, window, skip);
   }


}
