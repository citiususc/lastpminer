package source.restriccion;

import java.util.Arrays;

public class RIntervalo implements Comparable<RIntervalo>{

   /*
    * Atributos
    */

   private final String tipoA, tipoB;
   private int[] valores;

   /*
    * Constructores
    */

   protected RIntervalo(String tipoA, String tipoB){
      this.tipoA = tipoA;
      this.tipoB = tipoB;
   }

   public RIntervalo(String tipoA, String tipoB, int min, int max){
      this(tipoA, tipoB);
      valores = new int[]{min, max};
   }

   /*
    * Métodos
    */

   public String getTipoA() {
      return tipoA;
   }

   public String getTipoB() {
      return tipoB;
   }

   public int[] getValores() {
      return valores;
   }

   public int getInicio(){
      return valores[0];
   }

   public int getFin(){
      return valores[1];
   }

//   public void asegurarOrden(){
//      if(tipoA.compareTo(tipoB)>0){
//         int aux = valores[0];
//         valores[0] = valores[1];
//         valores[1] = aux;
//         String auxS = tipoB;
//         tipoB = tipoA;
//         tipoA = auxS;
//      }
//   }

   public boolean incluye(RIntervalo r){
      // Ambas deben tener el mismo tipo
      if(tipoA != r.tipoA || tipoB!=r.tipoB){
         return false;
      }
      // Si 'this' cubre un intervalo mayor que 'r'
      if(valores[0]<=r.valores[0] && valores[1]>=r.valores[1]){
         return true;
      }
      // En otro caso
      return false;
   }

   public String toString(){
      //Para evitar -0.0 se utiliza Double.toString(valor + 0.0)
      return tipoA + " " + tipoB + " -> [" + Double.toString(valores[0] + 0.0) + ","+ Double.toString(valores[1] + 0.0) +"]";
   }

   @Override
   public int compareTo(RIntervalo intervalo2) {
      return Integer.compare(valores[0], intervalo2.valores[0]);
   }

   @Override
   public boolean equals(Object r){
      return r instanceof RIntervalo? equalsTo((RIntervalo)r) : false;
   }

   public boolean equalsTo(RIntervalo r){
      return tipoA==r.tipoA && tipoB==r.tipoB && valores[0]==r.valores[0] && valores[1]==r.valores[1] ||
            tipoA==r.tipoB && tipoB==r.tipoA && valores[0]==(-1)*r.valores[1] && valores[1]==(-1)*r.valores[0];
   }

   @Override
   public int hashCode(){
      return tipoA.hashCode() * tipoB.hashCode()* Arrays.hashCode(valores);
   }
}