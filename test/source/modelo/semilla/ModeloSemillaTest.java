package source.modelo.semilla;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.Test;

import source.evento.Evento;
import source.patron.Patron;
import source.patron.PatronSemilla;
import source.restriccion.RIntervalo;



public class ModeloSemillaTest {

   @Test public void testClonar(){
         final boolean savePatternInstances = true;

         String[] tipos = new String[]{"A", "B", "C"};
         PatronSemilla ps = new PatronSemilla(tipos, Arrays.asList(new RIntervalo("A","B",0,5)), savePatternInstances);
         ModeloSemilla ms = new ModeloSemilla(tipos, 10, new ArrayList<Patron>(Arrays.asList(ps)), null);

         ModeloSemilla ms2 = ms.clonar();

         ms2.recibeEvento(0, new Evento("A",0), savePatternInstances);
         ms2.recibeEvento(0, new Evento("B",2), savePatternInstances);
         ms2.recibeEvento(0, new Evento("C",2), savePatternInstances);

         System.out.println("ms: " + ms.getSoporte() + ", ms2: " + ms2.getSoporte());
         System.out.println("ms. ultima encontrada: " + Arrays.toString(ms.ultimaEncontrada));
         System.out.println("ms2. ultima encontrada: " + Arrays.toString(ms2.ultimaEncontrada));
         System.out.println("ms: " + ms.getPatron(0).getOcurrencias().size() + ", ms2: " + ms2.getPatron(0).getOcurrencias().size());

         System.out.println("ms: " + Arrays.toString(ms.getDistribuciones()[0]));
         System.out.println("ms: " + Arrays.toString(ms.getDistribuciones()[1]));
         System.out.println("ms: " + Arrays.toString(ms.getDistribuciones()[2]));


         System.out.println("ms2: " + Arrays.toString(ms2.getDistribuciones()[0]));
         System.out.println("ms2: " + Arrays.toString(ms2.getDistribuciones()[1]));
         System.out.println("ms2: " + Arrays.toString(ms2.getDistribuciones()[2]));

         assertEquals(0, ms.getSoporte());
         assertEquals(0, ms.getPatron(0).getOcurrencias().size());
         assertEquals(0, ms.ultimaEncontrada[0]);

         assertEquals(1, ms2.getSoporte());
         assertEquals(1, ms2.getPatron(0).getOcurrencias().size());
      }

   @Test public void testAgregar(){
      final boolean savePatternInstances = true;
      String[] tipos = new String[]{"A", "B", "C"};

      PatronSemilla ps = new PatronSemilla(tipos, Arrays.asList(new RIntervalo("A","B",0,5)), savePatternInstances);
      ModeloSemilla ms = new ModeloSemilla(tipos, 10, new ArrayList<Patron>(Arrays.asList(ps)), null);

      ModeloSemilla ms2 = ms.clonar();

      ms2.recibeEvento(0, new Evento("A",0), savePatternInstances);
      ms2.recibeEvento(0, new Evento("B",2), savePatternInstances);
      ms2.recibeEvento(0, new Evento("C",2), savePatternInstances);

      ms.agregar(ms2);

      assertArrayEquals(ms.getDistribuciones()[0], ms2.getDistribuciones()[0]);
      assertArrayEquals(ms.getDistribuciones()[1], ms2.getDistribuciones()[1]);
      assertArrayEquals(ms.getDistribuciones()[2], ms2.getDistribuciones()[2]);

      assertEquals(1, ms.getSoporte());
      assertEquals(1, ms.getPatron(0).getOcurrencias().size());

      assertEquals(1, ms2.getSoporte());
      assertEquals(1, ms2.getPatron(0).getOcurrencias().size());
   }
}
