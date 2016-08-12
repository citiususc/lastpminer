package source.interrupcion;

public class IteracionDTO implements PasoDTO {
   private final int nivel;

   public IteracionDTO(int nivel){
      this.nivel = nivel;
   }

   public int getNivel(){
      return nivel;
   }
}
