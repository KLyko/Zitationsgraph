package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import javax.swing.tree.TreeNode;

import de.uni.leipzig.asv.zitationsgraph.data.Author;
import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.tests.controls.TestEditingCtrl;
import de.uni.leipzig.asv.zitationsgraph.tests.data.Constants;

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
public class TestPanel extends javax.swing.JSplitPane implements PropertyChangeListener{
	private static final String CIT = "citation";
	private static final String TITLE = "title";
	private static final String YEAR = "year";
	private static final String SENTENCE =  "sentence";
	private static final String AUTHOR = "author";
	private JRadioButton sentenceButton;
	private JButton genOpenDialogBt;
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
	private JButton jOpenBt;
	private JButton clearObjBt;
	private JButton saveBt;
	private JButton clearBt;
	private JButton newCitBt;
	
	private JPanel jeditPane;
	private JPanel entityCtrlPane;
	private ButtonGroup entityBtGroup;
	private JPanel entityPane;
	private JRadioButton yearButton;
	private JRadioButton titleButton;
	
	private JRadioButton authorButton;
	private JPanel jConfigPane;
	private JScrollPane jScrollPane1;
	private JTree testInstanceTree;
	private JTextPane entityEditEditor;
	private JPanel testSchemaPan;
	private DefaultMutableTreeNode selectedNode;
	
	private String currentFile;

	protected String objectType;
	private String saveFileOption;
	//private Vector<Citation> testInstance;
	private DefaultMutableTreeNode testRoot;
	private DefaultTreeModel testTreeModel;
	protected DefaultMutableTreeNode currentEntity;
	protected DefaultMutableTreeNode currentAuthors;
	private JTabbedPane tabPane;
	private EvalPanel evalPanel;
	private static int entityId =0;
	
	private GenerateDocDialog genDialog;
	private JFileChooser openChooser;

	private TestEditingCtrl editControl;
	private JFileChooser saveFileChooser;
	private DefaultMutableTreeNode currentSentences;
	
	public TestPanel() {
		super();
		
		this.editControl = new TestEditingCtrl(this);
		genDialog = new GenerateDocDialog();
		this.initOpenFileChooser();
		this.initSaveFileChooser();
		initGUI();
		initListener();
	}
	
	private void initGUI() {
		try {
			{
				this.setOneTouchExpandable(true);
				this.setDividerSize(5);
				tabPane= new JTabbedPane();	
				//BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.X_AXIS);
				//this.setLayout(thisLayout);
				{
					testSchemaPan = new JPanel();
					BorderLayout testSchemaPanLayout = new BorderLayout();
					testSchemaPan.setLayout(testSchemaPanLayout);
					testSchemaPan.add(getOpenGenDialog(), BorderLayout.NORTH);
					testSchemaPan.add(getJScrollTreeTestPane(), BorderLayout.CENTER);
					testSchemaPan.add(getConfigPane(), BorderLayout.SOUTH);

					evalPanel = new EvalPanel();
					tabPane.add("test case building", testSchemaPan);
					tabPane.add("evaluation",evalPanel);
					this.add(tabPane,JSplitPane.LEFT);
					
					this.add(getJSplitPane1(),JSplitPane.RIGHT);
			
				
				    
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JScrollPane getJScrollTreeTestPane() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTestInstanceTree());
		}
		return jScrollPane1;
	}
	
	private JTree getTestInstanceTree() {
		if(testInstanceTree == null) {
			testRoot = new DefaultMutableTreeNode ("root");
			testTreeModel = new DefaultTreeModel(testRoot);
			testInstanceTree = new JTree(testTreeModel);
			testInstanceTree.addTreeSelectionListener(new TreeSelectionListener(){

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
					if (selectedNode.getLevel() ==1){
						currentEntity = selectedNode;
						Enumeration<DefaultMutableTreeNode> childs = currentEntity.children();
						DefaultMutableTreeNode child;
						while (childs.hasMoreElements()){
							child = childs.nextElement();
							if (child.toString().equals(AUTHOR)){
								currentAuthors= child;
								break;
							}
						}
					}
					
				}
				
			});
		}
		return testInstanceTree;
	}
	
	private JPanel getConfigPane() {
		if(jConfigPane == null) {
			jConfigPane = new JPanel();
			BoxLayout jPanel1Layout = new BoxLayout(jConfigPane, javax.swing.BoxLayout.Y_AXIS);
			jConfigPane.setLayout(jPanel1Layout);
			jConfigPane.add(getEntitySelPane());
			jConfigPane.add(getEntityCtrlPane());
		}
		return jConfigPane;
	}
	
