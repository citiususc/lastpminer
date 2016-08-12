package source.modelo;

import java.util.Arrays;
//import java.util.logging.Logger;

import source.evento.Evento;

/**
 * Este tipo de asociación temporal no tiene patrones. Se utiliza en la versión lazy
 * del algoritmo, para crear las asociaciones del nivel siguiente y contabilizar si
 * las propias asociaciones son frecuentes. Si no es el caso, los patrones para dicha
 * asociación no se generarán en la iteración siguiente.
 * Por ejemplo, si estamos en la iteración 3, se generarían las asociaciones y los
 * patrones de tamaño 3 así como las asociaciones de tamaño 4 con ModeloAsociacion.
 * En la iteración siguiente, la 4, sólo se generarán los patrones para las asociaciones
 * que han demostrado ser viables siendo frecuentes. Además, se generán las asociaciones
 * de tamaño 5. Así sucesivamente.
 * @author vanesa.graino
 *
 */
public class ModeloAsociacion extends Modelo {
   //private static final Logger LOGGER = Logger.getLogger(ModeloAsociacion.class.getName());
   private static final long serialVersionUID = 8722068219251852434L;

   public ModeloAsociacion(String[] tipos, int ventana, Integer frecuencia) {
      super(tipos, ventana, frecuencia);
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo(); //tipo del evento
      int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo); //indice del tipo de evento en la asociación temporal

      int tSize = tipos.length; //tamaño de la asociacion temporal

      int tmp = ev.getInstante(); //instante del evento
      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int frecuenciaLocal=0;
      int[] indices = new int[tSize];
      int[] tam = getTam();

      // Actualizar frecuencias
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      do{ // Recorre cada lista
         frecuenciaLocal++;
         indices = siguienteCombinacion(tam, indices, index, tipo);
      }while(indices != null);

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);

   }

}
