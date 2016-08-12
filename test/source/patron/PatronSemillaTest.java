package source.patron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.restriccion.RIntervalo;



public class PatronSemillaTest {

   @Test public void testSimple(){
      String[] tipos = new String[]{"fA", "fD", "fF", "fT", "iA", "iD", "iF", "iT"};
      boolean savePatternInstances = true;
      List<RIntervalo> restricciones = new ArrayList<RIntervalo>(Arrays.asList(
            new RIntervalo("fA", "fD", 10, 20)
      ));
      //PatronSemilla p = new PatronSemilla(getParseResult().getEventTypes(), p.getRestricciones(), SAVE_PATTERN_INSTANCES);
      new PatronSemilla(tipos, restricciones, savePatternInstances);
   }

   @Test public void testClonar(){
         String[] tipos = new String[]{"A", "B", "C"};
         PatronSemilla ps = new PatronSemilla(tipos, Arrays.asList(new RIntervalo("A","B",0,5)), true);
         ps.clonar();

      }
}
