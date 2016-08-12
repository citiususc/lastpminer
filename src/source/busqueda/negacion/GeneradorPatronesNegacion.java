package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.busqueda.AbstractMine;
import source.busqueda.jerarquia.GeneradorPatronesArbol;
import source.excepciones.FactoryInstantiationException;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.arbol.SupernodoNegacion;
import source.modelo.negacion.IAsociacionConNegacion;
import source.patron.Patron;
import source.patron.PatternFactory;

/**
 * Generador de patrones cuando hay negación. En este caso los patrones
 * pueden ser de tamaños distintos.
 * @author vanesa.graino
 *
 */
public class GeneradorPatronesNegacion extends GeneradorPatronesArbol {

   //protected int patronesPadre = 0;
   /**
    *
    */
   protected int padresConPatrones = 0;

   public GeneradorPatronesNegacion(int tam, AbstractMine mine) {
      super(tam, mine);
   }


   /**
    *
    */
   @Deprecated
   public String[] getModArray(){
      return super.getModArray();
   }

   @Override
   public String getTipoNuevo(){
      IAsociacionConNegacion asoc = (IAsociacionConNegacion)asocBase[1];
      if(asoc.parteNegativa()){
         return asoc.getTiposNegados()[asoc.getTiposNegados().length-1];
      }
      return asoc.getTipos()[tam-2];
   }

   public boolean tipoNuevoPositivo(){
      IAsociacionConNegacion asoc = (IAsociacionConNegacion)asocBase[1];
      return !asoc.parteNegativa();
   }

   /**
    * En su lugar utilizar la función que incluye la lista de eventos
    * positivos y la lista de eventos negados de la asociación, es decir,
    * {@link #comprobarSubasociaciones(SupernodoNegacion, String[], String[])}
    */
   @Deprecated
   public boolean comprobarSubasociaciones(Supernodo raizArbol, String[] mod){
      return super.comprobarSubasociaciones(raizArbol, mod);
   }



