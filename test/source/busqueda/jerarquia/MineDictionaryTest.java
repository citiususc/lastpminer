package source.busqueda.jerarquia;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.Principal;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.Supernodo;
import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.patron.PatternFactory;
import source.restriccion.RIntervalo;


public class MineDictionaryTest {

   @Test
   public void testGeneral(){
      Collection<Object[]> data =Arrays.asList(new Object[][] {
         {"apnea", 80, false},
         {"BDRoE6", 20, false}
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral(Algorithms.ALG_HSTP, Modes.MODE_BASIC, (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   //@Ignore
   @Test public void testBDRoE15(){ //FIXME resultados diferentes
      //Patron.setPrintID(false);
      AllThatYouNeed capsulaAstp = new AllThatYouNeedSinteticas("BDRoE15");
      capsulaAstp.params.setWindowSize(4);
      capsulaAstp.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsulaAstp.params.setMode(Modes.MODE_BASIC);
      capsulaAstp.params.setMinFreq(5);
      capsulaAstp.params.setSavePatternInstances(true);
      capsulaAstp.params.setTamMaximoPatron(4);
      capsulaAstp.coleccion = new ColeccionSimple(Arrays.asList((ISecuencia) new SecuenciaSimple(capsulaAstp.coleccion.get(0).subList(0, 100))));
      System.out.println("No se utiliza toda la colección!!!!");

      IColeccion copiaCollecion = capsulaAstp.coleccion.clone();

      AllThatYouNeed capsulaHstp = new AllThatYouNeed(copiaCollecion, new ArrayList<String>(capsulaAstp.tipos), capsulaAstp.episodios,
            capsulaAstp.ocurrenciasEpisodios, "BDRoE15");
      capsulaHstp.params = capsulaAstp.params.clonar();
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      String fileAstp = capsulaAstp.mineria();
      //Patron.nextID=1;
      String fileHstp = capsulaHstp.mineria();
      System.out.println("Ficheros de astp e hstp respectivamente:\n" + fileAstp + "\n"+fileHstp);

      //PrincipalTest.validarResultados(capsula.resultados, copiaCollecion);
      //PatronDictionaryFinalEvent p = (PatronDictionaryFinalEvent)PrincipalTestSAHS.getPatron(capsulaHstp.resultados, 4, 436);
      //PrincipalTestSAHS.ocurrenciaFalta(p, coleccionOriginal);
      //PrincipalTestSAHS.ocurrenciasRepetidas(p);
      //System.out.println("Frecuencia: " + p.getFrecuencia() + ", numOcus: " + p.getOcurrencias().size() );

      //Comparacion comp = AbstractMine.compararResultados(capsulaAstp.resultados, capsulaHstp.resultados);
      //System.out.println(comp.toString(false));
      //assertTrue(comp.sonIguales());
      assertTrue(Principal.compararFicheros(fileAstp, fileHstp));
   }

   /**
    * Hijo repetido
    * @throws FactoryInstantiationException
    */
   @Ignore("Por el momento")
   @Test public void testGeneracionPatrones() throws FactoryInstantiationException{ // tam>=3
      int tam = 4, windowSize = 4;
      String associationClassName = "ModeloDictionary", patternClassName = "PatronDictionaryFinalEvent";

      IAsociacionTemporal modelo;
      String[] mod;
      IAsociacionTemporal patBase[] = new IAsociacionTemporal[tam];
      int patCount[] = new int[tam];
      int patIndex[] = new int[tam];
      Patron patCache[] = new Patron[tam];
      int l;

      List<Patron> patrones;
      List<RIntervalo> constraints;
      boolean savePatternInstances = false;

      //Asociacion padres
      mod = new String[]{"9", "10", "7"};
      patrones = new ArrayList<Patron>();
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
               new RIntervalo("9","10",-2,3),
               new RIntervalo("9","7", -3,3),
               new RIntervalo("10","7",-3,3)
            ));
      GeneradorID genID = new GeneradorID();
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));

      IAsociacionTemporal modeloPadre = modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            mod, windowSize, patrones, 0);
      for(Patron p:patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(modeloPadre);
      }

      //Asociacion madre
      mod = new String[]{"9", "10", "1"};
      patrones = new ArrayList<Patron>();
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
               new RIntervalo("9","10",-2,3),
               new RIntervalo("9","1", -3,3),
               new RIntervalo("10","1",-3,3)
            ));
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));
      IAsociacionTemporal modeloMadre = modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            mod, windowSize, patrones, 0);
      for(Patron p:patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(modeloMadre);
      }

      //Asociación 2
      mod = new String[]{"9", "7", "1"};
      patrones = new ArrayList<Patron>();
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
               new RIntervalo("9","7", -1,3),
               new RIntervalo("9","1", -3,1),
               new RIntervalo("7","1",-3,-2)
            ));
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
            new RIntervalo("9","7", -3,3),
            new RIntervalo("9","1", -3,3),
            new RIntervalo("7","1", -2,3)
         ));
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));

      IAsociacionTemporal modeloBase2 = modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            mod, windowSize, patrones, 0);
      for(Patron p:patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(modeloBase2);
      }

      //Asociación 3
      mod = new String[]{"10", "7", "1"};
      patrones = new ArrayList<Patron>();
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
               new RIntervalo("10","7", -1,3),
               new RIntervalo("10","1", -3,1),
               new RIntervalo("7","1", -3,-2)
            ));
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));
      constraints = new ArrayList<RIntervalo>(Arrays.asList(
            new RIntervalo("10","7", -3,3),
            new RIntervalo("10","1", -3,3),
            new RIntervalo("7","1", -2,3)
         ));
      patrones.add(PatternFactory.getInstance().getPattern(genID, patternClassName, mod, constraints, savePatternInstances, 1));

      IAsociacionTemporal modeloBase3 = modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            mod, windowSize, patrones, 0);
      for(Patron p:patrones){
         ((PatronDictionaryFinalEvent)p).setAsociacion(modeloBase3);
      }

      Nodo padre = new Nodo(modeloPadre), madre = new Nodo(modeloMadre);

