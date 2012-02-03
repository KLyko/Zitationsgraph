package de.uni.leipzig.asv.zitationsgraph.tests.vis;



import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.logging.impl.LogFactoryImpl;

import de.uni.leipzig.asv.zitationsgraph.tests.data.GraphManager;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.ActionSwitch;
import prefuse.action.RepaintAction;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.Expression;
import prefuse.data.expression.ExpressionVisitor;
import prefuse.data.expression.FunctionTable;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.HoverPredicate;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;

public class PubVis extends Visualization implements Observer{
	
	private static final Logger log = Logger.getLogger(PubVis.class.getName());
	private Graph pubGraph;
	
	public static final String GRAPH = "graph";
	
	public static final String NODES = "graph.nodes";
	
	public static final String EDGES = "graph.edges";
	
	public static final String CLUSTERAGGR = "clusterAggregates";
	
	/**
	 * Property-Name for the current focused Node.
	 */
	public static final String PROPERTY_FOCUS_NODE = "focusNode";
	
	/**
	 * Property name if the focus removed from a node
	 */
	public static final String PROPERTY_REMOVE_FOCUS = "removeFocus";
	
	/**
	 * Actionname for Action, which is responsible for the size of each edge
	 */
	public static final String EDGESIZE = "edgeSize";
	
	/**
	 * Actionname for Action, which is responsible for the size of each node
	 */
	public static final String NODESIZE = "nodeSize";
	
	/**
	 * Actionname for Action, which is responsible for the color of each VisualItem
	 */
	public static final String COLOR = "color";
	
	/**
	 * Actionname for animation
	 */
	public static final String ANIMATETRANSITION = "animatetransition";
	
	
	/**
	 * Group Name for focusItems
	 */
	public static final String FOCUSNODES = "focusnodes";
	
	/**
	 * decorator groupname for mouse over
	 */
	public static final String DECORATORHOVER = "decoratorhover"; 
	
	
	/**
	 * decorator groupname for edge decorators
	 */
	public static final String DECORATOREDGE = "decoratoredge";
	
	/**
	 * name for spring layout
	 */
	private static final String SPRING = "spring";
	
	private static final String FISH_FILTER = "fishEyeFilter";
	
	
	
	public static final String DECORATORNODENAME = "nodeName";

	private static final String LAYOUTSWITCH = "layoutSwitch";
	
	
	
	/**
	 * Schema for decorators
	 */
	private static Schema LABEL_SCHEMA = PrefuseLib.getVisualItemSchema();
	    static {
	       LABEL_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
	       LABEL_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(100));
	       LABEL_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma",10));
	       LABEL_SCHEMA.setDefault(VisualItem.FILLCOLOR,ColorLib.rgb(205, 197, 191));
	       LABEL_SCHEMA.addColumn(VisualItem.LABEL, String.class);
	       LABEL_SCHEMA.setDefault(VisualItem.VISIBLE, false);
	       LABEL_SCHEMA.setDefault(VisualItem.STROKECOLOR, ColorLib.gray(100));
	       LABEL_SCHEMA.setDefault(VisualItem.SHAPE, Constants.SHAPE_RECTANGLE);
	        
	        
	    }
	
// - ATTRIBUTES: --------------------------------------------------------------
		    
	 /**
	 * ShapeAction for nodes
	 */
	private ShapeAction shapeAction;
	
	/**
	 * Animation ActionList
	 */
	private ActionList animate;
	
	
	/**
	 * Actions for the Color of an Item
	 */
	private ActionList color;
	
	/**
	 * ActionSwitcher for the layouts
	 */
	private ActionSwitch layoutSwitcher;
	
/*------------------------------Layouts------------------------------------*/	
	/**
	 * Springlayout
	 */
	private SpringLayout spring;
	
	
/*-------------------------------------Decorators---------------------------------------*/	
	
	
	/**
     * show all Labels
     */
   private boolean isDecoratorHide; 
    
    
	
    /**
	 *decorator layout for mouse over
	 */
	private DecoratorHoverLayout decoratorLayout;
	
	/**
	 * decorator layout to show all Labels with ICDCode or diseasename
	 */
	private DecoratorLabelLayout  decoratorLabelLayout;
	
	/**
	 * decorator layout for the edges for phi and rr weight
	 */
	private DecoratorEdgeLayout decoratorEdgeLayout;
	
	/**
	 * ActionList for decoratorHoverLayout
	 */
	private ActionList decoratorNodeHover;
	
	/**
	 * ActionList for decoratorLabelLayout
	 */
	private ActionList decoratorNodeLabel;
	
	/**
	 * Action Lsit for EdgeLayout
	 */
	private ActionList decoratorEdgeLabel;
	
	
	
	
	private PropertyChangeSupport changes;
	
	private GraphManager gm ;

	private GraphDistanceFilter ftf;
	private NodeVisibilityAction nodeVisAction;
	

	
