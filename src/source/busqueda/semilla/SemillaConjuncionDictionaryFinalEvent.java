package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.busqueda.IBusquedaAnotaciones;
import source.busqueda.IBusquedaArbol;
import source.busqueda.jerarquia.Anotaciones;
import source.busqueda.jerarquia.GeneradorPatronesArbol;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.DictionaryUtils;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class SemillaConjuncionDictionaryFinalEvent extends SemillaConjuncion implements IBusquedaArbol, IBusquedaAnotaciones {
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionDictionaryFinalEvent.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   /*
    * Atributos propios
    */

   protected Supernodo raizArbol; // La raíz del árbol de enumeración
   protected List<Supernodo> nivelActual; // Nivel del árbol de enumeración a usar en la actual iteración
   protected Anotaciones anotaciones;
   protected String treeClassName;

   {
      treeClassName = "Supernodo";
      associationClassName = "ModeloDictionaryFinalEvent";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   public SemillaConjuncionDictionaryFinalEvent(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, boolean saveAllAnnotations, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      anotaciones = new Anotaciones(saveAllAnnotations);
   }

   /**
    * En base a unas anotaciones calcula la lista de eventos con los que extender los patrones
    * de dichas anotaciones.
    * @param actual - Las anotaciones del evento actual
    * @param tiposAmpliar - La lista en la que se guardan los tipos para ampliar.
    * Tiene que estar inicializada
    */
   protected List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return anotaciones.posiblesTiposParaAmpliar(actual, tiposAmpliar);
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición:
   // Salida: lista de modelos con al menos cobertura mínima
   //private void comprobarSoporte(List<Modelo> candidatas, int supmin){
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1; i>=0; i--){
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

   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones ocurren en cada ventana.
      int sid = 0;
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            ventanaActual.clear();
            // Se recorre la lista de la tabla hash
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               List<Patron> aux = receptor.getPatrones();
               List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
               for(Patron patron : aux){
                  lista.add((PatronDictionaryFinalEvent)patron);
               }
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
               ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,ventanaActual);
            }
         }
         sid++;
      }
   }

   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      // Caso general. Se tiene calculado qué asociaciones podrían ocurrir en cada
      //posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      //de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      //se podrían encontrar en la siguiente iteración en base a las encontradas.
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();

            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal aux : receptores){
               ((IAsociacionDiccionario)aux).actualizaVentana(sid, evento);
            }
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  }
               }
            }

            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
   }

   //@Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatas.isEmpty()){
         anotaciones.guardarAnotaciones();
         return;
      }
      int tamActual = candidatas.get(0).size();
      if(tamActual<3){
         super.calcularSoporte(candidatas, coleccion);
         return;
      }else if(tamActual==3){
         calcularSoporteTam3(coleccion);
      }else{
         calcularSoporteGeneral(coleccion, tamActual);
      }
      anotaciones.guardarAnotaciones();
   }

   // PROBLEMA: NO COMPROBADO PARA TIPOS REPETIDOS
   // Precondición: Debe ser llamado ANTES de generarCandidatas, crea 'nuevoMapa'
   //   si no se hiciese, 'mapa' acabaría incompleto
   // Salida: cada extensión de una semilla es el resultado de añadirle un tipo de
   //  evento nuevo a la semilla
   @Override
   protected List<IAsociacionTemporal> extenderSemillas(List<IAsociacionTemporal> semillas, List<String> tipos){
      List<IAsociacionTemporal> extensiones = new ArrayList<IAsociacionTemporal>();
      int sSize = semillas.size();
      Map<String,List<IAsociacionTemporal>> nuevoMapa = construyeMapa(tipos.size(), tipos);

      if(sSize<=0){
         return extensiones;
      }
      int tamAnt = semillas.get(0).size();
      //int tam = tamAnt+1;

      // Construir el segundo nivel del árbol de enumeración de asociaciones temporales
      List<Supernodo> nuevoNivel = new ArrayList<Supernodo>();
      for(IAsociacionTemporal semilla : semillas){
         List<String> cadenaTipos = Arrays.asList(semilla.getTipos()).subList(0,tamAnt-1);
         Nodo padre = raizArbol.obtenerNodoEnArbol(cadenaTipos); // Nodo del padre
         if(padre!=null){
            Nodo hijo = creaNodoFachada(semilla);
            padre.addHijo(hijo, semilla.getTipos()[tamAnt-1]);
         }
      }
      for(Supernodo supernodo : nivelActual){
         for(Nodo nodo : supernodo.getListaNodos()){
            nuevoNivel.add(nodo.getHijos());
         }
      }
      nivelActual = nuevoNivel;

      setNuevoMapa(nuevoMapa);
      return extensiones;

   }

   protected void generarCandidatasTam2(List<IAsociacionTemporal> candidatas,
         List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      List<Nodo> nodos = raizArbol.getListaNodos();
      final int tam = 2, nSize = nodos.size();
      int i, j;
      String[] modArray;
      IAsociacionEvento padre, madre;
      for(i=0; i<nSize-1; i++){
         Nodo nodo = nodos.get(i);
         padre = (IAsociacionEvento)nodo.getModelo();
         // Crear supernodo de hijos
         Supernodo hijos = nodo.getHijos();
         for(j=i+1; j<nSize; j++){
            madre = (IAsociacionEvento)nodos.get(j).getModelo();
            String tipo = madre.getTipoEvento();
            modArray = new String[]{ padre.getTipoEvento(), tipo};
            IAsociacionTemporal modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, candidatas, mapa);
            creaNodoFachada(modelo, hijos, tipo);
         }
         if(!hijos.getNodos().isEmpty()){
            nuevoNivel.add(hijos);
         }
      }
   }


   protected void resetMapas(List<String> tipos){
      mapa.clear();
      for(String tipo : tipos ){
         mapa.put(tipo,new ArrayList<IAsociacionTemporal>());
      }
   }

   //@Override //Es el original
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
             List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
       int i, j;
       IAsociacionTemporal modelo;
       String[] mod;

       resetMapas(tipos);

       GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

       for(Supernodo supernodo : nivelActual){
          List<Nodo> nodos = supernodo.getListaNodos();
          int nSize = nodos.size();
          for(i=0; i<nSize; i++){
             Nodo padre = nodos.get(i);
             genp.setPadre(padre.getModelo(), 0);
             Supernodo hijos = padre.getHijos();
             for(j=i+1; j<nSize; j++){
                registroT.tiempoAsociaciones(tam-1, true);
                // Construir la asociación temporal
                Nodo madre = nodos.get(j);
                genp.setPadre(madre.getModelo(), 1);
                mod = genp.getModArray();

                // Comprobar que las subasociaciones temporales son frecuentes
                boolean valido = genp.comprobarSubasociaciones(raizArbol, mod);
                registroT.tiempoAsociaciones(tam-1, false);

                if(!valido){ continue; }

                // Combinar los patrones
                List<Patron> patrones = genp.generarPatrones(mod);

                // Construir el modelo
                registroT.tiempoModelo(tam-1, true);
                if(!patrones.isEmpty()){
                   // Hay: añadir punteros en la tabla hash y a candidatas
                   modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, windowSize, patrones, numHilos);

                   notificarModeloGenerado(tam, patrones.size(), modelo, mod, candidatas, mapa);
                   //setModeloPatrones(patrones, modelo);

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
          } // for i
       } // if tam>=3
       //mapa = nuevoMapa; // Actualizar mapa global
   }

   ///* Genera patrones candidatos como la combinación de los demás patrones
   // Entrada: lista de modelos frecuentes anteriores, tipos existentes en el registro
   // Precondicion:
   // Poscondición: los modelos devueltos no tienen submodelos no frecuentes
   // * los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo)
   // * no hay dos modelos que compartan todos los tipos de eventos
   // Salida: lista de modelos para la actual iteración
   protected List<IAsociacionTemporal> generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos) throws FactoryInstantiationException {
      //int tam = anteriores.get(0).size() + 1;
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      List<Supernodo> nuevoNivel = new ArrayList<Supernodo>();

      if(tam==2){
         generarCandidatasTam2(candidatas, nuevoNivel);
      }else{ // tam>=3
         generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
      }
      nivelActual = nuevoNivel;
      return candidatas;
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos,
         int win, String[] tiposSemilla, List<ModeloSemilla> semillas,
         List<List<IAsociacionTemporal>> semNivel, int cSize) throws FactoryInstantiationException{
      super.inicializaEstructuras(tipos, candidatos, win, tiposSemilla, semillas, semNivel, cSize);
      iniciaArbol(tipos);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws AlgoritmoException{
      anotaciones.generarEstructuraAnotaciones(coleccion,2);
      return super.buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win);
   }

   protected void iniciaArbol(List<String> tipos) throws FactoryInstantiationException{
      nivelActual = new ArrayList<Supernodo>();
      raizArbol = DictionaryUtils.crearArbol(treeClassName, nivelActual, tipos, this);
   }

   protected void iniciaArbolModelosBase(List<String> tipos,
         List<IAsociacionTemporal> modelosBase) throws FactoryInstantiationException{
      nivelActual = new ArrayList<Supernodo>();
      raizArbol = DictionaryUtils.crearArbol(treeClassName, nivelActual, tipos, this, modelosBase);
   }

   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize);
      iniciaArbolModelosBase(tipos, modelosBase);
   }

   /*
    * Se asume que los modelos tienen tamaño 2.
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException{
      anotaciones.generarEstructuraAnotaciones(coleccion, 2);
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }

   /* Fachadas para crear nodos. Exactamente igual al de MineDictionary */

   public Supernodo getRaizArbol() {
      return raizArbol;
   }

   public List<Supernodo> getNivelActual(){
      return nivelActual;
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new Nodo(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo){
      return new Nodo(modelo);
   }

   public Iterator<List<Patron>> getActualIterator(int sid) {
      return anotaciones.getActual().get(sid).iterator();
   }

   @Override
   public Anotaciones getAnotaciones(){
      return anotaciones;
   }

   @Override
   public void setSaveAllAnnotations(boolean saveAllAnnotations) {
      anotaciones.setSaveAllAnnotations(saveAllAnnotations);
   }
}