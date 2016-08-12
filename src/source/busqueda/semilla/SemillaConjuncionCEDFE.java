package source.busqueda.semilla;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.busqueda.IBusquedaConSemillayEpisodios;
import source.busqueda.episodios.EpisodiosUtils;
import source.busqueda.jerarquia.GeneradorPatronesArbol;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.excepciones.ModelosBaseVaciosException;
import source.excepciones.PatronSemillaNoFrecuenteException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionEvento;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.ArbolFactory;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;

public class SemillaConjuncionCEDFE extends SemillaConjuncionDictionaryFinalEvent implements IBusquedaConSemillayEpisodios{
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionCEDFE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   // mapa(y candidatas) puede estar vacío, lo que supone que esa iteración se omite, pero mientras mapaGeneradas
   // no esté vacío (y por tanto candidatasGeneradas) el proceso continua
   private List<IAsociacionTemporal> candidatasGeneradas; // ¿Debe estar a este nivel?
   protected List<Episodio> listaEpisodios;

   protected long[] patronesNoFrecuentesNivel;


   {
      associationClassName = "ModeloEpisodiosDFE";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   /*
    * Constructores
    */

   public SemillaConjuncionCEDFE(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         boolean saveAllAnnotations, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, saveAllAnnotations, clustering, removePatterns);
   }

   /*
    * Métodos propios
    */

   // Salida: asociación que incluye todos los tipos de eventos de 'nueva', o null si no existía.
   protected IAsociacionTemporal fueSemilla(List<String> nueva, List<IAsociacionTemporal> creadas){
      for(IAsociacionTemporal creada : creadas){
         if(Arrays.asList(creada.getTipos()).containsAll(nueva)){ return creada; }
      }
      return null;
   }

   protected void resetMapas(List<String> tipos){
      //int tSize = tipos.size();
      //mapa = new HashMap<String,List<IAsociacionTemporal>>(tSize);
      //mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tSize);

      mapa.clear();
      //mapaGeneradas.clear();
      for(String tipo : tipos ){
         mapa.put(tipo, new ArrayList<IAsociacionTemporal>());
//         mapaGeneradas.put(tipo,new ArrayList<IAsociacionTemporal>());
      }
   }

   // Poscondición: actualiza el valor de mapa_pares
   // Meter en 'mapaPares' las asociaciones de 'semillas'?
   @Override
   protected List<IAsociacionTemporal> generarCandidatasTam2(List<IAsociacionTemporal> anteriores,
            List<String> tipos, List<IAsociacionTemporal> semillas) throws FactoryInstantiationException{
      List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      final int tam=2, aSize = anteriores.size();
      int i, j;
      IAsociacionEvento padre, madre;

      resetMapas(tipos);

      String[] modArray;
      for(i=0; i<aSize-1; i++){
         padre = (IAsociacionEvento)anteriores.get(i);
         for(j=i+1; j<aSize; j++){
            madre = (IAsociacionEvento)anteriores.get(j);
            modArray = new String[]{ padre.getTipoEvento(), madre.getTipoEvento() };
            if(yaIncluida(Arrays.asList(modArray), semillas)){
               continue;
            }
            IAsociacionTemporal modelo = crearModelo(modArray);
            notificarModeloGenerado(tam, 0, modelo, modArray, true, candidatas, candidatasGeneradas, mapa);
         }
      }
      return candidatas;
   }

