package source.modelo.negacion;

import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionTemporal;

public interface IAsociacionConNegacion extends IAsociacionTemporal {

   String PREF_NEG = "-";
   String SUF_NEG = "-";

   /**
    * Devuelve un array con los tipos negativos de la asociación
    * @return
    */
   String[] getTiposNegados();
   /**
    *
    * @return true si la asociación tiene una parte positiva
    */
   boolean partePositiva();
   /**
    *
    * @return true is la asociación tiene una parte negativa
    */
   boolean parteNegativa();

   /**
    * Devuelve una lista que combina los tipos de eventos positivos en una
    * Los eventos negativos pueden tener un sufijo o un prefijo para
    * diferenciarlos (depende la implemementación)
    * @return
    */
   List<String> getTiposConNegacion();

   /**
    *
    * @param sid
    * @param ev
    * @param savePatternInstances
    */
   void saleEventoNegado(int sid, Evento ev, boolean savePatternInstances);

}
