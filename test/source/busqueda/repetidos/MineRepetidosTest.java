package source.busqueda.repetidos;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class MineRepetidosTest extends PrincipalTestGeneral {


    @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            { Modes.MODE_BASIC, ConfigurationParameters.APNEA_DB, 80, false }, //0

      });
   }

   public MineRepetidosTest(Modes mode, String collection,
             Integer window, boolean skip){
       super(Algorithms.ALG_REP, mode, collection, window, skip);
       tamMaximoPatron = 2; // TODO borrar
   }
}
