package source.modelo.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.modelo.IMarcasIntervalos;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Sólo cambia respecto a ModeloDictionary en que implementa IMarcasIntervalos
 * por lo que tiene que guardar la ultima ocurrencia de sus patrones cuando
 * se llama a recibeEvento y encuentra alguna.
 * @author vanesa.graino
 *
 */
public class ModeloDictionaryIntervalMarking extends ModeloDictionary implements IMarcasIntervalos{
   //private static final Logger LOGGER = Logger.getLogger(ModeloDictionaryIntervalMarking.class.getName());
   private static final long serialVersionUID = 4647763641730659275L;

   /*
    * Atributos
    */

   protected int[] ultimaOcurrencia = new int[2];

   /*
    * Constructores
    */

   public ModeloDictionaryIntervalMarking(String[] tipos, int ventana,
         Integer frecuencia){
      super(tipos, ventana, frecuencia);
   }

   public ModeloDictionaryIntervalMarking(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos, ventana, patrones, frecuencia);
   }

   /*public ModeloDictionaryIntervalMarking(List<String> tipos, int ventana, List<Patron> patrones, int[][] distribucion, Boolean savePatternInstances,
         IClustering clustering){
      super(tipos, ventana, patrones, distribucion, savePatternInstances, clustering);
   }*/

   /*
    * Otros métodos
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      //super.recibeEvento(sid,ev,savePatternInstances);
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tSize = tipos.length;
      int tmp = ev.getInstante(); //instante del evento

      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      int[] tam = getTam();
      int[] patFrecLocal = new int[patrones.size()];
      int[] indices = new int[tSize];
      int tMin, frecuenciaLocal=0;

      // Actualizar frecuencias
      int[] instancia = new int[tSize];
      instancia[index] = tmp;
      ultimaOcurrencia[0] = tmp;
      ultimaOcurrencia[1] = tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);

         if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
            frecuenciaLocal++;
            if(tMin < ultimaOcurrencia[0]){
               ultimaOcurrencia[0] = tMin;
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,patFrecLocal);
   }

   @Override
   protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal,
         int sid, boolean savePatternInstances) {
      boolean encontrado = false;
      int i=0;
      for(Patron patron : patrones){
         if(patron.representa(sid,instancia,savePatternInstances)){
            encontrado = true;
            patFrecLocal[i]++;
            break;
         }
         i++;
      }
      return encontrado;
   }

   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados){
      String[] tipos = getTipos();

      int[] tam = getTam();

      int i=0,tSize = tipos.length;
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

      int tMin, frecuenciaLocal = 0;
      int tmp = evento.getInstante();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];
      int[] instancia = new int[tSize];
      instancia[index] = tmp;
      ultimaOcurrencia[0] = tmp;
      ultimaOcurrencia[1] = tmp;


      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados,
               savePatternInstances)){
            frecuenciaLocal++;
            if(tMin<ultimaOcurrencia[0]){
               ultimaOcurrencia[0] = tMin;
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){
         patron.setUltimoEventoLeido(evento);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);
   }

   @Override
   public int[] getUltimaEncontrada(){
      return ultimaOcurrencia;
   }

}
