package de.uni.leipzig.asv.zitationsgraph.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;


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
* 
* This class represent a tree for the entities of the head, reference or
* the body part
*/
public class EntityTree extends javax.swing.JTree {

	/*
	 * tree types
	 */
	public static final int HEAD_TREE = 0;
	
	public static final int REFERENCE_TREE = 1;
	
	public static final int BODY_TREE =2;
	
	static final String SELECT_DOC = "selectedDoc";
	
	
	
	private DefaultTreeModel treeModel;

	private DefaultMutableTreeNode root;
	
	
	/**
	 * root node of an entity for a citation list or head extraction entity with his title, author
	 * etc.
	 */
	private String currentDoc;
	
	private int type;
	
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new EntityTree(0));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public EntityTree(int type) {
		super();
		this.setType(type);
		initGUI();
	}
	
	public EntityTree() {
		super();
		initGUI();
	}
	
	
	
	private void initGUI() {
		try { 
			{
				root = new DefaultMutableTreeNode("documents");
				treeModel = new DefaultTreeModel(root);
				this.setModel(treeModel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This listener is used to get events when the first node after
	 * the root is selected.
	 * The parent component should be a propertychangelistener and should react 
	 * of the event name SELECT_DOC 
	 */
	public void addDocSelectionListener (){
		this.getSelectionModel().
		
		addTreeSelectionListener(new TreeSelectionListener(){
			
			

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getPath().getPath().length ==2){
					System.out.println(e.getPath().getPath()[1].toString());
					firePropertyChange(SELECT_DOC, "",e.getPath().getPath()[1].toString());	
				}
			}
			
		});
	}
	
	/**
	 * This fill the tree with the current object
	 * The building depends on the type of the initialized tree
	 * @param o
	 */
	public void updateTree (Object o){
		switch (type){
		case HEAD_TREE:
			this.updateHeadTree((Publication)o);
			break;
		case REFERENCE_TREE:
			this.updateRefTree((Vector<Citation>)o);
			break;
		case BODY_TREE:
			this.updateBodyTree((HashSet <String>)o);
		}
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
			root.add(doc);
			this.treeModel.reload();
		}
	}
	
	
	
	private void updateHeadTree(Publication pub){	
		DefaultMutableTreeNode docNode = new DefaultMutableTreeNode(currentDoc);
		root.add(docNode);
		DefaultMutableTreeNode title =new DefaultMutableTreeNode (
				(pub.getTitle()!=null)
				?pub.getTitle():"");
		docNode.add(title);
		DefaultMutableTreeNode authors  = new DefaultMutableTreeNode("authors");
		title.add(authors);
		for (Author author: pub.getAuthors()){
			authors.add(new DefaultMutableTreeNode(author.getName()));
		}
		this.treeModel.reload();
	}
	
	private void updateBodyTree(HashSet<String> sentences){	
		DefaultMutableTreeNode docNode = new DefaultMutableTreeNode(currentDoc);
		root.add(docNode);
		for (String sentence :sentences){
			String[] sentenceSplit =sentence.split("\\s");
			String formatSentence ="";
			for (int i = 0;i<sentenceSplit.length;i++){
				if (i%10 ==0 &&i != 0){
					formatSentence += sentenceSplit[i]+System.getProperty("line.separator");
				}else {
					formatSentence += sentenceSplit[i]+" ";
				}
			}
			DefaultMutableTreeNode sentNode= new DefaultMutableTreeNode (formatSentence);
			docNode.add(sentNode);
		}
		this.treeModel.reload();
	}
	
	public void resetTree(){
		this.root.removeAllChildren();
		this.treeModel.reload();
	}

	public String getCurrentDoc() {
		return currentDoc;
	}

	public void setCurrentDoc(String currentDoc) {
		this.currentDoc = currentDoc;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
}
