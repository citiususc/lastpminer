package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.IColeccion;

public class InterrupcionDeSecuencia implements IPuntoInterrupcion{
   protected int sid;

   public InterrupcionDeSecuencia(int sid){
      this.sid = sid;
   }

   @Override
   public boolean interrumpir(List<PasoDTO> pasosDto, AbstractMine mine, IColeccion coleccion){
      for(PasoDTO pasoDto: pasosDto){
         if(pasoDto instanceof CalculoSoportePasoDTO){
            CalculoSoportePasoDTO dto = (CalculoSoportePasoDTO)pasoDto;
            return dto.getSid() == sid;
         }
      }
      return false;
   }
}