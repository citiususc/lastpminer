package source.modelo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import source.modelo.episodios.ModeloEpisodiosTest;
import source.modelo.semilla.ModeloSemillaTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ModeloSemillaTest.class,
   ModeloEpisodiosTest.class
})
public class SuiteModelos {


}
