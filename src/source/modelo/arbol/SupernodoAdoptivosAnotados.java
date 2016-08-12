package source.modelo.arbol;

import java.util.List;

import source.modelo.IAsociacionConEpisodios;

/**
 * Supernodo con nodos adoptivos y un contador para
 * @author vanesa.graino
 *
 */
public class SupernodoAdoptivosAnotados extends SupernodoAdoptivos{

   //TODO que es mejor un contador o una lista de los nodos?
   protected transient int contador;

   protected SupernodoAdoptivosAnotados(){
      super();
   }

   protected SupernodoAdoptivosAnotados(Nodo padre){
      super(padre);
   }

   public int getContador(){
      return contador;
   }

   /**
    * Guarda en una lista parámetro los nodos marcados del supernodo actualizando su atributo contador..
    * @param tmp - instante del evento actual
    * @param ventana - tamaño de la ventana
    * @param marcados - lista donde se almacenarán los nodos marcados.
    * @return - True si hay por lo menos dos nodos marcados, false en otro caso.
    */
   public boolean nodosMarcados(int tmp, int ventana, List<NodoAntepasadosAnotado> marcados){
      marcados.clear();
      for(int i=0; contador>1 && i<lista.size(); i++){
         NodoAntepasadosAnotado nodo = (NodoAntepasadosAnotado)lista.get(i);
         if(((NodoAntepasadosAnotado)lista.get(i)).asegurarOcurrencia(tmp, ventana)){
            marcados.add(nodo);
         }
      }
      return contador>1;
   }

   /**
    * Guarda en una lista parámetro los nodos marcados del supernodo y los que se corresponden a
    * asociaciones temporales parciales, todo ello actualizando su atributo contador.
    * @param tmp - instante del evento actual
    * @param ventana - tamaño de la ventana
    * @param marcados - lista donde se almacenarán los nodos marcados.
    * @return - True si hay por lo menos dos nodos marcados, false en otro caso.
    */
   public boolean nodosMarcadosMasParciales(int tmp, int ventana, List<NodoAntepasadosAnotado> marcados){
      marcados.clear();
      for(int i=0; i<lista.size(); i++){
         NodoAntepasadosAnotado nodo = (NodoAntepasadosAnotado)lista.get(i);
         if(((IAsociacionConEpisodios)lista.get(i).getModelo()).sonEpisodiosCompletos()){
            marcados.add(nodo);
         }else if(((NodoAntepasadosAnotado)lista.get(i)).asegurarOcurrencia(tmp, ventana)){
            marcados.add(nodo);
         }
      }
      return contador>0;
   }

   /**
    *
    */
   public void resetAnotaciones(){
      contador = 0;
      for(Nodo n : lista){
         ((NodoAntepasadosAnotado)n).resetAnotacion();
      }
   }

}
