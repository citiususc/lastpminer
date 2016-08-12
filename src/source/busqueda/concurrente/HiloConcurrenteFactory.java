package source.busqueda.concurrente;

import java.util.List;
import java.util.Map;

import source.busqueda.concurrente.episodios.HiloConcurrenteTam4Episodios;
import source.busqueda.concurrente.episodios.HiloConcurrenteTam5Episodios;
import source.busqueda.concurrente.jerarquia.HiloConcurrenteTam3;
import source.busqueda.concurrente.jerarquia.HiloConcurrenteTam4;
import source.busqueda.concurrente.semilla.HiloConcurrenteSemilla;
import source.busqueda.concurrente.semilla.HiloConcurrenteTam5Arbol;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;

public final class HiloConcurrenteFactory {

   private HiloConcurrenteFactory(){

   }

   public static HiloConcurrente getHiloConcurrente(String className, int numHilo, IBusquedaConcurrenteSecuencia mine, IColeccion coleccion,
         Map<String, List<IAsociacionTemporal>> mapa, int tamActual, List<Episodio> episodios) throws FactoryInstantiationException{
      HiloConcurrente hilo = null;
      if("HiloConcurrente".equals(className)){
         hilo = new HiloConcurrente(numHilo, mine, coleccion, mapa);
      }else if("HiloConcurrenteTam3".equals(className)){
         hilo = new HiloConcurrenteTam3(numHilo, (IBusquedaConcurrenteSecuenciaAnotaciones)mine, coleccion, mapa);
      }else if("HiloConcurrenteTam4".equals(className)){
         hilo = new HiloConcurrenteTam4(numHilo, (IBusquedaConcurrenteSecuenciaAnotaciones)mine, coleccion, mapa);
      }else if("HiloConcurrenteTam4Episodios".equals(className)){
         hilo = new HiloConcurrenteTam4Episodios(numHilo, (IBusquedaConcurrenteSecuenciaAnotaciones)mine, coleccion, mapa);
      }else if("HiloConcurrenteTam5Episodios".equals(className)){
         hilo = new HiloConcurrenteTam5Episodios(numHilo, (IBusquedaConcurrenteSecuenciaAnotaciones)mine, coleccion, mapa, tamActual, episodios);
      }else if("HiloConcurrenteTam5Arbol".equals(className)){
         hilo = new HiloConcurrenteTam5Arbol(numHilo, (IBusquedaConcurrenteSecuenciaAnotaciones)mine, coleccion, mapa, tamActual, episodios);
      }else if("HiloConcurrenteSemilla".equals(className)){
         hilo = new HiloConcurrenteSemilla(numHilo, mine, coleccion, mapa);
      }else{
         throw new FactoryInstantiationException("HiloConcurrente.getHiloConcurrente cannot handle de className: " + className);
      }

      return hilo;
   }
}
