package source.busqueda.jerarquia;

import java.util.List;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.SuperModelo;
import source.patron.Patron;

public class MineArbolSuperModelo extends MineArbol {
   private static final Logger LOGGER = Logger.getLogger(MineArbolSuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   protected SuperModelo supermodelo;

   /*
    * Constructores
    */

   public MineArbolSuperModelo(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
          IClustering clustering, boolean removePatterns) {
       super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Métodos
    */

   /*
    * (non-Javadoc)
    * @see source.busqueda.MineArbol#inicializaEstructuras(java.util.List, java.util.List, int)
    */
   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize) throws FactoryInstantiationException {
      super.inicializaEstructuras(tipos, actual, win, cSize);
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.Mine#calcularSoporte(java.util.List, source.evento.IColeccion)
    */
   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int tam = candidatas.get(0).size();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         for(Evento evento : secuencia){
            supermodelo.actualizaVentana(sid, evento);
            // Si en la ventana hay menos eventos que el tamaño de
            // la iteración ya no hay que comprobar nada más
            // TODO comprobar si es un cambio positivo en términos de tiempo
            if(supermodelo.enVentana()<tam){
               continue;
            }
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               receptor.recibeEvento(sid, evento, savePatternInstances);
            }
         }
         sid++;
      }
   }


   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            modArray, windowSize, getClustering(), supermodelo, numHilos);
   }


   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize,
            patrones, supermodelo, numHilos);
   }

}
