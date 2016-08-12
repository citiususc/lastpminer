package source;

import org.junit.Assert;
import org.junit.Test;

import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;

public class TestOcurrenciasRepetidasPatron {

   private class TestOcurrenciasRepetidasPatronInt {
      /*
       * Atributos
       */

      AllThatYouNeed capsula;
      String[] mod;
      int indicePatron = 0;

      /*
       * Constructores
       */

      public TestOcurrenciasRepetidasPatronInt(AllThatYouNeed capsula, String[] mod) {
         this.capsula = capsula;
         this.mod = mod;
      }

      /*
       * Métodos
       */


      public void test(){
         Assert.assertNotNull("capsula cannot be null", capsula);
         Assert.assertNotNull("mod cannot be null", mod);

         // Asegurar tamaño maximo patron fijado
         capsula.params.setTamMaximoPatron(mod.length);
         capsula.params.setSavePatternInstances(true);

         // Ejecutar cápsula
         capsula.mineria();

         //Obtener asociación y patrón
         IAsociacionTemporal asoc = PrincipalTest.getAsociacion(capsula.resultados, mod);
         Assert.assertNotNull("No se encuentra la asociacion en los primeros resultados", asoc);

         Patron p = asoc.getPatron(indicePatron);
         Assert.assertNotNull("No se encuentra el patrón en los primeros resultados", p);

         Assert.assertFalse("Hay ocurrencias repetidas", PrincipalTest.ocurrenciasRepetidas(p));

      }
   }

   @Test
   public void testOcurrencias(){
      AllThatYouNeed capsula1 = new AllThatYouNeedSAHS();
      capsula1.params.setMode(Modes.MODE_FULL);

      TestOcurrenciasRepetidasPatron.TestOcurrenciasRepetidasPatronInt test = new TestOcurrenciasRepetidasPatron.TestOcurrenciasRepetidasPatronInt(capsula1, new String[]{"fA", "fD", "iA", "iD"});
      test.test();

   }
}
