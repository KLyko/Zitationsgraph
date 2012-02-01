package de.uni.leipzig.asv.zitationsgraph.tests.data;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.TreeMap;
import java.util.logging.Logger;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;

public class GraphManager extends Observable implements PropertyChangeListener{

	private static final Logger log = Logger.getLogger(GraphManager.class.getName());
	private Graph dataGraph ;
	private Table nodeTable;
	private Table edgeTable;
	private PubData data;
	public static final Object WILL_CHANGE = "willChange";
	public static final Object CHANGED = "changed";
	
	
	public GraphManager (PubData data){
		this.data = data;
		this.data.addPropertyChangeListener(this);
		nodeTable = new Table();
		nodeTable.addColumn(Constants.TITLE, String.class);
		nodeTable.addColumn(Constants.AUTHORS, String.class);
		nodeTable.addColumn(Constants.YEAR, String.class);
		nodeTable.addColumn(Constants.ID, int.class);
		edgeTable = new Table();
		edgeTable.addColumn(Constants.SOURCE, int.class);
		edgeTable.addColumn(Constants.TARGET, int.class);
		dataGraph= new Graph(nodeTable,edgeTable,false,
					Constants.ID,Constants.SOURCE,Constants.TARGET);
	}


	
	
	public void initGraph (TreeMap<String, Publication>pubMap,
			TreeMap<String,List<String>>citeMap){
		if (pubMap.comparator() instanceof PubComparator){
			log.info("instance right");
		}
		this.setChanged();
		this.notifyObservers(WILL_CHANGE);
		int edgeInd, target,existNode;
		Publication pub = null;
		Publication citedPub;
		for (Entry<String, List<String>> e: citeMap.entrySet()){
			existNode = nodeExist (e.getKey());
			if (existNode==-1){
				pub = pubMap.get(e.getKey());
				existNode = nodeTable.addRow();
				nodeTable.set(existNode, Constants.TITLE,pub.getTitle());
				nodeTable.set(existNode, Constants.AUTHORS,Arrays.toString(
						pub.getAuthors().toArray(new Author[0])));
				nodeTable.set(existNode, Constants.ID, existNode);
			}
			if (e.getValue()!= null)
			for (String cit: e.getValue()){
				citedPub = pubMap.get(cit);
				target = nodeExist(cit);
				if (target ==-1){
					if (citedPub==null){
						log.info("reference is null:"+cit);
						if (!pubMap.containsKey(cit)){
							log.info("no key for "+cit);
							log.info("floor:"+pubMap.floorKey(cit)+"\n"+
									"ceiling:"+pubMap.ceilingKey(cit));
						}
					}
				target = nodeTable.addRow();
					nodeTable.set(target, Constants.TITLE,citedPub.getTitle());
					nodeTable.set(target, Constants.AUTHORS, Arrays.toString(
							citedPub.getAuthors().toArray(new Author[0])));
					nodeTable.set(target, Constants.ID, target);
				}
				edgeInd = edgeTable.addRow();
				edgeTable.set(edgeInd, Constants.SOURCE, existNode);
				edgeTable.set(edgeInd, Constants.TARGET, target);
			}
			
		}
		this.setChanged();
		log.info("#nodes " +nodeTable.getTupleCount()+"#edges "+edgeTable.getTupleCount());
		this.notifyObservers(CHANGED);
	}
	
	@SuppressWarnings("unchecked")
	private int nodeExist(String node){
	
		int exist=-1;
		/*Suche mir alle Knoten raus die den Namen node haben*/
		Predicate existingQuery=(Predicate) ExpressionParser.parse(Constants.TITLE
				+" ='"+node+"'");
		
		try{
			Iterator<Tuple> nodeIterator=(Iterator<Tuple>)nodeTable.tuples(existingQuery);
			if(!nodeIterator.hasNext()){
				/*keine Elemente vorhanden->Knoten existiert nicht in der Knotentabelle*/
				exist=-1;
			}else{
				exist=nodeIterator.next().getRow();
			}
		}
		catch(NullPointerException e){
		/*Exception da Iterator NullElemente besitzen kann*/
			exist=-1;
		}
		return exist;
	}
	
	public Graph getGraph(){
		return dataGraph;
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PubData.NEW_DATA)){
			
			initGraph(this.data.getPubMap(),this.data.getCiteMap());
		}
		
	}
}
