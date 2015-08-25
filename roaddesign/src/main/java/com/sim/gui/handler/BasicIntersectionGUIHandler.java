package com.sim.gui.handler;

import java.awt.Color;



import java.awt.Graphics;
import java.util.ArrayList;

import com.sim.core.basic.ModelGenConsts;
import com.sim.geometries.RoadVector;
import com.sim.gui.ViewMode;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.intersections.basic.BasicLeg;
import com.sim.util.GraphicsUtil;


/**
 * This class is able to draw a basic intersection in one of 
 * the three modes: <p>
 * <ol>
 * <li> only the border of intersection
 * <li> draw the triangles of graphics model
 * <li> draw the graphics model with color
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class BasicIntersectionGUIHandler implements GUIHandler{

	private BasicIntersection intersection;

	public BasicIntersectionGUIHandler(BasicIntersection intersection){
		this.intersection = intersection;
	}
	
	@Override
	public void draw(Graphics g, com.sim.gui.ThreeDEnv env, ViewMode mode) {
		if(mode==ViewMode.CENTER_LINE){
			drawBorder(g,env);
		}else if(mode==ViewMode.TRIANGLE){
			drawTriangles(g,env);			
		}else if(mode==ViewMode.COLOR){
			drawWithColor(g,env);
		}
	}
	
	private void drawBorder(Graphics g, com.sim.gui.ThreeDEnv env){
		ArrayList<RoadVector> border = intersection.getInnerBorder();
		GraphicsUtil.drawPolygon(g, border, env);
	}

	private void drawTriangles(Graphics g, com.sim.gui.ThreeDEnv env){
		// 1. get the legs
		ArrayList<BasicLeg> legs = intersection.getLegs();
		for(int i=0;i<legs.size();i++){
			BasicLeg leg = legs.get(i);
			ArrayList<ArrayList<RoadVector>> extension = leg.getExtension();
			ArrayList<ArrayList<RoadVector>> marker = leg.getMarker();
			
			GraphicsUtil.drawCrossSectionTriangles(g, extension, env);
			GraphicsUtil.drawCrossSectionTriangles(g, marker, env);
		}
		
		// 2. get the inner area
		ArrayList<RoadVector> innerArea = intersection.getInnerArea();
		RoadVector pts[] = new RoadVector[innerArea.size()];
		innerArea.toArray(pts);
		GraphicsUtil.drawTriangles(g, pts, env);
		
		//3. get the shoulders
		ArrayList<RoadVector> shoulderAreas = intersection.getShoulderAreas();
		pts = new RoadVector[shoulderAreas.size()];
		shoulderAreas.toArray(pts);
		GraphicsUtil.drawTriangles(g, pts, env);
	}
	
	private void drawWithColor(Graphics g, com.sim.gui.ThreeDEnv env){
		// 1. get the legs
		ArrayList<BasicLeg> legs = intersection.getLegs();
		for(int i=0;i<legs.size();i++){
			BasicLeg leg = legs.get(i);
			ArrayList<ArrayList<RoadVector>> extension = leg.getExtension();
			ArrayList<ArrayList<RoadVector>> marker = leg.getMarker();
			
			GraphicsUtil.fillTriangles(g, extension, env);
			GraphicsUtil.fillTriangles(g, marker, Color.white, env);
		}
		
		// 2. get the inner area
		ArrayList<RoadVector> innerArea = intersection.getInnerArea();
		RoadVector pts[] = new RoadVector[innerArea.size()]; 
		innerArea.toArray(pts);
		GraphicsUtil.fillTriangles(g, pts, Color.gray, env);
		
		//3. get the shoulders
		ArrayList<RoadVector> shoulderAreas = intersection.getShoulderAreas();
		pts = new RoadVector[shoulderAreas.size()]; 
		pts= shoulderAreas.toArray(pts);
		GraphicsUtil.fillTriangles(g, pts, Color.green, env);

	}
}
