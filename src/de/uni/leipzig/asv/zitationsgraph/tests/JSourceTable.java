package de.uni.leipzig.asv.zitationsgraph.tests;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;



public class JSourceTable extends prefuse.util.ui.JPrefuseTable {

	
	/**
	 * add a column with a remove button with this constant
	 */
	public static final String REMOVE_COL = "remove";
	
	private int removeIndex;
	private FileCellEditor fileEditor;
	private boolean isSourceMode = true;
	private FileCellRenderer fileRenderer;
	
	private RemoveCellRenderer removeRenderer;

	private RemoveCellEditor removeEditor;
	
	/**
	* Auto-generated main method to display this 
	* JInternalFrame inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JSourceTable(null,true));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public JSourceTable(Table dataTable,boolean isSourceMode) {
		super(dataTable);
		this.isSourceMode = isSourceMode;
		Column c = dataTable.getColumn(REMOVE_COL);
		removeIndex = dataTable.getColumnNumber(c);
		this.tableHeader.setReorderingAllowed(false);
		if (removeIndex != -1){
		TableColumn  removeCol = tableHeader.getColumnModel().getColumn(removeIndex);
		
		removeCol.setMaxWidth(45);
		removeCol.setHeaderValue("");
		}
		fileEditor = new FileCellEditor();
		fileRenderer = new FileCellRenderer();
		removeEditor = new RemoveCellEditor();
		removeRenderer = new RemoveCellRenderer();
		this.setCellSelectionEnabled(true);
		if (isSourceMode)
		this.getTable().addRow();
	}

	
	@Override
	public boolean isCellEditable(int row, int col){
		if (isSourceMode){
		if ((col==0 && row==0)||row>0)
			return true;
		else
			return  false;
		}else {
			if (col>0){
				return true;
			}else 
				return false;
		}
	} 
	
	@Override
	public TableCellEditor getCellEditor(int row, int col){
		if (isSourceMode){
		if ((col !=1||row!=0)&&col != removeIndex){
			return fileEditor;
		}else if (col == removeIndex && row!=0){
			return removeEditor;
		}else{
			return super.getCellEditor();
		}
		}else {
			if (col >0){
				return fileEditor;
			}else
				return super.getCellEditor();
		}
	}
	
	
	public TableCellRenderer getCellRenderer (int row, int col){
		if (isSourceMode){
		if (row ==0 && col ==0)
			return fileRenderer;
		else if (row>0 && col == removeIndex){
			return removeRenderer;
		}else
			return super.getCellRenderer(row, col);
		}else 
			return super.getCellRenderer(row, col);
	}
	
	public void setVisibleColumn (boolean visible, int col){
		TableColumn column = this.columnModel.getColumn(col);
		if (!visible){
			column.setMinWidth(0);
			column.setMaxWidth(0);
			column.setWidth(0);
		}else {
			column.setMaxWidth(100);
			column.setMinWidth(40);
			column.setWidth(50);
			
		}
	}

	private class FileCellEditor extends AbstractCellEditor implements TableCellEditor,ActionListener{
		
		JButton addBt;
		JButton editCaseBt;
		String changeSource;
		
		JFileChooser fileChooser;
		
		FileCellEditor (){
			addBt = new JButton();
			
			addBt.setActionCommand("add");
			addBt.addActionListener(this);
			editCaseBt = new JButton ();
			editCaseBt.setActionCommand("edit");
			editCaseBt.addActionListener(this);
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (isSourceMode){
				if (row ==0 &&column==0 ){
					return addBt;
				}	
				else if (row >0 ){
					return editCaseBt;
				}else {
					return null;
				}
			}else {
				if (column>0){
					return editCaseBt;
				}else 
					return null;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("add")){
				fileChooser.setMultiSelectionEnabled(true);
				int mode = fileChooser.showOpenDialog(null);
				if (mode == JFileChooser.APPROVE_OPTION){
					File[] files = fileChooser.getSelectedFiles();
					String [] sources = new String[files.length] ;
					for (int i = 0;i<files.length;i++){
						sources[i]= files[i].getAbsolutePath();
					}
					fillList(sources);
					
				}
				
			}else if (e.getActionCommand().equals("edit")){
				fileChooser.setMultiSelectionEnabled(false);
				int mode = fileChooser.showOpenDialog(null);
				if (mode == JFileChooser.APPROVE_OPTION){
					File f = fileChooser.getSelectedFile();
					changeSource = f.getAbsolutePath();
				}
			}
			this.fireEditingStopped();
		}

		@Override
		public Object getCellEditorValue() {
		
			return changeSource;
		}
		
	}
	
	private class RemoveCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener{

		JButton removeBt; 
		int selectedRow;
		RemoveCellEditor(){
			removeBt = new JButton();
			removeBt.setActionCommand("remove");
			removeBt.addActionListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("remove")){
				removeTuple(selectedRow);
			}
			this.cancelCellEditing();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			selectedRow = row;
			return removeBt;
		}
		
	}
	
	private class FileCellRenderer extends JButton implements TableCellRenderer{

		
		FileCellRenderer (){
			super("add source");
			this.setForeground(new Color(37, 65, 23));
			
			Font font = new Font ("arial", Font.BOLD, 14);
			this.setFont(font);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			// TODO Auto-generated method stub
			return this;
		}
		
	}
	
	private class RemoveCellRenderer extends JButton implements TableCellRenderer{
		RemoveCellRenderer(){
			super ("X");
			this.setForeground(Color.red);
			this.setToolTipText("remove");
			
			Font font = new Font ("arial", Font.BOLD, 14);
			this.setFont(font);
			this.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("remove");
					
				}
				
			});
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return this;
		}
		
		
	}
	
	private  void fillList(String[] sources) {
		for (String source: sources){
			int row = this.getTable().addRow();
			this.getTable().set(row, 0, source);
		}
	}
	
	
	public void removeTuple(int row){
		List<Tuple> tempList = new ArrayList<Tuple>();
		Iterator <Tuple> iter = this.getTable().tuples();
		Table tempTable = new Table();
		tempTable.addColumns(this.getTable().getSchema());
		int rowCount=0;
		while (iter.hasNext()){
			Tuple t = iter.next();
			
			if (rowCount != row){
				int tempRow = tempTable.addRow();
				for (int col =0; col <t.getColumnCount();col++){
					 
					tempTable.set(tempRow, col, t.get(col));
				}
			}
			rowCount++;
		}
		this.getTable().clear();
		iter = tempTable.tuples();
		while (iter.hasNext()){
			this.getTable().addTuple(iter.next());
		}
		
	}
	
	
	
	
}
