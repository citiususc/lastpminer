package source.busqueda.jerarquia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Logger;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionDiccionario;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;
import source.patron.PatronAnotaciones;
import source.patron.PatronDictionaryFinalEvent;

/**
 * Esta versión del algoritmo utiliza una estrategia que es una extensión de {@link MineDictionaryExpress}.
 * En este caso se buscan todos los padres de un patron antes de buscarlo. Es decir, como antes, se buscan
 * tam-1 padre en las anotaciones del evento y, si están, se buscan el padre que falta en el resto de la ventana
 * (funcion purgeCandidatesWindow).
 * @author vanesa.graino
 *
 */
public class MineAnotaciones extends MineDictionaryExpress {
   private static final Logger LOGGER = Logger.getLogger(MineAnotaciones.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   private int[] extensionesEvitadasVentana;
   //private int[] extensionesEvitadas;
   //private long[] tiemposPurgaExtensiones;

   int[] patronesIteracion;
   int[] patronesMasEspecificosIteracion;

   {
      patternClassName = "PatronAnotaciones";
   }

   public MineAnotaciones(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents,
            clustering, removePatterns);
   }

   @Override
   protected void iniciarContadores(int tSize, int cSize) {
      super.iniciarContadores(tSize, cSize);
      extensionesEvitadasVentana = new int[tSize];
      //extensionesEvitadas = new int[tSize];
      //tiemposPurgaExtensiones = new long[tSize];

      patronesIteracion = new int[tSize];
      patronesMasEspecificosIteracion = new int[tSize];
   }

   protected void notificarModeloGenerado(int tam, int pSize,
         IAsociacionTemporal modelo, String[] mod, List<IAsociacionTemporal> candidatas,
         Map<String,List<IAsociacionTemporal>> nuevoMapa){
      super.notificarModeloGenerado(tam, pSize, modelo, mod, candidatas, nuevoMapa);
      for(Patron p:modelo.getPatrones()){
         if(((PatronAnotaciones)p).esMasEspecifico()){
            patronesMasEspecificosIteracion[tam-1]++;
         }
         patronesIteracion[tam-1]++;
      }
   }

   @Override
   protected List<IAsociacionTemporal> generarCandidatas(final int tam,
         List<IAsociacionTemporal> anteriores, List<String> tipos)
         throws FactoryInstantiationException {
      List<IAsociacionTemporal> generadas = super.generarCandidatas(tam, anteriores, tipos);
      //int tam = anteriores.get(0).size() + 1;
      LOGGER.info("Patrones generados " + patronesIteracion[tam-1] + ", de los que "
            + patronesMasEspecificosIteracion[tam-1] + " son más específicos.");
      return generadas;
   }

