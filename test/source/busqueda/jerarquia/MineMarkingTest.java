package source.busqueda.jerarquia;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.CapsulaEjecucion;
import source.Principal;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.Patron;

@RunWith(Parameterized.class)
public class MineMarkingTest {

   @Parameters
   public static Collection<Object[]> data(){
      List<Object[]> configuraciones = new ArrayList<Object[]>();
      for(int win=20;win<=80;win+=20){
         for(Algorithms alg : new Algorithms[]{Algorithms.ALG_IM, Algorithms.ALG_IM2}){
            configuraciones.add(new Object[]{alg, Modes.MODE_BASIC, "BDRoE6", win});
         }
      }
      for(int win=10;win<=40;win+=10){
         for(Algorithms alg : new Algorithms[]{Algorithms.ALG_IM}){
            configuraciones.add(new Object[]{alg, Modes.MODE_BASIC, "BDRoE9", win});
         }
      }
      for(int win : new int[]{2,4,6,8,10}){
         for(Algorithms alg : new Algorithms[]{Algorithms.ALG_IM}){
            configuraciones.add(new Object[]{alg, Modes.MODE_BASIC, "BDRoE15", win});
         }
      }
      return configuraciones;
   }

   private Algorithms algorithm;
   private Modes mode;
   private int win = -1;
   private String bbdd;

   public MineMarkingTest(Algorithms algorithm, Modes mode, String bbdd, int win){
      this.win = win;
      this.algorithm = algorithm;
      this.bbdd = bbdd;
      this.mode = mode;
   }

   @Test public void test(){
      //System.out.println("win:" + win + ", alg: " + algorithm );
      Patron.setPrintID(false);
      AllThatYouNeed capsula = new AllThatYouNeedSinteticas(bbdd);
      capsula.params.setAlgorithm(algorithm);
      capsula.params.setMode(mode);
      capsula.params.setWindowSize(win);
      //capsula.params.NUM_THREADS=1;

      String file = capsula.mineria();
      String referencia = CapsulaEjecucion.getFicheroValidacion(capsula.params);
      boolean sonIguales = Principal.compararFicheros(referencia, file);
      if(sonIguales){ //borrar el fichero
         new File(file).delete();
      }
      assertTrue("[bbdd=" + bbdd + "]. Falla para win = " + win
            + " con algoritmo " + capsula.params.getAlgorithmString()
            + " y modo " + capsula.params.getModeString(),sonIguales);
   }


}
