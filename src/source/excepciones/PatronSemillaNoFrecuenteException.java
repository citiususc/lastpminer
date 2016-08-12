package source.excepciones;

public class PatronSemillaNoFrecuenteException extends SemillasNoValidasException {

   /**
    *
    */
   private static final long serialVersionUID = 2298405984265213110L;

   public PatronSemillaNoFrecuenteException(){
      super();
   }

   public PatronSemillaNoFrecuenteException(String message){
      super(message);
   }
}
