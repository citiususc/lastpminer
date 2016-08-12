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
public class MineEpisodesTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_ASTP,  Modes.MODE_EPISODE, "apnea", 80, false, false},
      });
   }

   public MineEpisodesTest(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean per, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      //this.tamMaximoPatron = 4; //TODO borrar
      //this.savePatternInstances = true;
      if(per){
         this.currentPercentage = 0.2;
         this.maximumPercentage = 0.4;
      }
      this.compararConFichero = false;
      this.writeStatistics = true;
   }
}