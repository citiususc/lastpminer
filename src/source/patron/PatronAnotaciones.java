package source.patron;

import java.util.Arrays;
import java.util.List;

import source.restriccion.RIntervalo;

public class PatronAnotaciones extends PatronDictionaryFinalEvent {

   /**
    * Identifica si el patron es más específico que alguno de sus padres
    * o si por el contrario tiene las mismas restricciones temporales.
    * Puede modificarse en la combinación o en el cálculo de consistencia (aplicación
    * del algoritmo de Floyd-Warshall).
    */
   private boolean masEspecifico; //defaut false

   public PatronAnotaciones(String[] tipos, boolean savePatternInstances) {
      super(tipos, savePatternInstances);
   }

   public PatronAnotaciones(String[] tipos, List<RIntervalo> restricciones,
         boolean savePatternInstances) {
      super(tipos, restricciones, savePatternInstances);
   }

   public PatronAnotaciones(String[] tipos, Patron patron) {
      super(tipos, patron);
   }

   public PatronAnotaciones(Patron patron) {
      super(patron);
   }

   public PatronAnotaciones(PatronDictionaryFinalEvent patron) {
      super(patron);
   }

   public boolean combinar(Patron patron){
      if(patron==null){ return false; }

      int i,j;
      //int tSize = tipos.size()-1; // Se comparan todos los tipos menos primero y ultimo
      int pSize = patron.getTipos().length;
      int[][] pMatriz = patron.getMatriz(), matriz = getMatriz();
      List<String> tipos = Arrays.asList(getTipos()), pTipos = Arrays.asList(patron.getTipos());

      int thisIndexA,thisIndexB; //indices en el patron 'this' de los tipos A y B en 'patron'

      // Asumiendo que patron incluye todos los 'tipos' menos el primero
      for(i=0;i<pSize;i++){
         thisIndexA = tipos.indexOf(pTipos.get(i));
         for(j=i+1;j<pSize;j++){
            thisIndexB = tipos.indexOf(pTipos.get(j));

            // Todavía no existía esta restricción
            if(matriz[thisIndexA][thisIndexB]== NEGATIVE_INFINITY){
               matriz[thisIndexA][thisIndexB] = pMatriz[i][j];
               matriz[thisIndexB][thisIndexA] = pMatriz[j][i];
               continue;
            }

            // Las restricciones comunes deben tener interseccion no nula
            if(pMatriz[i][j] < (-1)*matriz[thisIndexB][thisIndexA]){ return false; }
            if((-1)*pMatriz[j][i] > matriz[thisIndexA][thisIndexB]){ return false; }

            // Hay intersección, quedarse con la intersección de cada restricción
            if(pMatriz[i][j] < matriz[thisIndexA][thisIndexB]){
               matriz[thisIndexA][thisIndexB] = pMatriz[i][j];
               masEspecifico = true;
            }
            if(pMatriz[j][i] < matriz[thisIndexB][thisIndexA]){
               matriz[thisIndexB][thisIndexA] = pMatriz[j][i] ;
                     //Math.max(matriz[thisIndexB][thisIndexA],pMatriz[j][i]);
                     //Math.min(matriz[thisIndexB][thisIndexA],pMatriz[j][i]); // Son números multiplicados por -1
               masEspecifico = true;
            }
         }
      }

      if(patron instanceof PatronDictionaryFinalEvent){
         padres.add((PatronDictionaryFinalEvent)patron);
      }

      return true;
   }


   @Override
   public boolean combinar(Patron patron, int indiceAusente){
      if(patron==null){ return false; }
      String[] tipos = getTipos();
      int i,j,tSize = tipos.length; // Se comparan todos los tipos menos primero y ultimo
      int[][] pMatriz = patron.getMatriz(), matriz = getMatriz();

      int dI=0,dJ=0;

      for(i=0;i<tSize;i++){
         if(i==indiceAusente) {
            dI=1;
            dJ=1;
            continue;
         }
         for(j=i+1;j<tSize;j++){
            if(j==indiceAusente) {
               dJ=1;
               continue;
            }
            if(j>indiceAusente) {
               dJ=1;
            }
            // Nueva restriccion?
            if(matriz[i][j]== NEGATIVE_INFINITY){
               matriz[i][j] = pMatriz[i-dI][j-dJ];
               matriz[j][i] = pMatriz[j-dJ][i-dI];
            }else{
               // Restriccion compatible?
               if(pMatriz[i-dI][j-dJ] < (-1)*matriz[j][i]){ return false; }
               if((-1)*pMatriz[j-dJ][i-dI] > matriz[i][j]){ return false; }
               // Hay intersección, quedarse con la intersección de cada restricción
               if(pMatriz[i-dI][j-dJ] < matriz[i][j]){
                  matriz[i][j]=pMatriz[i-dI][j-dJ];
                  masEspecifico = true;
               }
               // Son números multiplicados por -1 por lo que en lugar de mayor es menor
               if(pMatriz[j-dJ][i-dI]<matriz[j][i]){
                  matriz[j][i] = pMatriz[j-dJ][i-dI];
                  masEspecifico = true;
               }
            }
         }
         dJ=0;
      }

      if(patron instanceof PatronDictionaryFinalEvent){
         padres.add((PatronDictionaryFinalEvent)patron);
      }
      return true;
   }

   @Override
   public boolean esConsistente(GeneradorID genId){
      int num = getTipos().length;
      int min,max;
      int[][] matriz = getMatriz();
      int i=0,j=0,k=0;
      boolean masEspecificoLocal = false;

      // Se eliminan los valores flag
      for(i=0;i<num;i++){
         for(j=0;j<num;j++){
            if(matriz[i][j] == NEGATIVE_INFINITY){
               matriz[i][j]=0;
            }
         }
      }

      // Si calculan los nuevos valores de las restricciones
      for(k=0;k<num;k++){
         for(i=0;i<num;i++){
            for(j=0;j<num;j++){
               int aux = matriz[i][k]+matriz[k][j];
               if(aux < matriz[i][j]){
                  masEspecificoLocal = true;
                  matriz[i][j] = aux;
               }
            }
         }
      }

      // Se actualizan las restricciones mientras se comprueba la consistencia
      //restricciones = new ArrayList<RIntervalo>();
      for(i=0;i<num;i++){
         for(j=i+1;j<num;j++){
            min = (-1)*matriz[j][i];
            max = matriz[i][j];
            if(min>max){ return false; }
         }
      }
      this.masEspecifico = masEspecificoLocal;
      patternID = genId.nextID();

      calculaTiposFinales();


      for(PatronDictionaryFinalEvent padre : padres){
         padre.addHijo(this);
         //padre.comprobarConsistenciaDiccionario();//TODO esto no hace falta no? @vanesa
      }
      return true;
   }


   public boolean esMasEspecifico(){
      return masEspecifico;
   }

}
