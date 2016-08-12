package source.busqueda.jerarquia;

import source.busqueda.AbstractMine;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoAntepasados;

/**
 * Cuando los nodos siguientes ya se han generado se pueden utilizar para la combinación
 * @author vanesa.graino
 *
 */
public class GeneradorPatronesArbolSiguientes extends GeneradorPatronesArbol {

   public GeneradorPatronesArbolSiguientes(int tam, AbstractMine mine) {
      super(tam, mine);
   }

   public boolean setPadres(NodoAntepasados nodoCandidato){
      int index =1;
      if(nodoCandidato.getPadresAdoptivos().size()<tam-1){
         return false;
      }
      for(Nodo madre : nodoCandidato.getPadresAdoptivos()){
         setPadre(madre.getModelo(), index++);
      }
      return true;
   }

}
