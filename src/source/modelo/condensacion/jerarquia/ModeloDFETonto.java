package source.modelo.condensacion.jerarquia;

import java.util.Collections;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.condensacion.IModeloTonto;
import source.modelo.condensacion.ISuperModelo;
import source.modelo.condensacion.ModeloTonto;
import source.modelo.jerarquia.ModeloDictionaryFinalEvent;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class ModeloDFETonto extends ModeloDictionaryFinalEvent implements IModeloTonto {

   /**
    *
    */
   private static final long serialVersionUID = 4193136323147626260L;

   protected int[] indices;
   protected int[] tamColeccion;

   public ModeloDFETonto(String[] tipos, int ventana, Integer frecuencia,
         ISuperModelo supermodelo){
      super(tipos,ventana,frecuencia);
      //indices = new int[tipos.size()];
      indices = supermodelo.fijarEstructuras(this);
   }

   public ModeloDFETonto(String[] tipos, int ventana, List<Patron> patrones, Integer frecuencia,
         ISuperModelo supermodelo){
      super(tipos,ventana,patrones, frecuencia);
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
