package source.busqueda;

import java.util.List;

import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.IAsociacionTemporal;

public interface IBusqueda {

   List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException;

   List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win)  throws AlgoritmoException;

   List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException, AlgoritmoException;

}
