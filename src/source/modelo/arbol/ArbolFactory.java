package source.modelo.arbol;

import source.excepciones.FactoryInstantiationException;

public class ArbolFactory {

   static class ArbolFactoryHolder{
      private static final ArbolFactory INSTANCE = new ArbolFactory();
   }

   //Singleton
   protected ArbolFactory(){
      //proteger constructor para singleton
   }

   public static ArbolFactory getInstance(){
      return ArbolFactoryHolder.INSTANCE;
   }

   public Supernodo getSupernodo(String className) throws FactoryInstantiationException{
      Supernodo result = null;
      className = className.intern();
      if(className == "Supernodo"){
         result = new Supernodo();
      }else if(className == "SupernodoAdoptivos"){
         result = new SupernodoAdoptivos();
      }else if(className == "SupernodoAdoptivosAnotados"){
         result = new SupernodoAdoptivosAnotados();
      }else if(className == "SupernodoNegacion"){
         result = new SupernodoNegacion();
      }else if(className == "SupernodoNegacionSufijo"){
         result = new SupernodoNegacionSufijo();
      }else{
         throw new FactoryInstantiationException("getSupernodo (1) cannot handle className :" + className);
      }
      return result;

   }

   public Supernodo getSupernodo(String className, Nodo padre) throws FactoryInstantiationException{
      Supernodo result = null;
      className = className.intern();
      if(className == "Supernodo"){
         result = new Supernodo(padre);
      }else if(className == "SupernodoAdoptivos"){
         result = new SupernodoAdoptivos(padre);
      }else if(className == "SupernodoAdoptivosAnotados"){
         result = new SupernodoAdoptivosAnotados(padre);
      }else if(className == "SupernodoNegacion"){
         result = new SupernodoNegacion(padre);
      }else if(className == "SupernodoNegacionSufijo"){
         result = new SupernodoNegacionSufijo(padre);
      }else{
         throw new FactoryInstantiationException("getSupernodo (1) cannot handle className :" + className);
      }
      return result;

   }

}
