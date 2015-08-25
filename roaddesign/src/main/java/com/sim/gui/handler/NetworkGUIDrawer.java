package com.sim.gui.handler;

import java.awt.Color;


import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import com.sim.gui.ThreeDEnv;
import com.sim.gui.ViewMode;
import com.sim.intersections.Intersection;
import com.sim.network.RoadNetwork;
import com.sim.roads.Road;
import com.sim.util.GraphicsUtil;

import com.sim.core.basic.*;

/**
 * This class is able to draw a road network in one of 
 * the three modes: <p>
 * <ol>
 * <li> only draw the center lines and borders of intersections
 * <li> draw the triangles of graphics model
 * <li> draw the graphics model with color
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class NetworkGUIDrawer implements GUIHandler{

	private RoadNetwork network;
	private boolean showTerrain;

	public NetworkGUIDrawer(RoadNetwork network){
		this.network = network;
	}
	
	public void showTerrain(boolean show){
		showTerrain = show;
	}
	
	@Override
	public void draw(Graphics g, com.sim.gui.ThreeDEnv threeDEnv, ViewMode viewMode) {
		Iterator<Intersection> intersections = network.getIntersections();
		while(intersections.hasNext()){
			Intersection intersection = intersections.next();
			intersection.getGUIHandler().draw(g, threeDEnv, viewMode);
		}
		
		Iterator<Road> roads = network.getRoadSegments(); 
		while(roads.hasNext()){
			Road road = roads.next();
			road.getGUIHandler().draw(g, threeDEnv, viewMode);
		}
		
		if(showTerrain && network.getTerrain()!=null){
			network.getTerrain().getGUIHandler().draw(g, threeDEnv, viewMode);
		}
	}

	public static NetworkGUIDrawer findGUIHandler(RoadNetwork network) {
		NetworkGUIDrawer handler = new NetworkGUIDrawer(network);
		return handler;
	}
}
