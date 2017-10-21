import javax.swing.*;        

import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private HashMap<Integer, HashMap<Integer, Integer>> map;
  private List<Integer> vecinos;
  private List<Integer> destinos;
  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    //Instancio map que sera mi tabla de ruteo
    map=  new HashMap<Integer, HashMap<Integer, Integer>>();
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
		  HashMap<Integer, Integer> miVector=map.get(myID);
		  if(miVector==null)
		  {
		  	miVector=new HashMap<Integer,Integer>();
		    miVector.put(myID, 0);
		  }
		  //Agrego el costo del vecino correspondiente al paso de la iteracion en este momento
		  miVector.put(vecino, vecinoCostoInteger);
		  //Agrego mi vector de distancia a mi tabla de ruteo			
		  map.put(myID, miVector);
		
		  
		  //De aqui en mas se realiza para completar en la tabla de ruteo los costos espejo111
		  //Obtengo el vector de distancias del vecino de la tabla de ruteo, sino existe aun lo instancio y le asigno a mi mismo el costo 0
		  
		  HashMap<Integer, Integer> miVecinoVector=map.get(vecino);
		  if(miVecinoVector==null)
		  {
		  	miVecinoVector=new HashMap<Integer,Integer>();
		  	miVecinoVector.put(vecino, 0);
				
		  }
		  //Agrego el espejo de costo del vecino a mi
		  miVecinoVector.put(myID, vecinoCostoInteger);
		  
		  //Agrego el vector distancia del vecino a la tabla ruteo
		  map.put(vecino, miVecinoVector);
		
		
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
			  if(v1!=v2)
			  {
				  HashMap<Integer, Integer> vecinoVector=map.get(v1);
				  if(vecinoVector==null)
				 	  vecinoVector=new HashMap<Integer,Integer>();
				  if(vecinoVector.get(v2)==null)
					  vecinoVector.put(v2, this.sim.INFINITY);
			    map.put(v1, vecinoVector);
			  }
		  }
	  }
	  
  }
  private void notificarVecinos()
  {
	  
	  //recorro la lista de mis vecinos para notificarlos y enviarles mi vector de distancia
	  for (Integer vecinoID : vecinos) {
			RouterPacket pkt= new RouterPacket(myID, vecinoID, map.get(myID));
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
		    Integer costoAlVecino=map.get(myID).get(vecino);
		    //Verifico si el destino alcanzable por el vecino no se encuentra en mi vector de distancia o si mi costo a el es mayor que mi costo al vecino mas el costo del vecino al destino alcanzable por el
		    if((map.get(myID).get(destinoAlcanzablePorVecino)==null) || (map.get(myID).get(destinoAlcanzablePorVecino)>costoDestinoAlcanzablePorVecino+costoAlVecino))
		    {
		    	//Si se cumple lo anterior pongo dicho costo al vecino mas el costo del vecino al destino alcanzable como mi nuevo costo al destino alcanzable
		    	map.get(myID).put(destinoAlcanzablePorVecino, costoDestinoAlcanzablePorVecino+costoAlVecino);
		    	 //si el destino no existe lo agrego a mi lista de destinos
				  if (!destinos.contains(vecino))
					  destinos.add(vecino);
				
		    	//Agrego el espejo
		    	 HashMap<Integer, Integer> desAlcVector=map.get(destinoAlcanzablePorVecino);
				  if(desAlcVector==null)
				  {
					  desAlcVector=new HashMap<Integer,Integer>();
					  desAlcVector.put(destinoAlcanzablePorVecino, 0);
				  }
				  desAlcVector.put(myID, costoDestinoAlcanzablePorVecino+costoAlVecino);
				  map.put(destinoAlcanzablePorVecino, desAlcVector);
				//marco que hay cambios para notificar
		    	hayCambios=true;
		    	
		    }
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
  
private String formatearNumero(Integer i)
{
	//formateo los costos e IDs para la salida en pantalla
	String s;
	if(i==sim.INFINITY)
		s="IFN";
	else
		
		s=i.toString();
	   
	while(s.length()<4)
	{
		s="_"+s;
	}
	
	s=s+"_|";
return s;
}
  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());

	  String cabezal="_O/D_|";
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
		    Iterator itI = ((HashMap<Integer, Integer>) o.getValue()).entrySet().iterator();
			  while (itI.hasNext()) {
				    Map.Entry i = (Map.Entry)itI.next();
				    Integer x=(Integer) i.getKey();
				    Integer costo=(Integer) i.getValue();
				    if (cabezalImprimir)
				    	cabezal=cabezal+formatearNumero(x);				    	
				    if(origenImprimir)
				    	out=out+formatearNumero(y);
				    origenImprimir=false;
				    out=out+formatearNumero(costo);
				    
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
	  if(vecinos.contains(dest) && map.get(myID).get(dest)!=newcost)
	  {
		  //sustituyo el nuevo valor del costo del link
		  HashMap<Integer,Integer> miVector=map.get(myID);
		  miVector.put(dest, newcost);
		  map.put(myID, miVector);
		
		  //sustituyo el espejo de dicho costo
		  HashMap<Integer,Integer> vecinoVector=map.get(dest);
		  vecinoVector.put(myID, newcost);
		  map.put(dest, vecinoVector);
		  
		notificarVecinos();
	  }
  }

}
