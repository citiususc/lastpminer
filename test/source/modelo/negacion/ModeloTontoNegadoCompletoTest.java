package source.modelo.negacion;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import source.evento.Evento;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.patron.Patron;
import source.restriccion.RIntervalo;


public class ModeloTontoNegadoCompletoTest {


   /**
    * Prueba que comprueba que se van generando adecuadamente las ventanas y
    * que cuando sale un evento pero hay otro del mismo tipo en la ventana
    * no se devuelve en el conjunto D
    */
   /*
    * D  D  B
    * +--+--+
    * 1  2  3
    */
   @Test
   public void testSiguienteInstancia(){
      int ventana = 4, sid = 0;
      SuperModeloNegacion supermod = new SuperModeloNegacion(new String[]{"A","B","C","D"}, ventana);
      ModeloTontoNegadoCompleto mod = new ModeloTontoNegadoCompleto(new String[]{"B"},
            new String[]{"D"}, ventana, Collections.<Patron>emptyList(), 0, supermod);

      ISecuencia secuencia = new SecuenciaSimple(new Evento("D",1), new Evento("D",2),
            new Evento("B",3)/*, new Evento("A",4)*/);
      supermod.setSecuencia(secuencia);

      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (D,1) en la ventana", Arrays.asList(new Evento("D",1)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());


      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (D,2) en la ventana", Arrays.asList(new Evento("D",2)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (B,3) en la ventana", Arrays.asList(new Evento("B",3)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, supermod.getN().get(0), false);
      Assert.assertEquals("Soporte incorrecto: como hay un evento negado debería ser 0", 0, mod.getSoporte());

//      supermod.nextWindow();
//      Assert.assertEquals("No ha entrado el evento (A,4) en la ventana", Arrays.asList(new Evento("A",4)), supermod.getN());
//      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("Han entrado eventos que no deberían en la ventana", Collections.<Evento>emptyList(), supermod.getN());
      Assert.assertEquals("Como hay otro D, aunque sale (D,1) no debería estar en el conjunto D", Collections.<Evento>emptyList(), supermod.getD());


      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("Han entrado eventos que no deberían en la ventana", Collections.<Evento>emptyList(), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Arrays.asList(new Evento("D",2)), supermod.getD());

      mod.saleEventoNegado(sid, supermod.getD().get(0), false);
      Assert.assertEquals("Soporte incorrecto", 1, mod.getSoporte());


      Assert.assertFalse(supermod.nextWindow());

   }

   /**
    *  Dos ocurrencias nuevas cuando sale un evento negado
    */
   /*
    * B     A  A     A
    * +--+--+--+--+--+
    * 1     3  4     6
    */
   @Test
   public void testSiguienteInstancia2(){
      int ventana = 4, sid = 0;
      SuperModeloNegacion supermod = new SuperModeloNegacion(new String[]{"A","B"}, ventana);
      ModeloTontoNegadoCompleto mod = new ModeloTontoNegadoCompleto(new String[]{"A"},
            new String[]{"B"}, ventana, Collections.<Patron>emptyList(), 0, supermod);

      ISecuencia secuencia = new SecuenciaSimple(new Evento("B",1), new Evento("A",3), new Evento("A",4),
            new Evento("A",6));
      supermod.setSecuencia(secuencia);

      // Entra (B,1)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (B,1) en la ventana", Arrays.asList(new Evento("B",1)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      // Entra (A,3)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (A,3) en la ventana", Arrays.asList(new Evento("A",3)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, supermod.getN().get(0), false);
      Assert.assertEquals("Soporte incorrecto: como hay un evento negado debería ser 0", 0, mod.getSoporte());

      // Entra (A,4)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (A,4) en la ventana", Arrays.asList(new Evento("A",4)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, supermod.getN().get(0), false);
      Assert.assertEquals("Soporte incorrecto: como hay un evento negado debería ser 0", 0, mod.getSoporte());

      // Sale (B,1)

      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No debería entrar ningún evento", Collections.<Evento>emptyList(), supermod.getN());
      Assert.assertEquals("No ha salido el evento (B,1)", Arrays.asList(new Evento("B",1)), supermod.getD());

      mod.saleEventoNegado(sid, supermod.getD().get(0), false);
      Assert.assertEquals("Soporte incorrecto: hay dos instancias del modelo por salir (B,1)", 2, mod.getSoporte());

      // Entra (A,6)

      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (A,6) en la ventana", Arrays.asList(new Evento("A", 6)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, supermod.getN().get(0), false);
      Assert.assertEquals("Soporte incorrecto: como hay un evento negado debería ser 0", 3, mod.getSoporte());

   }


