package source.modelo.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.evento.Episodio;

public class EpisodiosWrapper {
   protected String[] tiposReordenados; //ver organizarTipos
   //protected int[] repReordenados;
   protected List<Episodio> episodios;
   protected int[] equivalenciasTipos; // Devuelve  la posición adecuada de 'tipos' para la posición 'i' de tiposRordenados.
   protected int eventosDeEpisodios;
   //protected Evento ultimoEventoLeido = null;
   protected boolean episodiosCompletos = true;

   public EpisodiosWrapper(List<Episodio> episodios, String[] tipos){
      this.episodios = episodios;
      organizarTipos(tipos);
   }

   /**
    * Se construyen las estructuras auxiliares para manejar una asociacion con
    * episodios más cómodamente. Se crea tiposOrdenados que contiene los tipos
    * de eventos de episodios al principio, cuando la asociacion contiene inicio y fin,
    * y el resto a continuación (en orden lexicográfico).
    * Ejemplo: si tenemos [A,B,C,D] y el episodio D~B en tipos reordenados quedará [D,B,A,C].
    * @param tipos - tipos de eventos originales en el orden original (lexicográfico)
    */
   protected void organizarTipos(String[] tipos){
      eventosDeEpisodios = 0;
      int tSize = tipos.length;

      //repReordenados = new int[tSize];
      equivalenciasTipos = new int[tSize];
      if(episodios == null || episodios.isEmpty()){
         //tiposReordenados.addAll(Arrays.asList(tipos));
         tiposReordenados = tipos.clone();
         for(int i=0;i<tSize;i++){
            equivalenciasTipos[i]=i;
         }
         return;
      }

      List<String> restantes = new ArrayList<String>(Arrays.asList(tipos));
      tiposReordenados = new String[tSize];
      int insertados = 0, indiceOrd = 0;
      // Insertar los tipos contenidos en el listado de episodios
      for(Episodio episodio : episodios){
         // Comprobar si contiene ambos tipos de eventos
         //if(!tipos.contains(episodio.getTipoInicio()) || !tipos.contains(episodio.getTipoFin())){
         String tipoInicio = episodio.getTipoInicio();
         int indexInicio = Arrays.binarySearch(tipos, tipoInicio); //tipos.indexOf(tipo);
         if(indexInicio < 0){
            // Ignorar el episodio, puesto que no contiene ambos tipos
            episodiosCompletos = false;
            continue;
         }
         String tipoFin = episodio.getTipoFin();
         int indexFin = Arrays.binarySearch(tipos, tipoFin); //tipos.indexOf(tipo);
         if(indexFin < 0){
            // Ignorar el episodio, puesto que no contiene ambos tipos
            episodiosCompletos = false;
            continue;
         }

         eventosDeEpisodios += 2;
         equivalenciasTipos[insertados] = indexInicio;
         //tiposReordenados.add(tipo);
         tiposReordenados[indiceOrd++] = tipoInicio;
         insertados++;
         equivalenciasTipos[insertados] = indexFin;
         //tiposReordenados.add(tipoFin);
         tiposReordenados[indiceOrd++] = tipoFin;
         insertados++;

         //Borrar de restantes
         restantes.set(restantes.indexOf(tipoInicio), null);
         restantes.set(restantes.indexOf(tipoFin), null);
      }

      // Insertar el resto de tipos
      for(int index = 0; index<tSize; index++){
         if(restantes.get(index) != null){
            equivalenciasTipos[insertados++] = index;
            tiposReordenados[indiceOrd++] = restantes.get(index);
         }
      }
   }

