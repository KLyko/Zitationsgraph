package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	
	private static final Logger log = Logger.getLogger(GraphToolbar.class.getName());
	private JButton picSaveBt;
	private ReferencePan refPan;
	private JLabel jLabel1;
	private JSlider refSlider;
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
						log.info(text);
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
			{
				jLabel1 = new JLabel();
				this.add(jLabel1);
				jLabel1.setText("reference filter");
			}
			{
				refSlider = new JSlider();
				refSlider.setMinimum(1);
				refSlider.setMaximum(5);
				refSlider.setBounds(0, 0, 150, 20);
				refSlider.setPaintLabels(true);
				refSlider.setMajorTickSpacing(1);
				refSlider.setPaintTicks(true);
				refSlider.setValue(1);
				refSlider.addChangeListener(new ChangeListener(){

					@Override
					public void stateChanged(ChangeEvent e) {
						int numRef = refSlider.getValue();
						refPan.setNumOfRef(numRef);
						
					}
					
				});
				this.add(refSlider);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
