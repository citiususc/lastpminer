package source;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.configuracion.Algorithms;
import source.configuracion.Modes;

/**
 * Tests que comprueban que las diferentes versiones de los algoritmos dan
 * el resultado esperado (tomando por referencia el obtenido por las versiones
 * originales del algoritmo ASTPminer).
 * @author vanesa.graino
 *
 */
@RunWith(Parameterized.class)
public class PrincipalTestSAHS extends PrincipalTestGeneral{
   //private static final Logger logger = Logger.getLogger(PrincipalTestSAHS.class.getName());

   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_FULL, PASADO},		//0
            {Algorithms.ALG_ASTP, Modes.MODE_SEED, false},		//1
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, PASADO},	//2
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, PASADO},		//3
            //hstp
            {Algorithms.ALG_HSTP, Modes.MODE_FULL, PASADO},		//4
            {Algorithms.ALG_HSTP, Modes.MODE_SEED, PASADO},		//5
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, PASADO},	//6
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, PASADO},		//7
            //parallel astp
            {Algorithms.ALG_PAR, Modes.MODE_FULL, PASADO},			//8
            {Algorithms.ALG_PAR, Modes.MODE_SEED, PASADO},			//9
            {Algorithms.ALG_PAR, Modes.MODE_EPISODE, PASADO},		//10
            {Algorithms.ALG_PAR, Modes.MODE_BASIC, PASADO},		//11
            //parallel hstp
            {Algorithms.ALG_HPAR, Modes.MODE_EPISODE, PASADO},	//12
            {Algorithms.ALG_HPAR, Modes.MODE_BASIC, PASADO},		//13
            // interval marking
            {Algorithms.ALG_IM, Modes.MODE_SEED, PASADO},			//14
            {Algorithms.ALG_IM, Modes.MODE_BASIC, PASADO},			//15
            // lazy
            {Algorithms.ALG_LAZY, Modes.MODE_BASIC, PASADO},		//16
            // concurrent astp
            {Algorithms.ALG_CON, Modes.MODE_FULL, IGNORAR},			//17
            {Algorithms.ALG_CON, Modes.MODE_SEED, PASADO},			//18
            {Algorithms.ALG_CON, Modes.MODE_EPISODE, IGNORAR},		//19
            {Algorithms.ALG_CON, Modes.MODE_BASIC, PASADO},		//20
            // concurrent hstp
            {Algorithms.ALG_HCON, Modes.MODE_EPISODE, IGNORAR},	//21
            {Algorithms.ALG_HCON, Modes.MODE_BASIC, PASADO},		//22
            {Algorithms.ALG_HCON, Modes.MODE_SEED, PASADO},		//23 problemas de carrera se pierden instancias en tamaño 3
            {Algorithms.ALG_HCON, Modes.MODE_FULL, IGNORAR},		//24
            // om y hom
            {Algorithms.ALG_OM, Modes.MODE_BASIC, PASADO}, 		//25
            {Algorithms.ALG_HOM, Modes.MODE_BASIC, PASADO}, 		//26
            // wm
            {Algorithms.ALG_WM, Modes.MODE_BASIC, PASADO},			//27
            // super
            {Algorithms.ALG_SUPER, Modes.MODE_BASIC, PASADO}, 		//28
            {Algorithms.ALG_SUPER, Modes.MODE_EPISODE, PASADO}, 	//29
            {Algorithms.ALG_SUPER, Modes.MODE_SEED, IGNORAR}, 		//30
            {Algorithms.ALG_SUPER, Modes.MODE_FULL, IGNORAR}, 		//31
            // mark
            {Algorithms.ALG_MARK, Modes.MODE_BASIC, PASADO}, 		//32
            //super save and express
            {Algorithms.ALG_SMSAVEXP, Modes.MODE_BASIC, PASADO}, //33

            //tstp
            {Algorithms.ALG_TSTP, Modes.MODE_FULL, IGNORAR },		//34
            {Algorithms.ALG_TSTP, Modes.MODE_SEED, IGNORAR },		//35
            {Algorithms.ALG_TSTP, Modes.MODE_EPISODE, PASADO },	//36
            {Algorithms.ALG_TSTP, Modes.MODE_BASIC, PASADO },		//37

      });
   }

   public PrincipalTestSAHS(Algorithms algorithm, Modes mode, boolean skip){
      super(algorithm, mode, "apnea", 80, skip);
   }

}
