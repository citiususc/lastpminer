package source.modelo.clustering;

import java.util.ArrayList;
import java.util.List;

import source.restriccion.RIntervalo;

public class DominantPointClustering implements IClustering {

   protected DominantPointClustering(){
      // proteger el constructor para que haya que utiliza la fábrica
   }

   private double dEuclidea(int x1,int y1,int x2,int y2){
      return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
   }

   private int regSoporte(int[] df,int xInicio){
      int k;
      int n = df.length;
      int yperp;
      double lik,lik2,dik,dik2;

      int x1,x2,a1,a2;
      x1 = Math.max(xInicio-1,0);
      x2 = Math.min(xInicio+1,n-1);
      yperp = df[x1]+(xInicio-x1)*(df[x2]-df[x1])/(x2-x1);
      lik= dEuclidea(x1,df[x1],x2,df[x2]);
      dik= dEuclidea(xInicio,df[xInicio],xInicio,yperp);
      if(yperp<df[xInicio]){ dik=dik*(-1); }

      boolean continuar=true;
      for(k=1;continuar;k++){
         a1 = Math.max(x1-1,0);
         a2 = Math.min(x2+1,n-1);
         lik2=dEuclidea(a1,df[a1],a2,df[a2]);
         yperp=df[a1]+(xInicio-a1)*(df[a2]-df[a1])/(a2-a1);
         dik2=dEuclidea(xInicio,df[xInicio],xInicio,yperp);
         if(yperp<df[xInicio]){ dik2=dik2*(-1); }
         // Condición (3) del paper
         if(lik>=lik2){
            continuar=false;
         }else{
         // Condición (4) del paper
            if((dik>0)&&(dik/lik>=dik2/lik2)){
               continuar=false;
            }else{
               if((dik<0)&(dik/lik<=dik2/lik2)){ continuar=false; }
            }
         }
         lik=lik2;
         dik=dik2;
      }
      return k;
   }

   public List<RIntervalo> agrupar(int[] df, String tipoA, String tipoB) {
      List<RIntervalo> restricciones = new ArrayList<RIntervalo>();
      int n = df.length;
      int ventana = (n-1)/2;
      int i,j,k;
      //int[] limites = new int[2]; // Límites del punto actualmente en análisis

      // Localizar los 4 puntos dominantes "extremos"
      // int minX=0,maxX=N-1,minY=Integer.MAX_VALUE,maxY=0,pmaxY,pminY;
      // filas=pminX,pmaxX,pminY,pmaxY
      // cols=valor de X, valor de Y
      int [][] pDomInt = new int[ventana][2]; // x,k
      //int [][] extremos = new int[4][2];

      pDomInt[0][0] = 0;
      pDomInt[3][0] = n-1;
      pDomInt[1][1] = Integer.MAX_VALUE;

      for(i=0;i<n;i++){
         if(pDomInt[1][1]>df[i]){pDomInt[1][1]=df[i];pDomInt[1][0]=i;}
         if(pDomInt[2][1]<df[i]){pDomInt[2][1]=df[i];pDomInt[2][0]=i;}
      }
      // Ordenar extremos[i] según el eje X
      if(pDomInt[1][0]>pDomInt[2][0]){
         int aux=pDomInt[1][0];
         pDomInt[1][0]=pDomInt[2][0];
         pDomInt[2][0]=aux;
      }

      //Cálculo de la región de soporte de los cuatro puntos calculados

      for(i=0;i<4;i++){
         pDomInt[i][1]=regSoporte(df,pDomInt[i][0]);
      }

      int tam=4;
      // Buscar puntos dominantes entre los puntos anteriores
      for(i=0;i<tam-1;i++){ // No se trabajo con el punto maxX
         // Sólo hace falta comparar con el siguiente 'extremo'
         if(pDomInt[i][0]==pDomInt[i+1][0]){ continue; }// Son el mismo punto
         //else, no se solapan, hay un punto dominante en medio
         int xit = pDomInt[i][0];
         int kant = pDomInt[i][1];
         double pend=(df[pDomInt[i+1][0]]-df[xit])/(pDomInt[i+1][0]-xit); //pendiente del arco
         int pmax=xit;
         double yarco=df[xit],difmax=0;
         for(j=xit+1;j<pDomInt[i+1][0]-pDomInt[i+1][1];j++){
            yarco+=pend;
            if(yarco-df[j]>difmax) {difmax=yarco-df[j];pmax=j;}
         }
         if((pmax<xit+kant)||(pmax>pDomInt[i+1][0]-pDomInt[i+1][1])) {continue;}
         // punto encontrado, calcular region de soporte
         k = regSoporte(df,pmax);
         // Comprobar si la región se solapa con las vecinas
         if ((pmax-k<xit+kant)||(pmax+k>pDomInt[i+1][0]-pDomInt[i+1][1])){
            continue;
         }else{
            for(j=tam;j>i+1;j--){
               pDomInt[j][0]=pDomInt[j-1][0];
               pDomInt[j][1]=pDomInt[j-1][1];
            }
            pDomInt[i+1][0]=pmax;
            pDomInt[i+1][1]=k;
            tam++;
            //xant=pmax;
            //kant=k;
         }
      }

      // Construir las restricciones
      // ¿Cómo elegirlas?
      // ¿Cómo actuar con los solapamientos?
      for(i=0;i<tam;i++){
         if(pDomInt[i][0]==pDomInt[i+1][0]){ continue; }
         RIntervalo r = new RIntervalo(tipoA,tipoB,Math.max(pDomInt[i][0]-pDomInt[i][1]-ventana,(-1)*ventana),Math.min(pDomInt[i][0]+pDomInt[i][1]-ventana,ventana));
         restricciones.add(r);
      }


      return restricciones;
   }

}
