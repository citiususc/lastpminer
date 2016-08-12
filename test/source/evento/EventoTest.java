package source.evento;

import org.junit.Test;
import static org.junit.Assert.*;


public class EventoTest {

   @Test
   public void testComparableImplementation(){
      Evento e1 = new Evento("A", 22);
      Evento e2 = new Evento("A", 22);

      assertEquals(0, e1.compareTo(e2));
      assertEquals(0, e2.compareTo(e1));

      Evento e3 = new Evento("A",23);
      assertEquals(-1, e1.compareTo(e3));
      assertEquals(1, e3.compareTo(e1));

      Evento e4 = new Evento("B", 22);
      assertEquals(-1, e1.compareTo(e4));
      assertEquals(1, e4.compareTo(e1));

   }
}
