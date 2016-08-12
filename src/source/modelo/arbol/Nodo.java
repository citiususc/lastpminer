package source.modelo.arbol;

import java.util.Arrays;
import java.util.Map;

import source.modelo.IAsociacionTemporal;

public class Nodo{

   /*
    *  Atributos
    */

   // Modelo asociado a 'this'
   protected IAsociacionTemporal modelo;
   // Modelos que extienden a 'modelo'
   // hijos no es nunca null (?)
   protected Supernodo hijos;
   // Supernodo en el que se encuentra 'this'
   protected Supernodo supernodo;
   // los padres que no son directamente el padre del nodo
   // pero son subasociaciones del mismo
   //private final List<Nodo> padresAdoptivos; //usado por MineAhorro

   /*
    *  Constructores
    */

   //Sólo para clases hijas
   protected Nodo(){

   }

   public Nodo(IAsociacionTemporal modelo){
      this.modelo = modelo;
      this.hijos = new Supernodo(this);
      this.supernodo = null;
   }

   public Nodo(IAsociacionTemporal modelo, Supernodo supernodo){
      this.modelo = modelo;
      // Crear supernodo de hijos y decirle que 'this' es el padre
      this.hijos = new Supernodo(this);
      this.supernodo = supernodo;
   }

   public Nodo(IAsociacionTemporal modelo, Supernodo hijos, Supernodo supernodo){
      this.modelo = modelo;
      this.hijos = hijos;
      this.supernodo = supernodo;
   }

   // Getters y Setters

   public IAsociacionTemporal getModelo(){
      return modelo;
   }

   public void setModelo(IAsociacionTemporal modelo){
      this.modelo = modelo;
   }

   public Supernodo getHijos(){
      return hijos;
   }

   public void setHijos(Supernodo hijos){
      this.hijos = hijos;
   }

   public void addHijo(Nodo nodo, String tipo){
      nodo.setSupernodo(hijos);
      hijos.addNodo(nodo, tipo);
   }

   public Supernodo getSupernodo(){
      return supernodo;
   }

   public void setSupernodo(Supernodo supernodo){
      this.supernodo = supernodo;
   }

   public void removeHijos(){
      this.hijos = null;
   }

   //public List<Nodo> getPadresAdoptivos(){
   //   return padresAdoptivos;
   //}

   /**
    * Devuelve el ultimo tipo de la asociacion temporal del nodo
    * @return
    */
   public String getUltimoTipo(){
      return modelo.getUltimoTipo();
   }

   public String toString(){
      StringBuilder aux= new StringBuilder(25);
      aux.append("Nodo: ")
         .append( modelo.toStringSinPatrones())
         .append( "\n Hijos: [ ");
      if(!hijos.getListaNodos().isEmpty()){
         for(Nodo hijo : hijos.getListaNodos()){
            aux.append( Arrays.toString(hijo.getModelo().getTipos())).append(", ");
         }
         aux.deleteCharAt(aux.length()-1);
         aux.deleteCharAt(aux.length()-1);
      }
      aux.append( "]\n");
      return aux.toString();
   }

   public Nodo clonar(Supernodo supernodo, Map<String[],IAsociacionTemporal> asociacionesClonadas){
      Nodo clon = new Nodo(asociacionesClonadas.get(modelo.getTipos()), supernodo);
      clon.supernodo = supernodo;
      clon.hijos = hijos.clonar(asociacionesClonadas,clon);
      return clon;
   }
}
