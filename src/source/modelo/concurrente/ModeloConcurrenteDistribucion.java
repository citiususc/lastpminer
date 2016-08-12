package source.modelo.concurrente;

import java.util.List;

import source.modelo.IAsociacionConDistribucion;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.Patron;

/**
 * @author vanesa.graino
 *
 */
public class ModeloConcurrenteDistribucion extends ModeloConcurrente implements IAsociacionConDistribucion {

   /**
    *
    */
   private static final long serialVersionUID = -3327926383739912695L;
   protected IClustering clustering;
   protected int[] distribucion;

   /*
    * Clases privadas
    */

   public ModeloConcurrenteDistribucion(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos,
         boolean... createHilos) {
      super(tipos, ventana, frecuencia, numHilos, createHilos);
      this.clustering = clustering;
   }

   public ModeloConcurrenteDistribucion(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos, boolean... createHilos) {
      super(tipos, ventana, patrones, frecuencia, numHilos,
            createHilos);
      this.clustering = clustering;
   }

   @Override
   public IClustering getClustering(){
      return clustering;
   }

   @Override
   public int[] getDistribucion(){
      return distribucion;
   }

   @Override
   public void incrementarDistribucion(int valor){
      throw new UnsupportedOperationException("Este método no debe llamarse para esta clase");
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloDistribucionHilo extends ModeloParaleloHilo {

      protected int[] distribucionHilo;


      public ModeloParaleloDistribucionHilo(int tSize) {
         super(tSize);
         this.distribucionHilo = ModeloDistribucion.iniciarDistribucion(tSize, ventana);//new int[(numTipos*(numTipos-1))/2][2*ventana + 1];
      }

      /**
      *
      * @param tSize
      * @param index
      * @param tmp
      * @param abiertas
      * @param limites
      * @param indices
      * @param instancia
      * @return devuelve el valor más pequeño de la instancia
      */
      protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas,
            int[][] limites, int[] indices, int[] instancia){
         int tMin = tmp;
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
               if(tMin>instancia[i]){ tMin=instancia[i]; }
            }
         }
         return tMin;
      }

      protected int buscaOcurrenciasTam2(String tipo, int index, int tmp, int tSize, int[] indices){
         int i,valor;
         int tMin = tmp;
         int[] instancia = new int[tSize];
         int[][] abiertas = getAbiertas();
         int[][] limites = getLimites();
         int[] tam = getTam();
         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia );

            // Actualizar las distribuciones de frecuencia
            for(i=0;i<tSize;i++){
               if(i==index){ continue; }// De esta lista solo se procesa el evento nuevo
               valor = (int)(instancia[i] - tmp);
               if((index>i) || (tipos[index] == tipos[i])) { // Relacion i-> index
                  valor *= -1;
                  distribucionHilo[valor+ventana]++;
                  //incrementarDistribucion(indexH, valor+ventana);
               }else{ // Relacion index -> i
                  distribucionHilo[valor+ventana]++;
                  //incrementarDistribucion(indexH, valor+ventana);
               }
            }
            frecuenciaHilo++;
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);
         }while(indices != null);
         return tMin;
      }
   }

   @Override
   public String[] getTiposRestricciones() {
       return tipos;
   }

}
