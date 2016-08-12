package source.modelo.condensacion;

import source.modelo.IAsociacionConEpisodios;

public interface IModeloTontoEpisodios extends IModeloTonto, IAsociacionConEpisodios {
   public int getEventosDeEpisodios();
   String[] getTiposReordenados();

}
