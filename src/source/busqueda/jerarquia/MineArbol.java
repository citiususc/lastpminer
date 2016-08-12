package source.busqueda.jerarquia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.IBusquedaArbol;
import source.busqueda.Mine;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.DictionaryUtils;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/*
 * Version de ASPTminer con árbol para crear las asociaciones temporales (pero sin anotaciones
 * en los eventos)
 */
public class MineArbol extends Mine implements IBusquedaArbol {

   private static final Logger LOGGER = Logger.getLogger(MineArbol.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   protected Supernodo raizArbol; // La raíz del árbol de enumeración

   protected List<Supernodo> nivelActual;   // Nivel del árbol de enumeración a usar en la actual iteración
   protected List<Supernodo> nivelAnterior; // Nivel anterior del arbol para borrar los patrones de iteraciones
                                             // anteriores cuando removePatterns está activo
   protected String treeClassName = "Supernodo";


   {
      associationClassName = "Modelo";
      patternClassName = "Patron";
   }


   /*
    * Constructors
    */

   public MineArbol(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
           IClustering clustering, boolean removePatterns) {
       super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Otros métodos
    */

   @Deprecated
   @Override
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa) throws FactoryInstantiationException {
      super.generarCandidatasTam2(anteriores, tipos, candidatas, nuevoMapa);
   };

   /**
    * Genera las asociaciones y los patrones candidatos utilizando la estructura de árbol.
    * @param anteriores
    * @param tipos - es necesaria en clases hijas??
    * @param candidatas
    * @param nuevoNivel
    * @throws FactoryInstantiationException
    */
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores, List<String> tipos,
         List<IAsociacionTemporal> candidatas, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      List<Nodo> nodos = raizArbol.getListaNodos();
      final int nSize = nodos.size(), tam=2;
      int i, j;
      IAsociacionEvento padre, madre;
      IAsociacionTemporal modelo;
      String[] modArray;

      mapa = construyeMapa(tipos.size(), tipos);
      for(i=0; i<nSize-1; i++){
         Nodo nodo = nodos.get(i);
         padre = (IAsociacionEvento)nodo.getModelo();
         // Crear supernodo de hijos
         Supernodo hijos = nodo.getHijos();
         for(j=i+1; j<nSize; j++){
            madre = (IAsociacionEvento)nodos.get(j).getModelo();
            modArray = new String[]{ padre.getTipoEvento(), madre.getTipoEvento() };
            modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, candidatas, mapa);
            creaNodoFachada(modelo, hijos, madre.getTipoEvento());
         }
         if(!hijos.getNodos().isEmpty()){
            nuevoNivel.add(hijos);
         }
      }
   }

   //@Override
   protected void resetMapas(List<String> tipos){
      mapa.clear();
      // Inicializar mapa
      for(String tipo : tipos){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
      }
   }

   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
             List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       int i, j;
       IAsociacionTemporal modelo;
       String[] modArray;
       GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

       resetMapas(tipos);
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

                // Comprobar que las subasociaciones temporales son frecuentes
                boolean valido = genp.comprobarSubasociaciones(raizArbol, modArray);
                registroT.tiempoAsociaciones(tam-1, false);
                if(!valido){ continue; }

                // Combinar los patrones
                List<Patron> patrones = genp.generarPatrones(modArray);

                // Construir el modelo
                registroT.tiempoModelo(tam-1, true);
                if(!patrones.isEmpty()){
                   modelo = crearModelo(modArray, patrones, genp);

                   // Hay: añadir punteros en la tabla hash y a candidatas
                   notificarModeloGenerado(tam, patrones.size(), modelo, modArray, candidatas, mapa);
                   // Añadir el Nodo al nuevo
                   //Nodo hijo = creaNodoFachada(modelo,hijos);
                   //hijos.addNodo(hijo, tipoNuevo);
                   creaNodoFachada(modelo, hijos, genp.getTipoNuevo());

                }// else: No hay patrones candidatos: descartar modelo candidato actual
                registroT.tiempoModelo(tam-1, false);

             } // for j
             if(!hijos.getNodos().isEmpty()){
                nuevoNivel.add(hijos);
             }
          } // fin for i
       }
   }

   /** Genera patrones candidatos como la combinación de los demás patrones.
    * Entrada: lista de modelos frecuentes anteriores, tipos existentes en el registro.
    * Precondicion:
    * Poscondición:
    * 1. los modelos devueltos no tienen submodelos no frecuentes.
    * 2. los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo).
    * 3. no hay dos modelos que compartan todos los tipos de eventos.
    * @return lista de modelos para la actual iteración.
    * @throws FactoryInstantiationException
    */
   @Override
   protected List<IAsociacionTemporal> generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos) throws FactoryInstantiationException{
      //int tam = anteriores.get(0).size() + 1;
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      List<Supernodo> nuevoNivel = new ArrayList<Supernodo>();

      if(tam==2){
         generarCandidatasTam2(anteriores, tipos, candidatas, nuevoNivel);
      }else{ // tam>=3
         generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
      }
      // Borrar los patrones de iteraciones anteriores desreferenciandolos
      // para que el garbage collector de Java pueda eliminarlos.
      if(removePatterns && nivelAnterior != null){
         //Igual se podría hacer en el loop anterior
         borrarPatrones();
      }
      nivelAnterior = nivelActual;
      nivelActual = nuevoNivel;
      //setMapa(mapa);
      return candidatas;
   }

   protected void borrarPatrones(){
      for(Supernodo sn : nivelAnterior){
         for(Nodo n : sn.getListaNodos()){
            IAsociacionTemporal modelo = n.getModelo();
            modelo.getPatrones().clear();
         }
      }
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición:
   // Salida: lista de modelos con al menos cobertura mínima
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1;i>=0;i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatas.remove(i); // Eliminar de candidatas
            if(raizArbol != null){
               raizArbol.eliminarNodoEnArbol(modelo.getTipos());
            }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos,
         List<IAsociacionTemporal> actual, int win, int cSize)
         throws FactoryInstantiationException {
      super.inicializaEstructuras(tipos, actual, win, cSize);
      iniciaArbol(tipos);
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize);
      iniciaArbolModelosBase(tipos, modelosBase);
   }


   protected void iniciaArbol(List<String> tipos) throws FactoryInstantiationException{
      nivelActual = new ArrayList<Supernodo>();
      raizArbol = DictionaryUtils.crearArbol(treeClassName, nivelActual, tipos, this);
   }

   protected void iniciaArbolModelosBase(List<String> tipos, List<IAsociacionTemporal> modelosBase) throws FactoryInstantiationException{
      nivelActual = new ArrayList<Supernodo>();
      raizArbol = DictionaryUtils.crearArbol(treeClassName, nivelActual, tipos, this, modelosBase);
   }

   /* Fachadas para crear nodos.  */

   @Override
   public Supernodo getRaizArbol() {
      return raizArbol;
   }

   @Override
   public List<Supernodo> getNivelActual(){
      return nivelActual;
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.IBusquedaDiccionario#creaNodoFachada(source.modelo.IAsociacionTemporal, source.modelo.arbol.Supernodo, java.lang.String)
    */
   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new Nodo(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   /*
    * (non-Javadoc)
    * @see source.busqueda.IBusquedaDiccionario#creaNodoFachada(source.modelo.IAsociacionTemporal)
    */
   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo) throws FactoryInstantiationException{
      return new Nodo(modelo);
   }

}
