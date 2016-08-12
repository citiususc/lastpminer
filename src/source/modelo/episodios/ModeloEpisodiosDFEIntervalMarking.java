package source.modelo.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IMarcasIntervalos;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class ModeloEpisodiosDFEIntervalMarking extends ModeloEpisodiosDFE implements IMarcasIntervalos{
   //private static final Logger LOGGER = Logger.getLogger(ModeloEpisodiosDFEIntervalMarking.class.getName());
   private static final long serialVersionUID = 8091996045760602977L;

   /*
    * Atributos
    */

   protected int[] ultimaEncontrada = new int[2];

   /*
    * Constructores
    */

   public ModeloEpisodiosDFEIntervalMarking(String[] tipos, List<Episodio> episodios,
         int ventana, Integer frecuencia){
      super(tipos, episodios, ventana, frecuencia);
   }

   public ModeloEpisodiosDFEIntervalMarking(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos, episodios, ventana, patrones, frecuencia);
   }

   public ModeloEpisodiosDFEIntervalMarking(String[] tipos, int ventana, Integer frecuencia){
      this(tipos, new ArrayList<Episodio>(), ventana, frecuencia);
   }

   public ModeloEpisodiosDFEIntervalMarking(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia){
      this(tipos, new ArrayList<Episodio>(), ventana, patrones, frecuencia);
   }

   /*
    * Métodos
    */

   @Override
   public int[] getUltimaEncontrada(){
      return ultimaEncontrada;
   }

   /**
    * Copiado de de ModeloDictionaryFinalEvent modificado para que fije ultimaOcurrencia
    * para implementar correctamente IMarcasIntervalos
    */
   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados){
      String[] tipos = getTipos();

      int[] tam = getTam();

      int i=0,frecuenciaLocal=0;

      int tSize = tipos.length;
      // Comprobar si puede haber ocurrencias de algún patrón
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){ return; }
      }

      String tipo = evento.getTipo();
      // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
      List<PatronDictionaryFinalEvent> posibles = new ArrayList<PatronDictionaryFinalEvent>();
      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
         //if(tipos.containsAll(patron.getTipos()))
         //if(aux.getUltimoEventoLeido()!=evento && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=evento && aux.esTipoFinal(tipo)){
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){ return; }

      int tmp = evento.getInstante();

      boolean[] anotados = new boolean[posibles.size()];
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tMin;

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      instancia[index] = tmp;
      ultimaEncontrada[0] = tmp;
      ultimaEncontrada[1] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);

         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances)){
            frecuenciaLocal++;
            if(tMin<ultimaEncontrada[0]){
               ultimaEncontrada[0] = tMin;
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);
      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){
         patron.setUltimoEventoLeido(evento);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal, null);
   }

   /**
    * Copiado de ModeloEpisodiosDFE quitando la parte que comprueba si hai eventosDeEpisodios
    * y guardando la última instancia encontrada para implementar la interfaz IMarcasIntervalos
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      String[] tiposReordenados = episodios.getTiposReordenados();
      int index =  Arrays.asList(tiposReordenados).indexOf(tipo);

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
      int frecuenciaLocal=0;
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      int tMin;
      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int pSize = patrones.size();
      int[] patFrecLocal = new int[pSize];
      ultimaEncontrada[0] = tmp;
      ultimaEncontrada[1] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
            if(tMin<ultimaEncontrada[0]){
               ultimaEncontrada[0] = tMin;
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);
      }while(indices != null);

      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }



}
