package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Dimension;
import javax.swing.BoxLayout;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

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
public class ResultView extends javax.swing.JPanel {
	
	
	private SourcePanel sourcePanel;
	private ReferencePan referencePan;
	private JTabbedPane jTabbedPane1;
	private JSplitPane jSplitPane1;
	private FileMenuBar fileMenuBar;
	private FolderReader folderExtractor;
	private ReferenceExtraction refExtraction;
	private HeadExtraction headExtraction;
	private BodyExtraction bodyExtraction;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new ResultView(null));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public ResultView(FileMenuBar jMenuBar1) {
		super();
		folderExtractor = new FolderReader();
		refExtraction = new ReferenceExtraction();
		headExtraction = new HeadExtraction();
		bodyExtraction = new BodyExtraction();
		this.fileMenuBar = jMenuBar1;
		initGUI();
	}
	
	private void initGUI() {
		try {
			BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.X_AXIS);
			this.setLayout(thisLayout);
			this.setSize(229, 500);
			this.setPreferredSize(new java.awt.Dimension(579, 500));
			{
				jSplitPane1 = new JSplitPane();
				
				this.add(jSplitPane1);
				{
					sourcePanel = new SourcePanel(folderExtractor,refExtraction,headExtraction,bodyExtraction);
				
					jSplitPane1.add(sourcePanel, JSplitPane.LEFT);
					jSplitPane1.setOneTouchExpandable(true);
					fileMenuBar.addPropertyChangeListener(sourcePanel);

				}
				{
					jTabbedPane1 = new JTabbedPane();
					jSplitPane1.add(jTabbedPane1, JSplitPane.RIGHT);
					{
						referencePan = new ReferencePan();
						sourcePanel.addPropertyChangeListener(referencePan);
						jTabbedPane1.addTab("references", null, referencePan, null);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
