package source.busqueda.semilla;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.excepciones.SemillasNoValidasException;


public class NegacionSemillaTest {


   @Test
   public void testSemilla() throws SemillasNoValidasException{
      int win = 80;
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_NEG);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(win);
      capsula.params.setSaveRemovedEvents(true);
      capsula.mineriaFicheros();
   }
}
