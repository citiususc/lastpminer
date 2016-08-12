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
public class MineCEDFESuperModeloTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0	 	07/09/15
            {"BD4", 80, PASADO}, //1		07/09/15
            {"BD5", 20, PASADO}, //2		07/09/15
            {"BD6", 20, PASADO}, //3		07/09/15
            {"BD7", 20, PASADO}, //4		07/09/15
            {"BDR56", 20, PASADO}, //5		07/09/15
            {"BDR57", 20, PASADO}, //6		07/09/15
      });
   }

   public MineCEDFESuperModeloTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_SUPER, Modes.MODE_EPISODE, collection, window, skip);
   }

}