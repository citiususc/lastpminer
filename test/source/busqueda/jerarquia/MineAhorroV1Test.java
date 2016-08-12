package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;




//import source.Repeat;
import source.modelo.PatronPrueba;
import source.modelo.arbol.DictionaryUtilsTest;
import source.modelo.arbol.Supernodo;
import source.patron.GeneradorID;
import source.patron.Patron;


public class MineAhorroV1Test {
   Logger logger = Logger.getLogger(MineAhorroV1Test.class.getName());

   /**
    * Test que comprueba que dadas unas anotaciones se mantienen las extensibles y
    * se borra la que no lo es.
    */
   @Test
   //@Repeat( times = 1000000 )
   public void testUtiles(){
      logger.info("Iniciando test");

      int tam = 3, tmp = 3;
      String evento = "C";

      MineAhorroV1 mine = new MineAhorroV1("", true, true, true, null, false);

      List<Patron> encontrados = new ArrayList<Patron>(), ventanaActual = new ArrayList<Patron>();
      GeneradorID genID = new GeneradorID();
      PatronPrueba p1 = new PatronPrueba(genID, new String[]{"A","B","C"}, "P1");
      encontrados.add( p1 );
      PatronPrueba p2 = new PatronPrueba(genID, new String[]{"A","C","D"}, "P2");
      encontrados.add( p2 );
      PatronPrueba  p3 = new PatronPrueba(genID, new String[]{"B","C","D"}, "P3");
      encontrados.add( p3 );
      PatronPrueba p4 = new PatronPrueba(genID, new String[]{"C","D","E"}, "P4");
      encontrados.add( p4 );

      List<Supernodo> nivelActual = new ArrayList<Supernodo>();
      mine.raizArbol = DictionaryUtilsTest.generarArbol(Arrays.asList("A","B","C","D","E"), tam+1, nivelActual);
      mine.nivelActual = nivelActual;//no se utiliza
      mine.iniciarContadores(tam,0);
      mine.setAnotacionesEvento(encontrados, ventanaActual, evento, tmp, tam);

      System.out.println("Ventana actual: " + ventanaActual);
      Assert.assertTrue("Falta " + p1.toString(), ventanaActual.contains(p1)  );
      Assert.assertTrue("Falta " + p2.toString(), ventanaActual.contains(p2)  );
      Assert.assertTrue("Falta " + p3.toString(), ventanaActual.contains(p3)  );
      Assert.assertFalse("Sobra " + p4.toString(), ventanaActual.contains(p4)  );
   }


   @Test public void testUtilesConRepetidos(){
      logger.info("Iniciando test");

      int tam = 3, tmp = 3;
      String evento = "C";

      MineAhorroV1 mine = new MineAhorroV1("", true, true, true, null, false);

      List<Patron> encontrados = new ArrayList<Patron>(), ventanaActual = new ArrayList<Patron>();
      GeneradorID genID = new GeneradorID();
      PatronPrueba p1a = new PatronPrueba(genID, new String[]{"A","B","C"}, "P1a");
      encontrados.add( p1a );
      PatronPrueba p1b = new PatronPrueba(genID, new String[]{"A","B","C"}, "P1b");
      encontrados.add( p1b );
      PatronPrueba p2 = new PatronPrueba(genID, new String[]{"A","C","D"}, "P2");
      encontrados.add( p2 );
      PatronPrueba  p3 = new PatronPrueba(genID, new String[]{"B","C","D"}, "P3");
      encontrados.add( p3 );
      PatronPrueba p4 = new PatronPrueba(genID, new String[]{"C","D","E"}, "P4");
      encontrados.add( p4 );

      List<Supernodo> nivelActual = new ArrayList<Supernodo>();
      mine.raizArbol = DictionaryUtilsTest.generarArbol(Arrays.asList("A","B","C","D","E"), tam+1, nivelActual);
      mine.nivelActual = nivelActual;//no se utiliza
      mine.iniciarContadores(tam,0);
      mine.setAnotacionesEvento(encontrados, ventanaActual, evento, tmp, tam);

      System.out.println("Ventana actual: " + ventanaActual);
      Assert.assertTrue("Falta " + p1a.toString(), ventanaActual.contains(p1a)  );
      Assert.assertTrue("Falta " + p1b.toString(), ventanaActual.contains(p1b)  );
      Assert.assertTrue("Falta " + p2.toString(), ventanaActual.contains(p2)  );
      Assert.assertTrue("Falta " + p3.toString(), ventanaActual.contains(p3)  );
      Assert.assertFalse("Sobra " + p4.toString(), ventanaActual.contains(p4)  );
   }
}
