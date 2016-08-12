package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.IColeccion;

/**
 * Interfaz para los puntos de interrupción de la ejecución paso a paso de los algoritmos.
 * @author vanesa.graino
 *
 */
public interface IPuntoInterrupcion {
   /**
    * Función que recibe un pasoDto (variable de estado), el algoritmo, la colección y devuelve
    * si la interrupción hace efecto o no
    * @param pasoDto
    * @param mine
    * @param coleccion
    * @return true si la interrupción se dispara o false si no lo hace
    */
   boolean interrumpir(List<PasoDTO> pasoDto, AbstractMine mine, IColeccion coleccion);


}


