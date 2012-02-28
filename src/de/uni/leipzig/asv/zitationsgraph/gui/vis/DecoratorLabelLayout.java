package de.uni.leipzig.asv.zitationsgraph.gui.vis;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import de.uni.leipzig.asv.zitationsgraph.gui.data.Constants;

import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class DecoratorLabelLayout extends Layout{
	public DecoratorLabelLayout(String group){
		super(group);
		
	}

	/**
	 * override the run Action of prefuse
	 * if all Decorators are hide, the decororators visible is false 
	 */
	@Override
	public void run(double frac) {
		// TODO Auto-generated method stub
		
		if(this.m_vis.getGroup("graph").getTupleCount()!=0){
			Iterator iter = m_vis.items(PubVis.DECORATORNODENAME);
			
			while ( iter.hasNext() ) {
				
				DecoratorItem item = (DecoratorItem)iter.next();
				
					/*all Decorators will be shown*/
					VisualItem node = item.getDecoratedItem();
					Rectangle2D decBounds = item.getBounds(); 
					Rectangle2D bounds = node.getBounds();
					setX(item, null, bounds.getMinX());
					setY(item, null, bounds.getMaxY());
					
						/* text of Decorators are ICD-Codes*/
						item.setString(VisualItem.LABEL, node.getString(Constants.TITLE));
						
						
				
				
			}
		}
		
	}
}
