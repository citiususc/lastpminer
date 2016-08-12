package source.busqueda.episodios;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.AllThatYouNeedSinteticas;
import source.Comparacion;
import source.Principal;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Evento;
import source.evento.EventoEliminado;
import source.evento.IColeccion;
import source.evento.SecuenciaSimple;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.Ocurrencia;
import source.patron.Patron;

public class MineCEDFEIntervalMarkingTest {


   /*@Ignore("Funcionooo")*/
   @Test public void testGeneralSAHS() throws FactoryInstantiationException{

      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      ConfigurationParameters params = capsula.params;
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(false);
      params.setMode(Modes.MODE_BASIC);
      params.setMinFreq(30);
      params.setWindowSize(80);
      params.setResultPath(ExecutionParameters.PROJECT_HOME + "/test/output/testsIM/apnea/");

      List<List<IAsociacionTemporal>> resultA=null, resultB;

      //Minería con las dos versiones
      params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula.mineria();
      resultA = capsula.resultados;

      AllThatYouNeed capsulaIM = new AllThatYouNeedSAHS();
      capsula.params = params;
      params.setAlgorithm(Algorithms.ALG_IM);
      capsulaIM.mineria();
      resultB = capsulaIM.resultados;

      //Escribir resultados para poder comparar ficheros
      //ResultWriter.escribirPatrones(params, resultB);
      //params.algorithm = ConfigurationParameters.ALG_HSTP;
      //ResultWriter.escribirPatrones(params, resultA);

      Comparacion comp = Principal.compararResultados(resultA, resultB);
      System.out.println(comp.toString(true));
      assertTrue(comp.sonIguales());
   }

   /*@Ignore("Funcionooo")*/
   @Test public void testGeneralSinteticas() throws FactoryInstantiationException{
      AllThatYouNeed capsula = new AllThatYouNeedSinteticas("BD4");//"BDRoE9";
      ConfigurationParameters params = capsula.params;
      params.setWindowSize(30);
      params.setMode(Modes.MODE_BASIC);

      params.setSavePatternInstances(true);
      params.setSaveRemovedEvents(false);
      params.setMinFreq(300);
      params.setInputFileName("");
      params.setInputPath(ExecutionParameters.PATH_SINTETICAS + "/" + params.getCollection());
      params.setResultPath(ExecutionParameters.PROJECT_HOME + "/test/output/testsIM/" + params.getCollection() + "/");
      //AbstractMine.VERBOSE = false;

      // Hacer copia de las secuencias originales
      IColeccion copiaColeccion = capsula.coleccion.clone();

      List<List<IAsociacionTemporal>> resultA, resultB;
      params.setAlgorithm(Algorithms.ALG_HSTP);
      capsula.mineria();
      resultA = capsula.resultados;

      //Restaurar copia
      capsula.coleccion = copiaColeccion.clone();

      params.setAlgorithm(Algorithms.ALG_IM);
      capsula.mineria();
      resultB = capsula.resultados;

      //ResultWriter.escribirPatrones(params, resultB);
      //params.algorithm = ConfigurationParameters.ALG_HSTP;
      //ResultWriter.escribirPatrones(params, resultA);

      Comparacion comp = Principal.compararResultados(resultA, resultB);
      System.out.println(comp.toString(false));
      assertTrue(comp.sonIguales());
      comparaOcurrencias(resultA.get(4).get(1).getPatron(0), resultB.get(4).get(1).getPatron(0));
   }



   @Test public void testSimpleBorrar() throws FactoryInstantiationException{
      IColeccion coleccion = new ColeccionSimple();
      coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("A", 1), new Evento("B", 3), new Evento("C", 4), new Evento("A", 8),
            new Evento("B", 9), new Evento("C", 10), new Evento("D", 12), new Evento("E", 13)
            )));
      coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("A", 7), new Evento("B", 8), new Evento("C", 9),
            new Evento("C", 14), new Evento("A", 15), new Evento("B", 16)
            )));
      coleccion.add(new SecuenciaSimple(Arrays.asList(
            new Evento("C", 2), new Evento("A", 3), new Evento("B", 4), new Evento("E", 7),
            new Evento("D", 8), new Evento("A", 11), new Evento("B", 13), new Evento("C", 14)
            )));

      List<String> tipos = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E"));

      AllThatYouNeed capsulaHstp = new AllThatYouNeed(coleccion, tipos, null, null);
      capsulaHstp.params.setMinFreq(2);
      capsulaHstp.params.setWindowSize(4);

      //Minería con las dos versiones
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsulaHstp.mineria();

      AllThatYouNeed capsulaIM = new AllThatYouNeed(coleccion, tipos, null, null);
      capsulaIM.params = capsulaHstp.params;
      capsulaIM.params.setAlgorithm(Algorithms.ALG_IM);
      capsulaIM.mineria();

      Comparacion comp = Principal.compararResultados(capsulaHstp.resultados, capsulaIM.resultados);
      System.out.println(comp.toString(true));
      assertTrue(comp.sonIguales());
   }

   /**
    * Primero se comprueba que los patrones son de la misma asociación temporal.
    * @param pA
    * @param pB
    */
   protected void comparaOcurrencias(Patron pA, Patron pB){
      if(!Arrays.asList(pA.getTipos()).equals(Arrays.asList(pB.getTipos()))){ return; }
      for(int i=0, j=0; i<pA.getOcurrencias().size(); i++){
         Ocurrencia ocA = pA.getOcurrencias().get(i);
         Ocurrencia ocB = pB.getOcurrencias().get(j);
         if(!ocA.equals(ocB)){
            System.out.println("Falta la ocurrencia en sid " + ocA.getSequenceID() + " con tiempos " + Arrays.toString(ocA.getEventTimes()));
         }else{
            j++;
         }
      }
   }

   protected List<EventoEliminado> buscarOcurrenciaEnElimandos(List<List<EventoEliminado>> eliminados, Patron patron){
      List<EventoEliminado> coincidencia = new ArrayList<EventoEliminado>();
      List<Ocurrencia> ocurrencias = patron.getOcurrencias();
      for(Ocurrencia oc: ocurrencias){
         //for(List<EventoEliminado> nivel: eliminados){
         for(int nivel=0; nivel<eliminados.size() && nivel<patron.getTipos().length; nivel++){
            List<EventoEliminado> eliminadosNivel = eliminados.get(nivel);
            for(EventoEliminado eliminado: eliminadosNivel){
               if(eliminado.getSid() != oc.getSequenceID()) continue;
               for(int i=0; i<patron.getTipos().length; i++){
                  if(eliminado.getEvento().getTipo().equals(patron.getTipos()[i])
                        && eliminado.getEvento().getInstante() == oc.getEventTimes()[i]){
                     coincidencia.add(eliminado);
                     System.out.println("Coincidencia: " + eliminado.getEvento());
                  }
               }
            }
         }
      }

      return coincidencia;
   }
}
