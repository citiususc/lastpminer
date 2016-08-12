package source.modelo.concurrente;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Evento;
import source.patron.GeneradorID;
import source.patron.Patron;

public class ModeloConcurrenteEventoFinal extends ModeloConcurrente {
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteEventoFinal.class.getName());
   private static final long serialVersionUID = 6662312689271679432L;

   /*
    * Atributos específicos.
    */

   // Cada posición 'i' contiene una lista de patrones que pueden terminar
   // con el evento de getTipos() que está en la misma posición 'i'.
   private final List<List<Patron>> diccionarioPatrones;

   /*
    * Constructores
    */

   public ModeloConcurrenteEventoFinal(String[] tipos, int ventana,
         Integer frecuencia, int hilos){
      super(tipos,ventana, frecuencia, hilos);
      this.diccionarioPatrones = new ArrayList<List<Patron>>();
   }

   public ModeloConcurrenteEventoFinal(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int hilos){
      super(tipos,ventana,patrones, frecuencia, hilos);
      this.diccionarioPatrones = new ArrayList<List<Patron>>();
      //for(String tipo : tipos){
      for(int i=0; i<tipos.length; i++){
         diccionarioPatrones.add(new ArrayList<Patron>());
      }
      for(Patron patron : patrones){
         if(patron instanceof PatronConcurrenteEventoFinal){
            for(String tipo : ((PatronConcurrenteEventoFinal)patron).getTiposFinales()){
               int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
               diccionarioPatrones.get(index).add(patron);
            }
         }
      }
   }

   /*
    * Métodos
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

   protected List<List<Patron>> getDiccionarioPatrones(){
      return diccionarioPatrones;
   }

   @Override
   public String toString(){
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      StringBuilder aux = new StringBuilder(50);
      aux.append("Modelo: ").append(Arrays.toString(getTipos())).append(" - Numero de patrones: ").append(pSize).append('\n');

      for(int i=0;i<pSize;i++){
         aux.append(" Fr: ")
         .append(((PatronConcurrenteEventoFinal)patrones.get(i)).getFrecuencia()).append(" - ")
         .append(patrones.get(i)).append('\n');
      }

      return aux.toString();
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloEventoFinalHilo(tSize));
      }
   }

   /*
    * Clases privadas
    */
   protected class ModeloParaleloEventoFinalHilo extends ModeloParaleloHilo {

      public ModeloParaleloEventoFinalHilo(int tSize) {
         super(tSize);
      }

      /*
       *  CAMBIOS CON EL HEREDADO: Una vez se ha comprobado que se puede formar alguna
       * ocurrencia de algún patrón, se comprueba si alguno de los patrones tiene el tipo de
       * evento de 'ev' como posible tipo final. Solo es necesario comprobar que hay
       * ocurrencias de estos patrones. Si no hubiese ningún patrón que cumpliese esta
       * condición, entonces no habrá ninguna ocurrencia de ningún patrón y se puede terminar
       * la ejecución de este método.
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String[] tipos = getTipos();
         String tipo = ev.getTipo();
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
         int tSize = tipos.length;
         int tmp = ev.getInstante();

         //Actualizar la ventana y comprobar si se puede formar ocurrencia de patron
         if(!actualizaVentana(sid, tipo, tmp, index, tSize)){
            return;
         }

         // Actualizar frecuencias
         int[] indices = new int[tSize];
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         List<Patron> posiblesPatrones = diccionarioPatrones.get(index);
         if(posiblesPatrones.isEmpty()){
            return;
         }

         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            fijarInstancia(instancia, index, indices, tSize, tmp);

            if(comprobarPatrones(posiblesPatrones, instancia, sid, savePatternInstances)){
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);
         }while(indices != null);
         //addFrecuencias(frecuencia,null);//se usan las variables del hilo
      }

      @Deprecated
      @Override
      protected boolean comprobarPatrones(int[] instancia, int sid,
            boolean savePatternInstances) {
         return super.comprobarPatrones(instancia, sid, savePatternInstances);
      }

      protected boolean comprobarPatrones(List<Patron> posiblesPatrones, int[] instancia, int sid,
            boolean savePatternInstances) {
         boolean encontrado = false;
         for(Patron patron : posiblesPatrones){
            if(patron.representa(sid,instancia,savePatternInstances)) {
               encontrado = true;
               ((PatronConcurrenteEventoFinal)patron).encontrado();
               break;
            }
         }
         return encontrado;
      }
   }


}
