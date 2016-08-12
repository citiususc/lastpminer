package source.modelo.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase con útiles para el manejo de modelos con negación, como las pruebas
 * para comprobar si se pueden combinar dos modelos.
 * @author vanesa.graino
 *
 */
public final class HelperModeloNegacion {

   private HelperModeloNegacion() {
      // empty
   }

   /**
    * Sirve para comprobar si dos modelos con un único tipo de evento cada uno son
    * combinables (es decir, si son eventos diferentes).
    * Se presupone que están ordenados lexicográficamente, es decir, que modelo1 < modelo2.
    * Tener en cuenta que un tipo de evento negado siempre es mayor que uno positivo.
    * @param tipoEvento1 El tipo de evento del primer modelo.
    * @param esPositivo1 Si tipoEvento1 es positivo o no.
    * @param tipoEvento2 El tipo de evento del segundo modelo.
    * @param esPositivo2 Si tipoEvento2 es positivo o no.
    * @return Si no son combinables devuelve null. En otro caso, devuelve una lista con
    * dos elementos en el que el primero es un array con los elementos positivos de la
    * combinación de los dos modelos y el segundo un array con los negativos.
    */
   public static List<String[]> combinablesPrefijo(String tipoEvento1, boolean esPositivo1,
         String tipoEvento2, boolean esPositivo2){

      if(tipoEvento1 == tipoEvento2) return null;

      if(!esPositivo1){ //los dos son negativos
         List<String[]> sol = new ArrayList<String[]>();
         sol.add(new String[0]);
         sol.add(new String[]{tipoEvento1, tipoEvento2});
         return sol;
      }
      if(esPositivo2){ //los dos son positivos
         List<String[]> sol = new ArrayList<String[]>();
         sol.add(new String[]{tipoEvento1, tipoEvento2});
         sol.add(new String[0]);
         return sol;
      }
      List<String[]> sol = new ArrayList<String[]>();
      sol.add(new String[]{tipoEvento1});
      sol.add(new String[]{tipoEvento2});
      return sol;

   }

   /**
    * No se pueden hacer las asunciones de orden como con prefijos ya que puede ser negativo el primero y positivo el segundo.
    * @param tipoEvento1
    * @param esPositivo1
    * @param tipoEvento2
    * @param esPositivo2
    * @return
    */
   public static List<String[]> combinablesSufijo(String tipoEvento1, boolean esPositivo1,
         String tipoEvento2, boolean esPositivo2){

      if(tipoEvento1 == tipoEvento2) return null;

      List<String[]> sol = new ArrayList<String[]>();
      if(esPositivo1 && esPositivo2){
         sol.add(new String[]{tipoEvento1, tipoEvento2});
         sol.add(new String[0]);
      }else if(!esPositivo1 && !esPositivo2){
         sol.add(new String[0]);
         sol.add(new String[]{tipoEvento1, tipoEvento2});
      }else if(esPositivo1){
         sol.add(new String[]{tipoEvento1});
         sol.add(new String[]{tipoEvento2});
      }else{
         sol.add(new String[]{tipoEvento2});
         sol.add(new String[]{tipoEvento1});
      }
      return sol;

   }

   /**
    * Sobrecarga del método {@link #combinablesPrefijo(String[], String[], String[], String[])}.
    * @param asoc1
    * @param asoc2
    * @return
    */
   public static List<String[]> combinablesPrefijo(IAsociacionConNegacion asoc1, IAsociacionConNegacion asoc2){
      return combinablesPrefijo(asoc1.getTipos(), asoc1.getTiposNegados(), asoc2.getTipos(), asoc2.getTiposNegados());
   }

   public static List<String[]> combinarPrefijo(IAsociacionConNegacion asoc1, IAsociacionConNegacion asoc2){
      return combinarPrefijo(asoc1.getTipos(), asoc1.getTiposNegados(), asoc2.getTipos(), asoc2.getTiposNegados());
   }

   /**
    * A diferencia de combinables no comprueba si hay contradicción
    * Atención: Este método solo funciona con modelos ordenados por prefijo
    * porque presupone que si no hay negativos en el segundo tampoco los habrá
    * en el primero (esto no se cumple con ordenación con sufijos)
    * @param positivos1
    * @param negativos1
    * @param positivos2
    * @param negativos2
    * @return
    */
   public static List<String[]> combinarPrefijo(String[] positivos1, String[] negativos1,
         String[] positivos2, String[] negativos2){

      List<String[]> sol = new ArrayList<String[]>();
      //Si negativos2.length > 0 el nuevo tipo está ahí
      if(negativos2.length>0){
         sol.add(positivos1.clone());
         String[] modArray = Arrays.copyOf(negativos1, negativos1.length+1);
         modArray[modArray.length-1] = negativos2[negativos2.length-1];
         sol.add(modArray);
      }else{
         //No tienen negativos
         String[] modArray = Arrays.copyOf(positivos1, positivos1.length+1);
         modArray[modArray.length-1] = positivos2[positivos2.length-1];
         sol.add(modArray);
         sol.add(new String[0]);
      }
      return sol;
   }

