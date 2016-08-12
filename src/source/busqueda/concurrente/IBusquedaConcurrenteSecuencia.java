package source.busqueda.concurrente;

import source.evento.IColeccion;

public interface IBusquedaConcurrenteSecuencia {
   int getSiguienteSecuencia(IColeccion coleccion);
   boolean isSavePatternInstances();
}
