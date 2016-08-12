package source;

import java.util.ArrayList;
import java.util.List;

import source.patron.Ocurrencia;
import source.patron.Patron;

public class ComparacionPatrones {
   /*
    * Atributos
    */

   private List<Ocurrencia> soloA;
   private List<Ocurrencia> soloB;
   private List<Ocurrencia> comunes;

   /*
    * Constructores
    */

   public ComparacionPatrones(){
      soloA = new ArrayList<Ocurrencia>();
      soloB = new ArrayList<Ocurrencia>();
      comunes = new ArrayList<Ocurrencia>();
   }

   /*
    * Métodos
    */


   /*
    * Static
    */

   public static ComparacionPatrones comparaPatrones(Patron pA, Patron pB){
      if(!pA.equals(pB)){
         return null;
      }

      ComparacionPatrones comp = new ComparacionPatrones();
      comp.comunes.addAll(pA.getOcurrencias());
      if(comp.comunes.retainAll(pB.getOcurrencias())){
         //Si entra aquí B no tiene todas las ocurrencias de A, es decir A tiene ocurrencias que B no
         for(Ocurrencia o:pA.getOcurrencias()){
            if(!comp.comunes.contains(o)){
               comp.soloA.add(o);
            }
         }
      }
      if(comp.comunes.size()<pB.getOcurrencias().size()){
         for(Ocurrencia o:pB.getOcurrencias()){
            if(!comp.comunes.contains(o)){
               comp.soloB.add(o);
            }
         }
      }
      return comp;
   }


   /*
    * Non-static
    */


   @Override
   public String toString(){
      return "Comparacion de ocurrencias de patrones: "
            + "\n\tIguales: " + comunes.size()
            + "\n\tSolo el primero: " + soloA.size()
            + "\n\t\t" + soloA
            + "\n\tSolo el segundo: " + soloB.size()
            + "\n\t\t" + soloB;
   }

   public List<Ocurrencia> getSoloA() {
      return soloA;
   }

   public void setSoloA(List<Ocurrencia> soloA) {
      this.soloA = soloA;
   }

   public List<Ocurrencia> getSoloB() {
      return soloB;
   }

   public void setSoloB(List<Ocurrencia> soloB) {
      this.soloB = soloB;
   }

   public List<Ocurrencia> getComunes() {
      return comunes;
   }

   public void setComunes(List<Ocurrencia> comunes) {
      this.comunes = comunes;
   }

   public boolean sonIguales(){
      return soloA.isEmpty() && soloB.isEmpty();
   }

}
