package source.modelo.concurrente2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionConDistribucion;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.GeneradorID;
import source.patron.Patron;

public class ModeloConcurrenteDistribucion extends ModeloConcurrente implements IAsociacionConDistribucion{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteDistribucion.class.getName());
   private static final long serialVersionUID = -4606062922688255094L;

   /*
    * Atributos propios
    */

   private int[] distribucion;
   protected IClustering clustering;

   /*
    * Constructores
    */

   public ModeloConcurrenteDistribucion(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos,
         boolean... createHilos) {
      super(tipos, ventana, frecuencia, numHilos, createHilos);
      this.clustering = clustering;
      this.distribucion = ModeloDistribucion.iniciarDistribucion(tipos.length, ventana);
   }

   public ModeloConcurrenteDistribucion(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos, boolean... createHilos) {
      super(tipos, ventana, patrones, frecuencia, numHilos, createHilos);
      this.clustering = clustering;
//      this.distribucion = ModeloDistribucion.iniciarDistribucion(tipos.size(), this.numTipos, ventana);
   }

   public ModeloConcurrenteDistribucion(String[] tipos, int ventana, List<Patron> patrones, int[] distribucion,
         IClustering clustering, int numHilos, boolean... createHilos){
      super(tipos, ventana, patrones, null, numHilos, createHilos);
      this.clustering = clustering;
      this.addFrecuencias(ModeloDistribucion.frecuenciaPorDistribucion(distribucion, ventana), null);
      int[] patFrec = getPatFrec();
      // Recoger frecuencia de los patrones de 'distribucion'
      ModeloDistribucion.frecuenciaPatronesPorDistribucion(patFrec, patrones, distribucion, ventana);
   }

   /*
    * Métodos propios
    */

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      for(int i=0;i<numHilos;i++){
         modelos.add(new ModeloParaleloDistribucionHilo(tSize));
      }
   }

   // Copiado de ModeloDistribucion
   @Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException{

     int i=0;
     int[] patFrec = getPatFrec();

     // Evaluación del histograma y agrupamiento
     int[] aux = ModeloDistribucion.construirPatrones(genID, this, patternClassName, savePatternInstances, patrones);

     int len = aux.length;
     for(i=patrones.size()-1;i>=0;i--){
        if(aux[i]<supmin){ patrones.remove(i); }
     }
     //Si todos eran frecuentes se sustituye patFrec, sino se eliminan los que no son frecuentes
     if(patrones.size()==len){
        patFrec=aux;
     }else{
        int j=0;
        patFrec = new int[patrones.size()];
        for(i=0,j=0;(i<aux.length)&&(j<patFrec.length);i++){
           if(aux[i]>=supmin){
              patFrec[j]=aux[i];
              j++;
           }
        }
     }
     setPatFrec(patFrec);
     return 0;
  }

   @Override
   public int[] getDistribucion() {
      return distribucion;
   }

   @Override
   public IClustering getClustering() {
      return clustering;
   }

   @Override
   public void incrementarDistribucion(int valor) {
      synchronized(distribucion){
         distribucion[valor]++;
      }
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloDistribucionHilo extends ModeloParaleloHilo {

      public ModeloParaleloDistribucionHilo(int tSize) {
         super(tSize);
      }

      protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites, int[] indices, int[] instancia){
         int tMin = tmp;
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               if(tMin>instancia[i]){ tMin=instancia[i]; }
            }
         }
         return tMin;
      }

      protected int buscaOcurrenciasTam2(String tipo, int index, int indexReal, int tmp, int tSize, int[] indices){
         int i,frecuenciaLocal=0,valor;
         int tMin = tmp;
         int[] instancia = new int[tSize];

         int[][] abiertas=getAbiertas();
         int[][] limites = getLimites();
         int[] tam = getTam();
         do{ // Recorre cada lista
            tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia);
            // Actualizar las distribuciones de frecuencia
            for(i=0;i<tSize;i++){ // Actualiza las distribuciones de frecuencia
               if(i!=indexReal){
                  // De esta lista solo se procesa el evento nuevo
                  valor = (int)(instancia[i] - tmp);
                  if(indexReal>i || tipos[indexReal] == tipos[i]) { // Relacion i-> index
                     valor *= -1;
                     incrementarDistribucion(valor + ventana);
                  }else{ // Relacion index -> i
                     incrementarDistribucion(valor + ventana);
                  }
               }
            }
            frecuenciaLocal++;
            indices = siguienteCombinacionHilo(tam, indices, indexReal, tipo);
         }while(indices != null);
         addFrecuencias(frecuenciaLocal, null);
         return tMin;
      }

      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String tipo = ev.getTipo(); //tipo del evento
         int index = Arrays.binarySearch(tipos, tipo); // tipos.indexOf(tipo); //indice del tipo de evento en la asociación temporal
         final int tSize = 2; //tipos.length; //tamaño de la asociacion temporal
         int tmp = ev.getInstante(); //instante del evento

         if(!actualizaVentana(sid, ev,tipo,tmp,index,tSize)){
            return;
         }

         int[] indices = new int[tSize];

         // Actualizar frecuencias
         int[] instancia = new int[tSize];
         instancia[index] = tmp;

         //if(tSize==2){
            buscaOcurrenciasTam2(tipo, index, index, tmp, tSize, indices);
         //}
      }

   }


   @Override
   public boolean necesitaPurga(int minFreq) {
      return getSoporte()<minFreq;
   }

   @Override
   public String[] getTiposRestricciones() {
       return tipos;
   }
}
