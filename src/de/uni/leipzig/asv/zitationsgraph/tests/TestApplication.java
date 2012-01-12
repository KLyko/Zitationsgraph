package de.uni.leipzig.asv.zitationsgraph.tests;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;

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
public class TestApplication extends javax.swing.JFrame {

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	static private JEditorPane entityEditEditor;
	static private JPanel mainPanel;
	private FileMenuBar jMenuBar1;
	private TestControl control;
	private ResultView resultView;
	private JTabbedPane jTabbedPane1;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		
				TestApplication inst = new TestApplication();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
				
		
	}
	
	public TestApplication() {
		super();
		initGUI();
		
	}
	
	private void initGUI() {
		try {
			control = new TestControl (this);
			this.addWindowListener(control);
			GridLayout thisLayout = new GridLayout(1, 1);
			thisLayout.setHgap(5);
			thisLayout.setVgap(5);
			thisLayout.setColumns(1);
			getContentPane().setLayout(thisLayout);
			setSize(400, 300);
			{
				jMenuBar1 = new FileMenuBar(this);
				setJMenuBar(jMenuBar1);
			}

			{
				jTabbedPane1 = new JTabbedPane();
				getContentPane().add(jTabbedPane1);
				
				{
					mainPanel = new JPanel();
					jTabbedPane1.addTab("Test Editing", null, mainPanel, null);
					GridBagLayout mainPanelLayout = new GridBagLayout();
					mainPanelLayout.rowWeights = new double[] {0.1};
					mainPanelLayout.rowHeights = new int[] {7};
					mainPanelLayout.columnWeights = new double[] {0.1, 0.1};
					mainPanelLayout.columnWidths = new int[] {7, 7};
					mainPanel.setLayout(mainPanelLayout);
					{
						entityEditEditor = new JEditorPane();
						System.out.println("te");
						
						entityEditEditor.addMouseMotionListener(new MouseMotionListener(){
							
							@Override
							public void mouseDragged(MouseEvent e) {
								System.out.println(entityEditEditor.getSelectedText());
								
							}
							
							@Override
							public void mouseMoved(MouseEvent e) {
								// TODO Auto-generated method stub
								
							}
							
						});
						mainPanel.add(entityEditEditor, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						entityEditEditor.setText("jEditorPane1");
					}
				}
				{
					resultView = new ResultView(jMenuBar1);
					
					jTabbedPane1.addTab("Results", null, resultView, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	public void release() {
		// TODO Auto-generated method stub
		this.dispose();
		this.control =null;
	}
}
