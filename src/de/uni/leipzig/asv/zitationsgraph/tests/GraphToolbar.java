package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import prefuse.Display;
import prefuse.util.display.ExportDisplayAction;

import de.uni.leipzig.asv.zitationsgraph.tests.vis.PubVis;

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
public class GraphToolbar extends javax.swing.JPanel {
	private JToggleButton jToggleButton1;
	
	private JButton picSaveBt;
	private ReferencePan refPan;
	private JButton showAllBt;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new GraphToolbar());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public GraphToolbar() {
		super();
		initGUI();
	}
	
	public GraphToolbar (ReferencePan refPan){
		super();
		this.refPan = refPan;
		initGUI();
		
	}
	
	private void initGUI() {
		try {
			BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.X_AXIS);
			this.setLayout(thisLayout);
			{
				jToggleButton1 = new JToggleButton();
				this.add(jToggleButton1);
				jToggleButton1.setText("stop animation");
				jToggleButton1.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						refPan.animateLayout(jToggleButton1.isSelected());
						String text = (jToggleButton1.isSelected())?"stop animation":"start Animation";
						jToggleButton1.setText(text);
					}
					
				});
			}
			{
				picSaveBt = new JButton();
				this.add(picSaveBt);
				Action a = refPan.getImageAction();
				a.putValue(Action.NAME, "save pic");
				picSaveBt.setAction(a);
			}
			{
				showAllBt = new JButton();
				this.add(showAllBt);
				showAllBt.setText("show Complete");
				showAllBt.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						refPan.resetFilter();
						
					}
					
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
