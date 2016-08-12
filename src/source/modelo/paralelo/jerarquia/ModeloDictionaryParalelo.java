package source.modelo.paralelo.jerarquia;

import java.util.List;

import source.modelo.jerarquia.ModeloDictionary;
import source.modelo.paralelo.IAsociacionAgregable;
import source.patron.Patron;

public class ModeloDictionaryParalelo extends ModeloDictionary implements IAsociacionAgregable{
   private static final long serialVersionUID = -4907229131386207177L;


   /*
    * Constructores
    */

   public ModeloDictionaryParalelo(String[] tipos, int ventana, Integer frecuencia){
      super(tipos, ventana, frecuencia);
   }

   public ModeloDictionaryParalelo(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia){
      super(tipos, ventana, patrones, frecuencia);
   }

   /*
    * Métodos
    */

   @Override
   protected void patronEncontrado(List<Patron> encontrados, Patron patron){
      synchronized(encontrados){
         encontrados.add(patron);
      }
   }




}
