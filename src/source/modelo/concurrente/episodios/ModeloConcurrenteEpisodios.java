package source.modelo.concurrente.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.concurrente.ModeloConcurrente;
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

   public ModeloConcurrenteEpisodios(String[] tipos, List<Episodio> episodios, int ventana,
         Integer frecuencia, int numHilos){
      super(tipos, ventana,frecuencia, numHilos,false);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteEpisodios(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, int numHilos){
      super(tipos, ventana, patrones, frecuencia, numHilos,false);
      this.episodios = new EpisodiosWrapper(episodios,tipos);
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

   /*
    * Clases privadas
    */

   protected class ModeloParaleloEpisodiosHilo extends ModeloParaleloHilo {

      public ModeloParaleloEpisodiosHilo(int tSize) {
         super(tSize);
      }

      /*
       *   Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
       * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
       *   Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
       * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
       * 'tiposReordenados') y actúa como en la versión tradicional. La segunda parte se centra en los tipos
       * que forman parte de episodio,
       */
      @Override
      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         return episodios.siguienteCombinacion(tam, indices, index, tipo);
      }

      @Override
      protected boolean actualizaVentana(int sid, String tipo, int tmp, int index, int tSize){

         int[][] abiertas = getAbiertas();
         int[][] limites = getLimites();
         int[] tam = getTam();
         return episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp); //TODO ultimoSid
      }

      /*
       * Redefinición del método para tratar con el conocimiento de episodios.
       *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
       *   -) Una vez calculados los índices, se construye la instancia usando
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
         String[] tiposReordenados = episodios.getTiposReordenados();
         String tipo = ev.getTipo();
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);
         int tSize = tiposReordenados.length;

         int tmp = ev.getInstante();

         if(!actualizaVentana(sid, tipo, tmp, index, tSize)){
            return;
         }

         int[] tam = getTam();
         int[] indices = episodios.primerosIndices(index, tam);
         if(indices == null){
            return;
         }

         // Actualizar frecuencias
         int[] instancia = new int[tSize];
         instancia[index]=tmp;
         int[][] abiertas = getAbiertas();
         int[][] limites = getLimites();

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);

            if(comprobarPatrones(instancia, sid, savePatternInstances)){
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam,indices,index,tipo);
         }while(indices != null);
         //addFrecuencias(frecuenciaLocal,patFrec);//se usan las variables del hilo
      }

   }	//Fin de clase interna


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


}
