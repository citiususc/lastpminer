package source.modelo.arbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import source.modelo.negacion.IAsociacionConNegacion;

/**
 * Permite la gestión de eventos negativos en la árbol de forma que sólo de cambian los métodos
 * de acceso pero no la representación del árbol (sigue utilizándose Nodo). Para ello utiliza
 * un sufijo para los modelos negativos {@code IAsociacionConNegacion.SUF_NEG}.
 * @author vanesa.graino
 *
 */
public class SupernodoNegacionSufijo extends SupernodoNegacion {

   public SupernodoNegacionSufijo() {
   }

   public SupernodoNegacionSufijo(Nodo padre) {
      super(padre);
   }

   public Nodo obtenerNodoEnArbol(List<String> tiposPositivos, List<String> tiposNegativos){
      Nodo resultado = null;
      Supernodo actual = this;
      for(String tipo : tiposPositivos){
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      for(String tipoNeg : tiposNegativos){
         String tipo = tipoNeg + IAsociacionConNegacion.SUF_NEG;
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      return resultado;
   }

   public Nodo obtenerNodoEnArbol(String[] tiposPositivos, String[] tiposNegativos){
      Nodo resultado = null;
      Supernodo actual = this;
      for(String tipo : tiposPositivos){
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      for(String tipoNeg : tiposNegativos){
         String tipo = tipoNeg + IAsociacionConNegacion.SUF_NEG;
         if(actual==null){ return null; }
         resultado = actual.getHijo(tipo);
         if(resultado==null){ return null; }
         actual = resultado.getHijos();
      }
      return resultado;
   }

   public Nodo eliminarNodoEnArbol(String[] tiposPositivos, String[] tiposNegativos){
      List<String> tiposAuxPositivos, tiposAuxNegativos;
      String ultimoTipo;
      boolean esPositivo = tiposNegativos.length == 0;
      if(esPositivo){
         tiposAuxPositivos = Arrays.asList(tiposPositivos).subList(0, tiposPositivos.length-1);
         ultimoTipo = tiposPositivos[tiposPositivos.length - 1];;
         tiposAuxNegativos = Collections.emptyList();
      }else{
         tiposAuxPositivos = Arrays.asList(tiposPositivos);
         tiposAuxNegativos = Arrays.asList(tiposNegativos).subList(0, tiposNegativos.length-1);
         ultimoTipo = tiposNegativos[tiposNegativos.length - 1];
      }
      Nodo actual = obtenerNodoEnArbol(tiposAuxPositivos, tiposAuxNegativos);

      if(actual!=null && actual.getHijos()!=null){
         return ((SupernodoNegacionSufijo)actual.getHijos()).removeNodo(ultimoTipo,  esPositivo);
      }

      return null;
   }

   /*public Nodo eliminarNodoEnArbol(String[] tiposPositivos, String[] tiposNegativos){
      String[] tiposAuxPositivos, tiposAuxNegativos;
      String ultimoTipo;
      boolean esPositivo = tiposNegativos.length == 0;
      if(esPositivo){
         tiposAuxPositivos = Arrays.copyOf(tiposPositivos, tiposPositivos.length-1);
         ultimoTipo = tiposPositivos[tiposPositivos.length - 1];;
         tiposAuxNegativos = new String[0];
      }else{
         tiposAuxPositivos = tiposPositivos.clone();
         tiposAuxNegativos = Arrays.copyOf(tiposNegativos, tiposNegativos.length-1);
         ultimoTipo = tiposNegativos[tiposNegativos.length - 1];
      }
      Nodo actual = obtenerNodoEnArbol(tiposAuxPositivos, tiposAuxNegativos);

      if(actual!=null && actual.getHijos()!=null){
         return ((SupernodoNegacion)actual.getHijos()).removeNodo(ultimoTipo,  esPositivo);
      }

      return null;
   }*/

   protected Nodo removeNodo(String tipo, boolean esPositivo){
      Nodo nodo = nodos.remove(esPositivo? tipo : tipo + IAsociacionConNegacion.SUF_NEG);
      lista.remove(nodo);
      return nodo;
   }

   public Nodo getHijo(String tipo, boolean esPositivo){
      return nodos.get(esPositivo? tipo : tipo + IAsociacionConNegacion.SUF_NEG);
   }


   public void addNodo(Nodo nodo, String tipo, boolean esPositivo){
      nodos.put(esPositivo? tipo : tipo + IAsociacionConNegacion.SUF_NEG,nodo);
      lista.add(nodo);
   }

   @Override
   public String toString(){
      StringBuilder aux = new StringBuilder("Supernodo: [ ");
      if(!lista.isEmpty()){
         for(Nodo nodo : lista){
            aux.append(nodo.getModelo().toStringSinPatrones()).append(", ");
         }
         aux.deleteCharAt(aux.length()-1).deleteCharAt(aux.length()-1);
      }
      aux.append("]\n");
      return aux.toString();
   }

}
