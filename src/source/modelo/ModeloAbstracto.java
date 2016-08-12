package source.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.excepciones.FactoryInstantiationException;
import source.patron.GeneradorID;
//import source.modelo.clustering.IClustering;
import source.patron.Patron;
//import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

/**
 * Encapsula los atributos y métodos que tienen en común Modelo y ModeloConcurrente.
 * @author vanesa.graino
 *
 */
public abstract class ModeloAbstracto extends ComparadorAsociaciones implements IAsociacionTemporal{

   protected final String[] tipos;
   protected List<Patron> patrones;
   protected int ventana;
   private int frecuencia;
   /**
    * Frecuencia de cada patrón de la asociación temporal (importante, hay subclases
    * que delegan en los patrones este valor).
    */
   private int[] patFrec;
   //protected int[] rep; // Numero de tipos iguales al indice actual
   //protected int numTipos; // Número de tipos de eventos sin contar repetidos
   //protected boolean savePatternInstances;

   protected ModeloAbstracto(String[] tipos, int ventana, List<Patron> patrones,
         /*boolean savePatternInstances,*/ /*IClustering clustering, */Integer frecuencia ){
      this.tipos = tipos;
      this.ventana = ventana;
      //this.savePatternInstances = savePatternInstances;
      this.frecuencia = frecuencia==null? 0 : frecuencia;

      //int tSize = tipos.length;

      //int rSize = tSize; //tamaño sin contar repetidos

      //Se rellena el valor de rep y se calcula el número de tipos de eventos sin contar repetidos
//      int dSize = 0; //variable auxiliar
//      rep = new int[tSize];
//      for (int i=0; i<tSize-1; i++) {
//         if (tipos[i]==tipos[i+1]) {
//            dSize++;
//         } else {
//            for (int j=0;j<=dSize;j++){
//               rep[i-j]=dSize;
//            }
//            rSize -= dSize;
//            dSize = 0;
//         }
//      }
//      for(int j=0;j<=dSize;j++) { //por si el último tipo está repetido
//         rep[tSize-1-j] = dSize;
//      }
//      System.out.println("Rep: " + Arrays.toString(rep));
      //this.numTipos = rSize;

      if(patrones==null){
         this.patrones = new ArrayList<Patron>();
      }else{
         this.patrones = patrones;
         this.patFrec = new int[patrones.size()];
      }
   }

   /*protected ModeloAbstracto(List<String> tipos, int ventana, List<Patron> patrones,
         boolean savePatternInstances, Integer frecuencia ){
      this(tipos.toArray(new String[tipos.size()]), ventana, patrones, savePatternInstances, frecuencia);
   }*/

   protected ModeloAbstracto(String[] tipos, int ventana, /*boolean savePatternInstances, */Integer frecuencia/*, IClustering clustering*/){
      this(tipos, ventana, null, /*savePatternInstances, */frecuencia);
   }


   @Override
   public String toString(){
      StringBuilder aux = new StringBuilder("Modelo: " + Arrays.toString(tipos) + " - Numero de patrones: " + patrones.size() + "\n");
      for(int i=0;i<patrones.size();i++){
         aux.append( " Fr: "
               + patFrec[i] + " - "
               + patrones.get(i)+"\n");
      }
      return aux.toString();
   }

   // Peligroso, un cambio en lo devuelto afecta aquí
   /*public List<String> getTipos(){
      return tipos;
   }*/


   public String[] getTipos(){
      return tipos;
   }

   public void addPatron(Patron patron){
      for(Patron patLocal : patrones){
         if(patLocal.equals(patron)){ return; }
      }
      patrones.add(patron);
   }

   protected void setPatrones(List<Patron> patrones, int[] patFrec){
      this.patrones = patrones;
      this.patFrec = patFrec;
   }

   public List<Patron> getPatrones(){
      return patrones;
   }

   public Patron getPatron(int index){
      return patrones.size()>index ?  patrones.get(index) : null;
   }

   public int getSoporte(){
      return frecuencia;
   }

