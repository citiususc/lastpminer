package source.busqueda.paralela;

import java.util.Iterator;
import java.util.List;
import source.modelo.arbol.Supernodo;
import source.patron.Patron;

public interface IBusquedaParalelaSecuenciaAnotaciones extends IBusquedaParalelaSecuencia {
   Supernodo getRaizArbol();
   List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar);
   Iterator<List<Patron>> getActualIterator(int sid);
   List<Supernodo> getNivelActual();
   boolean isSavePatternInstances();
}
