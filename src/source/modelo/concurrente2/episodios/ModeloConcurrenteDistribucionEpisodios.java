package source.modelo.concurrente2.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.clustering.IClustering;
import source.modelo.concurrente2.ModeloConcurrenteDistribucion;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.Patron;

/**
 * Tiene que ser clase hija de ModeloConcurrenteDistribucion para poder
 * instanciar el modelo de hilo con o sin episodios. Si fuese hija de
 * ModeloConcurrenteEpisodios con la que comparte gran parte del código,
 * no podría instanciar el modelo de hilo.
 * @author vanesa.graino
 *
 */
public class ModeloConcurrenteDistribucionEpisodios extends ModeloConcurrenteDistribucion implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteDistribucionEpisodios.class.getName());
   private static final long serialVersionUID = -7751615141484685680L;

   /*
    * Atributos propios
    */

   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloConcurrenteDistribucionEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana, Integer frecuencia, IClustering clustering, int numHilos) {
      super(tipos, ventana, frecuencia, clustering, numHilos,
            false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteDistribucionEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos) {
      super(tipos, ventana, patrones, frecuencia, clustering,
            numHilos, false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteDistribucionEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana, List<Patron> patrones, int[] distribucion,
         IClustering clustering, int numHilos){
      super(tipos, ventana, patrones, distribucion, clustering, numHilos, false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   /*
    * Métodos propios
    */

   /*
    * Como hay atributo de episodios se instancia el modelo con o sin episodios
    * (non-Javadoc)
    * @see source.modelo.concurrente2.ModeloConcurrenteDistribucion#crearModelosHilos(int)
    */
   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      if(episodios.getEventosDeEpisodios()==0){
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloDistribucionHilo(tSize));
         }
      }else{
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloDistribucionEpisodiosHilo(tSize));
         }
      }
   }

   @Override
   public List<Episodio> getEpisodios() {
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos() {
      return episodios.isEpisodiosCompletos();
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloDistribucionEpisodiosHilo extends ModeloParaleloDistribucionHilo {

      public ModeloParaleloDistribucionEpisodiosHilo(int tSize) {
         super(tSize);
      }

      @Override
      protected int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites, int[] indices, int[] instancia){
         return episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, getVentana());
      }

      /*
       * Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
       * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
       * Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
       * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
       * {@code tiposReordenados}) y actúa como en la versión tradicional. La segunda parte se centra en los tipos
       * que forman parte de episodio,
       */
      @Override
      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
//         if(eventosDeEpisodios==0){
//            return super.siguienteCombinacion(tam,indices,index,tipo);
//         }
         return episodios.siguienteCombinacion(tam, indices, index, tipo);
      }

      @Override
      public void actualizaVentana(int sid, Evento evento) {
         int index = Arrays.binarySearch(getTipos(), evento.getTipo());
         actualizaVentana(sid, evento, evento.getTipo(), evento.getInstante(), index, getTipos().length );
      }

      @Override
      public boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){

         int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

         return episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp); //TODO ultimoSid
      }

      /*
       * Redefinición del método para tratar con el conocimiento de episodios.
       *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
       *   -) Una vez calculados los índices, se construye la instancia usando
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         //Esta comprobación no es necesaria ya que se instancia el tipo de ModeloParaleloHijo correspondiente
         //si hay o no eventosDeEpisodio.
//         if(eventosDeEpisodios==0){
//            super.recibeEvento(sid, ev, savePatternInstances);
//            return;
//         }
         String[] tiposReordenados = episodios.getTiposReordenados();
         String tipo = ev.getTipo();
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);
         int tSize = tiposReordenados.length;

         int tmp = ev.getInstante();

         // Actualizar índices fin e inicio para adaptarse a la ventana
         // Eliminar elementos que ya no están en ventana
         // Eliminar eventos que pertenecen a episodios
         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
         int[] indices = episodios.primerosIndices(index, tam);
         if(indices == null){
            return;
         }

         // Actualizar frecuencias
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         buscaOcurrenciasTam2(tipo, index, Arrays.binarySearch(tipos, tipo), tmp, tSize, indices);


      }

   }

}
