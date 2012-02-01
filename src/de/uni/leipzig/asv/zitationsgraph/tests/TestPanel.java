package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
*/
public class TestPanel extends javax.swing.JPanel implements PropertyChangeListener{
	private static final String CIT = "citation";
	private static final String TITLE = "title";
	private static final String YEAR = "year";
	private static final String AUTHOR = "author";
	private JButton jButton4;
	private JButton jButton3;
	private JButton jButton2;
	private JPanel jPanel3;
	private ButtonGroup buttonGroup1;
	private JPanel jPanel2;
	private JRadioButton yearButton;
	private JRadioButton title;
	private JRadioButton authorButton;
	private JButton jButton1;
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private JTree testInstancetree;
	private JEditorPane entityEditEditor;
	private JTree schemaTree;
	private JPanel testSchemaPan;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel schemaTreeModel;
	private DefaultMutableTreeNode selectedNode;
	private List<String> definedObjects;
	
	private String newSchemaNode;
	protected String objectType;
	private Vector<Citation> testInstance;
	private DefaultMutableTreeNode testRoot;
	private DefaultTreeModel testModel;
	protected DefaultMutableTreeNode currentCitation;
	protected DefaultMutableTreeNode currentAuthors;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new TestPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public TestPanel() {
		super();
		definedObjects = new ArrayList<String>();
		definedObjects.add(CIT);
		definedObjects.add(TITLE);
		definedObjects.add(YEAR);
		definedObjects.add(AUTHOR);
		this.testInstance = new Vector <Citation> ();
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.X_AXIS);
				this.setLayout(thisLayout);
				{
					testSchemaPan = new JPanel();
					BoxLayout testSchemaPanLayout = new BoxLayout(testSchemaPan, javax.swing.BoxLayout.Y_AXIS);
					testSchemaPan.setLayout(testSchemaPanLayout);
					this.add(testSchemaPan);
					{
						
						
						testSchemaPan.add(initSchemaTree());
						testSchemaPan.add(getJScrollPane1());
						testSchemaPan.add(getJPanel1());
					}
				}
				{
					entityEditEditor = new JEditorPane();
					this.add(entityEditEditor);
					entityEditEditor.setText("");
					entityEditEditor.addMouseListener(new MouseListener(){

						

						@Override
						public void mouseClicked(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void mousePressed(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void mouseReleased(MouseEvent e) {
							if (entityEditEditor.getSelectedText()!= null){
								if (testInstance.lastElement()!= null){
									String object =entityEditEditor.getSelectedText();
									if (objectType.equals(TITLE)){
										DefaultMutableTreeNode title = new DefaultMutableTreeNode(TITLE);
										currentCitation.add(title);
										DefaultMutableTreeNode titleInstance = new DefaultMutableTreeNode(object);
										title.add(titleInstance);
										
									}else if (objectType.equals(AUTHOR)){
										if (!currentCitation.isNodeChild(currentAuthors)){
											currentAuthors = new DefaultMutableTreeNode(AUTHOR);
											currentCitation.add(currentAuthors);
										}
										DefaultMutableTreeNode authorInstance = new DefaultMutableTreeNode(object);
										currentAuthors.add(authorInstance);
										
									}else{
										DefaultMutableTreeNode year = new DefaultMutableTreeNode(YEAR);
										DefaultMutableTreeNode yearInstance = new DefaultMutableTreeNode(object);
										year.add(yearInstance);
										currentCitation.add(year);
										
									}
								}
							}
							testModel.reload(currentCitation);
						}

						@Override
						public void mouseEntered(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void mouseExited(MouseEvent e) {
							// TODO Auto-generated method stub
							
						}
						
					});
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveTestFile (File file){
		Publication pub ;
		String title = null; 
		Vector<Author> authors = null;
		String year = null;
		Enumeration<DefaultMutableTreeNode> enumCit = testRoot.children();
		while( enumCit.hasMoreElements()){
			DefaultMutableTreeNode cit = (DefaultMutableTreeNode) enumCit.nextElement();
			Enumeration<DefaultMutableTreeNode> objEnum =  cit.children();
			while (objEnum.hasMoreElements()){
				DefaultMutableTreeNode obj = (DefaultMutableTreeNode) objEnum.nextElement();
				if (obj.toString().equals(TITLE)){
					title = obj.getChildAt(0).toString();
				}else if (obj.toString().equals(AUTHOR)){
					Enumeration<DefaultMutableTreeNode> autEnum =  obj.children();
					authors = new Vector<Author>();
					while(autEnum.hasMoreElements()){
						authors.add(new Author(autEnum.nextElement().toString()));
					}
				}else if (obj.toString().equals(YEAR)){
					year = obj.getChildAt(0).toString();
				}
			}
			pub = new Publication(authors,title);
			pub.setYearString(year);
			this.testInstance.add(new Citation(pub));
		}
		try {
		FileOutputStream fos = new FileOutputStream(file);
		
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.testInstance);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private JTree initSchemaTree(){
		if (schemaTree ==null){
			this.root = new DefaultMutableTreeNode ("schema");
			schemaTreeModel = new DefaultTreeModel(root);
			schemaTree = new JTree(schemaTreeModel);
			schemaTree.addTreeSelectionListener(new TreeSelectionListener(){
			
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					
				}
				
			});
		}
		return schemaTree;
	}
	
	private JPopupMenu showPopup(){
		JPopupMenu obSelMen= new JPopupMenu();
		ButtonGroup group = new ButtonGroup();
		for (String ob : this.definedObjects){
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(ob);
			item.addChangeListener(new ChangeListener(){
				
				@Override
				public void stateChanged(ChangeEvent e) {
					newSchemaNode = ((JRadioButtonMenuItem)e.getSource()).getText();
				}
				
			});
			group.add(item);
			obSelMen.add(item);
		}
		return obSelMen;
	}
	
	private JTree getTestInstancetree() {
		if(testInstancetree == null) {
			testRoot = new DefaultMutableTreeNode ("root");
			testModel = new DefaultTreeModel(testRoot);
			testInstancetree = new JTree(testModel);
			testInstancetree.addTreeSelectionListener(new TreeSelectionListener(){

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
					
				}
				
			});
		}
		return testInstancetree;
	}
	
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTestInstancetree());
		}
		return jScrollPane1;
	}
	
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			BoxLayout jPanel1Layout = new BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS);
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.add(getJPanel2());
			jPanel1.add(getJPanel3());
		}
		return jPanel1;
	}
	
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			
			jButton1.setText("new Citation");
			jButton1.setMnemonic(KeyEvent.VK_C);
			jButton1.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					currentCitation = new DefaultMutableTreeNode("cit");
					testRoot.add(currentCitation);
					testModel.reload(testRoot);
					testInstance.add(new Citation(new Publication (null, null)));
					
				}
				
			});
		}
		return jButton1;
	}
	
	private JRadioButton getJRadioButton1() {
		if(authorButton == null) {
			authorButton = new JRadioButton();
			authorButton.setText("author");
			authorButton.setMnemonic(KeyEvent.VK_A);
			authorButton.setActionCommand(AUTHOR);
		}
		return authorButton;
	}
	
	private JRadioButton getTitle() {
		if(title == null) {
			title = new JRadioButton();
			title.setMnemonic(KeyEvent.VK_T);
			title.setActionCommand(TITLE);
			title.setText("title");
		}
		return title;
	}
	
	private JRadioButton getJRadioButton2() {
		if(yearButton == null) {
			yearButton = new JRadioButton();
			yearButton.setMnemonic(KeyEvent.VK_Y);
			yearButton.setActionCommand(YEAR);
			yearButton.setText("year");
		}
		return yearButton;
	}
	
	private JPanel getJPanel2() {
		if(jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getJRadioButton2());
			jPanel2.add(getJRadioButton1());
			jPanel2.add(getTitle());
			getButtonGroup1();
		}
		return jPanel2;
	}
	
	private ButtonGroup getButtonGroup1() {
		if(buttonGroup1 == null) {
			buttonGroup1 = new ButtonGroup();
			buttonGroup1.add(authorButton);
			buttonGroup1.add(yearButton);
			buttonGroup1.add(title);
			Enumeration<AbstractButton> enumBt =buttonGroup1.getElements();
			while(enumBt.hasMoreElements()){
				
				enumBt.nextElement().addActionListener(new ActionListener(){
					

					@Override
					public void actionPerformed(ActionEvent e) {
						objectType = buttonGroup1.getSelection().getActionCommand();
						System.out.println (objectType);
					}
				});
				
			}
			
		}
		return buttonGroup1;
	}
	
	private JPanel getJPanel3() {
		if(jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.add(getJButton1());
			jPanel3.add(getJButton2());
			jPanel3.add(getJButton3());
			jPanel3.add(getJButton4());
		}
		return jPanel3;
	}
	
	private JButton getJButton2() {
		if(jButton2 == null) {
			jButton2 = new JButton();
			
			this.jButton2.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					testInstance.clear();
				}
				
			});
			jButton2.setText("clear");
		}
		return jButton2;
	}
	
	private JButton getJButton3() {
		if(jButton3 == null) {
			jButton3 = new JButton();
			jButton3.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser saveFile = new JFileChooser();
					saveFile.setDialogType(JFileChooser.SAVE_DIALOG);
					int mode = saveFile.showSaveDialog(null);
					if (mode == JFileChooser.APPROVE_OPTION){
					  try {
						saveFile.getSelectedFile().createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
						saveTestFile( saveFile.getSelectedFile());
					}
					
				}
				
			});
			jButton3.setText("save");
		}
		return jButton3;
	}

	private String readFile (String path){
		StringBuffer sb = new StringBuffer();
		BufferedReader br;
		try {
		br = new BufferedReader (new FileReader (path));
		
			
			
				while (br.ready()){
					
						sb.append(br.readLine()+System.getProperty("line.separator"));
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return sb.toString();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(FileMenuBar.TEST)){
			this.entityEditEditor.setText(this.readFile((String)evt.getNewValue()));
		}
		
	}
	
	private JButton getJButton4() {
		if(jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText("clear Obj");
			jButton4.setMnemonic(KeyEvent.VK_X);
			jButton4.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
				if (selectedNode!= null){
					selectedNode.removeFromParent();
					testModel.reload();
				}
				}
				
			});
		}
		return jButton4;
	}
}
