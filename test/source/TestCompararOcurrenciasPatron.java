package source;
import org.junit.Assert;
import org.junit.Test;

import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;

public class TestCompararOcurrenciasPatron {

   private class TestCompararOcurrenciasPatronInt {
      AllThatYouNeed capsula1, capsula2;
      String[] mod;
      int indicePatron = 0;

      public TestCompararOcurrenciasPatronInt(AllThatYouNeed capsula1, AllThatYouNeed capsula2, String[] mod) {
         this.capsula1 = capsula1;
         this.capsula2 = capsula2;
         this.mod = mod;
      }

      public void test(){
         Assert.assertNotNull("capsula1 cannot be null", capsula1);
         Assert.assertNotNull("capsula2 cannot be null", capsula2);
         Assert.assertNotNull("mod cannot be null", mod);

         IColeccion copiaColeccion = capsula1.coleccion.clone();

         //Asegurar tamaño maximo patron fijado
         int tamPatron = mod.length;
         capsula1.params.setTamMaximoPatron(tamPatron);
         capsula2.params.setTamMaximoPatron(tamPatron);
         capsula1.params.setSavePatternInstances(true);
         capsula2.params.setSavePatternInstances(true);

         //Ejecutar cápsula

         capsula1.mineria();
         capsula2.mineria();

         // TODO Obtener asociaciones
         IAsociacionTemporal asoc1 = PrincipalTest.getAsociacion(capsula1.resultados, mod);
         Assert.assertNotNull("No se encuentra la asociacion en los primeros resultados", asoc1);

         IAsociacionTemporal asoc2 = PrincipalTest.getAsociacion(capsula2.resultados, mod);
         Assert.assertNotNull("No se encuentra la asociacion en los segundos resultados", asoc2);

         // Obtener patrones y compara ocurrencias
         Patron p1 = asoc1.getPatron(indicePatron);
         Assert.assertNotNull("No se encuentra el patrón en los primeros resultados", p1);
         Patron p2 = asoc2.getPatron(indicePatron);
         Assert.assertNotNull("No se encuentra el patrón en los segundos resultados", p2);

         ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(p1, p2);

         Assert.assertTrue("No tienen las mismas ocurrencias", comp.sonIguales());


         Assert.assertFalse("Faltan ocurrencias", PrincipalTest.ocurrenciaFalta(p1, copiaColeccion));
      }
   }

   @Test
   public void test(){
      AllThatYouNeed capsula1 = new AllThatYouNeedSAHS();
      capsula1.params.setMode(Modes.MODE_FULL);

      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params.setMode(Modes.MODE_FULL);
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);

      String[] mod = new String[]{"fA", "fD", "iA", "iD"};

      TestCompararOcurrenciasPatron.TestCompararOcurrenciasPatronInt test = new TestCompararOcurrenciasPatron.TestCompararOcurrenciasPatronInt(capsula1, capsula2, mod);
      test.test();




   }
}
