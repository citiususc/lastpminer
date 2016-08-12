package source.busqueda.concurrente.semilla;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import source.busqueda.AbstractMine;
import source.busqueda.concurrente.HiloConcurrente;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuencia;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionTemporalConcurrente;
import source.modelo.concurrente.IMarcasIntervaloConcurrente;

public class HiloConcurrenteSemilla extends HiloConcurrente{

   public HiloConcurrenteSemilla(int numHilo, IBusquedaConcurrenteSecuencia mine, IColeccion coleccion,
         Map<String,List<IAsociacionTemporal>> mapa/*, List<IAsociacionTemporal> candidatas*/){
      super(numHilo, mine, coleccion, mapa);
      this.mine = mine;
   }

   @Override
   public void run() {
      int sid = mine.getSiguienteSecuencia(coleccion);
      while(sid != -1){
         //System.out.println("Hilo #" + hilo + " con secuencia " + sid);
         calcularSoporteSemilla(coleccion.get(sid), sid);
         sid = mine.getSiguienteSecuencia(coleccion);
      }
      //mine.agregarCandidatos(candidatosOriginales, candidatos);
   }

   protected void calcularSoporteSemilla(ISecuencia secuencia, int sid){
      if(secuencia.isEmpty()){ return; }
      Evento ev, bv; // Evento leído y Evento de principio de ventana
      ListIterator<Evento> inicioVentana, finalVentana;
      //Map<String,List<IAsociacionTemporal>> mapa = getMapa();
      int[] ultimaOcurrencia, intervaloActual = {0,0};
      List<int[]> intervalosActivos = new ArrayList<int[]>();

      inicioVentana = secuencia.listIterator();
      bv = inicioVentana.next(); // Evento de principio de ventana

      //int sSize = secuencia.size(), eliminados = 0;
      int tmp;
      //List<Evento> copia = new ArrayList<Evento>(secuencia);
      ISecuencia copia = secuencia.clone();
      finalVentana = copia.listIterator();

      while(finalVentana.hasNext()){
         ev = finalVentana.next();
         tmp = ev.getInstante();
         List<IAsociacionTemporal> receptores = mapa.get(ev.getTipo());
         boolean hayOcurrencia=false;
         intervaloActual[0]=tmp;
         intervaloActual[1]=tmp;
         if(receptores != null){
            for(IAsociacionTemporal receptor : receptores){
               // Calcular el intervalo de eventos que incluyen ocurrencias
               IMarcasIntervaloConcurrente modelo = (IMarcasIntervaloConcurrente) receptor;
               int antes = modelo.getSoporte(numHilo);
               ((IAsociacionTemporalConcurrente)modelo).recibeEvento(numHilo, sid, ev, mine.isSavePatternInstances());
               int despues = modelo.getSoporte(numHilo);
               if(antes!=despues){ // Se encontró alguna ocurrencia
                  hayOcurrencia=true;
                  ultimaOcurrencia = modelo.getUltimaEncontrada(numHilo);
                  if(intervaloActual[0]>ultimaOcurrencia[0]){
                     intervaloActual[0]=ultimaOcurrencia[0];
                  }
               }
            }
         }
         if(hayOcurrencia){
            // 'intervaloActual' contiene el intervalo más grande de eventos incluidos en una ocurrencia
            // Recorrer la lista de intervalos activos
            boolean algunoActualizado=false;
            for(int[] intervalo : intervalosActivos){
               // comprobar si 'intervaloActual' se solapa
               if(intervaloActual[0]>=intervalo[0] && intervaloActual[0]<=intervalo[1]){
                  // Actualizar 'intervalo' para que incluya 'intervaloActual'
                  intervalo[1]=tmp;
                  algunoActualizado=true;
                  break; // los intervalos de 'intervalosActivos' no deben solaparse
               }
            }
            if(!algunoActualizado){
               // Ningún intervalo fue actualizado, añadir intervalo actual
               //intervalosActivos.add(intervaloActual);
               intervaloActual[1] = tmp;
               int[] aux = new int[]{ intervaloActual[0], tmp };
               intervalosActivos.add(aux);
               intervaloActual = new int[2];
            }
         }

         // Avanzar el comienzo de la ventana, y eliminar aquellos eventos que
         // no están en un intervalo de eventos utilizados
         while(bv.getInstante()<=ev.getInstante()-((AbstractMine)mine).getWindowSize()-1){
            // Comprobar si 'bv' pertenece a algún intervalo activo
            boolean estaActivo=false;
            for(int[] intervalo : intervalosActivos){
               int instante = bv.getInstante();
               if(instante>=intervalo[0] && instante<=intervalo[1]){
                  estaActivo=true;
                  break;
               }
            }
            if(!estaActivo){
               // No pertenece a ningún intervalo activo, eliminar
               ((AbstractMine)mine).notificarEventoEliminado(bv, sid, 0);
               inicioVentana.remove();
            }
            bv = inicioVentana.next();
         }

         // Comprobar si algún intervalo activo salió de la ventana
         Iterator<int[]> iteradorIntervalosActivos = intervalosActivos.iterator();
         while(iteradorIntervalosActivos.hasNext()){
            int[] intervalo = iteradorIntervalosActivos.next();
            if(intervalo[1]<bv.getInstante()){
               iteradorIntervalosActivos.remove();
            }
         }
      }
      //}
      //imprimirEliminados(logger, borrados, restantes);
   }
}
