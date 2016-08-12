package source.busqueda;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;
import source.patron.PatternFactory;

public class GeneradorPatronesSemilla extends GeneradorPatrones {

   //private static final Logger LOGGER = Logger.getLogger(GeneradorPatronesSemilla.class.getName());

   public GeneradorPatronesSemilla(int tam, AbstractMine mine) {
      super(tam, mine);
   }



   public boolean comprobarSubasociacionesSemilla(String tipo, List<String> mod, Map<String,List<IAsociacionTemporal>> mapa){
      int index = 1;
      List<IAsociacionTemporal> listaTipo = mapa.get(tipo);
      for(int k=0; k<listaTipo.size() && index<tam; k++){
         IAsociacionTemporal padre = listaTipo.get(k);
         if(mod.containsAll(Arrays.asList(padre.getTipos()))){
            asocBase[index] = padre;
            patCount[index] = padre.getPatrones().size();
            index++;
         }
      }
      return index == tam;
      //if(index<tam){ continue; }// No se puede generar ninguna extensión, faltan patrones
   }


   // Usado durante la generación de candidatos
   // Salida: valor del último índice modificado
   // Precondición: indices[0] es el último índice en cambiarse (condición de fin)
//   protected int siguienteCombinacion(){
//      return siguienteCombinacion(tam-1);
//      /*int i;
//      patIndex[tam-1]++;
//      for(i=tam-1;i>=1;i--){
//         //patIndex[l]++;
//         if(patIndex[i]<patCount[i]){
//            break;
//         }else {
//            patIndex[i]=0;
//            patIndex[i-1]++;
//         }
//      }
//      return i;*/
//   }

//   @Override
//   public List<Patron> generarPatrones(String[] modArray) throws FactoryInstantiationException{
//      List<Patron> patrones = new ArrayList<Patron>();
//
//      int uValido=-1;
//      long sumaux=1;
//      for(int l=0;l<tam;l++){
//         patIndex[l]=0;
//         sumaux*=patCount[l];
//      }
//      mine.patronesPosiblesNivel[tam-1]+=sumaux;
//
//      while(patIndex[0]<patCount[0]){
//         uValido = generarPatron(uValido, modArray, patrones);
//      }
//      return patrones;
//   }

   /**
    * Difiere del método que sobreescribe en que no hay que comprobar si la
    * combinación de patrones falla
    * @throws FactoryInstantiationException
    */
   @Override
   public int generarPatron(int uValido, String[] modArray, List<Patron> patrones) throws FactoryInstantiationException{
      int uValidoOut = uValido;
      if(uValidoOut<=0){
         //patCache[0] = new Patron(modArray, asocBase[0].getPatron(patIndex[0]));
         patCache[0] = PatternFactory.getInstance().getPatternExtension(mine.patternClassName, modArray,
               asocBase[0].getPatron(patIndex[0]), mine.numHilos);
         uValidoOut=1;
      }
      Patron patAux = null;
      int l;
      for(l=uValidoOut; l<tam; l++){
         //patAux = new Patron(patCache[l-1]);
         patAux = PatternFactory.getInstance().getPatternClone(mine.patternClassName,
               patCache[l-1], mine.numHilos);
         // En este caso la combinacioń no puede fallar,se añaden
         // restricciones que antes no había, pero no hay ninguna compartida
         patAux.combinar(asocBase[l].getPatron(patIndex[l]));
         patCache[l] = patAux;
      }
      uValidoOut = siguienteCombinacion();
      mine.notificarPatronGenerado(tam, patrones, patAux);
      return uValidoOut;
   }
}
