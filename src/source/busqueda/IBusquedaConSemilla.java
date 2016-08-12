package source.busqueda;

import java.util.List;

import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.IAsociacionTemporal;
import source.modelo.semilla.ModeloSemilla;

public interface IBusquedaConSemilla {

   List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException;

   /**
    *
    * @param tipos
    * @param coleccion
    * @param semillas
    * @param supmin
    * @param win
    * @return Una lista con las asociaciones temporales de tamaño 2 que contienen las distribuciones de frecuencia. Esta
    * lista puede estar vacía.
    * @throws SemillasNoValidasException
    * @throws AlgoritmoException
    */
   List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException, AlgoritmoException;

   List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException, AlgoritmoException;

}
