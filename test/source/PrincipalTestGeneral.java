package source;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;

@RunWith(Parameterized.class)
public class PrincipalTestGeneral {
   private static final Logger LOGGER = Logger.getLogger(PrincipalTestGeneral.class.getName());
   public final static boolean PASADO = true;
   public final static boolean IGNORAR = true;

   @Parameters
   public static Collection<Object[]> data(){
      boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //0
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, "BDR56", 20, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO},
            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            //hstp
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //5
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "BDR56", 20, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, "apnea", 80, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO},
            //parallel astp
            {Algorithms.ALG_PAR, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //10
            //parallel hstp
            {Algorithms.ALG_HPAR, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //11
            {Algorithms.ALG_HPAR, Modes.MODE_EPISODE, "BDR56", 20, PASADO}, // TODO problemas de sincronización: falla a veces
            {Algorithms.ALG_HPAR, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_HPAR, Modes.MODE_BASIC, "BDRoE15", 2, PASADO},

            //concurrent astp
            {Algorithms.ALG_CON, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //15
            //concurrent Hstp
            {Algorithms.ALG_HCON, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //16
            {Algorithms.ALG_HCON, Modes.MODE_EPISODE, "BDR56", 20, PASADO},
            {Algorithms.ALG_HCON, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_HCON, Modes.MODE_BASIC, "BDRoE15", 2, PASADO},
            // window marking
            {Algorithms.ALG_WM, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //20
            // lazy
            {Algorithms.ALG_LAZY, Modes.MODE_BASIC, "BDRoE6", 20, IGNORAR}, //21
            // IM
            {Algorithms.ALG_IM, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //
            // hom
            {Algorithms.ALG_HOM, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //Express
            {Algorithms.ALG_EXP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //
            {Algorithms.ALG_EXP, Modes.MODE_BASIC, "BDRoE6", 40, PASADO}, //25
            {Algorithms.ALG_EXP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //Save express
            {Algorithms.ALG_SAVEXP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //
            {Algorithms.ALG_SAVEXP, Modes.MODE_BASIC, "BDRoE6", 40, IGNORAR}, //28 PASADO
            {Algorithms.ALG_SAVEXP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            // super
            {Algorithms.ALG_SUPER, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //30
            {Algorithms.ALG_SUPER, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //Super express
            {Algorithms.ALG_SMEXP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //
            {Algorithms.ALG_SMEXP, Modes.MODE_BASIC, "BDRoE6", 40, PASADO}, //
            {Algorithms.ALG_SMEXP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //SUPER save andexpress
            {Algorithms.ALG_SMSAVEXP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //35
            {Algorithms.ALG_SMSAVEXP, Modes.MODE_BASIC, "BDRoE6", 40, PASADO}, //
            {Algorithms.ALG_SMSAVEXP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //MARKT
            {Algorithms.ALG_MARKT, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //
            {Algorithms.ALG_MARKT, Modes.MODE_EPISODE, "BDR56", 20, PASADO},//
            {Algorithms.ALG_MARKT, Modes.MODE_BASIC, "BDRoE6", 20, PASADO}, //40
            {Algorithms.ALG_MARKT, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

            //tstp
            {Algorithms.ALG_TSTP, Modes.MODE_EPISODE, "BD4", 20, PASADO}, //42
            {Algorithms.ALG_TSTP, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
            {Algorithms.ALG_TSTP, Modes.MODE_EPISODE, "BDR56", 20, PASADO},
            {Algorithms.ALG_TSTP, Modes.MODE_BASIC, "BDRoE15", 2, PASADO}, //

      });
   }

   protected Algorithms algorithm;
   protected Modes mode;
   protected Integer window = -1;
   protected String collection = null;
   protected boolean skip;
   protected boolean ignore;
   protected boolean completeResult = true;
   protected int tamMaximoPatron = -1;
   protected boolean soloPrimeraSecuencia = false;
   protected boolean savePatternInstances = false;
   protected boolean writeStatistics = false;
   protected List<List<IAsociacionTemporal>> resultados;
   protected boolean compararConFichero = true;
   protected Double currentPercentage = null;
   protected Double maximumPercentage = null;

   public PrincipalTestGeneral(Algorithms algorithm, Modes mode, String collection,
         Integer window, boolean skip){
      this.algorithm = algorithm;
      this.mode = mode;
      this.collection = collection;
      this.window = window;
      this.skip = skip;
      //this.ignore = ignore == null || ignore.length==0 ? false : ignore[0];
   }

   public String toString(){
      ConfigurationParameters params = new ConfigurationParameters();
      params.setAlgorithm(algorithm);
      params.setMode(mode);
      return toString(params);
   }

   public String toString(ConfigurationParameters params){
      return "Test del algoritmo " + params.getAlgorithmString()
            + " en modo " + params.getModeString()
            + " con la colección " + collection
            + " y win = " + window;
   }

   public AllThatYouNeed createCapsula(){
      AllThatYouNeed capsula;
      if(ConfigurationParameters.APNEA_DB.equals(collection)){
         capsula = new AllThatYouNeedSAHS(this.soloPrimeraSecuencia);
      }else{
         capsula = new AllThatYouNeedSinteticas(collection);
      }
      capsula.params.setAlgorithm(algorithm);
      capsula.params.setWindowSize(window);
      capsula.params.setMode(mode);
      capsula.params.setSavePatternInstances(false);
      capsula.params.setCompleteResult(completeResult);
      capsula.params.setTamMaximoPatron(tamMaximoPatron);
      capsula.params.setSavePatternInstances(savePatternInstances);
      if(currentPercentage != null){
         capsula.params.setCurrentPercentage(currentPercentage);
      }
      if(maximumPercentage != null){
         capsula.params.setMaximumPercentage(maximumPercentage);
      }
      return capsula;
   }

   @Test public void test(){
      Assume.assumeFalse("Se salta el test " + this, skip);
      Assume.assumeFalse("Test ignorado! " + this, ignore);

      Patron.setPrintID(false);
      AllThatYouNeed capsula = createCapsula();
      String file = capsula.mineria();
      if(writeStatistics){
         LOGGER.info("Se escriben las estadísticas de ejecución");
         capsula.escribirEstadisticas(1);
      }
      String referencia = CapsulaEjecucion.getFicheroValidacion(capsula.params);
      boolean sonIguales =  completeResult? Principal.compararFicheros(referencia, file) : Principal.compararFicherosFinal(referencia, file);
      if(sonIguales){ //borrar el fichero
         new File(file).delete();
      }
      if(compararConFichero){
         Assert.assertTrue("Falla el " + toString(capsula.params),sonIguales);
      }
      resultados = capsula.resultados;
   }

}
