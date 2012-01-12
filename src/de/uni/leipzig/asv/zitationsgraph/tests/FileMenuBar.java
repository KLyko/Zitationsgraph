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
	public static final String REF_FILE = "referenceFile";
	public static final String HEAD_FILE = "headFile";
	
	public static final String CHANGE_ADD_FOLDER = "AddFolder";
	public static final String PROP_SAVE_FOLDER = "saveFolder";
	private JMenu jMenu3;
	private JMenu jMenu5;
	private JMenuItem helpMenuItem;
	private JMenuItem deleteMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem pasteMenuItem;
	private JSeparator jSeparator1;
	private JMenuItem cutMenuItem;
	private JMenu jMenu4;
	private JMenuItem exitMenuItem;
	private JSeparator jSeparator2;
	private JMenuItem closeFileMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem openFileMenuItem;
	private JMenuItem newFileMenuItem;
	private JFileChooser folderChooser;
	private JFileChooser saveChooser;
	private String folderPath;
	private String saveFolderPath;
	private TestApplication testApp;
	
	private String openSelectOpt;
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
				
					folderChooser.setAccessory(this.initTestSelection());
					openFileMenuItem.addActionListener(new ActionListener() {
						

						public void actionPerformed(ActionEvent e) {
							
							int mode = folderChooser.showOpenDialog(null);
							if (mode == JFileChooser.APPROVE_OPTION){
								String oldValue = getFolderPath();
								setFolderPath(folderChooser.getSelectedFile().getAbsolutePath());
								System.out.println(folderPath+"option"+openSelectOpt);
								if (folderPath != ""){
									firePropertyChange(CHANGE_ADD_FOLDER, oldValue, folderPath);
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
				{
					saveAsMenuItem = new JMenuItem();
					jMenu3.add(saveAsMenuItem);
					saveAsMenuItem.setText("Save As ...");
				}
				{
					closeFileMenuItem = new JMenuItem();
					jMenu3.add(closeFileMenuItem);
					closeFileMenuItem.setText("Close");
				}
				{
					jSeparator2 = new JSeparator();
					jMenu3.add(jSeparator2);
				}
				{
					exitMenuItem = new JMenuItem();
					jMenu3.add(exitMenuItem);
					exitMenuItem.setText("Exit");
				}
			}
			{
				jMenu4 = new JMenu();
				this.add(jMenu4);
				jMenu4.setText("Edit");
				{
					cutMenuItem = new JMenuItem();
					jMenu4.add(cutMenuItem);
					cutMenuItem.setText("Cut");
				}
				{
					copyMenuItem = new JMenuItem();
					jMenu4.add(copyMenuItem);
					copyMenuItem.setText("Copy");
				}
				{
					pasteMenuItem = new JMenuItem();
					jMenu4.add(pasteMenuItem);
					pasteMenuItem.setText("Paste");
				}
				{
					jSeparator1 = new JSeparator();
					jMenu4.add(jSeparator1);
				}
				{
					deleteMenuItem = new JMenuItem();
					jMenu4.add(deleteMenuItem);
					deleteMenuItem.setText("Delete");
				}
			}
			{
				jMenu5 = new JMenu();
				this.add(jMenu5);
				jMenu5.setText("Help");
				{
					helpMenuItem = new JMenuItem();
					jMenu5.add(helpMenuItem);
					helpMenuItem.setText("Help");
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
		refText.setText("reference text");
		refText.setActionCommand(REF_FILE);
		selPan.add(refText);
		JRadioButton headText = new JRadioButton ();
		headText.setText("head text");
		headText.setActionCommand(HEAD_FILE);
		selPan.add(headText);
		openSelection.add(allDoc);
		openSelection.add(refText);
		openSelection.add(headText);
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
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	/**
	 * @return the folderPath
	 */
	public String getFolderPath() {
		return folderPath;
	}

}
