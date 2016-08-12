package source.modelo.condensacion.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionArbol;
import source.modelo.arbol.NodoAntepasadosAnotado;
import source.modelo.condensacion.ISuperModelo;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 *
 * @author vanesa.graino
 *
 */
public class ModeloEpisodiosDFETontoMarcarArbol extends ModeloEpisodiosDFETonto implements IAsociacionArbol{

   /**
    *
    */
   private static final long serialVersionUID = 1554543921031854734L;
   private NodoAntepasadosAnotado nodo;

   public ModeloEpisodiosDFETontoMarcarArbol(String[] tipos,
         List<Episodio> episodios, int ventana, ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, supermodelo);
   }

   public ModeloEpisodiosDFETontoMarcarArbol(String[] tipos,
         List<Episodio> episodios, int ventana, List<Patron> patrones,
         ISuperModelo supermodelo) {
      super(tipos, episodios, ventana, patrones, supermodelo);
   }

   /*
    * (non-Javadoc)
    * @see source.modelo.jerarquia.ModeloDictionaryFinalEvent#comprobarPatrones(java.util.List, int, int[], java.util.List, boolean[], int, int, boolean)
    */
   @Deprecated
   @Override
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid,
         int[] instancia, List<Patron> encontrados, boolean[] anotados,
         boolean savePatternInstances){
      return super.comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances);
   }

   @Override
   public void recibeEvento(int sid, Evento ev,
         boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados) {
      actualizaTam();

      //Con los modelos tontos se instancia de forma que si es un modelo con episodios siempre tiene
      //if(episodios.getEventosDeEpisodios()==0){
      //   super.recibeEvento(sid, ev, savePatternInstances, aComprobar, encontrados);
      //   return;
      //}

      // Comprobar qué patrones de 'aComprobar' pueden terminar con el evento leído 'evento'.
      List<PatronDictionaryFinalEvent> posibles = new ArrayList<PatronDictionaryFinalEvent>();
      String tipo = ev.getTipo();

      for(Patron patron : aComprobar){
         PatronDictionaryFinalEvent aux = (PatronDictionaryFinalEvent)patron;
         // Comprobar si pertenece a esta asociación temporal (siempre debería ser así)
         //if(tipos.containsAll(patron.getTipos()))
         //if(aux.getUltimoEventoLeido()!=ev && aux.getTiposFinales().contains(tipo)){
         if(aux.getUltimoEventoLeido()!=ev && aux.esTipoFinal(tipo)){
            posibles.add(aux);
         }
      }
      if(posibles.isEmpty()){//Porque solo cuando posibles es vacio y cuando
         // Actualizar los patrones con notificación de cuál fue el último evento leído
         for(PatronDictionaryFinalEvent patron : aComprobar){
         //for(PatronDictionaryFinalEvent patron : posibles){
            patron.setUltimoEventoLeido(ev);
         }
         return;
      }

      String[] tiposReordenados = episodios.getTiposReordenados();
      int index = Arrays.asList(tiposReordenados).indexOf(tipo);
      int tSize = tiposReordenados.length;
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      // Comprobar si puede haber ocurrencias de algún patrón
      for(int i=0;i<tSize;i++){
         if(tam[i]<=0){ return; }
      }

      // Actualizar frecuencias

      int tmp = ev.getInstante();

      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

      int[] indices = episodios.primerosIndices(index, tam, abiertas, limites, ventana, tmp);
      if(indices == null){
         return;
      }

      //int actualizadas;
      int frecuenciaLocal=0;
      boolean[] anotados = new boolean[posibles.size()];

      int[] instancia = new int[tSize]; // Construido en base a 'tipos'
      instancia[index]=tmp;
      int tMin;

      do{ // Recorre cada lista
         // Comprobar si pertenece a algun patrón
         tMin = episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
         if(comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances, tMin, tmp)){
            frecuenciaLocal++;
         }
         indices = siguienteCombinacion(tam, indices, index, tipo);

      }while(indices != null);

      // Actualizar los patrones con notificación de cuál fue el último evento leído
      for(PatronDictionaryFinalEvent patron : posibles){//patron : aComprobar
         patron.setUltimoEventoLeido(ev);
      }

      //addFrecuencias(frecuencia,patFrec);
      addFrecuencias(frecuenciaLocal, null);
   }

   //@Override
   protected boolean comprobarPatrones(List<PatronDictionaryFinalEvent> posibles, int sid,
         int[] instancia, List<Patron> encontrados, boolean[] anotados,
         boolean savePatternInstances, int inicioInstancia, int finInstancia){
      boolean encontrado = super.comprobarPatrones(posibles, sid, instancia, encontrados, anotados, savePatternInstances);
      if(encontrado){
         nodo.nuevaOcurrencia(inicioInstancia,finInstancia);
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
