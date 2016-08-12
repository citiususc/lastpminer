package source.busqueda.episodios;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.busqueda.IBusquedaDiccionarioConEpisodios;
import source.busqueda.IEliminaEventos;
import source.busqueda.jerarquia.MineDictionaryFinalEvent;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.ArbolFactory;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;


public class MineCEDFE extends MineDictionaryFinalEvent implements IBusquedaDiccionarioConEpisodios, IEliminaEventos{
   private static final Logger LOGGER = Logger.getLogger(MineCEDFE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   //Mapa 1: Contiene todos los candidatos generados según el procedimiento normal.
   //Mapa 2: Contiene aquellos candidatos cuya frecuencia debe comprobarse realmente (subconjunto del mapa anterior).

   /*
    * Atributos propios
    */

   // mapa(y candidatas) puede estar vacío, lo que supone que esa iteración se omite, pero mientras mapaGeneradas no esté vacío
   //(y por tanto candidatasGeneradas) el proceso continua
   //protected Map<String,List<IAsociacionTemporal>> mapa; // Contiene aquellos candidatos que se buscarán en el cálculo de frecuencia.
   //protected Map<String,List<IAsociacionTemporal>> mapaGeneradas; // Contiene todos los candidatos que se generaron en la iteración.
   protected List<IAsociacionTemporal> candidatasGeneradas; // ¿Debe estar a este nivel?

   protected List<String> listaTipos;
   protected List<Episodio> listaEpisodios;
   protected List<String> listaTiposNoEpisodios;

   protected long[] patronesNoFrecuentesNivel;

   {
      associationClassName = "ModeloEpisodiosDFE";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   /*
    * Constructores
    */

   public MineCEDFE(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Redefinición de métodos plantilla y métodos propios
    */

   //Se añade el manejo de episodios
   @Override
   protected IAsociacionTemporal crearModelo(String[] mod)
         throws FactoryInstantiationException {
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(mod));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, eps, windowSize,
            getClustering(), numHilos);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones,
         GeneradorPatrones genp) throws FactoryInstantiationException{
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(mod), genp.getAsociacionesBase());
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, eps, windowSize,
            patrones, numHilos);
   }

   @Override
   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod,
         List<IAsociacionTemporal> candidatas,
         Map<String, List<IAsociacionTemporal>> nuevoMapa) {
      notificarModeloGenerado(tam, pSize, modelo, mod, tam==2 || ((IAsociacionConEpisodios)modelo).sonEpisodiosCompletos(),
            candidatas, candidatasGeneradas, mapa);
   }

   ///* Genera patrones candidatos como la combinación de los demás patrones
   // Entrada: lista de modelos frecuentes anteriores, tipos existentes en el registro
   // Precondicion:
   // Poscondición: los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo)
   // * no hay dos modelos que compartan todos los tipos de eventos
   // * los modelos devueltos contienen la información sobre episodios que les corresponde
   // Salida: lista de modelos para la actual iteración


   /*
    * (non-Javadoc)
    * @see source.busqueda.jerarquia.MineArbol#generarCandidatas(int, java.util.List, java.util.List)
    */
   @Override
   protected List<IAsociacionTemporal> generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos) throws FactoryInstantiationException{
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();
      return super.generarCandidatas(tam, anteriores, tipos);
   }



   protected void calcularSoporteTam4(IColeccion coleccion){
      ///*
      //  Asociaciones de tamaño 4, aquí aún no hay ningún tipo de señalización sobre
      // qué asociaciones *con* episodios ocurren en cada ventana. Es necesario tener
      // en cuenta ambos casos.

      int sid = 0;

      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
               for(IAsociacionTemporal aux : receptores){
                  ((IAsociacionDiccionario)aux).actualizaVentana(sid, evento);
               }
               // Se recorre la lista de la tabla hash
               for(IAsociacionTemporal receptor : receptores){
                  //   Procesar únicamente aquellos candidatos que vienen con episodios completos,
                  // y vienen por primera vez. (Si hay tipos de eventos que no provienen de episodios
                  // en la anterior iteración se pudieron buscar adecuadamente.
                  if(((IAsociacionConEpisodios)receptor).sonEpisodiosCompletos()){
                     List<Patron> aux = receptor.getPatrones();
                     List<PatronDictionaryFinalEvent> lista = new ArrayList<PatronDictionaryFinalEvent>();
                     for(Patron patron : aux){
                        lista.add((PatronDictionaryFinalEvent)patron);
                     }
                     ((IAsociacionDiccionario)receptor).recibeEvento(sid, evento, savePatternInstances, lista, encontrados);
                  }
               }
            }
            // Se comprueban las anotaciones que haya de la iteración anterior
            //listatTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            boolean conservar = false;
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     //if(posible==null){ continue; } // En rara ocasión se puede dar
                     if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                        //solo se busca si es completo
                        posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                     }else{
                        conservar=true;
                     }
                  }
               }
               if(conservar){
                  // Añadir la vieja anotación a la lista de anotaciones aceptadas
                  //if(!encontrados.contains(patron)) encontrados.add(patron);
                  encontrados.add(patron);
               }
            }
            ventanaActual.clear();
            ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
            encontrados.clear();
         }
         sid++;
      }
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      // Caso general. Se tiene calculado qué asociaciones podrían ocurrir en cada
      //posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      //de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      //se podrían encontrar en la siguiente iteración en base a las encontradas.
      // Se deben diferenciar dos casos, que la anotación se hiciese en la iteración
      //anterior, o que se hiciese hace 2 iteraciones. En el primer caso, se procede
      //como en MineCE, mientras que en el segundo, hay que saltar dos pasos en la
      //jerarquía de patrones.
      // Además, deben conservarse aquellas anotaciones que son extendidas por alguna
      //asociación temporal que en la actual iteración no se va a buscar, por incluir
      //únicamente un tipo de evento de un episodio en lugar del episodio completo.
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();

            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            if(receptores != null){
               for(IAsociacionTemporal aux : receptores){
                  ((IAsociacionDiccionario)aux).actualizaVentana(sid, evento);
               }
            }
            //listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               // Comprobar de qué tipo de anotación se trata
               boolean conservar=false;
               // Caso a: anotación hecha en la anterior iteración
               if(patron.getTipos().length == tamActual-1){
                  //for(String tipo : listaTipos){
                  //Siempre van a ser asociaciones completas en este primer bucle
                  for(String tipo : listaTiposNoEpisodios){//TODO cambiado, validar
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                     conservar = recibeEventoExtensiones(extensiones, sid, evento, encontrados, conservar);
                  }
                  // En esta parte sólo pueden crearse asociaciones incompletas por lo que si existen
                  // extensiones automáticamente
                  for(int i=0; !conservar && i<listaEpisodios.size(); i++){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(listaEpisodios.get(i).getTipoInicio());
                     if(extensiones!=null && !extensiones.isEmpty()){
                        conservar = true;
                     }
                  }
                  //for(Episodio episodio : listaEpisodios){
                  //   List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(episodio.getTipoInicio());
                  //   conservar = recibeEventoExtensiones(extensiones, sid, evento, encontrados, conservar);
                  //}
                  if(conservar){
                     // Añadir la vieja anotación a la lista de anotaciones aceptadas
                     encontrados.add(patron);
                  }
               }else{
                  // Caso b: anotación hecha hace 2 iteraciones
                  // Comprobación por episodios.
                  for(Episodio episodio : listaEpisodios){
                     List<PatronDictionaryFinalEvent> extensiones1 = patron.getExtensiones(episodio.getTipoInicio());
                     if(extensiones1!=null && !extensiones1.isEmpty()){
                        for(PatronDictionaryFinalEvent intermedio : extensiones1){
                           List<PatronDictionaryFinalEvent> extensiones = intermedio.getExtensiones(episodio.getTipoFin());
                           recibeEventoExtensiones(extensiones, sid, evento, encontrados, conservar);
                        }
                     }
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

   /**
    * @return conservar o no la anotación
    */
   protected boolean recibeEventoExtensiones(List<PatronDictionaryFinalEvent> extensiones, int sid,
         Evento evento, List<Patron> encontrados, boolean conservarAnterior){
      boolean conservar = conservarAnterior;
      if(extensiones!=null && !extensiones.isEmpty()){
         //Nodo nodo = obtenerNodoEnArbol(extensiones.get(0).getTipos());
         //if(nodo==null) continue;
         //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
         IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
         //if(posible==null){ return conservar; }
         if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
            posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
         }else{
            conservar=true;
         }
      }
      return conservar;
   }

   @Override
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
      }else if(tamActual==4){
         calcularSoporteTam4(coleccion);
      }else{ //tam>4
         calcularSoporteGeneral(coleccion, tamActual);
      }
      anotaciones.guardarAnotaciones();
   }

   // Entrada: lista de modelos de esta iteración, soporte mínimo
   // Precondición:
   // Poscondición: se eliminan de candidatasGeneradas y de mapaGeneradas los mismos candidatos
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      int i=0;
      for(i=candidatas.size()-1;i>=0;i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatasGeneradas.remove(modelo);
            //Como se usa el árbol no es necesario borrar de los mapas
            if(raizArbol != null){ raizArbol.eliminarNodoEnArbol(modelo.getTipos()); }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win) {
      return calcularDistribuciones(tipos, coleccion, supmin, win, new ArrayList<Episodio>());
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios) {
      List<List<IAsociacionTemporal>> all = buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win){
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win, new ArrayList<Episodio>(), false);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios){
         return buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, false);
   }

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> actual,
         int win, int cSize) throws FactoryInstantiationException{
      int tSize = tipos.size();
      iniciarContadores(tSize, cSize);
      windowSize = win;

      mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tSize);

      // Crear el supernodo del árbol
      raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);//new Supernodo();
      nivelActual = new ArrayList<Supernodo>();
      nivelActual.add(raizArbol);

      asociacionesNivel[0]=tSize;
      for(String tipo : tipos){
         //Modelo modelo = new Modelo(aux, win, isSavePatternInstances(), getClustering());
         IAsociacionTemporal modelo = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento", tipo, numHilos);
         //Nodo nodo = creaNodoFachada(modelo,raizArbol);
         //raizArbol.addNodo(nodo, tipo);
         creaNodoFachada(modelo, raizArbol, tipo);

         List<IAsociacionTemporal> maux = new ArrayList<IAsociacionTemporal>();
         actual.add(modelo);
         candidatasGeneradas.add(modelo);
         maux.add(modelo);
         mapa.put(tipo, maux);
      }
   }

   /*
    * Se guarda en patronesNoFrecuenteNivel los descartados
    * (non-Javadoc)
    * @see source.busqueda.AbstractMine#calculaPatrones(java.util.List, int, int)
    */
   @Override
   public void calculaPatrones(List<IAsociacionTemporal> candidatos, int supmin, int tam) throws FactoryInstantiationException{
      registroT.tiempoCalcula(tam-1, true);
      for(IAsociacionTemporal modelo : candidatos){
         patronesNoFrecuentesNivel[tam-1] += modelo.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
      registroT.tiempoCalcula(tam-1, false);
   }

   /*
    * Este método es case idéntico al de MineCompleteEpisodes, sólo cambia en la inicialización de las anotaciones
    * y en su guardado al final de cada iteración
    */
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
               int supmin, int win, List<Episodio> episodios, boolean hastaNivel2){
      listaEpisodios = new ArrayList<Episodio>(episodios);

      try{
         int tSize = tipos.size();
         long tiempoAux, inicioTotal, inicioIteracion;
         List<IAsociacionTemporal> actual;
         List<List<IAsociacionTemporal>> todos;

         inicioTotal = System.currentTimeMillis();
         inicioIteracion = inicioTotal;

         todos = new ArrayList<List<IAsociacionTemporal>>();
         if(removePatterns){
            todos.add(null);
         }

         Runtime runtime = Runtime.getRuntime();

         actual = new ArrayList<IAsociacionTemporal>();

         inicializaEstructuras(tipos, actual, win, coleccion.size());
         anotaciones.generarEstructuraAnotaciones(coleccion,2);
         imprimirInstanciasPatrones(LOGGER, "pre-soporte", 1);

         // Cálculo de soporte
         registroT.tiempoSoporte(0, true);
         calcularSoporte(actual,coleccion);
         registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         purgarCandidatas(actual, supmin, 1);
         //memoriaSinPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();

         // Ya sabemos qué tipos de eventos son frecuentes, eliminar los que no lo son
         tipos = purgarTiposYEventos(coleccion, actual, tipos, tSize);
         listaTipos = tipos;
         tSize = tipos.size();

         // Construir la lista de tipos de eventos que no forman parte de un
         // episodio ni como fin ni como inicio
         listaTiposNoEpisodios = new ArrayList<String>(listaTipos);
         for(Episodio e: listaEpisodios){
            listaTiposNoEpisodios.remove(e.getTipoInicio());
            listaTiposNoEpisodios.remove(e.getTipoFin());
         }
         //memoriaConPurgaNivel[0] =  runtime.totalMemory() - runtime.freeMemory();
         imprimirUsoMemoria(LOGGER, 0);

         tiempoAux = System.currentTimeMillis();
         registroT.tiempoIteracion(0, tiempoAux - inicioIteracion);
         inicioIteracion = tiempoAux;

         //Fin tamaño 1

         int tam = 2;
         while(!candidatasGeneradas.isEmpty()){
            if(removePatterns){
               todos.set(0, candidatasGeneradas);
            }else{
               todos.add(candidatasGeneradas);
            }

            llamarGC();
            imprimirInstanciasPatrones(LOGGER, "post-purga", tam);

            if (tam>tipos.size() || hastaNivel2 && tam > 2
                  || tamMaximoPatron!=-1 && tam>tamMaximoPatron) {
               break;
            }

            actual = buscarModelosIteracion(tipos, coleccion, supmin, win, hastaNivel2, candidatasGeneradas, runtime, tSize, tam );

            tiempoAux = System.currentTimeMillis();
            registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
            inicioIteracion = tiempoAux;

            tam++;
         }

         registroT.setTiempoTotal(System.currentTimeMillis() - inicioTotal);

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);

         return todos;

      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }

   @Override
   protected void iniciarContadores(final int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      patronesNoFrecuentesNivel = new long[tSize];
   }

   @Override
   public List<Episodio> getListaEpisodios(){
      return listaEpisodios;
   }


   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize);
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tipos.size());
   }



   /*
    * Se asume que los modelos tienen tamaño 2.
    */
