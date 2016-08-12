
¿Cómo ejecutar los algoritmos?
==============================
1. Definir el fichero src/config/config.properties. Hay un fichero de ejemplo en la carpeta en el que se explican los posibles parámetros del mismo.
2. Identificar los parámetros de ejecución. Estos parámetros se explican en la documentación de la clase Principal. Los más relevantes son:

  * algorithm [**ASTP** | HSTP | IM | WM | ...]
  * mode [**BASIC** | EPISODE | SEED | FULL]
  * windowSize [**80**, int]
  * minFreq [**30**, int]
  * collection [**apnea** | BD4 | BD5 | BDR56 | ... ]
  * iterations [**1**, int ]

3. Ejecutar el comando con las opciones de classpath y las opciones de JVM adecuadas.

Ejemplo:

`java -cp /path/to/hstpminer.jar:/path/to/lib/javacsv.jar:/path/to/lib/commons-io-2.4.jar:/path/to/lib/jOpenDocument-1.3.jar -Xms512m -Xmx4g -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC  source.Principal algorithm=wm mode=basic windowSize=20 minFreq=300 collection="BDRoE6" iterations=5 resultPath="/path/to/results/BDRoE6/" writeReport=true writeMarkingReport=true writeIterationsReport=true`

Introducción al código
======================

En esta sección se presenta una imagen global del código. Para información sobre clases completas se recomienda ver su JavaDoc.

ASTPMiner
---------
Las clases principales de ASPTMiner son Mine, MineCompleteEpisodes, SemillaConjuncion y SemillaConjuncionCompleteEpisodes.


HSTPMiner
---------
Las clases principales de HSTPMiner son MineCEDFE y SemillaConjuncionCEDFE. Ambos algoritmos construyen instancias de ModeloEpisodiosDFE y de PatronDictionaryFinalEvent.

###MineCEDFE
Equivale a HSTPminer sin patrón semilla. Se utiliza igual que MineCompleteEpisodes.

* public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, List<List<Evento>> coleccion, int supmin, int win, List<Episodio> episodios). **Episodios** es la lista de episodios, si está vacía, o no se proporciona este argumento, debería comportarse exactamente igual que MineDictionaryFinalEvent.

###SemillaConjuncionCEDFE
HSTPminer cando se quere usar un patrón semente.
* public List<List<IAsociacionTemporal>> buscarPatronesFrecuentes(List<String> tipos, List<List<Evento>> coleccion, List<ModeloSemilla> semillas, int supmin, int win, List<Episodio> episodios)

###ModeloEpisodiosDFE
Gestiona la ventana de forma diferente, mediante los métodos actualizaVentana y recibeEvento.

###PatronDictionaryFinalEvent
Implementa la jerarquía de patrones, añadiendo dos métodos que permiten ir arriba y abajo.

* getPadres(): Devuelve una lista con todos los patrones que se usaron para construir 'this'. Es dicir, para un patrón ABCD, devolvería 4 patrones ABC, ABD, ACD e BCD.
* getExtensiones(String tipo): Devuelve todas las extensiones de 'this' con el tipo de evento 'tipo'. Por ejemplo, para un patrón ABC y un tipo D, devolvería todos los patrones ABCDn que se pudieron construir con 'this'.



Registro de cambios
===================


