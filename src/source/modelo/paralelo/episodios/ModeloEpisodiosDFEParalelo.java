package source.modelo.paralelo.episodios;

import java.util.List;

import source.evento.Episodio;
import source.modelo.episodios.ModeloEpisodiosDFE;
import source.modelo.paralelo.IAsociacionAgregable;
import source.patron.Patron;

public class ModeloEpisodiosDFEParalelo extends ModeloEpisodiosDFE implements IAsociacionAgregable {
   /**
    *
    */
   private static final long serialVersionUID = 7050511155996835160L;

   public ModeloEpisodiosDFEParalelo(String[] tipos, List<Episodio> episodios, int ventana, Integer frecuencia){
      super(tipos, episodios, ventana,frecuencia);
   }

   public ModeloEpisodiosDFEParalelo(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia){
      super(tipos, episodios, ventana, patrones,frecuencia);
   }

   @Override
   protected void patronEncontrado(List<Patron> encontrados, Patron patron){
      synchronized(encontrados){
         encontrados.add(patron);
      }
   }

}
