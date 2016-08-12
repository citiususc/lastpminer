package source.modelo.repetidos;

import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionConRepeticion;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.Patron;

public class ModeloDistribucionRepetido extends ModeloDistribucion implements IAsociacionConRepeticion {

   private static final long serialVersionUID = -5862289662944170394L;

   /*
    * Constructores
    */

   int[] rep = new int[]{1};
   int[] indices = new int[]{0,1};
   String[] tiposConRepetidos = new String[2];

   // El array sólo tiene un evento (es un array por compatibilidad)
   public ModeloDistribucionRepetido(String[] tipos, int ventana,
         Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, frecuencia, clustering);
      this.tiposConRepetidos[0] = tipos[0];
      this.tiposConRepetidos[1] = tipos[0];
   }

   // El array sólo tiene un evento (es un array por compatibilidad)
   public ModeloDistribucionRepetido(String[] tipos, int ventana,
         List<Patron> patrones,
         Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, patrones, frecuencia, clustering);
      this.tiposConRepetidos[0] = tipos[0];
      this.tiposConRepetidos[1] = tipos[0];
   }

   // El array sólo tiene un evento (es un array por compatibilidad)
   public ModeloDistribucionRepetido(String[] tipos, int ventana, List<Patron> patrones,
         int[] distribucion, IClustering clustering){
      super(tipos, ventana, patrones, distribucion, clustering);
      this.tiposConRepetidos[0] = tipos[0];
      this.tiposConRepetidos[1] = tipos[0];
   }

   // Diferencia: como se obtienen las instancias: hay que tener cuidado
   // de no tomar el mismo evento en los dos casos y que el segundo evento esté siempre
   // después del primero para no contabilizar una misma instancia dos veces

   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);

      final int tSize = tipos.length;

      int tmp = ev.getInstante();


      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize, rep)){
         return;
      }

      int[] indices = new int[tSize];

      // Actualizar frecuencias
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      buscaOcurrenciasTam2(tipo, index, index, tmp, indices);
   }


   //@Override
   protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites,
             int[] indices, int[] instancia){
      // Instancia tiene dos elementos y el segundo ya está fijado a tmp
      int i=0;
      instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
      return instancia[i];
   }

   @Override
   protected int buscaOcurrenciasTam2(String tipo, int index, int indexReal, int tmp, /*int tSize,*/ int[] indices){
      int frecuenciaLocal=0, valor;
      int tMin = tmp;
      final int tSize = tipos.length;

      int[][] abiertas = getAbiertas();
      int[][] limites=getLimites();
      int[] tam = getTam();

      int[] instancia = new int[2];
      instancia[1] = tmp;
      do{ // Recorre cada lista
         tMin = fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia);
         valor = (int)(tmp - instancia[0]);
          // Actualizar la distribucion de frecuencia
         incrementarDistribucion(valor+ventana);
         frecuenciaLocal++;
         indices = siguienteCombinacion(tam, indices, indexReal, tipo);
      }while(indices != null);
      //ultimaEncontrada[0]=tMin;
      //ultimaEncontrada[1]=tmp;

      addFrecuencias(frecuenciaLocal,null);
      return tMin;
   }


   @Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      int tSize = 1;//tipos.length;
      indices[tSize-1]++;
      int resta = rep[tSize-1];
      int i,j;
      for(i=tSize-1;i>0;i--){
         String tipoI = tipos[i];
         String tipoAnt = tipos[i-1];
         int mod;
         if(i==index){ //Si es el tipo del evento leido
            if(indices[i]!=0){
               indices[i]=0;
               indices[i-1]++;
               // Este indice no se cambia, es el nuevo evento
               // Los indices del mismo tipo se ponen a 0,1,2...
               //Si i fuese 0 entonces no se entraria aqui -> i no puede ser cero! @vanesa
               for(j=1;j<=rep[i];j++){
                  indices[i+j]=j-1;
               }
            }
            resta = rep[i-1];
            continue; // no puede ser i==index y además estar en un tipo repetido sin ser el i mas bajo
         }
         mod= tipoI==tipo? 1 : 0;
         if(indices[i]>=(tam[i]-(rep[i]-resta+mod))){
            indices[i-1]++;
            if(tipoAnt==tipoI){
               indices[i]=indices[i-1]+1;
               j=i+1;
               while((j<tSize)&&(tipos[j-1]==tipos[j])){
                  indices[j]=indices[j-1]+1;
                  j++;
               }
            }else{
               indices[i]=0;
               j=i+1;
               while((j<tSize)&&(tipos[j-1]==tipos[j])){
                  indices[j]=indices[j-1]+1;
                  j++;
               }
            }
         }else{
            break; // No hay que propagar más cambios, combinación de eventos válida.
         }
         if(tipoI==tipoAnt){
            resta--;
         }else{
         }
      }
      if(index==0){
         if(indices[0]>0){ return null; }
      }else{
         if(indices[0]>=(tam[0]-rep[0])){ return null; }
      }

      return indices;
   }

   public String[] getTiposRestricciones(){
       return tiposConRepetidos;
   }

   @Override
   public String toStringSinPatrones(){
      //return Arrays.toString(tipos);
       return "[" + tipos[0] + ", " + tipos[0] + INFIX + "1]";
   }

    @Override
    public int[] getIndices() {
        return indices;
    }

    @Override
    public int[] getRep() {
        return rep;
    }
}
