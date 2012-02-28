package de.uni.leipzig.asv.zitationsgraph.gui.vis;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import de.uni.leipzig.asv.zitationsgraph.gui.data.Constants;
import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class DecoratorEdgeLayout  extends Layout{
	public DecoratorEdgeLayout(String group){
		super(group);
		
	}
	/**
	 * override the run method of an action
	 * iterate over all edges and set the weight as string on the labelfield 
	 * of the decoratoritem
	 */
	@Override
	public void run(double frac) {
		
		Iterator iter = m_vis.items(m_group);
		
		if(m_vis.getGroup("graph").getTupleCount()!=0){
			while ( iter.hasNext() ) {
				DecoratorItem item = (DecoratorItem)iter.next();
				VisualItem edge = item.getDecoratedItem();
				//System.out.println(edge.toString());
				Rectangle2D bounds = edge.getBounds();
				//System.out.println(edge.getEndX());
				setX(item, null, bounds.getMinX()+ bounds.getWidth()/2);
				setY(item, null, bounds.getMinY()+bounds.getHeight()/2);
				//System.out.println(bounds.getMinX());
					
			}
		}
	}
}
