package source.modelo.concurrente2.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.patron.Patron;

public class ModeloConcurrenteDFE extends ModeloConcurrenteDictionary {
   private static final long serialVersionUID = 3662401436699454758L;

   /*
    * Atributos específicos.
    */



   /*
    * Constructores heredados.
    */

   public ModeloConcurrenteDFE(String[] tipos, int ventana,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos,ventana,frecuencia, numHilos, createHilos);
   }

   public ModeloConcurrenteDFE(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos,ventana,patrones, frecuencia, numHilos, createHilos);
   }

   /*
    * Métodos
    */

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloDFEHilo(i,tSize));
      }
   }


   /*
    * Clases privadas
    */

   protected class ModeloParaleloDFEHilo extends ModeloParaleloDictionaryHilo {


      public ModeloParaleloDFEHilo(int numHilo, int tSize) {
         super(numHilo, tSize);
      }

      @Override
      public void actualizaVentana(int sid, Evento evento){
         if(ultimoEventoLeido == evento){ return; }
         super.actualizaVentana(sid, evento);
         ultimoEventoLeido = evento;
      }

      /**
       *   Comprueba qué patrones, de entre los que se encuentran en 'aComprobar', se pueden
       * encontrar en la actual ventana temporal gracias a la lectura del evento 'evento'.
       * Aquellos patrones de los cuales se encuentre una ocurrencia se añadirán a la lista
       * 'encontrados', que debe estar inicializada. Sólo se comprobarán aquellos patrones
       * de 'aComprobar' que puedan terminar con el evento leído.
       * @param sid Identificador de la secuencia a la que pertenece el evento.
       * @param evento Evento leído de la secuencia.
       * @param aComprobar Patrones pertenecientes a 'this' a comprobar con el evento.
       * @param encontrados Patrones, de entre 'aComprobar', encontrados (no puede ser null, únicamente se añaden patrones).
       */
      @Override
      public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
            List<PatronConcurrenteDFE> aComprobar, List<Patron> encontrados){
         String[] tipos = getTipos();

         int[] tam = getTam();
         int i, tSize = tipos.length;

         // Comprobar si puede haber ocurrencias de algún patrón
         for(i=0;i<tSize;i++){
            //if(tam[i]<=rep[i]){ return; }
            if(tam[i]<=0){
               return;
            }
         }

         String tipo = evento.getTipo();
         // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
         List<PatronConcurrenteDFE> posibles = new ArrayList<PatronConcurrenteDFE>();
         for(Patron patron : aComprobar){
            PatronConcurrenteDFE aux = (PatronConcurrenteDFE)patron;
            // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
            //if(tipos.containsAll(patron.getTipos()))
            //if(aux.getUltimoEventoLeido(numHilo)!=evento && aux.getTiposFinales().contains(tipo)){
            if(aux.getUltimoEventoLeido(numHilo)!=evento && aux.esTipoFinal(tipo)){
               posibles.add(aux);
            }
         }
         if(posibles.isEmpty()){
            return;
         }

         // Comprobar si hay alguna ocurrencia de algún posible patrón.
         int frecuenciaLocal = 0;
         int[] indices = new int[tSize];
         boolean[] anotados = new boolean[posibles.size()];
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
         int tmp = evento.getInstante();
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         do{
            // Comprobar si pertenece a algun patrón
            fijarInstancia(tSize, index, indices, instancia);
            fijarModeloPatrones();

            if(comprobarPatrones(posibles, sid, instancia, encontrados,
                  anotados, savePatternInstances)){
               frecuenciaLocal++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);

         // Actualizar los patrones con notificación de cuál fue el último evento leído
         //for(PatronDictionaryFinalEvent patron : aComprobar){
         for(PatronConcurrenteDFE patron : posibles){
            patron.setUltimoEventoLeido(numHilo, evento);
         }

         // Actualizar la frecuencia
         addFrecuencias(frecuenciaLocal,null);
      }

   } // Fin de clase interna

}
