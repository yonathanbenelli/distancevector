import javax.swing.*;        

import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, HashMap<Integer, Integer[]>> map;
  private List<Integer> vecinos;
  private List<Integer> destinos;
  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    //Instancio map que sera mi tabla de ruteo que contiene En filas el origen, en columnas el destino, y en cada lugar el par camino/costo de la forma [Integer camino]integer costo
    map=  new HashMap<Integer, HashMap<Integer, Integer[]>>();
    vecinos = new ArrayList<Integer>();
    destinos = new ArrayList<Integer>();
    
    

    //Itero sobre los costos de los vecinos recibido en el construcor
    Iterator it = costs.entrySet().iterator();
			
	  while (it.hasNext()) {
		  Map.Entry e = (Map.Entry)it.next();
		  //obtengo id del vecino
		  Integer vecino=(Integer) e.getKey();
		  //obtengo costo del vecino
		  Integer vecinoCostoInteger=(Integer) e.getValue();
		  //si el vecino no existe lo agrego a mi lista de vecinos, para luego saber a quien notificar
		  if (!vecinos.contains(vecino))
			  vecinos.add(vecino);
		  
		  //si el destino no existe lo agrego a mi lista de destinos
		  if (!destinos.contains(vecino))
			  destinos.add(vecino);
		  
		  //Obtengo mi vector de distancias de la tabla de ruteo, sino existe aun lo instancio y me asigno a mi mismo el costo 0
		  HashMap<Integer, Integer[]> miVector=map.get(myID);
		  if(miVector==null)
		  {
			//me agrego a mi mismo como destino
			  if (!destinos.contains(myID))
				  destinos.add(myID);
		  	miVector=new HashMap<Integer,Integer[]>();
		  	
		    miVector.put(myID, new Integer[]{myID,0});
		  }
		  //Agrego el costo del vecino correspondiente al paso de la iteracion en este momento
		  miVector.put(vecino, new Integer[]{vecino,vecinoCostoInteger});
		  //Agrego mi vector de distancia a mi tabla de ruteo			
		  map.put(myID, miVector);
		
		  
		 
		
		}
	  //relleno los valores infinitos
	  rellenarInfinitos();
	   
	  //Notifico a todos mis vecinos que hubo cambios dado que antes no tenia datos
	  notificarVecinos();	
}
  
  private void rellenarInfinitos()
  {

	  for (Integer v1 : destinos) {
		  for (Integer v2 : destinos) {
			 // if(v1!=v2)
			  //{
				  HashMap<Integer, Integer[]> vecinoVector=map.get(v1);
				  if(vecinoVector==null)
				 	  vecinoVector=new HashMap<Integer,Integer[]>();
				  if(vecinoVector.get(v2)==null)
					  vecinoVector.put(v2, new Integer[]{null,this.sim.INFINITY});
			    map.put(v1, vecinoVector);
			  //}
		  }
	  }
	  
  }
  
  private HashMap<Integer, Integer> obtengoMiVectorDistancia()
  {
	  //armo de mi tabla de ruteo mi vector de distancia, es decir le quito el componente camino de la calve camino/costo 
	  HashMap<Integer, Integer> dvAEnviar= new HashMap<Integer,Integer>();
	  HashMap<Integer, Integer[]> dvEnTR= new HashMap<Integer,Integer[]>();
	  dvEnTR=map.get(myID);
	  Iterator it = dvEnTR.entrySet().iterator();
	  while (it.hasNext()) {
		    Map.Entry e = (Map.Entry)it.next();
		    Integer [] caminoCosto=(Integer[]) e.getValue();
		    dvAEnviar.put((Integer) e.getKey(), caminoCosto[1]);
	  }
	  return dvAEnviar;
  }
  private void notificarVecinos()
  {
	  
	  HashMap<Integer, Integer> dv= obtengoMiVectorDistancia();
	  //recorro la lista de mis vecinos para notificarlos y enviarles mi vector de distancia
	  
	  for (Integer vecinoID : vecinos) {
		  
			RouterPacket pkt= new RouterPacket(myID, vecinoID, dv);
			sendUpdate(pkt);
		}
  }
  
  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
	  
	  HashMap<Integer,Integer> mincost = pkt.mincost;
	  //Id del vecino que me notifica de un cambio
	  Integer vecino=pkt.sourceid;
	  //bandera para verificar cambio en mi vector de distancia y notificar a mis vecinos al final
	  Boolean hayCambios=false;
	  //itero sobre el vector de distancia del vecino
	  Iterator it = mincost.entrySet().iterator();
	  while (it.hasNext()) {
		    Map.Entry e = (Map.Entry)it.next();
		    
		    //Obtengo el ID del destino alcanzable por mi vecino
		    Integer destinoAlcanzablePorVecino=(Integer) e.getKey();
		    //Obtengo el el costo de ese destino alcanzable por mi vecino
		    Integer costoDestinoAlcanzablePorVecino=(Integer) e.getValue();
		    //Obtengo mi costo a este vecino (NO AL DEL DESTINO ALCANZABLE)
		    Integer costoAlVecino=map.get(myID).get(vecino)[1];
		    //Agrego este detino alcanzable del vector distancia de mi vecino a mi tabla de ruteo en el vector distancia de mi vecino
			map.get(vecino).put(destinoAlcanzablePorVecino,new Integer[]{vecino,costoDestinoAlcanzablePorVecino} );
		    
		    //Verifico si el destino alcanzable por el vecino no se encuentra en mi vector de distancia o si mi costo a el es mayor que mi costo al vecino mas el costo del vecino al destino alcanzable por el
		    if((map.get(myID).get(destinoAlcanzablePorVecino)==null) || (map.get(myID).get(destinoAlcanzablePorVecino)[1]>costoDestinoAlcanzablePorVecino+costoAlVecino))
		    {
		    	//Si se cumple lo anterior pongo dicho costo al vecino mas el costo del vecino al destino alcanzable como mi nuevo costo al destino alcanzable
		    	map.get(myID).put(destinoAlcanzablePorVecino,  new Integer[]{vecino,costoDestinoAlcanzablePorVecino+costoAlVecino});
		    	 //si el destino no existe lo agrego a mi lista de destinos
				
		    	hayCambios=true;
		    	
		    }
		    if (!destinos.contains(destinoAlcanzablePorVecino))
				  destinos.add(destinoAlcanzablePorVecino);
			
		}
	  
	  rellenarInfinitos();	  
	//SI hay cambios aviso a vecinos
	  if(hayCambios)
		  notificarVecinos();
  }
  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }

  private String formatearDato(Integer camino,Integer costo)
  {
  	//formateo los costos e IDs para la salida en pantalla
	  String s;
	  String co;
  	if(costo==sim.INFINITY)
  		co="#";
  	else
  		
  		co=costo.toString();
  	   
  	

  	String ca;
  	if(camino==null)
  		ca="#";
  	else
  		
  		ca=camino.toString();
  	   
  	ca="["+ca+"]";
  	s=ca+co;
  	
  	s=F.format(s,15);
  	
  	
  return s;
  }
