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
import prefuse.data.expression.parser.ParseException;
import prefuse.data.util.Sort;

/**
 * This class class initialize the citationgraph with his node and edge table.
 * The graph based on the extracted Publication and their Citations.<br>
 * It implement the PropertyChangeListener to get information, if the extraction
 * part of the documents is ready
 * @author loco
 *
 */
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
		nodeTable.addColumn(Constants.YEAR, int.class);
		//nodeTable.addColumn(Constants.COLOR, int.class);
		nodeTable.addColumn(Constants.ID, int.class);
		edgeTable = new Table();
		edgeTable.addColumn(Constants.SOURCE, int.class);
		edgeTable.addColumn(Constants.TARGET, int.class);
		dataGraph= new Graph(nodeTable,edgeTable,true,
					Constants.ID,Constants.SOURCE,Constants.TARGET);
	}


	
	/**
	 * This mehtod will called, if the extraction part is ready
	 * @param pubMap
	 * @param citeMap
	 */
	public void initGraph (TreeMap<String, Publication>pubMap,
			TreeMap<String,List<String>>citeMap){
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
				if(pub.getAuthors()!= null)
				nodeTable.set(existNode, Constants.AUTHORS,Arrays.toString(
						pub.getAuthors().toArray(new Author[0])));
				nodeTable.set(existNode, Constants.ID, existNode);
				//nodeTable.set(existNode, Constants.COLOR, pub.getColor());
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
					if(pub.getAuthors()!= null)
					nodeTable.set(target, Constants.AUTHORS, Arrays.toString(
							citedPub.getAuthors().toArray(new Author[0])));
					int year = -1;
					try {
					 year = Integer.parseInt(citedPub.getYearString());
					}catch (NumberFormatException nfe){
						
					}
					nodeTable.set(target, Constants.YEAR,year );
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
	private int nodeExist(String nodeName){
	
		int exist=-1;
		/*Suche mir alle Knoten raus die den Namen node haben*/
		String nodeN = nodeName.replace("'", "\\'");
		try{
			Predicate existingQuery=(Predicate) ExpressionParser.parse(Constants.TITLE
					+" ='"+nodeN+"'",true);
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
		}catch (ParseException pe){
			log.info(nodeN);
			
			exist = -1;
		}
		return exist;
	}
	
	public Graph getGraph(){
		return dataGraph;
	}
	
	public int getMinYear (){
		
		Predicate p = ExpressionParser.predicate(Constants.YEAR+" > 1500 ");
		Sort s =new Sort ();
		s.add(Constants.YEAR, true);
		Iterator <Tuple> itert = nodeTable.tuples(p,s);
		if (itert.hasNext()){
			return itert.next().getInt(Constants.YEAR);
		}else return 1900;
	}
	
	public int getMaxYear (){
		int row = nodeTable.getMetadata(Constants.YEAR).getMaximumRow();
		Tuple t = nodeTable.getTuple(row);
		if (t.getInt(Constants.YEAR)!=-1){
			return t.getInt(Constants.YEAR);
		}else return 2020;
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(PubData.NEW_DATA)){
			
			initGraph(this.data.getPubMap(),this.data.getCiteMap());
		}
		
	}



	public void clearData() {
		this.setChanged();
		this.notifyObservers(WILL_CHANGE);
		this.nodeTable.clear();
		this.edgeTable.clear();
		this.setChanged();
		this.notifyObservers(CHANGED);
		
		
	}
}
