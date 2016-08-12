package source.modelo;

import java.util.List;

import source.evento.Episodio;

public interface IAsociacionConEpisodios extends IAsociacionTemporal{

   /*
    * Devuelve aquellos episodios de los que contiene al menos un tipo.
    */
   List<Episodio> getEpisodios();

   /*
    * Dice si, para todo episodio obtenido con 'getEpisodios()', contiene ambos tipos de eventos.
    */
   boolean sonEpisodiosCompletos();


/*   public void setEventosDeEpisodios(int eventosDeEpisodios);

   void setTiposReordenados(List<String> tiposReordenados);

   void setEquivalenciaTipos(int[] equivalenciasTipos);

   void setRepReordenados(int[] repReordenados);

   void setEpisodiosCompletos(boolean episodiosCompletos);

   int[] getRep();*/
}
