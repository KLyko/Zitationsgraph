package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BoxLayout;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.uni.leipzig.asv.zitationsgraph.tests.data.Constants;
import de.uni.leipzig.asv.zitationsgraph.tests.data.EvaluationData;
import de.uni.leipzig.asv.zitationsgraph.tests.data.PubData;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.ui.JPrefuseTable;


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
public class EvalPanel extends javax.swing.JPanel implements PropertyChangeListener {
	
	private static final Logger log = Logger.getLogger(EvalPanel.class.getName());
	private JPanel jPanel1;
	private Table instDataTable;
	private JSourceTable instanceTable;
	private JRadioButton phrExtBt;
	private JRadioButton heaExtBt;
	private JRadioButton refExt;
	private ButtonGroup runOptions;
	private String runOption;
	private JTree evaluationTree;
	private JPanel jPanel3;
	private JPanel jPanel2;
	private JPanel runOptionPan;
	private JButton runBt;
	private EvaluationData evalData;
	private JSplitPane splitSourceEvalRes;
	
	private EntityTree testMethodTree;
	private EntityTree testSerializeTree;
	
	private DefaultMutableTreeNode rootEval;
	private DefaultTreeModel evalTreeModel;
	
	
	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new EvalPanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public EvalPanel() {
		super();
		evalData = new EvaluationData();
		evalData.addPropertyChangeListener(this);
		initGUI();
		
	}
	
