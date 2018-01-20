import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Router Class
 * 
 * Router implements Dijkstra's algorithm for computing the minumum distance to all nodes in the network
 * @author      Marcus Tang 10086730
 *     
 *
 */
public class Router {

 	/**
     	* Constructor to initialize the program 
     	* 
     	* @param peerip		IP address of other routers (we assume that all routers are running in the same machine)
     	* @param routerid	Router ID
     	* @param port		Router UDP port number
     	* @param configfile	Configuration file name
	* @param neighborupdate	link state update interval - used to update router's link state vector to neighboring nodes
        * @param routeupdate 	Route update interval - used to update route information using Dijkstra's algorithm
 
     */
	 String pIP;
	 int rID;
	 int port;
	
	 int nUpdate;
	 int rUpdate;
	 int numNodes;
	 
	 String cFile;
	 int[] distanceVector;
	 int[] portsArry;
	 int[][] vectorMatrix;
	 byte[] receivedBytes;
	 boolean[] receivedVectors;
	
	 
	 DatagramSocket routerSocket;
	 DatagramPacket sendLinkState;
	 DatagramPacket receiveLinkState;
	 
	 
	 
	public Router(String peerip, int routerid, int port, String configfile, int neighborupdate, int routeupdate) {
		this.pIP = peerip;
		this.rID = routerid;
		this.cFile = configfile;
		this.nUpdate = neighborupdate;
		this.rUpdate = routeupdate;
		this.port = port;

	
	}
	
  
    	/**
     	*  Compute route information based on Dijkstra's algorithm and print the same
     	* 
     	*/
	// Initalizes and sets timers for the router to begin sending and receiving
	public void compute() {
	  	/**** You may use the follwing piece of code to print routing table info *******/
        	
			try{
			   
		       FileReader reader = new FileReader(cFile);
		       BufferedReader buffer = new BufferedReader(reader);
			   String line = buffer.readLine();
			   // reads the first line and get the number of routers
			   numNodes = Integer.parseInt(line);
			   distanceVector = new int[numNodes];
			   portsArry = new int[numNodes];
			   receivedVectors = new boolean[numNodes];
			   vectorMatrix= new int [numNodes][numNodes];
			   List<String> neighbours = new ArrayList<String>();
			   
			   // gathers all the neighbours info
			   while(line != null){
				  neighbours.add(line);
				  System.out.println(neighbours);
		          line = buffer.readLine();
                  
			    }
				
			    String[] node;
				// sets the values for the router we are currently on
				distanceVector[rID] = 0;
				receivedVectors[rID] = true;
				portsArry[rID] = port;
				
				for(int y = 0; y < numNodes; y++){
					if(y != rID){
						distanceVector[y] = 999;
						receivedVectors[y] = false;
						
						
					}else{
						continue;
					}
					
					
			    }
				
				int id;
				int cost;
				int nodePort;
				//splits and parses the values from the neighbours
				for(int x = 1; x < neighbours.size(); x++){
					node = neighbours.get(x).split(" ", 4);
					id = Integer.parseInt(node[1]);
					System.out.println(id + "++++++++++");
					cost = Integer.parseInt(node[2]);
					nodePort = Integer.parseInt(node[3]);
					
					distanceVector[id] = cost;
					portsArry[id] = nodePort;						
				}
				for(int z = 0; z < numNodes;z++){
					vectorMatrix[rID][z] = distanceVector[z];
				}
					
			   
	        }catch(Exception e){
		      System.out.println(e);
	        }
			System.out.println("Routing Info");
        	System.out.println("RouterID \t Distance \t Prev RouterID");
			
        	for(int i = 0; i < numNodes; i++)
          	{
                System.out.println(i + "\t\t   " + distanceVector[i] +  "\t\t\t" /*+  prev[i]*/);
          	}
			
			try{
				this.routerSocket = new DatagramSocket(this.port);
				receivedBytes = new byte[LinkState.MAX_SIZE];
				
			}catch(Exception e){
				System.out.println(e);
			}
			// sets the times to be constantly checking and sending
			Timer timerSend = new Timer(true);
			timerSend.scheduleAtFixedRate(new SendVectors(this), nUpdate, nUpdate);
			
	        Timer timerUpdate = new Timer(true);
			timerUpdate.scheduleAtFixedRate(new UpdateNode(this), rUpdate, rUpdate);
			
			while(true){
				try{
					receiveLinkState = new DatagramPacket(receivedBytes, receivedBytes.length);
					routerSocket.receive(receiveLinkState);
					processUpdateDS(receiveLinkState);
				}catch(Exception e){
					
				}
			}
			
	}
	 	/*******************/

