package de.uni.leipzig.asv.zitationsgraph.tests;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
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
public class ReferencePan extends javax.swing.JPanel implements PropertyChangeListener{
	private JTextPane jTextPane1;
	private JTree referenceTree;
	private DefaultTreeModel refTreeModel;
	private DefaultMutableTreeNode refRoot;
	private static HashMap<String, String> refPartMap;
	private static HashMap<String,String> headPartMap;
	private GraphToolbar graphToolbar1;

	private JTabbedPane jTabbedPane1;
	private JSplitPane jSplitPane1;
	private String currentDoc;
	private DefaultMutableTreeNode headRoot;
	private DefaultTreeModel headTreeModel;
	private JTree headTree;
	private PubData data;
	private JSplitPane jSplitPane2;
	private GraphManager gm;
	private PubVis vis;
	private Display d ;

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
		refPartMap = new HashMap<String,String>();
		headPartMap = new HashMap<String,String>();
		
		
	}
	
	public ReferencePan(PubData data) {
		super();
		
		refPartMap = new HashMap<String,String>();
		headPartMap = new HashMap<String,String>();
		this.data = data;
		gm = new GraphManager (this.data);
		vis = new PubVis(gm);
		d =new Display(vis);
		this.data.addPropertyChangeListener(this);
		initGUI();
		
	}

	private void initGUI() {
		try {
			
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			{
				jSplitPane1 = new JSplitPane();
				this.add(getGraphToolbar1(), BorderLayout.NORTH);
				this.add(jSplitPane1, BorderLayout.CENTER);
				jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
				{
					JScrollPane textPane = new JScrollPane();
					jSplitPane1.add(textPane, JSplitPane.BOTTOM);
					jTextPane1 = new JTextPane();
					textPane.setViewportView(jTextPane1);
					jTextPane1.setText("jTextPane1");
				}
				{
					
					JScrollPane refTreePane = new JScrollPane();
					refTreePane.setViewportView(this.initRefTree());
					JScrollPane headTreePane = new JScrollPane();
					headTreePane.setViewportView(this.initHeadTree());
					
					jTabbedPane1 = new JTabbedPane();
					jTabbedPane1.addTab("head Tree", headTreePane);
					jTabbedPane1.addTab("reference Tree", refTreePane);
					jSplitPane2 = new JSplitPane();
					jSplitPane1.add(jSplitPane2,JSplitPane.TOP);
					jSplitPane2.add(jTabbedPane1, JSplitPane.LEFT);
				}
				{
					
					d.addControlListener(new PanControl());
					d.addControlListener(new ZoomControl());
					d.addControlListener(new DragControl());
					d.addControlListener(new DisplayControl(vis,gm));
					jSplitPane2.add(d, JSplitPane.RIGHT);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private JTree initRefTree(){
		refRoot = new DefaultMutableTreeNode("documents");
		refTreeModel = new DefaultTreeModel(refRoot);
		
		referenceTree = new JTree(refTreeModel);
		referenceTree.getSelectionModel().
		
		addTreeSelectionListener(new TreeSelectionListener(){
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getPath().getPath().length ==2){
					System.out.println(e.getPath().getPath()[1].toString());
					jTextPane1.setText(refPartMap.get(e.getPath().getPath()[1].toString()));
				}else if (e.getPath().getPath().length ==4){
					//not efficient but the text is small
					int start = jTextPane1.getText().indexOf(e.getPath().getPath()[3].toString());
					int end =start+e.getPath().getPath()[2].toString().length();
					jTextPane1.select(start, end);
				}
			}
			
		});
		return referenceTree;
	}
	
	private JTree initHeadTree(){
		headRoot = new DefaultMutableTreeNode("documents");
		headTreeModel = new DefaultTreeModel(headRoot);
		
		headTree = new JTree(headTreeModel);
		headTree.getSelectionModel().
		
		addTreeSelectionListener(new TreeSelectionListener(){
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getPath().getPath().length ==2){
					System.out.println(e.getPath().getPath()[1].toString());
					jTextPane1.setText(headPartMap.get(e.getPath().getPath()[1].toString()));
				}
			}
			
		});
		return headTree;
	}
	
	private void updateRefTree(Vector<Citation> citList){
		DefaultMutableTreeNode doc = new DefaultMutableTreeNode(currentDoc);
		for (Citation cit : citList){
			DefaultMutableTreeNode title  = new DefaultMutableTreeNode("title");
			title.add(new DefaultMutableTreeNode(cit.getPublication().getTitle()));
			doc.add(title);
			DefaultMutableTreeNode authors  = new DefaultMutableTreeNode("authors");
			title.add(authors);
			for (Author author: cit.getPublication().getAuthors()){
				authors.add(new DefaultMutableTreeNode(author.getName()));
			}
			DefaultMutableTreeNode year  = new DefaultMutableTreeNode ("year");
			year.add( new DefaultMutableTreeNode(cit.getPublication().getYearString()));
			title.add(year);
			refRoot.add(doc);
			this.refTreeModel.reload();
		}
	}
	
	private void updateHeadTree(Document doc){
		
	
		DefaultMutableTreeNode docNode = new DefaultMutableTreeNode(currentDoc);
		
		headRoot.add(docNode);
		DefaultMutableTreeNode title =new DefaultMutableTreeNode (
				(doc.getPublication().getTitle()!=null)
				?doc.getPublication().getTitle():"");
		docNode.add(title);
		this.headTreeModel.reload();
		
		
	}
	private GraphToolbar getGraphToolbar1() {
		if(graphToolbar1 == null) {
			graphToolbar1 = new GraphToolbar(this);
		}
		return graphToolbar1;
	}
	
	
	
	public static HashMap<String, String> getHeadPartMap() {
		return headPartMap;
	}

	
	
	public  static HashMap<String, String> getRefPartMap() {
		return refPartMap;
	}
	
	public void animateLayout(boolean isAnimate){
		if (isAnimate)
			vis.runSpringLayout();
		else 
			vis.stopSpringLayout();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(SourcePanel.NEW_DOC)){
			String[] fileName = ((String)evt.getNewValue()).split("\\\\");
			currentDoc=fileName[fileName.length-1];
			System.out.println(currentDoc);
			refPartMap.put(currentDoc,null);
			headPartMap.put(currentDoc, null);
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_REF_PART)){
			refPartMap.put(currentDoc,(String) evt.getNewValue());
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_HEAD_PART)){
			headPartMap.put(currentDoc, (String) evt.getNewValue());
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_REF_VECTOR)){
			Vector<Citation> citList = (Vector<Citation>) evt.getNewValue();
			this.updateRefTree(citList);
		}else if (evt.getPropertyName().equals(SourcePanel.NEW_HEAD_ENTITIES)){
			this.updateHeadTree((Document)evt.getNewValue());
		}else if (evt.getPropertyName().equals(SourcePanel.RESET)){
			refPartMap.clear();
			headPartMap.clear();
			refRoot.removeAllChildren();
			headRoot.removeAllChildren();
			currentDoc ="";
			this.headTreeModel.reload();
			this.refTreeModel.reload();
		}
		
	}

	public Action getImageAction() {
		return new ExportDisplayAction(d);
	}

	public void resetFilter() {
		vis.stopFilter();
		
	}
	
	public void setNumOfRef(int ref){
		vis.setRefFilter (ref);
	}
	
	

}