// - CONSTRUCTORS: ------------------------------------------------------------
	
	/**
	 * Mapping of the Datagraph to the Visualization
	 * and inizialize the layouts, color features, size features, animations, focusgroups
	 */
	public PubVis(GraphManager gm) {
		super();
		/*Mapping*/
		this.gm =gm;
		gm.addObserver(this);
		pubGraph = this.gm.getGraph();
		this.add(GRAPH,pubGraph);
		//this.setInteractive(EDGES, null, true);	
		
		
		
		this.initDecorators();
		/* friendly child mode
		LabelRenderer imageRend = new LabelRenderer (null ,GraphManager.IMAGE);
		imageRend.setTextField(null);
       		imageRend.setVerticalAlignment(Constants.BOTTOM);
        	imageRend.setHorizontalPadding(0);
        	imageRend.setVerticalPadding(0);
        	imageRend.setMaxImageDimensions(20,20);
		*/
		/*Create DefaulRenderer FActory*/
		DefaultRendererFactory rendererFac=new DefaultRendererFactory();
		
		
		//LabelRenderer label = new LabelRenderer(data.Constants.NAME);
		//label.setRoundedCorner(50, 50);
		//rendererFac.add(new InGroupPredicate(NODES), label);
        
		/*Decorator LabelRenderer for show all Labels*/
		rendererFac.add(new InGroupPredicate(DECORATORNODENAME),
				new LabelRenderer(VisualItem.LABEL));
		
		/* friendly child mode
		rendererFac.add(new InGroupPredicate(NODES), imageRend);
		*/
		
		/*Decorator LabelRenderer for mouse over*/
		rendererFac.add(new InGroupPredicate(DECORATORHOVER),
				new LabelRenderer(VisualItem.LABEL));
		
		/*Decorator LabelRenderer for edges*/
		rendererFac.add(new InGroupPredicate(DECORATOREDGE),
				new LabelRenderer(VisualItem.LABEL));
		
		rendererFac.add(new InGroupPredicate(EDGES),
				new EdgeRenderer(Constants.EDGE_TYPE_LINE,Constants.EDGE_ARROW_FORWARD));
		
		this.setRendererFactory(rendererFac);
		
		
		/*FocusGroup*/
		this.addFocusGroup(FOCUSNODES);
		
		/*List for size of VisualItems
		 * includes a instance of the nodeSizeAction and an instance of the EdgeSizeAction
		 */
		

		
/*---------------item color----------------------------------------------------------*/		
			
	    initColorAction();
		
/*----------------------------quality renderer and circle node Action----------------*/		
		initQualityAction();
        

/*----------------------------init Layouts----------------------------------------*/        
				
		initLayout();
/*---------------------------init PropertyChangeSupport--------------------------------*/
      
		initFilters();
		
		changes = new PropertyChangeSupport(this);
        
	}
	

