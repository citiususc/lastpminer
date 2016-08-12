package source.modelo.concurrente.episodios;

import java.util.ArrayList;
import java.util.List;

import source.evento.Episodio;
import source.modelo.clustering.IClustering;
import source.modelo.concurrente.ModeloConcurrenteDistribucion;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.Patron;

public class ModeloConcurrenteDistribucionEpisodios extends ModeloConcurrenteDistribucion{
   /**
    *
    */
   private static final long serialVersionUID = -6758273490928577609L;

   protected EpisodiosWrapper episodios;

   public ModeloConcurrenteDistribucionEpisodios(String[] tipos,
         List<Episodio> episodios, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos) {
      super(tipos, ventana, frecuencia, clustering, numHilos, null);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   public ModeloConcurrenteDistribucionEpisodios(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos) {
      super(tipos, ventana, patrones, frecuencia, clustering, numHilos);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   @Override
   public void incrementarDistribucion(int valor){
      throw new UnsupportedOperationException("Este método no debe llamarse para esta clase");
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      if(episodios.getEventosDeEpisodios()==0){
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloDistribucionHilo(tSize));
         }
      }else{
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloDistribucionEpisodiosHilo(tSize));
         }
      }
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloDistribucionEpisodiosHilo extends ModeloParaleloHilo {

      public ModeloParaleloDistribucionEpisodiosHilo(int tSize) {
         super(tSize);
      }



      //@Override
      protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites,
            int[] indices, int[] instancia){
         return episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
      }
   }
}