 //@Override
   public int fijarInstancia(int tSize, int index, int tmp, int[][] abiertas, int[][] limites,
         int[] indices, int[] instancia, int ventana){
      int tMin = tmp;
      for(int i=0;i<tSize;i++){
         // Convertir la posición 'i' de 'tiposReordenados' a la apropiada de 'tipos'
         //if(i!=index) instancia[equivalenciasTipos[i]]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         //if(i!=index){instancia[tipos.indexOf(tiposReordenados.get(i))]=abiertas[i][(limites[i][0]+indices[i])%ventana];}
         //instancia[tipos.indexOf(tiposReordenados.get(i))]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         instancia[equivalenciasTipos[i]] = abiertas[i][(limites[i][0]+indices[i])%ventana];
         //if(!=indexTipos){instancia[equivalenciasTipos[i]]=abiertas[i][(limites[i][0]+indices[i])%ventana];}
         if(tMin > instancia[equivalenciasTipos[i]]){
            tMin = instancia[equivalenciasTipos[i]];
         }
      }
      instancia[equivalenciasTipos[index]] = tmp; // El evento que se corresponde al tipo de evento leído no puede cambiar.
      return tMin;
   }
   // TODO comprobar si esto es equivalente a fijarInstancia sin calcular tMin
   /*public void fijarInstancia(int tSize, int index, int[][] abiertas, int[][] limites,
         int[] indices, int[] instancia, int ventana){
      int indiceEquivalente = equivalenciasTipos[index];
      for(int i=0;i<tSize;i++){
         if(i != indiceEquivalente){
            instancia[equivalenciasTipos[i]]=abiertas[i][(limites[i][0]+indices[i])%ventana];
         }
      }
   }*/

   /**
    * Obtiene el primer array de indices que se utilizará construir una instancia y para comprobar la frecuencia de los patrones
    * de una asociación temporal.
    * @param index
    * @param tam
    * @return El primer juego de índices o null si se ha leído un inicio de episodio sin su fin correspondiente.
    */
   public int[] primerosIndices(int index, int[] tam){
      int[] indices = new int[tiposReordenados.length];
      if(index < eventosDeEpisodios){
         if((index % 2) == 0){
            return null; // Se leyó un comienzo de episodio, su fin todavía no se ha leído y por tanto no se puede construir una ocurrencia
         }else{
            // Se leyó un fin de episodio, asegurarse de construir el episodio correctamente
            indices[index] = tam[index]-1;
            //indices[index] = tam[index];
            indices[index-1] = indices[index]; // No puede ser tam[index-1]-1, porque podría haber más inicios que finales en caso de que en una ventana se encuentren varios inicios de episodio (independientemente de si se solapan o no)
         }
      }else{
         indices[index] = tam[index]-1; //TODO en ModeloEpisodios y ModeloDistribucionEpisodios estaba así, en el resto no
      }
      return indices;
   }

   /**
    * Obtiene el primer array de indices que se utilizará construir una instancia y para comprobar la frecuencia de los patrones
    * de una asociación temporal. Además, comprueba si, debido a un tamaño de ventana muy pequeño, el fin del episodio puede
    * ocurrir cuando el inicio no está en la ventana. Si se inserta un evento de fin de episodio y ya ha salido el de inicio
    * correspondiente de la ventana, se devuelve null.
    * @param index
    * @param tam
    * @param abiertas
    * @param limites
    * @param ventana
    * @param tmp
    * @return El primer juego de índices o null si se ha leído un inicio de episodio sin su fin correspondiente
    * o si ya ha salido el inicio de episodio cuando entra el final.
    */
   public int[] primerosIndices(int index, int[] tam, int[][] abiertas, int[][] limites, int ventana, int tmp){
      int[] indices = primerosIndices(index, tam);

      // Si el tamaño de ventana es muy pequeño, el fin del episodio puede ocurrir cuando el inicio no está en
      // la ventana. Hay que detectar este caso y no insertar el fin de episodio en la ventana.
      // Detecta si el evento actual fue insertado o no.
      if(indices == null || index<eventosDeEpisodios && (index%2==1)
            && abiertas[index][(limites[index][0]+indices[index])%ventana]!=tmp){
         return null;
      }
      return indices;
   }

   /**
    * Siguiente combinación de índices con respecto a los indices actuales que contiene
    * el parámetros {@code indices}.
    * @param tam Total de cada tipo de evento de la asociación
    * @param indices Indices actuales
    * @param index Índice del tipo de evento entrante en la lista de tipos de eventos del modelo
    * @param tipo Tipo del evento que se ha leido
    * @return
    */
   public int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      //if(eventosDeEpisodios==0){ return super.siguienteCombinacion(tam,indices,index,tipo); }
      int i, tSize = tiposReordenados.length;
      // Parte no ligada a episodios
      indices[tSize-1]++;
      for(i=tSize-1; i>=eventosDeEpisodios; i--){
         if(i == index){
            if(indices[i]!=tam[i]-1){
               // Realmente no debería ser 0, debería ser tam[index]-1 (ahora mismo esto se fuerza al construir la instancia
               indices[i]=tam[i]-1;
               indices[i-1]++;
            }
            continue;
         }
         if(indices[i] >= tam[i]){
            indices[i-1]++;
            indices[i] = 0;
         }else{
            // El último cambio de índice fue válido. No es necesario propagar más cambios.
            return indices;
         }
      }

