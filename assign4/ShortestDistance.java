/*
 * 
 * 
 * Marcus Tang 10086730
 * http://www.geeksforgeeks.org/greedy-algorithms-set-6-dijkstras-shortest-path-algorithm/
 */



import java.util.*;
import java.lang.*;
import java.io.*;

class ShortestDistance
{
	// A utility function to find the vertex with minimum distance value,
	// from the set of vertices not yet included in shortest path tree
	int V=0;
	
	
	int minDistance(int dist[], Boolean sptSet[])
	{
		// Initialize min value
		int min = Integer.MAX_VALUE, min_index=-1;

		for (int v = 0; v < V; v++)
			if (sptSet[v] == false && dist[v] <= min)
			{
				min = dist[v];
				min_index = v;
			}

		return min_index;
	}

	// A utility function to print the constructed distance array
	void printSolution(int dist[], int n, int prevNode[])
	{
		System.out.println("Routing Info");
		System.out.println("RouterID \t Distance \t Prev RouterID");
		for (int i = 0; i < V; i++)
			System.out.println(i+"\t\t   "+dist[i] + "\t\t\t" + prevNode[i]);
	}

	//Dijkstra's algorithm is implemented here
	void dijkstra(int matrix[][], int src, int numNodes)
	{
		
		this.V = numNodes;
		int dist[] = new int[V]; //holds the shordst distance between the source and i

		int prevNode[] = new int[V];

		// Will be true if and only if the shortest
		// path from the source to i is final
		Boolean sptSet[] = new Boolean[V];

		// Set all distances as 999 and stpSet[] as false
		for (int i = 0; i < V; i++)
		{
			dist[i] = Integer.MAX_VALUE;
			sptSet[i] = false;
		}

		// Distance of source vertex from itself is always 0
		dist[src] = 0;

		prevNode[src] = src;

		// Find shortest path for all vertices
		for (int count = 0; count < V-1; count++)
		{
			//Where u will eventually become the smallest distance for a vector
			int u = minDistance(dist, sptSet);

			// Mark the picked vertex as processed
			sptSet[u] = true;

			// Updates the values of the vectors
			for (int v = 0; v < V; v++)

				// Update dist[v] only if is not in the set, where the edge
				//between u and v is smaller than the current value of dist[v]
				if (!sptSet[v] && matrix[u][v]!=0 &&
						dist[u] != Integer.MAX_VALUE &&
						dist[u]+matrix[u][v] < dist[v]){
					dist[v] = dist[u] + matrix[u][v];
					prevNode[v] = u;
				}

		}


	}


}

