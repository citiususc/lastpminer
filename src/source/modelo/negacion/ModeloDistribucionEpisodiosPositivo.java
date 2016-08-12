package source.modelo.negacion;

import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.clustering.IClustering;
import source.modelo.episodios.ModeloDistribucionEpisodios;
import source.patron.Patron;

/**
 * Sólo tiene tipos positivos.
 * @author vanesa.graino
 *
 */
public class ModeloDistribucionEpisodiosPositivo extends
      ModeloDistribucionEpisodios implements IAsociacionConNegacion{

   /**
    *
    */
   private static final long serialVersionUID = 512104592425005568L;

   public ModeloDistribucionEpisodiosPositivo(String[] tipos,
         List<Episodio> episodios, int ventana, Integer frecuencia,
         IClustering clustering) {
      super(tipos, episodios, ventana, frecuencia, clustering);
   }

   public ModeloDistribucionEpisodiosPositivo(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering) {
      super(tipos, episodios, ventana, patrones, frecuencia, clustering);
   }

   public ModeloDistribucionEpisodiosPositivo(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         int[] distribucion, IClustering clustering) {
      super(tipos, episodios, ventana, patrones, distribucion, clustering);
   }

   @Override
   public String[] getTiposNegados() {
      return new String[0];
   }

   @Override
   public boolean partePositiva() {
      return true;
   }

   @Override
   public boolean parteNegativa() {
      return false;
   }

   @Override
   public List<String> getTiposConNegacion() {
      return Arrays.asList(tipos);
   }

   @Override
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances) {
      // No se hace nada ya que los dos eventos son positivos
   }

   //@Override
   public int compare(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }

}
