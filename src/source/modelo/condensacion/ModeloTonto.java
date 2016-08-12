package source.modelo.condensacion;

import java.util.Collections;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.Modelo;
import source.patron.Patron;

/**
 * Se utilizan sus métodos estáticos.
 * Utilizada {@link MineSuperModelo} (version supermodelo de ASTPMiner)
 *
 * @author vanesa.graino
 *
 */
public class ModeloTonto extends Modelo implements IModeloTonto {
   private static final long serialVersionUID = 4099774630441370831L;

   /*
    * Atributos de Tonto
    */

   /**
    * Índices de los tipos de eventos del modelo en el SuperModelo
    */
   protected int[] indices;
   /**
    * Atributo tam del Supermodelo en el que se registra el número de eventos de
    * cada tipo para cada tipo de evento de la colección
    */
   protected int[] tamColeccion;

   public void setTamColeccion(int[] tamColeccion) {
      this.tamColeccion = tamColeccion;
   }

   //Para test
   protected ModeloTonto(String[] tipos, int ventana, Integer frecuencia){
      super(tipos, ventana, frecuencia);
   }

   public ModeloTonto(String[] tipos, int ventana, Integer frecuencia,
         ISuperModelo supermodelo) {
      super(tipos, ventana, frecuencia);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloTonto(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia,
         ISuperModelo supermodelo) {
      super(tipos, ventana, patrones, frecuencia);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   /*
    * Métodos estáticos relacionados expuestos publicamente
    */

   /**
    * Actualiza tam y devuelve false si algún valor de tam es cero.
    * @param tamColeccion
    * @param indices
    * @param tam
    * @return
    */
   public static boolean actualizaTamComprueba(int[] tamColeccion, int[] indices, int[] tam){
      for(int i=0;i<indices.length;i++){
         tam[i] = tamColeccion[indices[i]];
         //if(tam[i]<=rep[i]){ return false; }
         if(tam[i]<=0){ return false; }
      }
      return true;
   }

   public static void actualizaTam(int[] tamColeccion, int[] indices, int[] tam){
      for(int i=0;i<indices.length;i++){
         tam[i] = tamColeccion[indices[i]];
      }
   }

   /* Métodos sobreescritos */

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      return ModeloTonto.actualizaTamComprueba(tamColeccion, indices, getTam());
   }


   /*
    * Publico
    * (non-Javadoc)
    * @see source.modelo.Modelo#getLimites()
    */
   @Override
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