/*-------------------------------private methods-------------------------------------------*/	
	
	
	
	/**
	 * Action for the color of the VisualItems
	 */
	private void initColorAction(){
		color=new ActionList(Activity.INFINITY);
		NodeColorAction fill = new NodeColorAction();
		//DataColorAction yearColor = new DataColorAction (NODES, Constants.YEAR, Constants.NUMERICAL, VisualItem.FILLCOLOR,ColorLib.getCoolPalette(10));
		ColorAction nodeStroke=new ColorAction(NODES,
				VisualItem.STROKECOLOR, ColorLib.gray(100));
		nodeStroke.add(VisualItem.HIGHLIGHT, ColorLib.rgb(139,90,0));
		nodeStroke.add(VisualItem.FIXED, ColorLib.rgb(255, 0, 0));
		ColorAction edge=new ColorAction(EDGES,
				VisualItem.STROKECOLOR, ColorLib.gray(100));
		ColorAction edgeArr = new ColorAction (EDGES, VisualItem.FILLCOLOR,ColorLib.gray(100));
		//ColorAction text = new ColorAction(NODES,VisualItem.TEXTCOLOR,ColorLib.gray(0));

		//comment out for the friendly child mode
		color.add(nodeStroke);
		color.add(fill);
		color.add(edge);
		color.add(edgeArr);
		//color.add(text);
		color.add(new RepaintAction());
		this.putAction(COLOR, color);
	}
	
	/**
	 * add the specific Decorators, which show the informations of a disease in a rectangle 
	 * beside the node of the disease and accordingly the edge weight in the center of an edge.<br>
	 * The DecoratorLayouts control the position of the Display, control if the decorator
	 * is visible or not and the text content of each decorator
	 */
	private void initDecorators(){
		
	
		/*Decorator of nodes for all labels*/
		this.addDecorators(DECORATORNODENAME, NODES, LABEL_SCHEMA);
		/*Decorators of nodes for mouseover*/
		this.addDecorators(DECORATORHOVER, NODES, new HoverPredicate(), LABEL_SCHEMA);
		/*Decorators for Edges*/
		this.addDecorators(DECORATOREDGE, EDGES, LABEL_SCHEMA);
		/*create the instances of the Decorator Layout*/
		this.decoratorLayout = new DecoratorHoverLayout(DECORATORHOVER);
		this.decoratorLabelLayout = new DecoratorLabelLayout(DECORATORNODENAME);
		this.decoratorEdgeLayout = new DecoratorEdgeLayout(DECORATOREDGE);
		
		decoratorNodeHover = new ActionList(Activity.INFINITY);
		decoratorNodeLabel = new ActionList(Activity.INFINITY);
		decoratorEdgeLabel = new ActionList(Activity.INFINITY);
		
		decoratorNodeHover.add(decoratorLayout);
		decoratorNodeHover.add(new RepaintAction());
		decoratorNodeLabel.add(decoratorLabelLayout);
		decoratorNodeLabel.add(new RepaintAction());
		decoratorEdgeLabel.add(decoratorEdgeLayout);
		decoratorEdgeLabel.add(new RepaintAction());
		
		this.putAction("decoratorHover", decoratorNodeHover);
		this.putAction("decoratorLabel", decoratorNodeLabel);
		this.putAction("decoratorEdgeLabel", decoratorEdgeLabel);
	}
	
	/**
	 * Action, which is responsible for the highquality rendering and the round 
	 * shape of each node
	 */
	private void initQualityAction(){
		animate= new ActionList(200);
        
		animate.add(new QualityControlAnimator());
        animate.add(new RepaintAction());
        /*ShapeAction for Nodes*/
		shapeAction=new ShapeAction(NODES, Constants.SHAPE_ELLIPSE);
       SizeAction size = new SizeAction(NODES, 1);
       animate.add(size);
		animate.add(shapeAction);
        this.putAction("animate", animate);
	}
	
	/**
	 * initialize the Layouts spring, circle,Hierarchical and radialHierarchical
	 * the layoutSwitch, which is an instance of the ActionSwitch class, include all layouts,
	 * that is possible to switch the layouts
	 * the order of Adding the layouts is the order of the list of the GUI 
	 */
	private void initLayout(){
		spring = new SpringLayout();
		 ftf= new GraphDistanceFilter(GRAPH,FOCUSNODES,4);
		
		layoutSwitcher=new ActionSwitch();
		
		
		//layoutSwitcher.setStepTime(200);
		layoutSwitcher.setDuration(Activity.INFINITY);
		
		layoutSwitcher.add(spring);
		
	   
		
		this.putAction(LAYOUTSWITCH, layoutSwitcher);
        //this.putAction(SPRING, spring);
        	
	}	
	
	private void initFilters(){
		ActionList filter = new ActionList ();
		filter.add(ftf);
		this.putAction(FISH_FILTER, filter);
		
		
		ActionList filter2 = new ActionList();
		 nodeVisAction= new NodeVisibilityAction();
		
		
		filter2.add(nodeVisAction);
		this.putAction("relationFilter", filter2);
		
	}
