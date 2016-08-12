package source.modelo.concurrente;

import source.evento.Evento;
import source.modelo.IAsociacionTemporal;

public interface IAsociacionTemporalConcurrente extends IAsociacionTemporal{

   /**
    * Método 'observador'. Se le notifica a la asociación temporal que el
    * evento 'ev' ha entrado en la ventana temporal y el identificador de
    * la secuencia a la que pertenece 'sid', para que haga las actualizaciones
    * de frecuencia de los patrones que sean necesarias.
    */
   void recibeEvento(int hilo, int sid, Evento ev, boolean savePatternInstances);

}
