package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.IColeccion;

public class InterrupcionContinua implements IPuntoInterrupcion{
   @Override
   public boolean interrumpir(List<PasoDTO> pasoDto, AbstractMine mine, IColeccion coleccion){
      return true;
   }
}