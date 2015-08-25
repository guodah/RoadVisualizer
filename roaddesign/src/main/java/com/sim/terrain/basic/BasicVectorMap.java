package com.sim.terrain.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.sim.debug.DebugView;
import com.sim.debug.components.DebugMesh;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.intersections.Intersection;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.intersections.basic.BasicLeg;
import com.sim.intersections.basic.BasicLegType;
import com.sim.network.RoadNetwork;
import com.sim.roads.Road;
import com.sim.roads.basic.BasicRoad;


/**
 * This class finds the terrain given a network of 
 * {@link com.sim.roads.basic.BasicRoad} and 
 * {@link com.sim.intersections.basic.BasicIntersection}.<p>
 * 
 * Each BasicVectorMap consists of three types of things:
 * <ol>
 * <li> outer loops of the road sub-networks, each two of which are not attached
 * <li> inner holes which are surrounded by BasicRoads and BasicIntersections
 * <li> floating BasicRoads which are not connected to other roads
 * </ol>
 * @author Dahai Guo
 *
 */
class BasicVectorMap {
	/**
	 * Which network this object is calculated for
	 */
	private RoadNetwork network;
	
	/**
	 * Each separate sub-network is outlined by an outer loop
	 */
	private ArrayList<BasicOuterPath> outerLoops;
	
	/**
	 * Each hole is surround by a closed loop of roads and intersections
	 */
	private ArrayList<BasicHole> holes;
	
	/**
	 * Each one of {@link #outerLoops} is found a sequence of points which
	 * make a closed border of the outer path
	 */
	private ArrayList<ArrayList<RoadVector>> outerPerimeters;

	/**
	 * Each one of {@link #holes} is found a sequence of points which
	 * make a closed border of the outer path
	 */
	private ArrayList<ArrayList<RoadVector>> innerPerimeters;

	/**
	 * Roads that are not adjacent to any intersection
	 */
	private HashSet<BasicRoad> isolatedRoads;
	
	
	/**
	 * Default constructor only for allocating spaces of variables
	 * @param network
	 */
	private BasicVectorMap(RoadNetwork network) {
		this.network = network;
		outerLoops = null;
		holes = null;
	}

	/**
	 * First finds the terminal/tangling roads. For for each tangling road, finds the 
	 * outer perimeter (loop) of the subnetwork where the tangling road belongs to.
	 * (A subnetwork does not overlap with another subnetwork.) The process of finding
	 * outer loops will remove all the used terminal roads, except the ones in holes.
	 * Then it will find inner holes and isolated roads.
	 * <p>
	 * At last, it will find the perimeters of all outer paths, inner holes and 
	 * isolated roads.
	 * 
	 * @param network
	 * @return
	 */
	protected static BasicVectorMap findVectorMap(RoadNetwork network){
		BasicVectorMap map = new BasicVectorMap(network);
		
		// find terminal or tangling roads
		HashSet<BasicRoad> terminal = findTerminalRoads(network);
	
		map.outerLoops = findOuterLoops(terminal);
		map.holes = findInnerHoles(network, terminal);
		map.isolatedRoads = findIsolatedRoads(network);
		
		findOuterPerimeters(map);
		findInnerPerimeters(map);
		return map;
	}
	
	/**
	 * For each terminal road, finds the outer perimeter (loop) of the subnetwork
	 * where terminal road belongs to.
	 * 
	 * @param terminal the set of terminal roads
	 * @return a collection of BasicOuterPaths, each of which is for a subnetwork.
	 */
	private static ArrayList<BasicOuterPath> findOuterLoops(
			HashSet<BasicRoad> terminal) {
		
		if(terminal==null || terminal.size()==0){
			return null;
		}
		ArrayList<BasicOuterPath> outerLoops = new ArrayList<BasicOuterPath>();
		BasicOuterPath path = findOuterPath(terminal);
		while(path!=null){
			outerLoops.add(path);
			path = findOuterPath(terminal);
		}
		return outerLoops;
	}

