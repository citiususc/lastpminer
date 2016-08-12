package source.modelo.condensacion.episodios;

import java.util.List;

import source.evento.Episodio;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.IModeloTontoEpisodios;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.episodios.ModeloDistribucionEpisodios;
import source.patron.Patron;

/**
 * No se está utilizando. No se utiliza para tamaño 2 con SuperModelo.
 * @author vanesa.graino
 *
 */
public class ModeloDistribucionEpisodiosTonto extends ModeloDistribucionEpisodios implements IModeloTontoEpisodios{
   private static final long serialVersionUID = -3608919576490325340L;

   /*
    * Atributos de tonto
    */

   protected int[] indices;
   protected int[] tamColeccion;

   public ModeloDistribucionEpisodiosTonto(String[] tipos,
         List<Episodio> episodios, int ventana,
         Integer frecuencia, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, frecuencia, clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloDistribucionEpisodiosTonto(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, patrones, frecuencia,
            clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);

   }

   public ModeloDistribucionEpisodiosTonto(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         int[] distribucion, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, patrones, distribucion, clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   @Override
   public void setTamColeccion(int[] tam) {
      this.tamColeccion = tam;
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getLimites()
    */
   @Override
   public int[][] getLimites() {
      return super.getLimites();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getAbiertas()
    */
   @Override
   public int[][] getAbiertas() {
      return super.getAbiertas();
   }

   @Override
   public int getEventosDeEpisodios() {
      return episodios.getEventosDeEpisodios();
   }

   @Override
   public String[] getTiposReordenados() {
      return episodios.getTiposReordenados();
   }
}

