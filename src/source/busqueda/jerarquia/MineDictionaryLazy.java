package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.DictionaryUtils;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.jerarquia.ModeloDictionary;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Esta versión lazy genera las asociaciones y patrones de tamaño tam+1 e inmediatamente después las asociaciones de tam+2.
 * Cuando realiza el cálculo de soporte de tam+1, también hace el cálculo de tam+2. De esta manera, cuando el tamaño de las
 * asociaciones a generar es 4 o mayor, podemos evitar añadir las asociaciones que no tenían suficiente soporte.
 * Funciona de esta forma:
 * 1. Cálculo del soporte de los tipos de eventos.
 * 2. Generación y cálculo de soporte asociaciones de tam 2. Clustering de distribuciones de frecuencia.
 * 3. Generación candidatos tam 3 y asociaciones tam 4. Cálculo de soporte de ambas y purga.
 * Para TAM>=4
 * 4. Generación candidatos tam TAM teniendo en cuenta la frecuencia de las asociaciones, generación de asociaciones de tam TAM+1.
 * Cálculo de soporte y purga.
 * Hasta que no hay nada nuevo.
 *
 * Esta versión no utiliza las anotaciones!!
 * @author vanesa.graino
 *
 */
public class MineDictionaryLazy extends MineDictionary {
   protected static final String TIEMPOS_LAZY = "lazy";
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryLazy.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   private long[] asociacionesDescartadasLazy;
   private long[] patronesDescartadosLazy;
   //private long[] tiemposLazy;
   private int minFreq;

   /**
    * Mapa de los modelos de la siguiente iteración que se utiliza en el cálculo de soporte
    * para obtener dichos modelos.
    */
   protected Map<String,List<IAsociacionTemporal>> mapaFuturas; // = new HashMap<String, List<IAsociacionTemporal>>();