	/**
	 * Finds perimeters of inner holes.
	 * 
	 * @param map
	 */
	private static void findInnerPerimeters(BasicVectorMap map) {
		if(map.holes==null){
			map.innerPerimeters = null;
			return;
		}
		map.innerPerimeters = new ArrayList<ArrayList<RoadVector>>();
		
		if(map.holes!=null){
			for(BasicHole hole : map.holes){
				map.innerPerimeters.add(hole.getPerimeter());
			}
		}
	}

	/**
	 * Finds perimeters of subnetworks
	 * @param map
	 */
	private static void findOuterPerimeters(BasicVectorMap map) {
		if(map.outerLoops==null && map.isolatedRoads==null){
			return;
		}
		map.outerPerimeters = new ArrayList<ArrayList<RoadVector>>();
		ArrayList<BasicOuterPath> paths = map.outerLoops;
		if(paths!=null){
			for(BasicOuterPath path : paths){
				map.outerPerimeters.add(path.getPerimeter());
			}
		}
		
		for(BasicRoad road : map.isolatedRoads){
			if(road.getPerimeter()!=null)
				map.outerPerimeters.add(road.getPerimeter());
		}
	}
	
	/**
	 * Finds roads in the network that does not intersect other roads.
	 * @param network
	 * @return BasicRoads which does not intersect other BasicRoads
	 */
	private static HashSet<BasicRoad> findIsolatedRoads(RoadNetwork network){
		return findRoadWithIntersections(network, 0);
	}
	
	/**
	 * Finds terminal roads which only intersect another BasicRoad at one end 
	 * @param network
	 * @return BasicRoads which only intersect another BasicRoad at one end 
	 */
	public static HashSet<BasicRoad> findTerminalRoads(RoadNetwork network){
		return findRoadWithIntersections(network, 1);
	}
	
	/**
	 * Within the road network, finds the roads which have a specific number
	 * of intersections.
	 * <p>
	 * Note it only works for BasicRoads.
	 * 
	 * @param network
	 * @param num number of intersections
	 * @return see the description of this method
	 */
	private static HashSet<BasicRoad> findRoadWithIntersections(
			RoadNetwork network, int num){
		Iterator<Road> roads = network.getRoadSegments();
		HashSet<BasicRoad> result = new HashSet<BasicRoad>();
		while(roads.hasNext()){
			Road _r = roads.next();
			if(!(_r instanceof BasicRoad)){
				continue;
			}
			BasicRoad r = (BasicRoad)_r;
			if(r.getNumOfIntersections()==num){
				result.add(r);
			}
		}
		return result;
	}
	
	/**
	 * Selects a terminal and finds its outer path which consists of
	 * BasicRoads and BasicIntersections alternately.
	 * 
	 * @param terminal
	 * @return
	 */
	private static BasicOuterPath findOuterPath(HashSet<BasicRoad> terminal){
		
		if(terminal==null || terminal.size()==0){
			return null;
		}
		
		// finds terminal road that is left-most
		// this step is to avoid selecting a terminal road within 
		// a hole
		float minX = Float.MAX_VALUE;
		BasicRoad road=null;
		for(BasicRoad _road : terminal){
			ArrayList<RoadVector> pts = _road.getCurvePts();
			RoadVector v1 = pts.get(0);
			RoadVector v2 = pts.get(pts.size()-1);
			RoadVector v = (v1.getX()<v2.getX())?v1:v2;
			if(v.getX()<minX){
				minX = v.getX();
				road = _road;
			}
		}
		
		// double-check if the road is terminal
		if(road.getNumOfIntersections()!=1){
			return null;
		}
		
		BasicRoad begin = road;
		BasicIntersection intersection = null;
		BasicOuterPath path = new BasicOuterPath();

		// always turn right to walk along the road network
		do{
			path.addRoad(road);
			if(road.getNumOfIntersections()==1){
				terminal.remove(road); // remove a terminal if it is already used in
										// finding the outer loop
			}
			
			BasicIntersection temp1 = (BasicIntersection) road.getIntersection(
					BasicLegType.UP_STREAM);
			BasicIntersection temp2 = (BasicIntersection) road.getIntersection(
					BasicLegType.DOWN_STREAM);

			// find the next intersection
			if(road.getNumOfIntersections()==1){
				intersection=(temp1==null)?temp2:temp1;
			}else{
				intersection=(temp1==intersection)?temp2:temp1;
			}
			
			path.addIntersection(intersection);
			
			// find the following road
			road = findNextRoad(intersection, road);
		}while(road!=begin); // stop when the begin road is met again

		return path;
	}
	
