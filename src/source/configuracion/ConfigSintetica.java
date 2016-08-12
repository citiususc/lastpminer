package source.configuracion;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;


public class ConfigSintetica {
   private static final Logger LOGGER = Logger.getLogger(ConfigSintetica.class.getName());

   public String nombre;
   public boolean nueva = false;
   public Modes modo = Modes.MODE_BASIC;
   public int[] windows;
   public String fichero;
   public String path;

   public static String modeToString(Modes mode){
      switch(mode){
         case MODE_BASIC: return "basic";
         case MODE_EPISODE: return "episode";
         case MODE_SEED: return "seed";
         case MODE_FULL: return "full";
         default:
         throw new RuntimeException("El modo " + mode + " no se ha especificado");
      }
   }

   public ConfigSintetica(String nombre) {
      if(nombre==null) throw new NullPointerException("nombre no puede ser nulo");
      this.nombre = nombre;
   }

   public ConfigSintetica(String nombre, boolean nueva, Modes modo, int[] windows) {
      this(nombre);
      this.nueva = nueva;
      this.modo = modo;
      this.windows = windows;
   }

   public ConfigSintetica(String nombre, boolean nueva, Modes modo, int[] windows, String path, String fichero) {
      this(nombre,nueva,modo,windows);
      this.fichero = fichero;
      this.path = path;
   }

   public String toString(){
      return "BBDD " + nombre + (nueva? "": " no") + " es nueva"
            + ", modo <" + modeToString(modo) + ">"
            + ", ventanas: " + Arrays.toString(windows);
   }


   /*
    * Parte estática
    */

   public static ConfigSintetica fromArgs(String[] args, String prefixIn){
      if(args == null){ return null; }


      String prefix = prefixIn;
      if(prefix == null){
         prefix = "";
      }

      String nombre=null, path=null, fichero=null;
      boolean nueva = true;
      Modes modo = Modes.MODE_BASIC;
      int[] windows = new int[]{20,40,60,80};

      for(String arg: args){

         if(arg.startsWith(prefix + "nombre=")){
             nombre = arg.substring((prefix + "nombre=").length());
         }else if(arg.startsWith(prefix + "nueva=")){
             Boolean valor = Boolean.valueOf(arg.substring((prefix + "nueva=").length()));
             nueva = valor;
         }else if(arg.startsWith(prefix + "modo=")){
             String modeIn = arg.substring((prefix + "mode=").length());
             modo = Modes.valueOf("MODE_" + modeIn.toUpperCase(Locale.FRANCE));
         }else if(arg.startsWith(prefix + "windows=")){
             String windowsIn = arg.substring((prefix + "windows=").length());
             String[] strsVentana = windowsIn.split(",");
             windows = HelperProperties.getArray(Integer.parseInt(strsVentana[0]),
                   Integer.parseInt(strsVentana[1]), Integer.parseInt(strsVentana[2]));
         }else if(arg.startsWith(prefix + "fichero=")){
             fichero = arg.substring((prefix + "fichero=").length());
         }else if(arg.startsWith(prefix + "path=")){
             path = arg.substring((prefix + "path=").length());
         }else{
             LOGGER.warning("Argumento no reconocido: " + arg);
         }
      }
      if(nombre == null){
          return null;
      }

      ConfigSintetica cs = new ConfigSintetica(nombre, nueva, modo, windows);
      if(path!= null){
          cs.path = path;
      }
      if(fichero != null){
          cs.fichero = fichero;
      }

      LOGGER.info("La colección de los argumentos tiene la configuración: " + cs);

      return cs;
  }
}
