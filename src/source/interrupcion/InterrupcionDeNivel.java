package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.IColeccion;

/**
 * Interrupción que se activa cuando se cambia al nivel que tiene fijado en su atributo nivel.
 * @author vanesa.graino
 *
 */
public class InterrupcionDeNivel implements IPuntoInterrupcion{
   private final int nivel;
   private int nivelAnterior;

   public InterrupcionDeNivel(int nivel){
      this.nivel = nivel;
   }

   @Override
   public boolean interrumpir(List<PasoDTO> pasosDto, AbstractMine mine, IColeccion coleccion){

      for(PasoDTO pasoDto: pasosDto){
         if(pasoDto instanceof IteracionDTO){
             //saber cual es el nivel actual
            int nivelActual = ((IteracionDTO)pasoDto).getNivel();
            boolean interrumpir = nivelActual == nivel && nivelAnterior<nivelActual;
            nivelAnterior = nivel;
            if(interrumpir){ return true; }
         }
      }
      return false;

   }
}