package source.busqueda.negacion;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;


@RunWith(Parameterized.class)
public class NegacionMineTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_NEG,  Modes.MODE_BASIC, "apnea", 80, true, false}, //0
            {Algorithms.ALG_NEG,  Modes.MODE_EPISODE, "apnea", 80, true, false}, //0
            {Algorithms.ALG_NEG,  Modes.MODE_BASIC, "apnea", 80, false, false}, //0
            {Algorithms.ALG_NEG,  Modes.MODE_EPISODE, "apnea", 80, false, false}, //0
      });
   }

   public NegacionMineTest(Algorithms algorithm, Modes mode,
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

   /*@Override
   public void test() {
      this.compararConFichero = false;
      super.test();

      //Si llega aquí se ha pasado el test y tenemos resultados fijados
      int nivel = 3, last = -1;

      for(int i=0; i<resultados.get(nivel-1).size();i++){
         IAsociacionConNegacion asoc = (IAsociacionConNegacion)resultados.get(nivel-1).get(i);
         if(!asoc.parteNegativa() || asoc.getTiposNegados().length<2){
            //Si no tiene parte negativa continuamos la siguiente asociación
            continue;
         }

         System.out.println("Comprobando modelo: " + asoc.toStringSinPatrones() );
         //Ultimo tipo omitido

         for(int j=last+1;j<resultados.get(nivel-2).size();j++){
            IAsociacionConNegacion subasoc = (IAsociacionConNegacion)resultados.get(nivel-2).get(j);
            if(subasoc.parteNegativa() && subasoc.partePositiva() &&
                  subasoc.getTipos()[0] == asoc.getTipos()[0] && subasoc.getTiposNegados()[0] == asoc.getTiposNegados()[0]){
               Assert.assertTrue(asoc.toStringSinPatrones() + " tiene soporte mayor que su subasociacion "
                     + subasoc.toStringSinPatrones(), asoc.getSoporte() <= subasoc.getSoporte());
               last = j;
               break;
            }
         }
         // Penultimo tipo omitido
         for(int j=last+1;j<resultados.get(nivel-2).size();j++){
            IAsociacionConNegacion subasoc = (IAsociacionConNegacion)resultados.get(nivel-2).get(j);
            if(subasoc.parteNegativa() && subasoc.partePositiva() &&
                  subasoc.getTipos()[0] == asoc.getTipos()[0] && subasoc.getTiposNegados()[0] == asoc.getTiposNegados()[1]){
               Assert.assertTrue(asoc.toStringSinPatrones() + " tiene soporte mayor que su subasociacion "
                     + subasoc.toStringSinPatrones(), asoc.getSoporte() <= subasoc.getSoporte());
               break;
            }
         }
      }
   }*/

   /*@Override
   public void test(){
      this.compararConFichero = false;
      super.test();

      IAsociacionTemporal asoc = PrincipalTest.getAsociacion(this.resultados, new String[]{"fA","fF"}, new String[]{"fD","iA"});
      Assert.assertNotNull("Non se atopou a asociación temporal", asoc);

      Patron p = asoc.getPatron(0);
      System.out.println("Patrón (1): #" + p.getID());

      Assert.assertFalse("Hai ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));

      // Another
      asoc = PrincipalTest.getAsociacion(this.resultados, new String[]{"fA","iA", "iT"}, new String[]{"iF"});
      Assert.assertNotNull("Non se atopou a asociación temporal", asoc);

      p = asoc.getPatron(0);
      System.out.println("Patrón (2) : #" + p.getID());

      Assert.assertFalse("Hai ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));


      // Another (125 ocurrencias)
      asoc = PrincipalTest.getAsociacion(this.resultados, new String[]{"fA","fD", "iT"}, new String[]{"fT","iF"});
      Assert.assertNotNull("Non se atopou a asociación temporal", asoc);

      p = asoc.getPatron(0);
      System.out.println("Patrón (3) : #" + p.getID());

      Assert.assertFalse("Hai ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));


//      // Another (64 ocurrencias)
//      asoc = PrincipalTest.getAsociacion(this.resultados, new String[]{"fA","fT", "iT"}, new String[]{"fT","iA"});
//      Assert.assertNotNull("Non se atopou a asociación temporal", asoc);
//
//      p = asoc.getPatron(0);
//      System.out.println("Patrón (4) : #" + p.getID());
//
//      Assert.assertFalse("Hai ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));


      // Another (41 ocurrencias)
      asoc = PrincipalTest.getAsociacion(this.resultados, new String[]{"fA","iD", "iT"}, new String[]{"fD","iA"});
      Assert.assertNotNull("Non se atopou a asociación temporal", asoc);

      p = asoc.getPatron(2);
      System.out.println("Patrón (5) : #" + p.getID());

      Assert.assertFalse("Hai ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));
   }*/

}