   /**
    * Salida simultánea de dos eventos cuando hay uno más adelante
    */
   /*
    * B
    * A  C                       A
    * +--+--+--+--+--+--+--+--+--+
    * 1  2                       10
    */
   @Test
   public void testSiguienteInstancia3(){
      int ventana = 2, sid = 0;
      SuperModeloNegacion supermod = new SuperModeloNegacion(new String[]{"A","B","C"}, ventana);
      ModeloTontoNegadoCompleto mod = new ModeloTontoNegadoCompleto(new String[]{"C"},
            new String[]{"A","B"}, ventana, Collections.<Patron>emptyList(), 0, supermod);

      ISecuencia secuencia = new SecuenciaSimple(new Evento("A",1), new Evento("B",1), new Evento("C",2), new Evento("A",10));
      supermod.setSecuencia(secuencia);

      // Entran (A,1) y (B,1)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No han entrado los eventos (A,1) y (B,1) en la ventana", Arrays.asList(new Evento("A",1), new Evento("B",1)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      // Entra (C,2)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No ha entrado el evento (C,2) en la ventana", Arrays.asList(new Evento("C",2)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());
      mod.recibeEvento(sid, supermod.getN().get(0), false);

      Assert.assertEquals("Soporte incorrecto del modelo", 0, mod.getSoporte());

      // Salen (A,1) y (B,2)
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No debería entrar ningún evento", Collections.<Evento>emptyList(), supermod.getN());
      Assert.assertEquals("No han salido los eventos (A,1) y (B,1)", Arrays.asList(new Evento("A",1), new Evento("B",1)), supermod.getD());

      mod.saleEventoNegado(sid, supermod.getD().get(0), false);
      mod.saleEventoNegado(sid, supermod.getD().get(0), false);

      Assert.assertEquals("Soporte incorrecto del modelo", 1, mod.getSoporte());
   }


   /*
    * Test positivo: comprobar ocurrencia repetida
    */
   @Test
   public void testSiguienteInstancia4(){
      int ventana = 20, sid = 0;
      boolean savePatternInstances = true;
      SuperModeloNegacion supermod = new SuperModeloNegacion(new String[]{"fA","fD","fF","iA","iD","iF"}, ventana);

      String[] tipos = new String[]{"fA","fD","fF"};
      Patron p = new Patron(tipos,
            Arrays.asList(
                  new RIntervalo("fA","fD",-19,16),
                  new RIntervalo("fA","fF",-1,3),
                  new RIntervalo("fD","fF",-14,19)
            ), true);
      ModeloTontoNegadoCompleto mod = new ModeloTontoNegadoCompleto(tipos,
            new String[]{}, ventana, Arrays.asList(p), 0, supermod);

      ISecuencia secuencia = new SecuenciaSimple(new Evento("iA",413), new Evento("iF",413),
            new Evento("fD", 419), new Evento("fA", 421), new Evento("fF", 421), new Evento("iD", 428));
      supermod.setSecuencia(secuencia);

      // Entran iF,iA de 413
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No han entrado en la ventana: " + Arrays.asList(secuencia.get(0), secuencia.get(1)),
            Arrays.asList(secuencia.get(0), secuencia.get(1)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      // Entra fd,419
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No han entrado en la ventana: " + Arrays.asList(secuencia.get(2)),
            Arrays.asList(secuencia.get(2)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, secuencia.get(2), savePatternInstances);
      Assert.assertEquals("Soporte incorrecto del modelo", 0, mod.getSoporte());

      // Entran
      Assert.assertTrue(supermod.nextWindow());
      Assert.assertEquals("No han entrado en la ventana: " + Arrays.asList(secuencia.get(3), secuencia.get(4)),
            Arrays.asList(secuencia.get(3), secuencia.get(4)), supermod.getN());
      Assert.assertEquals("Han salido eventos de la ventana que no deberían", Collections.<Evento>emptyList(), supermod.getD());

      mod.recibeEvento(sid, supermod.getN().get(0), savePatternInstances);
      Assert.assertEquals("Soporte incorrecto del modelo", 0, mod.getSoporte());

      mod.recibeEvento(sid, supermod.getN().get(1), savePatternInstances);
      Assert.assertEquals("Soporte incorrecto del modelo", 1, mod.getSoporte());

   }

}
