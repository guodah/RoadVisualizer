package com.sim.debug;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.sim.debug.components.DebugComponent;
import com.sim.geometries.Vector23f;


public class SimpleViewer extends JDialog
	implements KeyListener{
	
	public static final float WINDOW_WIDTH = 0.8f;
	public static final float WINDOW_HEIGHT = 0.8f;
	public SimpleViewerPanel panel;
	public SimpleViewer(JFrame owner, String title, int width, int height) {
		super(owner, title, true);
		setSize(width, height);
		
		panel = new SimpleViewerPanel(width, height, 2);
		
		add(panel);
		this.addKeyListener(this);
	}

	public void addComponent(DebugComponent c){
		panel.addComponent(c);
	}
	
	public void centerView(){
		panel.centerView();
	}
	
	public static void main(String args[]){
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		SimpleViewer frame=new SimpleViewer(null, "Simple Viewer", 
				(int)(WINDOW_WIDTH*dim.width), 
				(int)(WINDOW_HEIGHT*dim.height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	private int keyPressed = -1;
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		keyPressed = arg0.getKeyCode();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		if(keyPressed==KeyEvent.VK_ESCAPE){
			this.dispose();
		}
	}

	public void refresh() {
		panel.repaint();
	}
}
