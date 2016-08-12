package source.modelo.condensacion;

import source.evento.Evento;

public interface ISuperModelo {

   void actualizaVentana(int sid, Evento evento);
   /*int[][] getLimites();
   int[][] getAbiertas();*/
   int[] getTam();
   void omitir(String tipo);
   String[] getTipos();
   int[] fijarEstructuras(IModeloTonto modelo);
   int[] fijarEstructuras(IModeloTontoEpisodios modelo);
   /**
    *
    * @return Número de eventos actualmente en la ventana
    */
   int enVentana();

   //Método para algoritmos con negación (sin episodios por el momento)
   int[] obtenerIndices(String[] tipos);
}