	/**
	 * Finds the next road in the counter-clockwise direction.
	 * 
	 * @param intersection
	 * @param road current road
	 * @return
	 */
	private static BasicRoad findNextRoad(BasicIntersection intersection, BasicRoad road) {
		ArrayList<BasicLeg> legs = intersection.getLegs();
		int count = 0;
		for(BasicLeg leg : legs){
			if(leg.getRoad()==road){
				break;
			}
			count++;
		}
		
		return legs.get((count+1)%legs.size()).getRoad();
	}

	/**
	 * Finds the previous road in the counter-clockwise direction.
	 * 
	 * @param intersection
	 * @param road current road
	 * @return
	 */
	private static BasicRoad findPreviousRoad(BasicIntersection intersection, BasicRoad road) {
		ArrayList<BasicLeg> legs = intersection.getLegs();
		int count = 0;
		for(BasicLeg leg : legs){
			if(leg.getRoad()==road){
				break;
			}
			count++;
		}
		count += legs.size()-1;
		return legs.get(count%legs.size()).getRoad();
	}

	/**
	 * Finds all the holes in a network.
	 * <p>
	 * Each leg in each intersection may find a hole. It is possible that 
	 * two different legs find the same hole which needs to be checked. 
	 *  
	 * @param network
	 * @param terminal terminal roads
	 * @return the list of holes
	 */
	private static ArrayList<BasicHole> findInnerHoles(RoadNetwork network, 
			HashSet<BasicRoad> terminal){
		
		if(terminal==null){
			return null;
		}
		Iterator<Intersection> intersections = network.getIntersections();
		ArrayList<BasicHole> result = new ArrayList<BasicHole>();
		while(intersections.hasNext()){
 
			BasicIntersection intersection = 
					(BasicIntersection) intersections.next();
			for(int j=0;j<intersection.getLegs().size();j++){
				BasicLeg leg = intersection.getLegs().get(j);
				BasicHole hole = findHole(intersection, leg, terminal);
				
				if(hole!=null && !result.contains(hole)){
					hole.getPerimeter();
					result.add(hole);
				}
			}
		}
		
		
		return result;
	}

	/**
	 * Finds a hole given a leg in an intersection.
	 * 
	 * @param intersection
	 * @param leg
	 * @param terminal
	 * @return any hole found
	 */
	private static BasicHole findHole(BasicIntersection intersection,
			BasicLeg leg, HashSet<BasicRoad> terminal) {
		
		BasicRoad road = leg.getRoad();
		BasicLegType type = leg.getType(); // up_stream or down_stream
		BasicRoad begin = road;
		BasicHole hole = new BasicHole();
				
		do{
			hole.addIntersection(intersection);
			hole.addRoad(road);
			
			// get the other intersection, adjacent to road
			BasicIntersection temp = (BasicIntersection) 
					road.getIntersection(type.not());

			if(temp==null){
				// this is when road has only one intersection.
				// this is ok only if it is a terminal road.
				if(!terminal.contains(road)){
					return null;
				}
			}else{
				intersection = temp;
			}

			// finds the counter-clock-wise previous road 
			road = findPreviousRoad(intersection, road);
			
			// finds the type of the next road
			if(road.getIntersection(type)!=intersection){
				type = type.not();
			}
		}while(begin!=road); // stop when the begin road is met again
		return hole;
	}

	public ArrayList<ArrayList<RoadVector>> getInnerPerimeter(){
		return this.innerPerimeters;
	}

	public ArrayList<ArrayList<RoadVector>> getOuterPerimeter() {
		return outerPerimeters;
	}
}
