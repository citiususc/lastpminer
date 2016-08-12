package source.modelo.condensacion.jerarquia;

import java.util.Collections;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.condensacion.IModeloTonto;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloTonto;
import source.modelo.jerarquia.ModeloDictionary;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Utiliza las estructuras del supermodelo en lugar de manejar una
 * lista de abiertas y limites propios, como hace Modelo.
 * NOTA: Con tipos de eventos repetidos esto posiblemente no vaya a funcionar.
 *
 * @author vanesa.graino
 *
 */
public class ModeloDictionaryTonto extends ModeloDictionary implements IModeloTonto {
   private static final long serialVersionUID = 1959550880222186531L;

   /*
    * Atributos de Tonto
    */

   protected int[] indices;
   protected int[] tamColeccion;

   public ModeloDictionaryTonto(String[] tipos, int ventana, ISuperModelo supermodelo) {
      super(tipos, ventana,null);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloDictionaryTonto(String[] tipos, int ventana,
         List<Patron> patrones,  ISuperModelo supermodelo) {
      super(tipos, ventana, patrones,null);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   @Deprecated
   @Override
   public void actualizaVentana(int sid, Evento evento) {
      //Esto solo se hace con el supermodelo
   }

   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
      return ModeloTonto.actualizaTamComprueba(tamColeccion, indices, getTam());
   }

   @Override
   public void recibeEvento(int sid, Evento evento, boolean savePatternInstances,
         List<PatronDictionaryFinalEvent> aComprobar, List<Patron> encontrados) {
      ModeloTonto.actualizaTam(tamColeccion, indices, getTam());
      super.recibeEvento(sid, evento, savePatternInstances, aComprobar, encontrados);
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
