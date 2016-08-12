package source.interrupcion;

public class CalculoSoportePasoDTO implements PasoDTO {
   private int sid;
   private int eid;
   public CalculoSoportePasoDTO(int sid, int eid){
      this.eid = eid;
      this.sid = sid;
   }

   public int getSid(){
      return sid;
   }

   public int getEid(){
      return eid;
   }

   public void setSid(int sid){
      this.sid = sid;
   }

   public void setEid(int eid){
      this.eid = eid;
   }
}