   @Override
   protected void calcularSoporteGeneral(IColeccion coleccion, int tamActual) {
      // Caso general.
      // Se actualizan las ventanas de los posibles receptores.
      //
      // Se tiene calculado qué asociaciones podrían ocurrir en cada
      // posible ventana temporal. Para cada evento leído, se comprueba qué asociaciones
      // de entre las posibles realmente ocurren y, finalmente, se calcula qué asociaciones
      // se podrían encontrar en la siguiente iteración en base a las encontradas.
      Iterator<List<List<Patron>>> itActual = anotaciones.getActual().iterator();
      List<Patron> encontrados = new ArrayList<Patron>();
      List<Integer> faltantes;
      ListIterator<Evento> itInicioVentana;
      //List<List<Patron>> anotacionesSecuencia;
      ListIterator<List<Patron>> itVentanaActual;
      List<String> listaTipos = new ArrayList<String>();
      Deque<List<Patron>> anotacionesIteracionAnterior = new ArrayDeque<List<Patron>>();
      int sid = 0;
      for(ISecuencia secuencia : coleccion){
        // System.out.println("Secuencia #" + sid + "\n");
         itVentanaActual = itActual.next().listIterator();
         //anotacionesSecuencia = itAnotacionesColeccion.next();
         itInicioVentana = secuencia.listIterator();
         int inicioVentanaIndex = 0;
         //eid=-1;
         for(Evento evento : secuencia){
            //eid++;
            //Salen de la ventana los eventos que quedan fuera
            while(itInicioVentana.next().getInstante() <= (evento.getInstante() - windowSize - 1)){
               inicioVentanaIndex++;
               anotacionesIteracionAnterior.removeFirst();
            }
            //Evento auxEvento = itInicioVentana.previous();

            List<Patron> ventanaActual = itVentanaActual.next();
            List<Integer> patternIDs = new ArrayList<Integer>();
            for(Patron anotacion: ventanaActual){
               patternIDs.add(anotacion.getID());
            }
            Collections.sort(patternIDs); //esto es necesario
            // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
            List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
            for(IAsociacionTemporal receptor : receptores){
               ((IAsociacionDiccionario)receptor).actualizaVentana(sid, evento);
            }
            // Calcular las asociaciones temporales a comprobar para el evento actual
            listaTipos = posiblesTiposParaAmpliar(ventanaActual, listaTipos);
            for(Patron aux : ventanaActual){
               PatronDictionaryFinalEvent patron = (PatronDictionaryFinalEvent)aux;
               for(String tipo : listaTipos){
                  List<PatronDictionaryFinalEvent> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = raizArbol.obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     faltantes = new ArrayList<Integer>(Collections.nCopies(extensiones.size(), (Integer)null));
                     extensiones = purgeCandidates(extensiones, patternIDs, faltantes, tamActual);
                     if(extensiones.isEmpty()){ continue; }
                     purgeCandidatesWindow(extensiones, faltantes, secuencia.listIterator(inicioVentanaIndex),
                           anotacionesIteracionAnterior.iterator(), evento,tamActual);
                     if(extensiones.isEmpty()){ continue; }
                     IAsociacionDiccionario posible = (IAsociacionDiccionario)extensiones.get(0).getAsociacion();
                     //IAsociacionDiccionario posible = (IAsociacionDiccionario)nodo.getModelo();
                     posible.recibeEvento(sid,evento, savePatternInstances,extensiones,encontrados);
                  }
               }
            }
            itVentanaActual.set(encontrados);
            //encontrados = ventanaActual;//para no crear un nuevo objeto
            //encontrados.clear();

            anotacionesIteracionAnterior.push(ventanaActual);
            encontrados = new ArrayList<Patron>();
            //anotacionesIteracionAnterior.push(new ArrayList<Patron>(ventanaActual));

         }
         sid++;
      }
      LOGGER.info("Extensiones evitadas: " + extensionesEvitadas[tamActual-1]);
      LOGGER.info("Extensiones evitadas ventana: " + extensionesEvitadasVentana[tamActual-1]);
   }


   /**
    * Precondición: aComprobar no está vacío.
    * @param aComprobar - los patrones de los que se van a buscar ocurrencias.
    * @param faltantes - las anotaciones que falta por asegurar
    * @param ventana - un ListIterator para recorrer la ventana hasta el evento actual
    * @param anotaciones - un ListIterator sobre las anotaciones de la ventana
    * @param eventoActual - el evento que se está procesando
    * @param tam - la iteración o tamaño de los candidatos actuales
    */
   protected void purgeCandidatesWindow(List<PatronDictionaryFinalEvent> aComprobar,
         List<Integer> faltantes, ListIterator<Evento> ventana, Iterator<List<Patron>> anotaciones,
         Evento eventoActual, int tam){
            List<PatronDictionaryFinalEvent> especificos = new ArrayList<PatronDictionaryFinalEvent>();
      ListIterator<Integer> itFaltantes = faltantes.listIterator();
      ListIterator<PatronDictionaryFinalEvent> itAComprobar = aComprobar.listIterator();
      // Se comprueba de los patrones de aComprobar cuales son más específicos
      while(itAComprobar.hasNext()){
         PatronDictionaryFinalEvent p = itAComprobar.next();
         itFaltantes.next();
         if(((PatronAnotaciones)p).esMasEspecifico()){
            especificos.add(p);
            itAComprobar.remove();
         }else{
            itFaltantes.remove();
         }
      }
      if(especificos.isEmpty()){ return; }

      List<Patron> anotacionesEv;
      Evento ev;
      //System.out.println("Mas especificos: " + especificos.size() + ", no específicos: " + aComprobar.size());
      //TODO podemos saltar tam-2 eventos

      while(ventana.hasNext() && !faltantes.isEmpty()){
         ev = ventana.next();
         if(ev.equals(eventoActual)){ break; }
         anotacionesEv = anotaciones.next();
         for(Patron p : anotacionesEv){
            int index = faltantes.indexOf(p.getID());
            //se ha encontrado una anotación que faltaba (puede ser el padre de más de una)
            ///int entra = 0;
            while(index>=0){
               //entra++;
               //aComprobar.remove(index);
               //faltantes.remove(index);
               faltantes.set(index, null);
               index = faltantes.indexOf(p.getID());
            }
            //if(entra>0) System.out.println("Entra: " + entra);
         }
      }

      //Borramos candidatos a los que sigue faltándole una anotación padre
      for(int i=0;i<faltantes.size();i++){
         if(faltantes.get(i)!=null){ //la anotación faltante no se encontró
            especificos.remove(i);
            faltantes.remove(i);
            i--;
            extensionesEvitadasVentana[tam-1]++;
         }
      }

      //Añadimos los específicos
      aComprobar.addAll(especificos);
   }

   /**
    * Se purgan los candidatos que no tienen los suficientes padres en las anotaciones.
    * @param aComprobar - lista inicial de patrones a comprobar
    * @param patternIDs - ids de las anotaciones del evento
    * @param faltantes - una array del mismo tamaño que aComprobar inicializado a valores nulos.
    * @return
    */
   protected List<PatronDictionaryFinalEvent> purgeCandidates(List<PatronDictionaryFinalEvent> aComprobar,
         List<Integer> patternIDs, List<Integer> faltantes, int tam){
      List<PatronDictionaryFinalEvent> aComprobarFinal = new ArrayList<PatronDictionaryFinalEvent>(aComprobar);
      //Ultimo índice en cada padre
      List<Integer> indices = new ArrayList<Integer>(Collections.nCopies(aComprobarFinal.size(), 0));

      ListIterator<PatronDictionaryFinalEvent> itAComprobar;
      List<PatronDictionaryFinalEvent> padres;
      Integer id, padreId;
      for(int i=0,x=patternIDs.size();i<x && !aComprobarFinal.isEmpty();i++){
         id = patternIDs.get(i);
         itAComprobar = aComprobarFinal.listIterator();
         int j=0;
         while(itAComprobar.hasNext()){
            padres = itAComprobar.next().getPadres();
            if(indices.get(j) == padres.size()){ continue; }
            padreId = padres.get(indices.get(j)).getID();
            int compare = id.compareTo(padreId);
            // permitimos que falte una anotacion y guardamos constancia de ello
            if(compare>0 && faltantes.get(j) == null){
               faltantes.set(j, padreId);
               //seguimos con el siguiente padre que ya no puede faltar
               indices.set(j, indices.get(j)+1);
               padreId = padres.get(indices.get(j)).getID();
               compare = id.compareTo(padreId);
            }
            // la anotacion no contiene un segundo padre: se borra el candidato
            if(compare>0){
               indices.remove(j);
               itAComprobar.remove();//aComprobarFinal.remove(j);
               faltantes.remove(j);
               extensionesEvitadas[tam-1]++;
               continue; //j--;
            }else if(compare==0){//aumentar índice
               //la anotacion es el padre actual
               indices.set(j, indices.get(j)+1);
            }
            j++;
         }
      }
      return aComprobarFinal;
   }

   @Override
   public void escribirEstadisticasEstrategia(List<List<IAsociacionTemporal>> resultados,
         Writer fwp, boolean shortVersion, int maxIteracion) throws IOException{
      super.escribirEstadisticasEstrategia(resultados, fwp, shortVersion, maxIteracion);

      fwp.write(SEPARADOR);

      long[] tiemposPurgaExtensiones = registroT.getTiempos(TIEMPOS_PURGA_EXTENSIONES);
      fwp.write("\nTiempos empleados por iteración en purga de extensiones:\n");
      for(int i=0;i<tiemposPurgaExtensiones.length;i++){
         fwp.write(nivel(i) + timeFormat(tiemposPurgaExtensiones[i]) + "\n");
      }

      fwp.write("\nExtensiones evitadas en cada iteración:\n");
      for(int i=0;i<extensionesEvitadas.length;i++){
         fwp.write(nivel(i) + "evento: "+ numberFormat(extensionesEvitadas[i])
               + ", ventana: " + numberFormat(extensionesEvitadasVentana[i]) + "\n");
      }


      fwp.write(SEPARADOR);

      fwp.write("\nPatrones generados (patrones más específicos):\n");
      for(int i=2;i<patronesIteracion.length;i++){
         fwp.write(nivel(i) + numberFormat(patronesIteracion[i])
               + "(" + numberFormat(patronesMasEspecificosIteracion[i]) + ")\n");
      }
   }
}
