package source.busqueda.concurrente;

import java.util.List;
import java.util.Map;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionTemporalConcurrente;

public class HiloConcurrente implements Runnable {
   protected Map<String,List<IAsociacionTemporal>> mapa;
   protected int numHilo;
   protected IColeccion coleccion;
   protected IBusquedaConcurrenteSecuencia mine;

   public HiloConcurrente(int numHilo, IBusquedaConcurrenteSecuencia mine, IColeccion coleccion,
         Map<String,List<IAsociacionTemporal>> mapa){
      this.numHilo = numHilo;
      this.mine = mine;
      this.coleccion = coleccion;
      this.mapa = mapa;
   }

   @Override
   public void run() {
      int sid = mine.getSiguienteSecuencia(coleccion);
      while(sid != -1){
         calcularSoporte(coleccion.get(sid), sid);
         sid = mine.getSiguienteSecuencia(coleccion);
      }
   }

   protected void calcularSoporte(ISecuencia secuencia, int sid){
      for(Evento ev : secuencia){
         List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
         for(IAsociacionTemporal receptor : receptores){
            ((IAsociacionTemporalConcurrente)receptor).recibeEvento(numHilo,sid,ev,mine.isSavePatternInstances());
         }
      }
   }
}
