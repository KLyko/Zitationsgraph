package de.uni.leipzig.asv.zitationsgraph.tests;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import prefuse.data.Table;
import prefuse.util.ui.JPrefuseTable;


import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.data.Document;
import de.uni.leipzig.asv.zitationsgraph.data.Publication;
import de.uni.leipzig.asv.zitationsgraph.extraction.BodyExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.HeadExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.ReferenceExtraction;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.FolderReader;
import de.uni.leipzig.asv.zitationsgraph.tests.data.Constants;
import de.uni.leipzig.asv.zitationsgraph.tests.data.PubData;

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
public class SourcePanel extends javax.swing.JPanel implements PropertyChangeListener {
	private static final Logger log = Logger.getLogger(SourcePanel.class.getName());
	public static final String NEW_REF_VECTOR ="newReferences";
	public static final String NEW_DOC = "newDocument";
	public static final String NEW_REF_PART ="newReferencePart";
	public static final String NEW_HEAD_PART = "newHeadPart";
	public static final String RESET = "Reset";
	public static final String NEW_HEAD_ENTITIES = "newHeadEntities";
	
	public static final String COLOR = "color";
	
	private static final String ALL_STEPS = "allSteps";
	private static final String SPLIT_STEP = "splitStep";
	
	private static final String HEAD_STEP = "headSteps";
	private static final String REF_STEPS = "referenceSteps";
	private static final String PHR_STEPS = "phraseSteps";
	
	private JButton resetRes;
	private JButton runBt;
	private JPanel jPanel1;
	
	private JRadioButton allRunBt;
	private ButtonGroup runOptions;
	private JPanel runOptionPan;
	private String runOption;
	private JScrollPane jScrollPane1;
	private PubData data;
	private JSourceTable sourceTable;
	private Table sourceDataTable;
	
	

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new SourcePanel());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public SourcePanel() {
		super();
		initGUI();
	}

	public SourcePanel(PubData data) {
		super();
		
		this.data = data ;
		initGUI();
	}

	private void initGUI() {
		try {
			BoxLayout layout = new BoxLayout(this, javax.swing.BoxLayout.Y_AXIS);
			this.setLayout(layout);
			this.add(getSourceFolderList());
			this.add(getJPanel1());
			{
				this.add(getJScrollPane1());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JPrefuseTable getSourceFolderList() {
		
		if (sourceTable ==null){
			sourceDataTable = new Table ();
			
			sourceDataTable.addColumn(Constants.SOURCE_PATH, String.class);
			sourceDataTable.addColumn(JSourceTable.REMOVE_COL, String.class);
			//sourceDataTable.addColumn(COLOR, int.class);
			sourceTable = new JSourceTable(sourceDataTable,true);
		}
		return sourceTable;
	}
	
	
	
	private JRadioButton getAllRunBt() {
		if(allRunBt == null) {
			allRunBt = new JRadioButton();
			allRunBt.setText("all Parts");
			allRunBt.setActionCommand(PubData.ALL_STEPS);
			
		}
		return allRunBt;
	}
	
	
	
	private JButton getRunBt() {
		if(runBt == null) {
			runBt = new JButton();
			runBt.setText("Run");
			runBt.addActionListener(new ActionListener(){
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] folders = new String[sourceDataTable.getTupleCount()-1];
					for (int i = 1;i<sourceDataTable.getTupleCount();i++){
						folders[i-1]=sourceDataTable.getString(i,Constants.SOURCE_PATH );			
					}
					data.initProcess(folders);
				}
			});
		}
		return runBt;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FileMenuBar.PROP_SAVE_FOLDER)){
			String folderPath = (String) evt.getNewValue();
			/*try {
				saveParts(folderPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	
	/*
	private void saveParts(String folderPath) throws IOException {
		FileWriter headWriter;
		FileWriter refWriter;
		for (String key : ReferencePan.getRefPartMap().keySet()){
			if (ReferencePan.getHeadPartMap().get(key)!=null) {
				File headPart = new File (folderPath+"/"+key+".head");
				headWriter =new FileWriter(headPart);
				headWriter.append(ReferencePan.getHeadPartMap().get(key));
				headWriter.flush();
				headWriter.close();
			}
			if (ReferencePan.getRefPartMap().get(key)!=null){
				File refPart= new File (folderPath+"/"+key+".ref");
				refWriter =new FileWriter(refPart);
				refWriter.append(ReferencePan.getRefPartMap().get(key));
				refWriter.flush();
				refWriter.close();
			}
		}
		
	}*/

	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			
			jScrollPane1.setViewportView(getSourceFolderList());
		}
		return jScrollPane1;
	}

	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getRunBt());
			jPanel1.add(getResetRes());
			
		}
		return jPanel1;
	}
	
	private JButton getResetRes() {
		if(resetRes == null) {
			resetRes = new JButton();
			resetRes.setText("reset Results");
			resetRes.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					firePropertyChange(RESET,false,true);
					
				}
				
			});
		}
		return resetRes;
	}
	
}
	