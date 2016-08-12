package source.modelo.negacion;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class HelperModeloNegacionTest {

   @Test
   public void testCombinablesPrefijo(){

      // Son combinables, sólo tienen parte positiva
      List<String[]> sol = HelperModeloNegacion.combinablesPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A","C"}, new String[0]);
      Assert.assertArrayEquals(sol.get(0), new String[]{"A","B","C"});
      Assert.assertArrayEquals(sol.get(1), new String[]{});

      // Son combinables, los dos tienen sólo parte negativa
      List<String[]> sol2 = HelperModeloNegacion.combinablesPrefijo(
            new String[0], new String[]{"A","B"},
            new String[0], new String[]{"A","C"});
      Assert.assertArrayEquals(sol2.get(0), new String[0]);
      Assert.assertArrayEquals(sol2.get(1), new String[]{"A","B","C"});


      // NO son combinables, sólo el segundo tiene parte negativa
      List<String[]> sol3 = HelperModeloNegacion.combinablesPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A"}, new String[]{"B"});
      Assert.assertNull(sol3);

      // Son combinables, sólo el 2 tiene parte negativa
      List<String[]> sol4 = HelperModeloNegacion.combinablesPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A"}, new String[]{"C"});
      Assert.assertArrayEquals(sol4.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol4.get(1), new String[]{"C"});


      // No son combinables, solo el 2 tiene parte negativa
      List<String[]> sol5 = HelperModeloNegacion.combinablesPrefijo(
            new String[]{"A","B", "C"}, new String[0],
            new String[]{"A","B"}, new String[]{"C"});
      Assert.assertNull(sol5);

      // Son combinables, los dos tienen parte negativa
      List<String[]> sol6 = HelperModeloNegacion.combinablesPrefijo(
            new String[]{"A","B"}, new String[]{"C"},
            new String[]{"A","B"}, new String[]{"D"});
      Assert.assertArrayEquals(sol6.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol6.get(1), new String[]{"C","D"});

   }

   @Test
   public void testCombinarPrefijo(){

      // Son combinables, sólo tienen parte positiva
      List<String[]> sol = HelperModeloNegacion.combinarPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A","C"}, new String[0]);
      Assert.assertArrayEquals(sol.get(0), new String[]{"A","B","C"});
      Assert.assertArrayEquals(sol.get(1), new String[]{});

      // Son combinables, los dos tienen sólo parte negativa
      List<String[]> sol2 = HelperModeloNegacion.combinarPrefijo(
            new String[0], new String[]{"A","B"},
            new String[0], new String[]{"A","C"});
      Assert.assertArrayEquals(sol2.get(0), new String[0]);
      Assert.assertArrayEquals(sol2.get(1), new String[]{"A","B","C"});

      // NO son combinables, sólo el segundo tiene parte negativa
      List<String[]> sol3 = HelperModeloNegacion.combinarPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A"}, new String[]{"B"});
      Assert.assertArrayEquals(sol3.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol3.get(1), new String[]{"B"});

      // Son combinables, sólo el 2 tiene parte negativa
      List<String[]> sol4 = HelperModeloNegacion.combinarPrefijo(
            new String[]{"A","B"}, new String[0],
            new String[]{"A"}, new String[]{"C"});
      Assert.assertArrayEquals(sol4.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol4.get(1), new String[]{"C"});


      // No son combinables, solo el 2 tiene parte negativa
      List<String[]> sol5 = HelperModeloNegacion.combinarPrefijo(
            new String[]{"A","B", "C"}, new String[0],
            new String[]{"A","B"}, new String[]{"C"});
      Assert.assertArrayEquals(sol5.get(0), new String[]{"A","B","C"});
      Assert.assertArrayEquals(sol5.get(1), new String[]{"C"});


      // Son combinables, los dos tienen parte negativa
      List<String[]> sol6 = HelperModeloNegacion.combinarPrefijo(
            new String[]{"A","B"}, new String[]{"C"},
            new String[]{"A","B"}, new String[]{"D"});
      Assert.assertArrayEquals(sol6.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol6.get(1), new String[]{"C","D"});

   }

   @Test
   public void testCombinarSufijo(){
      String suf = IAsociacionConNegacion.SUF_NEG;

      // Son combinables, sólo tienen parte positiva
      List<String[]> sol = HelperModeloNegacion.combinarSufijo(
            new String[]{"A","B"}, new String[0],
            "C");
      Assert.assertArrayEquals(sol.get(0), new String[]{"A","B","C"});
      Assert.assertArrayEquals(sol.get(1), new String[]{});

      // Son combinables, los dos tienen sólo parte negativa
      List<String[]> sol2 = HelperModeloNegacion.combinarSufijo(
            new String[0], new String[]{"A","B"},
            "C" + suf);
      Assert.assertArrayEquals(sol2.get(0), new String[0]);
      Assert.assertArrayEquals(sol2.get(1), new String[]{"A","B","C"});


      // Son combinables, sólo el 2 tiene parte negativa
      List<String[]> sol4 = HelperModeloNegacion.combinarSufijo(
            new String[]{"A","B"}, new String[0],
            "C" + suf);
      Assert.assertArrayEquals(sol4.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol4.get(1), new String[]{"C"});

      // Son combinables, los dos tienen parte negativa
      List<String[]> sol6 = HelperModeloNegacion.combinarSufijo(
            new String[]{"A","B"}, new String[]{"C"},
            "D" + suf);
      Assert.assertArrayEquals(sol6.get(0), new String[]{"A","B"});
      Assert.assertArrayEquals(sol6.get(1), new String[]{"C","D"});

     // Son combinables, sólo el primero tiene parte negativa
      List<String[]> sol7 = HelperModeloNegacion.combinarSufijo(
            new String[]{"B"}, new String[]{"A"},
            "C");
      Assert.assertArrayEquals(sol7.get(0), new String[]{"B","C"});
      Assert.assertArrayEquals(sol7.get(1), new String[]{"A"});
   }
}
