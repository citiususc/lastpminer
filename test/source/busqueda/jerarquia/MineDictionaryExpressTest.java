package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.PatronDictionaryFinalEvent;


public class MineDictionaryExpressTest {

   @Test
   public void testGeneral(){
      Collection<Object[]> data =Arrays.asList(new Object[][] {
            {Algorithms.ALG_EXP, Modes.MODE_BASIC, "apnea", 80, false}, //0
            {Algorithms.ALG_EXP, Modes.MODE_BASIC, "BDRoE6", 20, false}, //1
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }
   }

   private class PatronExp extends PatronDictionaryFinalEvent {
      protected PatronExp(int id, List<Integer> padres) {
         this(id);
         for(Integer padre : padres){
            this.padres.add(new PatronExp(padre));
         }
      }
      protected PatronExp(int id) {
         super(new String[]{"a"}, false);
         this.patternID = id;
      }
      public String toString(){
         return Integer.toString(patternID);
      }
      public boolean equals(Object obj){
         return ((PatronExp)obj).patternID == this.patternID;
      }
   }

   @Test
   public void testPurge(){
      MineDictionaryExpress mine = new MineDictionaryExpress("", true, true, true, null, false);
      mine.iniciarContadores(5,0);
      PatronDictionaryFinalEvent p1 = new PatronExp(1, Arrays.asList(2,3,4,6));
      PatronDictionaryFinalEvent p2 = new PatronExp(2, Arrays.asList(2,5,7,8));
      PatronDictionaryFinalEvent p3 = new PatronExp(3, Arrays.asList(1,2,4,5));

      List<Integer> anotaciones = Arrays.asList(1,2,4,5,7,8);

      List<PatronDictionaryFinalEvent> resultado = mine.purgaAnotaciones(
            new ArrayList<PatronDictionaryFinalEvent>(Arrays.asList(p1,p2,p3)), anotaciones, 4);

      System.out.println(resultado);
      Assert.assertFalse("No se ha borrado p1", resultado.contains(p1));
      Assert.assertTrue("Se ha borrado p2", resultado.contains(p2));
      Assert.assertTrue("Se ha borrado p3", resultado.contains(p3));

   }

}
