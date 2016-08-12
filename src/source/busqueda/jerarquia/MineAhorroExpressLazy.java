package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.ModeloAsociacion;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.arbol.SupernodoAdoptivos;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 * TODO No pasa las pruebas
 * @author vanesa.graino
 *
 */
public class MineAhorroExpressLazy extends MineAhorroExpress {
   private static final Logger LOGGER = Logger.getLogger(MineAhorroExpressLazy.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected long[] asociacionesDescartadasLazy;
   protected long[] patronesDescartadosLazy;
   protected long[] tiempoLazy;
   protected int minFreq;

   public MineAhorroExpressLazy(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   // TODO crear solo las asociaicones que frecuencia>minFreq
   // copiar el método de lazy

   /*
    * Version alterada de MineSave para que también se calculen las asociaciones
    * y se añada su frecuencia
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineAhorro#getAnotacionesEventoUtiles(java.util.List, java.util.List, java.util.List, java.lang.String, int)
    */
   protected List<Boolean> getAnotacionesEventoUtiles(List<List<String>> allModels, List<Boolean> utiles,
         List<String> listaEventos, String evento, int tam){
      for(int i=0; i<allModels.size(); i++){
         //Si ya habiamos comprobado que la asociacion es util continuamos
         //if(utiles.get(i)){ continue; }
         // Debido a esta línea cuando estamos analizando las subasociaciones de un candidato
         // no podemos empezar en i+1
         List<String> m = allModels.get(i);

         Nodo nodo = raizArbol.obtenerNodoEnArbol(m);
         SupernodoAdoptivos hijos = (SupernodoAdoptivos)nodo.getHijos();

         List<String> mExt = new ArrayList<String>(m);
         List<Integer> indices = new ArrayList<Integer>();
         for(int j=0;j<listaEventos.size();j++){
            indices.clear();
            String e = listaEventos.get(j);
            int insertionPoint = Collections.binarySearch(mExt, e);
            //La asociacion ya tiene este evento, pasamos
            if(insertionPoint > -1){ continue; }
            insertionPoint = -insertionPoint - 1;
            mExt.add(insertionPoint, e); //se inserta ordenadamente

            //Si existe el hijo/hijo adoptivo en el nodo
            Nodo adoptivo = hijos.getHijo(e, insertionPoint == mExt.size()-1);
            if(adoptivo != null ){
               for(int k=tam;k>=0;k--){
                  if(k == insertionPoint){ continue; }
                  String borrado = mExt.remove(k);
                  // Si el borrado es del evento anotado, no nos sirve
                  if(borrado.equals(evento)){
                     mExt.add(k,borrado);
                     continue;
                  }
                  //Buscamos la anotacion en las anotaciones
                  for(int l=0; l<allModels.size();l++){
                     if(i==l){ continue; }
                     if(allModels.get(l).equals(mExt)){
                        indices.add(l);
                        break;
                     }
                  }

                  //Volvemos a meter el evento
                  mExt.add(k,borrado);
               }
               if(indices.size() >= tam-1){
                  // Si entra aquí hay una posible extensión y por tanto una ocurrencia
                  // de la asociación temporal
                  ((ModeloAsociacion)adoptivo.getModelo()).incrementarSoporte();
                  utiles.set(i,true);
                  for(int iUtil : indices){ utiles.set(iUtil, true); }
                  break;
               }
            }//Fin de busqueda de subasociaciones para mExt

            mExt.remove(insertionPoint);
         }//Fin de extender a m

      }//Fin de recorrer las asociaciones

      return utiles;
   }

   /**
    * Copiado de MineDictionaryLazy3
    * @param anteriores
    * @param tipos
    * @return
    * @throws FactoryInstantiationException
    */
   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas,
           List<String> tipos, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       if(tam == 3){
          super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
          generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
          return;
       }
       //tam>3
       int i,j;
       IAsociacionTemporal modelo;
       String[] modArray;
       GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

       // Inicializar mapa
       resetMapas(tipos);

       Nodo nodoAux = null;

       for(Supernodo supernodo : nivelActual){
          List<Nodo> nodos = supernodo.getListaNodos();
          int nSize = nodos.size();
          for(i=0;i<nSize;i++){
             Nodo padre = nodos.get(i);
             genp.setPadre(padre.getModelo(), 0);
             Supernodo hijos = padre.getHijos();
             for(j=i+1;j<nSize;j++){
                registroT.tiempoAsociaciones(tam-1, true);
                // Construir la asociación temporal
                Nodo madre = nodos.get(j);
                genp.setPadre(madre.getModelo(), 1);
                modArray = genp.getModArray();

                boolean valido=true;
                nodoAux = raizArbol.obtenerNodoEnArbol(modArray);
                //valido = nodoAux != null && nodoAux.getModelo().getSoporte()>=minFreq;
                if(nodoAux == null || nodoAux.getModelo().getSoporte()<minFreq){
                   valido = false;
                   asociacionesDescartadasLazy[tam-1]++;
                   patronesDescartadosLazy[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
                   patronesNoGeneradosNivel[tam-1] += genp.getPatCount()[0]*genp.getPatCount()[1];
                }else{
                   // Aunque tenga la frecuencia es necesario llamar a comprobarSubasociaciones ya que
                   // fijan las variables del array asocBase
                   valido = genp.comprobarSubasociaciones(raizArbol, modArray);
                }

                registroT.tiempoAsociaciones(tam-1, false);

                if(!valido){ continue; }

                // Combinar los patrones
                List<Patron> patrones = genp.generarPatrones(modArray);

                // Construir el modelo si hay patrones
                registroT.tiempoModelo(tam-1, true);
                if(!patrones.isEmpty()){
                   // Hay: añadir punteros en la tabla hash y a candidatas
                   modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize, patrones,
                         numHilos);

                   notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);
                   //El nodo siempre existe en asociaciones de tamaño > 3
                   nodoAux.setModelo(modelo);
                }// else: No hay patrones candidatos: descartar modelo candidato actual
                registroT.tiempoModelo(tam-1, false);
             } // for j
             if(!hijos.getNodos().isEmpty()){
                nuevoNivel.add(hijos);
             }
          } // for i
       } //for supernodos

       LOGGER.info("Descartados por lazy. Asociaciones: " + asociacionesDescartadasLazy[tam-1]
             + ", patrones: " + patronesDescartadosLazy[tam-1]);


       generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);

   }



   @Override
   public void iniciarContadores (int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      asociacionesDescartadasLazy = new long[tSize];
      patronesDescartadosLazy = new long[tSize];
      tiempoLazy = new long[tSize];
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      minFreq = supmin;
      // Continuar normalmente
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }
}
