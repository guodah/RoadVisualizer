package com.sim.gui.handler;

import java.awt.Graphics;

import com.sim.gui.ViewMode;
import com.sim.roads.*;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.GraphicsUtil;


/**
 * This class is able to draw a basic road in one of 
 * the three modes: <p>
 * <ol>
 * <li> only draw the center line
 * <li> draw the triangles of graphics model
 * <li> draw the graphics model with color
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class BasicRoadGUIHandler implements GUIHandler{
	private BasicRoad road;
	public BasicRoadGUIHandler(BasicRoad road){
		this.road = road;
	}
	
	public void draw(Graphics g, com.sim.gui.ThreeDEnv env, ViewMode mode) {
		switch(mode){
		case CENTER_LINE:
			drawCenterLine(g, env);
			break;
		case TRIANGLE:
			drawDiagonal(g, env);
			break;
		case COLOR:
			drawTriangles(g, env);
			break;
		}
	}

	private void drawCenterLine(Graphics g, com.sim.gui.ThreeDEnv env){
		GraphicsUtil.drawPolyLine(g, road.getCurvePts(), env);
	}

	private void drawTriangles(Graphics g, com.sim.gui.ThreeDEnv env){
		GraphicsUtil.fillTriangles(g, road.getXsecPts(), env);
	}
	
	private void drawDiagonal(Graphics g, com.sim.gui.ThreeDEnv env){
		GraphicsUtil.drawCrossSectionTriangles(g, road.getXsecPts(), env);
	}
}
