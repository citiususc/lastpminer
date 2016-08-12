package source.busqueda.jerarquia;

import static source.PrincipalTestGeneral.PASADO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Evento;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class MineAnotacionesTest {

   @Test public void testTangencial(){
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_WM);
      capsula.params.setMode(Modes.MODE_BASIC);
      capsula.params.setTamMaximoPatron(4);
      capsula.mineria();

      List<List<IAsociacionTemporal>> resultados = capsula.resultados;

      List<IAsociacionTemporal> resultadosTam4 = resultados.get(3);
      IAsociacionTemporal asoc = resultadosTam4.get(0);
      PatronDictionaryFinalEvent p = (PatronDictionaryFinalEvent)asoc.getPatron(0);

      System.out.println("Patron : " + p);
      System.out.println("Padres: " + p.getPadres());
   }

   private class PatronTest extends PatronDictionaryFinalEvent {
      protected PatronTest(int id, String[] tipos){
         super(tipos, false);
         this.patternID = id;
      }
      protected PatronTest(int id, String[] tipos, List<PatronTest> padres) {
         this(id, tipos);
         //this.padres = padres;
         for(PatronTest p:padres) this.padres.add(p);
      }
      public String toString(){
         return this.getTipos().toString() + patternID;
      }
      public boolean equals(Object obj){
         if(obj instanceof Patron) return ((Patron)obj).getID() == this.patternID;
         return false;
      }
   }

   @Test public void testPurgaCandidatosVentana(){
      Evento a = new Evento("A",1);
      Evento b = new Evento("B",2);
      Evento c = new Evento("C",3);
      ListIterator<Evento> ventana = new ArrayList<Evento>(Arrays.asList(a,b,c)).listIterator();

      PatronTest ab1 = new PatronTest(1,new String[]{"A","B"});
      PatronTest ab2 = new PatronTest(2,new String[]{"A","B"});
      PatronTest ac3 = new PatronTest(3,new String[]{"A","C"});
      PatronTest bc4 = new PatronTest(4,new String[]{"B","C"});
      PatronTest abc5 = new PatronTest(5,new String[]{"A","B","C"}, Arrays.asList(ab1,ac3,bc4));
      PatronTest abc6 = new PatronTest(6,new String[]{"A","B","C"}, Arrays.asList(ab2,ac3,bc4));

      List<List<Patron>> anotaciones = new ArrayList<List<Patron>>(Arrays.asList(
            new ArrayList<Patron>(),//a
            Arrays.asList((Patron)ab2),//b
            Arrays.asList((Patron)ac3,(Patron)bc4)//c
      ));

      MineAnotaciones mine = new MineAnotaciones("", true, true, true, null, false);
      int tam = 3;
      mine.iniciarContadores(tam,0);

      List<PatronDictionaryFinalEvent> aComprobar = new ArrayList<PatronDictionaryFinalEvent>();
      aComprobar.add(abc5); aComprobar.add(abc6);

      List<Integer> faltantes = new ArrayList<Integer>(Collections.nCopies(aComprobar.size(), (Integer)null));

      aComprobar = mine.purgeCandidates(aComprobar, Arrays.asList(ac3.getID(),bc4.getID()), faltantes, tam);
      Assert.assertEquals("No se debe borrar ninguno", 2, aComprobar.size());

      mine.purgeCandidatesWindow(aComprobar, faltantes, ventana, anotaciones.listIterator(), c, tam);

      Assert.assertEquals("Solo puede quedar ABC6", 1, aComprobar.size());

      Assert.assertEquals(abc6, aComprobar.get(0));
   }

   @Test public void testGeneral(){
      Collection<Object[]> data =Arrays.asList(new Object[][] {
            {Algorithms.ALG_ANOT, Modes.MODE_BASIC, "apnea", 80, PASADO},
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral((Algorithms)d[0], (Modes)d[1], (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }
   }

   @Test public void testSegmento(){
      ISecuencia secuencia = new SecuenciaSimple(Arrays.asList(
            new Evento("iF",374),
            new Evento("fF",387),
            new Evento("iD",392),
            new Evento("iA",413),
            new Evento("iF",413),
            new Evento("fD",419),
            new Evento("fA",421),
            new Evento("fF",421),
            new Evento("iD",428)
            ));
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setMinFreq(1);
      capsula.params.setAlgorithm(Algorithms.ALG_ANOT);
      capsula.params.setMode(Modes.MODE_BASIC);

      capsula.coleccion = new ColeccionSimple(Arrays.asList(secuencia));

      capsula.mineria();
   }
}

