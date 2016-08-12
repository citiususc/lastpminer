package source.busqueda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;
import source.patron.PatternFactory;

/**
 * Encapsula la lógica de generación de patrones que es común a diferentes
 * algoritmos.
 *
 * @author vanesa.graino
 *
 */
public class GeneradorPatrones {

   //private static final Logger LOGGER = Logger.getLogger(GeneradorPatrones.class.getName());

   protected Patron cache;
   protected int[] patIndex;
   protected int[] patCount;
   //protected long totalPatrones;
   protected IAsociacionTemporal[] asocBase;
   protected Patron[] patCache;
   protected int tam;
   protected AbstractMine mine;

   public GeneradorPatrones(int tam, AbstractMine mine) {
      this.tam = tam;
      this.mine = mine;
      patCount = new int[tam];
      patIndex = new int[tam];
      patCache = new Patron[tam];
      asocBase = new IAsociacionTemporal[tam];
   }

   public List<Patron> generarPatrones(String[] mod) throws FactoryInstantiationException{
      List<Patron> patrones = new ArrayList<Patron>();

      int uValido=-1;
      long sumaux=1;
      for(int l=0;l<tam;l++){
         patCache[l] = null;
         patIndex[l] = 0;
         //Cuando hay negación, puede haber modelos sin patrones
         if(patCount[l]>0){
            sumaux *= patCount[l];
         }
      }
      //totalPatrones = sumaux;
      mine.patronesPosiblesNivel[tam-1]+=sumaux;

      mine.registroT.tiempoFundir(tam-1, true);
      while(patIndex[0]<patCount[0]){
         uValido = generarPatron(uValido, mod, patrones);
      }
      mine.registroT.tiempoFundir(tam-1, true);
      return patrones;
   }

   protected int generarPatron(int uValido, String[] mod,
         List<Patron> patrones) throws FactoryInstantiationException{
      int uValidoOut = uValido;
      if(uValidoOut<=0){
         patCache[0] = PatternFactory.getInstance().getPatternExtension(mine.patternClassName, mod,
               asocBase[0].getPatron(patIndex[0]), mine.numHilos);
         mine.patronesGeneradosConAuxiliaresNivel[tam-1]++;
         uValidoOut = 1;
      }
      int l = uValidoOut;
      //for(int l=uValidoOut;l<tam;l++){
      //for(;l<tam && patCount[l-1]!=0 && patCount[l]!=0;l++){ // Cambiado para que funcione con negación @vanesa
      for(;l<tam && patCount[l-1]!=0;l++){ // Cambiado para que funcione de nuevo @vanesa
         Patron patAux = PatternFactory.getInstance().getPatternClone(mine.patternClassName,
               patCache[l-1], mine.numHilos);
         mine.patronesGeneradosConAuxiliaresNivel[tam-1]++;
         if(patAux.combinar(asocBase[l].getPatron(patIndex[l]),tam-1-l)){
            patCache[l] = patAux;
         }else{
            //No válido: se descarta el patrón
            notificarPatronDescartado(l);
            return siguienteCombinacion(l);
         }
      }
      uValidoOut = siguienteCombinacion();
      mine.notificarPatronGenerado(tam, patrones, patCache[l-1]);
      return uValidoOut;
   }

   protected int notificarPatronGenerado(List<Patron> patrones, int indicePatron){
      int l = siguienteCombinacion();
      Patron patron = patCache[indicePatron];
      mine.notificarPatronGenerado(tam, patrones, patron);
      return l;
   }

   protected void notificarPatronDescartado(int currentIndex){
      mine.patronesDescartadosNivel[tam-1]++;
      if(currentIndex!=tam-1){ // si el índice no era del último modelo que se va a combinar
         int imposibles=1;
         for(int i=currentIndex+1;i<tam;i++){
            if(patCount[i]!=0){
               imposibles *= patCount[i]; // patIndex[o]==0
            }
         }
         mine.patronesNoGeneradosNivel[tam-1]+=imposibles-1;
      }
   }

   protected int siguienteCombinacion(int currentIndex){
      //Avanzar al siguiente posible patrón
      int o;
      patIndex[currentIndex]++;
      for(o=currentIndex;o>=1;o--){
         if(patIndex[o]<patCount[o]){
            break;
         }else{
            patIndex[o]=0;
            patIndex[o-1]++;
         }
      }
      return o;
   }
   protected int siguienteCombinacion(){
      return siguienteCombinacion(tam-1);
   }

   public void setPadre(IAsociacionTemporal padre, int index){
      asocBase[index] = padre;
      patCount[index] = padre.getPatrones().size();
   }

   /*
    * Disjunción entre los tipos de eventos del padre y la madre
    */
   public List<String> disjointPadreMadre(){
      List<String> aux0 = new ArrayList<String>(Arrays.asList(asocBase[0].getTipos()));
      List<String> aux1 = new ArrayList<String>(Arrays.asList(asocBase[1].getTipos()));
      // tipos que están en asocBase[0] pero no en asocBase[1]
      aux0.removeAll(aux1);
      // tipos que están en asocBase[1] pero no en asocBase[0]
      aux1.removeAll(Arrays.asList(asocBase[0].getTipos()));
      aux0.addAll(aux1);
      return aux0;
   }

   /**
    * Comprueba si una nueva asociación temporal es posibles para lo que
    * comprueba que todas las subasociaciones de la misma son frecuentes.
    * En caso de ser posible, rellena las variables que permitirán crear
    * sus patrones temporales.
    * Al llamar a esta función, en asocBase y patCount están fijados los dos
    * primeros elementos.
    * @param tam Tamaño de la asociación que estamos creando.
    * @param mod Array de tipos de eventos de la nueva asociación temporal.
    * @param mapa Mapa en el que la clave es un tipo de evento y el valor es
    * la lista de asociaciones temporales de tamaño (tam-1) que contienen ese
    * tipo de evento.
    * @return Si todas las subasociaciones son frecuentes (existen en
    * el mapa) devuelve true, false en otro caso. Además, si la nueva asociación
    * es viables, fija los elementos de los array {@code asocBase} y {@code patCount}.
    */
   public boolean comprobarSubasociaciones(int tam, List<String> mod,
         Map<String,List<IAsociacionTemporal>> mapa){
      boolean valido = true;
      List<String> modaux = new ArrayList<String>(mod);
      int index = 2,m;
      for(int l=tam-3;l>=0 && valido;l--){ // Eliminar tipos en orden inverso
         //List<String> modaux = new ArrayList<String>(mod);
         //modaux.remove(l);
         String eliminado = modaux.remove(l);
         List<IAsociacionTemporal> laux = mapa.get(modaux.get(0));
         // ¿Se podría acotar más? ¿Como comparando el primer tipo?
         // Hacer búsqueda binaria
         m = 0;
         for(IAsociacionTemporal modIt : laux){
            if(Arrays.asList(modIt.getTipos()).equals(modaux)){
               asocBase[index] = modIt;
               patCount[index] = modIt.getPatrones().size();
               if(patCount[index]<=0){
                  valido = false;
               }
               index++;
               break;
            } //if
            m++;
         } //m, busqueda subsecuencias lista actual
         modaux.add(l, eliminado);
         if(m>=laux.size()){
            // Si entra aquí ha revisado todas las asociaciones y ninguna
            // era la subasociación buscada
            valido=false;
         }
      } // l, busqueda subsecuencias
      return valido;
   }

   /*
    * Getters & setters
    */

   public final IAsociacionTemporal[] getAsociacionesBase(){
      return asocBase;
   }

   public final int[] getPatCount() {
      return patCount;
   }


}
