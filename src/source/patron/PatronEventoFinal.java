package source.patron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.restriccion.RIntervalo;

/*
 *   Extensión de 'Patron' en el que se calcula qué evento o eventos pueden ocurrir
 * como final de un patrón. Saber esto permite descartar, durante el cálculo de
 * frecuencia, aquellos patrones que no pueden terminar con el evento leído.
 *
 *   Esta clase está pensada para trabajar con la implementación de IAsociacionTemporal
 * 'ModeloEventoFinal'.
 */
public class PatronEventoFinal extends Patron{

   /*
    * Atributos específicos.
    */

   protected List<String> tiposFinales;
   private int frecuencia;

   /*
    * Constructores heredados.
    */

   public PatronEventoFinal(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      super(tipos, restricciones, savePatternInstances);
      frecuencia = 0;
   }

   protected PatronEventoFinal(String[] tipos, boolean savePatternInstances){
      super(tipos, savePatternInstances);
      frecuencia = 0;
   }

   public PatronEventoFinal(Patron patron){
      super(patron);
      if(patron instanceof PatronEventoFinal){
         PatronEventoFinal pef = (PatronEventoFinal)patron;
         if(pef.tiposFinales != null){
            tiposFinales = new ArrayList<String>(pef.tiposFinales);
         }
         frecuencia = pef.frecuencia;
      }else{
         frecuencia=0;
      }
   }

   public PatronEventoFinal(String[] tipos, Patron patron){
      super(tipos, patron);
      frecuencia=0;
   }


   /*
    * Métodos específicos.
    */


   // Mejor comprobar la consistencia por cada restriccion añadida?
   public boolean esConsistente(GeneradorID genId){
      boolean result = super.esConsistente(genId);
      if(result){
         calculaTiposFinales();
      }
      return result;
   }

   protected void calculaTiposFinales(){
      tiposFinales = new ArrayList<String>(Arrays.asList(getTipos()));
      int num = getTipos().length;
      String[] tipos = getTipos();
      int[][] matriz = getMatriz();
      for(int i=0;i<num;i++){
         for(int j=i+1;j<num;j++){
            if(-matriz[j][i]>0){ tiposFinales.remove(tipos[i]); }//min>0
            if(matriz[i][j]<0){ tiposFinales.remove(tipos[j]); }//max<0
         }
      }
   }

   /**
    * Devuelve true si el tipo de evento {@code tipo} es un posible
    * tipo final de este patrón.
    * @param tipo
    * @return
    */
   public boolean esTipoFinal(String tipo){
      List<String> finales = getTiposFinales();
      int index = Collections.binarySearch(finales, tipo);
      return index>=0;
      //return finales.contains(tipo);
   }

   public List<String> getTiposFinales(){
      //if(tiposFinales == null || tiposFinales.isEmpty()){
      if(tiposFinales == null){
         calculaTiposFinales();
      }
      return tiposFinales;
   }

   /**
    * Obtiene la frecuencia del patrón
    * @return
    */
   public int getFrecuencia(){
      return frecuencia;
   }

   /**
    * Este método debe ser llamado después de una respuesta afirmativa de 'representa',
    * cuando se quiera que dicha ocurrencia se tenga en cuenta de cara a la frecuencia.
    */
   public void encontrado(){
      frecuencia++;
   }

   public void agregar(Patron patron){
      super.agregar(patron);
      if(patron instanceof PatronEventoFinal){
         frecuencia += ((PatronEventoFinal)patron).frecuencia;
      }
   }

}
