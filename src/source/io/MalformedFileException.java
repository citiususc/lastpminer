package source.io;

public class MalformedFileException extends Exception {

   /**
    *
    */
   private static final long serialVersionUID = 7169128749836774685L;

   public MalformedFileException(){
      super();
   }

   public MalformedFileException(String message){
      super(message);
   }

   public MalformedFileException(String message, Throwable t){
      super(message, t);
   }
}
