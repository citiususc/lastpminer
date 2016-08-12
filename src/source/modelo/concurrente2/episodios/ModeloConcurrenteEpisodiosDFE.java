package source.modelo.concurrente2.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.modelo.concurrente.PatronConcurrenteEventoFinal;
import source.modelo.concurrente2.jerarquia.ModeloConcurrenteDFE;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.GeneradorID;
import source.patron.Patron;

/**
 * Los modelos de hilo se instancia el propio o el sin episodios dependiendo si la asociación
 * es completa (todos los fin de episodios tienen su inicio y viceversa) o parcial.
 * @author vanesa.graino
 *
 */
public class ModeloConcurrenteEpisodiosDFE extends ModeloConcurrenteDFE implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteEpisodiosDFE.class.getName());
   private static final long serialVersionUID = 7494061526490455455L;
   /*
    * Atributos propios
    */

   protected EpisodiosWrapper episodios;

   /*
    * Constructores heredados, añadiendo el uso de episodios
    */
   public ModeloConcurrenteEpisodiosDFE(String[] tipos, List<Episodio> episodios, int ventana, Integer frecuencia,
         int numHilos){
      super(tipos, ventana, frecuencia, numHilos,false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteEpisodiosDFE(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos){
      super(tipos, ventana, patrones, frecuencia, numHilos,false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   /*
    * Métodos propios
    */

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      if(episodios.getEventosDeEpisodios()==0){
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloDFEHilo(i,tSize));
         }
      }else{
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloEpisodiosDFEHilo(i,tSize));
         }
      }
   }

   @Override
   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos(){
      return episodios.isEpisodiosCompletos();
   }

   /*
    *   Se necesita sobreescribir este método porque la frecuencia de los patrones, en este
    * caso, se almacena en los propios patrones, y no en 'this', como en el resto de casos.
    */
   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID, boolean savePatternInstances){
      if(getTipos().length == 2){
         return super.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
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


   /*
    * Clases privadas
    */

   protected class ModeloParaleloEpisodiosDFEHilo extends ModeloParaleloDFEHilo {

      protected Evento ultimoEventoLeido;
      protected int ultimoSid=-1;

      public ModeloParaleloEpisodiosDFEHilo(int numHilo, int tSize) {
         super(numHilo, tSize);
      }

      /*
       * Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
       * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
       * Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
       * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
       * <tiposReordenados>) y actúa como en la versión tradicional. La segunda parte se centra en los tipos
       * que forman parte de episodio,
       *
       * CopyPaste de 'ModeloEpisodios' a 29 de enero de 2013 (revisión 19).
       */
      @Override
      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         return episodios.siguienteCombinacion(tam, indices, index, tipo);
      }

      /*protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites, int[] indices, int[] instancia){
         return episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, getVentana());
      }*/

      /*
       * Redefinición del método para tratar con el conocimiento de episodios.
       *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
       *   -) Una vez calculados los índices, se construye la instancia usando
       *   -) Añadida la funcionalidad de jerarquía de patrones.
       */

      /**
       * Comprueba qué patrones, de entre los que se encuentran en 'aComprobar', se pueden
       * encontrar en la actual ventana temporal gracias a la lectura del evento 'evento'.
       * Aquellos patrones de los cuales se encuentre una ocurrencia se añadirán a la lista
       * 'encontrados', que debe estar inicializada. Sólo se comprobarán aquellos patrones
       * de 'aComprobar' que puedan terminar con el evento leído. Si el modelo representa una
       * asociación temporal en la que hay tipos de episodios presentes, únicamente se
       * construirán instancias que satisfagan las restricciones adicionales que representan
       * los episodios.
       * @param sid Identificador de la secuencia a la que pertenece el evento.
       * @param evento Evento leído de la secuencia.
       * @param aComprobar Patrones pertenecientes a 'this' a comprobar con el evento.
       * @param encontrados Patrones, de entre 'aComprobar', encontrados (no puede ser null, únicamente se añaden patrones).
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances,
            List<PatronConcurrenteDFE> aComprobar, List<Patron> encontrados){
         //Nunca se da eventosDeEpisodios==0 para este ModeloHilo
         //if(eventosDeEpisodios==0){ super.recibeEvento(sid, ev, savePatternInstances, aComprobar, encontrados); return; }

         // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
         List<PatronConcurrenteDFE> posibles = new ArrayList<PatronConcurrenteDFE>();
         String tipo = ev.getTipo();

         for(Patron patron : aComprobar){
            PatronConcurrenteDFE aux = (PatronConcurrenteDFE)patron;
            // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
            //if(tipos.containsAll(patron.getTipos()))
            //if(aux.getUltimoEventoLeido(numHilo)!=ev && aux.getTiposFinales().contains(tipo)){
            if(aux.getUltimoEventoLeido(numHilo)!=ev && aux.esTipoFinal(tipo)){
               posibles.add(aux);
            }
         }
         if(posibles.isEmpty()){
            // Actualizar los patrones con notificación de cuál fue el último evento leído
            for(PatronConcurrenteDFE patron : aComprobar){
               patron.setUltimoEventoLeido(numHilo, ev);
            }
            return;
         }

         String[] tiposReordenados = episodios.getTiposReordenados();
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);

         int tSize = tiposReordenados.length;

         int[] tam=getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

         int i;
         // Comprobar si puede haber ocurrencias de algún patrón
         for(i=0;i<tSize;i++){
            if(tam[i]==0){
               return;
            }
         }
         int[][] abiertas=getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites=getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
         int tmp = ev.getInstante();
         int[] indices = episodios.primerosIndices(index, tam, abiertas, limites, ventana, tmp);
         if(indices == null){
            return;
         }

         // Actualizar frecuencias
         int[] instancia = new int[tSize]; // Construido en base a 'tipos'
         instancia[index] = tmp;

         boolean[] anotados = new boolean[posibles.size()];
         int frecuenciaLocal = 0;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
            if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances)){
               frecuenciaLocal ++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);
         }while(indices != null);

         // Actualizar los patrones con notificación de cuál fue el último evento leído
         //for(PatronDictionaryFinalEvent patron : aComprobar){
         for(PatronConcurrenteDFE patron : posibles){
            patron.setUltimoEventoLeido(numHilo,ev);
         }

         //addFrecuencias(frecuencia,patFrec);
         addFrecuencias(frecuenciaLocal ,null);
      }

      /*
       * Copy-paste de ModeloEpisodios
       *   -) Solo debería usarse para la iteración tam=2.
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         /*if(eventosDeEpisodios==0){//Nunca se da
            super.recibeEvento(sid, ev, savePatternInstances);
            return;
         }*/
         String[] tiposReordenados = episodios.getTiposReordenados();
         String tipo = ev.getTipo();
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);
         int tSize = tiposReordenados.length;
         int tmp = ev.getInstante();

         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         // Actualizar frecuencias
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
         int[] indices = episodios.primerosIndices(index, tam);
         if(indices == null){
            return;
         }

         int frecuenciaLocal=0;
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         int[] patFrecLocal = new int[patrones.size()];
         int[][] abiertas=getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites=getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);
         }while(indices != null);
         addFrecuencias(frecuenciaLocal,patFrecLocal);
      }

      public boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){

         int[][] abiertas = getAbiertas(), limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

         boolean seguir = episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp, sid, ultimoSid);
         ultimoSid = sid;
         return seguir;
      }

      /*
       * Redefinición del método que respeta las restricciones adicionales impuestas
       * por la definición de los tipos de episodio. Siempre y cuando afecten a la
       * asociación temporal actual.
       */
      @Override
      public void actualizaVentana(int sid, Evento evento){
         if(ultimoEventoLeido==evento){ return; }
         String[] tiposReordenados = episodios.getTiposReordenados();
         String tipo = evento.getTipo();
         //List<String> tipos = getTipos();
         //int index = tipos.indexOf(tipo); // Los índices estan en base a tiposReordenados, salvo cuando se le pasan al patrón para comprobar ocurrencia.
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);

         int tmp = evento.getInstante();
         int tSize = tiposReordenados.length;

         actualizaVentana(sid, evento, tipo, tmp, index, tSize);
         ultimoEventoLeido=evento;

      }

   } // Final clase interna

}
