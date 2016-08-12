package source.modelo.arbol;

import org.junit.Test;

import source.modelo.ModeloEvento;
import source.modelo.negacion.ModeloEventoNegado;
import source.modelo.negacion.ModeloTontoNegadoCompleto;


public class SupernodoNegacionTest {

   @Test
   public void testConstruccion(){
      SupernodoNegacion sn = new SupernodoNegacion();
      int win = 60;

      String tipo = "A";
      ModeloEvento mod = new ModeloEvento(tipo, 0);
      Nodo n = new Nodo(mod, sn);
      sn.addNodo(n, tipo);

      ModeloEventoNegado mod2 = new ModeloEventoNegado(tipo, 0, win);
      SupernodoNegacion sn1 = new SupernodoNegacion();
      Nodo n2 = new Nodo(mod2, sn, sn1);
      sn1.setPadre(n2);
      sn.addNodo(n2, tipo, false);

      String tipo2 = "B";
      ModeloTontoNegadoCompleto mod3 = new ModeloTontoNegadoCompleto(new String[]{}, new String[]{tipo, tipo2}, win, 0);
      SupernodoNegacion sn2 = new SupernodoNegacion();
      Nodo n3 = new Nodo(mod3, n2.hijos, sn2);
      sn2.setPadre(n3);
      ((SupernodoNegacion)n2.hijos).addNodo(n3, tipo2, false);

      //TODO test
      System.out.println("Árbol: " + sn.toString());
   }
}
