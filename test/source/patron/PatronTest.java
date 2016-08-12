package source.patron;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.CapsulaEjecucion;
import source.Principal;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.restriccion.RIntervalo;


public class PatronTest{
   private static final Logger LOGGER = Logger.getLogger(PatronTest.class.getName());

   @Ignore
   @Test
   public void test(){
      List<String> tipos = new ArrayList<String>(Arrays.asList("iA","iD","fA","fD"));
      Collections.sort(tipos);
      int[][] a =	{{0,		20,	-20,	8},
                     {-10,		0,		5,		0},
                     {30,		2,		0,		60},
                     {-30,		1,		60,	0}};
      Patron p = new Patron((String[])tipos.toArray(), a, true);

      List<String> tipos2 = new ArrayList<String>(Arrays.asList("iA","iD","iF","iT", "fA","fD","fF","fT"));
      Collections.sort(tipos2);

      PatronSemilla ps = new PatronSemilla((String[])tipos2.toArray(), p.getRestricciones(), true);
      LOGGER.info("Semilla: " + ps);
      LOGGER.info("Semilla inf:" + PatronSemilla.NEGATIVE_INFINITY );
      LOGGER.info("Patron inf:" + Patron.NEGATIVE_INFINITY );

      LOGGER.info(Boolean.toString(-PatronSemilla.POSITIVE_INFINITY == PatronSemilla.NEGATIVE_INFINITY)) ;
      LOGGER.info(Boolean.toString(-Patron.POSITIVE_INFINITY == Patron.NEGATIVE_INFINITY)) ;



      Patron p3 = new Patron((String[])tipos.toArray(), Arrays.asList(new RIntervalo("iA","fA",10,20)), true);
      LOGGER.info("Patron a secas: " + p3);

   }


   @Ignore
   @Test
   public void testEsConsistente(){
      String[] tipos = new String[]{"A", "B", "C", "D"};

      List<RIntervalo> rests = new ArrayList<RIntervalo>();
      rests.add(new RIntervalo("A", "B", -1, -1));
      rests.add(new RIntervalo("A", "C",  1,  1));
      rests.add(new RIntervalo("A", "D",  0,  1));
      rests.add(new RIntervalo("B", "C", -2, -2));
      rests.add(new RIntervalo("B", "D", -1,  0));
      rests.add(new RIntervalo("C", "D",  1,  2));
      Patron p = new Patron(tipos, rests, false);
      boolean consistente = p.esConsistente(new GeneradorID());
      LOGGER.info("Consistente: " + consistente);
      Assert.assertFalse(consistente);

   }


   @Test
   public void testBorrado(){
      Patron.setPrintID(false);
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setWindowSize(80);
      capsula.params.setMode(Modes.MODE_BASIC);
      capsula.params.setSavePatternInstances(false);
      capsula.params.setCompleteResult(false);
      String file = capsula.mineria();
      String referencia = CapsulaEjecucion.getFicheroValidacion(capsula.params);
      boolean sonIguales =  Principal.compararFicheros(referencia, file);
      if(sonIguales){ //borrar el fichero
         new File(file).delete();
      }
      Assert.assertTrue("Falla",sonIguales);
   }

   @Test
   public void testDeleteOccurrences(){
      String[] tipos = new String[]{"A", "B"};
      List<RIntervalo> rests = new ArrayList<RIntervalo>();
      rests.add(new RIntervalo("A", "B", -1, -1));
      Patron p = new Patron(tipos, rests, true);
      for(int i=1;i<20;i++){
         p.addOcurrencia(0, new int[]{i,i+1});
      }
      Assert.assertEquals("Patron. # ocurrencias debería ser 19 antes del borrado", 19, p.getOcurrencias().size());

      p.deteleOcurrencias(5);
      Assert.assertEquals("Patron. # ocurrencias debería ser 14 después del borrado: ", 14, p.getOcurrencias().size());
   }

}