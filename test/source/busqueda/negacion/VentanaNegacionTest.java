package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import source.evento.Evento;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;


public class VentanaNegacionTest {



   // Detectar ventanas
   @Test
   public void test(){
      int window = 4;
      ISecuencia s1 = new SecuenciaSimple(new Evento("X",1), new Evento("A",3),
            new Evento("B",4), new Evento("Y",6));
      VentanaNegacion ventana = new VentanaNegacion(s1, window);

      List<Evento> estado1 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1)));
      Assert.assertEquals("Primer evento de la colección", estado1, ventana.nextState());
      Assert.assertEquals("Entra X en la ventana y en N", estado1, ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado2 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1),new Evento("A",3)));
      Assert.assertEquals("Añadir un evento cuando ya había otro", estado2, ventana.nextState());
      Assert.assertEquals("Entra A en la ventana y en N", Arrays.asList(new Evento("A",3)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado3 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1),new Evento("A",3),new Evento("B",4)));
      Assert.assertEquals("Añadir un evento cuando ya había otros dos", estado3, ventana.nextState());
      Assert.assertEquals("Entra B en la ventana y en N", Arrays.asList(new Evento("B",4)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado4 = new ArrayList<Evento>(Arrays.asList(new Evento("A",3),new Evento("B",4)));
      Assert.assertEquals("Salida de un evento sin que entre nada nuevo", estado4, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana X", Arrays.asList(new Evento("X",1)), ventana.D);

      List<Evento> estado5 = new ArrayList<Evento>(Arrays.asList(new Evento("A",3), new Evento("B",4), new Evento("Y",6)));
      Assert.assertEquals("Añadir un evento cuando ya había otros dos", estado5, ventana.nextState());
      Assert.assertEquals("Entra Y en la ventana y en N", Arrays.asList(new Evento("Y",6)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado6 = new ArrayList<Evento>(Arrays.asList(new Evento("B",4), new Evento("Y",6)));
      Assert.assertEquals("Salida de un evento al final de la secuencia", estado6, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana X", Arrays.asList(new Evento("A",3)), ventana.D);

      List<Evento> estado7 = new ArrayList<Evento>(Arrays.asList(new Evento("Y",6)));
      Assert.assertEquals("Salida de un evento al final de la secuencia. Último evento de la secuencia", estado7, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana X", Arrays.asList(new Evento("B",4)), ventana.D);

      Assert.assertEquals("Null cuando no hay más elementos", null, ventana.nextState());

   }


   @Test
   public void testEventosSimultaneos(){
      int window = 4;
      ISecuencia s1 = new SecuenciaSimple(new Evento("X",1), new Evento("A",3),
            new Evento("B",4), new Evento("C",4), new Evento("Y",6));
      VentanaNegacion ventana = new VentanaNegacion(s1, window);

      List<Evento> estado1 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1)));
      Assert.assertEquals("Primer evento de la colección", estado1, ventana.nextState());
      Assert.assertEquals("Entra X en la ventana y en N", estado1, ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado2 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1),new Evento("A",3)));
      Assert.assertEquals("Añadir un evento cuando ya había otro", estado2, ventana.nextState());
      Assert.assertEquals("Entra A en la ventana y en N", Arrays.asList(new Evento("A",3)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado3 = new ArrayList<Evento>(Arrays.asList(new Evento("X",1),new Evento("A",3),new Evento("B",4), new Evento("C",4)));
      Assert.assertEquals("Añadir un evento cuando ya había otros dos", estado3, ventana.nextState());
      Assert.assertEquals("Entran B y C en la ventana y en N", Arrays.asList(new Evento("B",4), new Evento("C",4)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado4 = new ArrayList<Evento>(Arrays.asList(new Evento("A",3),new Evento("B",4), new Evento("C",4)));
      Assert.assertEquals("Salida de un evento sin que entre nada nuevo", estado4, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana X", Arrays.asList(new Evento("X",1)), ventana.D);

      List<Evento> estado5 = new ArrayList<Evento>(Arrays.asList(new Evento("A",3), new Evento("B",4), new Evento("C",4), new Evento("Y",6)));
      Assert.assertEquals("Añadir un evento cuando ya había otros dos", estado5, ventana.nextState());
      Assert.assertEquals("Entra Y en la ventana y en N", Arrays.asList(new Evento("Y",6)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      List<Evento> estado6 = new ArrayList<Evento>(Arrays.asList(new Evento("B",4), new Evento("C",4), new Evento("Y",6)));
      Assert.assertEquals("Salida de un evento al final de la secuencia", estado6, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana X", Arrays.asList(new Evento("A",3)), ventana.D);

      List<Evento> estado7 = new ArrayList<Evento>(Arrays.asList(new Evento("Y",6)));
      Assert.assertEquals("Salida de un evento al final de la secuencia. Último evento de la secuencia", estado7, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Salen de la ventana B y C", Arrays.asList(new Evento("B",4), new Evento("C",4)), ventana.D);

      Assert.assertEquals("Null cuando no hay más elementos", null, ventana.nextState());

   }


   @Test
   public void testSalidaVentana(){
      int window = 2;
      ISecuencia s1 = new SecuenciaSimple(new Evento("A",1),new Evento("B",2), new Evento("C",3), new Evento("D",4),
            new Evento("F",6), new Evento("E",8));
      VentanaNegacion ventana = new VentanaNegacion(s1, window);

      ventana.nextState(); // entró A
      ventana.nextState(); // entró B

      List<Evento> estado0 = new ArrayList<Evento>(Arrays.asList(new Evento("B",2), new Evento("C",3)));
      Assert.assertEquals("Sale C", estado0, ventana.nextState()); // entró C, salió A

      ventana.nextState(); //ENTRA D

      List<Evento> estado1 = new ArrayList<Evento>(Arrays.asList(new Evento("D",4)));
      Assert.assertEquals("Sale C", estado1, ventana.nextState());
      Assert.assertEquals("Nada entra en la ventana ni en N", Collections.EMPTY_LIST, ventana.N);
      Assert.assertEquals("Sale de la ventana C", Arrays.asList(new Evento("C",3)), ventana.D);

      List<Evento> estado2 = new ArrayList<Evento>(Arrays.asList(new Evento("F",6)));
      Assert.assertEquals("Añadir un evento y eliminar otro", estado2, ventana.nextState());
      Assert.assertEquals("Entra A en la ventana y en N", Arrays.asList(new Evento("F",6)), ventana.N);
      Assert.assertEquals("Nada sale de la ventana", Collections.EMPTY_LIST, ventana.D);

      ventana.nextState(); // sale F,6 queda E,8

      Assert.assertEquals("Null cuando no hay más elementos", null, ventana.nextState());

   }
}


