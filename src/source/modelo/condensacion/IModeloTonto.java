package source.modelo.condensacion;

import source.modelo.IAsociacionConEpisodios;

public interface IModeloTonto extends IAsociacionConEpisodios{

   //String[] getTipos();
   int[][] getLimites();
   int[][] getAbiertas();
   void setTamColeccion(int[] tam);
   boolean sonEpisodiosCompletos();

}
