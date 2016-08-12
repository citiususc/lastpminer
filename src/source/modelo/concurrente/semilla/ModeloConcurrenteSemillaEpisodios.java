package source.modelo.concurrente.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.logging.Logger;

import source.evento.Episodio;
import source.evento.Evento;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.clustering.IClustering;
import source.modelo.episodios.EpisodiosWrapper;
import source.patron.Patron;

public class ModeloConcurrenteSemillaEpisodios extends ModeloConcurrenteSemilla implements IAsociacionConEpisodios {
   //private static final Logger LOGGER = Logger.getLogger(ModeloConcurrenteSemillaEpisodios.class.getName());
   private static final long serialVersionUID = -9221930686172345393L;

   /*
    * Atributos
    */
   protected EpisodiosWrapper episodios;

   /*
    * Constructores
    */

   public ModeloConcurrenteSemillaEpisodios(String[] tipos, List<Episodio> episodios, int ventana,
         Integer frecuencia, IClustering clustering, int numHilos){
      super(tipos, ventana,frecuencia,clustering,numHilos,false);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   public ModeloConcurrenteSemillaEpisodios(String[] tipos, List<Episodio> episodios, int ventana, List<Patron> patrones,
         Integer frecuencia, IClustering clustering, int numHilos){
      super(tipos, ventana, patrones,frecuencia,clustering,numHilos,false);
      this.episodios = new EpisodiosWrapper(episodios, tipos);
      crearModelosHilos(numHilos);
   }

   /*
    * Métodos
    */

   /*
    *   Dados unos límites y unos índices, devuelve los índices para una nueva combinación de eventos
    * de la ventana, o 'null' si no hay combinaciones posibles o éstas ya se agotaron.
    *   Redefinido para tratar con el conocimiento de los episodios. El método se divide en dos partes.
    * La primera actúa sobre aquellos eventos que no son episodio (que son los últimos tipos en la lista
    * 'tiposReordenados') y actúa como en la versión tradicional. La segunda parte se centra en los tipos
    * que forman parte de episodio,
    */


   public List<Episodio> getEpisodios(){
      return episodios.getEpisodios();
   }

   @Override
   public boolean sonEpisodiosCompletos() {
      return episodios.isEpisodiosCompletos();
   }

   @Override
   protected void crearModelosHilos(int numHilos){
      int tSize = tipos.length;
      modelos = new ArrayList<ModeloParaleloHilo>();
      if(episodios.getEventosDeEpisodios()==0){
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloSemillaHilo(tSize));
         }
      }else{
         for(int i=0;i<numHilos;i++){
            modelos.add(new ModeloParaleloSemillaEpisodiosHilo(tSize));
         }
      }
   }

   /*
    * Clases privadas
    */

   protected class ModeloParaleloSemillaEpisodiosHilo extends ModeloParaleloSemillaHilo {

      public ModeloParaleloSemillaEpisodiosHilo(int tSize) {
         super(tSize);
      }

      @Override
      protected int[] siguienteCombinacionHilo(int[] tam, int[] indices, int index, String tipo){
         return episodios.siguienteCombinacion(tam, indices, index, tipo);
      }

      protected boolean actualizaVentana(int sid, Evento ev, String tipo, int tmp, int index, int tSize){
         int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'
         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas

         return episodios.actualizarVentana(abiertas, limites, tam, ventana, index, tmp);//TODO ultimoSid??
      }

      /*
       * Redefinición del método para tratar con el conocimiento de episodios.
       *   -) Los eventos se insertan siguiendo el orden de 'tiposReordenados'.
       *   -) Una vez calculados los índices, se construye la instancia usando
       */
      @Override
      public void recibeEvento(int sid, Evento ev, boolean savePatternInstances){
//       if(eventosDeEpisodios==0){
//          super.recibeEvento(sid, ev, savePatternInstances);
//          return;
//       }
         String[] tiposReordenados = episodios.getTiposReordenados();
         String tipo = ev.getTipo();
         int index = Arrays.asList(tiposReordenados).indexOf(tipo);

         int tmp = ev.getInstante();
         int tSize = tiposReordenados.length;
         if(tSize==1) {
            //LOGGER.severe("Entra en tSize==1 al recibir evento");
            frecuenciaHilo++;//incrementarSoporte();
            ultimaEncontrada[0] = tmp;
            ultimaEncontrada[1] = tmp;
            return;
         }

         if(!actualizaVentana(sid, ev, tipo, tmp, index, tSize)){
            return;
         }

         int[] tam = getTam(); // Tamaño de las listas circulares, numero de elementos en ellas
         int[] indices = episodios.primerosIndices(index, tam);
         if(indices == null){
            return;
         }

         // Actualizar frecuencias
         int i=0, j=0, valor;
         int[] instancia = new int[tSize];
         instancia[index] = tmp;

         int[][] abiertas = getAbiertas(); // Listas circulares, contienen instantes temporales
         int[][] limites = getLimites(); // Límites de las listas circulares, contienen índices a 'abiertas'

         ultimaEncontrada[0] = tmp;
         ultimaEncontrada[1] = tmp;

         do{ // Recorre cada lista
            // Comprobar si pertenece a algun patrón
            episodios.fijarInstancia(tSize, index, tmp, abiertas, limites, indices, instancia, ventana);
            if(comprobarPatrones(instancia, sid, savePatternInstances)){
               // Actualizar las distribuciones de frecuencia
               int dist=0;
               for(i=0;i<tSize;i++){ // Actualiza las distribuciones de frecuencia
                  if(ultimaEncontrada[0]>instancia[i]){ ultimaEncontrada[0]=instancia[i]; }
                  for(j=i+1;j<tSize;j++){
                     valor = (int)(instancia[j] - instancia[i]);
                     distribucionesHilo[dist][valor+ventana]++;
                     dist++;
                  }
               }
               frecuenciaHilo++;
            }
            indices = siguienteCombinacionHilo(tam, indices, index, tipo);
         }while(indices != null);
         //addFrecuencias(frecuenciaLocal,patFrec);
      }
   }


}
