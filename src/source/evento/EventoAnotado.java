package source.evento;

import java.util.ArrayList;
import java.util.List;

import source.patron.Patron;

/**
 * Agregado de una instancia de tipo Evento con las anotaciones de patrones que se pueden encontrar en la ventana
 * que termina en el Evento.
 * @author Miguel
 */
public class EventoAnotado {

   /*
    * Atributos
    */

   private final Evento evento;
   private final List<Patron> anotaciones;

   /*
    * Constructores
    */

   public EventoAnotado(Evento evento, List<Patron> anotaciones){
      this.evento=evento;
      this.anotaciones = new ArrayList<Patron>(anotaciones.size());
      this.anotaciones.addAll(anotaciones);
   }

   /*
    * Getters & Setters
    */

   public Evento getEvento() {
      return evento;
   }

   public List<Patron> getAnotaciones() {
      return anotaciones;
   }

   public String toString(){
      StringBuilder result= new StringBuilder();
      result.append("[ " + evento + " : ");
      for(Patron patron : anotaciones){
         result.append(patron.getID()).append(", ");
      }
      result.append(']');
      return result.toString();
   }
}
