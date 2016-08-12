package source.modelo.negacion;

import org.junit.Assert;
import org.junit.Test;

import source.evento.Evento;

/**
 *
 * @author vanesa.graino
 *
 */
public class ModeloEventoNegadoSimpleTest {

   @Test
   public void testCalculoFrecuencia(){
      int ventana = 3, sid = 0;
      boolean savePatternInstances = false;
      ModeloEventoNegado mod = new ModeloEventoNegado("A", 0, ventana);

      Evento evento;

      evento = new Evento("A",1);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 0, mod.getSoporte());

      // No se suma si el evento negado está en la ventana
      evento = new Evento("B",2);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 0, mod.getSoporte());

      // No se suma si el evento negado está en la ventana
      evento = new Evento("C",3);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 0, mod.getSoporte());

      // Se suma ocurrencia cuando sale el evento negado de la ventana
      evento = new Evento("C",5);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 1, mod.getSoporte());

      // Se sigue sumando ocurrencias
      evento = new Evento("B",7);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 2, mod.getSoporte());

      // No se suman un instante dos veces
      evento = new Evento("C",7);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 2, mod.getSoporte());

      // No se suma al entrar el evento negado en la ventana ni se resta si no está en el mismo instante
      evento = new Evento("A",9);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 2, mod.getSoporte());

      evento = new Evento("B",9);
      mod.recibeEvento(sid, evento, savePatternInstances);

      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 2, mod.getSoporte());

      //Se resta si en el mismo instante aparece el evento negado
      evento = new Evento("B",19);
      mod.recibeEvento(sid, evento, savePatternInstances);
      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 3, mod.getSoporte());
      evento = new Evento("A",19);
      mod.recibeEvento(sid, evento, savePatternInstances);
      Assert.assertEquals("No se ha calculado correctamente el soporte con ModeloEvenoNegadoSimple después de " + evento, 2, mod.getSoporte());
   }
}
