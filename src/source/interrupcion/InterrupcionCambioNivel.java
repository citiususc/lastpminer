package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.IColeccion;

/**
 * Interrupción que se activa cuando se cambia al nivel que tiene fijado en su atributo nivel.
 * @author vanesa.graino
 *
 */
public class InterrupcionCambioNivel implements IPuntoInterrupcion{
   private int nivelAnterior;

   @Override
   public boolean interrumpir(List<PasoDTO> pasosDto, AbstractMine mine, IColeccion coleccion){

      for(PasoDTO pasoDto: pasosDto){
         if(pasoDto instanceof IteracionDTO){
             //saber cual es el nivel actual
            int nivelActual = ((IteracionDTO)pasoDto).getNivel();
            return nivelAnterior<nivelActual;
         }
      }
      return false;

   }
}