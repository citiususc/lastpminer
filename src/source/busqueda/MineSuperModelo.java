package source.busqueda;

import java.util.List;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.SuperModelo;
import source.patron.Patron;


/**
 * Versión de ASTPminer sin episodios ni semilla que utiliza SuperModelo para
 * gestionar la ventana  (inserta y elimina los eventos) para todos los eventos.
 * Los demás modelos utilizan la ventana de este supermodelo (sólo los tipos que
 * les interesan) y comprueban las ocurrencias de sus respectivos patrones.
 *
 * @author vanesa.graino
 *
 */
public class MineSuperModelo extends Mine {
   private static final Logger LOGGER = Logger.getLogger(MineSuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /**
    * Es el único modelo que va a recibir eventos. Los demas
    * utilizarán sus estructuras de control.
    */
   protected SuperModelo supermodelo;

   public MineSuperModelo(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);

   }

   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize) throws FactoryInstantiationException {
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
      super.inicializaEstructuras(tipos, actual, win, cSize);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win) throws ModelosBaseNoValidosException {
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }


   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      final int tam = candidatas.get(0).size();
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
