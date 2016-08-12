package source.modelo.concurrente;

import java.util.List;

import source.evento.Evento;
import source.patron.Patron;

public interface IAsociacionDiccionarioConcurrente extends IAsociacionTemporalConcurrente {

   /*
    * Actualiza la ventana de la asociación temporal sin buscar ocurrencias de patrones.
    */
   void actualizaVentana(int hilo, int sid, Evento evento);

   /*
    *   Busca ocurrencias de los patrones contenidos en 'aComprobar' y coloca en 'encontrados' aquellos de los
    * que se encuentra una ocurrencia, pero solo los coloca una vez.
    *   No actualiza la ventana
    */
   void recibeEvento(int hilo, int sid, Evento evento, boolean savePatternInstances,
         List<PatronConcurrenteDFE> aComprobar, List<Patron> encontrados);

}
