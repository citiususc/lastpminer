package source.modelo.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionDiccionario;
import source.modelo.Modelo;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatronEventoFinal;

/**
 * Diferencia con ModeloDictionaryFinalEvent:
 * ModeloDictionary sólo debe usarse con {@link PatronDictionaryFinalEvent} y subclases del mismo.
 * Las frecuencias de los patrones ya no se almacenan en el atributo {@code patFrec} sino
 * que se delegan en los patrones ( se puede obtener con el médoto
 * {@link PatronDictionaryFinalEvent#getFrecuencia()}).
 *
 * @author vanesa.graino
 *
 */
public class ModeloDictionary extends Modelo implements IAsociacionDiccionario {
   private static final long serialVersionUID = -9153166690141670305L;

   /*
    * Atributos propios
    */

   protected Evento ultimoEventoLeido;

   /*
    * Constructores heredados
    */

   public ModeloDictionary(String[] tipos, int ventana, Integer frecuencia){
      super(tipos, ventana, frecuencia);
   }

   public ModeloDictionary(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos, ventana, patrones, frecuencia);
      fijarModeloPatrones();
   }

   /*
    * Métodos propios
    */

   protected void fijarModeloPatrones(){
      for(Patron p: patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(this);
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
         PatronEventoFinal patron = (PatronEventoFinal)patrones.get(i);
         if(patron.getFrecuencia()<supmin){
            patrones.remove(i);
            borrados++;
         }
      }
      setPatrones(patrones,null);
      return borrados;
   }

   /**
    * Comprueba qué patrones, de entre los que se encuentran en 'aComprobar', se pueden
    * encontrar en la actual ventana temporal gracias a la lectura del evento <evento>.
    * Aquellos patrones de los cuales se encuentre una ocurrencia se añadirán a la lista
    * <encontrados>, que debe estar inicializada. Sólo se comprobarán aquellos patrones
    * de <aComprobar> que puedan terminar con el evento leído.
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
      // si falta algun tipo de evento (que no sea el del evento actual) se sale
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){
            return;
         }
      }

      String tipo = evento.getTipo();
      // Descartar aquellos patrones que ya fueron comprobados con este evento
      List<PatronDictionaryFinalEvent> posibles = new ArrayList<PatronDictionaryFinalEvent>();
      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
         //TODO contrastar si la segunda parte de la condición mejora o empeora los tiempos
         //if(aux.getUltimoEventoLeido()!=evento && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=evento && aux.esTipoFinal(tipo)){ //añadida la segunda parte de la condición @vanesa
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){ return; }

      int tmp = evento.getInstante();
      int index = Arrays.binarySearch(tipos, tipo);

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];

      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      fijarInstancia(tSize, index, indices, instancia);

      do{ // Recorre cada instancia
         // Comprobar si pertenece a algun patrón
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados,
               savePatternInstances)){
            frecuenciaLocal++;
         }
         //indices = siguienteCombinacion(tam,indices,index,tipo);
      }while(siguienteInstancia(tam, indices, instancia, index, tipo, tSize));

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){
         patron.setUltimoEventoLeido(evento);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);
   }

   /**
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


   /*
    * Se controla que no se había actualizado ya el modelo para el evento <evento>
    * (non-Javadoc)
    * @see source.modelo.Modelo#actualizaVentana(int, source.evento.Evento)
    */
   @Override
   public void actualizaVentana(int sid, Evento evento){
      if(ultimoEventoLeido == evento){ return; }
      super.actualizaVentana(sid, evento);
      ultimoEventoLeido = evento;
   }

   /*
    * Hay que sobreescribir este método porque ahora la frecuencia
    * de los patrones sólo se almacena en los patrones y no en patFrec
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#toString()
    */
   @Override
   public String toString(){
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      StringBuilder aux = new StringBuilder(50);
      aux.append("Modelo: " + Arrays.toString(getTipos()) + " - Numero de patrones: " + pSize + "\n");
      for(int i=0;i<pSize;i++){
         aux.append(" Fr: ")
            .append(((PatronEventoFinal)patrones.get(i)).getFrecuencia()).append(" - ")
            .append(patrones.get(i)).append('\n');
      }
      return aux.toString();
   }

   @Override
   public int getPatFrec(int index){
      return ((PatronEventoFinal)patrones.get(index)).getFrecuencia();
   }

   @Override
   public ModeloDictionary clonar() {
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((Patron)p).clonar());
      }
      return new ModeloDictionary(getTipos(), getVentana(), patronesCopia, getSoporte());
   }

}
