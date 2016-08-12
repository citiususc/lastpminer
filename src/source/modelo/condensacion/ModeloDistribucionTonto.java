package source.modelo.condensacion;

import java.util.Collections;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.Patron;

public class ModeloDistribucionTonto extends ModeloDistribucion implements IModeloTonto {
   private static final long serialVersionUID = -451618837974848959L;

   /*
    * Atributos de tonto
    */
   protected int[] indices;
   protected int[] tamColeccion;

   public ModeloDistribucionTonto(String[] tipos, int ventana, Integer frecuencia,
         IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, ventana, frecuencia, clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloDistribucionTonto(String[] tipos, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, ventana, patrones, frecuencia, clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloDistribucionTonto(String[] tipos, int ventana,
         List<Patron> patrones, int[] distribucion, IClustering clustering, ISuperModelo supermodelo) {
      super(tipos, ventana, patrones, distribucion, clustering);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   @Deprecated
   @Override
   public void actualizaVentana(int sid, Evento evento) {
      //Esto solo se hace con el supermodelo
      //super.actualizaVentana(sid, evento);
   }

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      return ModeloTonto.actualizaTamComprueba( tamColeccion, indices, getTam());
   }


   @Override
   public void setTamColeccion(int[] tam) {
      this.tamColeccion = tam;
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getLimites()
    */
   public int[][] getLimites() {
      return super.getLimites();
   }

   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getAbiertas()
    */
   @Override
   public int[][] getAbiertas() {
      return super.getAbiertas();
   }

   @Override
   public boolean sonEpisodiosCompletos() {
      return true;
   }

   @Override
   public List<Episodio> getEpisodios(){
      return Collections.emptyList();
   }
}
