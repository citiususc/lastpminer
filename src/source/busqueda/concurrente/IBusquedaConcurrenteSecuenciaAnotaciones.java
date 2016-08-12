package source.busqueda.concurrente;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Supernodo;
import source.patron.Patron;

public interface IBusquedaConcurrenteSecuenciaAnotaciones extends IBusquedaConcurrenteSecuencia{
   Map<String,List<IAsociacionTemporal>> getMapa();
   Supernodo getRaizArbol();
   List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar);
   Iterator<List<Patron>> getActualIterator(int sid);
   List<Supernodo> getNivelActual();
}
