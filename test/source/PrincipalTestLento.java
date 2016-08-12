package source;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class PrincipalTestLento extends PrincipalTestGeneral{


   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, "BD4", 80, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE6", 80, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE15", 4, false}, //no pasado
            //hstp
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD4", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE6", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BDR56", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE15", 4, false},//FIXME java heap space
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE15", 10, false},
            //parallel astp
            {Algorithms.ALG_PAR, Modes.MODE_BASIC, "BDRoE15", 4, false},//FIXME falla
            //parallel hstp
            {Algorithms.ALG_HPAR, Modes.MODE_EPISODE, "BD4", 80, PASADO},
            {Algorithms.ALG_HPAR, Modes.MODE_EPISODE, "BDR56", 80, PASADO},
            {Algorithms.ALG_HPAR, Modes.MODE_BASIC, "BDRoE6", 80, PASADO},
            {Algorithms.ALG_HPAR, Modes.MODE_BASIC, "BDRoE15", 4, false},
            //concurrent astp
            {Algorithms.ALG_CON, Modes.MODE_BASIC, "BDRoE15", 4, false},//FIXME falla
            //concurrent Hstp
            {Algorithms.ALG_HCON, Modes.MODE_EPISODE, "BD4", 80, false},
            {Algorithms.ALG_HCON, Modes.MODE_EPISODE, "BDR56", 80, false},
            {Algorithms.ALG_HCON, Modes.MODE_BASIC, "BDRoE6", 80, false},
            {Algorithms.ALG_HCON, Modes.MODE_BASIC, "BDRoE15", 4, false},//FIXME HEAP SPACE
            // window marking
            {Algorithms.ALG_WM, Modes.MODE_BASIC, "BDRoE6", 20, false}, //20,40,60,80
            // lazy
            {Algorithms.ALG_LAZY, Modes.MODE_BASIC, "BDRoE6", 60, false}
      });
   }
   public PrincipalTestLento(Algorithms algorithm, Modes mode, String collection,
         Integer window, boolean skip){
      super(algorithm, mode, collection, window, skip);
   }

}
