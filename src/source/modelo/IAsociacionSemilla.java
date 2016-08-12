package source.modelo;

import source.modelo.clustering.IClustering;

public interface IAsociacionSemilla extends IAsociacionTemporal {
	/**
	 * Distribuciones de distancias temporales entre los pares de eventos de la semilla
	 * @return
	 */
	int[][] getDistribuciones();


   /**
    * Devuelve el nombre de la clase utilizada para el clustering de distribuciones de frecuencia
    */
   IClustering getClustering();

   /**
    * 
    * @param indiceDistribucion
    * @param valor
    */
   void incrementarDistribucion(int indiceDistribucion, int valor);

}
