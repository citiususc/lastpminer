package source.busqueda.concurrente.jerarquia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionDiccionarioConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.patron.Patron;

public class HiloConcurrenteTam4 extends HiloConcurrenteTam3{

   public HiloConcurrenteTam4(int numHilo,
         IBusquedaConcurrenteSecuenciaAnotaciones mine,
         IColeccion coleccion, Map<String, List<IAsociacionTemporal>> mapa) {
      super(numHilo, mine, coleccion, mapa);
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      List<Patron> encontrados = new ArrayList<Patron>();
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      List<String> listaTipos = new ArrayList<String>();
      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();

         // Actualizar ventana de las asociaciones temporales con el tipo de evento leído
         List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
         for(IAsociacionTemporal aux : receptores){
            ((IAsociacionDiccionarioConcurrente)aux).actualizaVentana(numHilo,sid, evento);
         }
         // Calcular las asociaciones temporales a comprobar para el evento actual
         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         for(Patron aux : ventanaActual){
            PatronConcurrenteDFE patron = (PatronConcurrenteDFE)aux;
            for(String tipo : listaTipos){
               List<PatronConcurrenteDFE> extensiones = patron.getExtensiones(tipo);
               if(extensiones!=null && !extensiones.isEmpty()){
                  //Nodo nodo = (Nodo)mine.getRaizArbol().obtenerNodoEnArbol(extensiones.get(0).getTipos());
                  //IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)nodo.getModelo();
                  IAsociacionDiccionarioConcurrente posible = (IAsociacionDiccionarioConcurrente)extensiones.get(0).getAsociacion();
                  posible.recibeEvento(numHilo,sid,evento,mine.isSavePatternInstances(),extensiones,encontrados);
               }
            }
         }
         ventanaActual.clear();
         ventanaActual.addAll(encontrados);
         encontrados.clear();
      }
   }

}
