package source.busqueda.paralela.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.busqueda.paralela.HiloSoporte;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionDeHilo;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class HiloSoporteTam3 extends HiloSoporte {
   protected Iterator<List<List<Patron>>> itActual;

   public HiloSoporteTam3(IColeccion coleccion, int hilo,
         Map<String, List<IAsociacionTemporal>> mapa, boolean savePatternInstances,
         List<List<List<Patron>>> actual){
      super(coleccion, hilo, mapa, savePatternInstances);
      itActual = actual.iterator();
   }

   @Override
   public void run() { // ventana actual tiene que estar sincronizada -> se sincroniza en el modelo
      int sid=0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            //ventanaActual = Collections.synchronizedList(ventanaActual);
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               if(((IAsociacionDeHilo)receptor).getHilo() == hilo){
                  List<Patron> aux = receptor.getPatrones();
                  List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
                  for(Patron patron : aux){
                     lista.add((PatronDictionaryFinalEvent)patron);
                  }
                  ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
                  ((IAsociacionDiccionario)receptor).recibeEvento(sid, evento, savePatternInstances, lista, ventanaActual);
               }
            }
         }
         sid++;
      }
   }
}