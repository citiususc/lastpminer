package source.excepciones;

public class ModelosBaseNoValidosException extends AlgoritmoException {

   /**
    *
    */
   private static final long serialVersionUID = -7846435568939748967L;

   public ModelosBaseNoValidosException(){
      super("Los modelos base indicados no son válidos");
   }

   public ModelosBaseNoValidosException(String message){
      super(message);
   }
}
