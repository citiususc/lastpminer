package source.patron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import source.restriccion.RIntervalo;

public class Patron implements Comparator<Patron>, Comparable<Patron>{
   public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
   public static final int POSITIVE_INFINITY = Integer.MAX_VALUE;

   //public static int noOfInstances = 0;//TODO contabilizado instancias

   private static boolean printID = true;

   protected int patternID;
   private final String[] tipos;
   // Se guardan las restricciones del patrón en una matriz en la que la parte inferior está negada
   private int[][] matriz;
   private List<Ocurrencia> ocurrencias;

   /*
    * Static methods
    */

   public static boolean isPrintID() {
      return printID;
   }

   public static void setPrintID(boolean newPrintId) {
      printID = newPrintId;
   }

   /*
    * Constructores
    */

   //{ noOfInstances++; } //TODO contabilizado instancias

   protected Patron(String[] tipos, boolean savePatternInstances){
      this.tipos = tipos;
      if(savePatternInstances){
         this.ocurrencias = new ArrayList<Ocurrencia>();
      }
   }

   public Patron(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      this(tipos, savePatternInstances);
      construirMatrizRestricciones(restricciones);
   }

   public Patron(String[] tipos, int[][] matriz, Boolean savePatternInstances){
      this(tipos, savePatternInstances);
      this.matriz = matriz;
   }

   public Patron(Patron patron){
      int i, j;
      this.tipos = patron.tipos;
      if(patron.ocurrencias != null){
         this.ocurrencias = new ArrayList<Ocurrencia>(patron.ocurrencias);
      }
      matriz = new int[tipos.length][tipos.length];
      for(i=0; i<tipos.length; i++){
         for(j=i+1; j<tipos.length; j++){
            this.matriz[i][j] = patron.matriz[i][j];
            this.matriz[j][i] = patron.matriz[j][i];
         }
      }
   }

   public Patron(String[] tipos, Patron patron){
      this(tipos, patron.ocurrencias!=null);
      int num = tipos.length,i,j;
      this.matriz = new int[num][num];
      int indexI,indexJ;

      for(i=0; i<num; i++){
         for(j=0; j<num; j++){
            this.matriz[i][j] = NEGATIVE_INFINITY;
         }
      }

      for(i=0; i<patron.getTipos().length; i++){
         indexI = Arrays.binarySearch(tipos, patron.getTipos()[i]);//tipos.indexOf(patron.getTipos()[i]);
         for(j=i+1; j<patron.getTipos().length; j++){
            indexJ = Arrays.binarySearch(tipos, patron.getTipos()[j]);//tipos.indexOf(patron.getTipos()[j]);
            this.matriz[indexI][indexJ] = patron.matriz[i][j];
            this.matriz[indexJ][indexI] = patron.matriz[j][i];
         }
      }
   }

   /*
    * Métodos
    */

   public int[][] getMatriz(){
      return matriz;
   }

   private void addRestriccionToMatriz(RIntervalo r){
      final int num = tipos.length;
      int inicio = r.getInicio(), fin = r.getFin();
      int j=0, k=0;
      int indexA=0, indexB=0/*, mod=0, */;

      indexA = Arrays.binarySearch(tipos, r.getTipoA());
      indexB = Arrays.binarySearch(tipos, r.getTipoB());

      //Si A>B hacemos swap de las restricciones
      if(indexA>indexB){
         int aux = inicio;
         inicio = -1*fin;
         fin = -1*aux;
         int swap = indexA;
         indexA = indexB;
         indexB = swap;
      }

      boolean acabar = true;
      // Buscar que celdas no estan ocupadas
      for(k = indexA; k<=indexA && k<num; k++){
         for(j = indexB>k ? indexB : k+1; j <= indexB && j<num; j++){
            acabar = matriz[k][j] == NEGATIVE_INFINITY;
            if(acabar){ break; }
         }
         if(acabar) break;
      }

      // A -> B
      matriz[k][j] = fin;
      // B -> A
      matriz[j][k] = -1*inicio;
   }

