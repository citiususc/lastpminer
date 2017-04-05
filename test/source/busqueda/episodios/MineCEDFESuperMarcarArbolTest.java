package source.busqueda.episodios;

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

public class MineCEDFESuperMarcarArbolTest{

   @Test
   public void testGeneral(){
      boolean PASADO = false;
      Collection<Object[]> data =Arrays.asList(new Object[][] {
            {Algorithms.ALG_LASTP, Modes.MODE_EPISODE, "apnea", 80, PASADO}, //06/04/2015
            {Algorithms.ALG_LASTP, Modes.MODE_EPISODE, "BD4", 20, PASADO},  //06/04/2015
            {Algorithms.ALG_LASTP, Modes.MODE_EPISODE, "BD5", 20, PASADO},  //06/04/2015
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   @Test
   public void testFrecuenciaPatron(){
      Algorithms algorithm = Algorithms.ALG_LASTP;
      Modes mode = Modes.MODE_EPISODE;
      Integer window = 20;
      String collection = "BD5";

      int tamPatron = 5;//, idPatron = 2229;

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
      Patron p1 = null;//PrincipalTest.getPatron(capsula.resultados, tamPatron, idPatron);
      Patron p2 = null;//PrincipalTest.getPatron(capsula2.resultados, tamPatron, idPatron);

      String[] tipos = new String[]{"10", "b1", "b2", "f1", "f2"};

      if(p1 == null && p2 == null){
         IAsociacionTemporal asoc1 = PrincipalTest.getAsociacion(capsula.resultados, tipos);
         IAsociacionTemporal asoc2 = PrincipalTest.getAsociacion(capsula2.resultados, tipos);
         p1 = asoc1.getPatron(0);
         p2 = asoc2.getPatron(0);
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




}
