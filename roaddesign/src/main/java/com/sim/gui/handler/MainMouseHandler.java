package com.sim.gui.handler;


import java.awt.event.*;
import java.awt.*;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.sim.central.Logging;
import com.sim.central.RoadDesign;
import com.sim.debug.Debug;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.network.RoadNetwork;
import com.sim.network.update.NetworkUpdate;
import com.sim.network.update.NetworkUpdateHandler;
import com.sim.network.update.RoadAddUpdate;
import com.sim.network.update.RoadCloseUpdate;
import com.sim.roads.Road;


/**
 * This class is able to respond to either of the four events:<p>
 * <ol>
 * <li> left mouse-click to add a key point
 * <li> right mouse-click to end the editing of the current road
 * <li> drag mouse to move the road network
 * <li> scroll the mouse to zoom in or out
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class MainMouseHandler implements
	MouseListener, MouseMotionListener, MouseWheelListener{

	private static final int WHEEL_ZOOM_RATE = 10;	
	private Point previous = new Point();
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if(RoadDesign.DEBUG){
			Object temp = RoadDesign.popDropBox();
			if(temp!=null){
				int [] xy = (int[])temp;
				int _x = e.getX();
				int _y = e.getY();
				e.translatePoint(xy[0]-_x, xy[1]-_y);
			}
		}
		
		boolean left = false;
		NetworkUpdate update = null;
		Vector23f v = null;
		if(e.getButton()==MouseEvent.BUTTON1){
			// left click
			left = true;
			v = RoadDesign.mapToGround(e.getX(), e.getY()); 
			update = new RoadAddUpdate(v);
//			update = new RoadAddUpdate(Debug.getPt());
		}else if(e.getButton()==MouseEvent.BUTTON3){
			left = false;
			v = RoadDesign.mapToGround(e.getX(), e.getY()); 
			update = new RoadCloseUpdate(v); 
		}
		
		// updating the road network
		if(RoadDesign.handleNetworkUpdate(update)){
			
			if(left){
				Logging.getLogger().info(String.format(
						"PT_ADD: (%.3f, %.3f, %.3f)",	v.x,v.y, v.z));
				System.out.printf("(%d,%d)\n", e.getX(), e.getY());
			}else{
				Logging.getLogger().info(String.format(
						"PT_CLOSE: (%.3f, %.3f, %.3f)",	v.x,v.y, v.z));				
			}
			
			// saving the current key point 
			updatePrevious(e);
			
			// re-draw
			RoadDesign.update();			
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		updatePrevious(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int deltaX = e.getX()-previous.x;
		int deltaY = e.getY()-previous.y;
		
		RoadDesign.handleEnvUpdate(deltaX, deltaY, 0);
		updatePrevious(e);
		RoadDesign.update();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int r = e.getWheelRotation();
		RoadDesign.handleEnvUpdate(0, 0, r*WHEEL_ZOOM_RATE);
		RoadDesign.update();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		updatePrevious(e);
		RoadDesign.update();
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
}