private String formatearNumero(Integer i)
{
	//formateo los costos e IDs para la salida en pantalla
	String s;
	if(i==sim.INFINITY)
		s="#";
	else
		
		s=i.toString();
	   
	
  	s=F.format(s, 15);
  	
	
return s;
}
  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());

	  String cabezal=F.format("O/D" , 15);
	  Boolean cabezalImprimir=true;

	  String out;

	  Iterator itO = map.entrySet().iterator();
	  
	  Boolean origenImprimir;
	  //Itero sobre la tabla de ruteo y mando a pantalla el cabezal y costos
	  while (itO.hasNext()) {
		    Map.Entry o = (Map.Entry)itO.next();
		    Integer y=(Integer) o.getKey();
		    origenImprimir=true;
		    
		    out="";
		    Iterator itI = ((HashMap<Integer, Integer[]>) o.getValue()).entrySet().iterator();
			  while (itI.hasNext()) {
				    Map.Entry i = (Map.Entry)itI.next();
				    Integer x=(Integer) i.getKey();
				    Integer[] caminoCosto=(Integer[]) i.getValue();
				    if (cabezalImprimir)
				    	cabezal=cabezal+formatearNumero(x);				    	
				    if(origenImprimir)
				    	out=out+formatearNumero(y);
				    origenImprimir=false;
				    out=out+formatearDato(caminoCosto[0],caminoCosto[1]);
				    
			  }    
			  
			  if(cabezalImprimir)
				  myGUI.println(cabezal);
			  
			  cabezalImprimir=false;
			  myGUI.println(out);
			  
			    
	  }
	  


}

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
	  //Me aseguro que el destino sea siempre un nodo vecino y que el costo sea realmente diferente
	  if(vecinos.contains(dest) && map.get(myID).get(dest)[1]!=newcost)
	  {
		  //sustituyo el nuevo valor del costo del link
		  HashMap<Integer,Integer[]> miVector=map.get(myID);
		  miVector.put(dest, new Integer[]{miVector.get(dest)[0],newcost});
		  map.put(myID, miVector);
		
		  
		notificarVecinos();
	  }
  }

}
