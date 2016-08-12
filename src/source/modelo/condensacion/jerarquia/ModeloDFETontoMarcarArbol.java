package source.modelo.condensacion.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.modelo.IAsociacionArbol;
import source.modelo.arbol.NodoAntepasadosAnotado;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloTonto;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Se encarga de notificar a su nodo del árbol que ha habido una ocurrencia de uno
 * de sus patrones y cual es el inicio de ésta.
 *
 * Cuando se crea este modelo debe fijarse inmediatamente su nodo.
 * @author vanesa.graino
 *
 */
public class ModeloDFETontoMarcarArbol extends ModeloDFETonto implements IAsociacionArbol{

   /**
    *
    */
   private static final long serialVersionUID = 6403971122271423333L;

   private NodoAntepasadosAnotado nodo;

   public ModeloDFETontoMarcarArbol(String[] tipos, int ventana, Integer frecuencia,
         ISuperModelo supermodelo){
      super(tipos,ventana,frecuencia,supermodelo);
   }

   public ModeloDFETontoMarcarArbol(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, ISuperModelo supermodelo){
      super(tipos,ventana,patrones, frecuencia,supermodelo);
   }

   @Deprecated
   @Override
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid,
         int[] instancia, List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances){
      return super.comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances);
   }

   /*
    * Copy-paste de ModeloEventoFinal
    * (non-Javadoc)
    * @see source.modelo.ModeloEventoFinal#recibeEvento(int, source.evento.Evento, boolean)
    */
   /*@Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances) {
      String tipo = ev.getTipo();
      int index = Arrays.binarySearch(tipos, tipo);//tipos.indexOf(tipo);
      int tSize = tipos.length;
      int tmp = ev.getInstante();
      if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
         return;
      }

      List<Patron> posiblesPatrones = getDiccionarioPatrones().get(index);
      if(posiblesPatrones.isEmpty()){
         return;
      }

      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
      int frecuenciaLocal=0;

      // Actualizar frecuencias
      int[] indices = new int[tSize];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      fijarInstancia(tSize, index, indices, instancia);
      int tMin;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);

         if(comprobarPatrones(posiblesPatrones, instancia, sid, index,
               savePatternInstances)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);
      //}while(siguienteInstancia(tam, indices, instancia, index, tipo, tSize));
      }while(indices != null);

      addFrecuencias(frecuenciaLocal, null);
   }*/

   /*
    * Copy paste de ModeloDictionaryFinalEvent y añadida la llamada a ModeloTonto.actualizaTam.
    * Cambiada la llamada a comprobarPatrones que añade nuevos parametros para marcar el árbol
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionaryFinalEvent#recibeEvento(int, source.evento.Evento, boolean, java.util.List, java.util.List)
    */
   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados) {
      ModeloTonto.actualizaTam(tamColeccion, indices, getTam());
      String[] tipos = getTipos();
      int[] tam = getTam();
      //int[] rep = getRep();
      int i=0, frecuenciaLocal=0;
      int tSize = tipos.length;

      // Comprobar si puede haber ocurrencias de algún patrón
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){ return; }
      }

      String tipo = evento.getTipo();
      // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
      List<PatronDictionaryFinalEvent>  posibles = new ArrayList<PatronDictionaryFinalEvent>(aComprobar.size());
      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         //if(aux.getUltimoEventoLeido()!=evento && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=evento && aux.esTipoFinal(tipo)){
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){ return; }

      int index = Arrays.binarySearch(tipos, tipo); //tipos.indexOf(tipo);
      int tmp = evento.getInstante();

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;
      int tMin;
      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);
         if(comprobarPatrones(posibles, sid, instancia, encontrados,
               anotados, savePatternInstances, tMin, tmp)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam,indices,index,tipo);

      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      // Sólo los de posibles y no los de aComprobar porque se han podido quedado fuera
      // por no contener al evento como posible evento final.
      for(PatronDictionaryFinalEvent patron : posibles){ // patron : aComprobar){
         patron.setUltimoEventoLeido(evento);
      }

      // Actualizar la frecuencia
      addFrecuencias(frecuenciaLocal,null);
   }

   //@Override
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid,
         int[] instancia, List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances,
         int inicioInstancia, int finInstancia){
      boolean encontrado = super.comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances);
      if(encontrado){
         nodo.nuevaOcurrencia(inicioInstancia, finInstancia);
      }
      return encontrado;
   }

   public NodoAntepasadosAnotado getNodo(){
      return nodo;
   }

   public void setNodo(NodoAntepasadosAnotado nodo){
      this.nodo = nodo;
   }


}
