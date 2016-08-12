package source.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import source.PrincipalTest;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.EventoDeEpisodio;
import source.evento.IColeccion;
import source.evento.InstanciaEpisodio;


public class GenericEpisodesCSVReaderTest {


   @Test
   public void testWithEpisodes() throws IOException, MalformedFileException{
      File f = File.createTempFile("episodes", ".csv", new File(PrincipalTest.TMP_FOLDER));
      f.deleteOnExit();
      FileWriter writer = new FileWriter(f);

      writer.write("0,A,0,0\n");
      writer.write("0,B,1,0\n");
      writer.write("0,C,2,1\n");
      writer.write("0,D,3,1\n");
      writer.write("0,A,5,2\n");
      writer.write("0,W,12,NULL\n");
      writer.write("0,C,12,3\n");
      writer.write("0,D,15,3\n");
      writer.write("0,B,23,2\n");
      writer.close();

      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      List<InstanciaEpisodio> instancias = new ArrayList<InstanciaEpisodio>();

      IColeccion coleccion = GenericEpisodesCSVReader.parseFiles(tipos, episodios, instancias, f);

      Assert.assertEquals("Debería haber una única secuencia",1, coleccion.size());
      Assert.assertEquals("Debería haber 2 tipos de episodios: " + episodios,2, episodios.size());
      Assert.assertEquals("Debería haber 4 instancias de episodios",4, instancias.size());

      Assert.assertTrue("El evento de inicio episodio no se ha creado como episodio", coleccion.get(0).get(0) instanceof EventoDeEpisodio);
      Assert.assertTrue("El evento de fin episodio no se ha creado como episodio", coleccion.get(0).get(1) instanceof EventoDeEpisodio);
      Assert.assertFalse("El evento puntual se ha creado como episodio", coleccion.get(0).get(5) instanceof EventoDeEpisodio);
      Assert.assertEquals("No se han emparejado los episodios correctamente", coleccion.get(0).get(8),
            ((EventoDeEpisodio)coleccion.get(0).get(4)).getInstancia().getFin());

   }

   @Test
   public void testWithoutEpisodes() throws IOException, MalformedFileException{
      File f = File.createTempFile("no-episodes", ".csv", new File(PrincipalTest.TMP_FOLDER));
      f.deleteOnExit();
      FileWriter writer = new FileWriter(f);
      writer.write("0,A,0\n");
      writer.write("0,B,1\n");
      writer.write("0,C,2\n");
      writer.write("0,D,3\n");
      writer.write("0,A,5\n");
      writer.write("0,W,12\n");
      writer.write("0,C,12\n");
      writer.write("0,D,15\n");
      writer.write("0,B,23\n");
      writer.close();

      List<String> tipos = new ArrayList<String>();

      IColeccion coleccion = GenericEpisodesCSVReader.parseFiles(tipos, null, null, f);

      Assert.assertEquals("Debería haber una única secuencia",1, coleccion.size());
      Assert.assertEquals("Evento incorrecto", new Evento("A", 0), coleccion.get(0).get(0));
      Assert.assertEquals("Evento incorrecto", new Evento("W",12), coleccion.get(0).get(5));
      Assert.assertEquals("Evento incorrecto", new Evento("B",23), coleccion.get(0).get(8));
   }

   @Test
   public void testWithEpisodesDefinition() throws MalformedFileException, IOException{
      File f = File.createTempFile("episodes-definition", ".csv", new File(PrincipalTest.TMP_FOLDER));
      f.deleteOnExit();
      FileWriter writer = new FileWriter(f);
      writer.write("0,A,0\n");
      writer.write("0,B,1\n");
      writer.write("0,C,2\n");
      writer.write("0,D,3\n");
      writer.write("0,A,5\n");
      writer.write("0,W,12\n");
      writer.write("0,C,12\n");
      writer.write("0,D,15\n");
      writer.write("0,B,23\n");
      writer.close();

      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      episodios.add(new Episodio("A","B"));
      episodios.add(new Episodio("C","D"));
      List<InstanciaEpisodio> instancias = new ArrayList<InstanciaEpisodio>();

      IColeccion coleccion = GenericEpisodesCSVReader.parseFiles(tipos, episodios, instancias, f);

      Assert.assertEquals("Debería haber una única secuencia",1, coleccion.size());
      Assert.assertEquals("Evento incorrecto", new Evento("A", 0), coleccion.get(0).get(0));
      Assert.assertSame("",coleccion.get(0).get(8), ((EventoDeEpisodio)coleccion.get(0).get(4)).getInstancia().getFin() );
      Assert.assertEquals("Evento incorrecto", new Evento("W",12), coleccion.get(0).get(5));
      Assert.assertEquals("Evento incorrecto", new Evento("B",23), coleccion.get(0).get(8));
      Assert.assertTrue("evento w deberia ser instancia de Evento", coleccion.get(0).get(5) instanceof Evento);
      Assert.assertFalse("evento w no deberia ser instancia de EventoDeEpisodio", coleccion.get(0).get(5) instanceof EventoDeEpisodio);


      System.out.println(coleccion);
      System.out.println(instancias);
      Assert.assertEquals("Numero incorrecto de instancias de episodios", 4, instancias.size());
   }

   @Test
   public void testBase17(){
      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      List<InstanciaEpisodio> instancias = new ArrayList<InstanciaEpisodio>();

      GenericEpisodesCSVReader.parseFiles(tipos, episodios, instancias, "/home/remoto/vanesa.graino/workspace/generador/bbdds/",
            "Base17.csv", true);
   }
}
