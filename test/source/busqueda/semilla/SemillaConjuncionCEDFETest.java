package source.busqueda.semilla;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.Comparacion;
import source.Principal;
import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;



public class SemillaConjuncionCEDFETest {


   @Test
   public void testGeneral(){
      boolean PASADO = false;
      Collection<Object[]> data =Arrays.asList(new Object[][] {
         {Algorithms.ALG_HSTP, Modes.MODE_FULL, "apnea", 80, PASADO},
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   /**
    * Test que comprueba que el resultado de HSTPminer con semilla y
    * episodios es igual al de ASTPminer también con semilla y episodios
    * @throws FactoryInstantiationException
    */
   @Test public void testConASTP() throws FactoryInstantiationException{
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      ConfigurationParameters params = capsula.params;
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(false);
      params.setMode(Modes.MODE_FULL);

      List<List<IAsociacionTemporal>> resultA, resultB;

      params.setAlgorithm(Algorithms.ALG_ASTP);
      String ficheroAstp = capsula.mineria();
      resultA = capsula.resultados;

      //Hay que volver a ler porque se borran eventos da colección coa semente
      capsula = new AllThatYouNeedSAHS();
      capsula.params = params;
      params.setAlgorithm(Algorithms.ALG_HSTP);
      String ficheroHstp = capsula.mineria();
      resultB = capsula.resultados;

      System.out.println("ASTP: " + ficheroAstp + "\nHSTP:" + ficheroHstp);

      Comparacion comp = Principal.compararResultados(resultA, resultB);
      System.out.println(comp);
      //System.out.println(comp.getPatronesSoloA());
      assertTrue(comp.sonIguales());


   }

   /**
    * Comprueba que el resultado de SemillaConjuncionCEDFE sin episodios
    * es igual al de SemillaConjuncionDictionaryFinalEvent.
    * Utiliza como colección SAHS
    */
   @Test public void testSinEpisodios(){
      Patron.setPrintID(false);
      AllThatYouNeed capsula1 = new AllThatYouNeedSAHS();
      capsula1.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula1.params.setMode(Modes.MODE_SEED);

      AllThatYouNeed capsula2 = new AllThatYouNeed(capsula1.coleccion.clone(), capsula1.tipos,
            new ArrayList<Episodio>(), capsula1.ocurrenciasEpisodios);
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula2.params.setMode(Modes.MODE_FULL);

      String file1 = capsula1.mineria();
      String file2 = capsula2.mineria();
      assertTrue(Principal.compararFicheros(file1, file2));

   }


   /**
    * Comprueba que el resultado de SemillaConjuncionCEDFE sin episodios
    * es igual al de SemillaConjuncionDictionaryFinalEvent.
    * Utiliza como colección SAHS
    */
   @Test public void testSinEpisodiosSimple(){
      Patron.setPrintID(false);
      AllThatYouNeed capsula1 = new AllThatYouNeedSAHS();

      IColeccion coleccion = new ColeccionSimple();
      coleccion.add(capsula1.coleccion.get(1));
      capsula1.coleccion = coleccion;

      capsula1.params.setMinFreq(2);
      capsula1.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula1.params.setMode(Modes.MODE_SEED);
      capsula1.params.setSavePatternInstances(true);
      String file1 = capsula1.mineria();

      AllThatYouNeed capsula2 = new AllThatYouNeed(capsula1.coleccion.clone(), capsula1.tipos,
            new ArrayList<Episodio>(), capsula1.ocurrenciasEpisodios);
      capsula2.params = capsula1.params;
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula2.params.setMode(Modes.MODE_FULL);
      String file2 = capsula2.mineria();

      System.out.println("Comparando\n" + file1 + "\n" + file2);


      for(IAsociacionTemporal mod : capsula1.resultados.get(2)){
         boolean encontrado = false;
         for(Patron p:mod.getPatrones()){
            if(p.getID()==14){
               System.out.println("Patron 1: " + p);
               System.out.println("Ocurrencias: " + p.getOcurrencias());
               encontrado = true;
               break;
            }
         }
         if(encontrado) break;
      }

      for(IAsociacionTemporal mod : capsula2.resultados.get(2)){
         boolean encontrado = false;
         for(Patron p:mod.getPatrones()){
            if(p.getID()==4809){
               System.out.println("Patron 1: " + p);
               System.out.println("Ocurrencias: " + p.getOcurrencias());
               encontrado = true;
               break;
            }
         }
         if(encontrado) break;
      }


      /*Print

 Fr: 5 - [fA, fD, iA] ID: 14
fA fD -> [-16.0,-14.0]
fA iA -> [-45.0,-43.0]
fD iA -> [-29.0,-27.0]
 Fr: 7 - [fA, fD, iA] ID: 4809
fA fD -> [-16.0,-14.0]
fA iA -> [-45.0,-43.0]
fD iA -> [-29.0,-27.0]

*/

      //Comparacion comp = AbstractMine.compararResultados(capsula1.resultados, capsula2.resultados);
      //System.out.println(comp);

      assertTrue(Principal.compararFicheros(file1, file2));

   }

   /**
    * Comprueba que el número de ocurrencias y la frecuencia de los patrones coincide
    */
   @Test public void testOcurrenciasFrecuencia(){
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.episodios = new ArrayList<Episodio>();
      capsula.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula.params.setMode(Modes.MODE_FULL);
      capsula.params.setSavePatternInstances(true);

      IColeccion coleccionOriginal = capsula.coleccion.clone();

      capsula.mineria();
      List<List<IAsociacionTemporal>> resultados = capsula.resultados;

      PrincipalTest.validarResultados(resultados, coleccionOriginal);
   }


}
