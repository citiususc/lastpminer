package source.busqueda.paralela;

import java.util.List;
import java.util.Map;

import source.modelo.IAsociacionTemporal;

public interface IBusquedaParalela {
   Map<String,List<IAsociacionTemporal>> getMapa();
   boolean isSavePatternInstances();
}
