package source.busqueda.paralela;

import java.util.List;
import java.util.Map;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDeHilo;
import source.modelo.IAsociacionTemporal;

public class HiloSoporte implements Runnable {
   protected IColeccion coleccion;
   protected int hilo;
   protected Map<String,List<IAsociacionTemporal>> mapa;
   protected boolean savePatternInstances;

   public HiloSoporte(IColeccion coleccion, int hilo, Map<String,List<IAsociacionTemporal>> mapa, boolean savePatternInstances){
      this.coleccion = coleccion;
      this.hilo = hilo;
      this.mapa = mapa;
      this.savePatternInstances = savePatternInstances;
   }

   public void run(){
      int sid=0;
      for(ISecuencia secuencia : coleccion){
         for(Evento ev : secuencia){
            List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               if(((IAsociacionDeHilo)receptor).getHilo() == hilo){
                  receptor.recibeEvento(sid, ev, savePatternInstances);
               }
            }
         }
         sid++;
      }
   }
}