   protected IAsociacionTemporal crearModelo(String[] mod) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(mod));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod,
            eps, windowSize, getClustering(), numHilos);
   }

   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(mod), genp.getAsociacionesBase());

      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, eps, windowSize,
            patrones, numHilos);
   }

   ///* Genera patrones candidatos como la combinación de los demás patrones
   // Entrada: lista de modelos frecuentes anteriores, tipos existentes en el registro
   // Precondicion:
   // Poscondición: los modelos devueltos no contienen tipos de eventos repetidos (futuro: permitirlo)
   //* no hay dos modelos que compartan todos los tipos de eventos
   //  * los modelos devueltos contienen la información sobre episodios que les corresponde
   // Salida: lista de modelos para la actual iteración (de tamaño 3 o mayor)
   protected void generarCandidatas(final int tam, List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> candidatas) throws FactoryInstantiationException{
      //int tam = anteriores.get(0).size() + 1;
      int i=0,j=0;
      //List<IAsociacionTemporal> candidatas = new ArrayList<IAsociacionTemporal>();
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();
      List<Supernodo> nuevoNivel = new ArrayList<Supernodo>();

      IAsociacionTemporal modelo;
      String[] modArray;

      GeneradorPatronesArbol genp = new GeneradorPatronesArbol(tam, this);

      resetMapas(tipos);

      for(Supernodo supernodo : nivelActual){
         List<Nodo> nodos = supernodo.getListaNodos();
         int nSize = nodos.size();
         for(i=0; i<nSize; i++){
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
                  // Hay: añadir punteros en la tabla hash y a candidatas


                  //modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, windowSize,
                  //      patrones, eps, isSavePatternInstances(), numHilos);
                  modelo = crearModelo(modArray, patrones, genp);

                  //notificarModeloGenerado(tam, patrones.size(), modelo, modArray, buscar, candidatas,
                  //      candidatasGeneradas, mapa);
                  notificarModeloGenerado(tam, patrones.size(), modelo, modArray, ((IAsociacionConEpisodios)modelo).sonEpisodiosCompletos(), candidatas,
                        candidatasGeneradas, mapa);

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
         } //
      }
      nivelActual = nuevoNivel;
      //return candidatas;
   }

   /**
    * Cálculo de soporte en la tercera iteración
    * @param coleccion
    */
   protected void calcularSoporteTam3(IColeccion coleccion){
      int sid=0;

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

   /**
    * Cálculo de soporte en la cuarta iteración
    * @param coleccion
    */
   protected void calcularSoporteTam4(IColeccion coleccion){
      int sid=0;

      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      for(ISecuencia secuencia : coleccion){
         Iterator<List<Patron>> itVentanaActual = itActual.next().iterator();
         for(Evento evento : secuencia){
            List<Patron> ventanaActual = itVentanaActual.next();
            ventanaActual.clear();
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
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
                  ((IAsociacionDiccionario)receptor).recibeEvento(sid,evento, savePatternInstances,lista,encontrados);
               }
            }

            // Se comprueban las anotaciones que haya de la iteración anterior
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

   /**
    * Cálculo de soporte para iteraciones de tamaño 4 o mayor
    * @param coleccion
    * @param tamActual
    */
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual){
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();
      int sid=0;
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
               // Comprobar de qué tipo de anotación se trata
               boolean conservar=false;
               // Caso a: anotación hecha en la anterior iteración
               if(patron.getTipos().length == tamActual-1){
                  for(String tipo : listaTipos){
                     List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                     conservar = recibeEventoExtensiones(extensiones, sid, evento, encontrados, conservar);
                  }
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
    *
    * @param extensiones
    * @param sid
    * @param evento
    * @param encontrados
    * @param conservarAnterior
    * @return si se conserva o no la anotación
    */
   protected boolean recibeEventoExtensiones(List<PatronDictionaryFinalEvent> extensiones, int sid,
         Evento evento, List<Patron> encontrados, boolean conservarAnterior){
      boolean conservar = conservarAnterior;
      if(extensiones!=null && !extensiones.isEmpty()){
         //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
         //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
         IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
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
   // Salida: lista de modelos con al menos cobertura mínima
   @Override
   protected void purgarCandidatas(List<IAsociacionTemporal> candidatas, int supmin, int tamActual){
      registroT.tiempoPurgar(tamActual-1, true);
      for(int i=candidatas.size()-1;i>=0;i--){
         IAsociacionTemporal modelo = candidatas.get(i);
         if(modelo.necesitaPurga(supmin)){
            candidatasGeneradas.remove(modelo);
            if(raizArbol != null){ raizArbol.eliminarNodoEnArbol(modelo.getTipos()); }
         }else{
            patronesFrecuentesNivel[tamActual-1] += modelo.getPatrones().size();
         }
      }
      registroT.tiempoPurgar(tamActual-1, false);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException {
      List<List<IAsociacionTemporal>> all;
      all = buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, new ArrayList<Episodio>(), true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios) throws SemillasNoValidasException {
      List<List<IAsociacionTemporal>> all =  buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, episodios, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios) throws SemillasNoValidasException {
      return buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win, episodios, false);
   }

   @Override
   public void calculaPatrones(List<IAsociacionTemporal> candidatos, int supmin, int tam) throws FactoryInstantiationException{
      registroT.tiempoCalcula(0, true);
      for(IAsociacionTemporal candidato : candidatos){
         patronesNoFrecuentesNivel[tam-1] += candidato.calculaPatrones(supmin, patternClassName, genID, savePatternInstances);
      }
      registroT.tiempoCalcula(0, false);
   }

   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(mod));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,//"ModeloEpisodios",
            mod, eps, windowSize, patrones, numHilos);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones, int[] distribucion) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
    EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(mod));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,//"ModeloEpisodios",
            mod, eps, windowSize, patrones, distribucion, getClustering(), numHilos);
   }


   // Precondición: Sólo puede haber 1 semilla
   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios,
         boolean hastaNivel2) throws SemillasNoValidasException {
      if(semillas.size()!=1){
            throw new SemillasNoValidasException("Solo puede haber una semilla");
      }
      try{
         List<IAsociacionTemporal> candidatas, anteriores, semFrecuentes;
         String[] tiposSemilla;
         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

         Runtime runtime = Runtime.getRuntime();
         long tiempoAux, inicioIteracion, inicioTotal = System.currentTimeMillis();

         candidatas = new ArrayList<IAsociacionTemporal>();
         tiposSemilla = semillas.get(0).getTipos();

         listaEpisodios = new ArrayList<Episodio>(episodios);

         inicializaEstructuras(tipos, candidatas, win, tiposSemilla, semillas, semNivel, coleccion.size());

         // Calcular qué tipos de eventos y qué patrones semilla son frecuentes
         registroT.tiempoSoporte(0, true);
         calcularSoporteSemilla(coleccion);
         registroT.tiempoSoporte(0, false);
         registrarUsoMemoria(runtime, 1);

         calculaPatrones(candidatas, supmin, 1);
         //memoriaSinPurgaNivel[0] = runtime.totalMemory() - runtime.freeMemory();
         purgarCandidatas(candidatas, supmin, 1);

         // Añadir los tipos de evento del patrón semilla, por compatibilidad
         for(String tipo : tiposSemilla){
            // TODO modelo evento escrito en lugar de parámetro?
            IAsociacionTemporal mod = AssociationFactory.getInstance().getAssociationInstance("ModeloEvento",
                  tipo,numHilos);
            candidatas.add(mod);
            mapa.get(tipo).add(mod);
            //Nodo nodo = creaNodoFachada(mod,raizArbol);
            //raizArbol.addNodo(nodo, tipo);
            creaNodoFachada(mod, raizArbol, tipo);
         }
         // Separar los candidatos frecuentes en semillas frecuentes
         // y tipos de evento frecuentes
         anteriores = new ArrayList<IAsociacionTemporal>();
         semFrecuentes = new ArrayList<IAsociacionTemporal>();
         List<IAsociacionTemporal> pares = new ArrayList<IAsociacionTemporal>();
         procesarSemillas(candidatas, pares, anteriores, semFrecuentes);
         mapa = mapaPares; // ¿Aquí y así? En 'generarCandidatasTam2' se terminaría de actualizar

         semNivel.get(1).addAll(pares);
         todos.add(anteriores);
         if(semFrecuentes.isEmpty()){
            LOGGER.info("semFrecuentes vacío");
            //return todos;
            throw new PatronSemillaNoFrecuenteException();
         }

         // Actualizar la lista de tipos para que solo incluya los frecuentes
         tipos = purgarTiposYEventos(coleccion, anteriores, tipos);

         imprimirUsoMemoria(LOGGER, 0);

         int tam = 2;

         // Obtener pares de tipos de eventos frecuentes (no semilla)
         // y con qué disposiciones temporales son frecuentes (clustering)
         registroT.tiempoCandidatas(tam-1, true);
         //Se crean las candidatas de tamaño 2 que no son semilla
         candidatas = generarCandidatasTam2(anteriores, tipos, semNivel.get(1));
         registroT.tiempoCandidatas(tam-1, false);

         // Comprobar que los nuevos patrones generados para la semilla son frecuentes
         if(!candidatas.isEmpty()){
            registroT.tiempoSoporte(tam-1, true);
            calcularSoporte(candidatas,coleccion);
            registroT.tiempoSoporte(tam-1, false);
            calculaPatrones(candidatas, supmin, tam);
            purgarCandidatas(candidatas, supmin, tam);
         }

         imprimirUsoMemoria(LOGGER, tam-1);

         // Caso general
         // Empieza el procedimiento iterativo. Cada iteración i representa un tamaño i
         // de candidatos. El procedimiento continua mientras queden semillas que introducir
         // O extensiones de semillas que generar.
         // Solo interesan los patrones derivados de la semilla
         if(!semNivel.get(1).isEmpty()){
            todos.add(semNivel.get(1));
         }else{
            todos.add(new ArrayList<IAsociacionTemporal>()); // Esto NUNCA se debería dar
         }
         // Localizar semillas
         int nivelSem = 1;
         // Añadir los patrones semilla a los resultados
         todos.get(todos.size()-1).addAll(candidatas);
         candidatas = todos.get(todos.size()-1);
         candidatasGeneradas = candidatas;
         //mapaGeneradas = mapaPares;

         //Construir Arbol
         anotaciones.generarEstructuraAnotaciones(coleccion,tam);

         inicioIteracion = System.currentTimeMillis();

         tam = 3;

         while(!candidatasGeneradas.isEmpty()){
            anteriores = candidatasGeneradas;

            candidatas = buscarModelosIteracion(tipos, coleccion, supmin, win, hastaNivel2,
                  candidatasGeneradas, semNivel.get(nivelSem), runtime, tam);

            if(!candidatasGeneradas.isEmpty()){
               todos.add(candidatasGeneradas);
            }
            tiempoAux = System.currentTimeMillis();
            registroT.tiempoIteracion(tam-1, tiempoAux - inicioIteracion);
            inicioIteracion = tiempoAux;
            //LOGGER.log(Level.INFO, "{0}. Tiempo para iteración " + tam + ": "
            //		+ registroT.getTiemposIteracion()[tam-1], getExecutionId());

            nivelSem++;
            tam++;


            if(hastaNivel2 ||
                  (tamMaximoPatron != -1 && tam>tamMaximoPatron)){
               break;
            }
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
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos,
         int win, String[] tiposSemilla, List<ModeloSemilla> semillas,
         List<List<IAsociacionTemporal>> semNivel, int cSize) throws FactoryInstantiationException{
      //Equivalente a
      super.inicializaEstructuras(tipos, candidatos, win, tiposSemilla, semillas, semNivel, cSize);

      //todasAnotaciones = new ArrayList<List<List<List<Patron>>>>();
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>();
      //mapaGeneradas = new HashMap<String,List<IAsociacionTemporal>>(tipos.size());

      // Crear el supernodo del árbol
      raizArbol = ArbolFactory.getInstance().getSupernodo(treeClassName);//new Supernodo();
      nivelActual = new ArrayList<Supernodo>();
      nivelActual.add(raizArbol);

      for(IAsociacionTemporal mod: candidatos){
         //Nodo nodo = creaNodoFachada(mod,raizArbol);
         //raizArbol.addNodo(nodo, mod.getTipos().get(0));
         creaNodoFachada(mod, raizArbol, mod.getTipos()[0]);
         candidatasGeneradas.add(mod);
      }
//      for(String key:mapa.keySet()){
//         mapaGeneradas.put(key, new ArrayList<IAsociacionTemporal>());
//         mapaGeneradas.get(key).addAll(mapa.get(key));
//      }
      //mapaGeneradas.putAll(mapa); //Es lo mismo el bucle anterior que esto?
   }

   @Override
   protected void iniciarContadores(final int tSize, int cSize){
      super.iniciarContadores(tSize, cSize);
      patronesNoFrecuentesNivel = new long[tSize];
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win) throws SemillasNoValidasException{
      return buscarModelosFrecuentes(tipos,coleccion,semillas,supmin,win,new ArrayList<Episodio>(0));
   }

//TODO actualizar
   @Override
   protected void iniciarEstructurasReinicio(List<String> tipos, List<IAsociacionTemporal> modelosBase,
         int win, int cSize) throws FactoryInstantiationException{
      super.iniciarEstructurasReinicio(tipos, modelosBase, win, cSize);
      candidatasGeneradas = new ArrayList<IAsociacionTemporal>(tipos.size());
   }

   /*
    * Se asume que los modelos tienen tamaño 2.
    */
   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win, List<Episodio> episodios) throws ModelosBaseNoValidosException{
      if(modelosBase==null || modelosBase.isEmpty()){
         throw new ModelosBaseVaciosException();
      }
      if(modelosBase.get(0).getTipos().length != 2){
         throw new ModelosBaseNoValidosException("Esta implementación de reiniciarBusqueda solo admite modelos base de tamaño 2");
      }
      try{
         //pColeccion = coleccion;
         iniciarEstructurasReinicio(tipos, modelosBase, win, coleccion.size());
         anotaciones.generarEstructuraAnotaciones(coleccion, 2);

         // Guardar lista de episodios
         listaEpisodios = new ArrayList<Episodio>();
         listaEpisodios.addAll(episodios);

         // El resto del comportamiento no es el mismo, todos los modelso se manejan como
         // candidatos no como extensiones de semillas

         List<List<IAsociacionTemporal>> todos = new ArrayList<List<IAsociacionTemporal>>();
         todos.add(new ArrayList<IAsociacionTemporal>()); //para las asociaciones de tamaño 1 que no hay

         // Se asume que todos los modelosBase son frecuentes.

         long tiempo;
         Calendar diferencia = new GregorianCalendar();
         candidatasGeneradas = modelosBase;

         int tam = 3;
         List<IAsociacionTemporal> candidatas;

         while(!candidatasGeneradas.isEmpty()){
            todos.add(candidatasGeneradas);

            registroT.tiempoCandidatas(tam-1, true);
            candidatas = generarCandidatas(tam, candidatasGeneradas, tipos);
            registroT.tiempoCandidatas(tam-1, false);

            registroT.tiempoSoporte(tam-1, true);
            calcularSoporte(candidatas,coleccion);
            tiempo = registroT.tiempoSoporte(tam-1, false);
            diferencia = new GregorianCalendar();
            diferencia.setTimeInMillis(tiempo);
            imprimirTiempo("soporte", LOGGER, tam, diferencia);

            calculaPatrones(candidatas, supmin, tam);

            purgarCandidatas(candidatas, supmin, tam);

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

   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win) throws ModelosBaseNoValidosException{
      return reiniciarBusqueda(tipos,coleccion,modelosBase,supmin,win,new ArrayList<Episodio>(0));
   }



   @Override
   public List<Episodio> getListaEpisodios(){
      return listaEpisodios;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException {
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      imprimirParcialesYFrecuentes(resultados, fwp, shortVersion);
   }
}