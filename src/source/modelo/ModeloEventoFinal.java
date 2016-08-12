package source.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;



import source.evento.Evento;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronEventoFinal;

/**
 * Sólo cambia respecto a modelo en que una vez se ha comprobado que se puede formar alguna
 * ocurrencia de algún patrón, se comprueba si alguno de los patrones tiene el tipo de
 * evento de 'ev' como posible tipo final. Solo es necesario comprobar que hay
 * ocurrencias de estos patrones. Si no hubiese ningún patrón que cumpliese esta
 * condición, entonces no habrá ninguna ocurrencia de ningún patrón y se puede terminar
 * la ejecución de este método.
 * @author vanesa.graino
 *
 */
public class ModeloEventoFinal extends Modelo {
   //private static final Logger LOGGER = Logger.getLogger(ModeloEventoFinal.class.getName());
   private static final long serialVersionUID = 1458525452000051820L;

   /*
    * Atributos específicos.
    */

   /**
    *  Cada posición 'i' contiene una lista de patrones que pueden terminar
    *  con el evento de getTipos() que está en la misma posición 'i'.
    */
   private final List<List<Patron>> diccionarioPatrones;

   /*
    * Constructores heredados.
    */

   public ModeloEventoFinal(String[] tipos, int ventana, Integer frecuencia){
      super(tipos,ventana, frecuencia);
      this.diccionarioPatrones = new ArrayList<List<Patron>>();
   }

   public ModeloEventoFinal(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia){
      super(tipos,ventana,patrones,frecuencia);
      this.diccionarioPatrones = new ArrayList<List<Patron>>();
      //for(String tipo : tipos){
      for(int i=0; i<tipos.length; i++){
         diccionarioPatrones.add(new ArrayList<Patron>());
      }
      for(Patron patron : patrones){
         if(patron instanceof PatronEventoFinal){
            for(String tipo : ((PatronEventoFinal)patron).getTiposFinales()){
               int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo);
               diccionarioPatrones.get(index).add(patron);
            }
         }
      }
   }

   /*
    * Métodos específicos.
    */


   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp,
         int index, int tSize) {
      if(super.actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         List<Patron> posiblesPatrones = diccionarioPatrones.get(index);
         return !posiblesPatrones.isEmpty();
      }
      return false;
   }
   /*
    * CAMBIOS CON EL HEREDADO: Una vez se ha comprobado que se puede formar alguna
    * ocurrencia de algún patrón, se comprueba si alguno de los patrones tiene el tipo de
    * evento de 'ev' como posible tipo final. Solo es necesario comprobar que hay
    * ocurrencias de estos patrones. Si no hubiese ningún patrón que cumpliese esta
    * condición, entonces no habrá ninguna ocurrencia de ningún patrón y se puede terminar
    * la ejecución de este método.
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo);
      int tSize = tipos.length;
      int tmp = ev.getInstante();
      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      List<Patron> posiblesPatrones = diccionarioPatrones.get(index);
      if(posiblesPatrones.isEmpty()){
         return;
      }

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      int frecuenciaLocal=0;

      // Actualizar frecuencias
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      fijarInstancia(tSize, index, indices, instancia);

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         if(comprobarPatrones(posiblesPatrones, instancia, sid, index, savePatternInstances)){
            frecuenciaLocal++;
         }
      }while(siguienteInstancia(tam, indices, instancia, index, tipo, tSize));

      addFrecuencias(frecuenciaLocal, null);
   }

   /*
    * En esta clase debería ser deprecated ya que ya no se encarga el modelo de controlar
    * la frecuencia de los patrones, sino los propios patrones.
    * Aunque, en clases hijas cuando no hay anotaciones, sigue encargándose el modelo.
    * (non-Javadoc)
    * @see source.modelo.Modelo#comprobarPatrones(int[], int[], int, java.lang.String, int, boolean)
    */
   //@Deprecated
   protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal,
         int sid,  boolean savePatternInstances) {
      return super.comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances);
   }

   //@Override diferentes parametros (sin patFrecLocal principalmente)
   protected boolean comprobarPatrones(List<Patron> patrones, int[] instancia,
         int sid, int index, boolean savePatternInstances) {
      List<Patron> posiblesPatrones = diccionarioPatrones.get(index);
      boolean encontrado = false;
      for(Patron patron : posiblesPatrones){
         if(patron.representa(sid,instancia,savePatternInstances)) {
            encontrado = true;
            ((PatronEventoFinal)patron).encontrado();
            break;
         }
      }
      return encontrado;
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

   @Override
   public String toString(){
      if(getTipos().length==2){
         return super.toString();
      }
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      StringBuilder aux = new StringBuilder(50);
      aux.append("Modelo: ").append(Arrays.toString(getTipos())).append(" - Numero de patrones: ").append(pSize).append('\n');

      for(int i=0;i<pSize;i++){
         aux.append(" Fr: ")
            .append(((PatronEventoFinal)patrones.get(i)).getFrecuencia()).append( " - ")
            .append(patrones.get(i)).append('\n');
      }

      return aux.toString();
   }

   protected List<List<Patron>> getDiccionarioPatrones(){
      return diccionarioPatrones;
   }

   @Override
   public ModeloEventoFinal clonar() {
      List<Patron> patronesCopia = new ArrayList<Patron>();
      for(Patron p:getPatrones()){
         patronesCopia.add(((Patron)p).clonar());
      }
      return new ModeloEventoFinal(getTipos(), getVentana(), patronesCopia, getSoporte());
   }

}

