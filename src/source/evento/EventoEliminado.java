package source.evento;

import java.util.Comparator;

/*
 * Representa un evento que ha sido eliminado de la colección de secuencias de eventos.
 */
public class EventoEliminado implements Comparator<EventoEliminado>, Comparable<EventoEliminado>{

   /*
    * Atributos propios
    */

   /**
    * Iteración indica en qué iteración de la minería ha sido eliminado el evento.
    * El valor -1 indica que ha sido eliminado con el patrón semilla.
    */
   private final int iteracion;
   private final int sid;
   private final Evento evento;

   /*
    * Constructores
    */

   public EventoEliminado(int sid, Evento evento, int iteracion){
      this.sid = sid;
      this.iteracion = iteracion;
      this.evento = evento;
      this.evento.setEliminado(iteracion);
   }

   /*
    * Métodos propios
    */

   public int getSid() {
      return sid;
   }

   public Evento getEvento() {
      return evento;
   }

   public int getIteracion(){
      return iteracion;
   }

   @Override
   public String toString() {
      return iteracion + "#" + sid + ". " + evento;
   }

   @Override
   public boolean equals(Object obj){
      if(!(obj instanceof EventoEliminado)){ return false; }
      EventoEliminado eve = (EventoEliminado)obj;
      if(eve.sid != sid){ return false; }
      return eve.evento.equals(evento);
   }

   @Override
   public int compare(EventoEliminado o1, EventoEliminado o2) {
      if(o1.sid == o2.sid){ return o1.evento.compareTo(o2.evento); }
      return Integer.compare(o1.sid, o2.sid);
   }

   @Override
   public int compareTo(EventoEliminado o){
      return compare(this, o);
   }

   @Override
   public int hashCode(){
      return evento.hashCode() + sid*31;
   }
}
