package source.modelo.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.configuracion.ConfigurationParameters;
import source.configuracion.HelperConfiguration;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.ClusteringFactory;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.restriccion.RIntervalo;

@RunWith(Parameterized.class)
public class ModeloEpisodiosTest {
   private static final int VENTANA = 4;
   private static final IClustering CLUSTERING;

   static {
      ConfigurationParameters params = new ConfigurationParameters();
      CLUSTERING = ClusteringFactory.getClustering(params.getClusteringClassName());
      HelperConfiguration.setConfiguration(params, CLUSTERING);
   }

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {coleccionEjemplo1(), asociacionEjemploTam2BD(), 0}, //0
            {coleccionEjemplo1(), asociacionEjemploTam4ABCD(), 0}, //1

            {coleccionEjemplo2(), asociacionEjemploTam2BD(), 1}, //2
            {coleccionEjemplo2(), asociacionEjemploTam4ABCD(), 1}, //3

            {coleccionEjemplo3(), asociacionEjemploTam2BD(), 0}, //4
            {coleccionEjemplo3(), asociacionEjemploTam4ABCD(), 0}, //5

            {coleccionEjemplo4(), asociacionEjemploTam2BD(), 1}, //6
            {coleccionEjemplo4(), asociacionEjemploTam4ABCD(), 2}, //7

            {coleccionEjemplo5(), asociacionEjemploTam2BD(), 1}, //8
            {coleccionEjemplo5(), asociacionEjemploTam4ABCD(), 2}, //9
      });
   };


   /**
    * Esta coleccion tiene los eventos de fin e inicio de un tipo de episodio al mismo tiempo.
    * Por tanto, no debería haber ocurrencias de asociaciones en ella.
    * @return
    */
   private static IColeccion coleccionEjemplo1(){
      return new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(Arrays.asList(
            new Evento("A",0),
            new Evento("B",1),
            new Evento("C",1),
            new Evento("D",1)
      ))));
   }

   /**
    * Esta colección debería tener ocurrencias de ambas asociaciones.
    * @return
    */
   private static IColeccion coleccionEjemplo2(){
      return new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(Arrays.asList(
            new Evento("A",0),
            new Evento("B",1),
            new Evento("C",2),
            new Evento("D",2)
      ))));
   }

   /**
    * Cuando entra el fin de episodio el inicio ya ha salido.
    * No tiene ocurrencias.
    * @return
    */
   private static IColeccion coleccionEjemplo3(){
      return new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(Arrays.asList(
            new Evento("A",0),
            new Evento("B",0),
            new Evento("C",1),
            new Evento("D",4)
      ))));
   }

   private static IColeccion coleccionEjemplo4(){
      return new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(Arrays.asList(
            new Evento("A",0),
            new Evento("A",1),
            new Evento("B",1),
            new Evento("C",1),
            new Evento("D",2)
      ))));
   }

   private static IColeccion coleccionEjemplo5(){
      return new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(Arrays.asList(
            new Evento("D",0),
            new Evento("A",0),
            new Evento("A",1),
            new Evento("B",1),
            new Evento("C",1),
            new Evento("D",2)
      ))));
   }


   private static IAsociacionConEpisodios asociacionEjemploTam4ABCD(){
      String[] tipos = new String[]{"A","B","C","D"};

      List<RIntervalo> rests = new ArrayList<RIntervalo>();
      rests.add(new RIntervalo("A","B",-VENTANA, VENTANA));
      rests.add(new RIntervalo("A","C",-VENTANA, VENTANA));
      rests.add(new RIntervalo("A","D",-VENTANA, VENTANA));
      rests.add(new RIntervalo("B","C",-VENTANA, VENTANA));
      rests.add(new RIntervalo("B","D", 1, VENTANA));
      rests.add(new RIntervalo("C","D",-VENTANA, VENTANA));
      Patron p = new Patron(tipos, rests, true);
      IAsociacionConEpisodios asoc = new ModeloEpisodios(tipos, Arrays.asList(new Episodio("B","D")), VENTANA, Arrays.asList(p), 0);
      return asoc;
   }

   private static IAsociacionConEpisodios asociacionEjemploTam2BD(){
      return new ModeloDistribucionEpisodios(new String[]{"B", "D"},
            Arrays.asList(new Episodio("B","D")), VENTANA, 0, CLUSTERING);
   }

   protected IColeccion coleccion;
   protected int frecuencia;
   protected IAsociacionTemporal asoc;

   public ModeloEpisodiosTest(IColeccion coleccion, IAsociacionTemporal asoc, int frecuencia){
      this.coleccion = coleccion;
      this.frecuencia = frecuencia;
      this.asoc = asoc;
   }

   @Test
   public void testFrecuenciaModelo(){
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         for(Evento ev : secuencia){
            if(Arrays.asList(asoc.getTipos()).contains(ev.getTipo())){
               asoc.recibeEvento(sid, ev, false);
            }
         }
         sid++;
      }
      Assert.assertEquals("Para la asociación: " + asoc, frecuencia, asoc.getSoporte());
   }
}
