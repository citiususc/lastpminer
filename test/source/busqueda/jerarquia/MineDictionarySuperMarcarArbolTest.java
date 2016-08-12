package source.busqueda.jerarquia;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.ComparacionPatrones;
import source.Principal;
import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;

public class MineDictionarySuperMarcarArbolTest{

   @Test
   public void testGeneral(){
      boolean PASADO = false;
      Collection<Object[]> data =Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0
            {"BDRoE6", 20, PASADO}, //1
            {"BDRoE6", 40, PASADO}, //2
            {"BDRoE9", 10, PASADO}, //3
            {"BDRoE9", 20, PASADO}, //4
            {"BDRoE9", 30, PASADO}, //5
            {"BDRoE15", 2, PASADO}, //6
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral(Algorithms.ALG_MARKT, Modes.MODE_BASIC, (String)d[0], (Integer)d[1], (Boolean)d[2]);
         test.test();
      }
   }

   //Pasado a 14/09/15
   @Test public void testFrecuencia(){
      Algorithms algorithm = Algorithms.ALG_MARKT;
      Modes mode = Modes.MODE_BASIC;
      Integer window = 20;
      String collection = "BDRoE9";

      int tamPatron = 6, idPatron = 2229;

      //Patron.setPrintID(false);
      //AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      AllThatYouNeed capsula = new AllThatYouNeedSinteticas(collection);
      capsula.params.setAlgorithm(algorithm);
      capsula.params.setWindowSize(window);
      capsula.params.setMode(mode);
      capsula.params.setSavePatternInstances(true);
      capsula.params.setTamMaximoPatron(tamPatron);
      String fileLazy = capsula.mineria();

      //Patron.resetGenerator();
      //AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      AllThatYouNeed capsula2 = new AllThatYouNeedSinteticas(collection);
      capsula2.params = capsula.params;
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);
      String fileHstp = capsula2.mineria();

      //Patron con id 278
      Patron p1 = PrincipalTest.getPatron(capsula.resultados, tamPatron, idPatron);
      Patron p2 = PrincipalTest.getPatron(capsula2.resultados, tamPatron, idPatron);

      String[] tipos = new String[]{"1", "15", "2", "3", "7", "8"};

      if(p1 == null && p2 == null){
         IAsociacionTemporal asoc1 = PrincipalTest.getAsociacion(capsula.resultados, tipos);
         IAsociacionTemporal asoc2 = PrincipalTest.getAsociacion(capsula2.resultados, tipos);
         p1 = asoc1.getPatron(7);
         p2 = asoc2.getPatron(7);
      }else if(p1 == null && p2 != null){
         IAsociacionTemporal asoc1 = PrincipalTest.getAsociacion(capsula.resultados, p2.getTipos());
         p1 = asoc1.getPatron(7);
      }
      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(p1, p2);
      System.out.println(comp);


      boolean sonIguales =  Principal.compararFicheros(fileHstp, fileLazy);
      if(sonIguales){ //borrar el fichero
         new File(fileLazy).delete();
      }
      Assert.assertTrue("Falla porque... ",sonIguales);
   }

   //Pasado a 14/09/15
   @Test public void testFrecuencia2(){
      Algorithms algorithm = Algorithms.ALG_MARKT;
      Modes mode = Modes.MODE_BASIC;
      Integer window = 20;
      String collection = "BDRoE6";

      String[] tipos = new String[]{"11", "13", "14", "15"};
      int tamPatron = tipos.length+1;

      //Patron.setPrintID(false);
      //AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      AllThatYouNeed capsula = new AllThatYouNeedSinteticas(collection);
      capsula.params.setAlgorithm(algorithm);
      capsula.params.setWindowSize(window);
      capsula.params.setMode(mode);
      capsula.params.setSavePatternInstances(true);
      capsula.params.setTamMaximoPatron(tamPatron);
      String fileLazy = capsula.mineria();

      //Patron.resetGenerator();
      //AllThatYouNeed capsula2 = new AllThatYouNeedSAHS();
      AllThatYouNeed capsula2 = new AllThatYouNeedSinteticas(collection);
      capsula2.params = capsula.params;
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);
      String fileHstp = capsula2.mineria();

      //Patron con id 278


      IAsociacionTemporal asoc1 = PrincipalTest.getAsociacion(capsula.resultados, tipos);
      Assert.assertNotNull(asoc1);

      IAsociacionTemporal asoc2 = PrincipalTest.getAsociacion(capsula2.resultados, tipos);
      Assert.assertNotNull(asoc2);

      Patron p1 = asoc1.getPatron(0);
      Assert.assertNotNull(p1);

      Patron p2 = asoc2.getPatron(0);
      Assert.assertNotNull(p2);

      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(p1, p2);
      System.out.println(comp);
      Assert.assertTrue(comp.sonIguales());

      boolean sonIguales =  Principal.compararFicheros(fileHstp, fileLazy);
      if(sonIguales){ //borrar el fichero
         new File(fileLazy).delete();
      }
      Assert.assertTrue("Falla porque... ",sonIguales);
   }
}