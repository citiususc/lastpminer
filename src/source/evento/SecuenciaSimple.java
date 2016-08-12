package source.evento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Algunas operaciones tienen como precondición que la lista esté
 * ordenada.
 * @author vanesa.graino
 *
 */
public class SecuenciaSimple implements ISecuencia {

   /*
    * Atributos
    */

   List<Evento> eventos;

   /*
    * Constructores
    */

   public SecuenciaSimple() {
      eventos = new ArrayList<Evento>();
   }

   public SecuenciaSimple(List<Evento> eventos){
      this.eventos = eventos;
   }

   public SecuenciaSimple(Evento... eventos){
      this.eventos = new ArrayList<Evento>(Arrays.asList(eventos));
   }

   /*
    * Métodos
    */

   @Override
   public void add(Evento ev){
      eventos.add(ev);
   }

   @Override
   public void sort(){
      Collections.sort(eventos);
   }

   public int size(){
      return eventos.size();
   }

   @Override
   public Iterator<Evento> iterator() {
      return eventos.iterator();
   }

   @Override
   public Evento get(int index) {
      return eventos.get(index);
   }

   @Override
   public Evento remove(int index) {
      return eventos.remove(index);
   }

   @Override
   public boolean isEmpty() {
      return eventos.isEmpty();
   }

   @Override
   public ISecuencia clone() {
      return new SecuenciaSimple(new ArrayList<Evento>(eventos));
   }

   @Override
   public ListIterator<Evento> listIterator() {
      return eventos.listIterator();
   }

   @Override
   public ListIterator<Evento> listIterator(int index) {
      return eventos.listIterator(index);
   }

   @Override
   public List<Evento> subList(int fromIndex, int toIndex) {
      return eventos.subList(fromIndex, toIndex);
   }

   @Override
   public boolean contains(Evento ev) {
      //return eventos.contains(ev);
      return Collections.binarySearch(eventos, ev) > -1;
   }

   @Override
   public int indexOf(Evento ev) {
      //return eventos.indexOf(ev);
      int index = Collections.binarySearch(eventos, ev);
      return index<0? -1 : index;
   }

   @Override
   public int lastIndexOf(Evento ev) {
      //return eventos.lastIndexOf(ev);
      return indexOf(ev);
   }

   @Override
   public String toString(){
      return eventos.toString();
   }

   @Override
   public Evento set(int index, Evento ev) {
      return eventos.set(index, ev);
   }

   @Override
   public void subtractOffsetTime() {
      if(eventos.isEmpty()){ return; }
      int offset = eventos.get(0).getInstante();
      for(Evento ev: eventos){
         ev.setInstante(ev.getInstante()-offset);
      }
   }

   @Override
   public int lastInstant() {
      return eventos.isEmpty() ? -1 : eventos.get(eventos.size()-1).getInstante();
   }

}
