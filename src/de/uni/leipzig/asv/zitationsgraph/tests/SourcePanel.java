package de.uni.leipzig.asv.zitationsgraph.tests;
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
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;


import de.uni.leipzig.asv.zitationsgraph.data.Citation;
import de.uni.leipzig.asv.zitationsgraph.extraction.BodyExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.HeadExtraction;
import de.uni.leipzig.asv.zitationsgraph.extraction.ReferenceExtraction;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.FolderReader;

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
	
	public static final String NEW_REF_VECTOR ="newReferences";
	public static final String NEW_DOC = "newDocument";
	public static final String NEW_REF_PART ="newReferencePart";
	public static final String NEW_HEAD_PART = "newHeadPart";
	public static final String RESET = "Reset";
	public static final String NEW_HEAD_ENTITIES = "newHeadEntities";
	
	
	private static final String ALL_STEPS = "allSteps";
	private static final String SPLIT_STEP = "splitStep";
	private JButton resetRes;
	private JPanel jPanel1;
	private static final String HEAD_STEP = "headSteps";
	private static final String REF_STEPS = "referenceSteps";
	private static final String PHR_STEPS = "phraseSteps";
	
	
	private JList<String> sourceFolderList;
	private JButton jButton1;
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
	private ReferenceExtraction refExtractor;
	private HeadExtraction headExtractor;
	private FolderReader folExtractor;
	private BodyExtraction bodyExtractor;
	

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
	
	public SourcePanel(FolderReader folderExtractor,
			ReferenceExtraction refExtraction, HeadExtraction headExtraction,
			BodyExtraction bodyExtraction) {
		super();
		this.folExtractor = folderExtractor;
		this.refExtractor = refExtraction ;
		this.headExtractor = headExtraction;
		this.bodyExtractor = bodyExtraction;
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
						if (sourceFolderList.getSelectedIndex()!=-1){
							sourceFolderList.remove(sourceFolderList.getSelectedIndex());
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
	
	private JList<String> getSourceFolderList() {
		
		if(sourceFolderList == null) {
			sourceFolderListModel = 
				new DefaultListModel<String>();
			sourceFolderList = new JList<String>();
			sourceFolderList.setModel(sourceFolderListModel);
		}
		return sourceFolderList;
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
			allRunBt.setActionCommand(ALL_STEPS);
			
		}
		return allRunBt;
	}
	
	private JRadioButton getOnlyPrebt() {
		if(onlyPrebt == null) {
			onlyPrebt = new JRadioButton();
			onlyPrebt.setText("only splitting");
			this.onlyPrebt.setActionCommand(SPLIT_STEP);
		}
		return onlyPrebt;
	}
	
	private JRadioButton getRefExt() {
		if(refExt == null) {
			refExt = new JRadioButton();
			refExt.setText("reference extraction");
			refExt.setActionCommand(REF_STEPS);
		}
		return refExt;
	}
	
	private JRadioButton getHeaExtBt() {
		if(heaExtBt == null) {
			heaExtBt = new JRadioButton();
			heaExtBt.setText("head Extraction");
			heaExtBt.setActionCommand(HEAD_STEP);
		}
		return heaExtBt;
	}
	
	private JRadioButton getPhrExtBt() {
		if(phrExtBt == null) {
			phrExtBt = new JRadioButton();
			phrExtBt.setText("phrase Extraction");
			phrExtBt.setActionCommand(PHR_STEPS);
		}
		return phrExtBt;
	}
	
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("Run");
			jButton1.addActionListener(new ActionListener(){
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] folders = new String[sourceFolderList.getModel().getSize()];
					for (int i = 0;i<sourceFolderList.getModel().getSize();i++){
						folders[i] = sourceFolderList.getModel().getElementAt(i);
					}
					if (runOption.equals(ALL_STEPS)){
						initProcess(folders);
					}else {
						try {
							initSubProcess(folders,runOption);
						} catch (IOException e1) {
							JOptionPane.showConfirmDialog(null, e1.getMessage(),
									"ERROR", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						}
					}
					
				}

				
				
			});
		}
		return jButton1;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FileMenuBar.CHANGE_ADD_FOLDER))
		this.sourceFolderListModel.addElement((String)evt.getNewValue());
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

	
	private void  initProcess(String[] folderPath){
		try {
			for (String folder:folderPath){
				
			BaseDoc [] docs;
			if (!folder.contains("."))
				docs= this.folExtractor.processFolder(folder);
			else
				docs = new BaseDoc[]{this.folExtractor.processFile(new File(folder))};
				for (BaseDoc d : docs){
					
					
					this.firePropertyChange(NEW_DOC, "",d.getFileName());
					
					if (d.get(BaseDoc.HEAD)!=null&&d.get(BaseDoc.REFERENCES)!=null&&
							d.get(BaseDoc.BODY)!=null){
						this.firePropertyChange(NEW_HEAD_PART, "", d.get(BaseDoc.HEAD));
						this.firePropertyChange(NEW_REF_PART, "", d.get(BaseDoc.REFERENCES));
						
						this.headExtractor.headMining(d.get(BaseDoc.HEAD));
						this.firePropertyChange(NEW_HEAD_ENTITIES, "", "g");
						this.refExtractor.referenceMining(d.get(BaseDoc.REFERENCES));
						this.firePropertyChange(NEW_REF_VECTOR,null , refExtractor.getCitationVector());
						this.bodyExtractor.setText(d.get(BaseDoc.BODY));
						this.bodyExtractor.extractQuotes(refExtractor.getCitationVector());
					}else System.out.println("NO SPLIT"+d.getFileName());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initSubProcess(String[] sources, final String type) throws IOException {
		File dir;
		BufferedReader br;
		StringBuffer dataBuffer ;
		FilenameFilter filter = new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				if (type.equals(HEAD_STEP)){
					if (name.endsWith("head"))
						return true;
				}else if (type.equals(REF_STEPS)){
					if (name.endsWith("ref"))
						return true;
				}else if (type.equals(PHR_STEPS)){
					if (name.endsWith("body"))
						return true;
				}
				return false;
			}
			
		};
		for (String source: sources ){
			dir = new File(source);
			
			File[] files = dir.listFiles(filter);
			if (files ==null){
				files = new File[]{dir};
			}
			for (File f :files){
				br = new BufferedReader(new FileReader(f));
				dataBuffer = new StringBuffer();
				while(br.ready()){
					dataBuffer.append(br.readLine()+System.getProperty("line.separator"));
				}
				if (type.equals(HEAD_STEP)){
					this.headExtractor.headMining(dataBuffer.toString());
				}else if (type.equals(REF_STEPS)){
					this.firePropertyChange(NEW_DOC, "", f.getName());
					this.firePropertyChange(NEW_REF_PART, "", dataBuffer.toString());
					refExtractor.referenceMining(dataBuffer.toString());
					this.firePropertyChange(NEW_REF_VECTOR, null, refExtractor.getCitationVector());
				}else if (type.equals(PHR_STEPS)){
					bodyExtractor.setText(dataBuffer.toString());
				}
			}
				
		}
	}
	
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getJButton1());
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
