package source.busqueda;

import java.util.List;

import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.IAsociacionTemporal;
import source.modelo.semilla.ModeloSemilla;

public interface IBusquedaConSemillayEpisodios {

   List<Episodio> getListaEpisodios();

   List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios)
               throws SemillasNoValidasException, AlgoritmoException;

   List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios)
               throws SemillasNoValidasException, AlgoritmoException;

   List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios)
               throws ModelosBaseNoValidosException, AlgoritmoException;
}
