package com.sim.debug;
import java.awt.Dimension;

import java.awt.Toolkit;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.sim.central.RoadDesign;
import com.sim.debug.components.DebugComponent;
import com.sim.debug.components.DebugMesh;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.network.RoadNetwork;
import com.sim.roads.basic.BasicRoad;
//import sim.basic.BasicOuterPath;
//import sim.basic.BasicVectorMap;

public class DebugView {
	
	public static int HEIGHT = 800;
	public static int WIDTH = 800;
	public static SimpleViewer viewer;

	public static void clear(){
		viewer = new SimpleViewer(null, "Debug Viewer", WIDTH, HEIGHT);
	}
	
	public static void addComponent(DebugComponent component){
		viewer.addComponent(component);
	}
	
	public static void show(){
		viewer.centerView();
		viewer.refresh();
		viewer.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		viewer.setVisible(true);		
	}
	
	public static void showPolyline(ArrayList<RoadVector> pts, String name){
		clear();
		
		viewer.addComponent(new DebugPolyLine(pts, name));
	
		show();
	}

	public static void adhocView1() {
	/*
		RoadNetwork network = RoadDesign.getNetwork();
		HashSet<UniRoad> roads = BasicVectorMap.findTerminalRoads(network);
		DebugView.clear();
		for(int i=0;i<roads.size();i++){
			UniRoad road = roads.get(i);
			DebugView.addComponent(new DebugPolyLine(road.getCurvePts(), "road "+i));
		}
		DebugView.show();
		
		BasicVectorMap vMap = BasicVectorMap.findVectorMap(network);
		BasicPath path = vMap.getOuterLoop();
		
		DebugView.clear();
		roads = path.getRoads();
		for(int i=0;i<roads.size();i++){
			UniRoad road = roads.get(i);
			DebugView.addComponent(new DebugPolyLine(road.getCurvePts(),"road "+i));
		}
		
		ArrayList<BasicIntersection> inters = path.getIntersections();
		for(int i=0;i<inters.size();i++){
			BasicIntersection inter = inters.get(i);
			ArrayList<RoadVector> innerArea = inter.getInnerArea();
			DebugView.addComponent(new DebugMesh(innerArea, "intersection "+i));
		}
		
		DebugView.show();
	*/			
	}
	
	public static void adhocView2(){
/*
		RoadNetwork network = RoadDesign.getNetwork();
		BasicVectorMap vMap = BasicVectorMap.findVectorMap(network);
		ArrayList<BasicHole> holes = vMap.getInnerPerimeter();
		
		DebugView.clear();
		for(int i=0;i<holes.size();i++){
			BasicHole hole = holes.get(i);
			ArrayList<BasicRoad> roads = hole.getRoads();
			ArrayList<BasicIntersection> intersections = hole.getIntersections();
			
			for(int j=0;j<roads.size();j++){
				BasicRoad road = roads.get(j);
				DebugView.addComponent(new DebugPolyLine(road.getCurvePts(),"road "+j));
			}

			for(int j=0;j<intersections.size();j++){
				BasicIntersection inter = intersections.get(j);
				ArrayList<RoadVector> innerArea = inter.getInnerArea();
				DebugView.addComponent(new DebugMesh(innerArea, "intersection "+j));
			}
		}
		DebugView.show();
*/		
	}
	
	public static void adhocView3(){
/*		
		RoadNetwork network = RoadDesign.getNetwork();
		BasicVectorMap vMap = BasicVectorMap.findVectorMap(network);
		ArrayList<RoadVector> perimeter = vMap.getOuterPerimeter();
		
		DebugView.clear();
		DebugView.addComponent(new DebugPolyLine(perimeter, "perimeter"));
		DebugView.show();
*/		
	}
}
