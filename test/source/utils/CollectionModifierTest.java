package source.utils;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.utils.CollectionModifier;
import static org.junit.Assert.*;

public class CollectionModifierTest {

   @Test
   public void testMultiple() throws UnsupportedEncodingException{
      String answer = "3,3-4, 5, 2, 9, 21";
      //PrintWriter writer = System.console().writer();
      PrintStream output = System.out;
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
      List<Integer> toBeRemovedList = CollectionModifier.indexesToBeRemoved(writer, answer, 25);
      System.out.println("Indexes: " + toBeRemovedList);
      assertEquals("No se han expandido o ordenado correctamente para " + answer, Arrays.asList(21,9,5,4,3,2), toBeRemovedList);
   }

   @Test
   public void testMaximoRespectado() throws UnsupportedEncodingException{
      //PrintWriter writer = System.console().writer();
      PrintStream output = System.out;
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
      String answer = "3,3-4, 5, 2, 9, 21";
      List<Integer> toBeRemovedList = CollectionModifier.indexesToBeRemoved(writer, answer, 20);
      System.out.println("Indexes: " + toBeRemovedList);
      assertEquals("No se han expandido o ordenado correctamente para " + answer, Arrays.asList(9,5,4,3,2), toBeRemovedList);
   }

   @Test
   public void testExpandirGuion() throws UnsupportedEncodingException{
      //PrintWriter writer = System.console().writer();
      PrintStream output = System.out;
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
      String answer = "1-49";
      List<Integer> toBeRemovedList = CollectionModifier.indexesToBeRemoved(writer, answer, 49);
      System.out.println("Indexes: " + toBeRemovedList);
      List<Integer> expected = new ArrayList<Integer>();
      for(int i=49;i>0;i--){
         expected.add(i);
      }
      assertEquals("No se han expandido o ordenado correctamente para " + answer, expected, toBeRemovedList);
   }
}
