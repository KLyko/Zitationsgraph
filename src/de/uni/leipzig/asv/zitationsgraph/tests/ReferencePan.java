package de.uni.leipzig.asv.zitationsgraph.tests;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import prefuse.Display;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.util.display.ExportDisplayAction;
import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.tests.controls.DisplayControl;
import de.uni.leipzig.asv.zitationsgraph.tests.data.GraphManager;
import de.uni.leipzig.asv.zitationsgraph.tests.data.PubData;
import de.uni.leipzig.asv.zitationsgraph.tests.vis.PubVis;

import java.awt.BorderLayout;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class ReferencePan extends javax.swing.JPanel implements PropertyChangeListener,Observer{
	private JTextPane jTextPane1;
	private EntityTree referenceTree;
	private GraphToolbar graphToolbar1;

	private JTabbedPane partsTabbedPane;
	private JPanel graphTreeSplitPane;
	private String currentDoc;
	private DefaultMutableTreeNode headRoot;
	private DefaultTreeModel headTreeModel;
	private EntityTree headTree;
	private PubData data;
	private JSplitPane sourceResultSplitPane;
	private GraphManager gm;
	private PubVis vis;
	private Display graphDisplay ;
	private static final Logger log = Logger.getLogger(ReferencePan.class.getName());

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new ReferencePan());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public ReferencePan() {
		super();
		initGUI();
		
		
		
	}
	
	public ReferencePan(PubData data) {
		super();
		this.data = data;
		gm = new GraphManager (this.data);
		gm.addObserver(this);
		vis = new PubVis(gm);
		graphDisplay =new Display(vis);
		this.data.addPropertyChangeListener(this);
		initGUI();
		
	}

	private void initGUI() {
		try {
			
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			{
				graphTreeSplitPane = new JPanel();
				BorderLayout graphTreeSplitPaneLayout = new BorderLayout();
				graphTreeSplitPane.setLayout(graphTreeSplitPaneLayout);
				this.add(getGraphToolbar1(), BorderLayout.NORTH);
				this.add(graphTreeSplitPane, BorderLayout.CENTER);
				
				
				{
					
					JScrollPane refTreePane = new JScrollPane();
					refTreePane.setViewportView(this.initRefTree());
					JScrollPane headTreePane = new JScrollPane();
					headTreePane.setViewportView(this.initHeadTree());
					
					partsTabbedPane = new JTabbedPane();
					partsTabbedPane.addTab("head Tree", headTreePane);
					partsTabbedPane.addTab("reference Tree", refTreePane);
					sourceResultSplitPane = new JSplitPane();
					graphTreeSplitPane.add(sourceResultSplitPane, BorderLayout.CENTER);
					sourceResultSplitPane.add(partsTabbedPane, JSplitPane.LEFT);
				}
				{
					
					graphDisplay.addControlListener(new PanControl());
					graphDisplay.addControlListener(new ZoomControl());
					graphDisplay.addControlListener(new DragControl());
					graphDisplay.addControlListener(new DisplayControl(vis,gm));
					sourceResultSplitPane.add(graphDisplay, JSplitPane.RIGHT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private EntityTree initRefTree(){
		
		referenceTree = new EntityTree(EntityTree.REFERENCE_TREE);
		return referenceTree;
	}
	
	private JTree initHeadTree(){
		headTree = new EntityTree (EntityTree.HEAD_TREE);
		return headTree;
	}
	
	private GraphToolbar getGraphToolbar1() {
		if(graphToolbar1 == null) {
			graphToolbar1 = new GraphToolbar(this);
		}
		return graphToolbar1;
	}

	public void animateLayout(boolean isAnimate){
		if (isAnimate)
			vis.runSpringLayout();
		else 
			vis.stopSpringLayout();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof PubData){
		if (evt.getPropertyName().equals(SourcePanel.NEW_DOC)){
			String[] fileName = ((String)evt.getNewValue()).split("\\\\");
			currentDoc=fileName[fileName.length-1];
			referenceTree.setCurrentDoc(currentDoc);
			headTree.setCurrentDoc(currentDoc);
			
			
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_REF_VECTOR)){
			Vector<Citation> citList = (Vector<Citation>) evt.getNewValue();
			referenceTree.updateTree(citList);
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_HEAD_ENTITIES)){
			headTree.updateTree(evt.getNewValue());
		}
		}else if (evt.getSource() instanceof SourcePanel){
			if (evt.getPropertyName().equals(SourcePanel.RESET)){
				this.referenceTree.resetTree();
				this.headTree.resetTree();
				this.gm.clearData();
				currentDoc ="";
				
				
			}
		}
		
	}

	public Action getImageAction() {
		return new ExportDisplayAction(graphDisplay);
	}

	
	
	public void setNumOfRef(int ref){
		vis.setRefFilter (ref);
	}
	
	public void setYearBegin(int year){
		vis.setYearBegin(year);
	}

	public void setIsGraphVis(boolean isGraph){
		this.data.setGraphVis(isGraph);
	}
	
	public void storeInDb(boolean isStoreInDB){
		this.data.setStoreInDb(isStoreInDB);
	}
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof GraphManager){
			if (arg.equals(GraphManager.CHANGED)){
				log.info ("graph is ready");
				this.graphToolbar1.setExtremes (gm.getMinYear(),gm.getMaxYear());
			}
		}	
	}
	
	

}
