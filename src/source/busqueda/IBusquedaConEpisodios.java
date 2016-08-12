package source.busqueda;

import java.util.List;

import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.IAsociacionTemporal;

public interface IBusquedaConEpisodios {

   List<Episodio> getListaEpisodios();

   List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios) throws AlgoritmoException;

   List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios) throws AlgoritmoException;

   List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios) throws ModelosBaseNoValidosException, AlgoritmoException;
}
