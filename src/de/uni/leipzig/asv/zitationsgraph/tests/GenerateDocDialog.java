package de.uni.leipzig.asv.zitationsgraph.tests;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uni.leipzig.asv.zitationsgraph.preprocessing.BaseDoc;
import de.uni.leipzig.asv.zitationsgraph.preprocessing.FolderReader;
import de.uni.leipzig.asv.zitationsgraph.tests.data.Constants;
import edu.stanford.nlp.ling.Label;

import prefuse.data.Table;
import prefuse.data.Tuple;


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
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.<br>
* <br>
* This Dialog is responsible to split specified documents in their logical parts 
* head,body, references and save the parts in the specified folders
*/
public class GenerateDocDialog extends JDialog {
	
	
	private JPanel jPanel1;
	private JSourceTable sourceTable;
	private Table  sourceDataTable;
	private JScrollPane jScrollPane2;
	private JLabel jLabel1;
	private JScrollPane jScrollPane1;
	private Table targetDataTable;
	private JSourceTable targetTable;
	private JButton jButton1;
	
	private HashMap <String,String> headMap;
	private HashMap <String,String> refMap;
	private HashMap<String, String> bodyMap;
	FolderReader reader;
	

	public GenerateDocDialog(){
		super();
		headMap = new HashMap<String,String>();
		refMap = new HashMap<String,String>();
		bodyMap = new HashMap<String,String>();
		reader = new FolderReader ();
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				headMap.clear();
				refMap.clear();
				bodyMap.clear();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		this.initGUI();
	}
	
	private void initGUI() {
		try {
			{
				this.setModal(true);
				this.setTitle("splited document generation");
			}
			{
				setSize(400,300);
				jPanel1 = new JPanel();
				BoxLayout jPanel1Layout = new BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS);
				jPanel1.setLayout(jPanel1Layout);
				getContentPane().add(jPanel1, BorderLayout.CENTER);
				JLabel label = new JLabel ("specify the source files");
				jPanel1.add(label);
				label.setForeground(new java.awt.Color(0,0,255));
				jPanel1.add(getJScrollPane1());
				jPanel1.add(getJLabel1());
				jPanel1.add(getJScrollPane2());
				jPanel1.add(getJButton1());

			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private JSourceTable getTable (){
		if (sourceTable ==null){
			sourceDataTable = new Table();
			sourceDataTable.addColumn(Constants.SOURCE_PATH, String.class);
			sourceDataTable.addColumn(JSourceTable.REMOVE_COL, String.class);
			sourceTable = new JSourceTable (sourceDataTable,true);
		}
		return sourceTable;
	}
	
	private JSourceTable getTargetTable(){
		if (targetTable == null){
			targetDataTable = new Table ();
			targetDataTable.addColumn("part", String.class);
			targetDataTable.addColumn("target", String.class);
			int row ;
			row = targetDataTable.addRow();
			targetDataTable.set(row, "part", Constants.HEAD_PART);
			row = targetDataTable.addRow();
			targetDataTable.set(row, "part", Constants.BODY_PART);
			row = targetDataTable.addRow();
			targetDataTable.set(row, "part", Constants.REF_PART);
			targetTable = new JSourceTable (targetDataTable,false);
		}
		return targetTable;
	}
	
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTable());
		}
		return jScrollPane1;
	}
	
	private JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("target folders");
			jLabel1.setForeground(new java.awt.Color(0,0,255));
		}
		return jLabel1;
	}
	
	private JScrollPane getJScrollPane2() {
		if(jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getTargetTable());
		}
		return jScrollPane2;
	}
	
	private JButton getJButton1() {
		if(jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("generate Docs");
			jButton1.setForeground(new java.awt.Color(0,0,255));
			jButton1.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					if (validateTable()){
						List<String> target = new ArrayList<String> ();
						target.add(targetDataTable.getString(0, "target"));
						target.add(targetDataTable.getString(1, "target"));
						target.add(targetDataTable.getString(2, "target"));
						try {
							generateDocs(gatherSources(Constants.SOURCE_PATH),target);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						headMap.clear();
						bodyMap.clear();
						refMap.clear();
					}else {
						JOptionPane.showConfirmDialog(null, "You must specify" +
								" for every test case file a serialized test instance",
								"File specification",
								JOptionPane.WARNING_MESSAGE,JOptionPane.OK_OPTION);
					}
					
				}
				
			});
		}
		return jButton1;
	}
	
	private boolean validateTable(){
		Iterator <Tuple> iter = this.sourceDataTable.tuples();
		int row =0;
		Tuple t ;
		boolean isValid = true;
		while (iter.hasNext()){
			t = iter.next();
			if (row>0){
				if (t.getString(Constants.SOURCE_PATH)==null ||
						t.getString(Constants.SOURCE_PATH).equals("")){
					isValid = false;
					break;
				}
			}
			row++;
		}
		iter = this.targetDataTable.tuples();
		while (iter.hasNext()){
			t = iter.next();
			if (t.getString("target")==null ||
				t.getString("target").equals("")){
				isValid = false;
				break;
			}	
		}
		return isValid;
	}
	
	private List<String> gatherSources (String field){
		List <String>sources = new ArrayList<String>();
		Iterator <Tuple> iter =this.sourceDataTable.tuples();
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
	
	private void generateDocs (List<String> sources, List<String> target) throws IOException{
		String title;
		FileWriter fw ;
		for (String source:sources){
			if (source.contains(".")){
				BaseDoc doc = reader.processFile(new File (source));
				title = doc.getFileName().substring(doc.getFileName().lastIndexOf("\\")+1);
				this.headMap.put(title, doc.get(BaseDoc.HEAD));
				this.bodyMap.put(title, doc.get(BaseDoc.BODY));
				this.refMap.put(title, doc.get(BaseDoc.REFERENCES));
			}else{
				
				BaseDoc [] docs = reader.processFolder(source);
				for (BaseDoc doc :docs){
					title =  doc.getFileName().substring(doc.getFileName().lastIndexOf("\\")+1);
					if (doc.get(BaseDoc.HEAD)!= null)
					this.headMap.put(title, doc.get(BaseDoc.HEAD));
					if (doc.get(BaseDoc.BODY)!= null)
					this.bodyMap.put(title, doc.get(BaseDoc.BODY));
					if (doc.get(BaseDoc.REFERENCES)!= null)
					this.refMap.put(title, doc.get(BaseDoc.REFERENCES));
				}
			}
		}
		for (Entry<String,String> e:this.headMap.entrySet()){
			fw = new FileWriter(target.get(0)+"/" +e.getKey()+
					"."+Constants.HEAD_PART);
			fw.write(e.getValue());
			fw.flush();
			fw.close();
		}
		for (Entry<String,String> e:this.bodyMap.entrySet()){
			fw = new FileWriter(target.get(1)+ "/"+e.getKey()+
					"."+Constants.BODY_PART);
			fw.write(e.getValue());
			fw.flush();
			fw.close();
		}
		
		for (Entry<String,String> e:this.refMap.entrySet()){
			fw = new FileWriter(target.get(2)+"/" +e.getKey()+
					"."+Constants.REF_PART);
			fw.write(e.getValue());
			fw.flush();
			fw.close();
		}
		
	}

}
