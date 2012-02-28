package de.uni.leipzig.asv.zitationsgraph.tests.vis;

import java.util.Iterator;

import de.uni.leipzig.asv.zitationsgraph.tests.data.Constants;

import prefuse.action.Action;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class NodeColorAction extends Action{
	public NodeColorAction(){
		super();
		
	}
	@SuppressWarnings("unchecked")
	@Override
	public void run(double frac) {
		// TODO Auto-generated method stub
		synchronized(m_vis.getFocusGroup(PubVis.FOCUSNODES)){
			VisualItem node;
			int category = -1;
			Iterator<VisualItem> nodeIter = (Iterator<VisualItem>)m_vis.getVisualGroup
			(PubVis.NODES).tuples();
			while(nodeIter.hasNext()){
				
				node = nodeIter.next();
				node.setHighlighted(false);
				//if (node.getInt(Constants.COLOR)!= -1){
					//node.setFillColor(node.getInt(Constants.COLOR));
				//}else {
					node.setFillColor(ColorLib.rgb(220,100, 0));
				//}
				if(m_vis.getFocusGroup(PubVis.FOCUSNODES).containsTuple(node)){
					//System.out.println("opaque");
					node.setFillColor(ColorLib.rgba(120, 60,0, 125));
				}//if (containsTuple)
				
				
				
			}//while()
		}
	}//run()
}
