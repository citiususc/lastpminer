package source.modelo;

import java.util.List;

import source.excepciones.FactoryInstantiationException;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;

public class PatternFactoryTest extends PatternFactory{

   private static final String PATRON_PRUEBA = "PatronPrueba";

   private static class PatternFactoryTestHolder {
      private static final PatternFactory INSTANCE = new PatternFactoryTest();
   }

   public static PatternFactory getInstance(){
      return PatternFactoryTestHolder.INSTANCE;
   }

   protected PatternFactoryTest(){

   }

   @Override
   public Patron getPattern(GeneradorID genID, String className, String[] types,
         List<RIntervalo> constraints, boolean savePatternInstances, Integer id) throws FactoryInstantiationException {
      if(className.equals(PATRON_PRUEBA)){
         throw new RuntimeException("Not implemented");
      }
      return super.getPattern(genID, className, types, constraints, savePatternInstances, id);
   }

   @Override
   public Patron getPatternExtension(String className, String[] types,
         Patron subPattern, int threads) throws FactoryInstantiationException {
      if(className.equals(PATRON_PRUEBA)){
         return new PatronPrueba(types, subPattern);
      }
      return super.getPatternExtension(className, types, subPattern, threads);
   }

   @Override
   public Patron getPatternClone(String className, Patron pattern, int threads) throws FactoryInstantiationException {
      if(className.equals(PATRON_PRUEBA)){
         return new PatronPrueba(pattern);
      }
      return super.getPatternClone(className, pattern, threads);
   }


}