//TODO actualizar
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      if(modelosBase.get(0).size() != 2){
         throw new ModelosBaseNoValidosException("Esta implementación de reiniciarBusqueda solo admite modelos base de tamaño 2");
      }
      try{
         int tSize = tipos.size(), tam = 2;
         Runtime runtime = Runtime.getRuntime();
         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());
         anotaciones.generarEstructuraAnotaciones(coleccion, tam);

         // Guardar lista de episodios
         listaEpisodios = new ArrayList<Episodio>();
         listaEpisodios.addAll(episodios);

         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         todos.add(new ArrayList<IAsociacionTemporal>()); //para las asociaciones de tamaño 1 que no hay

         // El resto del comportamiento debería ser el mismo
         // ¿¿Cambios para manejar la jerarquía?

         // Se asume que todos los modelosBase son frecuentes.
         candidatasGeneradas = modelosBase;
//         registroT.tiempoCandidatas(tam-1, true);
//         generarCandidatas(tam, candidatasGeneradas, tipos);
//         registroT.tiempoCandidatas(tam-1, false);

         //todos.add(modelosBase);
         tam = 3;

         while(!candidatasGeneradas.isEmpty()){
            todos.add(candidatasGeneradas);
            buscarModelosIteracion(tipos, coleccion, supmin, win, false, candidatasGeneradas, runtime, tSize, tam );

            tam++;
         }

         imprimirNiveles(LOGGER, todos);
         imprimirTiempos(LOGGER);

         return todos;
      }catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Problema instanciando en fábrica", e);
      }
      return Collections.emptyList();
   }


   /**
    *
    */
   @Override
   public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return listaTipos;
   }

   /**
    *
    * @param actual
    * @param tiposAmpliar
    * @return
    */
   @Override
   public List<String> posiblesTiposParaAmpliarNoEpisodios(List<Patron> actual, List<String> tiposAmpliar, Evento evento){
      return listaTiposNoEpisodios;
   }

   /**
    *
    * @param episodiosAmpliar
    * @return
    */
   @Override
   public List<Episodio> posiblesEpisodiosParaAmpliar(List<Episodio> episodiosAmpliar, Evento evento){
      return listaEpisodios;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      imprimirParcialesYFrecuentes(resultados, fwp, shortVersion);
   }

   public Map<String, List<IAsociacionTemporal>> getMapaAsociaciones(){
      return mapa;
   }
}
