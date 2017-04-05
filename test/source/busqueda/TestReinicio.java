package source.busqueda;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import source.AllThatYouNeed;
import source.CapsulaEjecucion;
import source.Principal;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.Patron;

public class TestReinicio extends PrincipalTestGeneral {


   private static final Logger LOGGER = Logger.getLogger(TestReinicio.class.getName());
   public final static boolean PASADO = true;
   public final static boolean IGNORAR = true;

   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //0
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "apnea", 80, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_SEED, "apnea", 80, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_FULL, "apnea", 80, PASADO}, //5

            {Algorithms.ALG_TSTP, Modes.MODE_BASIC, "apnea", 80, PASADO},
            {Algorithms.ALG_TSTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_TSTP, Modes.MODE_SEED, "apnea", 80, false},
            //{Algorithms.ALG_TSTP, Modes.MODE_FULL, "apnea", 80, false},

            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "apnea", 80, PASADO}, //8
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_SEED, "apnea", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_FULL, "apnea", 80, PASADO},

            {Algorithms.ALG_MARK, Modes.MODE_BASIC, "apnea", 80, PASADO}, //12
            {Algorithms.ALG_MARK, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_MARK, Modes.MODE_SEED, "apnea", 80, PASADO},
            //{Algorithms.ALG_MARK, Modes.MODE_FULL, "apnea", 80, PASADO},

            {Algorithms.ALG_SUPER, Modes.MODE_BASIC, "apnea", 80, PASADO}, //14
            {Algorithms.ALG_SUPER, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_SUPER, Modes.MODE_SEED, "apnea", 80, PASADO},
            //{Algorithms.ALG_SUPER, Modes.MODE_FULL, "apnea", 80, PASADO},


            {Algorithms.ALG_LESS, Modes.MODE_BASIC, "apnea", 80, PASADO}, //16
            {Algorithms.ALG_LESS, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_LESS, Modes.MODE_SEED, "apnea", 80, PASADO},
            //{Algorithms.ALG_LESS, Modes.MODE_FULL, "apnea", 80, PASADO},

            {Algorithms.ALG_LASTP, Modes.MODE_BASIC, "apnea", 80, PASADO}, //18
            {Algorithms.ALG_LASTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_LASTP, Modes.MODE_SEED, "apnea", 80, PASADO},
            //{Algorithms.ALG_LASTP, Modes.MODE_FULL, "apnea", 80, PASADO},

            {Algorithms.ALG_SASTP, Modes.MODE_BASIC, "apnea", 80, PASADO}, //20
            {Algorithms.ALG_SASTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //{Algorithms.ALG_SASTP, Modes.MODE_SEED, "apnea", 80, PASADO},
            //{Algorithms.ALG_SASTP, Modes.MODE_FULL, "apnea", 80, PASADO},


      });
   }


   public TestReinicio(Algorithms algorithm, Modes mode, String collection,
         Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
   }



   @Test public void test(){
      Assume.assumeFalse("Se salta el test " + this, skip);
      Assume.assumeFalse("Test ignorado! " + this, ignore);

      Patron.setPrintID(false);
      AllThatYouNeed capsula =  createCapsula();
      String fileDistribuciones = capsula.minarDistribuciones();
      LOGGER.info("Se escriben las distribuciones: " + fileDistribuciones);

      String file = capsula.reiniciarMineria(capsula.distribuciones());

      if(writeStatistics){
         LOGGER.info("Se escriben las estadísticas de ejecución");
         capsula.escribirEstadisticas(1);
      }
      String referencia = CapsulaEjecucion.getFicheroValidacion(capsula.params);
      //boolean sonIguales =  completeResult? Principal.compararFicheros(referencia, file) : Principal.compararFicherosFinal(referencia, file);
      boolean sonIguales =  Principal.compararFicherosFinal(referencia, file);
      if(sonIguales){ //borrar el fichero
         new File(file).delete();
      }
      if(compararConFichero){
         Assert.assertTrue("Falla el " + toString(capsula.params),sonIguales);
      }
      resultados = capsula.resultados;
   }

}
