package source.evento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * IMPORTANTE: está sin acabar
 *
 * Implementación de una secuencia en la que se dividen los eventos en trozos
 * de un tamaño fijado por la constante {@code CHUNK_SIZE}.
 * La secuencia está dividida físicamente, no lógicamente como es el caso de {@code GuavaChunkedSequence}.
 * @author vanesa.graino
 *
 */
public class ChunkedSequence implements ISecuencia{
   private static final int CHUNK_SIZE = 1000;

   /*
    * Atributos
    */

   List<List<Evento>> chunks;

   /*
    * Constructores
    */

   public ChunkedSequence(List<List<Evento>> chunks) {
      this.chunks = new ArrayList<List<Evento>>();
      for(List<Evento> chunk : chunks){
         this.chunks.add(new ArrayList<Evento>(chunk));
      }
   }

   public ChunkedSequence() {
      chunks = new ArrayList<List<Evento>>();
      chunks.add(new ArrayList<Evento>());
   }

   /*
    * Methods
    */

   @Override
   public Iterator<Evento> iterator() {
      List<Evento> todas = chunksToList();
      return todas.iterator();
   }

   @Override
   public void add(Evento ev) {
      List<Evento> lastChunk = chunks.get(chunks.size()-1);
      if(lastChunk.size() >= CHUNK_SIZE){
         lastChunk = new ArrayList<Evento>();
         chunks.add(lastChunk);
      }
      lastChunk.add(ev);
   }

   private List<Evento> chunksToList(){
      List<Evento> todas = new ArrayList<Evento>();
      for(List<Evento> chunk : chunks){
         todas.addAll(chunk);
      }
      return todas;
   }

   @Override
   public void sort() {
      List<Evento> todas = chunksToList();
      Collections.sort(todas);
      chunks.clear();
      List<Evento> currentChunk = new ArrayList<Evento>();
      chunks.add(currentChunk);
      int counter = CHUNK_SIZE;
      while(!todas.isEmpty()){
         currentChunk.add(todas.remove(0));
         counter--;
         if(counter == 0){
            currentChunk = new ArrayList<Evento>();
            chunks.add(currentChunk);
            counter = CHUNK_SIZE;
         }
      }
      //Si el último chunk está vacío se borra
      if(currentChunk.isEmpty()){
         todas.remove(todas.size()-1);
      }
   }

   @Override
   public int size() {
      int size = 0;
      for(List<Evento> chunk : chunks){
         size += chunk.size();
      }
      return size;
   }

   @Override
   public Evento get(int index) {
      int[] chunkAndGlobalIndex = chunkForGlobalIndex(index);
      if(chunkAndGlobalIndex != null){
         return chunks.get(chunkAndGlobalIndex[0]).get(index-chunkAndGlobalIndex[1]);
      }
      return null;
   }

   @Override
   public Evento remove(int index) {
      int[] chunkAndGlobalIndex = chunkForGlobalIndex(index);
      if(chunkAndGlobalIndex != null){
         return chunks.get(chunkAndGlobalIndex[0]).remove(index-chunkAndGlobalIndex[1]);
      }
      return null;
   }

   @Override
   public boolean isEmpty() {
      return chunks.isEmpty() || chunks.get(0).isEmpty();
   }

   @Override
   public ISecuencia clone() {
      return new ChunkedSequence(chunks);
   }

   @Override
   public ListIterator<Evento> listIterator() {
      return listIterator(0);
   }

