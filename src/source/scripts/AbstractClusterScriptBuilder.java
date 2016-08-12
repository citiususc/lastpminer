package source.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import source.configuracion.ExecutionParameters;

/**
 * Clase abstracta que permite escribir un script para el cluster
 * mediante su implementación.
 * @author citius
 *
 */
public abstract class AbstractClusterScriptBuilder {

    /*
    * Atributos
    */

   protected String pathGeneracionScripts; //Directorio en el que se escriben los scripts
   protected String nombreFichero; //sin extensión

   protected String pathEjecucion = "$PBS_O_WORKDIR"; //Path de ejecución en el cluster
   protected String email;
   protected String nombreTrabajo;
   protected String ficheroError;
   protected String ficheroOutput;
   protected boolean notificacionEmision;
   protected boolean notificacionAborto = true;
   protected boolean notificacionFinalizacion = true;


   protected String moduleToLoad = "jdk";
   protected String javaOptions; //Sólo se escribe si moduleToLoad es "jdk"
   protected int numNodes = ExecutionParameters.CLUSTER_NODES;
   protected int ppn = ExecutionParameters.CLUSTER_PPN;
   protected String mem = null;
   protected String[] nodesExtra = null;
   protected String walltime = ExecutionParameters.CLUSTER_WALLTIME;

   /**
    * Escribe el código que ejecuta el script en el cluster
    * @param fScript
    * @throws IOException
    */
   protected abstract void escribirContenido(BufferedWriter fScript) throws IOException;


   protected void escribirCabeceras(BufferedWriter fScript) throws IOException{
       fScript.write("#!/bin/bash");
       fScript.newLine();

       //Nombre del trabajo
       if(nombreTrabajo != null){
          fScript.write("#PBS -N " + nombreTrabajo);
          fScript.newLine();
       }

       //En el config.properties de ejemplo se explican los parámetros
       fScript.write("#PBS -l nodes=" + numNodes + ":ppn=" + ppn);
       if(nodesExtra != null){
          for(String extra : nodesExtra){
             fScript.write(":" + extra);
          }
       }
       if(walltime != null){
          fScript.write(",walltime=" + walltime);
       }
       if(mem != null){
          fScript.write(",mem=" + mem);
       }
       fScript.newLine();

       //-e Indica el fichero en el que se redireccionará la salida estándar de error de nuestro ejecutable.
       // Por defecto, la salida estándar de error se redirecciona a un fichero con extensión .eXXXX (donde XXXX representa el identificador PBS del trabajo).
       // Ej: #PBS -e mySTD.err
       if(ficheroError != null){
           fScript.write("#PBS -e " + ficheroError);
           fScript.newLine();
       }

       //-o #Indica el fichero en el que se redireccionará la salida estándar de nuestro ejecutable.
       // Por defecto, la salida estándar se redirecciona a un fichero con extensión .oXXXX (donde XXXX representa el identificador PBS del trabajo).
       //Ej: #PBS -o mySTD.out
       if(ficheroOutput != null){
           fScript.write("#PBS -o " + ficheroOutput);
           fScript.newLine();
       }

       //Se notifican por correo los eventos sólo si se ha indicado un email de notificación.
       // b -> cuando el trabajo se emita a los nodos,
       // a -> en caso de que se aborte la ejecución del trabajo inexperadamente y/o
       // e -> cuando el trabajo termine su ejecución sin ningún incidente.
       if(email != null && (notificacionEmision || notificacionAborto || notificacionFinalizacion )){
          fScript.write("#PBS -m "
                + (notificacionEmision? "b" : "")
                + (notificacionAborto? "a" : "")
                + (notificacionFinalizacion? "e" : "")
                + " -M " + email);
          fScript.newLine();
       }

       if(pathEjecucion != null){
          fScript.write("cd " + pathEjecucion);
          fScript.newLine();
       }

       if(moduleToLoad != null){
           fScript.write("module load " + moduleToLoad);
           fScript.newLine();
           if(moduleToLoad.startsWith("jdk") && javaOptions != null){
               fScript.write("export _JAVA_OPTIONS=" + javaOptions);
               fScript.newLine();
           }
       }
   }


