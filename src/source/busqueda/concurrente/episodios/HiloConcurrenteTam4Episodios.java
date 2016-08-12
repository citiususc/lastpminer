package source.busqueda.concurrente.episodios;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.busqueda.concurrente.jerarquia.HiloConcurrenteTam4;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.modelo.IAsociacionConEpisodios;
import source.modelo.IAsociacionTemporal;
import source.modelo.concurrente.IAsociacionDiccionarioConcurrente;
import source.modelo.concurrente.PatronConcurrenteDFE;
import source.patron.Patron;

public class HiloConcurrenteTam4Episodios extends HiloConcurrenteTam4{

   public HiloConcurrenteTam4Episodios(int numHilo,
         IBusquedaConcurrenteSecuenciaAnotaciones mine,
         IColeccion coleccion,
         Map<String, List<IAsociacionTemporal>> mapa) {
      super(numHilo, mine, coleccion, mapa);
   }

   @Override
   protected void calcularSoporte(ISecuencia secuencia, int sid){
      Iterator<List<Patron>> itVentanaActual = mine.getActualIterator(sid);
      List<Patron> encontrados = new ArrayList<Patron>();

      List<String> listaTipos = new ArrayList<String>();

      for(Evento evento : secuencia){
         List<Patron> ventanaActual = itVentanaActual.next();
         List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());

         for(IAsociacionTemporal receptor : receptores){
            ((IAsociacionDiccionarioConcurrente)receptor).actualizaVentana(numHilo, sid, evento);
         }

         // Se recorre la lista de la tabla hash
         for(IAsociacionTemporal receptor : receptores){
            //   Procesar únicamente aquellos candidatos que vienen con episodios completos,
            // y vienen por primera vez. (Si hay tipos de eventos que no provienen de episodios
            // en la anterior iteración se pudieron buscar adecuadamente.
            if(((IAsociacionConEpisodios)receptor).sonEpisodiosCompletos()){
               List<Patron> aux = receptor.getPatrones();
               List<PatronConcurrenteDFE> lista = new ArrayList<PatronConcurrenteDFE>();
               for(Patron patron : aux){
                  lista.add((PatronConcurrenteDFE)patron);
               }
               ((IAsociacionDiccionarioConcurrente)receptor).recibeEvento(numHilo,sid,evento,mine.isSavePatternInstances(),lista,encontrados);
            }
         }


         // Se comprueban las anotaciones que haya de la iteración anterior
         listaTipos = mine.posiblesTiposParaAmpliar(ventanaActual, listaTipos);
         boolean conservar = false;
         for(Patron aux : ventanaActual){
            PatronConcurrenteDFE patron = (PatronConcurrenteDFE)aux;

            for(String tipo : listaTipos){
               List<PatronConcurrenteDFE> extensiones = patron.getExtensiones(tipo);
               if(extensiones!=null && !extensiones.isEmpty()){
                  IAsociacionTemporal posible = extensiones.get(0).getAsociacion();
                  if(posible==null){ continue; } // En rara ocasión se puede dar
                  if(((IAsociacionConEpisodios)posible).sonEpisodiosCompletos()){//si no es episodio completo no se busca
                     ((IAsociacionDiccionarioConcurrente)posible).recibeEvento(numHilo,sid,evento, mine.isSavePatternInstances(),extensiones,encontrados);
                  }else{
                     conservar=true;
                  }
               }
            }
            if(conservar){
               // Añadir la vieja anotación a la lista de anotaciones aceptadas
               encontrados.add(patron);
            }
         }

         ventanaActual.clear();
         ventanaActual.addAll(encontrados); //aquí se guardan las anotaciones de la ventana en actual
         encontrados.clear();

      }
   }
}
