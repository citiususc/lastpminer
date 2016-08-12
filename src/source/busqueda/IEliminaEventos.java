package source.busqueda;

import java.util.List;

import source.evento.EventoEliminado;

/**
 * Define los métodos que deben implementar aquellos algoritmos de minería de datos que eliminen eventos
 * de la colección de secuencias de entrada.
 * @author Miguel
 */
public interface IEliminaEventos {


   /**
    * @return Devuelve una lista de listas con los eventos eliminados de la colección durante la ejecución.
    * La primera lista determina la secuencia, mientras que la segunda contiene todos los eventos
    * eliminados de esa secuencia a lo largo de todas las iteraciones.
    */
   List<List<EventoEliminado>> getEventosEliminados();

   /**
    *
    * @return Devuelve si se guarda o no un registro de los eventos que han sido borrados
    */
   boolean isSaveRemovedEvents();

}
