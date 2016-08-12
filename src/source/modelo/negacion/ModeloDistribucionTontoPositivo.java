package source.modelo.negacion;

import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloDistribucionTonto;
import source.patron.Patron;

/**
 * Esta clase no se está utilizando. Como {@link ModeloDistribucionPositivo} en recibeEvento
 * también actualiza la ventana no es necesario utilizar el Supermodelo.
 * @author vanesa.graino
 *
 */
public class ModeloDistribucionTontoPositivo extends ModeloDistribucionTonto implements IAsociacionConNegacion {

   /**
    *
    */
   private static final long serialVersionUID = -1540982628519164224L;

   public ModeloDistribucionTontoPositivo(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, ventana, frecuencia, clustering, supermodelo);
   }

   public ModeloDistribucionTontoPositivo(String[] tipos, int ventana,
         List<Patron> patrones, Integer frecuencia, IClustering clustering,
         ISuperModelo supermodelo) {
      super(tipos, ventana, patrones, frecuencia, clustering, supermodelo);
   }

   public ModeloDistribucionTontoPositivo(String[] tipos, int ventana,
         List<Patron> patrones, int[] distribucion, IClustering clustering,
         ISuperModelo supermodelo) {
      super(tipos, ventana, patrones, distribucion, clustering, supermodelo);
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
