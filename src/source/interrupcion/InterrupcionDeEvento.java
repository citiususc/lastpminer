package source.interrupcion;

import java.util.List;

import source.busqueda.AbstractMine;
import source.evento.Evento;
import source.evento.IColeccion;

public class InterrupcionDeEvento implements IPuntoInterrupcion{
   protected String tipoEvento;

   public InterrupcionDeEvento(String tipoEvento){
      this.tipoEvento = tipoEvento;
   }

   @Override
   public boolean interrumpir(List<PasoDTO> pasosDto, AbstractMine mine, IColeccion coleccion){
      for(PasoDTO pasoDto: pasosDto){
         if(pasoDto instanceof CalculoSoportePasoDTO){
            CalculoSoportePasoDTO dto = (CalculoSoportePasoDTO)pasoDto;
            Evento ev = coleccion.get(dto.getSid()).get(dto.getEid());
            return tipoEvento.equals(ev.getTipo());
         }
      }
      return false;
   }
}