package de.uni.leipzig.asv.zitationsgraph.tests.vis;

import java.util.Iterator;

import prefuse.action.Action;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class NodeVisibilityAction extends Action{

	Predicate refFilter ;
	public NodeVisibilityAction (){
		super();
		refFilter = ExpressionParser.predicate("indegree()>=1");
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
		refFilter = ExpressionParser.predicate("indegree()>="+ref);
		
	}
	

}
