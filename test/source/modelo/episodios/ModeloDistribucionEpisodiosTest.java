package source.modelo.episodios;

import java.util.Arrays;

import org.junit.Test;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.distribucion.ModeloDistribucion;

/**
 * Tests para comprobar como funciona
 * @author vanesa.graino
 *
 */
public class ModeloDistribucionEpisodiosTest {
   /**
    * Test que comprueba que un modelo con episodios construye correctamente
    * la distribución de distancias temporales
    */
   @Test
   public void testDistribucion(){
      int ventana = 10, sid = 0;
      boolean savePatternInstances = false;
      ModeloDistribucionEpisodios modAB = new ModeloDistribucionEpisodios(new String[]{"A", "B"},
            Arrays.asList(new Episodio("A", "B")), ventana, 0, null);

      ModeloDistribucion modAB2 = new ModeloDistribucion(new String[]{"A", "B"},
            ventana, 0, null);

      ModeloDistribucionEpisodios modBC = new ModeloDistribucionEpisodios(new String[]{"B", "C"},
            Arrays.asList(new Episodio("A", "B")), ventana, 0, null);


      //

      Evento ev = new Evento("A",1);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);

      ev = new Evento("A",3);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);

      ev = new Evento("B",5);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);
      modBC.recibeEvento(sid, ev, savePatternInstances);

      ev = new Evento("A",6);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);

      ev = new Evento("B",8);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);
      modBC.recibeEvento(sid, ev, savePatternInstances);

      ev = new Evento("B",10);
      modAB.recibeEvento(sid, ev, savePatternInstances);
      modAB2.recibeEvento(sid, ev, savePatternInstances);
      modBC.recibeEvento(sid, ev, savePatternInstances);

      System.out.println("Distribucion (con episodios): " + Arrays.toString(modAB.getDistribucion()));
      System.out.println("Distribucion (sin episodios): " + Arrays.toString(modAB2.getDistribucion()));
   }
}