   private void construirMatrizRestricciones(List<RIntervalo> restricciones){
      final int num = tipos.length;
      this.matriz = new int[num][num];

      //Inicializar el array de distancias a valores 'imposibles'
      for(int i=0; i<num; i++){
         Arrays.fill(matriz[i], NEGATIVE_INFINITY);
      }

      for(RIntervalo r : restricciones){
         addRestriccionToMatriz(r);
      }

      restricciones = null;
   }

   public void addRestriccion(RIntervalo r){
      addRestriccionToMatriz(r);
   }

   public List<RIntervalo> getRestricciones() {
      //if(restricciones != null){
      //   return restricciones;
      //}
      List<RIntervalo> result = new ArrayList<RIntervalo>();
      int tSize = tipos.length;
      for(int i=0; i<tSize; i++){
         for(int j=i+1; j<tSize; j++){
            //TODO-MIGUEL: antes estaba esto: if(matriz[i][j]!= NEGATIVE_INFINITY && matriz[j][i]!= NEGATIVE_INFINITY) (@vanesa)
            RIntervalo r = new RIntervalo(tipos[i], tipos[j], -matriz[j][i], matriz[i][j]);
            if(!esRestriccionIndefinida(r)){
               result.add(r);
            }
         }
      }
      return result;
   }

   /**
    * TODO-MIGUEL: añadida esta función (@vanesa)
    */
   public boolean esRestriccionIndefinida(RIntervalo restriccion){
      //return ((restriccion.getValores()[0] == new Float(Integer.MIN_VALUE)) && (restriccion.getValores()[1] == new Float(Integer.MAX_VALUE)));
      return esRestriccionIndefinida(restriccion.getValores()[0], restriccion.getValores()[1]);
   }

   /*public boolean esRestriccionIndefinida(Integer valor0, Integer valor1){
      return Patron.NEGATIVE_INFINITY.equals(valor1) && Patron.POSITIVE_INFINITY.equals(valor0);
   }*/

   public boolean esRestriccionIndefinida(int valor0, int valor1){
      return Patron.NEGATIVE_INFINITY == valor1 && Patron.POSITIVE_INFINITY == valor0;
   }

   public List<RIntervalo> getRestricciones(String tipo){
      int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo);
      if(index<0){
         return null;
      }
      final int num = tipos.length;
      int i;

      List<RIntervalo> lista = new ArrayList<RIntervalo>();
      for(i=0; i<index; i++){
         //aux.add(new RIntervalo(tipos.get(i),tipos.get(index),(-1)*matriz[index][i],matriz[i][index]));
         RIntervalo aux = new RIntervalo(tipos[i], tipos[index], -matriz[index][i], matriz[i][index]);
         if(!esRestriccionIndefinida(aux)){
            lista.add(aux);
         }
      }
      for(i=index+1; i<num; i++){
         //aux.add(new RIntervalo(tipos.get(index),tipos.get(i),(-1)*matriz[i][index],matriz[index][i]));
         RIntervalo aux = new RIntervalo(tipos[index], tipos[i], -matriz[i][index], matriz[index][i]);
         if(!esRestriccionIndefinida(aux)){
            lista.add(aux);
         }
      }

