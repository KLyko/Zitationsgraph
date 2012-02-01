package de.uni.leipzig.asv.zitationsgraph.tests.controls;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import de.uni.leipzig.asv.zitationsgraph.tests.TestApplication;

public class TestControl implements WindowListener{

	private TestApplication app;
	public TestControl(TestApplication app){
		this.app = app;
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		app.release();
		app = null;
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
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

}
