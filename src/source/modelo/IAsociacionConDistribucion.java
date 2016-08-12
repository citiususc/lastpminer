package source.modelo;

import source.modelo.clustering.IClustering;

public interface IAsociacionConDistribucion extends IAsociacionTemporal{
   /**
    * Devuelve las distribuciones de frecuencia de la asociación temporal.
    */
   int[] getDistribucion();

   /**
    * Devuelve el nombre de la clase utilizada para el clustering de distribuciones de frecuencia
    */
   IClustering getClustering();

   void incrementarDistribucion(int valor);

   /**
    * Los tipos que se utilizarán para las restricciones temporales.
    * Se ha incluido este método para cuando hay tipos repetidos.
    * @return
    */
   String[] getTiposRestricciones();
}