	private JPanel getEntitySelPane() {
		if(entityPane == null) {
			entityPane = new JPanel();
			entityPane.add(getJRadioYearBt());
			entityPane.add(getJRadioAutBt());
			entityPane.add(getJRadioTitleBt());
			entityPane.add(getSentenceButton());
			getEntityBtGroup();
		}
		return entityPane;
	}
	
	
	private JRadioButton getJRadioAutBt() {
		if(authorButton == null) {
			authorButton = new JRadioButton();
			authorButton.setText("author");
			authorButton.setMnemonic(KeyEvent.VK_A);
			authorButton.setActionCommand(AUTHOR);
		}
		return authorButton;
	}
	
	private JRadioButton getJRadioTitleBt() {
		if(titleButton == null) {
			titleButton = new JRadioButton();
			titleButton.setMnemonic(KeyEvent.VK_T);
			titleButton.setActionCommand(TITLE);
			titleButton.setText("title");
		}
		return titleButton;
	}
	
	private JRadioButton getJRadioYearBt() {
		if(yearButton == null) {
			yearButton = new JRadioButton();
			yearButton.setMnemonic(KeyEvent.VK_Y);
			yearButton.setActionCommand(YEAR);
			yearButton.setText("year");
		}
		return yearButton;
	}
	
	private JRadioButton getSentenceButton() {
		if(sentenceButton == null) {
			sentenceButton = new JRadioButton();
			sentenceButton.setText("sentence");
			sentenceButton.setMnemonic(KeyEvent.VK_S);
			sentenceButton.setActionCommand(SENTENCE);
		}
		return sentenceButton;
	}
	
	private ButtonGroup getEntityBtGroup() {
		if(entityBtGroup == null) {
			entityBtGroup = new ButtonGroup();
			entityBtGroup.add(authorButton);
			entityBtGroup.add(yearButton);
			entityBtGroup.add(titleButton);
			entityBtGroup.add(sentenceButton);
			Enumeration<AbstractButton> enumBt =entityBtGroup.getElements();
			while(enumBt.hasMoreElements()){
				enumBt.nextElement().addActionListener(new ActionListener(){
					

					@Override
					public void actionPerformed(ActionEvent e) {
						objectType = entityBtGroup.getSelection().getActionCommand();
						
					}
				});	
			}
		}
		return entityBtGroup;
	}
	
