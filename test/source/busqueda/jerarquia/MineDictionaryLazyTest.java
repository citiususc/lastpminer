package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.PrincipalTest;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.Evento;
import source.evento.EventoAnotado;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.Ocurrencia;
import source.patron.Patron;
import source.restriccion.RIntervalo;
import static org.junit.Assert.*;


public class MineDictionaryLazyTest {

   private List<String[]> anotadas(){
      List<String[]> anotadas = new ArrayList<String[]>();
      anotadas.add(new String[]{"A", "E", "G"});
      anotadas.add(new String[]{"A", "G", "H"});
      anotadas.add(new String[]{"C", "D", "G"});
      anotadas.add(new String[]{"C", "E", "G"});
      anotadas.add(new String[]{"C", "G", "H"});
      anotadas.add(new String[]{"D", "E", "G"});
      anotadas.add(new String[]{"D", "G", "H"});
      anotadas.add(new String[]{"E", "G", "H"});
      return anotadas;
   }

   /**
    * Comprueba el método distintosYComunes
    */
   @Test public void testDistintosIguales(){

      String[] lista1 = new String[]{"A", "E", "G"};
      String[] lista2 = new String[]{"E", "G", "H"};
      String[] lista3 = new String[]{"B", "F", "H"};
      List<String> iguales = new ArrayList<String>();
      List<String> distintos = new ArrayList<String>();
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);

      //Ejemplos con elementos comunes y distintos
      mine.distintosYComunes(lista1, lista2, iguales, distintos);

      assertEquals(Arrays.asList("E","G"), iguales);
      assertEquals(Arrays.asList("A","H"), distintos);

      iguales.clear();
      distintos.clear();

      mine.distintosYComunes(lista2, lista3, iguales, distintos);

      assertEquals(Arrays.asList("H"), iguales);
      assertEquals(Arrays.asList("B", "E", "F", "G"), distintos);

      iguales.clear();
      distintos.clear();

      //Ejemplo con todos los elementos iguales
      mine.distintosYComunes(lista1, lista1, iguales, distintos);
      assertEquals(lista1, iguales);
      assertEquals(new ArrayList<String>(), distintos);

      iguales.clear();
      distintos.clear();

      //Ejemplo con todos los elementos distintos
      mine.distintosYComunes(lista1, lista3, iguales, distintos);
      assertEquals(new ArrayList<String>(), iguales);

