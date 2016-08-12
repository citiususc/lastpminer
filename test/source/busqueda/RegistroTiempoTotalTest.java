package source.busqueda;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;


public class RegistroTiempoTotalTest {


   @Test
   public void pruebaOtrosTiempos() throws InterruptedException{
      RegistroTiempoTotal reg = new RegistroTiempos();
      reg.addOtrosTiempos("tiempo1","tiempo2");
      reg.iniciar(10);

      long ahora = System.currentTimeMillis();
      long ahora2 = System.currentTimeMillis();
      System.out.println("Tiempo: " + (ahora2-ahora));
      reg.tiempo("tiempo1", 0, true);
      int count = 1000000;
      List<Double> a = new ArrayList<Double>();
      while(count>0){
         a.add(Math.pow(23, Math.sqrt(count--)));
      }
      reg.tiempo("tiempo1", 0, false);
      System.out.println("tiempo1: " + Arrays.toString(reg.getTiempos("tiempo1")));
   }
}