      // Parte de los índices ligada a episodios
      for(; i>2; i-=2){ // El inicio del episodio se calcula con el fin, inicio == posicion par, fin == posicion impar
         //   Si el evento leído es un inicio de episodio no se puede construir una ocurrencia correcta,
         // y por tanto no se va a llamar a este método. No es necesario considerar ese caso.
         if(i == index){
            if(indices[i]!=(tam[i]-1)){
               indices[i]=tam[i]-1;
               indices[i-1]=indices[i];
               // Este evento se había cambiado y no debería, cambiar el 'siguiente' episodio
               indices[i-2]++;
            }
            continue;
         }

         if(indices[i]>=tam[i]){
            // Volver a la primera combinación del episodio y "pasar la pelota" al siguiente episodio.
            indices[i]=0;
            indices[i-1]=0;
            indices[i-2]++;
            continue; // Comprobar siguiente episodio
         }
         if(indices[i]!=indices[i-1]){ // Alguien cambió este evento, cambiar también el inicio de episodio
            //indices[i-1]++;
            indices[i-1]=indices[i];
            // Puesto que no se entró en el 'if' anterior, no es necesario seguir cambiando índices.
            return indices;
         }
      }

      //
      if(index == 1){
         // Evento leído era el fin de episodio del 'primer' episodio.
         if(indices[1]!=tam[1]-1){ return null; }
      }else{
         if(indices[1]>=tam[1]){ return null; }
         if(indices[1]!=indices[0]){
            indices[0]=indices[1];
         }
      }