   /**
    * Se presupone que son combinables
    * @param asoc1
    * @param tipo
    * @return
    */
   public static List<String[]> combinarSufijo(String[] positivos1, String[] negativos1, String tipo ){
      List<String[]> sol = new ArrayList<String[]>();

      if(tipo.endsWith(IAsociacionConNegacion.SUF_NEG)){
         //El nuevo tipo es negativo
         String nuevoTipo = tipo.substring(0, tipo.length() - IAsociacionConNegacion.SUF_NEG.length());
         sol.add(positivos1.clone());
         String[] modArray = Arrays.copyOf(negativos1, negativos1.length+1);
          modArray[modArray.length-1] = nuevoTipo;
          sol.add(modArray);
      }else{
         //El nuevo tipo es positivo
         String nuevoTipo = tipo;
         String[] modArray = Arrays.copyOf(positivos1, positivos1.length+1);
          modArray[modArray.length-1] = nuevoTipo;
          sol.add(modArray);
          sol.add(negativos1.clone());
      }

      return sol;
   }

   /**
    * Se presupone que son combinables
    * @param asoc1
    * @param tipo
    * @return
    */
   public static List<String[]> combinarSufijo(IAsociacionConNegacion asoc1, String tipo ){
      return combinarSufijo(asoc1.getTipos(), asoc1.getTiposNegados(), tipo);
   }

   /**
    * Precondición: los modelos comparten todos los elementos menos el último.
    * @param positivos1
    * @param negativos1
    * @param positivos2
    * @param negativos2
    * @return
    */
   public static List<String[]> combinablesPrefijo(String[] positivos1, String[] negativos1,
         String[] positivos2, String[] negativos2){

      /*
       * Esto no es necesario ya que sólo difieren en el último tipo
       */
      /*if(!Collections.disjoint(Arrays.asList(positivos1), Arrays.asList(negativos2))){
         return false;
      }
      if(!Collections.disjoint(Arrays.asList(positivos2), Arrays.asList(negativos1))){
         return false;
      }*/
      // Solo hay positivos
      if(negativos1.length == 0 && negativos2.length == 0){
         //return true;
         List<String[]> sol = new ArrayList<String[]>();
         String[] modArray = Arrays.copyOf(positivos1, positivos1.length+1);
         modArray[modArray.length-1] = positivos2[positivos2.length-1];
         sol.add(modArray);
         sol.add(new String[0]);
         return sol;
      }

      // Sólo hay negativos
      if(positivos1.length == 0 && positivos2.length == 0){
         //return true;
         List<String[]> sol = new ArrayList<String[]>();
         sol.add(new String[0]);
         String[] modArray = Arrays.copyOf(negativos1, negativos1.length+1);
         modArray[modArray.length-1] = negativos2[negativos2.length-1];
         sol.add(modArray);
         return sol;
      }

      // Sólo uno de los dos tiene elementos negativos
      if(negativos1.length == 0){
         //return !Arrays.asList(positivos1).contains(negativos2[negativos2.length-1]);
         if(!Arrays.asList(positivos1).contains(negativos2[negativos2.length-1])){
            List<String[]> sol = new ArrayList<String[]>();
            sol.add(positivos1.clone());
            sol.add(negativos2.clone());
            return sol;
         }else{
            return null;
         }
      }

      if(negativos2.length == 0){
         //return !Arrays.asList(positivos2).contains(negativos1[negativos1.length-1]);
         if(!Arrays.asList(positivos2).contains(negativos1[negativos1.length-1])){
            List<String[]> sol = new ArrayList<String[]>();
            sol.add(positivos2.clone());
            sol.add(negativos1.clone());
            return sol;
         }else{
            return null;
         }
      }

      //Los dos tienen elementos negativos
      //TODO puede suceder la segunda parte?
      if(!Arrays.asList(positivos1).contains(negativos2[negativos2.length-1])
            && !Arrays.asList(positivos2).contains(negativos1[negativos1.length-1])){
         List<String[]> sol = new ArrayList<String[]>();
         sol.add(positivos2.clone());
         String[] modArray = Arrays.copyOf(negativos1, negativos1.length+1);
         modArray[modArray.length-1] = negativos2[negativos2.length-1];
         sol.add(modArray);
         return sol;
      }
      //return false;
      return null;
   }
}