      return lista;
   }

   public List<RIntervalo> getRestricciones(String desde, String hacia){

      //int indexD = tipos.indexOf(desde), indexH = tipos.indexOf(hacia);
      int indexD = Arrays.binarySearch(tipos, desde), indexH = Arrays.binarySearch(tipos, hacia);

      List<RIntervalo> ret = new ArrayList<RIntervalo>();
      if( indexD<0 || indexH<0){
         return ret;
      }

      int i,j;
      RIntervalo aux;
      for(i=indexD; i<=indexD; i++){ // Desde
         for(j= indexD==indexH? i+1 : indexH; j <= indexH; j++){ // Hacia
            aux = new RIntervalo(desde, hacia, (-1)*matriz[j][i], matriz[i][j]);
            if(!esRestriccionIndefinida(aux)){
               ret.add(aux);
            }
         }
      }

      return ret;
   }

   /**
    *
    * @param sid
    * @param instancia
    * @return
    */
   public boolean representa(int sid, int[] instancia, boolean savePatternInstances){
      final int num = instancia.length;
//      if(num != tipos.length){
//         return false;
//      }

      int i=0, j=0;
      boolean seguir=true;
      int dist;

      // Comprobar que se cumplen todas las restricciones
      // 'matriz' sigue el formalismo STP
      for(i=0; seguir && i<num; i++){
         for(j=i+1; seguir && j<num; j++){
            dist = (int)(instancia[j]-instancia[i]);
            if(dist>matriz[i][j] || dist<-matriz[j][i]){
               seguir = false;
            }
         }
      }
      if(seguir && savePatternInstances){
         addOcurrencia(sid, instancia);
      }
      return seguir;
   }

   // Mejor comprobar la consistencia por cada restriccion añadida?
   public boolean esConsistente(GeneradorID genId){
      final int num = tipos.length;
      int i=0, j=0, k=0;

      // Se eliminan los valores flag
      for(i=0;i<num;i++){
         for(j=0;j<num;j++){
            if(matriz[i][j] == NEGATIVE_INFINITY){
               matriz[i][j] = 0;
            }
         }
      }

      // Si calculan los nuevos valores de las restricciones
      for(k=0;k<num;k++){
         for(i=0;i<num;i++){
            for(j=0;j<num;j++){
               //matriz[i][j] = Math.min(matriz[i][j],matriz[i][k]+matriz[k][j]);
               matriz[i][j] = matriz[i][j]<matriz[i][k]+matriz[k][j] ? matriz[i][j] : matriz[i][k]+matriz[k][j];
            }
         }
      }

      // Se actualizan las restricciones mientras se comprueba la consistencia
      for(i=0;i<num;i++){
         for(j=i+1;j<num;j++){
            if((-1)*matriz[j][i]>matriz[i][j]){
               return false;
            }
         }
      }
      patternID = genId.nextID();//idGenerator++;
      return true;

   }

   public String[] getTipos(){
      return tipos;
   }

   //FIXME Double.toString para poder utilizar las referencias que ya están creadas cuando se utilizaban floats
   public String toString(){
      int i, j, num = tipos.length;
      StringBuilder aux = new StringBuilder(Arrays.toString(tipos)).append(" ID: " + (printID? patternID : "" ) + "\n");
      for(i=0; i<num; i++){
         for(j=i+1; j<num; j++){
            if(!esRestriccionIndefinida(-matriz[j][i],matriz[i][j])){
               aux.append(tipos[i] + " " + tipos[j]
                     + " -> [" + Double.toString((-1)*matriz[j][i])
                     + "," + Double.toString(matriz[i][j]) + "]\n");
            }
         }
      }
      return aux.toString();
   }

   @Override
   public boolean equals(Object obj){
      if(!(obj instanceof Patron)){
         return false;
      }
      return equalsTo((Patron)obj);
   }

   public boolean equalsTo(Patron patron){
      final int num = tipos.length;
      if(num != patron.tipos.length
            || !Arrays.asList(tipos).containsAll(Arrays.asList(patron.tipos))){
         return false;
      }
      for(int i=0;i<num;i++){
         for(int j=0;j<num;j++){
            if(i!=j && matriz[i][j]!=patron.matriz[i][j]){
               return false;
            }
         }
      }
      return true;
   }

   @Override
   public int hashCode(){
      return tipos.hashCode() * Arrays.hashCode(matriz);
   }


   public boolean isTheSame(Patron patron){
      return this.patternID==patron.patternID;
   }

   // Precond: 'patron' es un posible subpatron de 'this'
   /**
    * Combina un patron con this
    * @param patron Patrón con el que se quiere combinar this. Tiene que ser un posible subpatron de 'this'.
    * @return si se puede realizar la combinación o no
    */
   public boolean combinar(Patron patron){
      if(patron == null){
         return false;
      }

      int i,j,pSize = patron.tipos.length;
      int[][] pMatriz = patron.matriz;
      int thisIndexA,thisIndexB; //indices en el patron 'this' de los tipos A y B en 'patron'

      // Asumiendo que patron incluye todos los 'tipos' menos el primero
      for(i=0; i<pSize; i++){

         thisIndexA = Arrays.binarySearch(tipos, patron.tipos[i]);
         for(j=i+1; j<pSize; j++){
            thisIndexB = Arrays.binarySearch(tipos, patron.tipos[j]);

            // Todavía no existía esta restricción
            if(matriz[thisIndexA][thisIndexB] == NEGATIVE_INFINITY){
               matriz[thisIndexA][thisIndexB] = pMatriz[i][j];
               matriz[thisIndexB][thisIndexA] = pMatriz[j][i];
               continue;
            }

            // Las restricciones comunes deben tener interseccion no nula
            if(pMatriz[i][j] < (-1)*matriz[thisIndexB][thisIndexA] ||
                  (-1)*pMatriz[j][i] > matriz[thisIndexA][thisIndexB]){
               return false;
            }
            // Hay intersección, quedarse con la intersección de cada restricción
            matriz[thisIndexA][thisIndexB] =
               Math.min(matriz[thisIndexA][thisIndexB],pMatriz[i][j]);
            matriz[thisIndexB][thisIndexA] =
               Math.min(matriz[thisIndexB][thisIndexA],pMatriz[j][i]); // Son números multiplicados por -1
         }
      }

      return true;
   }

   // Precond: patron es un posible subpatron de this
   /**
    * Combina dos patrones, el patron this y patron. Hace la interseccion de restricciones en
    * caso de haber restricciones compatibles.
    * Devuelve true si se ha podido realizar la combinación o no en otro caso.
    * @param patron - patron con el que se quiere combinar 'this'
    * @param indiceAusente - indice del tipo de evento ausente en la combinación (el tipo de evento que 'this' no tenía)
    * @return si se ha podido realizar la combinación o no
    */
   public boolean combinar(Patron patron, int indiceAusente){
      if(patron == null){
         return false;
      }

      int i,j;
      final int tSize = tipos.length; // Se comparan todos los tipos menos primero y ultimo
      int[][] pMatriz = patron.matriz;

      int dI=0,dJ=0;

      for(i=0;i<tSize;i++){
         if(i==indiceAusente) {
            dI=1;
            dJ=1;
            continue;
         }
         for(j=i+1; j<tSize; j++){
            if(j == indiceAusente) {
               dJ=1;
               continue;
            }
            if(j > indiceAusente) {
               dJ=1;
            }
            // Nueva restriccion?
            if(matriz[i][j]== NEGATIVE_INFINITY){
               matriz[i][j] = pMatriz[i-dI][j-dJ];
               matriz[j][i] = pMatriz[j-dJ][i-dI];
            }else{
               // Restriccion compatible?
               if(pMatriz[i-dI][j-dJ] < (-1)*matriz[j][i]
                     || (-1)*pMatriz[j-dJ][i-dI] > matriz[i][j]){
                  return false;
               }
               // Hay intersección, quedarse con la intersección de cada restricción
               matriz[i][j] = Math.min(matriz[i][j],pMatriz[i-dI][j-dJ]);
               matriz[j][i] = Math.min(matriz[j][i],pMatriz[j-dJ][i-dI]); // Son números multiplicados por -1
            }
         }
         dJ=0;
      }
      return true;
   }

   public List<Ocurrencia> getOcurrencias(){
      return ocurrencias;
   }

   public void deteleOcurrencias(int noOccurrences){
      ocurrencias.subList(ocurrencias.size() - noOccurrences, ocurrencias.size()).clear();
   }

   protected void addOcurrencia(int sid, int[] ocurrencia){
      Ocurrencia nueva = new Ocurrencia(sid,ocurrencia);
      ocurrencias.add(nueva);
   }

   //protected int getID(){
   public int getID(){
      return patternID;
   }

   public Patron clonar() {
      return new Patron(this);
   }


   public void agregar(Patron patron){
      //if(savePatternInstances){
      if(patron.ocurrencias != null){
         if(ocurrencias == null){
            ocurrencias = new ArrayList<Ocurrencia>();
         }
         ocurrencias.addAll(patron.ocurrencias);
      }
   }

   @Override
   public int compare(Patron p1, Patron p2) {
      return Integer.compare(p1.patternID, p2.patternID);
   }

   @Override
   public int compareTo(Patron p) {
      return Integer.compare(this.patternID, p.patternID);
   }

   //TODO contabilizado instancias
   /*@Override
   protected void finalize() throws Throwable {
      noOfInstances--;
      super.finalize();
   }*/
}
