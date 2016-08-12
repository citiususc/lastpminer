package source.modelo.negacion;

import source.evento.Evento;
import source.modelo.IMarcasIntervalos;

public class ModeloEventoPositivoMarcas extends ModeloEventoPositivo implements IMarcasIntervalos{

   /*
    * Atributos
    */

   /**
    *
    */
   private static final long serialVersionUID = -5882757095989713377L;
   protected int[] ultimaEncontrada = {0,0};

   /*
    * Constructores
    */

   public ModeloEventoPositivoMarcas(String tipoEvento, Integer frecuencia) {
      super(tipoEvento, frecuencia);
   }

   /*
    * Métodos
    */

   @Override
   public int[] getUltimaEncontrada() {
      return ultimaEncontrada;
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      ultimaEncontrada[0] = ev.getInstante();
      ultimaEncontrada[1] = ev.getInstante();
      frecuencia++;
   }

   //@Override
   public int compare(IAsociacionConNegacion o) {
      // TODO Auto-generated method stub
      return 0;
   }

}
