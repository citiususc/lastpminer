package source.modelo.negacion;

import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.Patron;

/**
 * Tiene dos tipos de eventos para la iteración de inicialización en la
 * que se hará el clustering. Sólo se diferencia de ModeloDistribucion
 * en que implementa los métodos de la interfaz de asociaciones con negación.
 * @author vanesa.graino
 *
 */
public class ModeloDistribucionPositivo extends ModeloDistribucion implements IAsociacionConNegacion{

   /**
    *
    */
   private static final long serialVersionUID = -7420190694719772916L;

   public ModeloDistribucionPositivo(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, frecuencia, clustering);
   }

   public ModeloDistribucionPositivo(String[] tipos, int ventana,
         List<Patron> patrones, Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, patrones, frecuencia, clustering);
   }

   public ModeloDistribucionPositivo(String[] tipos, int ventana,
         List<Patron> patrones, int[] distribucion, IClustering clustering) {
      super(tipos, ventana, patrones, distribucion, clustering);
   }

   @Override
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances) {
      // No se hace nada ya que los dos eventos son positivos
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
   public boolean parteNegativa(){
      return false;
   }

   @Override
   public List<String> getTiposConNegacion(){
      return Arrays.asList(tipos);
   }

   //@Override
   public int compare(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }
}
