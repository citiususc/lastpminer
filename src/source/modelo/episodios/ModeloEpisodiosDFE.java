package source.modelo.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.jerarquia.ModeloDictionaryFinalEvent;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatronEventoFinal;

public class ModeloEpisodiosDFE extends ModeloDictionaryFinalEvent implements IAsociacionConEpisodios/*, IAsociacionAgregable*/ {
   //private static final Logger LOGGER = Logger.getLogger(ModeloEpisodiosDFE.class.getName());
   private static final long serialVersionUID = -3693456306830976933L;

   /*
    * Atributos propios
    */

   protected EpisodiosWrapper episodios;
   protected int ultimoSid=-1;

   /*
    * Constructores heredados, añadiendo el uso de episodios
    */
   public ModeloEpisodiosDFE(String[] tipos, List<Episodio> episodios, int ventana, Integer frecuencia){
      super(tipos, ventana,frecuencia);
      //this.episodios=episodios;
      //organizarTipos(this);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
   }

   public ModeloEpisodiosDFE(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos, ventana, patrones,frecuencia);
      //this.episodios=episodios;
      //organizarTipos(this);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
   }

   /*
    * Métodos propios
    */

   /*
    *   Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
    * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
    *   Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
    * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
    * 'tiposReordenados') y actúa como en la versión tradicional. La segunda parte se centra en los tipos
    * que forman parte de episodio,
    *
    * CopyPaste de 'ModeloEpisodios' a 29 de enero de 2013 (revisión 19).
    */
   @Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      if(episodios.getEventosDeEpisodios()==0){
         return super.siguienteCombinacion(tam,indices,index,tipo);
      }
      return episodios.siguienteCombinacion(tam,indices,index,tipo);
   }


   /*
    * Redefinición del método para tratar con el conocimiento de episodios.
    *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
    *   -) Una vez calculados los índices, se construye la instancia usando
    *   -) Añadida la funcionalidad de jerarquía de patrones.
    */
   /**
    *   Comprueba qué patrones, de entre los que se encuentran en 'aComprobar', se pueden
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
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados){
      if(episodios.getEventosDeEpisodios()==0){
         super.recibeEvento(sid, ev, savePatternInstances, aComprobar, encontrados);
         return;
      }

      // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
      List<PatronDictionaryFinalEvent> posibles = new ArrayList<PatronDictionaryFinalEvent>();
      String tipo = ev.getTipo();

      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
         //if(tipos.containsAll(patron.getTipos()))
         //if(aux.getUltimoEventoLeido()!=ev && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=ev && aux.esTipoFinal(tipo)){
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){//Porque solo cuando posibles es vacio y cuando
         // Actualizar los patrones con notificación de cuál fue el último evento leído
         for(PatronDictionaryFinalEvent patron : aComprobar){
         //for(PatronDictionaryFinalEvent patron : posibles){
            patron.setUltimoEventoLeido(ev);
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
         if(tam[i]<=0){ return; }
      }

      // Actualizar frecuencias

      int tmp = ev.getInstante();

      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

      int[] indices = episodios.primerosIndices(index, tam, abiertas, limites, ventana, tmp);
      if(indices == null){
         return;
      }

      //int actualizadas;
      int frecuenciaLocal=0;
      boolean[] anotados = new boolean[posibles.size()];

      int[] instancia = new int[tSize]; // Construido en base a 'tipos'
      instancia[index]=tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances )){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);

      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){//patron : aComprobar
         patron.setUltimoEventoLeido(ev);
      }

      //addFrecuencias(frecuencia,patFrec);
      addFrecuencias(frecuenciaLocal,null);
   }


   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index,
         int tSize){
      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      boolean seguir = episodios.actualizarVentana(abiertas, limites, tam, ventana,
            index, tmp, sid, ultimoSid);
      ultimoSid = sid;
      return seguir;
   }
   /*
    * Copy-paste de ModeloEpisodios
    *   -) Solo debería usarse para la iteración tam=2.
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      if(episodios.getEventosDeEpisodios()==0){
         super.recibeEvento(sid, ev, savePatternInstances);
         return;
      }
      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = ev.getTipo();
      int index = Arrays.asList(tiposReordenados).indexOf(tipo);
      int tSize = tiposReordenados.length;
      int tmp = ev.getInstante();

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      int[] indices = episodios.primerosIndices(index, tam);
      if(indices == null){
         return;
      }

      // Actualizar frecuencias
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      int frecuenciaLocal=0;
      int[] patFrecLocal = new int[pSize];
      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);

      }while(indices != null);
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }


   /*
    * Redefinición del método que respeta las restricciones adicionales impuestas
    * por la definición de los tipos de episodio. Siempre y cuando afecten a la
    * asociación temporal actual.
    */
   @Override
   public void actualizaVentana(int sid, Evento evento){
      if(ultimoEventoLeido == evento){
         // Ya se había actualizado la ventana con el mismo evento
         return;
      }

      ultimoEventoLeido = evento;

      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = evento.getTipo();
      int index =  Arrays.asList(tiposReordenados).indexOf(tipo);
      if(index<0){ return; }

      //List<String> tipos = getTipos();
      int[][] limites = getLimites();
      int[][] abiertas = getAbiertas();
      int[] tam = getTam();
      int ventana = getVentana();
      int tmp = evento.getInstante();
      //int index = tipos.indexOf(tipo); // Los índices estan en base a tiposReordenados, salvo cuando se le pasan al patrón para comprobar ocurrencia.

      episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp, sid, ultimoSid);
      ultimoSid = sid;
   }


   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
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
         PatronEventoFinal patron = (PatronEventoFinal)patrones.get(i);
         if(patron.getFrecuencia()<supmin){
            patrones.remove(i);
            borrados++;
         }
      }
      setPatrones(patrones,null);
      return borrados;
   }

   @Override
   public boolean sonEpisodiosCompletos(){
      return episodios.isEpisodiosCompletos();
   }


   @Override
   public ModeloEpisodiosDFE clonar(){
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((PatronDictionaryFinalEvent)p).clonar());
      }
      return new ModeloEpisodiosDFE(getTipos(), episodios.getEpisodios(), getVentana(), patronesCopia, getSoporte());
   }

   @Override
   public String toString(){
      if(sonEpisodiosCompletos()){
         return super.toString();
      }

      if(getTipos().length == 2){
         return super.toString();
      }
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      StringBuilder aux = new StringBuilder(50);
      aux.append("Modelo: ").append(Arrays.toString(getTipos()))
         .append(" - Numero de patrones: ").append(pSize).append('\n');

      for(int i=0;i<pSize;i++){
         aux.append(" Fr: 0 - ")
            .append(patrones.get(i)).append('\n');
      }
      return aux.toString();
   }

}
