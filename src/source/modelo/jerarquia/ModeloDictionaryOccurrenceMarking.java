package source.modelo.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Este modelo tiene el mismo comportamiento que ModeloDictionary y además
 * así como mantiene abiertas mantiene la la ventana de eventos que tienen los tipos
 * interesantes para el modelo (de la misma forma que lo hace ModeloOccurrenceMarking).
 * Cuando una instancia del modelo es ocurrencia de patrón, los eventos se marcan como
 * utilizados con el valor que dicta el parámetro <esUsado>.
 *
 * @author vanesa.graino
 *
 */
public class ModeloDictionaryOccurrenceMarking extends ModeloDictionary{
   //private static final Logger LOGGER = Logger.getLogger(ModeloDictionaryOccurrenceMarking.class.getName());
   private static final long serialVersionUID = 4493498253948637826L;

   /*
    * Atributos propios
    */

   protected Evento[][] eventos;

   /*
    * Constructores
    */

   public ModeloDictionaryOccurrenceMarking(String[] tipos, int ventana, Integer frecuencia) {
      super(tipos, ventana, frecuencia);
      eventos = new Evento[tipos.length][ventana];
   }

   public ModeloDictionaryOccurrenceMarking(String[] tipos, int ventana,
         List<Patron> patrones, Integer frecuencia) {
      super(tipos, ventana, patrones, frecuencia);
      eventos = new Evento[tipos.length][ventana];
   }

   /*
    * Métodos
    */

   /**
    * Comprueba qué patrones, de entre los que se encuentran en 'aComprobar', se pueden
    * encontrar en la actual ventana temporal gracias a la lectura del evento <evento>.
    * Aquellos patrones de los cuales se encuentre una ocurrencia se añadirán a la lista
    * <encontrados>, que debe estar inicializada. Sólo se comprobarán aquellos patrones
    * de <aComprobar> que puedan terminar con el evento leído.
    * @param sid Identificador de la secuencia a la que pertenece el evento.
    * @param ev Evento leído de la secuencia.
    * @param aComprobar Patrones pertenecientes a 'this' a comprobar con el evento.
    * @param encontrados Patrones, de entre 'aComprobar', encontrados (no puede ser null, únicamente se añaden patrones).
    */
   //@Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances, List<PatronDictionaryFinalEvent> aComprobar,
         List<Patron> encontrados, boolean esUsado){
      String[] tipos = getTipos();
      int[] tam = getTam();
      int i=0, frecuenciaLocal=0;
      int tSize = tipos.length;

      // Comprobar si puede haber ocurrencias de algún patrón
      // si falta algun tipo de evento (que no sea el del evento actual) se sale
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){ return; }
      }

      String tipo = ev.getTipo();

      // Descartar aquellos patrones que ya fueron comprobados con este evento
      List<PatronDictionaryFinalEvent> posibles = new ArrayList<PatronDictionaryFinalEvent>();
      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
         //if(aux.getUltimoEventoLeido()!=ev && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=ev && aux.esTipoFinal(tipo)){ //añadida la segunda parte de la condición @vanesa
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){ return; }

      int ventana = getVentana();
      int tmp = ev.getInstante();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int[][] limites = getLimites();
      int[][] abiertas = getAbiertas();

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];
      List<Evento> eventosInstancia = new ArrayList<Evento>(tSize);

      instancia[index]=tmp;

      do{ // Recorre cada instancia
         // Comprobar si pertenece a algun patrón
         eventosInstancia.clear();
         for(i=0;i<tSize;i++){
            if(i==index){
               eventosInstancia.add(ev);
            }else{
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               eventosInstancia.add(eventos[i][(limites[i][0]+indices[i])%ventana]);
            }
         }

         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances)){
            frecuenciaLocal++;
            for(Evento eux : eventosInstancia){
               eux.setUsado(esUsado);
            }
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);
      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){
         patron.setUltimoEventoLeido(ev);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);
   }

   /*
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionary#comprobarPatrones(java.util.List, int, int[], java.util.List, boolean[], int, int, boolean)
    */
   @Override
   protected boolean comprobarPatrones(
         List<PatronDictionaryFinalEvent> posibles, int sid, int[] instancia,
         List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances) {

      boolean encontrado = false;
      int i=0;
      for(PatronDictionaryFinalEvent patron : posibles){
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

   /*
    * Se actualiza tambien eventos[][]
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionary#actualizaVentana(int, source.evento.Evento)
    */
   //TODO ultimoSid
   @Override
   public void actualizaVentana(int sid, Evento evento){
      if(ultimoEventoLeido == evento){ return; }

      String[] tipos = getTipos();
      String tipo = evento.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

      int[][] limites = getLimites();
      int[][] abiertas = getAbiertas();
      int[] tam = getTam();
      int ventana = getVentana();

      int tmp = evento.getInstante();
      int tSize = tipos.length;

      //  Actualizar la ventana temporal y comprobar si hay suficientes eventos
      // en la ventana para completar una ocurrencia de algún patrón de la asociación.

      // Actualizar índices fin e inicio para adaptarse a la ventana
      // Eliminar elementos que ya no están en ventana
      for(int j,i=0;i<tSize;i++){
         j=limites[i][0];

         // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
         while(tam[i]>0 && ((tmp-ventana>=abiertas[i][j])||(tmp<abiertas[i][j]))){
            j=((j+1)%ventana); tam[i]--;
         }
         limites[i][0]=j; // Modificar el indicador de inicio
      }
      // Añadir el nuevo elemento
      abiertas[index][limites[index][1]] = tmp;
      eventos[index][limites[index][1]] = evento;
      limites[index][1] = ((limites[index][1]+1)%ventana);
      tam[index]++;

      ultimoEventoLeido = evento;
   }


}
