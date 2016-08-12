package source.excepciones;

public class ModelosBaseVaciosException extends ModelosBaseNoValidosException{

   /**
    *
    */
   private static final long serialVersionUID = 3319519835957149794L;

   public ModelosBaseVaciosException(){
      super("La lista de modelos base de reinicio de búsqueda no pueden ser nulos o estar vacíos");
   }

   public ModelosBaseVaciosException(String message){
      super(message);
   }
}
