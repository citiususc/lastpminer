package source.busqueda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.EventoEliminado;

public class MineOccurrenceMarkingTest {

   /**
    * Este test se asegura de que todos los eventos borrados por estrategias como
    * interval marking o windows marking también son borrados por occurrence marking.
    */
   @Test
   public void testBorradosFaltantes(){
      AllThatYouNeed capsula1 = new AllThatYouNeedSinteticas("BDRoE9");
      capsula1.params.setWindowSize(20);
      capsula1.params.setTamMaximoPatron(4);
      capsula1.params.setSaveRemovedEvents(true);
      capsula1.params.setMode(Modes.MODE_BASIC);
      capsula1.params.setAlgorithm(Algorithms.ALG_WM);
      capsula1.mineria();

      AllThatYouNeed capsula2 =  new AllThatYouNeedSinteticas("BDRoE9");
      capsula2.params = capsula1.params;
      //Tenemos que ir a la siguiente iteración ya que es cuando se borran los eventos
      //que en la iteración anterior se ha detectado que no se utilizan
      capsula1.params.setTamMaximoPatron(5);
      capsula2.params.setAlgorithm(Algorithms.ALG_OM);
      capsula2.mineria();

      List<List<EventoEliminado>> eliminados1 = capsula1.eventosEliminados;
      List<EventoEliminado> todosEliminados1 = new ArrayList<EventoEliminado>();
      for(List<EventoEliminado> lista : eliminados1){
         todosEliminados1.addAll(lista);
      }
      Collections.sort(todosEliminados1);

      List<List<EventoEliminado>> eliminados2 = capsula2.eventosEliminados;
      List<EventoEliminado> todosEliminados2 = new ArrayList<EventoEliminado>();
      for(List<EventoEliminado> lista : eliminados2){
         todosEliminados2.addAll(lista);
      }
      Collections.sort(todosEliminados2);

      compararEliminados(todosEliminados1, todosEliminados2);

   }

   private void compararEliminados(List<EventoEliminado> listaA, List<EventoEliminado> listaB){
      List<EventoEliminado> soloA = new ArrayList<EventoEliminado>(),
            soloB = new ArrayList<EventoEliminado>(),
            comunes = new ArrayList<EventoEliminado>();
      int i=0, j=0, x=listaA.size(), y=listaB.size();
      while(i<x && j<y){
         switch(listaA.get(i).compareTo(listaB.get(j))){
            case -1:
               soloA.add(listaA.get(i));
               i++;
               break;
            case 0:
               comunes.add(listaA.get(i));
               i++; j++;
               break;
            case 1:
               soloB.add(listaB.get(j));
               j++;
               break;
         }

      }
      soloA.addAll(listaA.subList(i, x));
      soloB.addAll(listaB.subList(j, y));

      Assert.assertTrue("La lista de eventos borrados por el primer algoritmo no está vacía",soloA.isEmpty());


      System.out.println("Comunes: " + comunes.size());
      System.out.println("Solo A (" + soloA.size() + "): " + soloA);
      System.out.println("Solo B (" + soloB.size() + "): " + soloB);
   }

}