   /**
    * Método que se llama para construir el fichero
    */
   public void escribirScript(){
      try {
         File path = new File(pathGeneracionScripts);
         if(!path.exists()){
             path.mkdirs();
         }

         File ficheroQsubs = new File(path, nombreFichero + ".sh");// PATH_LOCAL_OUTPUT + "/generarBDs.sh";
         BufferedWriter fScript = new BufferedWriter(new FileWriter(ficheroQsubs));
         escribirCabeceras(fScript);

         escribirContenido(fScript);

         fScript.newLine();
         fScript.flush();
         fScript.close();

         System.out.println("Se ha escrito " + ficheroQsubs);
      } catch (IOException ioe) {
         System.out.println("Error en la ESCRITURA del fichero");
         ioe.printStackTrace();
      }
   }

   /*
    * Getters y setters
    */

    public String getPathGeneracionScripts() {
        return pathGeneracionScripts;
    }

    public void setPathGeneracionScripts(String pathGeneracionScripts) {
        this.pathGeneracionScripts = pathGeneracionScripts;
    }

    public String getNombreFichero() {
        return nombreFichero;
    }

    public void setNombreFichero(String nombreFichero) {
        this.nombreFichero = nombreFichero;
    }

    public String getPathEjecucion() {
        return pathEjecucion;
    }

    public void setPathEjecucion(String pathEjecucion) {
        this.pathEjecucion = pathEjecucion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreTrabajo() {
        return nombreTrabajo;
    }

    public void setNombreTrabajo(String nombreTrabajo) {
        this.nombreTrabajo = nombreTrabajo;
    }

    public boolean isNotificacionEmision() {
        return notificacionEmision;
    }

    public void setNotificacionEmision(boolean notificacionEmision) {
        this.notificacionEmision = notificacionEmision;
    }

    public boolean isNotificacionAborto() {
        return notificacionAborto;
    }

    public void setNotificacionAborto(boolean notificacionAborto) {
        this.notificacionAborto = notificacionAborto;
    }

    public boolean isNotificacionFinalizacion() {
        return notificacionFinalizacion;
    }

    public void setNotificacionFinalizacion(boolean notificacionFinalizacion) {
        this.notificacionFinalizacion = notificacionFinalizacion;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public int getPpn() {
        return ppn;
    }

    public void setPpn(int ppn) {
        this.ppn = ppn;
    }

    public void setWalltime(int hours, int minutes){
        this.walltime = hours + ":" + minutes + ":00";
    }

    public void setWalltime(String walltime){
        this.walltime = walltime;
    }

    public String getWalltime(){
        return walltime;
    }

    public String getFicheroError() {
        return ficheroError;
    }

    public void setFicheroError(String ficheroError) {
        this.ficheroError = ficheroError;
    }

    public String getFicheroOutput() {
        return ficheroOutput;
    }

    public void setFicheroOutput(String ficheroOutput) {
        this.ficheroOutput = ficheroOutput;
    }

    public String getModuleToLoad() {
       return moduleToLoad;
    }

    public void setModuleToLoad(String moduleToLoad) {
       this.moduleToLoad = moduleToLoad;
    }

    public String getJavaOptions() {
       return javaOptions;
    }

    public void setJavaOptions(String javaOptions) {
       this.javaOptions = javaOptions;
    }

    public String getMem() {
       return mem;
    }

    public void setMem(String mem) {
       this.mem = mem;
    }

    public String[] getNodesExtra() {
       return nodesExtra;
    }

    public void setNodesExtra(String[] nodesExtra) {
       this.nodesExtra = nodesExtra;
    }


}

