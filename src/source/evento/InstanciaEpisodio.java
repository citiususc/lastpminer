package source.evento;

/**
 * Esta clase representa una ocurrencia de un episodio concreto.
 * El inicio del episodio debe suceder antes (al mismo tiempo también vale??)
 * del fin.
 * @author vanesa.graino
 *
 */
public class InstanciaEpisodio {

   private EventoDeEpisodio inicio;
   private EventoDeEpisodio fin;

   public InstanciaEpisodio(EventoDeEpisodio inicio, EventoDeEpisodio fin){
      this.inicio = inicio;
      this.fin = fin;
   }

   public InstanciaEpisodio(){
      //Constructor vacio
   }

   /*
    * Métodos propios
    */

   public EventoDeEpisodio getInicio(){
      return inicio;
   }

   public EventoDeEpisodio getFin(){
      return fin;
   }

   public void setInicio(EventoDeEpisodio inicio){
      this.inicio = inicio;
   }

   public void setFin(EventoDeEpisodio fin){
      this.fin = fin;
   }

   public String toString(){
      if(inicio == null){
         return "(null~"+fin.getTipo()+", null, " + fin.getInstante() + ")";
      }
      if(fin == null){
         return "("+inicio.getTipo()+"~null , " + inicio.getInstante() + ", null)";
      }
      return "("+inicio.getTipo()+"~"+fin.getTipo()+", " + inicio.getInstante() + ", " + fin.getInstante() + ")";
   }
}
