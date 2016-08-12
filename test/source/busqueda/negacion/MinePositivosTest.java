package source.busqueda.negacion;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.AllThatYouNeedSinteticas;
import source.ComparacionPatrones;
import source.Principal;
import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.IColeccion;
import source.modelo.negacion.IAsociacionConNegacion;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatronNegacion;


/*@RunWith(Parameterized.class)
public class MinePositivosTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_NEG_POS,  Modes.MODE_BASIC, "apnea", 20, false}, //0
      });
   }

   public MinePositivosTest(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      this.tamMaximoPatron = 3; //TODO borrar
   }*/

   /*@Override
   public void test() {
      this.compararConFichero = false;
      super.test();

      //Si llega aquí se ha pasado el test y tenemos resultados fijados
      int nivel = 3, last = -1;

      for(int i=0; i<resultados.get(nivel-1).size();i++){
         IAsociacionConNegacion asoc = (IAsociacionConNegacion)resultados.get(nivel-1).get(i);
         if(!asoc.parteNegativa() || asoc.getTiposNegados().length<2){
            //Si no tiene parte negativa continuamos la siguiente asociación
            continue;
         }

         System.out.println("Comprobando modelo: " + asoc.toStringSinPatrones() );
         //Ultimo tipo omitido

         for(int j=last+1;j<resultados.get(nivel-2).size();j++){
            IAsociacionConNegacion subasoc = (IAsociacionConNegacion)resultados.get(nivel-2).get(j);
            if(subasoc.parteNegativa() && subasoc.partePositiva() &&
                  subasoc.getTipos()[0] == asoc.getTipos()[0] && subasoc.getTiposNegados()[0] == asoc.getTiposNegados()[0]){
               Assert.assertTrue(asoc.toStringSinPatrones() + " tiene soporte mayor que su subasociacion "
                     + subasoc.toStringSinPatrones(), asoc.getSoporte() <= subasoc.getSoporte());
               last = j;
               break;
            }
         }
         // Penultimo tipo omitido
         for(int j=last+1;j<resultados.get(nivel-2).size();j++){
            IAsociacionConNegacion subasoc = (IAsociacionConNegacion)resultados.get(nivel-2).get(j);
            if(subasoc.parteNegativa() && subasoc.partePositiva() &&
                  subasoc.getTipos()[0] == asoc.getTipos()[0] && subasoc.getTiposNegados()[0] == asoc.getTiposNegados()[1]){
               Assert.assertTrue(asoc.toStringSinPatrones() + " tiene soporte mayor que su subasociacion "
                     + subasoc.toStringSinPatrones(), asoc.getSoporte() <= subasoc.getSoporte());
               break;
            }
         }
      }
   }
}*/
public class MinePositivosTest {


   private static final Logger LOGGER = Logger.getLogger(MinePositivosTest.class.getName());

   //Test para comprar ocurrencias
   @Test public void testComparacionOcurrencias(){
      //Patron.setPrintID(false);
      AllThatYouNeed capsulaHstp = new AllThatYouNeedSAHS();
      capsulaHstp.params.setWindowSize(20);
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsulaHstp.params.setMode(Modes.MODE_BASIC);
      capsulaHstp.params.setMinFreq(30);
      capsulaHstp.params.setSavePatternInstances(true);
      capsulaHstp.params.setTamMaximoPatron(3);

      LOGGER.info("El tamaño máximo de patron a buscar es: " + capsulaHstp.params.getTamMaximoPatron());

      IColeccion coleccionOriginal = capsulaHstp.coleccion.clone();

      AllThatYouNeed capsulaNegPos = new AllThatYouNeedSAHS();
      capsulaNegPos.params = capsulaHstp.params.clonar();
      capsulaNegPos.params.setAlgorithm(Algorithms.ALG_NEG_POS);

      String fileHstp = capsulaHstp.mineria();
      //Patron.resetGenerator();

      String fileHpar = capsulaNegPos.mineria();
      LOGGER.info("Ficheros de hstp e hpar respectivamente:\n" + fileHstp + "\n"+fileHpar);

      //PrincipalTest.validarResultados(capsula.resultados, copiaCollecion);

      //PatronDictionaryFinalEvent p1 = (PatronDictionaryFinalEvent)PrincipalTest.getPatron(capsulaNegPos.resultados, 3, 56);
      Patron p1 = PrincipalTest.getAsociacion(capsulaNegPos.resultados, new String[]{"fA", "fD", "fF"}).getPatron(0);
      //PrincipalTest.ocurrenciaFalta(p1, coleccionOriginal);
      Assert.assertFalse(PrincipalTest.ocurrenciasRepetidas(p1));

      //PatronDictionaryFinalEvent p2 = (PatronDictionaryFinalEvent)PrincipalTest.getPatron(capsulaHstp.resultados, 3, 56);
      Patron p2 = PrincipalTest.getAsociacion(capsulaHstp.resultados, new String[]{"fA", "fD", "fF"}).getPatron(0);

      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(p1, p2);
      PrincipalTest.ocurrenciaFalta(p1, comp.getSoloA(), coleccionOriginal);
      PrincipalTest.ocurrenciaFalta(p2, comp.getSoloB(), coleccionOriginal);

      LOGGER.info("Comparacion patrones: " + comp);

      //Comparacion comp = AbstractMine.compararResultados(capsulaHstp.resultados, capsulaHpar.resultados);
      //LOGGER.info(comp.toString(false));
      //assertTrue(comp.sonIguales());
      assertTrue(Principal.compararFicheros(fileHstp, fileHpar));
   }

}
