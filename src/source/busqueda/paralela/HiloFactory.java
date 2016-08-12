package source.busqueda.paralela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import source.busqueda.paralela.episodios.HiloSoporteTam4Episodios;
import source.busqueda.paralela.episodios.HiloSoporteTam5Episodios;
import source.busqueda.paralela.jerarquia.HiloSoporteTam3;
import source.busqueda.paralela.jerarquia.HiloSoporteTam4;
import source.busqueda.paralela.semilla.HiloSoporteSemilla;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionDeHilo;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoHilos;
import source.modelo.arbol.Supernodo;
import source.modelo.paralelo.IAsociacionAgregable;
import source.patron.Patron;

public final class HiloFactory {

   private HiloFactory(){

   }

   public static HiloSoporte getInstance(String className, IColeccion coleccion, int hilo,
         List<List<List<Patron>>> actual, List<IAsociacionTemporal> candidatas, IBusquedaParalela mine, int tamActual) throws FactoryInstantiationException{
      Map<String, List<IAsociacionTemporal>> mapa = mine.getMapa();

      if("HiloSoporte".equals(className)){
         return new HiloSoporte(coleccion, hilo, mapa, mine.isSavePatternInstances());
      }
      if("HiloSoporteTam3".equals(className)){
         return new HiloSoporteTam3(coleccion, hilo, mapa, mine.isSavePatternInstances(), actual);
      }

      List<IAsociacionTemporal> aux = new ArrayList<IAsociacionTemporal>();
      Map<String[],IAsociacionTemporal> clonadas = new HashMap<String[],IAsociacionTemporal>();
      Map<String, List<IAsociacionTemporal>> copiaMapa = ParallelHelper.copiaMapa(mine.getMapa(), candidatas, aux, clonadas, hilo);

      if("HiloSoporteSemilla".equals(className)){
         return new HiloSoporteSemilla(coleccion, hilo, copiaMapa, aux, candidatas, (IBusquedaParalelaSecuencia)mine);
      }

      IBusquedaParalelaSecuenciaAnotaciones mineAnotaciones = (IBusquedaParalelaSecuenciaAnotaciones)mine;
      List<Supernodo> nivelActual = mineAnotaciones.getNivelActual();
      for(Supernodo sn : nivelActual){
         for(Nodo nodo : sn.getListaNodos()){
            NodoHilos nodoh = (NodoHilos)nodo;
            IAsociacionTemporal clonada = clonadas.get(nodoh.getModelo().getTipos());
            if(clonada == null){
               clonada =((IAsociacionAgregable)nodo.getModelo()).clonar();
               ((IAsociacionDeHilo)clonada).setHilo(hilo);
               clonadas.put(clonada.getTipos(), clonada);
            }
            nodoh.setModelosHilo(hilo, clonada);
         }
      }
      if("HiloSoporteTam4".equals(className)){
         return new HiloSoporteTam4(coleccion, hilo, copiaMapa, aux, candidatas, mineAnotaciones);
      }else if("HiloSoporteTam4Episodios".equals(className)){
         return new HiloSoporteTam4Episodios(coleccion, hilo, copiaMapa, aux, candidatas, mineAnotaciones);
      }else  if("HiloSoporteTam5".equals(className)){
         return new HiloSoporteTam5Episodios(coleccion, hilo, copiaMapa, aux, candidatas, mineAnotaciones, tamActual);
      }
      throw new FactoryInstantiationException("nombre de hilo desconocido: " + className);
   }
}
