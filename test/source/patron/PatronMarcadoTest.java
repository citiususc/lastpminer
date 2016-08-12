package source.patron;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import source.evento.Evento;
import source.restriccion.RIntervalo;



public class PatronMarcadoTest {
   private static final int WIN = 10;
   private static final Evento EV1 = new Evento("A",24), EV2 = new Evento("A", 30);
   private static final int SID = 1;

   //private PatronMarcado buscado;
   private PatronMarcado padreEnVentana;
   private PatronMarcado padreEnEvento1, padreEnEvento2, padreEnEvento3;

   private void construirPatronesEjemplo(){
      //buscado = new PatronMarcado(Arrays.asList("A","B","C","D"),
      //      new ArrayList<RIntervalo>(), true);
      padreEnEvento1 = new PatronMarcado(new String[]{"A","B","C"},
            new ArrayList<RIntervalo>(), true);
      padreEnEvento2 = new PatronMarcado(new String[]{"A","B","D"},
            new ArrayList<RIntervalo>(), true);
      padreEnEvento3 = new PatronMarcado(new String[]{"A","C","D"},
            new ArrayList<RIntervalo>(), true);
      padreEnVentana = new PatronMarcado(new String[]{"B","C","D"},
            new ArrayList<RIntervalo>(), true);

      //Algunas entradas de relleno
      padreEnEvento1.encontrado(0, 10, 19);
      padreEnEvento1.encontrado(0, 15, 20);


      //La ocurrencia de prueba
      padreEnEvento1.encontrado(SID, 22, 24);
      padreEnEvento2.encontrado(SID, 21, 24);
      padreEnEvento3.encontrado(SID, 21, 24);
      padreEnVentana.encontrado(SID, 21, 23);

      //Más relleno
      padreEnEvento2.encontrado(SID, 22, 30);
      padreEnEvento2.encontrado(SID+1, 20, 30);
      padreEnEvento2.encontrado(SID+1, 30, 40);
   }

   @Test
   public void enEventoTest(){
      construirPatronesEjemplo();
      Assert.assertTrue("No se encuentra a padre1 en el evento", padreEnEvento1.enEvento(SID, EV1, WIN));
      Assert.assertTrue("No se encuentra a padre2 en el evento", padreEnEvento2.enEvento(SID, EV1, WIN));
      Assert.assertTrue("No se encuentra a padre3 en el evento", padreEnEvento3.enEvento(SID, EV1, WIN));

      Evento ev2 = new Evento("E",25);
      Assert.assertTrue("No se encuentra a padre1 en el evento " + ev2, padreEnEvento1.enVentana(SID, ev2, WIN));

      Assert.assertFalse("Se encuentra a padre1 en el evento", padreEnEvento1.enEvento(SID, EV2, WIN));
      Assert.assertTrue("No se encuentra a padre2 en el evento", padreEnEvento2.enEvento(SID, EV2, WIN));
      Assert.assertFalse("Se encuentra a padre3 en el evento", padreEnEvento3.enEvento(SID, EV2, WIN));

   }

   @Test
   public void enVentanaTest(){
      construirPatronesEjemplo();
      Assert.assertTrue("No se encuentra a padre en la ventana", padreEnVentana.enVentana(SID, EV1, WIN));
      Assert.assertTrue("No se encuentra a padre en la ventana", padreEnVentana.enVentana(SID, EV1, WIN));
   }


}
