package source.busqueda;

import java.util.List;

import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;

public interface IBusquedaArbol {
   //Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo sn);
   Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo sn, String tipo) throws FactoryInstantiationException;
   Nodo creaNodoFachada(IAsociacionTemporal modelo) throws FactoryInstantiationException;
   //Nodo creaNodoFachada(IAsociacionTemporal modelo, String tipo);
   Supernodo getRaizArbol();
   List<Supernodo> getNivelActual();

}
