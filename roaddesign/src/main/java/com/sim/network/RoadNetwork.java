package com.sim.network;

import java.io.FileNotFoundException;

import java.io.OutputStream;

import java.util.*;

import com.sim.geometries.*;
import com.sim.intersections.*;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.io.exporters.NetworkExporter;
import com.sim.roads.*;
import com.sim.terrain.Terrain;
import com.sim.terrain.basic.BasicTerrain;

//import sim.basic.BasicVectorMap;

/**
 * A road network consists roads, intersections, and terrain.
 * 
 * @author Dahai Guo
 *
 */
public class RoadNetwork {
	
	private ArrayList<Road> roads;
	private ArrayList<Intersection> intersections;
	private Terrain terrain;
	private NetworkExporter exporter;
	
	/**
	 * Default constructor for allocating spaces for variables
	 */
	public RoadNetwork(){
		roads = new ArrayList<Road>();
		intersections = new ArrayList<Intersection>();
		terrain = new BasicTerrain(this);
	}
	
	/**
	 * Finds the range of the network
	 * @return an array {minX, minY, maxX, maxY}
	 */
	public float [] getXYBoundingBox(){
		float minX=Float.MAX_VALUE;
		float minY=Float.MAX_VALUE;
		float maxX=Float.MIN_VALUE;
		float maxY=Float.MIN_VALUE;
		
		for(Road road : roads){
			float [] range = road.getBoundingBox();
			
			minX = (range[0]<minX)?range[0]:minX;
			minY = (range[1]<minY)?range[1]:minY;
			maxX = (range[2]>maxX)?range[2]:maxX;
			maxY = (range[3]>maxY)?range[3]:maxY;
		}
		
		for(Intersection intersection : intersections){
			float [] range = intersection.getBoundingBox();
			
			minX = (range[0]<minX)?range[0]:minX;
			minY = (range[1]<minY)?range[1]:minY;
			maxX = (range[2]>maxX)?range[2]:maxX;
			maxY = (range[3]>maxY)?range[3]:maxY;
		}

		return new float[]{minX, minY, maxX, maxY};
	}
	
	public Iterator<Road> getRoadSegments(){
		return roads.iterator();
	}
	
	public Iterator<Intersection> getIntersections(){
		return intersections.iterator();
	}
	
	/**
	 * Returns which road that has not be completed. This road
	 * should be the the current road being edited.
	 * <p>
	 * @see com.sim.network#findUnclosedRoad()
	 * 
	 * @return
	 */
	public Road findUnclosedRoad(){
		Road road = null;
		for(Road r: roads){
			if(!r.isClosed()){
				road = r;
			}
		}
		return road;
	}
	
	/**
	 * Exports the network 
	 * @param modelPath directory path where to export
	 * @throws FileNotFoundException
	 */
	public void export(String modelPath) 
			throws FileNotFoundException{
		exporter.export(modelPath);
	}

	/**
	 * Clears all roads, intersections, and terrain in the network.
	 */
	public void removeAll() {
		roads.clear();
		intersections.clear();
		terrain.clear();
	}
	
	public void removeRoad(Road road){
		roads.remove(road);
	}
	
	public void removeIntersection(Intersection intersection){
		intersections.remove(intersection);
	}
	
	public void addRoad(Road road){
		roads.add(road);
	}
	
	public void addIntersection(Intersection intersection){
		intersections.add(intersection);
	}

	public void addRoads(ArrayList<Road> road){
		roads.addAll(road);
	}
	
	public void addIntersections(ArrayList<BasicIntersection> intersections) {
		if(intersections!=null)	
			this.intersections.addAll(intersections);		
	}
	
	/**
	 * Updates the terrain, responding to the updates of roads and intersections
	 */
	public void updateTerrain(){
		terrain.update();
	}

	public Terrain getTerrain() {
		return terrain;
	}
}
