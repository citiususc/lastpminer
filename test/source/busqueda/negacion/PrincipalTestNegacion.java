package source.busqueda.negacion;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.modelo.negacion.IAsociacionConNegacion;

@RunWith(Parameterized.class)
public class PrincipalTestNegacion extends PrincipalTestGeneral {

   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_NEG, Modes.MODE_EPISODE, "BD4", 20, false}, //
      });
   }

   public PrincipalTestNegacion(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      this.compararConFichero = false;
      this.tamMaximoPatron = 3;
   }

   @Override
   @Test public void test(){
      super.test();

      // Buscar en los resultados que los patrones que tienen negación
      // no tiene mayor frecuencia que su subpatrones positivos

      for(int i = 1; i<resultados.size(); i++){
         // TODO
      }
   }

   /**
    *
    * @param target
    * @param nivel - Anterior nivel a target
    * @return
    */
   private List<IAsociacionTemporal> subasociaciones(IAsociacionConNegacion target, List<IAsociacionConNegacion> nivel){
      //TODO utilizar el arbol??
      return null;
   }

}
