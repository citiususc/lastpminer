package source.evento;

import org.junit.Test;


public class DescripcionColeccionTest {


   @Test
   public void testGeneral(){
      SecuenciaSimple s1 = new SecuenciaSimple();
      s1.add(new Evento("A",2));
      s1.add(new Evento("B",2));
      s1.add(new Evento("A",3));
      s1.add(new Evento("B",5));
      s1.add(new Evento("C",5));
      s1.add(new Evento("D",5));

      ColeccionSimple col = new ColeccionSimple();
      col.add(s1);

      new DescripcionColeccion(col).printInfo();
   }
}
