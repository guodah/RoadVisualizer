package com.sim.debug;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.*;


import javax.swing.JPanel;

import com.sim.central.RoadDesign;
import com.sim.debug.components.DebugComponent;
import com.sim.geometries.*;
import com.sim.gui.ThreeDEnv;
import com.sim.network.update.NetworkUpdate;
import com.sim.network.update.RoadAddUpdate;
import com.sim.network.update.RoadCloseUpdate;


public class SimpleViewerPanel extends JPanel 
	implements MouseMotionListener, MouseListener, MouseWheelListener{

	ThreeDEnv env;
	ArrayList<DebugComponent> components = new ArrayList<DebugComponent>();
	
	public SimpleViewerPanel(float width, float height, float init_scale){
		env = ThreeDEnv.findDefaultEnv(width, height, init_scale);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
	}
	
	protected void paintComponent(Graphics g){
		g.clearRect(0, 0, getWidth(), getHeight());
		for(DebugComponent c : components){
			c.draw(g, env);
		}
	}
	
	private static final int WHEEL_ZOOM_RATE = 10;	
	private Point previous = new Point();
	
	@Override
	public void mouseClicked(MouseEvent e) {
		updatePrevious(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		updatePrevious(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int deltaX = e.getX()-previous.x;
		int deltaY = e.getY()-previous.y;
		
		env.moveEye(deltaX, deltaY, 0);
		updatePrevious(e);
		repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int r = e.getWheelRotation();
		env.moveEye(0, 0, r*WHEEL_ZOOM_RATE);
		
		
		
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		updatePrevious(e);
		repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		updatePrevious(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		updatePrevious(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		updatePrevious(e);
	}
	
	private void updatePrevious(MouseEvent e){
		previous.x = e.getX();
		previous.y = e.getY();
	}

	public void addComponent(DebugComponent c) {
		components.add(c);
	}

	public void centerView() {
		Vector23f v = DebugComponent.findCentroid(components).getVector();
		float height, width;
		height = DebugComponent.findHeight(components);
		width = DebugComponent.findWidth(components);
		
		env.fitRange(width, height);		
		env.moveEyeTo(v.x, v.y);
	}

}
