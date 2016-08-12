package source.busqueda.negacion;



import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;


@RunWith(Parameterized.class)
public class NegacionCompletaPruebaNivel2Test extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_NEG_TEST2,  Modes.MODE_BASIC, "apnea", 20, false}, //0
      });
   }

   public NegacionCompletaPruebaNivel2Test(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      this.savePatternInstances = true;
      this.compararConFichero = false;
      //this.soloPrimeraSecuencia = true; //TODO borrar
      //this.tamMaximoPatron = 4; //TODO borrar
   }

}

/*public class NegacionCompletaPruebaNivel2Test {
   @Test
   public void testTam1(){
      ConfigurationParameters params = new ConfigurationParameters();
      params.setCollection("apnea");
      CapsulaEjecucion cap = new CapsulaEjecucion(params, null);
      IColeccion col = cap.coleccion;
      int soporte = 0;
      for(ISecuencia seq : col){

         for(Evento ev : seq){
            if("fD".equals(ev.getTipo())){
               soporte++;
            }
         }
         System.out.println("Soporte al final de la secunecia: " + soporte);
      }
      Assert.assertEquals(12594, soporte);
   }
}*/