	// sends the updated values of the vectors to neighbours 
	public synchronized void processUpdateNeighbour(){
		LinkState ls;
		DatagramPacket sendLinkState;
		System.out.println("Sending vector distances to neighbours");
		for(int i = 0; i < numNodes; i++){
			if(i != rID && distanceVector[i] != 999){
				System.out.println("Neighbour port of router " + rID + ": " + portsArry[i]);
				ls = new LinkState(rID, i, distanceVector);
                try{
                sendLinkState = new DatagramPacket(ls.getBytes(),ls.getBytes().length, InetAddress.getByName("localhost"),portsArry[i]);
                routerSocket.send(sendLinkState);
                System.out.println("Sending packets to " + portsArry[i]);
              
                }catch(Exception e){
					System.out.println(e);
				}
			}
		}
		
	}
	//checking to see if all the routers have send in their vectors to this router
	public synchronized void processUpdateRoute(){
		for(int i = 0; i < numNodes; i++){
			if(receivedVectors[i] == false){
				System.out.println("+++Did not receive all vectors+++");
                for(int x = 0; x < numNodes; x++){
                    for(int j = 0; j < numNodes; j++){
                       System.out.print(vectorMatrix[x][j] + " ");
                    }
                    System.out.println("\n");
                }
				return;
			}
		}
		
		ShortestDistance t = new ShortestDistance();
	    //Pass in adjacency graph and rID
	    t.dijkstra(vectorMatrix, rID, numNodes);
	}
	// check to see if some router has already sent in its vectors
	public synchronized void processUpdateDS(DatagramPacket receive){
        LinkState ls;
        int[] incomingVector;


        ls = new LinkState(receive);
        incomingVector = ls.getCost();

        if(receivedVectors[ls.sourceId] == false){
            receivedVectors[ls.sourceId] = true;

            for(int i = 0; i < numNodes; i++){
                vectorMatrix[ls.sourceId][i] = incomingVector[i];
            }
        }
        else{
           System.out.println("Already received vector");
        }


       System.out.println("Forwarding packets");
       passOnLinkState(ls);


	}
	//sends what distances this router has and gives it to its neighbours
	public synchronized void passOnLinkState(LinkState ls){
		LinkState passOn;
		for(int i = 0; i < numNodes; i++){
            if(i != rID && distanceVector[i] != 999){
                passOn = new LinkState(ls.sourceId, i, ls.getCost());
                try{
				InetAddress IPAddress = InetAddress.getByName("localhost");
                sendLinkState = new DatagramPacket(ls.getBytes(),ls.getBytes().length, IPAddress, portsArry[i]);
                routerSocket.send(sendLinkState);
                
                }catch(Exception e){
					System.out.println(e);
				}

            }
        }
	}

	
	/* A simple test driver 
     	
	*/
	public static void main(String[] args) {
		
		String peerip = "127.0.0.1"; // all router programs running in the same machine for simplicity
		String configfile = "";
		int routerid = 999;
        int neighborupdate = 1000; // milli-seconds, update neighbor with link state vector every second
		int forwardtable = 10000; // milli-seconds, print route information every 10 seconds
		int port = -1; // router port number
	
		// check for command line arguments
		if (args.length == 3) {
			// either provide 3 parameters
			routerid = Integer.parseInt(args[0]);
			port = Integer.parseInt(args[1]);	
			configfile = args[2];
		}
		else {
			System.out.println("wrong number of arguments, try again.");
			System.out.println("usage: java Router routerid routerport configfile");
			System.exit(0);
		}

		
		Router router = new Router(peerip, routerid, port, configfile, neighborupdate, forwardtable);
		
		System.out.printf("Router initialized..running");
		router.compute();
	}

}

class SendVectors extends TimerTask{
	Router router;
	
	public SendVectors(Router r){
		this.router = r;
	}
	@Override
	public void run(){
		router.processUpdateNeighbour();
	}
}

class UpdateNode extends TimerTask{
	Router router;
	
	public UpdateNode(Router r){
		this.router = r;
	}
	@Override
	public void run(){
		router.processUpdateRoute();
	}
}