	private JPanel getEntityCtrlPane() {
		if(entityCtrlPane == null) {
			entityCtrlPane = new JPanel();
			GridBagLayout entityCtrlPaneLayout = new GridBagLayout();
			entityCtrlPaneLayout.rowWeights = new double[] {0.1, 0.1};
			entityCtrlPaneLayout.rowHeights = new int[] {7, 7};
			entityCtrlPaneLayout.columnWeights = new double[] {0.1, 0.1, 0.1};
			entityCtrlPaneLayout.columnWidths = new int[] {7, 7, 7};
			entityCtrlPane.setLayout(entityCtrlPaneLayout);
			entityCtrlPane.add(newCitBut(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
			entityCtrlPane.add(getClearBt(), new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
			entityCtrlPane.add(getSaveBt(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
			entityCtrlPane.add(getClearObjBt(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
			entityCtrlPane.add(getJOpenBt(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
		return entityCtrlPane;
	}
	
	private JButton getClearBt() {
		if(clearBt == null) {
			clearBt = new JButton();
			
			this.clearBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					entityId =0;
					testRoot.removeAllChildren();
					testTreeModel.reload();
				}
				
			});
			clearBt.setText("clear");
		}
		return clearBt;
	}
	
	

	private JButton getClearObjBt() {
		if(clearObjBt == null) {
			clearObjBt = new JButton();
			clearObjBt.setText("clear Obj");
			clearObjBt.setMnemonic(KeyEvent.VK_X);
			clearObjBt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					if (selectedNode!= null){
						selectedNode.removeFromParent();
						if (selectedNode.getLevel()==1){
							entityId--;
							currentEntity = null;
						}	
						testTreeModel.reload();
					}
				}
				
			});
		}
		return clearObjBt;
	}
	
	
	
	
	
	
	
	
	
	private JButton getJOpenBt() {
		if(jOpenBt == null) {
			jOpenBt = new JButton();
			jOpenBt.setText("open");
			jOpenBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					int mode = openChooser.showOpenDialog(null);
					if (mode == JFileChooser.APPROVE_OPTION){
						File f = openChooser.getSelectedFile();
						currentFile = f.getAbsolutePath();
						entityEditEditor.setText(readFile(f));
						
					}
				}
				
			});
		}
		return jOpenBt;
	}
	
	
	
	
	
	private JPanel getJSplitPane1() {
		if(jeditPane == null) {
			jeditPane = new JPanel();
			BorderLayout jeditPaneLayout = new BorderLayout();
			jeditPane.setLayout(jeditPaneLayout);
			{
				entityEditEditor = new JTextPane();
				entityEditEditor.setContentType("text/plain;charset=UTF-8");
				
				entityEditEditor.setEditable(false);
				entityEditEditor.addKeyListener(this.editControl);
				JScrollPane editPane = new JScrollPane();
				editPane.setViewportView(entityEditEditor);
				jeditPane.add(editPane, BorderLayout.CENTER);
				entityEditEditor.setText("");
				entityEditEditor.addMouseListener(this.editControl);
			}
			
		}
		
		return jeditPane;
	}
	
	private JButton getSaveBt() {
		if(saveBt == null) {
			saveBt = new JButton();
			saveBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					
					saveFileChooser.setSelectedFile(new File (currentFile +".test"));
					int mode = saveFileChooser.showSaveDialog(null);
					if (mode == JFileChooser.APPROVE_OPTION){
					  try {
						saveFileChooser.getSelectedFile().createNewFile();
					} catch (IOException e1) {
						
						e1.printStackTrace();
					}
						if (saveFileOption.equals(Constants.REF_PART))
							saveReferenceFile( saveFileChooser.getSelectedFile());
						else if (saveFileOption.equals(Constants.HEAD_PART))
							saveHeadFile(saveFileChooser.getSelectedFile());
						else if (saveFileOption.equals(Constants.BODY_PART))
							savePhraseFile(saveFileChooser.getSelectedFile());
					}	
				}
			});
			saveBt.setText("save");
		}
		return saveBt;
	}
	
	
	
	
	
	

	public void markupEntities(){
		if (entityEditEditor.getSelectedText()!= null){
				String object =entityEditEditor.getSelectedText();
				
				String [] temp = object.split(System.getProperty("line.separator"));
			
				object = object.replaceAll("\\s+",
						" ");
				
				if (objectType!=null){
					if (objectType.equals(TITLE)){
						DefaultMutableTreeNode title = new DefaultMutableTreeNode(TITLE);
						currentEntity.add(title);
						DefaultMutableTreeNode titleInstance = new DefaultMutableTreeNode(object);
						title.add(titleInstance);
						
					}else if (objectType.equals(AUTHOR)){
						if (!currentEntity.isNodeChild(currentAuthors)){
							currentAuthors = new DefaultMutableTreeNode(AUTHOR);
							currentEntity.add(currentAuthors);
						}
						DefaultMutableTreeNode authorInstance = new DefaultMutableTreeNode(object);
						currentAuthors.add(authorInstance);
						
					}else if (objectType.equals(YEAR)){
						DefaultMutableTreeNode year = new DefaultMutableTreeNode(YEAR);
						DefaultMutableTreeNode yearInstance = new DefaultMutableTreeNode(object);
						year.add(yearInstance);
						currentEntity.add(year);
						
					}else if (objectType.equals(SENTENCE)){
						if (!currentEntity.isNodeChild(currentSentences)){
							currentSentences = new DefaultMutableTreeNode(SENTENCE);
							currentEntity.add(currentSentences);
						}
						DefaultMutableTreeNode sentenceInstance = new DefaultMutableTreeNode(object);
						currentSentences.add(sentenceInstance);
					}
					testTreeModel.reload(currentEntity);
				}
		}
		
	}
	
	
	
	private JButton newCitBut() {
		if(newCitBt == null) {
			newCitBt = new JButton();
			
			newCitBt.setText("new Entity");
			newCitBt.setMnemonic(KeyEvent.VK_C);
			newCitBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					currentEntity = new DefaultMutableTreeNode("entity");
					testRoot.add(currentEntity);
					testTreeModel.reload(testRoot);
					
					
				}
				
			});
		}
		return newCitBt;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(EntityTree.SELECT_DOC)){
			this.entityEditEditor.setText((String)evt.getNewValue());
		}
		
	}
	
	
	private void initOpenFileChooser (){
		this.openChooser = new JFileChooser();
		this.openChooser.setDialogType(JFileChooser.OPEN_DIALOG);
	
	}
	
	private String readFile (File f){
		StringBuffer sb = new StringBuffer();
		BufferedReader br;
		try {
		br = new BufferedReader (new FileReader (f));
				while (br.ready()){
					sb.append(br.readLine()+System.getProperty("line.separator"));
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			} 
			return sb.toString();
	}
	
	private JFileChooser initSaveFileChooser(){
		if (this.saveFileChooser ==null){
			 saveFileChooser = new JFileChooser();
			 saveFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			 saveFileChooser.setAccessory(this.initSaveOption());	
		}
		return saveFileChooser;
	}
	
	private JPanel initSaveOption (){
		JPanel optionPan = new JPanel();
		JRadioButton refSaving = new JRadioButton ();
		refSaving.setActionCommand(Constants.REF_PART);
		refSaving.setText("references");
		refSaving.setToolTipText("save citation vector");
		
		JRadioButton headSaving = new JRadioButton();
		headSaving.setActionCommand(Constants.HEAD_PART);
		headSaving.setText("head");
		headSaving.setToolTipText("save header entities");
		
		JRadioButton bodySaving = new JRadioButton();
		bodySaving.setActionCommand(Constants.BODY_PART);
		bodySaving.setText("body");
		bodySaving.setToolTipText("phrase saving");
		
		ButtonGroup saveGroup = new ButtonGroup();
		saveGroup.add(refSaving);
		saveGroup.add(headSaving);
		saveGroup.add(bodySaving);
		
		optionPan.add(headSaving);
		optionPan.add(refSaving);
		optionPan.add(bodySaving);
		Enumeration<AbstractButton> buttons = saveGroup.getElements();
		while (buttons.hasMoreElements()){
			buttons.nextElement().addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					saveFileOption = e.getActionCommand();
					
				}
				
			});
		}
		headSaving.doClick();
		return optionPan;
	}
	
	
	
	
	private void saveReferenceFile (File file){
		Publication pub ;
		String title = null; 
		Vector<Author> authors = null;
		String year = null;
		Vector <Citation> citVector = new Vector<Citation>();
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
			citVector.add(new Citation(pub));
		}
		try {
		FileOutputStream fos = new FileOutputStream(file);
		
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(citVector);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void saveHeadFile(File file){
		Publication pub = null;
		String title = null; 
		Vector<Author> authors = null;
		String year = null;
			
		DefaultMutableTreeNode cit = (DefaultMutableTreeNode) testRoot.getFirstChild();
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
		try {
			FileOutputStream fos = new FileOutputStream(file);
			
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(pub);
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public void savePhraseFile (File file){
		HashSet<String> sentenceSet = new HashSet<String>();
		Enumeration<DefaultMutableTreeNode> enumCit = testRoot.children();
		while( enumCit.hasMoreElements()){
			DefaultMutableTreeNode obj = enumCit.nextElement();
			Enumeration<DefaultMutableTreeNode> sentenceEnum =  obj.children();
			
			while(sentenceEnum.hasMoreElements()){
				DefaultMutableTreeNode sentNode = sentenceEnum.nextElement();
				if (sentNode.toString().equals(SENTENCE));
				Enumeration <DefaultMutableTreeNode> sentenceValues = sentNode.children();
				while (sentenceValues.hasMoreElements()){
					sentenceSet.add(sentenceValues.nextElement().toString());
				}
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(sentenceSet);
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	public void shortCutPress(int key){
		switch (key){
		case KeyEvent.VK_A:
			authorButton.doClick();
			break;
		case KeyEvent.VK_T:
			titleButton.doClick();
			break;
		case KeyEvent.VK_Y:
			yearButton.doClick();
			break;
		case KeyEvent.VK_S:
			sentenceButton.doClick();
			break;
		case KeyEvent.VK_C:
			newCitBt.doClick();
			
		}
	}
	
	private void initListener(){
		this.evalPanel.addPropertyChangeListener(this);
	}
	
	
	
	
	
	
	
	
	private JButton getOpenGenDialog() {
		if(genOpenDialogBt == null) {
			genOpenDialogBt = new JButton();
			genOpenDialogBt.setText("generate split docs");
			genOpenDialogBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					genDialog.setVisible(true);
				}
				
			});
		}
		return genOpenDialogBt;
	}
	
	public void release (){
		this.genDialog.dispose();
	}
	
	
}
	
