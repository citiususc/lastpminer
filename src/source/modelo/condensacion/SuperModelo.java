package source.modelo.condensacion;

import java.util.Arrays;

import source.evento.Evento;
import source.modelo.Modelo;

public class SuperModelo extends Modelo implements ISuperModelo{

   /**
    *
    */
   private static final long serialVersionUID = 5611444615182808675L;

   /**
    * Si hay tipo(s) de eventos que hay que omitir en la actualización de la ventana
    * porque han dejado de ser frecuentes
    * TODO: no se está utilizando ni actualizando.
    */
   private boolean[] omitidos;

   public SuperModelo(String[] tipos, int ventana) {
      super(tipos, ventana, null);
      omitidos = new boolean[tipos.length];
   }

   //Esta no tiene sentido
   public int[] fijarEstructuras(IModeloTontoEpisodios modelo){
      return null;
   }

   /**
    * Fija las estructuras para el modelo tonto
    * @param modelo
    * @return
    */
   @Override
   public int[] fijarEstructuras(IModeloTonto modelo){
      String[] tiposMod = modelo.getTipos();
      modelo.setTamColeccion(getTam());
      int[] indices = new int[tiposMod.length];
      int indice = 0;
      for(int i=0, tSize=tiposMod.length;i<tSize;i++){
         //esto es equivalente a indice = todosTipos.indexOf(tipos.get(i));
         indice = indice + Arrays.asList(tipos).subList(indice, tipos.length).indexOf(tiposMod[i]);
         indices[i] = indice;
         //modelo.getTam()[i] = supermodelo.getTam()[indice];//no funciona porque no son arrays sino int
         modelo.getLimites()[i] = getLimites()[indice];
         modelo.getAbiertas()[i] = getAbiertas()[indice];
      }
      return indices;
   }

   public int[] obtenerIndices(String[] tiposMod){
      int[] indices = new int[tiposMod.length];
      int indice = 0;
      for(int i=0, tSize=tiposMod.length;i<tSize;i++){
         //esto es equivalente a indice = todosTipos.indexOf(tipos.get(i));
         indice = indice + Arrays.asList(tipos).subList(indice, tipos.length).indexOf(tiposMod[i]);
         indices[i] = indice;
      }
      return indices;
   }

   public void omitir(String tipo){
      int i = Arrays.binarySearch(getTipos(), tipo);
      if(i>-1){
         omitidos[i] = true;
      }
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
    * Este método está en Modelo
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionary#actualizaVentana(int, source.evento.Evento)
    */
   @Override
   public void actualizaVentana(int sid, Evento evento){
      //Se hace público el método
      super.actualizaVentana(sid, evento);
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
