package source.modelo.concurrente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.modelo.ModeloAbstracto;
import source.patron.Patron;

public class ModeloConcurrente extends ModeloAbstracto implements IAsociacionTemporalConcurrente, Serializable{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrente.class.getName());
   private static final long serialVersionUID = 5771177521005562177L;

   /*
    * Atributos
    */

   protected List<ModeloParaleloHilo> modelos;

   /*
    * Constructores
    */

   public ModeloConcurrente(String[] tipos, int ventana,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos, ventana, frecuencia);
      if(createHilos==null || createHilos.length == 0 || (createHilos.length>0 && createHilos[0])){
         crearModelosHilos(numHilos);
      }
   }

   public ModeloConcurrente(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, frecuencia);
      if(createHilos==null || createHilos.length == 0 || (createHilos.length>0 && createHilos[0])){
         crearModelosHilos(numHilos);
      }
   }

   /*public ModeloConcurrente(List<String> tipos, int ventana, List<Patron> patrones, int[][] distribucion,
         boolean savePatternInstances, IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, distribucion, savePatternInstances, clustering);
      if(createHilos==null || createHilos.length == 0 || (createHilos.length>0 && createHilos[0])) crearModelosHilos(numHilos);
   }*/


   /*
    * Métodos
    */

   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloHilo(tSize));
      }
   }

   /*public void actualiza(int hilo, int sid, Evento ev){
      modelos.get(hilo).actualiza(sid, ev);
   }*/

   @Override
   public void recibeEvento(int hilo, int sid, Evento ev, boolean savePatternInstances){
      modelos.get(hilo).recibeEvento(sid, ev, savePatternInstances);
   }

   public void agregarResultadoHilos(){
      for(ModeloParaleloHilo mod:modelos){
         mod.agregarResultados();
      }
   }

   public int getSoporte(int hilo){
      return modelos.get(hilo).frecuenciaHilo;
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      throw new UnsupportedOperationException("Este método no debe llamarse para un ModeloParalelo");
   }

   @Override
   public String toStringSinPatrones(){
      return Arrays.toString(tipos);
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloHilo {
      /**
       * abiertas es una matriz de tamaño tSize x ventana
       */
      private int[][] abiertas; // Listas circulares, contienen instantes temporales
      /**
       * limites es Matrix de tSize x 2 con límite inferior y superior para cada tipo de evento de la asociacion temporal
       * estos límites son índices de las listas circulares, es decir, de abiertas
       */
      private int[][] limites; // Límites de las listas circulares, contienen índices a 'abiertas'
      /**
       *  tam es un array de tSize elementos cada uno contiene el tamaño de las listas circulares
       */
      private int[] tam; // Tamaño de las listas circulares, numero de elementos en ellas

      protected int frecuenciaHilo;
      protected int[] patFrecHilo;

      public ModeloParaleloHilo(int tSize){
         this.abiertas = new int[tSize][ventana]; // Hará falta más tamaño? @miguel
         this.tam = new int[tSize];
         this.limites = new int[tSize][2]; // inferior - superior
         this.patFrecHilo = new int[patrones.size()];
      }

      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         return siguienteCombinacion(tam, indices, index, tipo);
      }

      protected boolean actualizaVentana(int sid, String tipo, int tmp, int index, int tSize){
         boolean seguir = true;
         int i,j;
         // Actualizar índices fin e inicio para adaptarse a la ventana
         // Eliminar elementos que ya no están en ventana
         for(i=0;i<tSize;i++){
            j = limites[i][0];

            // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
            while((tam[i] > 0) && /*(j!=limites[i][1]) && */ ((tmp-ventana >= abiertas[i][j]) || (tmp < abiertas[i][j]))) {
               j = ((j+1) % ventana);
               tam[i]--;
            }
            limites[i][0] = j; // Modificar el indicador de inicio
            //if((i!=index) && (tam[i]==0)) { seguir=false; }
            //if((i!=index) && (tam[i]<=rep[i]) ) { seguir=false; }
            if((i!=index) && (tam[i]<=0) ) { seguir=false; }
         }
         // Añadir el nuevo elemento
         //for(i=index;i<=index+rep[index];i++){ //desde el indice del tipo hasta su última repeticion
         i=index;
            abiertas[i][limites[i][1]] = tmp;
            limites[i][1] = ((limites[i][1]+1)%ventana);
            tam[i]++;
            //if(tam[i]<=rep[i]){ seguir=false; }
            if(tam[i]<=0){ seguir=false; }
         //}
         return seguir;
      }

      protected void actualizaVentana(int sid, Evento evento){
         String tipo = evento.getTipo();
         int index = Arrays.binarySearch(tipos, tipo); // tipos.indexOf(tipo);

         int tmp = evento.getInstante();
         int tSize = tipos.length;
         actualizaVentana(sid, evento.getTipo(),tmp,index,tSize);
      }

      //Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String tipo = ev.getTipo(); //tipo del evento
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo); //indice del tipo de evento en la asociación temporal
         int tSize = tipos.length; //tamaño de la asociacion temporal
         int tmp = ev.getInstante(); //instante del evento
         if(!actualizaVentana(sid, tipo, tmp, index, tSize)){
            return;
         }

         // Actualizar frecuencias
         int[] indices = new int[tSize];
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            fijarInstancia(instancia, index, indices, tSize, tmp);

            if(comprobarPatrones(instancia, sid, savePatternInstances)){
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         //addFrecuencias(frecuenciaLocal, patFrecLocal); //se usan las variables del hilo
      }

      protected void fijarInstancia(int[] instancia, int index, int[] indices, int tSize, int tmp){
         instancia[index] = tmp;
         for(int i=0; i<tSize; i++){
            if(i!=index){
               instancia[i] = abiertas[i][(limites[i][0]+indices[i]) % ventana];
            }
         }
      }

      protected boolean comprobarPatrones(int[] instancia, int sid, boolean savePatternInstances) {
         int i=0;
         boolean encontrado = false;
         for(Patron patron : patrones){
            if(patron.representa(sid,instancia, savePatternInstances)){
               encontrado = true;
               patFrecHilo[i]++;
               break;
            }
            i++;
         }
         return encontrado;
      }

      protected int[][] getLimites(){
         return limites;
      }

      protected int[][] getAbiertas(){
         return abiertas;
      }

      protected int[] getTam(){
         return tam;
      }

      protected void agregarResultados(){
         //frecuencia y patFrec
         addFrecuencias(frecuenciaHilo, patFrecHilo);
         //distribucion
         //addDistribucion(distribucionHilo);
      }
   } //Fin de clase interna

}
