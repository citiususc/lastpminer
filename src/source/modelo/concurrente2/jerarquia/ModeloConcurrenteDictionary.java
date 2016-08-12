package source.modelo.concurrente2.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.concurrente.IAsociacionDiccionarioConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.modelo.concurrente.PatronConcurrenteEventoFinal;
import source.modelo.concurrente2.ModeloConcurrente;
import source.patron.GeneradorID;
import source.patron.Patron;

public class ModeloConcurrenteDictionary extends ModeloConcurrente implements IAsociacionDiccionarioConcurrente{
   private static final long serialVersionUID = -8625101583653771359L;

   /*
    * Constructores heredados
    */

   public ModeloConcurrenteDictionary(String[] tipos, int ventana,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos, ventana, frecuencia, numHilos, createHilos);
   }

   public ModeloConcurrenteDictionary(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, frecuencia, numHilos, createHilos);
      fijarModeloPatrones();
   }


   /*
    * Métodos propios
    */

   protected final void fijarModeloPatrones(){
      for(Patron p: patrones){
         ((PatronConcurrenteDFE)p).setAsociacion(this);
      }
   }

   /*
    * Se necesita sobreescribir este método porque la frecuencia de los patrones, en este
    * caso, se almacena en los propios patrones, y no en 'this', como en el resto de casos.
    */
   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID, boolean savePatternInstances){
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size(), borrados = 0;
      for(int i=pSize-1;i>=0;i--){
         PatronConcurrenteEventoFinal patron = (PatronConcurrenteEventoFinal)patrones.get(i);
         if(patron.getFrecuencia()<supmin){
            patrones.remove(i);
            borrados++;
         }
      }
      setPatrones(patrones,null);
      return borrados;
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloDictionaryHilo(i,tSize));
      }
   }

   @Override
   public void recibeEvento(int numHilo, int sid, Evento evento, boolean savePatternInstances,
         List<PatronConcurrenteDFE> aComprobar, List<Patron> encontrados){
      ((ModeloParaleloDictionaryHilo)modelos.get(numHilo)).recibeEvento(sid, evento, savePatternInstances, aComprobar, encontrados);
   }

   @Override
   public void actualizaVentana(int numHilo, int sid, Evento evento){
      ((ModeloParaleloDictionaryHilo)modelos.get(numHilo)).actualizaVentana(sid, evento);
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloDictionaryHilo extends ModeloParaleloHilo {
      protected int numHilo;
      protected Evento ultimoEventoLeido;

      public ModeloParaleloDictionaryHilo(int numHilo, int tSize) {
         super(tSize);
         this.numHilo = numHilo;
      }

      @Override
      public void actualizaVentana(int sid, Evento evento){
         if(ultimoEventoLeido == evento){ return; }
         super.actualizaVentana(sid, evento);
         ultimoEventoLeido=evento;
      }

      /**
       * Comprueba qué patrones, de entre los que se encuentran en {@code aComprobar}, se pueden
       * encontrar en la actual ventana temporal gracias a la lectura del evento {@code evento}.
       * Aquellos patrones de los cuales se encuentre una ocurrencia se añadirán a la lista
       * {@code encontrados}, que debe estar inicializada. Sólo se comprobarán aquellos patrones
       * de {@code aComprobar} que puedan terminar con el evento leído.
       * @param sid Identificador de la secuencia a la que pertenece el evento.
       * @param evento Evento leído de la secuencia.
       * @param aComprobar Patrones pertenecientes a <this> a comprobar con el evento.
       * @param encontrados Patrones, de entre 'aComprobar', encontrados (no puede ser null, únicamente se añaden patrones).
       */
      //@Override
      public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
            List<PatronConcurrenteDFE> aComprobar, List<Patron> encontrados){
         String[] tipos = getTipos();

         int[] tam = getTam();

         int i=0;
         int tSize = tipos.length;

         // Comprobar si puede haber ocurrencias de algún patrón
         for(i=0;i<tSize;i++){
            if(tam[i]==0){
               return;
            }
         }
         String tipo = evento.getTipo();
         // Descartar aquellos patrones que ya fueron comprobados con este evento
         List<PatronConcurrenteDFE> posibles = new ArrayList<PatronConcurrenteDFE>();
         for(Patron patron : aComprobar){
            PatronConcurrenteDFE aux = (PatronConcurrenteDFE)patron;
            // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
            //if(aux.getUltimoEventoLeido(numHilo)!=evento && aux.getTiposFinales().contains(tipo)){
            if(aux.getUltimoEventoLeido(numHilo)!=evento && aux.esTipoFinal(tipo)){ //añadida la segunda parte de la condición @vanesa
               posibles.add(aux);
            }
         }
         if(posibles.isEmpty()){ return; }

         boolean[] anotados = new boolean[posibles.size()];

         // Comprobar si hay alguna ocurrencia de algún posible patrón.
         int[] indices = new int[tSize];
         int[] instancia = new int[tSize];
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
         int tmp = evento.getInstante();
         instancia[index]=tmp;

         int frecuenciaLocal=0;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            fijarInstancia(tSize, index, indices, instancia);

            if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances)){
               frecuenciaLocal++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);

         // Actualizar los patrones con notificación de cuál fue el último evento leído
         for(PatronConcurrenteDFE patron : posibles){
            patron.setUltimoEventoLeido(numHilo, evento);
         }

         // Actualizar la frecuencia
         addFrecuencias(frecuenciaLocal,null);
      }


      protected boolean comprobarPatrones(List<PatronConcurrenteDFE> posibles, int sid,
            int[] instancia, List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances){
         int i=0;
         boolean encontrado = false;
         for(PatronConcurrenteDFE patron : posibles){
            if(patron.representa(sid,instancia, savePatternInstances)){
               encontrado = true;
               patron.encontrado();
               if(!anotados[i]){
                  encontrados.add(patron);
                  anotados[i]=true;
               }
               break;
            }
            i++;
         }
         return encontrado;
      }

   } // Fin de clase interna

   public String toString(){
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      StringBuilder aux = new StringBuilder(50);
      aux.append("Modelo: " + Arrays.toString(getTipos()) + " - Numero de patrones: " + pSize + "\n");

      for(int i=0;i<pSize;i++){
         aux.append(" Fr: ")
            .append(((PatronConcurrenteEventoFinal)patrones.get(i)).getFrecuencia()).append(" - ")
            .append(patrones.get(i)).append('\n');
      }

      return aux.toString();
   }



}
