package source.busqueda.concurrente.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.busqueda.concurrente.HiloConcurrente;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionDiccionarioConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.patron.Patron;

public class HiloConcurrenteTam3 extends HiloConcurrente{

   protected IBusquedaConcurrenteSecuenciaAnotaciones mine;

   public HiloConcurrenteTam3(int numHilo, IBusquedaConcurrenteSecuenciaAnotaciones mine,
         IColeccion coleccion, Map<String, List<IAsociacionTemporal>> mapa) {
      super(numHilo, mine, coleccion, mapa);
      this.mine = mine;
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();
         // Se recorre la lista de la tabla hash
         List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
         for(IAsociacionTemporal receptor : receptores){

            List<Patron> aux = receptor.getPatrones();
            List<PatronConcurrenteDFE> lista = new ArrayList<PatronConcurrenteDFE>();
            for(Patron patron : aux){
               lista.add((PatronConcurrenteDFE)patron);
            }
            ((IAsociacionDiccionarioConcurrente)receptor).actualizaVentana(numHilo, sid, evento);
            ((IAsociacionDiccionarioConcurrente)receptor).recibeEvento(numHilo, sid, evento, mine.isSavePatternInstances(), lista, ventanaActual);
         }
      }
   }

}
