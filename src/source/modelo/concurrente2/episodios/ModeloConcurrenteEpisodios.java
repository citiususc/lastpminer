package source.modelo.concurrente2.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.concurrente2.ModeloConcurrente;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.Patron;

public class ModeloConcurrenteEpisodios extends ModeloConcurrente implements IAsociacionConEpisodios{
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteEpisodios.class.getName());
   private static final long serialVersionUID = 4532794806297396774L;

   /*
    * Atributos propios
    */

   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloConcurrenteEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana,  Integer frecuencia, int numHilos){
      super(tipos, ventana,frecuencia,numHilos,false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteEpisodios(String[] tipos, List<Episodio> episodios,
         int ventana, List<Patron> patrones,  Integer frecuencia, int numHilos){
      super(tipos, ventana, patrones, frecuencia, numHilos, false);
      this.episodios= new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   /*
    * Métodos propios
    */

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      if(episodios.getEventosDeEpisodios()==0){
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloHilo(tSize));
         }
      }else{
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloEpisodiosHilo(tSize));
         }
      }
   }

   @Override
   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos(){
      return episodios.isEpisodiosCompletos();
   }

   /*
    * Métodos para que las subclases puedan acceder a la reordenación de tipos
    */

   protected String[] getTiposReordenados() {
      return episodios.getTiposReordenados();
   }

   protected int[] getEquivalenciasTipos() {
      return episodios.getEquivalenciasTipos();
   }

   protected int getEventosDeEpisodios() {
      return episodios.getEventosDeEpisodios();
   }



   /*
    * Clases privadas
    */

   protected class ModeloParaleloEpisodiosHilo extends ModeloParaleloHilo {

      public ModeloParaleloEpisodiosHilo(int tSize) {
         super(tSize);
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
//         	return super.siguienteCombinacion(tam,indices,index,tipo);
//      	}
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
//      		super.recibeEvento(sid, ev, savePatternInstances);
//      		return;
//      	}
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
         int frecuenciaLocal = 0;
         int[] instancia = new int[tSize];
         instancia[index]=tmp;

         int[] patFrecLocal = new int[patrones.size()];
         int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
            if(comprobarPatrones(instancia, patFrecLocal, sid, savePatternInstances)){
               frecuenciaLocal++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);

         }while(indices != null);
         addFrecuencias(frecuenciaLocal,patFrecLocal);
      }
   }

}
