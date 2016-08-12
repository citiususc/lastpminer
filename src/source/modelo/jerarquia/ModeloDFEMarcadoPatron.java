package source.modelo.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Evento;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatronMarcado;

public class ModeloDFEMarcadoPatron extends ModeloDictionaryFinalEvent {

   /**
    *
    */
   private static final long serialVersionUID = 5073029170826504597L;

   public ModeloDFEMarcadoPatron(String[] tipos, int ventana, Integer frecuencia){
      super(tipos,ventana,frecuencia);
   }

   public ModeloDFEMarcadoPatron(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia){
      super(tipos,ventana,patrones, frecuencia);
   }

   @Override
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

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         if(comprobarPatrones(posiblesPatrones, instancia, sid, index, savePatternInstances)){
            frecuenciaLocal++;
         }
      }while(siguienteInstancia(tam, indices, instancia, index, tipo, tSize));

      addFrecuencias(frecuenciaLocal, null);
   }

   @Override
   public void recibeEvento(int sid, Evento evento,
         boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados) {
      String[] tipos = getTipos();
      int[] tam = getTam();
      int i=0, frecuenciaLocal=0;
      int tSize = tipos.length;

      // Comprobar si puede haber ocurrencias de algún patrón
      for(i=0;i<tSize;i++){
         if(tam[i]<=0){
            return;
         }
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
      int tMin, tmp = evento.getInstante();

      // Comprobar si hay alguna ocurrencia de algún posible patrón.
      int[] indices = new int[tSize];
      boolean[] anotados = new boolean[posibles.size()];
      int[] instancia = new int[tSize];
      instancia[index]=tmp;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = fijarInstancia(tSize, index, tmp, indices, instancia);
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances, tMin, tmp)){
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
   /*
    *
    * Se diferencia del método que sobreescribe en que
    * se llama al método encontrado de los patrones de la asociación ya que son instancia de PatronMarcado.
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionaryFinalEvent#comprobarPatrones(java.util.List, int, int[], java.util.List, boolean[], int, int)
    */
   @Deprecated
   @Override
   protected boolean comprobarPatrones(List<Patron> patrones, int[] instancia,
         int sid, int index, boolean savePatternInstances) {
      return super.comprobarPatrones(patrones, instancia, sid, index, savePatternInstances);
   }

   @Deprecated
   @Override
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid, int[] instancia,
         List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances){
      return super.comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances);
   }

   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid, int[] instancia,
         List<Patron> encontrados, boolean[] anotados, boolean savePatternInstances, int inicioInstancia, int finInstancia){
      boolean encontrado = false;
      int i=0;
      for(PatronDictionaryFinalEvent patron : posibles){
         if(patron.representa(sid, instancia, savePatternInstances)){
            encontrado = true;
            ((PatronMarcado)patron).encontrado(sid, inicioInstancia, finInstancia);
            if(!anotados[i]){
               encontrados.add(patron);
               //patronEncontrado(encontrados, patron); //esta clase no desciende de ModeloDictionary sino de ModeloEventoFinal
               anotados[i]=true;
            }
            break;
         }
         i++;
      }
      return encontrado;
   }

   /*
    * Se sobreescribe para eliminar de los padres de los patrones la lista de ocurrencias encontradas
    * (non-Javadoc)
    * @see source.modelo.ModeloEventoFinal#calculaPatrones(int, java.lang.String)
    */
   /*@Override
   public void calculaPatrones(int supmin, String patternClassName, GeneradorID genID) {
      List<Patron> patrones = getPatrones();
      int pSize = patrones.size();
      for(int i=pSize-1;i>=0;i--){
         PatronMarcado patron = (PatronMarcado)patrones.get(i);
         if(patron.getFrecuencia()<supmin){
            patrones.remove(i);
         }
         for(PatronDictionaryFinalEvent padre : patron.getPadres()){
            ((PatronMarcado)padre).limpiar();
         }
      }
      setPatrones(patrones,null);
   }*/



}
