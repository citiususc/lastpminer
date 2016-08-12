package source.modelo;

import java.util.List;

import source.evento.Episodio;
import source.excepciones.FactoryInstantiationException;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

public class AssociationFactoryTest extends AssociationFactory {

   private static final String PATRON_PRUEBA = "PatronPrueba";

   private static class AssociationFactoryTestHolder {
      private static final AssociationFactoryTest INSTANCE = new AssociationFactoryTest();
   }

   public static AssociationFactory getInstance(){
      return AssociationFactoryTestHolder.INSTANCE;
   }


   public IAsociacionTemporal getAssociationInstance(String className, String[] types,
         int windowSize, List<Patron> patterns) throws FactoryInstantiationException{

      if(className.equals(PATRON_PRUEBA)){
         //TODO
      }
      return super.getAssociationInstance(className, types, windowSize, patterns, 0);

   }

   public IAsociacionTemporal getAssociationInstance(String className, String[] types,
         int windowSize, List<Patron> patterns, List<Episodio> episodes) throws FactoryInstantiationException{
      if(className.equals(PATRON_PRUEBA)){
         //TODO

      }
      return super.getAssociationInstance(className, types, episodes, windowSize, patterns, 0);
   }

   public IAsociacionTemporal getAssociationInstance(String className,
         String[] types,	int windowSize, IClustering clustering) throws FactoryInstantiationException{
      if(className.equals(PATRON_PRUEBA)){
         //TODO

      }
      return super.getAssociationInstance(className, types, windowSize, clustering,0);
   }

   public IAsociacionTemporal getAssociationInstance(String className,
         String[] types,	List<Episodio> episodes, int windowSize,
         IClustering clustering) throws FactoryInstantiationException{
      if(className.equals(PATRON_PRUEBA)){
         //TODO

      }
      return super.getAssociationInstance(className, types,episodes, windowSize, clustering,0);
   }
}
