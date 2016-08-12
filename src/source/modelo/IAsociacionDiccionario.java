package source.modelo;

import java.util.List;

import source.evento.Evento;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public interface IAsociacionDiccionario extends IAsociacionTemporal{

   /**
    * Actualiza la ventana de la asociación temporal sin buscar ocurrencias de patrones.
    * Es decir, si la asociación temporal incluye el tipo del evento (y no se había incluido
    * previamente) se añade a las listas circulares que controlan los eventos que tienen la ventana
    * para la asociación temporal.
    * @param sid - identificador de la secuencia
    * @param evento - evento que se ha leido en el cálculo de soporte
    */
   void actualizaVentana(int sid, Evento evento);

   /**
    * Busca ocurrencias de los patrones contenidos en 'aComprobar' y coloca en 'encontrados' aquellos de los
    * que se encuentra una ocurrencia, pero solo los coloca una vez.
    * No actualiza la ventana
    * @param sid - identificador de la secuencia
    * @param evento - evento que se ha leido en el cálculo de soporte
    * @param savePatternInstances - si se guardará o no una lista con las ocurrencias del patrón encontradas en la colección.
    * @param aComprobar - lista de patrones que pueden encontrarse potencialmente para la asociación temporal.
    * @param encontrados - lista en la que se almacenan los patrones de los que se ha encontrado ocurrencia.
    */
   void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados);
}
