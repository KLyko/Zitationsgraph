package de.uni.leipzig.asv.zitationsgraph.tests.controls;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import de.uni.leipzig.asv.zitationsgraph.tests.data.GraphManager;
import de.uni.leipzig.asv.zitationsgraph.tests.vis.PubVis;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.visual.VisualItem;


	public class DisplayControl extends ControlAdapter{
		
		private VisualItem activeItem;
	    protected Point2D down = new Point2D.Double();
	    protected Point2D temp = new Point2D.Double();
	    protected boolean dragged;
	    protected Point2D sP = null;
		protected Point2D tP = null;
	    

	//- ATTRIBUTES: --------------------------------------------------------------

	/** the Visualization, that is affected by events and functions,
	 * managed by this <code>CVDisplayControl</code>. */
	private PubVis vis;
	private GraphManager gm;
	private int source = -1;
	private int target = -1;

	//- CONSTRUCTORS: ------------------------------------------------------------

	/**
	 * Hier eventuell doch den Standardkonstruktor OHNE Parameter nehmen
	 * und die Visualisierung dann vom Display selbst zurückgeben lassen
	 * (und natüclich noch zu (CVVisualization) parsen!
	 */
	public DisplayControl(PubVis vis,GraphManager gm) {
		super();
		this.vis=vis;
		this.gm =gm;
	}


	//- OVERRIDDEN-METHODS: ------------------------------------------------------

	/**
	 * If a visual Item, in fact a node, is double-clicked, the 
	 * <code>GraphManger</code> will try to explore this node.
	 */
	@Override
	public void itemClicked(VisualItem item, MouseEvent e)
	{
		
		Display d = (Display)e.getSource();
		if(e.getClickCount()==1){
			if(item instanceof Node){
				
				vis.setFocusNodeItem(item);
				
			}
		}
	}
}
