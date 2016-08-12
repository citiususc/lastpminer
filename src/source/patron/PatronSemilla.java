package source.patron;

import java.util.List;

import source.restriccion.RIntervalo;

/*
 * El único cambio importante se encuentra en el método 'representa'. Un PatronSemilla no tiene
 * por qué tener todas sus restricciones definidas (pueden valer -Infinity) y se asegura de que
 * esas restricciones, que representan un valor desconocido, se cumplen para cualquier instancia.
 */

public class PatronSemilla extends Patron {

   public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
   public static final int POSITIVE_INFINITY = Integer.MAX_VALUE;

   private final String[] tipos;
   private final int[][] matriz;

   private static String[] internalizeTipos(String[] tipos){
      for(int i=0;i<tipos.length;i++){
         tipos[i] = tipos[i].intern();
      }
      return tipos;
   }

   private static List<RIntervalo> internalizeRestricciones(List<RIntervalo> restricciones){
      for(int i=0;i<restricciones.size();i++){
         RIntervalo rest = restricciones.get(i);
         rest = new RIntervalo(rest.getTipoA().intern(), rest.getTipoB().intern(), rest.getInicio(), rest.getFin());
         restricciones.set(i, rest);
      }
      return restricciones;
   }

   /*
    * Constructores
    */

   protected PatronSemilla(String[] tipos, boolean savePatternInstances){
      super(internalizeTipos(tipos), savePatternInstances);
      this.tipos = getTipos();
      this.matriz = getMatriz();
   }

   public PatronSemilla(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      super(internalizeTipos(tipos), internalizeRestricciones(restricciones), savePatternInstances);
      this.tipos = getTipos();
      this.matriz = getMatriz();
      for(int i=0;i<tipos.length;i++){
         for(int j=i+1;j<tipos.length;j++){
            if(matriz[i][j]==Patron.NEGATIVE_INFINITY){
               matriz[i][j]=PatronSemilla.POSITIVE_INFINITY;
               matriz[j][i]=PatronSemilla.POSITIVE_INFINITY;
            }
         }
      }
   }

   public PatronSemilla(Patron patron){
      super(patron);
      this.tipos = getTipos();
      this.matriz = getMatriz();
   }

   /*
    * Métodos
    */

   public int[][] getMatrizRestricciones(){
      return matriz.clone();
   }

   public boolean representa(int sid, int[] instancia, boolean savePatternInstances){
      int num=instancia.length;
      if(num != tipos.length){ return false; }

      boolean seguir=true;
      int dist,i=0,j=0;
      for(i=0;i<num && seguir;i++){
         for(j=i+1;j<num && seguir;j++){
            // Las restricciones no introducidas son siempre representadas (-inf,inf)
            if(matriz[i][j] == NEGATIVE_INFINITY || matriz[j][i] == NEGATIVE_INFINITY){
               continue;
            }
            dist = (int)(instancia[j]-instancia[i]);
            if(dist>matriz[i][j]){ seguir=false; }
            if(dist<(-1)*matriz[j][i]){ seguir=false; }
         }
      }
      if(seguir && savePatternInstances){ addOcurrencia(sid,instancia); }
      return seguir;

   }

   /*@Override
   public boolean esRestriccionIndefinida(Integer valor0, Integer valor1){
      return PatronSemilla.NEGATIVE_INFINITY.equals(valor0) && PatronSemilla.POSITIVE_INFINITY.equals(valor1);
   }*/

   @Override
   public boolean esRestriccionIndefinida(int valor0, int valor1){
      return (PatronSemilla.POSITIVE_INFINITY== -valor0 || PatronSemilla.NEGATIVE_INFINITY == valor0)
            && PatronSemilla.POSITIVE_INFINITY == valor1;
   }

   @Override
   public PatronSemilla clonar() {
      return new PatronSemilla(this);
   }
}