/*----------------------------------public methodes------------------------------------------------*/	
	

	
	/**
	 * all Actions will be run
	 */
	public void runActions(){
	
		if(this.getGroup("graph").getTupleCount()!=0){
			animate.run();
			color.run();
			decoratorNodeLabel.run();
			decoratorEdgeLabel.run();
			decoratorNodeHover.run();
			layoutSwitcher.run();
			this.getAction("relationFilter").run();
			VisiblePredicate p = new VisiblePredicate ();
			Iterator <Tuple> iter = this.getGroup(EDGES).tuples(p);
			int edgeCount=0;
			while (iter.hasNext()){
				iter.next();
				edgeCount++;
			}
			log.info("#edges"+edgeCount);
			 iter = this.getGroup(NODES).tuples(p);
			int nodeCount=0;
			while (iter.hasNext()){
				iter.next();
				nodeCount++;
			}
			log.info("#nodes"+nodeCount);
		}
		this.repaint();
		
	}
	
	/**
	 * stop all Actions  
	 */
	public void stopActions(){
		
		color.cancel();
		animate.cancel();
		decoratorEdgeLabel.cancel();
		decoratorNodeLabel.cancel();
		decoratorNodeHover.cancel();
		layoutSwitcher.cancel();
		
		
		
	}
	
	/**
	 * method to get the node, which has the focus
	 * @return the FocusVisualNode as visualItem
	 */
	
	public VisualItem getFocusItem(){
		Iterator <VisualItem> visualIter =
		getFocusGroup(FOCUSNODES).tuples();
		VisualItem focusNode = null;
		if (visualIter.hasNext()){
			focusNode = visualIter.next();
		}
		return focusNode;
	}
		
	
	public void setFocusNodeItem(int row) {
		
		//Visual Item per icdCode erstmal raussuchen
		Iterator<VisualItem> focusIter;
		focusIter = getVisualGroup(NODES).tuples();
		Tuple t; 
		while(focusIter.hasNext()){
			t= focusIter.next();
			if(t.getRow()==row){
			this.setFocusNodeItem((VisualItem)t);
			break;
			}
		}
	}
	
	/**
	 * set the Focus of a VisualItem
	 * @param item VisualItem, which has the focus
	 */
	
	public void setFocusNodeItem(VisualItem item) {
		
		Iterator<VisualItem> focusNodes = 
			 getFocusGroup(FOCUSNODES).tuples();
		
		
		VisualItem oldItem = null;
		if (focusNodes.hasNext()) {
			oldItem = focusNodes.next();
		}
		this.getFocusGroup(FOCUSNODES).clear();
		this.getFocusGroup(FOCUSNODES).addTuple(item);
		if(oldItem == null){//kein Knoten war fokussiert feuer
			changes.firePropertyChange(PROPERTY_FOCUS_NODE, oldItem, item);
		} else { 
			if(oldItem.getRow()
					!=item.getRow()){
				//oldItem ist nicht der selber wie neuer also feuer frei
				changes.firePropertyChange(PROPERTY_FOCUS_NODE, oldItem, item);	
			}
		}
	}//Method set FocusNode
	
	/**
	 * remove the Focus, that no Node is focused 
	 */
	
	public void removeFocusNode(){
		
		this.stopActions();
		Iterator<VisualItem> focusNodes = 
			 getFocusGroup(FOCUSNODES).tuples();
		VisualItem oldItem = null;
		if (focusNodes.hasNext()) {
			oldItem = focusNodes.next();
		}
		
		if(oldItem!=null){
			this.getFocusGroup(FOCUSNODES).clear();
			changes.firePropertyChange(PROPERTY_REMOVE_FOCUS,oldItem, null);
		}
		this.runActions();
	}
	
	/**
	 * get the visulaitem whth the secified Icdcode
	 * @param name specified Code for the disease
	 * @return visualnode
	 */
	
	public VisualItem getVisualItem(String name){
		Iterator<VisualItem> nodeIter;
		Predicate getNode = ExpressionParser.predicate(
				"'"+name+"' = "+de.uni.leipzig.asv.zitationsgraph.tests.data.Constants.TITLE);
		nodeIter = getVisualGroup(NODES).tuples(getNode);
		if(nodeIter.hasNext()){
			return nodeIter.next();
		}
		  return null;
	}
	
	/**
	 * stop the spring layout
	 * If the movements of the nodes are too fast, the user can stop the movements 
	 */
	public void stopSpringLayout(){
		if(layoutSwitcher.getSwitchValue()==0){
			layoutSwitcher.cancel();
		}
	}
	
	/**
	 * start the spring layout
	 */
	public void runSpringLayout(){
		if(this.getGroup("graph").getTupleCount()!=0){
			if(layoutSwitcher.getSwitchValue()==0){
				layoutSwitcher.run();
			}
		}
	}
	
	/**
	 * override method of the observer interface
	 * the update method is responsible to stop and start the action,
	 * if the data graph will explored or cleared and accordingly the graph is explored
	 * The Visualization will also clear his visual groups, if the graph will be clear
	 */
	
	public void update(Observable o, Object message) {
		if(message.equals(GraphManager.WILL_CHANGE)){
			this.removeFocusNode();
			System.out.println(this.getGroup(NODES).getTupleCount());
			this.stopActions();
		}else if (message.equals(GraphManager.CHANGED)){
			this.runActions();
		}
	}


	public void runFilter() {
		if (this.getAction(FISH_FILTER).isRunning())
			this.getAction(FISH_FILTER).cancel();
		this.getAction(FISH_FILTER).run();
	}
	
	public void stopFilter (){
		this.getAction(FISH_FILTER).cancel();
		this.getFocusGroup(FOCUSNODES).clear();
		this.spring.run();
	}


	public void setRefFilter(int ref) {
		this.getAction("relationFilter").cancel();
		this.nodeVisAction.setRefCount(ref);
		this.getAction("relationFilter").run();
		
	}
}
