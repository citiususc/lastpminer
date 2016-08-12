package source.evento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class SecuenciaSimpleTest {

   @Test
   public void testSublist(){
      //Se comprueba si el indice en una sublist se respeta
      List<Integer> lista = new ArrayList<Integer>();
      lista.addAll(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10));

      List<Integer> subList = lista.subList(5, 10);//lista.size());
      Assert.assertEquals("Es un indice global no relativo a la sublista", 0, subList.indexOf(5) );

      subList.clear();

      System.out.println(lista);
      Assert.assertEquals("La operación de borrado no afecta a la lista original", 6, lista.size());

      subList.add(100);
      System.out.println(lista);
   }
}
