package source.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import source.Principal;
import source.configuracion.ConfigSintetica;
import source.configuracion.ConfigurationParameters;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;

public class ConfScriptBuilder{
   protected Integer minFreqApnea = null;
   protected Integer[] minFreq = null;
   protected String[] algs;
   protected String[] modosApnea = {"episode", "full"};
   protected Map<String,Boolean> skips = new HashMap<String,Boolean>(source.configuracion.ExecutionParameters.BBDD.length);
   protected boolean writeReport = true,
         writeIterationsReport, writeSingleReport = false,
         writeMarkingReport, writeTxtReport, appendResults,
         completa = true, onlyLastWindow = false;;
   protected int iterations = Principal.ITERACIONES_PRUEBAS;
   protected String pathResultados = ExecutionParameters.PATH_BASE_RESULTADOS; //PROJECT_HOME + "output/";
   protected String scriptPath = "output/scripts/";
   protected String scriptSinteticasName = "mineSynthetic.sh";
   protected String scriptApneaName = "mineApnea.sh";
   protected String inputFileName = null;
   protected List<ConfigSintetica> bases = new ArrayList<ConfigSintetica>();

   protected ConfScriptBuilder(String[] algs){
      this.bases.addAll(Arrays.asList(source.configuracion.ExecutionParameters.BBDD));
      this.algs = algs;
   }

   public boolean skip(String bbddName){
      return (!skips.containsKey(bbddName) || skips.get(bbddName));
   }

   public boolean skip(int iBbdd){
      String key = bases.get(iBbdd).nombre;
      return (!skips.containsKey(key) || skips.get(key));
   }

   public void addColeccion(ConfigSintetica cs){
       //Siempre hay que mantener a apnea de última
       bases.add(bases.size()-1, cs);
   }


   /*
    * Parte estática
    */



   public static ConfScriptBuilder configuracion(String[] bbdds, String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(String bbdd: bbdds){
         c.skips.put(bbdd, false);
      }
      return c;
   }

   public static ConfScriptBuilder configuracionConBorrado(String[] bbdds, String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(String bbdd: bbdds){
         c.skips.put(bbdd, false);
      }
      c.completa = false;
      return c;
   }

   public static ConfScriptBuilder configuracionBasica(String[] algs){
      ConfScriptBuilder c = new ConfScriptBuilder(algs);
      return c;
   }

   public static ConfScriptBuilder configuracionIgnorarTodas(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(int i=0; i<c.bases.size()-1;i++){
         c.skips.put(c.bases.get(i).nombre, true);
      }
      c.completa = false;
      return c;
   }

   public static ConfScriptBuilder configuracionSoloColeccionesConEpisodios(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(int i=0; i<c.bases.size()-1;i++){
         if(c.bases.get(i).modo == Modes.MODE_EPISODE){
            c.skips.put(c.bases.get(i).nombre, false);
         }
      }
      c.skips.put(ConfigurationParameters.APNEA_DB, false);
      c.modosApnea = new String[]{"episode"};
      return c;
   }

   public static ConfScriptBuilder configuracionSoloColeccionesBasic(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(int i=0; i<c.bases.size()-1;i++){
         if(c.bases.get(i).modo == Modes.MODE_BASIC){
            c.skips.put(c.bases.get(i).nombre, false);
         }
      }
      c.skips.put(ConfigurationParameters.APNEA_DB, false);
      c.modosApnea = new String[]{"basic"};
      return c;
   }

   public static ConfScriptBuilder configuracionMarking(boolean writeReports){
      ConfScriptBuilder c = configuracionSoloColeccionesBasic(new String[]{"wm","im","im2","hom"});
      c.writeMarkingReport = writeReports;
      c.writeIterationsReport = writeReports;
      return c;
   }

   public static ConfScriptBuilder configuracionReferencias(){
      ConfScriptBuilder c = configuracionBasica(new String[]{"hstp"});
      c.scriptSinteticasName = "mineSynthetic-tese-referencias2.sh";
      c.writeReport = false;
      c.writeIterationsReport = false;
      c.writeMarkingReport = false;
      c.iterations = 1;
      c.pathResultados = ExecutionParameters.PATH_BASE_RESULTADOS + "referenciasNuevas/";
      return c;
   }

   public static ConfScriptBuilder configuracionNuevas(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      c.writeReport = true;
      for(int i=0; i<c.bases.size()-1;i++){
         if(c.bases.get(i).nueva){
            c.skips.put(c.bases.get(i).nombre, false);
         }
      }
      //"BD4-1", "BD7-8",
      c.skips.remove("BD4-1");
      c.skips.remove("BD7-8");
      return c;
   }

   public static ConfScriptBuilder configuracionNuevasSinEpisodios(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      c.writeReport = true;
      c.skips.put("BDRoE101-1", false);
      c.skips.put("BDRoE102-1", false);
      return c;
   }

   public static ConfScriptBuilder configuracionNuevasConEpisodios(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      c.writeReport = true;
      c.skips.put("BDR4-10", false);
      c.skips.put("BDR4-14", false);
      c.skips.put("BDR5-11", false);
      c.skips.put("BDR6-4", false);
      c.skips.put("BDR7-15", false);
      c.skips.put("BDR7-16", false);
      c.skips.put("BDRoG1-2", false);
      c.skips.put("BDRoG1-7", false);
      return c;
   }

   /**
    * Colecciones nuevas que unicamente tienen episodios y no eventos sueltos
    * @param algs
    * @return
    */
   public static ConfScriptBuilder configuracionNuevasSoloEpisodios(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      c.writeReport = true;
      c.skips.put("BDRoG1-2", false);
      c.skips.put("BDRoG1-7", false);
      return c;
   }


   public static ConfScriptBuilder configuracionNegacion(){
      ConfScriptBuilder c = configuracionBasica(new String[]{"NEG"});
      c.writeReport = true;
      for(int i=0; i<c.bases.size()-1;i++){
        c.skips.put(c.bases.get(i).nombre, true);
      }
      c.skips.put(ConfigurationParameters.APNEA_DB, false);
      c.modosApnea = new String[]{"basic"};
      return c;
   }

   public static ConfScriptBuilder confColeccionesArticulo(String[] algs){
      ConfScriptBuilder c = configuracionBasica(algs);
      for(int i=0; i<c.bases.size()-1;i++){
         c.skips.put(c.bases.get(i).nombre, true);
       }
       c.skips.put(ConfigurationParameters.APNEA_DB, true);
       c.skips.put("BDR56", false);
       c.skips.put("BDR57", false);
       c.skips.put("BDRoE6", false);
       c.skips.put("BDRoE9", false);
       c.skips.put("BDRoE15", false);
       c.skips.put("bbdartigo-2-1", false);

       return c;
   }


}
