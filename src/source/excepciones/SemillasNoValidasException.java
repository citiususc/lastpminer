package source.excepciones;

public class SemillasNoValidasException extends AlgoritmoException {
   private static final long serialVersionUID = -2150521807723797487L;

   public SemillasNoValidasException(){
      super("Patrón(es) semilla no válido(s)");
   }

   public SemillasNoValidasException(String message){
      super(message);
   }
}