   /**
    * Comprueba que existen las subasociaciones de la asociación formada
    * por positivos y negados.
    * @param raizArbol
    * @param positivos
    * @param negados
    * @return
    */
   public boolean comprobarSubasociacionesPrefijo(SupernodoNegacion raizArbol,
         String[] positivos, String[] negados){
      int index=2;
      //List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      List<String> modAux = new ArrayList<String>();
      modAux.addAll(Arrays.asList(positivos));
      for(String tipo : negados){
         modAux.add(IAsociacionConNegacion.PREF_NEG + tipo);
      }
      String tipo;
      boolean valido=true;
      // Se contabiliza en los primeros padres porque se van a combinar y después en los restantes
      padresConPatrones = (getPatCount()[0]>0? 1 : 0) + (getPatCount()[1]>0? 1 : 0);

      //TODO cambio para que funcione sin modelos completamente negados en el árbol
      for(int min = positivos.length == 1 ? 1 : 0, k = tam-3; k>=min; k--){
      //for(int k = tam-3; k>=0; k--){

         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         int numPatrones = aux.getModelo().getPatrones().size();
         getPatCount()[index] = numPatrones;
         padresConPatrones += numPatrones>0? 1 : 0;
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }

   private List<String> listaTiposConSufijo(String[] positivos, String[] negados){
      new ArrayList<String>();
      List<String> modAux =new ArrayList<String>();
      modAux.addAll(Arrays.asList(positivos));
      for(String tipo : negados){
         modAux.add(tipo + IAsociacionConNegacion.SUF_NEG);
      }
      Collections.sort(modAux);
      return modAux;
   }

   public boolean comprobarSubasociacionesSufijo(SupernodoNegacion raizArbol,
         String[] positivos, String[] negados){
      int index=2;
      //List<String> modAux = new ArrayList<String>(Arrays.asList(mod));
      List<String> modAux = listaTiposConSufijo(positivos, negados);/*new ArrayList<String>();
      modAux.addAll(Arrays.asList(positivos));
      for(String tipo : negados){
         modAux.add(IAsociacionConNegacion.PREF_NEG + tipo);
      }*/
      String tipo;
      boolean valido=true;
      // Se contabiliza en los primeros padres porque se van a combinar y después en los restantes
      padresConPatrones = (getPatCount()[0]>0? 1 : 0) + (getPatCount()[1]>0? 1 : 0);

      for(int k = tam-3; k>=0; k--){
         tipo = modAux.remove(k);
         //Nodo aux = mine.getRaizArbol().obtenerNodoEnArbol(modAux);
         Nodo aux = raizArbol.obtenerNodoEnArbol(modAux);
         if(aux==null){
            valido=false;
            break;
         }
         asocBase[index] = aux.getModelo();
         int numPatrones = aux.getModelo().getPatrones().size();
         getPatCount()[index] = numPatrones;
         padresConPatrones += numPatrones>0? 1 : 0;
         index++;
         modAux.add(k, tipo);
      }
      return valido;
   }

   @Deprecated
   public boolean comprobarSubasociaciones(NodoAntepasados nodo, SupernodoAdoptivos raizArbol,
         List<String> mod){
      return super.comprobarSubasociaciones(nodo, raizArbol, mod);
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.GeneradorPatrones#generarPatron(int, int, java.lang.String[], java.util.List)
    */
   /*@Override
   protected int generarPatron(int uValido, String[] mod,
         List<Patron> patrones) throws FactoryInstantiationException{

      if(padresConPatrones == 1){ //sólo hay un padre con patrón o patrones y tiene que ser el primero
         patCache[0] = PatternFactory.getInstance().getPatternExtension(mine.getPatternClassName(),mod,
               asocBase[0].getPatron(patIndex[0]), mine.getNumHilos());
         mine.notificarPatronGenerado(tam, getPatCount(), patIndex, patCache, patrones, 0);
         return 1;
      }
      return super.generarPatron(uValido, mod, patrones);

   }*/

   @Override
   protected int generarPatron(int uValido, String[] mod,
         List<Patron> patrones) throws FactoryInstantiationException{

      if(padresConPatrones == 1){ //sólo hay un padre con patrón o patrones y tiene que ser el primero
         patCache[0] = PatternFactory.getInstance().getPatternExtension(mine.getPatternClassName(),mod,
               asocBase[0].getPatron(patIndex[0]), mine.getNumHilos());
         //mine.notificarPatronGenerado(tam, getPatCount(), patIndex, patCache, patrones, 0);
         notificarPatronGenerado(patrones, 0);
         return 1;
      }

      int uValidoOut = uValido;
      if(uValidoOut<=0){
         patCache[0] = PatternFactory.getInstance().getPatternExtension(mine.getPatternClassName(), mod,
               asocBase[0].getPatron(patIndex[0]), mine.getNumHilos());
         mine.getPatronesGeneradosConAuxiliaresNivel()[tam-1]++;
         uValidoOut = 1;
      }
      boolean act = false;
      int l = uValidoOut;
      //for(int l=uValidoOut;l<tam;l++){
      for(;l<tam && patCount[l-1]!=0 && patCount[l]!=0;l++){ // Cambiado para que funcione con negación @vanesa
         Patron patAux = PatternFactory.getInstance().getPatternClone(mine.getPatternClassName(),
               patCache[l-1], mine.getNumHilos());
         mine.getPatronesGeneradosConAuxiliaresNivel()[tam-1]++;
         if(patAux.combinar(asocBase[l].getPatron(patIndex[l]),tam-1-l)){
            patCache[l]=patAux;
         }else{
            //No valido: se descarta el patrón
            act = true;
            notificarPatronDescartado(l);
            uValidoOut = siguienteCombinacion(l);
            break;
         }
      }
      if(!act){
         //uValidoOut = mine.notificarPatronGenerado(tam, getPatCount(), patIndex, patCache, patrones, tam-1);
         //uValidoOut = mine.notificarPatronGenerado(tam, getPatCount(), patIndex, patCache, patrones, l-1);// Cambiado para que funcione con negación @vanesa
         uValidoOut = notificarPatronGenerado(patrones, l-1);// Cambiado para que funcione con negación @vanesa
      }
      return uValidoOut;
   }

}
