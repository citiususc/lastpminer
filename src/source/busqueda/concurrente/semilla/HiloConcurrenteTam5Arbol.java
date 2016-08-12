package source.busqueda.concurrente.semilla;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.busqueda.concurrente.episodios.HiloConcurrenteTam4Episodios;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionDiccionarioConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.patron.Patron;

public class HiloConcurrenteTam5Arbol extends HiloConcurrenteTam4Episodios{
   protected int tamActual;
   protected List<Episodio> episodios;

   public HiloConcurrenteTam5Arbol(int numHilo,
         IBusquedaConcurrenteSecuenciaAnotaciones mine, IColeccion coleccion,
         Map<String, List<IAsociacionTemporal>> mapa, int tamActual, List<Episodio> episodios) {
      super(numHilo, mine, coleccion, mapa);
      this.tamActual = tamActual;
      this.episodios = episodios;
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      List<Patron> encontrados = new ArrayList<Patron>();
      List<String> listaTipos = new ArrayList<String>();

      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();

         // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
         List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
         for(IAsociacionTemporal receptor : receptores){
            ((IAsociacionDiccionarioConcurrente)receptor).actualizaVentana(numHilo,sid, evento);
         }

         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         for(Patron aux : ventanaActual){
            PatronConcurrenteDFE patron = (PatronConcurrenteDFE)aux;
            // Comprobar de qué tipo de anotación se trata
            // Caso a: anotación hecha en la anterior iteración
            if(patron.getTipos().length == tamActual-1){
               boolean conservar=false;
               for(String tipo : listaTipos){
                  List<PatronConcurrenteDFE> extensiones = patron.getExtensiones(tipo);
                  if(extensiones!=null && !extensiones.isEmpty()){
                     //Nodo nodo = mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getTipos());
                     //IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)nodo.getModelo();
                     IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)extensiones.get(0).getAsociacion();
                     if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                        posible.recibeEvento(numHilo,sid,evento,mine.isSavePatternInstances(),extensiones,encontrados);
                     }else{
                        conservar=true;
                     }
                  }
               }
               if(conservar){
                  // Añadir la vieja anotación a la lista de anotaciones aceptadas
                  encontrados.add(patron);
               }
            }else{
               // Caso b: anotación hecha hace 2 iteraciones
               // Comprobación por episodios.
               for(Episodio episodio : episodios){
                  List<PatronConcurrenteDFE> extensiones1 = patron.getExtensiones(episodio.getTipoInicio());
                  if(extensiones1!=null && !extensiones1.isEmpty()){
                     for(PatronConcurrenteDFE intermedio : extensiones1){
                        List<PatronConcurrenteDFE> extensiones = intermedio.getExtensiones(episodio.getTipoFin());
                        if(extensiones!=null && !extensiones.isEmpty()){
                           //Nodo nodo = (Nodo)mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getTipos());
                           //IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)nodo.getModelo();
                           IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)extensiones.get(0).getAsociacion();
                           if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){
                              posible.recibeEvento(numHilo,sid,evento,mine.isSavePatternInstances(),extensiones,encontrados);
                           }/*else{
                              conservar=true;
                           }*/
                        }
                     }
                  }
               }
            }

         }

         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }
}
