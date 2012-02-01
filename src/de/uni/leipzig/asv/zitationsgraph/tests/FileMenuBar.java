package de.uni.leipzig.asv.zitationsgraph.tests;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


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
public class FileMenuBar extends javax.swing.JMenuBar {
	

	public static final String PDF_FILE ="pdfFile";
	public static final String TEST_FILE = "testFile";
	
	public static final String CHANGE_ADD_FOLDER = "AddFolder";
	public static final String TEST = "test";
	public static final String PROP_SAVE_FOLDER = "saveFolder";
	
	private JMenu jMenu3;
	private JMenuItem saveMenuItem;
	private JMenuItem openFileMenuItem;
	private JMenuItem newFileMenuItem;
	private JFileChooser folderChooser;
	private JFileChooser saveChooser;
	private String[] folderPath;
	private String saveFolderPath;
	private TestApplication testApp;
	
	private String openSelectOpt = PDF_FILE;
	private ButtonGroup openSelection;

	/**
	* Auto-generated main method to display this 
	* JInternalFrame inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new FileMenuBar());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public FileMenuBar(TestApplication testApplication) {
		super();
		this.testApp =testApplication;
		initGUI();
	}
	
	public FileMenuBar (){
		super();
		initGUI();
	}
	
	private void initGUI() {
		try { 
			{
				
				jMenu3 = new JMenu();
				this.add(jMenu3);
				jMenu3.setText("File");
				{
					newFileMenuItem = new JMenuItem();
					jMenu3.add(newFileMenuItem);
					newFileMenuItem.setText("New");
				}
				{
					openFileMenuItem = new JMenuItem();
					jMenu3.add(openFileMenuItem);
					openFileMenuItem.setText("Open");
					folderChooser = new  JFileChooser();
					folderChooser.setDialogType(JFileChooser.OPEN_DIALOG);
					folderChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					folderChooser.setMultiSelectionEnabled(true);
					folderChooser.setAccessory(this.initTestSelection());
					openFileMenuItem.addActionListener(new ActionListener() {
						

						public void actionPerformed(ActionEvent e) {
							
							int mode = folderChooser.showOpenDialog(null);
							if (mode == JFileChooser.APPROVE_OPTION){
								String[] oldValue = getFolderPath();
								File[] files =(folderChooser.getSelectedFiles());
								folderPath = new String[files.length];
								for (int i = 0 ; i< files.length;i++ ){
									folderPath[i] = files[i].getAbsolutePath();
								}
								System.out.println(folderPath+"option"+openSelectOpt);
								if (folderPath.length!= 0 &&openSelectOpt.equals(PDF_FILE)){
									firePropertyChange(CHANGE_ADD_FOLDER, oldValue, folderPath);
								}else if (folderPath.length!= 0 &&openSelectOpt.equals(TEST_FILE)){
									System.out.println(folderPath[0]);
									firePropertyChange(TEST, null, folderPath[0]);
								}
							 
							}
							
						}

						
					});
				}
				{
					this.saveChooser = new JFileChooser();
					this.saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
					this.saveChooser.setMultiSelectionEnabled(true);
					this.saveChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						
					
					saveMenuItem = new JMenuItem();
					saveMenuItem.addActionListener(new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							int mode = saveChooser.showSaveDialog(null);
							if (mode ==JFileChooser.APPROVE_OPTION){
								saveFolderPath = saveChooser.getSelectedFile().getAbsolutePath();
								firePropertyChange(PROP_SAVE_FOLDER,"", saveFolderPath);
							}
							
						}
						
					});
					jMenu3.add(saveMenuItem);
					saveMenuItem.setText("Save");
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JComponent initTestSelection(){
		openSelection = new ButtonGroup();
		JPanel selPan = new JPanel ();
		selPan.setPreferredSize(new Dimension(80,20));
		JRadioButton allDoc = new JRadioButton ();
		allDoc.setText("pdf files");
		allDoc.setActionCommand(PDF_FILE);
		selPan.add(allDoc);
		JRadioButton refText = new JRadioButton ();
		refText.setText("test");
		refText.setActionCommand(TEST_FILE);
		selPan.add(refText);
		
		openSelection.add(allDoc);
		openSelection.add(refText);
		
		openSelection.setSelected(allDoc.getModel(), true);
		Enumeration<AbstractButton> enumBt =openSelection.getElements();
		while(enumBt.hasMoreElements()){
			
			enumBt.nextElement().addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					openSelectOpt = openSelection.getSelection().getActionCommand();
					System.out.println(openSelectOpt);
				}
			});
			
		}

			
		return selPan;
	}

	/**
	 * @param folderPath the folderPath to set
	 */
	public void setFolderPath(String[] folderPath) {
		this.folderPath = folderPath;
	}

	/**
	 * @return the folderPath
	 */
	public String[] getFolderPath() {
		return folderPath;
	}

}
