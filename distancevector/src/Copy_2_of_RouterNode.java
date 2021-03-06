import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Copy_2_of_RouterNode {
	
	private int myID;
	private GuiTextArea myGUI;
	private RouterSimulator sim;
	private HashMap<Integer, HashMap<Integer, Integer[]>> map;
	private HashMap<Integer, Integer> links;
	private Boolean llegaInfo=false;
	private String info="";
 
	Boolean aplicoRevInv=true;
  
	//--------------------------------------------------
	public Copy_2_of_RouterNode(int ID, RouterSimulator sim, HashMap<Integer,Integer> costs) {
	    
		myID = ID;
	    this.sim = sim;
	    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
	    //Instancio map que sera mi tabla de ruteo que contiene 
	    //En filas el origen, en columnas el destino, 
	    //y en cada lugar el par camino/costo de la forma [Integer camino]integer costo
	    map=  new HashMap<Integer, HashMap<Integer, Integer[]>>();
	    
	    //Itero sobre los costos de los vecinos recibido en el constructor
	    Iterator<Entry<Integer, Integer>> it = costs.entrySet().iterator();
			
	    while (it.hasNext()) {
		  
			Map.Entry<Integer, Integer> e = (Map.Entry<Integer, Integer>)it.next();
			  
			//obtengo id del vecino
			Integer vecino=(Integer) e.getKey();
			  
			//obtengo costo del vecino
			Integer vecinoCostoInteger=(Integer) e.getValue();
			  		  
			//Obtengo mi vector de distancias de la tabla de ruteo, sino existe aun lo instancio y me 
			//asigno a mi mismo el costo 0
			HashMap<Integer, Integer[]> miVector=map.get(myID);
			if(miVector==null){
				
				//me agrego a mi mismo como destino
			  	miVector=new HashMap<Integer,Integer[]>();
			    miVector.put(myID, new Integer[]{myID,0});
			    
			}
			  
			if(links==null)
				links=new HashMap<Integer,Integer>();
			  
			links.put(vecino,vecinoCostoInteger);
			  
			//Agrego el costo del vecino correspondiente al paso de la iteracion en este momento
			miVector.put(vecino, new Integer[]{vecino,vecinoCostoInteger});
			  
			//Agrego mi vector de distancia a mi tabla de ruteo			
			map.put(myID, miVector);
	
	    }
	
		//relleno los valores infinitos
		rellenarInfinitos(myID);
	   
		//Notifico a todos mis vecinos que hubo cambios dado que antes no tenia datos
		notificarVecinos();
	llegaInfo=true;
	}
  
	@SuppressWarnings("static-access")
	private void rellenarInfinitos(Integer idNodoConNuevoVecino){

		//hago la matriz cuadrada con lo nuevo, e infinitos donde corresponda
		HashMap<Integer, Integer[]> nodosRed=map.get(idNodoConNuevoVecino);
		if(nodosRed!=null){
		  
			Iterator<Entry<Integer,Integer[]>> it1 = nodosRed.entrySet().iterator();
			while (it1.hasNext()){
		
				Map.Entry<Integer, Integer[]> e = (Map.Entry<Integer, Integer[]>)it1.next();
				Integer v1=e.getKey();

				Iterator<Entry<Integer,Integer[]>> it2 = nodosRed.entrySet().iterator();
				while (it2.hasNext()){
				
					Map.Entry<Integer, Integer[]> e2 = (Map.Entry<Integer, Integer[]>)it2.next();
					Integer v2=e2.getKey();
			
					HashMap<Integer, Integer[]> vecinoVector=map.get(v1);
					if(vecinoVector==null)
						vecinoVector=new HashMap<Integer,Integer[]>();
					if(vecinoVector.get(v2)==null){

						if(v1==v2)
							vecinoVector.put(v2, new Integer[]{null,0,0});
						else
							vecinoVector.put(v2, new Integer[]{null,this.sim.INFINITY,0});
					
					}
					map.put(v1, vecinoVector);
			 
				}
			}
		} 
  
	}
  
	private HashMap<Integer, Integer> obtengoMiVectorDistancia(){
	  
		//armo de mi tabla de ruteo mi vector de distancia, es decir le quito el componente camino de la clave 
		//camino/costo 
		HashMap<Integer, Integer> dvAEnviar= new HashMap<Integer,Integer>();
		HashMap<Integer, Integer[]> dvEnTR= new HashMap<Integer,Integer[]>();
		dvEnTR=map.get(myID);
		Iterator<Entry<Integer, Integer[]>> it = dvEnTR.entrySet().iterator();
		while (it.hasNext()) {
		
			Map.Entry<Integer, Integer[]> e = (Map.Entry<Integer, Integer[]>)it.next();
		    Integer [] caminoCosto=(Integer[]) e.getValue();
		    dvAEnviar.put((Integer) e.getKey(), caminoCosto[1]);
		 
		}
		return dvAEnviar;
  
	}
	
	@SuppressWarnings("static-access")
	private void notificarVecinos(){
	  
		HashMap<Integer, Integer> dv= obtengoMiVectorDistancia();
		//recorro la lista de mis vecinos para notificarlos y enviarles mi vector de distancia
		Iterator<Entry<Integer, Integer>> it = links.entrySet().iterator();
		while (it.hasNext()){
			
			Map.Entry<Integer, Integer> e = (Map.Entry<Integer, Integer>)it.next();

			Integer destinoNodo=(Integer) e.getKey();
	  	  
			RouterPacket pkt= new RouterPacket(myID, destinoNodo, dv);
			
			if(aplicoRevInv){
				
				Iterator<Entry<Integer, Integer>> it2 = dv.entrySet().iterator();
				while (it2.hasNext()) {
					  
					Map.Entry<Integer, Integer> e2 = (Map.Entry<Integer, Integer>)it2.next();
					Integer destinoAlc=(Integer) e2.getKey();
					if(map.get(myID).get(destinoAlc)[0]!=null && map.get(myID).get(destinoAlc)[0].compareTo(destinoNodo)==0)
						pkt.mincost.put(destinoAlc,sim.INFINITY);
				}		
				
			}
			sendUpdate(pkt);
					
		}
	}
    
	@SuppressWarnings("static-access")
	private Integer[] bellmanFord(Integer destinoAlcanzablePorVecino){
	

		Integer keyMenor=sim.INFINITY;
	    Integer costoKeyMenor=sim.INFINITY;
	  
	    if(links.get(destinoAlcanzablePorVecino)!=null){
	    	keyMenor=destinoAlcanzablePorVecino;
	    	costoKeyMenor=links.get(destinoAlcanzablePorVecino);
	    }
	    
	    //recorro mis vecinos
	    Iterator<Entry<Integer, Integer>> it = links.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<Integer, Integer> e = (Map.Entry<Integer, Integer>)it.next();
		    Integer vecino=(Integer) e.getKey();
		    if(vecino.compareTo(destinoAlcanzablePorVecino)!=0){
		    	Integer costoVecino=links.get(vecino);
			
		    	if( map.get(vecino).get(destinoAlcanzablePorVecino)!=null)
		    		if(costoKeyMenor.compareTo(costoVecino+map.get(vecino).get(destinoAlcanzablePorVecino)[1])>0){
		    			
		    			keyMenor=vecino;
		    			costoKeyMenor=costoVecino+map.get(vecino).get(destinoAlcanzablePorVecino)[1];
		    		
		    		}
		    }
	    }
	 
	    if(keyMenor!=sim.INFINITY && costoKeyMenor!=sim.INFINITY)
	    	return new Integer[]{keyMenor,costoKeyMenor};
	    else
	    	return null;
	}
  
	public void recvUpdate(RouterPacket pkt) {
		llegaInfo=true;
		HashMap<Integer,Integer> mincost = pkt.mincost;
	  
		//Id del vecino que me notifica de un cambio
		Integer vecino=pkt.sourceid;
	
		//bandera para verificar cambio en mi vector de distancia y notificar a mis vecinos al final
		Boolean hayCambios=false;
	  
		//itero sobre el vector de distancia del vecino
		Iterator<Entry<Integer, Integer>> it = mincost.entrySet().iterator();
		info="recvUpdate--Origen="+vecino+"---DV(destino,costo){";
		while (it.hasNext()) {
		    
			Map.Entry<Integer, Integer> e = (Map.Entry<Integer, Integer>)it.next();
		    
		    //Obtengo el ID del destino alcanzable por mi vecino
		    Integer destinoAlcanzablePorVecino=(Integer) e.getKey();
		
		    //Obtengo el el costo de ese destino alcanzable por mi vecino
		    Integer costoDestinoAlcanzablePorVecino=(Integer) e.getValue();
		    info=info+"("+destinoAlcanzablePorVecino+","+costoDestinoAlcanzablePorVecino+");";
		    //Agrego este detino alcanzable del vector distancia de mi vecino a mi tabla de ruteo 
		    //en el vector distancia de mi vecino
			map.get(vecino).put(destinoAlcanzablePorVecino,new Integer[]{null,costoDestinoAlcanzablePorVecino} );
		    
		    //Verifico si el destino alcanzable por el vecino no se encuentra en mi vector de distancia o si mi 
			//costo a el es mayor que mi costo al vecino mas el costo del vecino al destino alcanzable por el
			
			if(destinoAlcanzablePorVecino.compareTo(myID)!=0){
				
				Integer[] resultBellmanFord=bellmanFord(destinoAlcanzablePorVecino);
				if(resultBellmanFord!=null) //si resultBellmanFord es null entonces no se llega al destino ni por mi ni por mis vecinos
				{
					if((map.get(myID).get(destinoAlcanzablePorVecino)==null) || ((map.get(myID).get(destinoAlcanzablePorVecino)[0]!=null && map.get(myID).get(destinoAlcanzablePorVecino)[0].compareTo(resultBellmanFord[0])!=0) || (map.get(myID).get(destinoAlcanzablePorVecino)[0]!=null && map.get(myID).get(destinoAlcanzablePorVecino)[1].compareTo(resultBellmanFord[1])!=0)))
					{
					//Si se cumple lo anterior pongo dicho costo al vecino mas el costo del vecino al destino alcanzable como mi nuevo costo al destino alcanzable
					map.get(myID).put(destinoAlcanzablePorVecino,resultBellmanFord);
			    	//si el destino no existe lo agrego a mi lista de destinos				
			    	hayCambios=true;
					}	
			    }
				else if(map.get(myID).get(destinoAlcanzablePorVecino)!=null && map.get(myID).get(destinoAlcanzablePorVecino)[0]!=null && map.get(myID).get(destinoAlcanzablePorVecino)[0].compareTo(vecino)==0) //sino llego a mi destino por mis vecinos y antes llegaba (pero puede que antes tampoco llegara)
				{
					//claramente dejo de llegar a ese destino
					map.get(myID).remove(destinoAlcanzablePorVecino);
			    	//si el destino no existe lo agrego a mi lista de destinos				
			    	hayCambios=true;
				}
				
			}
							
		}
		info=info+"}";
		rellenarInfinitos(vecino);	  
	
		//SI hay cambios aviso a vecinos
		if(hayCambios)
			notificarVecinos();
	
	}
	
	private void sendUpdate(RouterPacket pkt){
		sim.toLayer2(pkt);
	}

	@SuppressWarnings("static-access")
	private String formatearDato(Integer camino,Integer costo){
		
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
	
	@SuppressWarnings("static-access")
	private String formatearNumero(Integer i){
		
		//formateo los costos e IDs para la salida en pantalla
		String s;
		if(i==sim.INFINITY)
			s="#";
		else
			s=i.toString();

		s=F.format(s, 15);
		return s;
	}

	public void printDistanceTable() {
		if(llegaInfo)
		{
		myGUI.println("Current table for " + myID + "  at time " + sim.getClocktime());
		myGUI.println(info);
		String cabezal=F.format("O/D" , 15);
		Boolean cabezalImprimir=true;

		String out;

		Iterator<Entry<Integer, HashMap<Integer, Integer[]>>> itO = map.entrySet().iterator();
		
		Boolean origenImprimir;
		//Itero sobre la tabla de ruteo y mando a pantalla el cabezal y costos
		while (itO.hasNext()) {
		    
			Map.Entry<Integer, HashMap<Integer, Integer[]>> o = (Map.Entry<Integer, HashMap<Integer, Integer[]>>)itO.next();
		    Integer y=(Integer) o.getKey();
		    origenImprimir=true;
		    out="";
		    Iterator<Entry<Integer, Integer[]>> itI = ((HashMap<Integer, Integer[]>) o.getValue()).entrySet().iterator();
			while (itI.hasNext()) {
			
				Map.Entry<Integer, Integer[]> i = (Map.Entry<Integer, Integer[]>)itI.next();
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
		llegaInfo=false;
		}
	}

	public void simulacionRcvVecinos(int dest){
		
		Integer[] resultBellmanFord=bellmanFord(dest);
			
		if(resultBellmanFord!=null &&
				((map.get(myID).get(dest)==null) || (map.get(myID).get(dest)[0].compareTo(resultBellmanFord[0])!=0 || map.get(myID).get(dest)[1].compareTo(resultBellmanFord[1])!=0))){
	    	
			//Si se cumple lo anterior pongo dicho costo al vecino mas el costo del vecino al destino alcanzable como mi nuevo costo al destino alcanzable
			map.get(myID).put(dest,resultBellmanFord);
	
	    }
		
	}
	
	public void updateLinkCost(int dest, int newcost) {
		llegaInfo=true;
		info="UpdateLinkCost--Destino="+dest+"--NuevoCosto="+newcost;
		//Agrego y/o actualizo el link en mi lista de links vecinos si el costo no es infinito (caida de link)
		if(newcost!=sim.INFINITY){
			
			links.put(dest,newcost);
				
			//Solo se notifican vecinos y se actualiza el map cuando se usaba ese link, o cuando no se usaba pero el nuevo costo es menor o igual al anterior,
			//o cuando se conecta un nodo por primera vez.
			if((map.get(myID).get(dest) == null) || (map.get(myID).get(dest)[0].compareTo(dest)!=0 && map.get(myID).get(dest)[1].compareTo(newcost)>=0) || (map.get(myID).get(dest)[0].compareTo(dest)==0)){
				
				//sustituyo el nuevo valor del costo del link
				HashMap<Integer,Integer[]> miVector=map.get(myID);
				
				//Si es un nodo que se conecta por primera vez.
				if (map.get(myID).get(dest) == null) {
					
					miVector.put(dest, new Integer[]{dest,newcost});
					//Tambi�n agrego una fila para el nuevo destino en mi map.
					if (map.get(dest) == null) {
						
						HashMap<Integer, Integer[]> filaNueva = new HashMap<Integer, Integer[]>();
						map.put(dest, filaNueva);
						rellenarInfinitos(myID);
		
					};
				
				}else
					miVector.put(dest, new Integer[]{miVector.get(dest)[0],newcost});
				
				
				map.put(myID, miVector);
				notificarVecinos();
			}
		}
		else //remuevo el link de mi lista de links vecinos si el costo es infinito
		{
			
			//Modificaci�n Seba, si el cambio es por la caida de un link lo dejo en mi map a los vecinos pero con costo infinito
			//links.remove(dest);
			links.put(dest, sim.INFINITY);
	
			//Solo se notifican vecinos y se actualiza el map cuando se usaba ese link
			if (map.get(myID).get(dest)[0].compareTo(dest)==0){
				
				//Si estaba usando ese enlace para llegar al destino entonces no llego m�s.
				map.get(myID).put(dest, new Integer[] {null,sim.INFINITY});

				//elimino el camino a ese nodo en mi vector, ya que al usarlo dejo de llegar a el
				HashMap<Integer,Integer[]> miVector=map.get(myID);
				miVector.remove(dest);
				map.put(myID, miVector);
				
				//simulo llegada de distance vecto de vecinos
				simulacionRcvVecinos(dest);
	
				rellenarInfinitos(dest);
				
				notificarVecinos();

			}
		}
	}

}
