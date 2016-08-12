package source.modelo.arbol;

public class SupernodoRepetidos extends Supernodo {


   public void addNodo(Nodo nodo, String tipo, int indice){
      nodos.put(tipo,nodo);
      lista.add(nodo);
   }

   public void addNodoSorted(Nodo nodo, String tipo){
      nodos.put(tipo,nodo);
      //lista.add(nodo);
      int i=0;
      while(i<lista.size()){
         String ultimo = lista.get(i).getUltimoTipo();
         if(ultimo.compareTo(tipo) > 0){
            lista.add(i,nodo);
         }
         i++;
      }
      lista.add(nodo);
   }
}