   @Override
   public ListIterator<Evento> listIterator(int index) {
      return new ListIterator<Evento>(){
         //Apuntan a lo que devolvería next
         int cursorChunk=0, cursorIndex=0, cursorGlobalIndex=0;
         //Boolean nextLastCall = null;

         private void avanzarCursor(){
            cursorGlobalIndex++;
            cursorIndex++;
            if(cursorIndex>=chunks.get(cursorChunk).size()
                  && (cursorChunk+1) < chunks.size()){
               cursorChunk++;
               cursorIndex=0;
            }
         }

         private void retrasarCursor(){
            cursorGlobalIndex--;
            cursorIndex--;
            if(cursorIndex<0 && cursorChunk>0){
               cursorChunk--;
               cursorIndex = chunks.get(cursorChunk).size()-1;
            }
         }


         @Override
         public boolean hasNext() {
            return cursorIndex<chunks.get(cursorChunk).size();
         }

         @Override
         public Evento next() {
            try{
               Evento ev = chunks.get(cursorChunk).get(cursorIndex);
               avanzarCursor();
               return ev;
            }catch(IndexOutOfBoundsException e){
               throw new NoSuchElementException();
            }
         }

         @Override
         public boolean hasPrevious() {
            if(cursorIndex==0){
               //Basandonos en que los chunks no pueden estar vacios
               //Si hay un chunk anterior, tiene que haber un evento anterior
               return cursorChunk>0;
            }
            return true;
         }

         @Override
         public Evento previous() {
            try{
               retrasarCursor();
               return chunks.get(cursorChunk).get(cursorIndex);
            }catch(IndexOutOfBoundsException e){
               throw new NoSuchElementException();
            }
         }

         @Override
         public int nextIndex() {
            return cursorGlobalIndex;
         }

         @Override
         public int previousIndex() {
            return cursorGlobalIndex-1;
         }

         @Override
         public void remove() {
            /*if(nextLastCall == null){

            }
            nextLastCall = null;*/
            throw new UnsupportedOperationException();
         }

         @Override
         public void set(Evento e) {
            /*if(nextLastCall == null){

            }
            nextLastCall = null;*/
            throw new UnsupportedOperationException();

         }

         @Override
         public void add(Evento e) {
            /*if(nextLastCall == null){

            }
            nextLastCall = null;*/
            throw new UnsupportedOperationException();
         }

      };
   }

   @Override
   public List<Evento> subList(int b, int e) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean contains(Evento ev) {
      return indexOf(ev) != -1;
   }

   /**
    * Se presupone que todo está ordenado y que no hay chunks vacíos
    */
   @Override
   public int indexOf(Evento ev) {
      int globalIndex = 0;
      for(List<Evento> chunk : chunks){
         if(chunk.get(chunk.size()-1).getInstante()<ev.getInstante()){
            //El chunk entero está antes que el evento
            globalIndex += chunk.size();
            continue;
         }else if(chunk.get(0).getInstante()>ev.getInstante()){
            //El chunk entero está después del evento
            return -1;
         }
         int index = chunk.indexOf(ev);
         if(index != -1){
            return globalIndex + index;
         }
         globalIndex += chunk.size();
      }
      return -1;
   }

   /**
    * Se presupone que todo está ordenado. Devuelve el indice
    * como si todos los eventos estuviesen en una única lista.
    */
   @Override
   public int lastIndexOf(Evento ev) {
      //Como no puede haber eventos repetidos es lo mismo que indexOf
      return indexOf(ev);
   }

   @Override
   public Evento set(int globalIndex, Evento ev) {
      int[] chunkAndGlobalIndex = chunkForGlobalIndex(globalIndex);
      if(chunkAndGlobalIndex != null){
         return chunks.get(chunkAndGlobalIndex[0]).set(globalIndex-chunkAndGlobalIndex[1], ev);
      }
      return null;
   }

   @Override
   public void subtractOffsetTime() {
      if(limpiarChunksVacios()) return;
      int offset = chunks.get(0).get(0).getInstante();
      for(List<Evento> chunk : chunks){
         for(Evento ev : chunk){
            ev.setInstante(ev.getInstante() - offset);
         }
      }
   }

   /**
    *
    * @return true si la colección está vacía
    */
   private boolean limpiarChunksVacios(){
      while(!chunks.isEmpty() && chunks.get(0).isEmpty()){
         chunks.remove(0);
      }
      if(chunks.isEmpty()) return true;
      return false;
   }


   /**
    *
    * @param chunkIndex - indice del chunk en el array {@code chunks}
    * @return
    */
   @SuppressWarnings("unused")
   private int chunkGlobalInitialIndex(int chunkIndex){
      int globalIndex = 0;
      for(int i=0;i<chunkIndex;i++){
         globalIndex += chunks.get(i).size();
      }
      return globalIndex;
   }


   /**
    *
    * @param globalIndex
    * @return Un par {indexOfChunk,initialGlobalIndex} o null si no hay chunk para el índice {@code globalIndex}.
    */
   private int[] chunkForGlobalIndex(int globalIndex){
      int chunkIndex=0, index = 0;
      while(index<=globalIndex && chunkIndex<chunks.size()){
         index += chunks.get(chunkIndex++).size();
      }
      chunkIndex--;
      return index<globalIndex? null : new int[]{chunkIndex, index - chunks.get(chunkIndex).size()};
   }

   @Override
   public int lastInstant() {
      // TODO Auto-generated method stub
      return 0;
   }
}
