package source.modelo.negacion;

import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.ModeloEvento;

/**
 * Modelo para una asociación de tamaño 1 con un único evento positivo.
 * @author vanesa.graino
 *
 */
public class ModeloEventoPositivo extends ModeloEvento implements IAsociacionConNegacion {

   /**
    *
    */
   private static final long serialVersionUID = 8966365911559881996L;

   public ModeloEventoPositivo(String tipoEvento, Integer frecuencia){
      super(tipoEvento, frecuencia);
   }

   @Override
   public void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances) {
      // No se hace nada ya que no hay eventos negados
   }

   @Override
   public String[] getTiposNegados() {
      return new String[0];//ARRAY VACIO
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
   public List<String> getTiposConNegacion() {
      return Arrays.asList(tipoEvento);
   }

   //@Override
   public boolean ultimoPositivo() {
      return true;
   }

   //@Override
   public int compare(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }


}
