package source.modelo.concurrente2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;


import source.evento.Evento;
import source.modelo.ModeloAbstracto;
import source.modelo.concurrente.IAsociacionTemporalConcurrente;
import source.patron.Patron;

/**
 *
 * @author vanesa
 *
 */
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
      if(createHilos==null || createHilos.length==0 || (createHilos.length>0 && createHilos[0])){
         crearModelosHilos(numHilos);
      }
   }

   public ModeloConcurrente(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, frecuencia);
      if(createHilos==null || createHilos.length==0 || (createHilos.length>0 && createHilos[0])){
         crearModelosHilos(numHilos);
      }
   }

   /*public ModeloConcurrente(List<String> tipos, int ventana, List<Patron> patrones, int[][] distribucion,
         boolean savePatternInstances, IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, distribucion, savePatternInstances);
      if(createHilos==null || (createHilos.length>0 && createHilos[0])) crearModelosHilos(numHilos);
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

   @Override
   public void recibeEvento(int hilo, int sid, Evento ev, boolean savePatternInstances){
      modelos.get(hilo).recibeEvento(sid, ev, savePatternInstances);
   }

   /*
    * Se sincroniza
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#incrementarSoporte()
    */
   @Override
   public synchronized int incrementarSoporte(){
      //return ++frecuencia;
      return super.incrementarSoporte();
   }

   /*
    * Se sincroniza
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#incrementarPatFrec(int)
    */
   @Override
   public void incrementarPatFrec(int indice){
      synchronized(getPatFrec()){
         super.incrementarPatFrec(indice);
      }
   }

   /*
    * Se sincroniza
    * (non-Javadoc)
    * @see source.modelo.ModeloAbstracto#addFrecuencias(int, int[])
    */
   @Override
   protected synchronized void addFrecuencias(int frec, int[] patOcs){
      super.addFrecuencias(frec, patOcs);
   }

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      throw new UnsupportedOperationException("Este método no debe llamarse para un ModeloParalelo");
   }

   //frecuenciaHilo hace falta cuando hay IMarcasIntervalo (en el modelo de semilla)
   public int getSoporte(int hilo){
      return modelos.get(hilo).frecuenciaHilo;
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

      //frecuenciaHilo hace falta cuando hay IMarcasIntervalo (en el modelo de semilla)
      protected int frecuenciaHilo;

      public ModeloParaleloHilo(int tSize){
         this.abiertas = new int[tSize][ventana]; // Hará falta más tamaño? @miguel
         this.tam = new int[tSize];
         this.limites = new int[tSize][2]; // inferior - superior
      }

      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         return siguienteCombinacion(tam, indices, index, tipo);
      }

      public void actualizaVentana(int sid, Evento evento){
         String[] tipos = getTipos();
         String tipo = evento.getTipo();
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
         if(index<0){ return; }

         actualizaVentana(sid, evento, tipo, evento.getInstante(), index, tipos.length);
      }

      protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
         boolean seguir = true;
         // Actualizar índices fin e inicio para adaptarse a la ventana
         // Eliminar elementos que ya no están en ventana
         for(int j,i=0;i<tSize;i++){
            j = limites[i][0];

            // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
            //while((tam[i] > 0) && (j!=limites[i][1]) && ((tmp-ventana >= abiertas[i][j]) || (tmp < abiertas[i][j]))) {
            while((tam[i] > 0) && ((tmp-ventana >= abiertas[i][j]) || (tmp < abiertas[i][j]))) {
               j = ((j+1) % ventana);
               tam[i]--;
            }
            limites[i][0] = j; // Modificar el indicador de inicio
            if(i!=index && tam[i]<=0) {
               seguir=false;
            }
         }
         // Añadir el nuevo elemento

         abiertas[index][limites[index][1]] = tmp;
         limites[index][1] = ((limites[index][1]+1)%ventana);
         tam[index]++;

         return seguir;
      }

      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String tipo = ev.getTipo(); //tipo del evento
         int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo); //indice del tipo de evento en la asociación temporal
         int tSize = tipos.length; //tamaño de la asociacion temporal
         int tmp = ev.getInstante(); //instante del evento

         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         int frecuenciaLocal=0;
         int[] patFrecLocal = new int[patrones.size()];
         int[] indices = new int[tSize];

         // Actualizar frecuencias
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            fijarInstancia(tSize, index, indices, instancia);
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         addFrecuencias(frecuenciaLocal, patFrecLocal);
      }

      protected void fijarInstancia(int tSize, int index, int[] indices, int[] instancia){
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
            }
         }
      }

      protected int fijarInstancia(int tSize, int index, int tmp, int[] indices, int[] instancia){
         int tMin = tmp;
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               if(tMin>instancia[i]){
                  tMin=instancia[i];
               }
            }
         }
         return tMin;
      }

      protected boolean comprobarPatrones(int[] instancia, int[] patFrecLocal, int sid,
            boolean savePatternInstances){
         int i;
         boolean encontrado = false;
         for(i=0; i<patrones.size(); i++){
            if(patrones.get(i).representa(sid,instancia, savePatternInstances)){
               encontrado = true;
               patFrecLocal[i]++;
               break;
            }
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

   } // Fin clase interna

}
