package source.modelo.episodios;

import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.clustering.IClustering;
import source.modelo.distribucion.ModeloDistribucion;
import source.patron.Patron;

public class ModeloDistribucionEpisodios extends ModeloDistribucion implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloDistribucionEpisodios.class.getName());
   private static final long serialVersionUID = -7404006352032605711L;

   /*
    * Atributos propiso
    */

   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloDistribucionEpisodios(String[] tipos, List<Episodio> episodios, int ventana, Integer frecuencia,
         IClustering clustering) {
      super(tipos, ventana, frecuencia, clustering);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   public ModeloDistribucionEpisodios(String[] tipos, List<Episodio> episodios, int ventana,
         List<Patron> patrones, Integer frecuencia, IClustering clustering) {
      super(tipos, ventana, patrones, frecuencia, clustering);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   public ModeloDistribucionEpisodios(String[] tipos, List<Episodio> episodios,  int ventana,
         List<Patron> patrones, int[] distribucion, IClustering clustering){
      super(tipos, ventana, patrones, distribucion, clustering);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
   }

   /*
    * Métodos propios
    */



   /*
    * @see source.modelo.Modelo#actualizaVentana(source.evento.Evento, java.lang.String, int, int, int)
    */
   @Override
   protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp,
         int index, int tSize) {
      int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
      int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
      int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

      boolean seguir = episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp, sid, ultimoSid);
      ultimoSid = sid;
      return seguir;
   }

   @Override
   protected int fijarInstancia(int tSize, int index, int tmp,
         int[][] abiertas, int[][] limites, int[] indices, int[] instancia) {
      return episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, getVentana());
   }

   @Override
   protected int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      if(episodios.getEventosDeEpisodios()==0){
         return super.siguienteCombinacion(tam, indices, index, tipo);
      }
      return episodios.siguienteCombinacion(tam, indices, index, tipo);
   }

   /*
    * Copiado de ModeloEpisodios a día 10 de Diciembre de 2014
    * (non-Javadoc)
    * @see source.modelo.ModeloDistribucion#recibeEvento(int, source.evento.Evento, boolean)
    */
   @Override
   public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
      int eventosDeEpisodios = episodios.getEventosDeEpisodios();
      if(eventosDeEpisodios==0){
         super.recibeEvento(sid, ev, savePatternInstances);
         return;
      }

      String[] tiposReordenados = episodios.getTiposReordenados();
      String tipo = ev.getTipo();
      int index = Arrays.asList(tiposReordenados).indexOf(tipo);
      final int tSize = 2;// tiposReordenados.length;
      int tmp = ev.getInstante();

      // Actualizar índices fin e inicio para adaptarse a la ventana
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

      buscaOcurrenciasTam2(tipo, index, Arrays.binarySearch(tipos, tipo), tmp, /*tSize,*/ indices);
   }

   @Override
   public List<Episodio> getEpisodios() {
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos() {
      return episodios.isEpisodiosCompletos();
   }
}
