package source.busqueda;

import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.patron.Patron;

public interface IBusquedaDiccionarioConEpisodios extends IBusquedaArbol, IBusquedaConEpisodios{

   /**
    *
    * @param actual
    * @param tiposAmpliar
    * @param evento
    * @return
    */
   List<String> posiblesTiposParaAmpliarNoEpisodios(List<Patron> actual, List<String> tiposAmpliar, Evento evento);

   /**
    * Determina
    * @param episodiosAmpliar
    * @return
    */
   List<Episodio> posiblesEpisodiosParaAmpliar(List<Episodio> episodiosAmpliar, Evento evento);

}
