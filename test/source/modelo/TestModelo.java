package source.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.evento.Evento;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.ModeloEventoNegado;


@RunWith(Parameterized.class)
public class TestModelo {

   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {

            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 0),
                     new Evento("B", 10))),
               new ModeloEventoNegado("A", 0, 4),
               1
            },
            {
               new ArrayList<Evento>(Arrays.asList(
                     new Evento("A", 0),
                     new Evento("B", 10),
                     new Evento("A", 11))),
               new ModeloEventoNegado("A", 0, 4),
               0
            }
      });
   }


   protected List<Evento> eventos;
   protected IAsociacionConNegacion modelo;
   protected int value;

   public TestModelo(List<Evento> eventos,
         IAsociacionConNegacion modelo, int value){
      this.eventos = eventos;
      this.modelo = modelo;
      this.value = value;
   }

   @Test
   public void testRecibeEvento(){

      for(Evento ev : eventos){
         modelo.recibeEvento(0, ev, false);
      }

      Assert.assertEquals("Soporte incorrecto en la secuencia " + eventos + " para el modelo " + modelo,
            value, modelo.getSoporte());
   }

}
