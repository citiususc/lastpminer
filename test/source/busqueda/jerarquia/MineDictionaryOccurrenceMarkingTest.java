package source.busqueda.jerarquia;

import static source.PrincipalTestGeneral.PASADO;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.ComparacionPatrones;
import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.EventoEliminado;
import source.modelo.IAsociacionTemporal;
import source.patron.Ocurrencia;
import source.patron.Patron;


public class MineDictionaryOccurrenceMarkingTest {

   @Test
   public void testGeneral(){
      Collection<Object[]> data =Arrays.asList(new Object[][] {
         {"apnea", 80, PASADO},
         {"BDRoE6", 20, PASADO},
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral(Algorithms.ALG_HOM, Modes.MODE_BASIC, (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }



   @Ignore
   @Test public void testSolucionErrorBorrado(){
      AllThatYouNeed capsulaHom = new AllThatYouNeedSAHS();
      //Patron.setPrintID(false);
      capsulaHom.params.setAlgorithm(Algorithms.ALG_HOM);
      capsulaHom.params.setMode(Modes.MODE_BASIC);
      capsulaHom.params.setWindowSize(80);
      capsulaHom.params.setTamMaximoPatron(4);
      capsulaHom.params.setSaveRemovedEvents(true);
      capsulaHom.params.setSavePatternInstances(true);
      capsulaHom.mineria();
      //Patron.resetGenerator();

      AllThatYouNeed capsulaHstp = new AllThatYouNeedSAHS();
      capsulaHstp.params = capsulaHom.params.clonar();
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsulaHstp.params.setSavePatternInstances(true);
      capsulaHstp.mineria();

      NumberFormat nf = NumberFormat.getInstance();
      System.out.println("Eliminados: " + nf.format(capsulaHom.eventosEliminados.get(0).size()));
      //Comprobar que eventos se borraron que tiene ocurrencias
      List<List<IAsociacionTemporal>> resultadosHstp = capsulaHstp.resultados;
      //for(EventoEliminado ee: capsulaHom.eliminados.get(0)){
         //for(IAsociacionTemporal asoc : rmin.get(3)){//tam 4
         IAsociacionTemporal asocHstp = PrincipalTest.getAsociacion(resultadosHstp, new String[]{"fA", "fD", "fF", "fT"});
         IAsociacionTemporal asocHom = PrincipalTest.getAsociacion(capsulaHom.resultados, new String[]{"fA", "fD", "fF", "fT"});

         for(int i=0;i<asocHstp.getPatrones().size();i++){
            System.out.println("\nPatron : " + asocHstp.getPatron(i));
            System.out.println("\nPatron : " + asocHstp.getPatron(i).getID() + ", " + asocHstp.getPatron(i).getOcurrencias().size() + " ocurrencias");
            System.out.println("Patron : " + asocHom.getPatron(i).getID() + ", " + asocHom.getPatron(i).getOcurrencias().size() + " ocurrencias");
            ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(asocHstp.getPatron(i), asocHom.getPatron(i));
            System.out.println(comp.getSoloA().subList(0, 10));
         }


            for(Patron p: asocHstp.getPatrones()){ //cada patron
               System.out.println("Patron : " + p.getID() + ", " + p.getOcurrencias().size() + " ocurrencias");
               //Assert.assertFalse(ocurrenciasBorradas(p, ee));//tam-3
               Assert.assertFalse(ocurrenciasBorradas(p, capsulaHom.eventosEliminados.get(0)));//tam-3
            }
         //}
      //}
   }

   private boolean ocurrenciasBorradas(Patron p, List<EventoEliminado> eliminados){
      for(Ocurrencia o : p.getOcurrencias()){
         for(EventoEliminado ee : eliminados){
            if(o.getSequenceID() != ee.getSid()) continue;
            for(int i=0; i<o.getEventTimes().length; i++){
               if(ee.getEvento().getInstante() == o.getEventTimes()[i] && ee.getEvento().getTipo().equals(p.getTipos()[i])){
                  System.out.println("Se ha borrado " + ee + " cuando había una ocurrencia de " + p);
                  return true;
               }
            }
         }
      }

      return false;
   }

   /*private boolean ocurrenciasBorradas(Patron p, EventoEliminado ee){
      EventoEliminado eeAux;
      for(Ocurrencia o : p.getOcurrencias()){
         if(o.getSequenceID() != ee.getSid()) return false;
         for(int i=0; i<o.getEventTimes().length; i++){
            eeAux = new EventoEliminado(o.getSequenceID(), new Evento(p.getTipos().get(i),o.getEventTimes()[i]));
            if(ee.getEvento().equals(eeAux)){
               System.out.println("Se ha borrado " + ee + " cuando había una ocurrencia de " + p);
               return true;
            }
         }
      }

      return false;
   }*/
}
