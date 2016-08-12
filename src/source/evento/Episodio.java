package source.evento;

/**
 * Clase que representa un tipo de episodio.
 * @author vanesa.graino
 *
 */
public class Episodio{

   /*
    * Atributos
    */
   private final String tipoInicio;
   private final String tipoFin;

   /*
    * Constructores
    */

   public Episodio(String tipoInicio, String tipoFin) {
      this.tipoInicio = tipoInicio;
      this.tipoFin = tipoFin;
   }

   /*
    * Métodos
    */

   public String getTipoInicio(){
      return tipoInicio;
   }

   public String getTipoFin(){
      return tipoFin;
   }

   @Override
   public boolean equals(Object obj){
      return obj instanceof Episodio? equalsTo((Episodio)obj) : false;
   }

   public boolean equalsTo(Episodio episodio){
      return (this.tipoFin==episodio.tipoFin) && (this.tipoInicio==episodio.tipoInicio);
   }

   @Override
   public int hashCode(){
      return tipoFin.hashCode() + tipoInicio.hashCode()*31;
   }

   @Override
   public String toString(){
      return "("+tipoInicio+"~"+tipoFin+")";
   }

}
