package source.busqueda.paralela;

import source.evento.IColeccion;

/**
 *
 * @author vanesa.graino
 *
 */
public interface IBusquedaParalelaSecuencia extends IBusquedaParalela {
   //void addEventosEliminados(List<EventoEliminado> eliminadosSecuencia, int sSize, int eliminados);
   int getSiguienteSecuencia(IColeccion coleccion);
   //public List<Thread> crearHilos(IColeccion coleccion, int tamActual) throws FactoryInstantiationException;
}
