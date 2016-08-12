package source.modelo.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.restriccion.RIntervalo;

public class MountainClustering implements IClustering {
   private static final Logger LOGGER = Logger.getLogger(MountainClustering.class.getName());

   private double lambda; // = ConfigurationParameters.mountainLambda;

   protected MountainClustering(){
      //proteger el constructor para que haya que utiliza la fábrica
   }


   public List<RIntervalo> agrupar(int[] hist, String tipoA, String tipoB) {
      List<RIntervalo> restricciones = new ArrayList<RIntervalo>();
      int i=0,j=0,k=0;
      int n = hist.length;
      int ventana = (n -1)/2;

      //double lambda = 0.9; // Pertenencia para corte, lambda-corte

      double[][] matrixMj = new double[n][n]; // Fila=iteración, columna=valor para la iteración
      double suma,sij,val;

      int[] max = new int[n]; // Índice de los prototipos
      int[][] limites = new int[n][2]; // Índices de los límites de los intervalos

      // Calculo de r y delta, parametros para condicion de parada
      double sm=0,ftotal=0,maxs=0,mins=Integer.MAX_VALUE;
      for(i=0;i<n;i++){
         if(maxs<hist[i]){ maxs=hist[i]; }
         if(mins>hist[i]){ mins=hist[i]; }
      }
      //maxs=maxs-mins;
      //maxs = N-1;
      maxs=0;
      for(i=0;i<n;i++) {ftotal += hist[i];}
      for(i=0;i<n;i++) {
         for(j=0;j<n;j++){
            val = (hist[i]+hist[j])/ftotal*Math.abs(j-i);
            if(maxs<val){ maxs=val; }
         }
      }

      for(i=0;i<n;i++){
         for(j=0;j<n;j++){
            val = (hist[i]+hist[j])/ftotal*Math.abs(j-i);
            sm += (maxs-val)/maxs;
            //sm += hist[j]*((N-1)-Math.abs(j-i))/(N-1);
            //sm += (maxs-Math.abs(hist[i]-hist[j]))/maxs;
         }
      }
      //sm /= ftotal;
      sm /= n*n;

      int m=1;
      double rm=5*m, delta, epsilon=0.97, corr=0;
      double[] varJ = new double[n];
      double[] varK = new double[n];
      double[] aux;
      double sumj=0,sumk=0,sumjk=0,sumj2=0,sumk2=0;
      while(corr<epsilon){
         for(k=0;k<n;k++){
            suma=0;
            for(j=0;j<n;j++){
               val = (hist[k]+hist[j])/ftotal*Math.abs(k-j);
               //suma = suma + Math.pow((N-1-Math.sqrt(Math.pow((k-j),2)))/(N-1),rm);
               //suma = suma + hist[j]*Math.pow((N-1-Math.abs(k-j))/(N-1),rm);
               suma = suma + Math.pow((maxs-val)/maxs,rm);
               //suma = Math.pow((maxs - Math.abs(hist[j]-hist[k]))/maxs, rm);
            }
            varJ[k] = suma;
         }
         //Calculo del coeficiente de correlacion de Pearson
         suma=0;
         for(k=0;k<n;k++){
            sumj=sumj+varJ[k];
            sumjk=sumjk+varJ[k]*varK[k];
            sumj2=sumj2+Math.pow(varJ[k],2);
            //sumk=sumk+K[k];
            //sumk2=sumk2+Math.pow(K[k],2);
         }
         LOGGER.fine("Raiz problematica: " + Math.sqrt(n*sumj2-Math.pow(sumj,2)));
         //corr = (N*sumjk-sumj*sumk)/(Math.sqrt(N*sumj2-Math.pow(sumj,2))+Math.sqrt(N*sumk2-Math.pow(sumk,2)));
         corr = (n*sumjk-sumj*sumk)/(Math.sqrt(n*sumj2-Math.pow(sumj,2))*Math.sqrt(n*sumk2-Math.pow(sumk,2)));
         LOGGER.fine("corr: " + corr);
         LOGGER.fine("corr<epsilon: " + (corr<epsilon)); //Error raiz negativa!!!
         // swap(J,K)
         sumk=sumj;
         sumk2=sumj2;
         aux=varK;
         varK=varJ;
         varJ=aux;
         //actualizar m y rm
         m++;
         rm+=5;
      }

      m--;
      rm-=5;
      delta=1/rm;


      // Calculo de M_1
      max[0]=0;
      for(k=0;k<n;k++){
         suma=0;
         if(hist[k]==0) {matrixMj[0][k]=0; continue;}
         for(i=0;i<n;i++){
            if(hist[i]==0){ continue; }
            //sij = ((N-1)-Math.abs(k-i))/(N-1);
            //sij = (maxs-Math.abs(hist[i]-hist[k]))/maxs;
            val = (hist[k]+hist[i])/ftotal*Math.abs(k-i);
            sij = (maxs-val)/maxs;
            //suma = suma + (hist[i]+hist[k])*Math.exp((-1)*Math.pow((1-sij)/(1-sm),rm));
            //suma = suma + hist[i]*Math.exp((-1)*Math.pow((1-sij)/(1-sm),rm));
            suma = suma + Math.exp((-1)*Math.pow((1-sij)/(1-sm),rm));
            //if(sij<lambda) break;
         }
         matrixMj[0][k] = suma;
         if(matrixMj[0][max[0]]<suma){ max[0]=k; }
      }
      // Detección de límites
      for(k=max[0]-1;k>=0;k--){
         val = (hist[k]+hist[max[0]])/ftotal*Math.abs(k-max[0]);
         sij = (maxs-val)/maxs;
         //if(Mj[0][k]/Mj[0][max[0]]<lambda) break;
         //if((hist[k]==0)||(maxs-Math.abs(hist[max[0]]-hist[k]))/maxs<lambda) break;
         //if((hist[k]==0)||(sij<lambda)) break; // Admite solapamiento
         if((hist[k]==0)||(sij<lambda)||(matrixMj[0][k]==0)){
            break; // NO admite solapamiento
         }
      }
      limites[0][0] = k+1;
      for(k=max[0]+1;k<n;k++){
         val = (hist[k]+hist[max[0]])/ftotal*Math.abs(k-max[0]);
         sij = (maxs-val)/maxs;
         //if(Mj[0][k]/Mj[0][max[0]]<lambda) break;
         //if((hist[k]==0)||(maxs-Math.abs(hist[max[0]]-hist[k]))/maxs<lambda) break;
         //if((hist[k]==0)||(sij<lambda)) break; // Admite solapamiento
         if((hist[k]==0)||(sij<lambda)||(matrixMj[0][k]==0)){
            break; // NO admite solapamiento
         }
      }
      limites[0][1] = k-1;

      // Calculo del resto de iteraciones
      boolean fin=false;
      for(j=1;j<n;j++){ // Iteración actual
         max[j]=0;
         for(k=0;k<n;k++){ // xk
            if((k>=limites[j-1][0])&&(k<=limites[j-1][1])){
               // Está dentro del área de influencia del cluster anterior
               matrixMj[j][k]=0;
            }else{
               matrixMj[j][k] = matrixMj[j-1][k]; // No lo está, pero puede ser área de influencia de uno anterior de todas formas
            }
            if(matrixMj[j][k]>matrixMj[j][max[j]]){ max[j]=k; }
         }
         // Calculo de limites del cluster actual
         for(k=max[j]-1;k>=0;k--){
            //if((hist[k]==0)||(maxs-Math.abs(hist[max[j]]-hist[k]))/maxs<lambda) break; // Admite solapamiento
            val = (hist[k]+hist[max[j]])/ftotal*Math.abs(k-max[j]);
            sij = (maxs-val)/maxs;
            //if((hist[k]==0)||(sij<lambda)) break; // Admite solapamiento
            if((hist[k]==0)||(sij<lambda)||(matrixMj[j][k]==0)){
               break; // NO admite solapamiento
            }
         }
         limites[j][0] = k+1;
         for(k=max[j]+1;k<n;k++){
            //if((hist[k]==0)||(maxs-Math.abs(hist[max[j]]-hist[k]))/maxs<lambda) break; // Admite solapamiento
            val = (hist[k]+hist[max[j]])/ftotal*Math.abs(k-max[j]);
            sij = (maxs-val)/maxs;
            //if((hist[k]==0)||(sij<lambda)) break; // Admite solapamiento
            if((hist[k]==0)||(sij<lambda)||(matrixMj[j][k]==0)){
               break; // NO admite solapamiento
            }
         }
         limites[j][1] = k-1;

         // Comprobación de final
         for(k=0;(!fin)&&(k<j);k++){
            if((max[j]>=limites[k][0])&&(max[j]<=limites[k][1])){ fin=true; }
            if((max[k]>=limites[j][0])&&(max[k]<=limites[j][1])){ fin=true; }
         }
         if((fin)||(matrixMj[j][max[j]]==0)||((matrixMj[j][max[j]]/matrixMj[0][max[0]])<delta)){
            break;
         }
      }

      // Evitar solapamientos de clusters
      int reps=0;
      for(i=j;i>0;i--){
         //Si hay intersección, hacer la unión
         if(limites[i][0]>limites[i-1][1]){ continue; }
         if(limites[i][1]<limites[i-1][0]){ continue; }

         if(limites[i-1][0]>limites[i][0]){ limites[i-1][0] = limites[i][0]; }
         if(limites[i-1][1]<limites[i][1]){ limites[i-1][1] = limites[i][1]; }
         reps++;
         for(k=i;k<j-reps;k++){
            limites[k][0] = limites[k+1][0];
            limites[k][1] = limites[k+1][1];
         }
      }
      // Construcción de restricciones
      for(i=0;i<=j-reps;i++){ // No se incluye la última iteracion
         restricciones.add(new RIntervalo(tipoA,tipoB,limites[i][0]-ventana,limites[i][1]-ventana));
      }

      return restricciones;
   }


   public double getLambda() {
      return lambda;
   }


   public void setLambda(double lambda) {
      this.lambda = lambda;
   }

}
