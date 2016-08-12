package source.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import source.configuracion.ConfigurationParameters;
import source.evento.ColeccionSimple;
import source.evento.Episodio;
import source.evento.InstanciaEpisodio;


public class ApneaReaderTest {

   @Test
   public void test() throws MalformedFileException{
      ConfigurationParameters config = new ConfigurationParameters();
      config.setCollection(ConfigurationParameters.APNEA_DB);
      config.setInputFileName("apnea-sorted.txt");
      config.setInputPath("/home/remoto/vanesa.graino/Tese/");
      List<String> tipos = new ArrayList<String>();
      List<Episodio> episodios = new ArrayList<Episodio>();
      List<InstanciaEpisodio> instancias = new ArrayList<InstanciaEpisodio>();
      ColeccionSimple sahs = (ColeccionSimple)ApneaReader.parseFiles(config, tipos, episodios, instancias);
      Assert.assertEquals("No se han leido 50 secuencias", 50, sahs.size());

      sahs.sort();
      ApneaReader.writeFile(sahs, "/home/remoto/vanesa.graino/Tese/apnea-sorted.txt");
   }
}
