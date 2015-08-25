package com.sim.terrain.basic;

import java.util.ArrayList;
import java.util.Collections;

import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.intersections.basic.BasicLeg;
import com.sim.roads.basic.BasicRoad;


/**
 * This class defines a the outer path of a {@link com.sim.network.RoadNetwork}. 
 * It is made of surrounded by a sequence {@link com.sim.roads.basic.BasicRoad} and 
 * {@link com.sim.intersections.basic.BasicIntersection}.
 * <p>
 * Now the only way to create a BasicOuterPath is to create a "dummy" path,
 * then add BasicRoad objects and BasicIntersection objects to it. After the addition is done,
 * call findPerimeter to calculate for the perimeter which is a sequence of points,
 * which outlines the hole.
 * <p> 
 * Two variables {@link #intersections} and {@link #roads} are synchronous in that 
 * the ith intersection is connected to the ith and (i+1)th roads.
 * <p>
 * Within a road network (See {@link com.sim.network.RoadNetwork}, there may be multiple
 * sub-network, each two of which are not attached. 
 * 
 * @author Dahai Guo
 *
 */
class BasicOuterPath {
	/**
	 * The roads that are on the outer path.
	 */
	private ArrayList<BasicRoad> roads;

	/**
	 * The intersections that one the outer path
	 */
	private ArrayList<BasicIntersection> intersections;

	/**
	 * The sequence of points that outline the perimeter of the outer path.<p>
	 * All the points are already on {@link #roads} or {@link #intersections}
	 */
	private ArrayList<RoadVector> perimeter;
	
	/**
	 * Default constructor, allocating space for the instance variables
	 */
	protected BasicOuterPath(){
		roads = new ArrayList<BasicRoad>();
		intersections = new ArrayList<BasicIntersection>();
		perimeter = null;
	}
	
	/**
	 * Returns the sequence of points which are the outer perimeter
	 * of the road subnetwork.
	 * @return
	 */
	protected ArrayList<RoadVector> getPerimeter(){
		if(perimeter==null){
			findPerimeter();
		}
		return perimeter;
	}
	
	/**
	 * Finds the sequence of points which are the outer perimeter
	 * of the road subnetwork.
	 */
	private void findPerimeter(){
		perimeter = new ArrayList<RoadVector>();
		for(int i=0;i<roads.size();i++){
			BasicRoad road = roads.get(i);
			BasicIntersection intersection = intersections.get(i);
			BasicLeg leg = intersection.getLeg(road);
			
			ArrayList<RoadVector> left = leg.getLeftBorder();
			ArrayList<RoadVector> right = leg.getRightBorder();

			// only adds the left border when the road is terminal, 
			// being adjacent to only one intersection
			if(road.getNumOfIntersections()!=2){
				Collections.reverse(left);
				perimeter.addAll(left);
			}
			perimeter.addAll(right);
		}
		
		Collections.reverse(perimeter);
	}
	
	protected void addRoad(BasicRoad road) {
		roads.add(road);
	}
	
	protected void addIntersection(BasicIntersection intersection){
		intersections.add(intersection);
	}
/*
	public ArrayList<BasicRoad> getRoads() {
		return roads;
	}

	public ArrayList<BasicIntersection> getIntersections(){
		return intersections;
	}
*/	
}
