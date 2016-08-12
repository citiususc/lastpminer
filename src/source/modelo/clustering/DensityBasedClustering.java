package source.modelo.clustering;

import java.util.ArrayList;
import java.util.List;

import source.restriccion.RIntervalo;

public class DensityBasedClustering implements IClustering {
   private double currentPercentage;// = ConfigurationParameters.currentPercentage;
   private double maximumPercentage;// = ConfigurationParameters.maximumPercentage;

   protected DensityBasedClustering(){
      //proteger el constructor para que haya que utiliza la fábrica
   }

   public List<RIntervalo> agrupar(int[] freqs, String tipoA, String tipoB) {
      List<RIntervalo> restricciones = new ArrayList<RIntervalo>();
      int min=Integer.MAX_VALUE, max=Integer.MIN_VALUE;
      int len = freqs.length;
      int ventana = (len-1)/2;
      int i=0,j=0,k=0,nMax,nMin;
      int[] minimos = new int[len], maximos = new int[len];

      // Búsqueda de mínimos/máximos de cada índice del intervalo
      if(freqs[0]>freqs[1]){ // Detectar si el primer valor es máximo o mínimo
         j++; //es un maximo
      }else{
         k++; //es un minimo
      }
      while(i<(len-1)){
         if(freqs[i]<freqs[i+1]){
            while((i<len-1)&&(freqs[i]<=freqs[i+1])){ i++; }
            maximos[j]=i;
            j++;
         }else{
            while((i<len-1)&&(freqs[i]>=freqs[i+1])){ i++; }
            minimos[k]=i;
            k++;
         }
      }
      //Resultado (ejemplo)
      // max
      //System.out.println("Maximos 1: " + printArray(maximos));
      /*if(freqs[i-1]<=freqs[i]){
         if((j>0) && (maximos[j-1]==i-1)) maximos[j-1]=i; // Seguía creciendo
         else{
            maximos[j]=i; // Empezaba a crecer
            j++;
         }
      }
      //System.out.println("Maximos 2: " + printArray(maximos));
      if(freqs[i-1]>=freqs[i]){
         if((k>0) && (minimos[k-1]==i-1)) minimos[k-1]++; // Seguía decreciendo
         else{
            minimos[k]=i; // Empezaba a decrecer
            k++;
         }
      }*/

      nMax = j; //último índice con info en maximos
      nMin = k; //último índice con info en mínimos

      // Ordenar los máximos en base a su frecuencia?
      for(i=0;i<nMax-1;i++){
         int aux = i;
         for(j=i+1;j<nMax;j++){
            if(freqs[maximos[aux]]<freqs[maximos[j]]){
               aux=j;
            }
         }
         int aux2 = maximos[i];
         maximos[i] = maximos[aux];
         maximos[aux] = aux2;
      }
      max = freqs[maximos[0]];
      //Buscar valor mínimo
      for(i=0;i<nMin;i++){
         if(min>freqs[minimos[i]]){
            min = freqs[minimos[i]];
         }
      }

      // Para cada máximo, buscar una restricción a su alrededor
      // Establecer algún criterio de parada: no todos los máximos tienen sentido en comparación
      // Establecer algún criterio de ampliación de restricción: cuándo una disposición es suficientemente frecuente
      // Quizá usar un criterio de similitud basado en diferencias de frecuencia?
      // Una distribución temporal solo puede pertenecer a un intervalo
      // Toda restricción se construye alrededor de un 'maximos[i]' que será la distribución temporal de mayor frecuencia

      int[] cdf = new int[len];
      int limInf,limSup;
      System.arraycopy(freqs, 0, cdf, 0, len);
      for(i=0;i<nMax;i++){
         // Criterio de parada 2: el máximo actual está más cerca de 'min' que de 'max'
         if(cdf[maximos[i]]<maximumPercentage*max){ // El máximo no pertenecería al cluster original
            break;
         }
         j=maximos[i];//j = maximos[i]-1;
         cdf[j]=0;

         // Flanco izquierdo
         // criterio 1: df[actual] < cierto % de maximo[i]
         while((j>0)&&(cdf[j-1]>currentPercentage*freqs[maximos[i]])) {
            cdf[--j]=0;
         }
         limInf=j;

         // Flanco derecho
         j=maximos[i];
         while((j<len-1)&&(cdf[j+1]>currentPercentage*freqs[maximos[i]])) {
            cdf[++j]=0;
         }
         limSup=j;

         // Eliminar de 'maximos' los valores ya asociados a un cluster
         j = i+1;
         while(j<nMax){//por cada maximo restante
            if((maximos[j]>=limInf)&&(maximos[j]<=limSup)){ // Está en el intervalo, eliminar
               for(k=j;k<nMax-1;k++) {
                  maximos[k]=maximos[k+1];
               }
               nMax--;
               maximos[nMax]=0;
            }else{
               j++;
            }
         }

         // Crear y añadir restriccion
         restricciones.add(new RIntervalo(tipoA,tipoB,limInf-ventana,limSup-ventana));
      }
      return restricciones;
   }

   public double getCurrentPercentage() {
      return currentPercentage;
   }

   public void setCurrentPercentage(double currentPercentage) {
      this.currentPercentage = currentPercentage;
   }

   public double getMaximumPercentage() {
      return maximumPercentage;
   }

   public void setMaximumPercentage(double maximumPercentage) {
      this.maximumPercentage = maximumPercentage;
   }

}
