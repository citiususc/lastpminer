package source.patron;

import java.util.List;

import source.excepciones.FactoryInstantiationException;
import source.modelo.concurrente.PatronConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.modelo.concurrente.PatronConcurrenteEventoFinal;
import source.restriccion.RIntervalo;

/*
 * Tries to follow the SimpleFactory design pattern. Implements three methods,  the second
 * method creates instances of Patron from a set of event types and a subpattern, whereas the
 * the third creates instances of Patron intended to be clones of a Patron parameter.
 */
public class PatternFactory {
   //Thread safe singleton
   static class PatternFactoryHolder {
      private static final PatternFactory INSTANCE = new PatternFactory();
   }

   public static PatternFactory getInstance(){
      return PatternFactoryHolder.INSTANCE;
   }

   /*
    * Constructor protegido
    */

   protected PatternFactory(){
      //protegido pero no privado, para que pueda extenderse para las pruebas
   }

   /*
    * Métodos
    */

   /**
    *   Creates an appropriate instance of type Patron according to the parameters.
    * @param className Code for the particular Patron to be instanced.
    * @param types List of event types the instance will contemplate.
    * @return Appropriate instance of the pattern, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public Patron getPattern(GeneradorID genID, String className, String[] types,
         List<RIntervalo> constraints, boolean savePatternInstances) throws FactoryInstantiationException{
      return getPattern(genID, className, types, constraints, savePatternInstances, null);
   }

   public Patron getPattern(GeneradorID genID, String className, String[] types,
         List<RIntervalo> constraints, boolean savePatternInstances, Integer id) throws FactoryInstantiationException{
      Patron result;

      if(className=="Patron" || className==Patron.class.getName()){
         result = new Patron(types,constraints,savePatternInstances);
      }else if(className == "PatronDictionaryFinalEvent" || className==PatronDictionaryFinalEvent.class.getName()){
         result = new PatronDictionaryFinalEvent(types,constraints,savePatternInstances);
      }else if(className == "PatronEventoFinal" || className==PatronEventoFinal.class.getName()){
         result = new PatronEventoFinal(types,constraints,savePatternInstances);
      }else if(className=="PatronDictionaryFinalEvent"){
         result = new PatronDictionaryFinalEvent(types, constraints, savePatternInstances);
      }else if(className=="PatronAnotaciones"){
         result = new PatronAnotaciones(types, constraints, savePatternInstances);
      }else if(className=="PatronMarcado"){
         result = new PatronMarcado(types, constraints, savePatternInstances);
      }else if(className=="PatronNegacion"){
         result = new PatronNegacion(types, null, constraints, savePatternInstances);
      }else{

         throw new FactoryInstantiationException("PatternFactory cannot handle className: " + className);
      }
      if(id==null){
         result.patternID = genID.nextID();//Patron.nextID();
      }else{
         result.patternID = id;
         genID.assureID(id);//Patron.assureID(id);

      }
      return result;
   }

   /**
    *   Creates an appropriate instance of type Patron according to the parameters.
    * @param className Code for the particular Patron to be instanced.
    * @param types List of event types the instance will contemplate.
    * @param subPattern Subpattern of the instance to be produced.
    * @return Appropriate instance of the pattern, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public Patron getPatternExtension(String className, String[] types,
         Patron subPattern, int threads) throws FactoryInstantiationException{
      if(threads>0){
         return getPatternExtensionThreads(className, types, subPattern, threads);
      }
      Patron result;

      if(className=="Patron"){
         result = new Patron(types, subPattern);
      }else if(className=="PatronDictionaryFinalEvent"){
         result = new PatronDictionaryFinalEvent(types,subPattern);
      }else if(className=="PatronAnotaciones"){
         result = new PatronAnotaciones(types, subPattern);
      }else if(className=="PatronEventoFinal"){
         result = new PatronEventoFinal(types, subPattern);
      }else if(className=="PatronMarcado"){
         result = new PatronMarcado(types, subPattern);
      }else if(className=="PatronNegacion"){
         result = new PatronNegacion(types, null, subPattern);
      }else{
         throw new FactoryInstantiationException("PatternFactory cannot handle className: " + className);
      }

      return result;
   }
   private Patron getPatternExtensionThreads(String className, String[] types,
         Patron subPattern, int threads) throws FactoryInstantiationException{
      Patron result;

      if(className=="Patron"){
         result = new PatronConcurrente(types, subPattern);
      }else if(className=="PatronDictionaryFinalEvent"){
         result = new PatronConcurrenteDFE(types,subPattern, threads);
      }else if(className=="PatronEventoFinal"){
         result = new PatronConcurrenteEventoFinal(types,subPattern);
      }else{
         throw new FactoryInstantiationException("PatternFactory cannot handle className: " + className);
      }

      return result;
   }

   /**
    *   Creates an appropriate instance of type Patron according to the parameters.
    * @param className Code for the particular Patron to be instanced.
    * @param pattern Pattern to be cloned.
    * @return Appropriate instance of the pattern, or null if the code was wrong.
    * @throws FactoryInstantiationException
    */
   public Patron getPatternClone(String className, Patron pattern, int threads) throws FactoryInstantiationException{
      if(threads>0){
         return getPatternCloneThreads(className, pattern, threads);
      }
      Patron result;

      if(className=="Patron"){
         result = new Patron(pattern);
      }else if(className=="PatronDictionaryFinalEvent"){
         result = new PatronDictionaryFinalEvent(pattern);
      }else if(className=="PatronAnotaciones"){
         result = new PatronAnotaciones(pattern);
      }else if(className=="PatronEventoFinal"){
         result = new PatronEventoFinal(pattern);
      }else if(className=="PatronMarcado"){
         result = new PatronMarcado(pattern);
      }else if(className=="PatronNegacion"){
         result = new PatronNegacion(pattern);
      }else{
         throw new FactoryInstantiationException("PatternFactory cannot handle className: " + className);
      }

      return result;
   }

   private Patron getPatternCloneThreads(String className, Patron pattern, int threads) throws FactoryInstantiationException{
      Patron result;

      if(className=="Patron"){
         result = new PatronConcurrente(pattern);
      }else if(className=="PatronDictionaryFinalEvent"){
         result = new PatronConcurrenteDFE(pattern, threads);
      }else if(className=="PatronEventoFinal"){
         result = new PatronConcurrenteEventoFinal(pattern);
      }else{
         throw new FactoryInstantiationException("PatternFactory cannot handle className: " + className);
      }

      return result;
   }
}