   public int incrementarSoporte(){
      return ++frecuencia;
   }

   public void incrementarPatFrec(int indice){
      patFrec[indice]++;
   }

   /**
    *
    * @param frec
    * @param patOcs
    */
   protected void addFrecuencias(int frec, int[] patOcs){
      frecuencia+=frec;
      if(patOcs!=null) {
         for(int i=0, pSize = patrones.size();i<pSize;i++){
            patFrec[i]+=patOcs[i];
         }
      }
   }

   public int getVentana(){
      return ventana;
   }

   //TODO añadido para pruebas
   public int getPatFrec(int pIndex){
      return patFrec[pIndex];
   }

   protected int[] getPatFrec(){
      return patFrec;
   }

   public void setPatFrec(int[] patFrec) {
      this.patFrec = patFrec;
   }

   protected void resetPatrones(){
      patrones = new ArrayList<Patron>();
      patFrec = null;
   }

//   protected int[] getRep(){
//      return rep;
//   }

//   protected int getNumTipos(){
//      return numTipos;
//   }

   /*public Boolean getSavePatternInstances() {
      return savePatternInstances;
   }

   public void setSavePatternInstances(Boolean savePatternInstances) {
      this.savePatternInstances = savePatternInstances;
   }*/

   public List<RIntervalo> getRestricciones(String desde,String hacia){
      int i,j,k;
      List<RIntervalo> ret = new ArrayList<RIntervalo>(),aux;
      boolean incluye;
      RIntervalo r;

      for(i=0;i<patrones.size();i++){
         aux = patrones.get(i).getRestricciones(desde,hacia);
         for(j=0;j<aux.size();j++){
            r = aux.get(j);
            incluye=false;
            for(k=0;k<ret.size();k++){
               if((r.equals(ret.get(k)))||(ret.get(k).incluye(r))){
                  break;
               }
               if(r.incluye(ret.get(k))){
                  ret.remove(k);
                  if(incluye){
                     k--;
                  }else{
                     ret.add(k, r);
                     incluye=true;
                  }
               }
            }
            if((k==ret.size())&&(!incluye)){
               ret.add(aux.get(j));
            }
         }
      }
      return ret;
   }

   public List<RIntervalo> getRestricciones(String tipo){
      int i,j,k;
      List<RIntervalo> ret = new ArrayList<RIntervalo>(),aux;
      RIntervalo r;
      boolean incluye=false;

      for(i=0;i<patrones.size();i++){
         aux = patrones.get(i).getRestricciones(tipo);
         for(j=0;j<aux.size();j++){
            r = aux.get(j);
            incluye=false;
            if(r.getTipoA()==tipo || r.getTipoB()==tipo) {
               for(k=0;k<ret.size();k++){
                  // Quedarse con la más general
                  if((r.equals(ret.get(k)))||(ret.get(k).incluye(r))){
                     break;
                  }
                  if(r.incluye(ret.get(k))){
                     ret.remove(k);
                     if(incluye){
                        k--;
                     }else{
                        ret.add(k, r);
                        incluye=true;
                     }
                  }
               }
               if((k==ret.size())&&(!incluye)){
                  ret.add(aux.get(j));
               }
            }
         }
      }

      return ret;
   }

   public List<RIntervalo> getRestricciones(boolean masGenerales){
      int k;
      List<RIntervalo> retorno = new ArrayList<RIntervalo>();
      boolean incluye=false;

      for(Patron p: patrones){
         for(RIntervalo r:p.getRestricciones()){
            if(masGenerales){
               incluye=false;
               for(k=0;k<retorno.size();k++){
                  // Quedarse con la más general
                  if((r.equals(retorno.get(k)))||(retorno.get(k).incluye(r))){
                     break;
                  }
                  if(r.incluye(retorno.get(k))){
                     retorno.remove(k);
                     if(incluye){
                        k--;
                     }else{
                        retorno.add(k, r);
                        incluye=true;
                     }
                  }
               }
               if((k==retorno.size())&&(!incluye)){ retorno.add(r); }
            }else{
               retorno.add(r);
            }

         }
      }
      return retorno;
   }

