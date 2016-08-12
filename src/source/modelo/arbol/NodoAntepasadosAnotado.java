package source.modelo.arbol;

import source.modelo.IAsociacionArbol;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;

public class NodoAntepasadosAnotado extends NodoAntepasados{

   /*
    * Atributos
    */

   protected transient int tmp=-1;
   protected transient int inicioUltimaOcurrencia = -1;
   protected boolean util = false;

   /*
    * Constructores
    */

   public NodoAntepasadosAnotado(IAsociacionTemporal modelo){
      setModelo(modelo);
      this.hijos = new SupernodoAdoptivosAnotados(this);
      this.supernodo = null;
   }

   public NodoAntepasadosAnotado(IAsociacionTemporal modelo, Supernodo supernodo){
      this(modelo);
      this.supernodo = supernodo;
   }

   public NodoAntepasadosAnotado(IAsociacionTemporal modelo, Supernodo hijos, Supernodo supernodo){
      super(modelo, hijos, supernodo);
      setModelo(modelo);
   }

   /*
    * Métodos
    */

   /**
    * Se notifica al nodo que ha habido una nueva ocurrencia de
    * un patrón de la asociación temporal a la que representa.
    * @param inicioOcurrencia - el inicio de la nueva ocurrencia.
    */
   public void nuevaOcurrencia(int inicioOcurrencia, int tmp){
      this.tmp = tmp;
      if(inicioUltimaOcurrencia < inicioOcurrencia){
         if(inicioUltimaOcurrencia<0){
            ((SupernodoAdoptivosAnotados)this.supernodo).contador++;
         }
         inicioUltimaOcurrencia = inicioOcurrencia;
      }
   }

   /**
    * Comprueba si el nodo está marcado para el instante actual
    * teniendo en cuenta el tamaño de la ventana temporal.
    * @param tmp - instante del evento
    * @param ventana - tamaño de la ventana temporal
    * @return si la ocurrencia está en la ventana
    */
   public boolean asegurarOcurrencia(int tmp, int ventana){
      if(inicioUltimaOcurrencia<0){ return false; }
      if(this.tmp == tmp){
         return inicioUltimaOcurrencia>=0;
      }
      if(inicioUltimaOcurrencia < (tmp - ventana)){
         inicioUltimaOcurrencia = -1;
         ((SupernodoAdoptivosAnotados)this.supernodo).contador--;
         return false;
      }

      return true;
   }

   /**
    * Comprueba que todos los antepasados del nodo excepto los <skip> primeros
    * tienen una ocurrencia en la ventana.
    * @param tmp
    * @param ventana
    * @param skip
    * @return
    */
   public boolean asegurarAntepasados(int tmp, int ventana, int skip){

      //for(Nodo n : padresAdoptivos){
      for(int i=skip; i<padresAdoptivos.size();i++){
         NodoAntepasadosAnotado n = (NodoAntepasadosAnotado)padresAdoptivos.get(i);
         if(!n.asegurarOcurrencia(tmp, ventana)){
            return false;
         }
      }

      return true;
   }

   /**
    * Comprueba que todos los antepasados del nodo excepto los <skip> primeros
    * tienen una ocurrencia en la ventana o se corresponden a asociaciones
    * temporales parciales.
    * @param tmp
    * @param ventana
    * @param skip
    * @return
    */
   public boolean asegurarAntepasadosConParciales(int tmp, int ventana, int skip){

      //for(Nodo n : padresAdoptivos){
      for(int i=skip; i<padresAdoptivos.size();i++){
         NodoAntepasadosAnotado n = (NodoAntepasadosAnotado)padresAdoptivos.get(i);
         if(((IAsociacionConEpisodios)n.getModelo()).sonEpisodiosCompletos() && !n.asegurarOcurrencia(tmp, ventana)){
            return false;
         }
      }

      return true;
   }

   /**
    * Indica en todos los padres del nodo que son útiles para un evento
    * concreto.
    */
   public void padresUtiles(){
      //Padre
      ((NodoAntepasadosAnotado)this.supernodo.padre).setUtil(true);
      //Padres adoptivos
      for(int i=0; i<padresAdoptivos.size();i++){
         NodoAntepasadosAnotado n = (NodoAntepasadosAnotado)padresAdoptivos.get(i);
         n.setUtil(true);
      }
   }

   /**
    * Llamar cuando se empieza una nueva secuencia
    */
   public void resetAnotacion(){
      inicioUltimaOcurrencia = -1;
      tmp = -1;
   }

   public boolean isUtil() {
      return util;
   }

   public void setUtil(boolean util) {
      this.util = util;
   }


   public void setModelo(IAsociacionTemporal modelo){
      this.modelo = modelo;
      if(modelo instanceof IAsociacionArbol){
         ((IAsociacionArbol)modelo).setNodo(this);
      }
   }
}

