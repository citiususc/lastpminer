package source.modelo.paralelo;

import source.modelo.IAsociacionTemporal;

/**
 * Esta interfaz se utiliza en la estrategia paralelizada (PAR y HPAR)
 * @author vanesa.graino
 *
 */
public interface IAsociacionAgregable extends IAsociacionTemporal {
   /**
    * Debe sincronizarse la implementación!!!
    * @param agregada
    */
   void agregar(IAsociacionTemporal agregada);
   IAsociacionTemporal clonar();
   int getHilo();
}
