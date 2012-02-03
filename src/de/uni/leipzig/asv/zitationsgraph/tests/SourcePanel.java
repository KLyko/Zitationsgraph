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
	public static final String SOURCE_PATH = "sourcePath";
	public static final String COLOR = "color";
	
	private static final String ALL_STEPS = "allSteps";
	private static final String SPLIT_STEP = "splitStep";
	private JButton resetRes;
	private JPanel jPanel1;
	private static final String HEAD_STEP = "headSteps";
	private static final String REF_STEPS = "referenceSteps";
	private static final String PHR_STEPS = "phraseSteps";
	
	
	private JList<String> sourceFolderList;
	private JButton runBt;
	private JRadioButton phrExtBt;
	private JRadioButton heaExtBt;
	private JRadioButton refExt;
	private JRadioButton onlyPrebt;
	private JRadioButton allRunBt;
	private ButtonGroup runOptions;
	private JPanel runOptionPan;
	private String runOption;
	private JScrollPane jScrollPane1;
	private DefaultListModel<String> sourceFolderListModel;
	private PubData data;
	private JPrefuseTable sourceTable;
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
				runOptionPan = new JPanel();
				GridBagLayout runOptionPanLayout = new GridBagLayout();
				runOptionPan.setLayout(runOptionPanLayout);
				runOptionPanLayout.rowWeights = new double[] {0.1, 0.1};
				runOptionPanLayout.rowHeights = new int[] {7, 7};
				runOptionPanLayout.columnWeights = new double[] {0.1, 0.1, 0.1};
				runOptionPanLayout.columnWidths = new int[] {7, 7, 7};
				this.add(getJScrollPane1());
				JButton removeSource = new JButton("remove ");
				removeSource.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						if (sourceTable.getSelectedRow()!=-1){
							int ind = sourceTable.getSelectedRow();
							sourceTable.clearSelection();
							sourceDataTable.removeRow(ind);
							
						}
					}
					
				});

				this.add(removeSource);
				this.add(runOptionPan);
				runOptionPan.add(getAllRunBt(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getRefExt(), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getPhrExtBt(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getOnlyPrebt(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				runOptionPan.add(getHeaExtBt(), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				getRunOptions();
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JPrefuseTable getSourceFolderList() {
		
		if (sourceTable ==null){
			sourceDataTable = new Table ();
			
			sourceDataTable.addColumn(SOURCE_PATH, String.class);
			//sourceDataTable.addColumn(COLOR, int.class);
			
			sourceTable = new JPrefuseTable(sourceDataTable){
				public boolean isCellEditable(int x, int y){
					if (y ==1)
						return true;
					else 
						return false;
				}
				/*
				@Override
				public TableCellEditor getCellEditor(int row, int col){
					if (col ==0){
						return super.getCellEditor();
					}else{
						return colEditor;
					}
				}
				
				public TableCellRenderer getCellRenderer (int row, int col){
					if (col ==0)
						return super.getCellRenderer(row, col);
					else 
						return colRenderer;
				}*/
			};
			
			
			
			sourceTable.setCellSelectionEnabled(true);
			
			
		}
		return sourceTable;
	}
	
	private ButtonGroup getRunOptions() {
		if(runOptions == null) {
			runOptions = new ButtonGroup();
			runOptions.add(allRunBt);
			runOptions.add(heaExtBt);
			runOptions.add(onlyPrebt);
			runOptions.add(phrExtBt);
			runOptions.add(refExt);
			Enumeration<AbstractButton> enumBt =runOptions.getElements();
			while(enumBt.hasMoreElements()){
				
				enumBt.nextElement().addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						runOption = runOptions.getSelection().getActionCommand();
						System.out.println (runOption);
					}
				});
				
			}
		}
		return runOptions;
	}
	
	private JRadioButton getAllRunBt() {
		if(allRunBt == null) {
			allRunBt = new JRadioButton();
			allRunBt.setText("all Parts");
			allRunBt.setActionCommand(PubData.ALL_STEPS);
			
		}
		return allRunBt;
	}
	
	private JRadioButton getOnlyPrebt() {
		if(onlyPrebt == null) {
			onlyPrebt = new JRadioButton();
			onlyPrebt.setText("only splitting");
			this.onlyPrebt.setActionCommand(PubData.SPLIT_STEP);
		}
		return onlyPrebt;
	}
	
	private JRadioButton getRefExt() {
		if(refExt == null) {
			refExt = new JRadioButton();
			refExt.setText("reference extraction");
			refExt.setActionCommand(PubData.REF_STEPS);
		}
		return refExt;
	}
	
	private JRadioButton getHeaExtBt() {
		if(heaExtBt == null) {
			heaExtBt = new JRadioButton();
			heaExtBt.setText("head Extraction");
			heaExtBt.setActionCommand(PubData.HEAD_STEP);
		}
		return heaExtBt;
	}
	
	private JRadioButton getPhrExtBt() {
		if(phrExtBt == null) {
			phrExtBt = new JRadioButton();
			phrExtBt.setText("phrase Extraction");
			phrExtBt.setActionCommand(PubData.PHR_STEPS);
		}
		return phrExtBt;
	}
	
	private JButton getRunBt() {
		if(runBt == null) {
			runBt = new JButton();
			runBt.setText("Run");
			runBt.addActionListener(new ActionListener(){
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] folders = new String[sourceDataTable.getTupleCount()];
					for (int i = 0;i<sourceDataTable.getTupleCount();i++){
						folders[i]=sourceDataTable.getString(i,SOURCE_PATH );
								
					}
					
					
					if (runOption.equals(ALL_STEPS)){
						data.initProcess(folders);
					}else {
						try {
							
							data.initSubProcess(folders,runOption);
						} catch (IOException e1) {
							JOptionPane.showConfirmDialog(null, e1.getMessage(),
									"ERROR", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return runBt;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FileMenuBar.CHANGE_ADD_FOLDER)){
			String [] pathes =(String[])evt.getNewValue();
			for (String p: pathes){
				int row = this.sourceDataTable.addRow();
				this.sourceDataTable.set(row,SOURCE_PATH,(String)p);
				//this.sourceDataTable.set(row, COLOR, 0);
			}
			sourceTable.repaint();
		}
		else if (evt.getPropertyName().equals(FileMenuBar.PROP_SAVE_FOLDER)){
			String folderPath = (String) evt.getNewValue();
			try {
				saveParts(folderPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
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
		
	}

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
	/*
	public void release (){
		this.colEditor.release();
		this.colEditor = null;
		this.colRenderer = null;
	}
	
	 * idea for the nodeColoring by sources but I would mix data and visualization and this bad style
	private class ColorEditor  extends AbstractCellEditor implements TableCellEditor,ActionListener{

		JDialog dialog;
		JColorChooser colorPicker;
		Color currentColor;
		JButton button;
		
		ColorEditor (){
			button = new JButton ();
			button.setActionCommand("edit");
			button.addActionListener(this);
			colorPicker = new JColorChooser();
			dialog = JColorChooser.createDialog(null, "Choose a color for the nodes", true, colorPicker, this, null);
			
		}
		@Override
		public Object getCellEditorValue() {
			// TODO Auto-generated method stub
			return currentColor.getRGB();
		}
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			
			return button;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("edit")){
				dialog.setVisible(true);
				this.fireEditingStopped();
				
			}else {
				currentColor = colorPicker.getColor();
			}
			
		}
		 void release(){
			dialog.dispose();
		}
	}
	
	private class ColorRenderer extends JPanel implements TableCellRenderer{

		@Override
		public Component  getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			//this.setBorderPainted(false);
			if ((Integer)value !=0){
			Color color = new Color ((Integer)value);
			//Icon i = new Icon ()
			this.setBackground(color);
			}
			return this;
		}
	}
	*/
}
	