	private void initGUI() {
		try {
			BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.Y_AXIS);
			this.setLayout(thisLayout);
			{
				splitSourceEvalRes = new JSplitPane();
				splitSourceEvalRes.setOneTouchExpandable(true);
				splitSourceEvalRes.setOrientation(JSplitPane.VERTICAL_SPLIT);
				this.add(splitSourceEvalRes);
				jPanel1 = new JPanel();
				BoxLayout jPanel1Layout = new BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS);
				this.splitSourceEvalRes.add(jPanel1, JSplitPane.TOP);
				jPanel1.setLayout(jPanel1Layout);

				JScrollPane testScrollPane = new JScrollPane();
				testScrollPane.setViewportView(getTestInstanceTable());
				jPanel1.add(testScrollPane);
				runOptionPan = new JPanel();
				GridBagLayout runOptionPanLayout = new GridBagLayout();
				runOptionPan.setLayout(runOptionPanLayout);
				runOptionPanLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
				runOptionPanLayout.rowHeights = new int[] {7, 7, 7};
				runOptionPanLayout.columnWeights = new double[] {0.1, 0.1};
				runOptionPanLayout.columnWidths = new int[] {7, 7};
				jPanel1.add(runOptionPan);
				runOptionPan.add(getRefExt(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getPhrExtBt(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getHeaExtBt(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getRunBt(), new GridBagConstraints(0, 2,1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				getRunOptions();
				
				{
					JPanel panel2 = new JPanel ();
					BorderLayout panel2Layout = new BorderLayout();
					splitSourceEvalRes.add(panel2,JSplitPane.BOTTOM);
					panel2.setLayout(panel2Layout);
					panel2.add(getJPanel2(), BorderLayout.CENTER);
					panel2.add(getJPanel3(), BorderLayout.SOUTH);
					//this.splitSourceEvalRes.add(this.getJevalTree(),JSplitPane.BOTTOM);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private JSourceTable getTestInstanceTable(){
		if (instanceTable == null){
			instDataTable = new Table();
			instDataTable.addColumn(Constants.TEST_SOURCE, String.class);
			instDataTable.addColumn(Constants.SOURCE_PATH, String.class);
			instDataTable.addColumn(Constants.REF_SOURCE, String.class);
			instDataTable.addColumn(JSourceTable.REMOVE_COL, String.class);
			instanceTable = new JSourceTable(instDataTable, true);
			instanceTable.getColumnModel().getColumn(0).setHeaderValue("tested source");
			instanceTable.getColumnModel().getColumn(1).setHeaderValue("serialized source");
			instanceTable.getColumnModel().getColumn(2).setHeaderValue("reference source");
			instanceTable.setVisibleColumn(false, 2);
		
		}
		return instanceTable;
	}
	
	
	
	private JRadioButton getRefExt() {
		if(refExt == null) {
			refExt = new JRadioButton();
			refExt.setText("reference extraction");
			refExt.setActionCommand(EvaluationData.REF_STEPS);
		}
		return refExt;
	}
	
	private JRadioButton getHeaExtBt() {
		if(heaExtBt == null) {
			heaExtBt = new JRadioButton();
			heaExtBt.setText("head Extraction");
			heaExtBt.setActionCommand(EvaluationData.HEAD_STEP);
		}
		return heaExtBt;
	}
	
	private JRadioButton getPhrExtBt() {
		if(phrExtBt == null) {
			phrExtBt = new JRadioButton();
			phrExtBt.setText("phrase Extraction");
			phrExtBt.setActionCommand(EvaluationData.PHR_STEPS);
		}
		return phrExtBt;
	}
	
	private ButtonGroup getRunOptions() {
		if(runOptions == null) {
			runOptions = new ButtonGroup();
			runOptions.add(heaExtBt);
			runOptions.add(phrExtBt);
			runOptions.add(refExt);
			Enumeration<AbstractButton> enumBt =runOptions.getElements();
			while(enumBt.hasMoreElements()){
				
				enumBt.nextElement().addActionListener(new ActionListener(){
					

					@Override
					public void actionPerformed(ActionEvent e) {
						runOption = runOptions.getSelection().getActionCommand();
						evalData.setType(runOption);
						if (runOption.equals(EvaluationData.REF_STEPS)){
							testMethodTree.setType(EntityTree.REFERENCE_TREE);
							testSerializeTree.setType(EntityTree.REFERENCE_TREE);
							instanceTable.setVisibleColumn(false, 2);
						}else if (runOption.equals(EvaluationData.HEAD_STEP)){
							testMethodTree.setType(EntityTree.HEAD_TREE);
							testSerializeTree.setType(EntityTree.HEAD_TREE);
							instanceTable.setVisibleColumn(false, 2);
						}else if (runOption.equals(EvaluationData.PHR_STEPS)){
							testMethodTree.setType(EntityTree.BODY_TREE);
							testSerializeTree.setType(EntityTree.BODY_TREE);
							instanceTable.setVisibleColumn(true, 2);
						}
						
					}
				});
				
			}
		}
		return runOptions;
	}
	
	private EntityTree getJevalTree() {
		if(testMethodTree == null) {
			testMethodTree = new EntityTree();
			testMethodTree.addDocSelectionListener();
			testMethodTree.addPropertyChangeListener(this);
		}
		return testMethodTree;
	}
	
	private EntityTree getTestInstTree(){
		if (testSerializeTree == null){
			testSerializeTree = new EntityTree();
		}
		return testSerializeTree;
	}
	
	private JButton getRunBt (){
		if (this.runBt == null){
			
			runBt = new JButton("run test");
			runBt.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					if (validateTable()){
						try {
							evalData.reset();
							testSerializeTree.resetTree();
							testMethodTree.resetTree();
							resetEvalTree();
							
							List<String> testingFiles = gatherSources(Constants.TEST_SOURCE);
							List<String> testSerFiles = gatherSources(Constants.SOURCE_PATH);
							if (runOption.equals(EvaluationData.PHR_STEPS)){
								evalData.setRefSource(gatherSources(Constants.REF_SOURCE));
							}
							evalData.generateInstanceCaseMap(testingFiles, testSerFiles);
							evalData.initTestInstances(testSerFiles);
							evalData.initSubProcess( testingFiles);
							evalData.compareOverall();
						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}else {
						JOptionPane.showConfirmDialog(null, "You must specify" +
								" for every test case file a serialized test instance",
								"File specification",
								JOptionPane.WARNING_MESSAGE,JOptionPane.OK_OPTION);
					}
				}
				
			});
			
			
			
		}
		return runBt;
	}
	
	void readTestInstanceFile(List <String> sources ){
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof EntityTree){
			if (evt.getPropertyName().equals(EntityTree.SELECT_DOC)){
				this.firePropertyChange(EntityTree.SELECT_DOC, "", evalData.getPartMap().get(evt.getNewValue()));
			}
		}else if (evt.getSource() instanceof EvaluationData){
			if (evt.getPropertyName().equals(EvaluationData.NEW_DOC)){
				testMethodTree.setCurrentDoc(evt.getNewValue().toString());
			}else if(evt.getPropertyName().equals(EvaluationData.NEW_HEAD_ENTITIES)){
				testMethodTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_HEAD_TEST_ENTITIES)){
				testSerializeTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_REF_VECTOR)){
				testMethodTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_REF_TEST_VECTOR)){
				testSerializeTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_SENTENCES)){
				testMethodTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_SENTENCES_TEST)){
				testSerializeTree.updateTree(evt.getNewValue());
			}else if (evt.getPropertyName().equals(EvaluationData.NEW_TEST_DOC)){
				testSerializeTree.setCurrentDoc(evt.getNewValue().toString());
			}else if (evt.getPropertyName().endsWith(EvaluationData.EVAL)){
				String entityEval = evt.getPropertyName().split("#")[0];
				this.updateEvalTree(entityEval,(Map<String,Float>)evt.getNewValue());
			}
		}
	}
	
	private List<String> gatherSources (String field){
		List <String>sources = new ArrayList<String>();
		Iterator <Tuple> iter = instDataTable.tuples();
		int row = 0;
		Tuple t ;
		while (iter.hasNext()){
			t = iter.next();
			if (row >0){
				sources.add(t.getString(field));
			}
			row++;
		}
		return sources;
	}
	
	private boolean validateTable (){
		Iterator <Tuple> iter = this.instDataTable.tuples();
		boolean isValid = true;
		int row=0;
		Tuple t;
		
		while(iter.hasNext()){
			t = iter.next();
			if (row>0){
				String source  = t.getString(Constants.SOURCE_PATH);
				String caseSource = t.getString(Constants.TEST_SOURCE);
				
				if (source==null || caseSource == null){
					isValid = false;
					break;
				}
				if (runOption.equals(EvaluationData.PHR_STEPS)){
					if ( t.getString(Constants.REF_SOURCE) ==null){
						isValid = false;
						break;
					}
				}
			}
			row++;
		}
		
		if (row ==1)
			isValid = false;
		return isValid;
	}
	
	private JPanel getJPanel2() {
		if(jPanel2 == null) {
			jPanel2 = new JPanel();
			BoxLayout jPanel2Layout = new BoxLayout(jPanel2, javax.swing.BoxLayout.X_AXIS);
			jPanel2.setLayout(jPanel2Layout);
			{
				JScrollPane resTestPane = new JScrollPane ();
				jPanel2.add(resTestPane);
				resTestPane.setViewportView(getJevalTree());
			}
			{
				JScrollPane testInstPane = new JScrollPane();
				jPanel2.add(testInstPane);
				testInstPane.setViewportView(getTestInstTree());
			}
		}
		return jPanel2;
	}
	
	private JPanel getJPanel3() {
		if(jPanel3 == null) {
			jPanel3 = new JPanel();
			BorderLayout jPanel3Layout = new BorderLayout();
			jPanel3.setLayout(jPanel3Layout);
			jPanel3.add(getEvaluationTree(), BorderLayout.CENTER);
		}
		return jPanel3;
	}
	
	private JTree getEvaluationTree() {
		if(evaluationTree == null) {
			this.rootEval = new DefaultMutableTreeNode("evaluation results");
			this.evalTreeModel = new DefaultTreeModel(rootEval);
			evaluationTree = new JTree(evalTreeModel);
		
		}
		return evaluationTree;
	}
	
	private void resetEvalTree(){
		rootEval.removeAllChildren();
		evalTreeModel.reload();
	}
	
	private void updateEvalTree(String entity,Map<String,Float> evalMap){
		DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode (entity);
		rootEval.add(entityNode);
		DefaultMutableTreeNode precisionNode = new DefaultMutableTreeNode("precision:"+
				evalMap.get(EvaluationData.PRECISION));
		DefaultMutableTreeNode recallNode =new DefaultMutableTreeNode("recall:"+
				evalMap.get(EvaluationData.RECALL));
		entityNode.add(precisionNode);
		entityNode.add(recallNode);
		evalTreeModel.reload(rootEval);
	}

}
