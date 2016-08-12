package source.modelo.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionDiccionario;
import source.modelo.ModeloEventoFinal;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * En patrones de tamaño 3 se siguen utilizando los métodos de ModeloEventoFinal
 * que utilizan el diccionario de patrones en dónde se identifica por caada
 * tipo de evento del modelo, qué patrones lo tiene como posible evento final.
 * En tamaños superiores, esta implementación de Modelo es idéntica a ModeloDictionary.
 * El diccionario de patrones ya no se utiliza. Pero, ¿debería?
 *
 * @author vanesa.graino
 *
 */
public class ModeloDictionaryFinalEvent extends ModeloEventoFinal implements IAsociacionDiccionario{
   private static final long serialVersionUID = -7767990071469854187L;

   /*
    * Atributos específicos.
    */
   protected transient Evento ultimoEventoLeido;

   /*
    * Constructores heredados.
    */

   public ModeloDictionaryFinalEvent(String[] tipos, int ventana, Integer frecuencia){
      super(tipos,ventana,frecuencia);
   }

   public ModeloDictionaryFinalEvent(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia){
      super(tipos,ventana,patrones, frecuencia);
      fijarModeloPatrones();
   }

   /*
    * Métodos específicos. (recibeEvento y actualizaVentana son iguales a los de ModeloDictionary)
    */

   //Igual que el de ModeloDictionary
   protected void fijarModeloPatrones(){
      for(Patron p: patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(this);
      }
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
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados){
      String[] tipos = getTipos();
      int[] tam = getTam();
      //int[] rep = getRep();
      int i=0, frecuenciaLocal=0;
      int tSize = tipos.length;

      // Comprobar si puede haber ocurrencias de algún patrón
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){ return; }
      }

      String tipo = evento.getTipo();
      // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
      List<PatronDictionaryFinalEvent>  posibles = new ArrayList<PatronDictionaryFinalEvent>(aComprobar.size());
      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         //if(aux.getUltimoEventoLeido()!=evento && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=evento && aux.esTipoFinal(tipo)){
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){ return; }

      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tmp = evento.getInstante();

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         fijarInstancia(tSize, index, indices, instancia);
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      // Sólo los de posibles y no los de aComprobar porque se han podido quedado fuera
      // por no contener al evento como posible evento final.
      for(PatronDictionaryFinalEvent patron : posibles){ // patron : aComprobar){
         patron.setUltimoEventoLeido(evento);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);
   }

   /**
    * Igual que en ModeloDictionary
    *
    * Se comprueba si instancia es una ocurrencia de los posibles patrones.
    * Si lo es, se añade el patrón a encontrados (si no se había añadido antes,
    * para controlarlo se registra en el array de flags anotados).
    * @param posibles - patrones que se va a comprobar si representan la instancia
    * @param sid - identificador de la secuencia
    * @param instancia - instantes de cada tipo de evento de la asociación
    * @param encontrados - lista de encontrados
    * @param anotados - array en el que se anotan los patrones que ya se han añadido
    * a encontrados de esta asociación
    * @param tMin - inicio de la instancia
    * @param tmp - instante del evento actual
    * @return si se ha encontrado una instancia de algún patrón o no
    */
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid,
         int[] instancia, List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances){
      boolean encontrado = false;
      int i=0;
      for(PatronDictionaryFinalEvent patron : posibles){
         if(patron.representa(sid,instancia, savePatternInstances)){
            encontrado = true;
            patron.encontrado();
            if(!anotados[i]){
               //encontrados.add(patron);
               patronEncontrado(encontrados, patron);
               anotados[i]=true;
            }
            break;
         }
         i++;
      }
      return encontrado;
   }

 //Se añade este método para simplificar la paralelización
   protected void patronEncontrado(List<Patron> encontrados, Patron patron){
      encontrados.add(patron);
   }

   @Override
   public void actualizaVentana(int sid, Evento evento){
      if(ultimoEventoLeido == evento){ return; }
      super.actualizaVentana(sid, evento);
      ultimoEventoLeido=evento;
   }

}
