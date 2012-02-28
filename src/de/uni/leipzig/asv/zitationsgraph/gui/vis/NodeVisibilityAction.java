package de.uni.leipzig.asv.zitationsgraph.gui.vis;

import java.util.Iterator;

import de.uni.leipzig.asv.zitationsgraph.gui.data.Constants;

import prefuse.action.Action;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class NodeVisibilityAction extends Action{

	Predicate refFilter ;
	int references =1;
	int year =1900;
	public NodeVisibilityAction (){
		super();
		
		refFilter = ExpressionParser.predicate("indegree()>="+references+
				" AND ( "+Constants.YEAR+">= "+year+" OR " +Constants.YEAR+" = -1 )");
	}
	@Override
	public void run(double frac) {
		Iterator <Tuple> itemIter = m_vis.getVisualGroup(PubVis.GRAPH).tuples();
		while (itemIter.hasNext()){
			((VisualItem)itemIter.next()).setVisible(false);
		}
		Iterator <Tuple> iter = m_vis.getVisualGroup(PubVis.NODES).tuples(refFilter);
		while (iter.hasNext()){
			NodeItem referencedNode = (NodeItem) iter.next();
			referencedNode.setVisible(true);
			Iterator <Tuple> edgeIter = referencedNode.inEdges();
			while(edgeIter.hasNext()){
				EdgeItem  edge = (EdgeItem) edgeIter.next();
				edge.getSourceItem().setVisible(true);
				edge.setVisible(true);
			}
		}
	}
	public void setRefCount(int ref) {
		references = ref;
		refFilter = ExpressionParser.predicate("indegree()>="+references+
				" AND ( "+Constants.YEAR+">= "+year+" OR " +Constants.YEAR+" = -1 )");
		
	}
	
	public void setYearMin (int yearMin){
		year= yearMin;
		refFilter = ExpressionParser.predicate("indegree()>="+references+
				" AND ( "+Constants.YEAR+">= "+year+" OR " +Constants.YEAR+" = -1 )");
	}
	
	
	

}
