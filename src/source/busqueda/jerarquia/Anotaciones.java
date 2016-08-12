package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import source.evento.Evento;
import source.evento.EventoAnotado;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.patron.Patron;

public class Anotaciones {
   protected List<List<List<Patron>>> actual;
   protected List<List<List<List<Patron>>>> todasAnotaciones = new ArrayList<List<List<List<Patron>>>>();  // Guardar histórico de todas las anotaciones
   protected boolean saveAllAnnotations;

   public Anotaciones(boolean saveAllAnnotations){
      this.saveAllAnnotations = saveAllAnnotations;
   }

   public Anotaciones(List<List<List<List<Patron>>>> todasAnotaciones){
      this.todasAnotaciones = todasAnotaciones;
   }

   /**
    * Genera las listas asociadas a cada evento de la colección en las que se incluirán los patrones encontrados
    * en cada iteración.
    * @param coleccion Colección de secuencias de eventos.
    * @param vacios Iteraciones en las que no se van a guardar eventos y que deben inicializarse como vacias.
    */
   public void generarEstructuraAnotaciones(IColeccion pColeccion, int vacios){
      int cSize = pColeccion.size();
      // Inicializar las estructuras auxiliares/privadas
      actual = new ArrayList<List<List<Patron>>>(cSize);
      for(ISecuencia secuencia : pColeccion){
         int sSize = secuencia.size();
         List<List<Patron>> asocActual = new ArrayList<List<Patron>>(sSize);
         // Crear tantas listas como elementos tiene la secuencia
         //(no es necesario acceder a los elementos de la secuencia)
         for(int j=0;j<sSize;j++){
            asocActual.add(new ArrayList<Patron>());
         }
         actual.add(asocActual);
      }
      for(int i=0;i<vacios;i++){ guardarAnotaciones(); }
   }

   // Devuelve el último conjunto de anotaciones
   public List<List<EventoAnotado>> getAnotaciones(IColeccion pColeccion){
      List<List<EventoAnotado>> result = new ArrayList<List<EventoAnotado>>(pColeccion.size());
      int iActual = 0;
      for(ISecuencia secuencia : pColeccion){
         List<EventoAnotado> lista = new ArrayList<EventoAnotado>();
         List<List<Patron>> patrones = actual.get(iActual++);
         int iPatrones = 0;
         for(Evento evento : secuencia){
            EventoAnotado anotado = new EventoAnotado(evento,patrones.get(iPatrones++));
            lista.add(anotado);
         }
         result.add(lista);
      }
      return result;
   }

   //Devuelve el conjunto de anotaciones de la iteración 'iteracion'
   public List<List<EventoAnotado>> getAnotaciones(IColeccion pColeccion, int iteracion){
      List<List<EventoAnotado>> result = new ArrayList<List<EventoAnotado>>(pColeccion.size());
      if(iteracion>=todasAnotaciones.size()){
         return result;
      }
      int iActual = 0;
      List<List<List<Patron>>> anotaciones = todasAnotaciones.get(iteracion);
      for(ISecuencia secuencia : pColeccion){
         List<EventoAnotado> lista = new ArrayList<EventoAnotado>();
         List<List<Patron>> patrones = anotaciones.get(iActual++);
         int iPatrones = 0;
         for(Evento evento : secuencia){
            EventoAnotado anotado = new EventoAnotado(evento,patrones.get(iPatrones++));
            lista.add(anotado);
         }
         result.add(lista);
      }
      return result;
   }

   public EventoAnotado getAnotaciones(IColeccion pColeccion, int iteracion, int sid, Evento ev){
      EventoAnotado result = null;
      if(iteracion>=todasAnotaciones.size()){ return result; }
      List<List<List<Patron>>> anotaciones = todasAnotaciones.get(iteracion);
      List<List<Patron>> anotacionesSecuencia = anotaciones.get(sid);
      int index = pColeccion.get(sid).indexOf(ev);
      return new EventoAnotado(ev, anotacionesSecuencia.get(index));
   }

   // Guarda el conjunto de anotaciones actual en 'todasAnotaciones'.
   // Solo tiene sentido si saveAllAnnotations es true.
   public void guardarAnotaciones(){
      if(!saveAllAnnotations){ return; }
      List<List<List<Patron>>> nuevas = new ArrayList<List<List<Patron>>>();
      for(List<List<Patron>> lista : actual){
         List<List<Patron>> copiaLista = new ArrayList<List<Patron>>();
         for(List<Patron> subLista : lista){
            List<Patron> copiaSubLista = new ArrayList<Patron>();
            copiaSubLista.addAll(subLista);
            copiaLista.add(copiaSubLista);
         }
         nuevas.add(copiaLista);
      }
      todasAnotaciones.add(nuevas);
   }

   public List<List<List<Patron>>> getActual() {
      return actual;
   }

   public boolean isSaveAllAnnotations() {
      return saveAllAnnotations;
   }

   public void setSaveAllAnnotations(boolean saveAllAnnotations) {
      this.saveAllAnnotations = saveAllAnnotations;
   }

   public List<List<List<List<Patron>>>> getTodasAnotaciones(){
      return todasAnotaciones;
   }

   public List<String> posiblesTiposParaAmpliar(List<Patron> actualEvento, List<String> tiposAmpliar){
      /*List<String> tiposPosibles = new ArrayList<String>();
      // Leer patron de 'actual'
      for(Patron patron : actual){
         List<String> tipos = patron.getTipos();
         // Leer los tipos del patrón para ver si hay alguno nuevo
         for(String tipo : tipos){
            // Añadir aquellos tipos que no están contenidos en 'tiposPosibles'
            int indice=0;
            boolean contains=false;
            for(String tipoPosible : tiposPosibles){
               if(tipoPosible.compareTo(tipo)==0){ //si son iguales, contiene 'tipo', pasar al siguiente
                  contains=true;
                  break;
               }
               if(tipoPosible.compareTo(tipo)>0){
                  // No está y hay que añadirlo.
                  tiposPosibles.add(indice,tipo); // Añadir en la posición 'indice', lexicográficamente correcta
                  break;
               }
               indice++;
            }
            if(!contains){
               tiposPosibles.add(tipo);
            }
         }
      }
      return tiposPosibles;*/
      Set<String> setTipos = new TreeSet<String>();
      for(Patron p: actualEvento){
         setTipos.addAll(Arrays.asList(p.getTipos()));
      }
      tiposAmpliar.clear();
      tiposAmpliar.addAll(setTipos);

      return tiposAmpliar;
   }


}
