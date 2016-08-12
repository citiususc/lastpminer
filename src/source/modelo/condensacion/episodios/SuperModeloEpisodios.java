package source.modelo.condensacion.episodios;

import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.condensacion.IModeloTonto;
import source.modelo.condensacion.IModeloTontoEpisodios;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.SuperModelo;
import source.modelo.episodios.ModeloEpisodios;

/**
 * Versión de {@link SuperModelo} que maneja episodios.
 * @author vanesa.graino
 *
 */
public class SuperModeloEpisodios extends ModeloEpisodios implements ISuperModelo{

   /**
    *
    */
   private static final long serialVersionUID = 133795163789311587L;
   /**
    * Si hay tipo(s) de eventos que hay que omitir en la actualización de la ventana
    * porque han dejado de ser frecuentes
    * TODO: no se está utilizando ni actualizando.
    */
   private boolean[] omitidos;

   public SuperModeloEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana) {
      super(tipos, episodios, ventana, null);
      omitidos = new boolean[tipos.length];
   }

   public int[] fijarEstructuras(IModeloTontoEpisodios modelo){
      //Si no es completa no se va a buscar, por lo que ya no hay que hacer nada
      if(!modelo.sonEpisodiosCompletos()){ return null; }
      if(modelo.getEventosDeEpisodios() == 0){
         return fijarEstructuras((IModeloTonto) modelo);
      }
      //Si llega aquí es que tiposReordenados y tipos de modelo no coinciden
      String[] tiposReorMod = modelo.getTiposReordenados();
      List<String> todosTiposReor = Arrays.asList(getTiposReordenados());
      modelo.setTamColeccion(getTam());
      int[] indices = new int[tiposReorMod.length];
      int indice = 0;
      for(int i=0, tSize=tiposReorMod.length;i<tSize;i++){
         indice = todosTiposReor.indexOf(tiposReorMod[i]);
         indices[i] = indice;
         //modelo.getTam()[i] = supermodelo.getTam()[indice];//no funciona porque no son arrays sino int
         modelo.getLimites()[i] = getLimites()[indice];
         modelo.getAbiertas()[i] = getAbiertas()[indice];
      }
      return indices;
   }

   /**
    * Fija las estructuras para el modelo tonto. Se reordenan.
    * @param modelo
    * @return
    */
   @Override
   public int[] fijarEstructuras(IModeloTonto modelo){
      //Sin episodios eventos de episodio
      String[] tiposMod = modelo.getTipos();
      List<String> todosTiposReor = Arrays.asList(getTiposReordenados());
      modelo.setTamColeccion(getTam());
      int[] indices = new int[tiposMod.length];
      int indice = 0;
      for(int i=0, tSize=tiposMod.length;i<tSize;i++){
         //esto es equivalente a indice = todosTipos.indexOf(tipos.get(i));
         indice = todosTiposReor.indexOf(tiposMod[i]);
         indices[i] = indice;
         //modelo.getTam()[i] = supermodelo.getTam()[indice];//no funciona porque no son arrays sino int
         modelo.getLimites()[i] = getLimites()[indice];
         modelo.getAbiertas()[i] = getAbiertas()[indice];
      }
      return indices;
   }

   @Override
   public int[] obtenerIndices(String[] tiposMod) {
      //Sin episodios eventos de episodio
      List<String> todosTiposReor = Arrays.asList(getTiposReordenados());
      int[] indices = new int[tiposMod.length];
      int indice = 0;
      for(int i=0, tSize=tiposMod.length;i<tSize;i++){
         //esto es equivalente a indice = todosTipos.indexOf(tipos.get(i));
         indice = todosTiposReor.indexOf(tiposMod[i]);
         indices[i] = indice;
      }
      return indices;
   }

   public void omitir(String tipo){
      int i = Arrays.binarySearch(getTipos(), tipos);
      //if(i<0) return;
      omitidos[i] = true;
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getLimites()
    */
   @Override
   public int[][] getLimites() {
      return super.getLimites();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getAbiertas()
    */
   @Override
   public int[][] getAbiertas() {
      return super.getAbiertas();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getTam()
    */
   @Override
   public int[] getTam() {
      return super.getTam();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.episodios.ModeloEpisodios#actualizaVentana(int, source.evento.Evento)
    */
   @Override
   public void actualizaVentana(int sid, Evento evento){
      super.actualizaVentana(sid, evento);
   }

   /**
    * Todos los eventos activos
    * @param eventosActivos
    * @return
    */
   public List<String> eventosActivos(List<String> eventosActivos){
      //return listaTipos;
      int[] tam = getTam();
      String[] tiposReordenados = getTiposReordenados();
      eventosActivos.clear();
      for(int i=0; i< tam.length; i++){
         if(tam[i]>0){
            eventosActivos.add(tiposReordenados[i]);
         }
      }
      return eventosActivos;
   }

   /**
    * Todos los eventos activos que no pertenecen a episodios
    * @param eventosActivos
    * @return
    */
   public List<String> eventosActivosNoEpisodios(List<String> eventosActivos, Evento evento){
      //return listaTipos;
      int[] tam = getTam();
      String[] tiposReordenados = getTiposReordenados();
      eventosActivos.clear();
      for(int i=episodios.getEventosDeEpisodios(); i< tam.length; i++){
         if(tam[i]>0){
            eventosActivos.add(tiposReordenados[i]);
         }
      }
      //TODO comprobar
      eventosActivos.remove(evento.getTipo());
      return eventosActivos;
   }


   /**
    * Devuelve los episodios que están activos en la ventana actualmente.
    * @param episodiosActivos - lista inicializada en la que se guardarán los episodios activos.
    * @param llegaInicio - con este flag activo nos llega con que esté el inicio del episodio para añadirlo.
    * En otro caso, tienen que estar el inicio y el fin.
    * @return - lista de episodios activos
    */
   public List<Episodio> episodiosActivos(List<Episodio> episodiosActivos, boolean llegaInicio){
      int[] tam = getTam();
      episodiosActivos.clear();
      for(int i=0;i<episodios.getEventosDeEpisodios(); i+=2){
         if(tam[i]>0 && (llegaInicio || tam[i+1]>0)){
            episodiosActivos.add(episodios.getEpisodios().get(i/2));
         }
      }
      return episodiosActivos;
   }

   @Override
   public int enVentana() {
      int numTipos = 0;
      for(int valor : getTam()){
         if(valor>0) numTipos++;
      }
      return numTipos;
   }

}
