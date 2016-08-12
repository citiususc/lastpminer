package source.busqueda.jerarquia;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class MineMarcarTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO},
            {"BDRoE6", 60, PASADO},
            {"BDRoE6", 80, PASADO},
            {"BDRoE15", 8, false}, //heap overflow
            {"BDRoE15", 10, IGNORAR},//heap overflow
      });
   }

   public MineMarcarTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_MARK, Modes.MODE_BASIC, collection, window, skip);
   }


}
