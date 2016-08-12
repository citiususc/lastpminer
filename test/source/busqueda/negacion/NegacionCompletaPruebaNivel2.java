package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.modelo.negacion.IAsociacionConNegacion;
import source.modelo.negacion.ModeloEventoPositivo;
import source.modelo.negacion.ModeloTontoNegadoCompleto;
import source.modelo.negacion.SuperModeloNegacion;
import source.patron.Ocurrencia;

public class NegacionCompletaPruebaNivel2 extends NegacionMine {

   public NegacionCompletaPruebaNivel2(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }


   @Override
   protected void generarCandidatasTam2(List<IAsociacionTemporal> anteriores,
         List<String> tipos, List<IAsociacionTemporal> candidatas,
         List<Supernodo> nuevoNivel) throws FactoryInstantiationException {

      mapa = construyeMapa(tipos.size(), tipos);

      //List<Nodo> nodos = raizArbol.getListaNodos();
      Nodo nodo = raizArbol.getHijo("fD");
      Supernodo hijos = nodo.getHijos();
      int tam = 2;

      List<String[]> comb = new ArrayList<String[]>(Arrays.asList(new String[]{"fD"}, new String[]{"fA"}));

      IAsociacionConNegacion modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            comb.get(0), comb.get(1), windowSize, getClustering(), supermodelo, numHilos);

      notificarModeloGenerado(tam, 0, modelo, comb.get(0), comb.get(1), candidatas, mapa, true);

      ModeloEventoPositivo modelo2 = new ModeloEventoPositivo("fD", 0);
      notificarModeloGenerado(tam, 0, modelo2, modelo2.getTipos(), modelo2.getTiposNegados(), candidatas, mapa, true);

      creaNodoFachada(modelo, hijos, "fA", false);

      if(!hijos.getNodos().isEmpty()){
         nuevoNivel.add(hijos);
      }
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas,
         IColeccion coleccion) {
      int tam = candidatas.get(0).size();
      if(tam==2){
         System.out.println("Tam 2");
      }
      SuperModeloNegacion superModNeg = (SuperModeloNegacion)supermodelo;

      int sid = 0, lastSup = 0;

      for(ISecuencia secuencia : coleccion){
         System.out.println("\n\nSecuencia #" + sid);
         superModNeg.setSecuencia(secuencia);
         Evento target = new Evento("fD", 3674);


         while(superModNeg.nextWindow()){

//            if(!superModNeg.getN().isEmpty() && superModNeg.getN().get(0).equals(new Evento("fD",509))){
//               System.out.println("Evento target");
//            }
            //totalVentanas++;
            for(Evento evento: superModNeg.getN()){
               if(evento.equals(target)){
                  System.out.println("Primer repetido");
               }
               List<IAsociacionTemporal> receptores = mapa.get(evento.getTipo());
               for(IAsociacionTemporal receptor : receptores){
                  receptor.recibeEvento(sid, evento, savePatternInstances);
               }
            }
            lastSup = candidatas.get(0).getSoporte();
            for(Evento evento: superModNeg.getD()){
               List<IAsociacionConNegacion> receptores = mapaNegados.get(evento.getTipo());
               for(IAsociacionConNegacion receptor : receptores){
                  receptor.saleEventoNegado(sid, evento, savePatternInstances);
               }
            }
//            if(candidatas.get(0).getSoporte() != lastSup){
//               System.out.println("State: " + superModNeg.getLastState());
//               lastSup = candidatas.get(0).getSoporte();
//            }
//            if(tam==2){
//               if(candidatas.get(0).getSoporte() != lastSup && !contieneEventoTarget(superModNeg)){
//                  System.out.println("State: " + superModNeg.getLastState());
//                  lastSup = candidatas.get(0).getSoporte();
//                  System.out.println("Soporte: " + candidatas.get(0).getSoporte() + ", soporte (2): " + candidatas.get(1).getSoporte());
//                  if(candidatas.get(0).getSoporte() != candidatas.get(1).getSoporte()){
//                     System.out.println("Difiere soporte");
//                  }
//               }
//            }
         }
         //LOGGER.info("Total ventanas ( al final de la secuencia #" + sid + "): " + totalVentanas);
         sid++;
      }

      if(tam==2){
         System.out.println("Soporte de " + candidatas.get(0).toStringSinPatrones() + ": " + candidatas.get(0).getSoporte()
               + "(" + ((ModeloTontoNegadoCompleto)candidatas.get(0)).ocurrencias.size() + ")"
               + ", soporte de " + candidatas.get(1).toStringSinPatrones() + ": " + candidatas.get(1).getSoporte());

         // Pattern instances

         // TODO buscar repetidos en [fd,-fa]
         List<Ocurrencia> ocs = ((ModeloTontoNegadoCompleto)candidatas.get(0)).ocurrencias;
         for(int i=0;i<ocs.size()-1;i++){
            if(Arrays.equals(ocs.get(i).getEventTimes(), ocs.get(i+1).getEventTimes())){
               System.out.println("Ocurrencia repetida: " + ocs.get(i));
            }
         }

         // TODO comparar ocurrencias de

      }
   }

   private boolean contieneEventoTarget(SuperModeloNegacion mod){
      for(Evento e : mod.getN()){
         if(e.getTipo().equals("fD")){
            return true;
         }
      }
      return false;
   }

}
