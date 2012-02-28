package de.uni.leipzig.asv.zitationsgraph.gui.vis;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import de.uni.leipzig.asv.zitationsgraph.gui.data.Constants;

import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class DecoratorHoverLayout extends Layout{
	public DecoratorHoverLayout(String group){
		super(group);
	}
	
	@Override
	public void run(double frac) {
		// TODO Auto-generated method stub
		if(this.m_vis.getGroup("graph").getTupleCount()!=0){
			Iterator iter = m_vis.items(m_group);
			int row=0;
			while ( iter.hasNext() ) {
				DecoratorItem item = (DecoratorItem)iter.next();
				VisualItem node = item.getDecoratedItem();
				Rectangle2D decBounds = item.getBounds(); 
				Rectangle2D bounds = node.getBounds();
				setX(item, null, bounds.getMaxX()+ decBounds.getWidth()/2);
				setY(item, null, bounds.getMaxY()+decBounds.getHeight()/2);
				item.setString(VisualItem.LABEL,
						"title: "+node.getString(Constants.TITLE)
						+"\nyear: " +node.getString(Constants.YEAR));
				item.setVisible(true);
				
			}//while
		}//is graph empty
	}

}