      return indices;
   }


   /*	public int[] siguienteCombinacion(int[] tam, int[] indices, int index, String tipo){
      //if(eventosDeEpisodios==0){ return super.siguienteCombinacion(tam,indices,index,tipo); }
      int tSize = tiposReordenados.length;
      int resta=0;//repReordenados[tSize-1];
      int i,j;
      // Parte no ligada a episodios
      indices[tSize-1]++;
      for(i=tSize-1;i>=eventosDeEpisodios;i--){
         String tipoI = tiposReordenados[i];
         String tipoAnt = tiposReordenados[i-1];
         int mod;
         if(i==index){
            if(indices[i]!=tam[i]-1){
               // Realmente no debería ser 0, debería ser tam[index]-1 (ahora mismo esto se fuerza al construir la instancia
               indices[i]=tam[i]-1;
               indices[i-1]++;
               // Este indice no se cambia, es el nuevo evento
               // Los indices del mismo tipo se ponen a 0,1,2...
               //Si i fuese 0 entonces no se entraria aqui
               //for(j=1;j<=repReordenados[i];j++){
//               for(j=1;j<=0;j++){
//                  indices[i+j]=j-1;
//               }
            }
            resta=0;//repReordenados[i-1];
            continue; // no puede ser i==index y además estar en un tipo repetido sin ser el i más bajo
         }
         mod = tipoI==tipo? 1 : 0;
         //if(indices[i]>=(tam[i]-(repReordenados[i]-resta+mod))){
         if(indices[i]>=(tam[i]-(0-resta+mod))){
            indices[i-1]++;
            if(tipoAnt==tipoI){
               indices[i]=indices[i-1]+1;
               j=i+1;
               while((j<tSize)&&(tiposReordenados[j-1] == tiposReordenados[j])){
                  indices[j]=indices[j-1]+1;
                  j++;
               }
            }else{
               indices[i]=0;
               j=i+1;
               while((j<tSize)&&(tiposReordenados[j-1] == tiposReordenados[j])){
                  indices[j]=indices[j-1]+1;
                  j++;
               }
            }
         }else{
            // El último cambio de índice fue válido. No es necesario propagar más cambios.
            return indices;
         }
         if(tipoI == tipoAnt){
            resta--;
         }else{
            resta=0;//repReordenados[i-1];
         }
      }

      // Parte de los índices ligada a episodios
      for(;i>2;i-=2){ // El inicio del episodio se calcula con el fin, inicio == posicion par, fin == posicion impar
         //   Si el evento leído es un inicio de episodio no se puede construir una ocurrencia correcta,
         // y por tanto no se va a llamar a este método. No es necesario considerar ese caso.
         if(i==index){
            if(indices[i]!=(tam[i]-1)){
               indices[i]=tam[i]-1;
               indices[i-1]=indices[i];
               // Este indice no se cambia, es el nuevo evento
               // Los indices del mismo tipo se ponen a 0,1,2...
               //Si i fuese 0 entonces no se entraria aqui
//               for(j=1;j<=repReordenados[i];j++){
//                  indices[i+j]=j-1;
//               }
               // Este evento se había cambiado y no debería, cambiar el 'siguiente' episodio
               indices[i-2]++;
            }
            resta=0;//repReordenados[i-1];
            continue;
         }

         if(indices[i]>=tam[i]){
            // Volver a la primera combinación del episodio y "pasar la pelota" al siguiente episodio.
            indices[i]=0;
            indices[i-1]=0;
            indices[i-2]++;
            continue; // Comprobar siguiente episodio
         }
         if(indices[i]!=indices[i-1]){ // Alguien cambió este evento, cambiar también el inicio de episodio
            //indices[i-1]++;
            indices[i-1]=indices[i];
            // Puesto que no se entró en el 'if' anterior, no es necesario seguir cambiando índices.
            return indices;
         }
      }

      // En este punto sabemos que el modelo SÍ contiene episodios (si no, se ejecuta el método de 'Modelo')
      if(index==1){
         // Evento leído era el fin de episodio del 'primer' episodio.
         if(indices[1]!=tam[1]-1){return null;}
      }else{
         if(indices[1]>=tam[1]){return null;}
         if(indices[1]!=indices[0]){
            indices[0]=indices[1];
         }
      }

      return indices;
   }*/


   public boolean actualizarVentana(int[][] abiertas, int[][] limites, int[] tam,
         int ventana, int index, int tmp, int sid, int ultimoSid){
      int i,j;
      int tSize = tiposReordenados.length;
      boolean seguir = true;
      if(ultimoSid!=sid){
         // Forzar borrado de la ventana al entrar en una secuencia nueva
         for(i=0;i<tSize;i++){
            tam[i]=0;
            limites[i][0]=0;
            limites[i][1]=0;
         }
         seguir = false;
      }else{
         //Seguimos en la misma secuencia
         for(i=0;i<eventosDeEpisodios;i++){
            // ¿Se podrían recorrer solo los eventos de inicio de episodio? No, estamos quitando todo de la ventana no solo inicios
            j = limites[i][0];
            int borradosFin=0;
            while(tam[i]>0 && (tmp-ventana>=abiertas[i][j]) ) {
               j = ((j+1)%ventana);
               tam[i]--;
//               if((i%2)==0 && tam[i+1]>0){
//                  // Es evento inicio de episodio, también se debe borrar un elemento del siguiente
//                  //evento, que es el fin del episodio.
//                  tam[i+1]--;
//                  borradosFin++;
//               }
               if((i%2)==0){
                  if(tam[i+1]>0){ //En otro caso el evento de fin todavía no ha entrado en la ventana
                     borradosFin++;
                  }
                  // Es evento inicio de episodio, también se debe borrar un elemento del siguiente
                  //evento, que es el fin del episodio.
                  tam[i+1]--;
               }
            }
            if((i%2)==0 && borradosFin>0){
               //Si se han borrado eventos de inicio, borramos los eventos de fin correspondientes
               limites[i+1][0] = (limites[i+1][0] + borradosFin) % ventana;
            }
            limites[i][0]=j; // Modificar el indicador de inicio
            if(i!=index && tam[i]<=0){
               seguir=false;
            }
         }
         // Eliminar el resto de eventos
         for(/*i=eventosDeEpisodios*/;i<tSize;i++){
            j = limites[i][0];

            // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) -> con ultimoSid
            while(tam[i]>0 &&  (tmp-ventana)>=abiertas[i][j] ) {
               j = (j+1) % ventana;
               tam[i]--;
            }
            limites[i][0]=j; // Modificar el indicador de inicio
            if(i!=index && tam[i]<=0){
               seguir=false;
            }
         }

         //Debemos finales a eventos de inicio que ya han salido de la ventana
         if(/*index%2==1 &&*/ tam[index]<0){ // la primera comprobación no debería ser necesaria
            tam[index]++;
            return false;
         }

         //  Si el tamaño de ventana es muy pequeño, el fin del episodio puede ocurrir cuando el inicio no está en
         // la ventana. Hay que detectar este caso y no insertar el fin de episodio en la ventana.
         if(index%2==1 && index<eventosDeEpisodios && (tam[index]+1)>tam[index-1]){
            // Detecta si habría menos inicios de episodio que fin. Debería ser más general que la versión anterior.
            //seguir = false; //No puede ser así porque no se puede insertar el fin de episodio si no tiene el inicio
            return false;
         }
      }

      // Añadir el nuevo elemento
      abiertas[index][limites[index][1]] = tmp;
      limites[index][1] = (limites[index][1]+1) % ventana;
      tam[index]++;

      return seguir;
   }

   public boolean actualizarVentana(int[][] abiertas, int[][] limites, int[] tam,
         int ventana, int index, int tmp){
      int i,j;
      int tSize = tiposReordenados.length;
      boolean seguir = true;

      //Seguimos en la misma secuencia
      for(i=0;i<eventosDeEpisodios;i++){
         // ¿Se podrían recorrer solo los eventos de inicio de episodio?
         j = limites[i][0];
         int borradosFin=0;
         while(tam[i]>0 && ( (tmp-ventana>=abiertas[i][j]) || tmp<abiertas[i][j] ) ) {
            j=((j+1)%ventana);
            tam[i]--;
            if((i%2)==0 && tam[i+1]>0){
               // Es evento inicio de episodio, también se debe borrar un elemento del siguiente
               //evento, que es el fin del episodio.
               tam[i+1]--;
               borradosFin++;
            }
         }
         if((i%2)==0 && borradosFin>0){
            limites[i+1][0] = (limites[i+1][0] + borradosFin) % ventana;
         }
         limites[i][0]=j; // Modificar el indicador de inicio
         if(i!=index && tam[i]<=0){
            seguir = false;
         }
      }
      // Eliminar el resto de eventos
      for(;i<tSize;i++){
         j=limites[i][0];

         // mientras (hay elementos) y ((hay elementos fuera de la nueva definida) o (el elemento leido ocurre antes => nueva secuencia))
         while(tam[i]>0 && ( (tmp-ventana)>=abiertas[i][j] || tmp<abiertas[i][j] )) {
            j = (j+1) % ventana;
            tam[i]--;
         }
         limites[i][0]=j; // Modificar el indicador de inicio
         if(i!=index && tam[i]<=0){
            seguir=false;
         }
      }

      //  Si el tamaño de ventana es muy pequeño, el fin del episodio puede ocurrir cuando el inicio no está en
      // la ventana. Hay que detectar este caso y no insertar el fin de episodio en la ventana.
      if(index%2==1 && index<eventosDeEpisodios && tam[index]+1>tam[index-1]){ // Detecta si habría menos inicios de episodio que fin. Debería ser más general que la versión anterior.
         seguir = false;
      }

      // Añadir el nuevo elemento
      i=index;
      abiertas[i][limites[i][1]] = tmp;
      limites[i][1] = ((limites[i][1]+1)%ventana);
      tam[i]++;

      return seguir;
   }

   public String[] getTiposReordenados() {
      return tiposReordenados;
   }

   public List<Episodio> getEpisodios() {
      return episodios;
   }


   public int[] getEquivalenciasTipos() {
      return equivalenciasTipos;
   }


   public int getEventosDeEpisodios() {
      return eventosDeEpisodios;
   }


   public boolean isEpisodiosCompletos() {
      return episodiosCompletos;
   }


}
