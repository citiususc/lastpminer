package source.busqueda.concurrente.semilla;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import source.AllThatYouNeedSAHS;
import source.Principal;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.Patron;

public class ConcurrenteSemillaConjuncionTest {

   @Test public void testBorradosPorSemilla(){
      Patron.setPrintID(false);
      AllThatYouNeedSAHS capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.params.setWindowSize(80);
      capsula.params.setTamMaximoPatron(3);
      capsula.params.setSaveRemovedEvents(true);

      capsula.mineria();

      AllThatYouNeedSAHS capsula2 = new AllThatYouNeedSAHS();
      capsula2.params.setAlgorithm(Algorithms.ALG_CON);
      capsula2.params.setMode(Modes.MODE_SEED);
      capsula2.params.setWindowSize(80);
      capsula2.params.setTamMaximoPatron(3);
      capsula2.params.setSaveRemovedEvents(true);

      capsula2.mineria();

      assertTrue("No se han borrado los mismos eventos", Principal.compararEventosBorrados(capsula.eventosEliminados, capsula2.eventosEliminados));
   }
}