//		for(Supernodo supernodo : nivelActual){
//			List<Nodo> nodos = supernodo.getListaNodos();
//			int nSize = nodos.size();
//			for(i=0;i<nSize-1;i++){
            //Nodo padre = nodos.get(i);
            patBase[0] = padre.getModelo();
            patCount[0] = patBase[0].getPatrones().size();
            Supernodo hijos = padre.getHijos();
            //for(j=i+1;j<nSize;j++){
               //long inicioA = System.currentTimeMillis();
               // Construir la asociación temporal
               //Nodo madre = nodos.get(j);
               patBase[1] = madre.getModelo();
               patCount[1] = patBase[1].getPatrones().size();
               // Todos los nodos del supernodo comparten todos los tipos
               // de evento salvo el último.
               String tipoNuevo = patBase[1].getTipos()[tam-2];
               mod = Arrays.copyOf(patBase[0].getTipos(), patBase[0].getTipos().length+1);
               mod[mod.length-1]=tipoNuevo;

               patBase[2]= modeloBase2;
               patCount[2] = patBase[2].getPatrones().size();
               patBase[3]= modeloBase3;
               patCount[3] = patBase[3].getPatrones().size();

               // Combinar los patrones
               patrones = new ArrayList<Patron>();
               //long sumaux=1;
               int uValido=-1;
               for(l=0; l<tam; l++){ //Inicializar estructuras y contabilizar combinaciones totales
                  patCache[l] = null;
                  patIndex[l] = 0;
                  //sumaux *= patCount[l];
               }
               //posPatronesNivel[tam-1] += sumaux;

               while(patIndex[0] < patCount[0]){
                  //long inicioF = System.currentTimeMillis();
                  if(uValido<=0){
                     patCache[0] = PatternFactory.getInstance().getPatternExtension(patternClassName, mod, patBase[0].getPatron(patIndex[0]), 0);
                     uValido = 1;
                  }
                  boolean incombinable = false; //bandera que indica si ha habido un patron que no se ha podido combinar
                  for(l=uValido; l<tam; l++){
                     System.out.println("index1: " + (l-1) + ", index2: " + (tam-1-l));
                     Patron patAux = PatternFactory.getInstance().getPatternClone(patternClassName, patCache[l-1], 0);
                     //if(!patAux.combinar(patBase[l].getPatron(patIndex[l]))){
                     if(!patAux.combinar(patBase[l].getPatron(patIndex[l]), tam-1-l)){
                        //patronesDescartadosNivel[tam-1]++;
                        patIndex[l]++;
                        incombinable = true;
                        int o = l;
                        for(o=l; o>=1; o--){
                           if(patIndex[o]<patCount[o]){
                              break;
                           }else{
                              patIndex[o] = 0;
                              patIndex[o-1]++;
                           }
                        }
                        uValido = o;
                        break;
                     }else{
                        patCache[l]=patAux;
                     }
                  }
                  if(!incombinable){
                     // Actualizar índices
                     patIndex[tam-1]++;
                     for(l=tam-1; l>=1; l--){
                        if(patIndex[l]<patCount[l]){
                           break;
                        }else{
                           patIndex[l]=0;
                           patIndex[l-1]++;
                        }
                     }
                     uValido=l;
                     Patron patron = patCache[tam-1];
                     //if(patron==null) continue;
                     //long inicio = System.currentTimeMillis();

                     System.out.println("Patron generado: " + patron);
                     boolean esConsistente = patron.esConsistente(genID);
                     System.out.println("Es consistente? " + esConsistente);
                     //long fin = System.currentTimeMillis();
                     //acumConsistencia += fin - inicio;

                     //Se comprueba si es consistente y si ya se había generado
                     if(esConsistente /*&& !patrones.contains(patron)*/){
                        if(!patrones.contains(patron)){
                        patrones.add(patron);
                        }else{
                           System.out.println("El patrón ya se había añadido");
                        }
                     } //else patronesDescartadosNivel[tam-1]++;
                  } // !act
                  //long finF = System.currentTimeMillis();
                  //acumFundir += finF - inicioF;
               } // patCount[0]<patIndex[0]

               // Construir el modelo
               //long inicioM = System.currentTimeMillis();
               System.out.println("Patrones: " + patrones);
               if(patrones.size()>0){
                  modelo = AssociationFactory.getInstance().getAssociationInstance(associationClassName,
                        mod, windowSize, patrones, 0);

                  for(Patron patron : patrones){
                     ((PatronDictionaryFinalEvent)patron).setAsociacion(modelo);
                  }

                  Nodo hijo = new Nodo(modelo,hijos);
                  // Añadir el Nodo al nuevo
                  hijos.addNodo(hijo, tipoNuevo);

               }// else: No hay patrones candidatos: descartar modelo candidato actual
               //long finM = System.currentTimeMillis();

            //} // for j

         //} // for i
      //}
   }
}
