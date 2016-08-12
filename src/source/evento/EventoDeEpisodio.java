package source.evento;

import source.io.MalformedFileException;

public class EventoDeEpisodio extends Evento {

   /*
    * Atributos propios
    */

   private InstanciaEpisodio instancia;

   /*
    * Constructores heredados
    */

   public EventoDeEpisodio(String tipo, int instante){
      super(tipo, instante);
   }

   public EventoDeEpisodio(String tipo, int instante, int eliminado){
      super(tipo, instante, eliminado);
   }

   public EventoDeEpisodio(String token) throws MalformedFileException{
      super(token);
   }

   /*
    * Métodos propios
    */

   public InstanciaEpisodio getInstancia(){
      return instancia;
   }

   public void setInstancia(InstanciaEpisodio instancia) {
      this.instancia = instancia;
   }

   public Integer getDuracion(){
      if(instancia.getFin() == null || instancia.getInicio() == null){ return null; }
      return instancia.getFin().getInstante() - instancia.getInicio().getInstante();
   }
}
