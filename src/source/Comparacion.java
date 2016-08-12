package source;

import java.util.ArrayList;
import java.util.List;

import source.modelo.IAsociacionTemporal;

public class Comparacion{
   private static final String NIVEL_STR = "\n\tNivel ";

   protected List<List<IAsociacionTemporal>> patronesIguales = new ArrayList<List<IAsociacionTemporal>>();
   protected List<List<IAsociacionTemporal>> patronesSoloA = new ArrayList<List<IAsociacionTemporal>>();
   protected List<List<IAsociacionTemporal>> patronesSoloB = new ArrayList<List<IAsociacionTemporal>>();

   public boolean sonIguales(){
      for(List<IAsociacionTemporal> nivel:patronesSoloA){
         if(!nivel.isEmpty()){
            return false;
         }
      }
      for(List<IAsociacionTemporal> nivel:patronesSoloB){
         if(!nivel.isEmpty()){
            return false;
         }
      }
      return true;
   }

   public List<List<IAsociacionTemporal>> getPatronesIguales() {
      return patronesIguales;
   }

   public void setPatronesIguales(List<List<IAsociacionTemporal>> patronesIguales) {
      this.patronesIguales = patronesIguales;
   }

   public List<List<IAsociacionTemporal>> getPatronesSoloA() {
      return patronesSoloA;
   }

   public void setPatronesSoloA(List<List<IAsociacionTemporal>> patronesSoloA) {
      this.patronesSoloA = patronesSoloA;
   }

   public List<List<IAsociacionTemporal>> getPatronesSoloB() {
      return patronesSoloB;
   }

   public void setPatronesSoloB(List<List<IAsociacionTemporal>> patronesSoloB) {
      this.patronesSoloB = patronesSoloB;
   }

   @Override
   public String toString() {
      return toString(false);
   }

   public String toString(boolean imprimirPatrones) {
      boolean iguales = sonIguales();
      StringBuilder cadena = new StringBuilder(300);
      cadena.append("Resultado de la comparación: los resultados son ").append(iguales? " iguales " : "diferentes");

      int acumSoloA = 0, acumIguales=0, acumSoloB=0;
      for(int i=0;i<patronesSoloA.size();i++){
         for(IAsociacionTemporal asoc : patronesSoloA.get(i)){
            acumSoloA += asoc.getPatrones().size();
         }
      }
      for(int i=0;i<patronesSoloB.size();i++){
         for(IAsociacionTemporal asoc : patronesSoloB.get(i)){
            acumSoloB += asoc.getPatrones().size();
         }
      }
      for(int i=0;i<patronesIguales.size();i++){
         for(IAsociacionTemporal asoc : patronesIguales.get(i)){
            acumIguales += asoc.getPatrones().size();
         }
      }
      cadena.append("\n\tNúmero de patrones iguales: ").append(acumIguales)
               .append("\n\tNúmero de patrones que sólo tiene el 1º: ").append(acumSoloA)
               .append("\n\tNúmero de patrones que sólo tiene el 2º: ").append(acumSoloB)
               .append("\n\nPatrones que sólo tiene el 1º: ");

         if(acumSoloA>0){
            for(int i=0;i<patronesSoloA.size();i++){
               if(!patronesSoloA.get(i).isEmpty()){
                  cadena.append(NIVEL_STR).append(i+1).append(": ").append(patronesSoloA.get(i).size());
               }
            }
         }else{
            cadena.append("ninguno.");
         }


         cadena.append("\nPatrones que sólo tiene el 2º: ");
         if(acumSoloB>0){
            for(int i=0;i<patronesSoloB.size();i++){
               if(!patronesSoloB.get(i).isEmpty()){
                  cadena.append(NIVEL_STR + (i+1) + ": " + patronesSoloB.get(i).size());
               }
            }
         }else{
            cadena.append("ninguno.");
         }
      //}

      cadena.append("\nPatrones iguales: ");
      if(acumIguales>0){
         for(int i=0;i<patronesIguales.size();i++){
            if(!patronesIguales.get(i).isEmpty()){
               cadena.append(NIVEL_STR). append(i+1).append(": ").append(patronesIguales.get(i).size());
            }
         }
      }else{
         cadena.append("ninguno.");
      }

      if(imprimirPatrones){
         if(acumSoloA > 0){
            cadena.append("\nPatrones que sólo tiene el 1º: ");
            for(int i=0;i<patronesSoloA.size();i++){
               if(patronesSoloA.get(i).isEmpty()){ continue; }
               cadena.append(NIVEL_STR).append(i+1);
               for(IAsociacionTemporal asoc:patronesSoloA.get(i)){
                  cadena.append("\n\t\t").append(asoc);
               }
            }
         }

         if(acumSoloB > 0){
            cadena.append("\nPatrones que sólo tiene el 2º: ");
            for(int i=0;i<patronesSoloB.size();i++){
               if(patronesSoloB.get(i).isEmpty()){ continue; }
               cadena.append(NIVEL_STR + (i+1));
               for(IAsociacionTemporal asoc:patronesSoloB.get(i)){
                  cadena.append("\n\t\t").append(asoc);
               }
            }
         }

         /*cadena.append("\nPatrones iguales: ");
         for(int i=0;i<patronesIguales.size();i++){
            cadena.append(NIVEL_STR + (i+1));
            for(IAsociacionTemporal asoc:patronesIguales.get(i)){
               cadena.append("\n\t\t" + asoc);
            }
         }*/
      }
      return cadena.toString();
   }




}