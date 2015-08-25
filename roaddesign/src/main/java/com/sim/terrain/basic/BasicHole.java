package com.sim.terrain.basic;

import java.util.ArrayList;
import java.util.Collections;

import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.intersections.basic.BasicLeg;
import com.sim.roads.basic.BasicRoad;


/**
 * This class defines a hole surrounded by {@link com.sim.roads.basic.BasicRoad} and 
 * {@link com.sim.intersections.basic.BasicIntersection}.
 * <p>
 * Now the only way to create a BasicHole is to create a "dummy" hole,
 * then add BasicRoad objects and BasicIntersection objects to it. After the addition is done,
 * call findPerimeter to calculate for the perimeter which is a sequence of points,
 * which outlines the hole.
 * <p> 
 * Two variables {@link #intersections} and {@link #roads} are synchronous in that 
 * the ith intersection is connected to the ith and (i+1)th roads. 
 * 
 * @author Dahai Guo
 *
 */
class BasicHole {
	/**
	 * The roads that are adjacent to this hole.
	 */
	private ArrayList<BasicRoad> roads;
	
	/**
	 * The intersections are adjacent to this hole.
	 */
	private ArrayList<BasicIntersection> intersections;
	
	/**
	 * The sequence of points that outline the perimeter of the hole.<p>
	 * All the points are already on {@link #roads} or {@link #intersections}
	 */
	private ArrayList<RoadVector> perimeter;
	
	/**
	 * Default constructor that only allocates spaces for variables.
	 */
	public BasicHole(){
		roads = new ArrayList<BasicRoad>();
		intersections = new ArrayList<BasicIntersection>();
	}
	
	protected void addRoad(BasicRoad road){
		roads.add(road);
	}
	
	protected void addIntersection(BasicIntersection intersection){
		intersections.add(intersection);
	}
	
	/**
	 * Sees if two BaiscHoles are the same when they are
	 * surrounded by same BasicRoads and BasicIntersections
	 */
	public boolean equals(Object obj){
		if(!(obj instanceof BasicHole)){
			return false;
		}
		BasicHole hole = (BasicHole)obj;
		return hole.roads.containsAll(roads) &&
				roads.containsAll(hole.roads) &&
				hole.intersections.containsAll(intersections) &&
				intersections.containsAll(hole.intersections);
	}

	/**
	 * Calculates the perimeter
	 */
	private void findPerimeter() {
		perimeter = new ArrayList<RoadVector>();
		
		for(int i=0;i<roads.size();i++){
			BasicRoad road = roads.get(i);
			BasicIntersection intersection = intersections.get(i);
			BasicLeg leg = intersection.getLeg(road);
			
			ArrayList<RoadVector> right = leg.getRightBorder();
			Collections.reverse(right);

			perimeter.addAll(right);
			
			/*
			 * This if statement checks to see if there is a road, which is 
			 * tangling within the hole. 
			 */
			if(road.getNumOfIntersections()==1){
				ArrayList<RoadVector> left = leg.getLeftBorder();
				perimeter.addAll(left);
			}
		}
	}
	
	/**
	 * Returns the perimeter.
	 * <p>
	 * Only valid when called after findPerimeter
	 * @return
	 */
	protected ArrayList<RoadVector> getPerimeter(){
		if(perimeter==null){
			findPerimeter();
		}
		return perimeter;
	}
}