      List<String> l = new ArrayList<String>(Arrays.asList(lista1));
      l.addAll(Arrays.asList(lista3));
      Collections.sort(l);
      assertEquals(l, distintos);
   }

   /**
    * Comprueba el método indexOfFirstOccurrence
    */
   @Test public void testPrimeraOcurrencia(){
      List<String[]> anotadas = anotadas();
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
      List<String> mod = new ArrayList<String>(Arrays.asList("A","G"));

      assertEquals(0,mine.indexOfFirstOccurrence(anotadas, mod, 0));

      mod = new ArrayList<String>(Arrays.asList("A","H"));
      assertEquals(1, mine.indexOfFirstOccurrence(anotadas, mod, 0));

      mod = new ArrayList<String>(Arrays.asList("D","H"));
      assertEquals(6,mine.indexOfFirstOccurrence(anotadas, mod, 0));
   }

   /**
    * Comprueba el método genAsociacionesTemporales
    * @throws FactoryInstantiationException
    */
   @Test public void testGeneracionAsociaciones() throws FactoryInstantiationException{
      List<String[]> anotadas = anotadas();
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
      mine.iniciarContadores(6,0);

      List<String[]> nuevasAnotadas = mine.genAsociacionesTemporales(anotadas,"G",3);
      System.out.println(nuevasAnotadas);

      List<String[]> nuevasAnotadas2 = mine.genAsociacionesTemporales(nuevasAnotadas,"G",4);
      System.out.println(nuevasAnotadas2);

      assertEquals("No se calculan bien las asociaciones temporales a partir de las anotaciones ",
            Arrays.asList("C", "D", "E", "G", "H"), nuevasAnotadas2.get(0));
   }

   @Ignore("Pasado") //23/01/2015
   @Test public void testComparar(){
         String[] cad1 = new String[]{"A", "B", "C"};
         String[] cad2 = new String[]{"A", "F", "C"};
         String[] cad3 = new String[]{"B", "F", "C"};
         String[] cad4 = new String[]{"A", "B", "F"};

         MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
         assertEquals(cad1 + " debería estar antes que " + cad2, -1,mine.comparar(cad1, cad2));
         assertEquals(cad1 + " debería estar antes que " + cad3, -1,mine.comparar(cad1, cad3));
         assertEquals(cad2 + " debería estar después de " + cad1, 1,mine.comparar(cad2, cad1));
         assertEquals(cad3 + " debería estar después de " + cad1, 1, mine.comparar(cad3, cad1));
         assertEquals(cad4 + " debería estar después de " + cad1, 1, mine.comparar(cad4, cad1));
         assertEquals(cad1 + " debería estar antes que " + cad4, -1, mine.comparar(cad1, cad4));
         assertEquals(cad1 + " debería ser igual a " + cad1, 0, mine.comparar(cad1, cad1));
      }

   private List<Patron> anotacionesEjemploSimple(){
      List<Patron> patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"A"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"C"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"B"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"C"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"B"},new ArrayList<RIntervalo>(),true));
      return patrones;
   }

   @Ignore("Pasado") //23/01/205
   @Test public void testlistaAnotacionesSimple(){
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
      List<String[]> algo = mine.listaAnotaciones(anotacionesEjemploSimple());
      System.out.println("algo:" + algo);
      assertEquals("No se calcula correctamente la lista de asociaciones a partir de las anotaciones", new ArrayList<List<String>>(
            Arrays.asList(Arrays.asList("A"), Arrays.asList("B"), Arrays.asList("C"))),
            algo);
   }

   private List<Patron> anotacionesEjemplo(){
      List<Patron> patrones = new ArrayList<Patron>();
      patrones.add(new Patron(new String[]{"fA","fD","fF","fT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iA"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iA"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fD","fT","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fF","fT","iA"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fF","fT","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fF","fT","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fF","fT","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iA","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iA","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fA","fT","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iA"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iA"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fF","fT","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iA","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fD","fT","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iA","iD"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iA","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iA","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fF","fT","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iD","iF"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iD","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iA","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iD","iF","iT"},new ArrayList<RIntervalo>(),true));
      patrones.add(new Patron(new String[]{"fT","iD","iF","iT"},new ArrayList<RIntervalo>(),true));

      return patrones;
   }

   @Test public void testlistaAnotaciones(){
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
      List<String[]> algo = mine.listaAnotaciones(anotacionesEjemplo());
      System.out.println("algo:" + algo);
      //TODO definir assert
   }

   //@Ignore("Pasado")
   @Test public void testSAHSContrasteSinMineria(){
      MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
      mine.iniciarContadores(6,0);
      List<String> mod = Arrays.asList("fA", "fD", "fF", "fT", "iA");
      String eventType = "fT";
      List<String[]> nuevasAnotadas = mine.genAsociacionesTemporales(anotacionesEjemplo(),eventType);
      System.out.println(nuevasAnotadas);
      assertTrue("No se ha llegado a la asociacion temporal con las anotaciones",nuevasAnotadas.contains(mod));
   }

   @Test public void testSAHSContraste(){
      Patron.setPrintID(false);

      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      IColeccion coleccion = capsula.coleccion.clone();
      capsula.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula.params.setMode(Modes.MODE_BASIC);
      capsula.params.setSaveAllAnnotations(true);
      capsula.params.setSavePatternInstances(true);
      capsula.params.setTamMaximoPatron(5);

      capsula.mineria();/*String file = capsula.mineria();

      AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      capsula2.params.algorithm = ConfigurationParameters.ALG_LAZY;
      capsula2.params.mode = 						capsula.params.mode;
      capsula2.params.saveAllAnnotations = 	capsula.params.saveAllAnnotations;
      capsula2.params.savePatternInstances =	capsula.params.savePatternInstances;
      capsula2.params.tamMaximoPatron = capsula.params.tamMaximoPatron;
      String file2 = capsula2.mineria();
      System.out.println("Ficheros HSTP y LAZY:\n" + file + "\n" + file2);*/

      String[] mod = new String[]{"fA", "fD", "fF", "fT", "iA"};
      IAsociacionTemporal asoc = PrincipalTest.getAsociacion(capsula.resultados, mod);
      Patron p = asoc.getPatron(0);
      assertNotNull(p);

      assertFalse(p.getOcurrencias().isEmpty());
//		Ocurrencia oc = p.getOcurrencias().get(0);
      for(Ocurrencia oc:p.getOcurrencias()){
         System.out.println("Ocurrencias del patrón: " + oc);

         //Anotaciones de la primera ocurrencia
         int sid = oc.getSequenceID();
         String eventType = lastEvent(Arrays.asList(mod), oc.getEventTimes());
         int eventTime = oc.getEventTimes()[Arrays.asList(mod).indexOf(eventType)];
         Evento ev =  new Evento(eventType, eventTime);
         System.out.println("Evento: " + ev);

         Anotaciones anot = new Anotaciones(capsula.anotaciones);
         EventoAnotado anotado = anot.getAnotaciones(coleccion, mod.length-1, sid,ev);

         //System.out.println("Anotaciones: " + anotado.getAnotaciones());
         MineDictionaryLazy2 mine = new MineDictionaryLazy2("test", true, true, true, null, false);
         mine.iniciarContadores(capsula.tipos.size(), capsula.coleccion.size());
         List<String[]> nuevasAnotadas = mine.genAsociacionesTemporales(anotado.getAnotaciones(),eventType);
         System.out.println(nuevasAnotadas);

         assertTrue("No se ha llegado a la asociacion temporal con las anotaciones con la ocurrencia: " + oc,nuevasAnotadas.contains(mod));
      }
   }

   private String lastEvent(List<String> mod, int[] eventTimes){
      long max=-1;
      List<String> finalistas = new ArrayList<String>(mod);
      for(int i=finalistas.size()-1;i>=0;i--){
         if(max<eventTimes[i]){
            max = eventTimes[i];
         }else{
            finalistas.set(i,null);
         }
      }
      for(int i=finalistas.size()-1;i>=0;i--){
         if(finalistas.get(i)==null || eventTimes[i]<max) finalistas.remove(i);
      }
      return finalistas.get(0);
   }

   @Test public void testLastEvent(){
      String result = lastEvent(Arrays.asList("A","B","C"), new int[]{1,2,3});
      assertEquals("C", result);

      result = lastEvent(Arrays.asList("A","B","C"), new int[]{1,2,2});
      assertEquals("C", result);

      result = lastEvent(Arrays.asList("A","B","C"), new int[]{5,2,3});
      assertEquals("A", result);

      result = lastEvent(Arrays.asList("A","B","C"), new int[]{5,2,5});
      assertEquals("C", result);
   }
}
