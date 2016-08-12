package source.modelo.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.evento.Evento;
import source.modelo.TestModelo;

@RunWith(Parameterized.class)
public class ModeloEventoNegadoTest extends TestModelo {

   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {

            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 0),
                     new Evento("B", 10))
               ),
               new ModeloEventoNegado("A", 0, 4),
               1
            },
            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 10),
                     new Evento("B", 10))
               ),
               new ModeloEventoNegado("B", 0, 4),
               0
            },
            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 9),
                     new Evento("A", 10),
                     new Evento("B", 10))
               ),
               new ModeloEventoNegado("B", 0, 4),
               1
            },
            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 9),
                     new Evento("A", 10),
                     new Evento("B", 10),
                     new Evento("C", 10),
                     new Evento("D", 10))
               ),
               new ModeloEventoNegado("B", 0, 4),
               1
            }
      });
   }


   public ModeloEventoNegadoTest(List<Evento> eventos,
         IAsociacionConNegacion modelo, int value) {
      super(eventos, modelo, value);
   }
}