   {
      associationClassName = "ModeloDictionary";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   /*
    * Constructores
    */

   public MineDictionaryLazy(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      registroT.addOtrosTiempos(TIEMPOS_LAZY);
   }

   @Override
   protected void calcularSoporteTam3(IColeccion coleccion){
      // Asociaciones de tamaño 3, aquí aún no hay ningún tipo de señalización
      //sobre qué asociaciones ocurren en cada ventana.
      int sid = 0, tamActual = 3;
      //long inicio;
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
               ((ModeloDictionary)receptor).actualizaVentana(sid, evento);
               ((ModeloDictionary)receptor).recibeEvento(sid,evento,isSavePatternInstances(),lista,ventanaActual);
            }
            //Futuras
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_LAZY, tamActual-1, true);
            List<IAsociacionTemporal> receptores2 = mapaFuturas.get(evento.getTipo());
            for(IAsociacionTemporal receptor: receptores2){
               receptor.recibeEvento(sid, evento, savePatternInstances);
            }
            //tiemposLazy[tamActual-1] += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_LAZY, tamActual-1, false);
         }
         sid++;
      }
      LOGGER.info("Tiempo de lazy: " + registroT.getTiempos(TIEMPOS_LAZY)[tamActual-1] + " millisec.");
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tam){
      //long inicio;
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

            //Futuras
            //inicio = System.currentTimeMillis();
            registroT.tiempo(TIEMPOS_LAZY, tam-1, true);
            List<IAsociacionTemporal> receptores2 = mapaFuturas.get(evento.getTipo());
            for(IAsociacionTemporal receptor: receptores2){
               receptor.recibeEvento(sid, evento, savePatternInstances);
            }
            //tiemposLazy[tam-1] += System.currentTimeMillis() - inicio;
            registroT.tiempo(TIEMPOS_LAZY, tam-1, false);

            List<Patron> ventanaActual = itVentanaActual.next();

            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal aux : receptores){
               ((ModeloDictionary)aux).actualizaVentana(sid, evento);
            }
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //ModeloDictionary posible = (ModeloDictionary)nodo.getModelo();
                     ModeloDictionary posible = (ModeloDictionary)extensiones.get(0).getAsociacion();
                     posible.recibeEvento(sid,evento,isSavePatternInstances(),extensiones,encontrados);
                  }
               }
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados);
            encontrados.clear();
         }
         sid++;
      }
      LOGGER.info("Tiempo de lazy: " + registroT.getTiempos(TIEMPOS_LAZY)[tam-1] + " millisec.");
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win) throws AlgoritmoException{
      minFreq = supmin;
      // Continuar normalmente
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   private void generarAsociacionesCandidatasSiguientes(List<String> tipos, int tam, List<Supernodo> nuevoNivel) throws FactoryInstantiationException{
      //long inicio = System.currentTimeMillis();
      registroT.tiempo(TIEMPOS_LAZY, tam-1, true);
      int i=0,j=0,l;
      String[] mod;
      IAsociacionTemporal[] asocBase = new IAsociacionTemporal[tam];
      IAsociacionTemporal modelo;

      // Inicializar mapa
      Map<String,List<IAsociacionTemporal>> nuevoMapaFuturas = construyeMapa(tipos.size(), tipos);

      for(Supernodo supernodo : nuevoNivel){

         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0;i<nSize;i++){
            Nodo padre = nodos.get(i);
            asocBase[0] = padre.getModelo();
            Supernodo hijos = padre.getHijos();
            for(j=i+1;j<nSize;j++){
               // Construir la asociación temporal
               Nodo madre = nodos.get(j);
               asocBase[1] = madre.getModelo();
               String tipoNuevo = asocBase[1].getTipos()[tam-2];
               //mod = new ArrayList<String>(asocBase[0].getTipos());
               //mod.add(mod.size(),tipoNuevo);
               mod = Arrays.copyOf(asocBase[0].getTipos(), asocBase[0].getTipos().length+1);
               mod[mod.length-1] = tipoNuevo;

               boolean valido = DictionaryUtils.comprobarSubasociaciones(raizArbol, tam, asocBase, mod);

               if(!valido){ continue; }

               modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloAsociacion", mod, windowSize,
                     getClustering(), numHilos);

               //candidatas.add(modelo);

               nuevoMapaFuturas.get(mod[0]).add(modelo);
               for(l=1;l<tam;l++){
                  if(mod[l] != mod[l-1]){
                     nuevoMapaFuturas.get(mod[l]).add(modelo);
                  }
               }

               // Añadir el Nodo al nuevo
               creaNodoFachada(modelo, hijos, tipoNuevo);
            }
         }
      }
      mapaFuturas = nuevoMapaFuturas;
      //tiemposLazy[tam-1] += System.currentTimeMillis() - inicio;
      registroT.tiempo(TIEMPOS_LAZY, tam-1, false);
   }

   @Override
   protected void generarCandidatasGeneral(int tam, List<IAsociacionTemporal> candidatas, List<String> tipos,
           List<Supernodo> nuevoNivel) throws FactoryInstantiationException{

      if(tam == 3){
         super.generarCandidatasGeneral(tam, candidatas, tipos, nuevoNivel);
         generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);
         return;
      }
      //tam>=3
      int i=0,j=0;
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
                  // Comprobar que las subasociaciones temporales son frecuentes
                  valido = genp.comprobarSubasociaciones(raizArbol, modArray);
               }

               registroT.tiempoAsociaciones(tam-1, false);

               if(!valido){ continue; }

               // Combinar los patrones
               List<Patron> patrones = genp.generarPatrones(modArray);

               // Construir el modelo
               registroT.tiempoModelo(tam-1, true);
               if(!patrones.isEmpty()){
                  // Hay: añadir punteros en la tabla hash y a candidatas
                  modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, windowSize, patrones, numHilos);

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

      //
      LOGGER.info("Descartados por lazy. Asociaciones: " + asociacionesDescartadasLazy[tam-1]
            + ", patrones: " + patronesDescartadosLazy[tam-1]);


      generarAsociacionesCandidatasSiguientes(tipos, tam+1, nuevoNivel);

   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      asociacionesDescartadasLazy = new long[tSize+1];
      patronesDescartadosLazy = new long[tSize+1];
      //tiemposLazy = new long[tSize+1];
   }


   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      fwp.write("\nPatrones y asociaciones descartados por lazy:\n");
      for(int i=0;i<asociacionesDescartadasLazy.length;i++){
         fwp.write(nivel(i) + numberFormat(asociacionesDescartadasLazy[i]) + " asocs. / "
               + numberFormat(patronesDescartadosLazy[i]) + " patrones.\n");
      }

      long[] tiemposLazy = registroT.getTiempos(TIEMPOS_LAZY);
      fwp.write("\nTiempos de lazy:\n");
      for(int i=0;i<tiemposLazy.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposLazy[i]) + "\n");
      }

   }
}

