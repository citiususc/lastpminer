package source.evento;

import java.util.StringTokenizer;

import source.io.MalformedFileException;

public class Evento implements Comparable<Evento>{

   private final String tipo;
   private String descripcion;
   private /*final*/ int instante;
   /** Iteracion en la que ha sido eliminado. -1 es que no está borrado */
   private int eliminado = -1; //false
   private boolean usado; // Utilizado por OccurrenceMarking OM , default to false

   public Evento(String tipo, int instante){
      this.tipo = tipo;
      this.instante = instante;
   }

   public Evento(String tipo, int instante, int eliminado){
      this(tipo, instante);
      this.eliminado = eliminado;
   }

   /*
    * Construye un Evento partiendo de un token (Tipo,tiempo)
    */
   public Evento(String token) throws MalformedFileException{
      StringTokenizer tkn = new StringTokenizer(token,"(,)");
      if(tkn == null || !tkn.hasMoreTokens()){
         throw new MalformedFileException("No tiene tipo: " + token);
      }
      this.tipo = tkn.nextToken();
      if(tkn == null || !tkn.hasMoreTokens()){
         throw new MalformedFileException("No tiene instante:" + token);
      }
      try{
         this.instante = Integer.parseInt(tkn.nextToken());
      }catch(NumberFormatException nfe){
         throw new MalformedFileException("El instante no es un número entero", nfe);
      }

   }

   public int getInstante() {
      return instante;
   }
   public void setInstante(int instante) {
      this.instante = instante;
   }
   public String getTipo() {
      return tipo;
   }
   /*public void setTipo(String tipo) {
      this.tipo = tipo;
   }*/
   public String getDescripcion(){
      return descripcion;
   }
   public void setDescripcion(String descripcion){
      this.descripcion = descripcion;
   }

   public String toString(){
      return "("+tipo+","+instante+")";
   }

   public boolean enRangoTemporal(int inicioRango, int finRango){
      return instante>=inicioRango && instante<=finRango;
   }

   public boolean estaEliminado(){
       return eliminado>-1;
   }

   public int getEliminado() {
      return eliminado;
   }

   public void setEliminado(int eliminado) {
      this.eliminado = eliminado;
   }

   @Override
   public int compareTo(Evento o) {
      //return Integer.compare(getInstante(), o.getInstante());
      if(instante < o.instante){
         return -1;
      }else if(instante == o.instante ){
         return tipo.compareTo(o.tipo);
      }
      return 1;
   }

   @Override
   public boolean equals(Object obj) {
      if(! (obj instanceof Evento )){ return false; }
      Evento ev = (Evento)obj;
      return instante == ev.instante && tipo.equals(ev.tipo);
   }

   @Override
   public int hashCode(){
      return tipo.hashCode()*(int)instante;
   }

   public boolean isUsado() {
      return usado;
   }

   public void setUsado(boolean usado) {
      this.usado = usado;
   }


}