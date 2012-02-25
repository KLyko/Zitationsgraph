package de.uni.leipzig.asv.zitationsgraph.db;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.*;

public class Main {
	public static void main(String[] args){
		DBLoader dbl = new DBLoader();
		DirectedGraph<String, DefaultEdge> graph = dbl.createGraph();
		dbl.writeGraphToFile(graph);
	}
}
