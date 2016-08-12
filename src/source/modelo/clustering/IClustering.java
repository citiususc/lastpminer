package source.modelo.clustering;

import java.util.List;

import source.restriccion.RIntervalo;

public interface IClustering {

   // Método de agrupamiento
   List<RIntervalo> agrupar(int[] df, String tipoA, String tipoB);

}