   /*
    * Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
    * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
    */
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      int tSize = tipos.length;
      indices[tSize-1]++;
      int i;
      for(i=tSize-1;i>0;i--){
         if(i==index){
            if(indices[i]!=0){
               indices[i]=0;
               indices[i-1]++;
            }
         }else{
            if(indices[i] >= tam[i] ){
               indices[i-1]++;
               indices[i]=0;
            }else{
               break; // No hay que propagar más cambios, combinación de eventos válida.
            }
         }
      }
      if((index==0 && indices[0]>0) || indices[0]>=tam[0]){
         return null;
      }
      return indices;
   }

   /*protected boolean siguienteInstancia(int[] tam, int[][] abiertas, int[][] limites, int[] indices,
         int[] instancia, int index, String tipo, int tSize){
      indices = siguienteCombinacion(tam, indices, index, tipo);
      if(indices != null){
         for(int i=0;i<tSize;i++){
            if(i!=index){
               instancia[i]=abiertas[i][(limites[i][0]+indices[i])%ventana];
            }
         }
         return true;
      }
      return false;
   }*/


   //Para repetición
   /*protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      int tSize = tipos.length;
      indices[tSize-1]++;
      int resta=0;//rep[tSize-1];
      int i,j;
      for(i=tSize-1;i>0;i--){
         String tipoI = tipos[i];
         String tipoAnt = tipos[i-1];
         int mod;
         if(i==index){
            if(indices[i]!=0){
               indices[i]=0;
               indices[i-1]++;
               // Este indice no se cambia, es el nuevo evento
               // Los indices del mismo tipo se ponen a 0,1,2...
               //Si i fuese 0 entonces no se entraria aqui -> i no puede ser cero! @vanesa
               for(j=1;j<=0;j++){//for(j=1;j<=rep[i];j++){
                  indices[i+j]=j-1;
               }
            }
            resta=0;//rep[i-1];
            continue; // no puede ser i==index y además estar en un tipo repetido sin ser el i mas bajo
         }
         mod = tipoI==tipo? 1 : 0;
         //if(indices[i]>=(tam[i]-(rep[i]-resta+mod))){
         if(indices[i]>=(tam[i]-(0-resta+mod))){
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
            //resta=rep[i-1];
            resta=0;
         }
      }
      if(index==0){
         if(indices[0]>0){ return null; }
      }else{
         //if(indices[0]>=(tam[0]-rep[0])){ return null; }
         if(indices[0]>=(tam[0]-0)){ return null; }
      }

      return indices;
   }*/

   /**
    *
    * @param supmin
    * @param patternClassName
    * @throws FactoryInstantiationException
    */
   //@Override
   public int calculaPatrones(int supmin, String patternClassName, GeneradorID genID,
         boolean savePatternInstances) throws FactoryInstantiationException{
      // Se comprueba cuales de los posibles patrones se dan realmente
      int i=0,j=0,borrados=0;
      int pSize = patrones.size();
      for(i=0;i<patrones.size();i++){
         if(patFrec[i+j]<supmin){
            patFrec[i+j]=0;
            patrones.remove(i);
            borrados++;
            i--;j++;
         }
      }
      if(pSize!=patrones.size()){
         if(patrones.isEmpty()) {
            frecuencia=0;
            return borrados;
         }
         int[] frecaux = new int[patrones.size()];
         j=0;
         for(i=0;i<pSize;i++){
            if(patFrec[i]!=0){
               frecaux[j]=patFrec[i];
               j++;
            }
         }
         patFrec = frecaux;
      }
      return borrados;
   } // calculaPatrones


   @Override
   public boolean necesitaPurga(int minFreq) {
      return frecuencia<minFreq || patrones.isEmpty();
   }

   @Override
   public int size(){
      return tipos.length;
   }

   @Override
   public String getUltimoTipo(){
      return tipos[tipos.length-1];
   }

   @Override
   public int compareTo(IAsociacionTemporal o) {
      return compare(this, o);
   }
}
