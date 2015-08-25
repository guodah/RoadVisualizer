package com.sim.intersections.basic;

import java.awt.Dimension;



import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sim.central.RoadDesign;
import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.IntersectionCalcError;
import com.sim.core.basic.ModelGenConsts;
import com.sim.curves.ThreeCenteredCurve;
import com.sim.curves.ThreeCenteredTurnConsts;
import com.sim.debug.DebugView;
import com.sim.debug.components.DebugPoint;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.gui.handler.BasicIntersectionGUIHandler;
import com.sim.intersections.Intersection;
import com.sim.io.exporters.basic.BasicIntersectionExporter;
import com.sim.network.RoadNetwork;
import com.sim.obj.CrossSection;
import com.sim.roads.Road;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.CompareUtil;
import com.sim.util.CopyUtil;
import com.sim.util.GraphicsUtil;
import com.sim.util.ModelGenUtil;


/**
 * This class implements a very basic type of intersections which can 
 * be characterized as follows:<p>
 * <ol>
 * <li> no left lane
 * <li> all approaching roads are basic roads with uniform cross sections
 * <li> approaching roads do not have arrows on pavement
 * <li> no pavement marker on the intersection pavement
 * <li> the turn curves are three-centered curves
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class BasicIntersection extends Intersection{
	

	/**
	 * When the intersection is being defined by two basic roads (road1 and road2),
	 * this variable indicates if road1 and road2 make a left turn.
	 * <p>
	 * Used only in construction of the intersection. Should not be disclosed to the
	 * user.
	 */
	private boolean isLeftTurn;
	
	/**
	 * Two element list. The first is the direction of the road1, the parameter of
	 * {@link com.sim.intersections.basic.BasicIntersectionBuilder#splitRoads(BasicIntersection, BasicRoad, BasicRoad, RoadNetwork)}
	 * {@link #findIntersection(ArrayList, ArrayList)} 
	 */
	private ArrayList<RoadVector> directions;
	
	/**
	 * Four element list. Each road end means where the road ends and intersection
	 * begins.   
	 */
	private ArrayList<RoadVector> roadEnds;

	/**
	 * For a road in the intersection, if its right turn angle is less than
	 * 90 degree. Its right turn curve starts later than its left turn curve.
	 * This will cause non-uniformity of cross section. This program adds 
	 * to the road an extension which is the extension of the right side, including
	 * the median. 
	 * <p>
	 * This variable is a four element list. An element can be null which means the turn
	 * is greater than or equal to 90 degree and no need to extension. Otherwise, 
	 * it means a location on the center line to which the road needs to be extended to.
	 * <p>
	 * The order of the elements is in counter clockwise order, starting from the one
	 * right to and below the center line intersection.
	 */
	private ArrayList<RoadVector> extensions;
	
	/**
	 * Each leg can be either up_stream or down_stream, depending on whether or
	 * the list of road's curve points are approaching or leaving the intersection.   
	 * <p>
	 * This is a four element list. The order of the elements is in counter 
	 * clockwise order, starting from the one, below the 
	 * center line intersection.
	 */
	private ArrayList<BasicLegType> legTypes;
	
	/**
	 * Four element list. The intersections of road with width. The order of 
	 * the intersections ends are counter clockwise, starting from the one,
	 * right to and below the intersection of center lines. 
	 */
	private ArrayList<RoadVector> inters;
	
	/**
	 * Four element list. The four legs of the intersection.
	 */
	protected ArrayList<BasicLeg> legs;
	
	/**
	 * Intersection of center lines.
	 */
	private RoadVector interCenterLines;

	/**
	 * Four element list. Each element is a turn curve. The order of 
	 * the turn curves are counter clockwise, starting from the one,
	 * right to and below the intersection of center lines. 
	 */
	private ArrayList<ArrayList<RoadVector>> curves;
	
	/**
	 * Every three points make a triangle in the mesh for the intersection area.
	 */
	private ArrayList<RoadVector> innerArea;
	
	/**
	 * Every three points make a triangle in the mesh for the shoulder area.
	 */
	private ArrayList<RoadVector> shoulderArea;
	
	/**
	 * The border of the inner area
	 */
	private ArrayList<RoadVector> innerBorder;
	
	/**
	 * Four elements, each of which is the border of a shoulder area.
	 */
	private ArrayList<ArrayList<RoadVector>> shoulderBorders;
	
	public boolean isLeftTurn(){
		return isLeftTurn;
	}

	/**
	 * Given an existing network and a new/extended road, finds the first intersection,
	 * if any.
	 * <p>
	 * The first parameter just grew another point which may cause 
	 * objects of {@link com.sim.intersections.basic.BasicIntersection} 
	 * in the road network
	 * 
	 * @param road1 
	 * @param network
	 * @return
	 */
	public static BasicIntersection findNearestIntersections(
		BasicRoad road1, RoadNetwork network){
		
		BasicIntersectionBuilder builder = (BasicIntersectionBuilder) 
				RoadDesign.getIntersectionBuilder(BasicIntersectionBuilder.class);
		
		// find all the roads in the existing network that intersect road1
		NavigableMap<Float, BasicRoad> interCandidates = builder.
				findInterCandidates(road1, network);
		
		if(interCandidates==null){
			return null; // no intersect at all
		}else if(interCandidates.size()==0){
			// road1 does intersect with the network, but there is no implementable
			// now in the basic package
			throw new IllegalStateException(
				"Intersections cannot be implemented.\n"+
				"Possible reasons could be 1) intersecting roads are"+
				"not co-planar, 2) the straight sections of the "+
				"intersecting roads are too short."
			);			
		}
		
		// get the previously last point in road1
		ArrayList<RoadVector> curve = road1.getCurvePts();
		RoadVector v1 = curve.get(curve.size()-2);
		
		// implement the nearest intersection.
		Float distance = interCandidates.firstKey();
		if(distance!=null){
			BasicRoad road2 = interCandidates.get(distance);
			BasicIntersection intersection = findIntersection(road1, road2, v1, distance);
			intersection.exporter = 
					BasicIntersectionExporter.getExporter(intersection);
			
			// the order of passing road1 and road2 must be consistent with the order
			// when calling findIntersection
			road1 = builder.splitRoads(intersection, road1, road2, network);
			
			builder.findInnerArea(intersection);
			builder.findShoulders(intersection);
			
//			intersection.findIntersectionModel();
			return intersection;
		}
		return null;
	}

	/**
	 * Used when a road of the intersection changes.
	 *  
	 * @param road1 the original road
	 * @param road2 the new road
	 */
	public void replace(BasicRoad road1, BasicRoad road2){
		for(int i=0;i<legs.size();i++){
			BasicLeg leg = legs.get(i);
			if(leg.getRoad()==road1){
				leg.setRoad(road2);
				return;
			}
		}
	}
	
	/**
	 * Finds the intersection between road1 and road2. 
	 * 
	 * @param road1 this road just grew another point
	 * @param road2 an existing road in the road network
	 * @param last road1's last point
	 * @param distance the estimate of the distance between last and 
	 * 		  where road1 and road2 intersect
	 * @return the intersection between road1 and road2, 
	 *  	   null no such intersection exists
	 */
	public static BasicIntersection findIntersection(BasicRoad road1,
			BasicRoad road2, RoadVector last, Float distance) {
		ArrayList<RoadVector> curve1 = road1.getCurvePts();
		ArrayList<RoadVector> curve2 = road2.getCurvePts();
		
		ArrayList<CrossSection> xsecs = new ArrayList<CrossSection>();
		xsecs.add(road1.getXsec());
		xsecs.add(road2.getXsec());
		
		ArrayList<RoadVector> pts = new ArrayList<RoadVector>(); 		
		RoadVector v1 = curve1.get(0);
		
		// for each pair of segments in curve1 and curve2
		// tries to find the intersection
		for(int i=0;i<curve1.size()-1;i++){
			RoadVector v2 = curve1.get(i+1);
			
			RoadVector _v1 = curve2.get(0);
			for(int j=0;j<curve2.size()-1;j++){
				RoadVector _v2 = curve2.get(j+1);	
				
				pts.clear();
				pts.add(v1); pts.add(v2);
				pts.add(_v1); pts.add(_v2);
				
				RoadVector _inter = RoadVector.intersect(v1, v2, _v1, _v2);
				
				if(_inter!=null && Math.abs(RoadVector.subtract(_inter, last).
						magnitude()-distance)<DesignConsts.DELTA){
					BasicIntersection inter = findIntersection(pts, xsecs);
					if(inter!=null){
						return inter;
					}	
				}
				
				_v1 = _v2;
			}
			
			v1 = v2;
		}
		return null;
	}

	/**
	 * Finds a BasicIntersection give four points and two cross sections.
	 * <p>
	 * The first two points form an approach and the other two form another. 
	 * The first two in approaches make the approach for road1 in the 
	 * parameter list of 
	 * {@link com.sim.intersections.basic.BasicIntersectionBuilder#splitRoads(BasicIntersection, BasicRoad, BasicRoad, RoadNetwork)}
	 * and 
	 * {@link #findIntersection(BasicRoad, BasicRoad, RoadVector, Float)}. 
	 * Then of course the last two make the approach for road2 in the same methods. The 
	 * cross sections (xsecs) are for the two approaches. 
	 * 
	 * @param approaches 
	 * @param xsecs
	 * @return 
	 */
	private static BasicIntersection findIntersection(
			ArrayList<RoadVector> approaches, 
			ArrayList<CrossSection> xsecs){

		BasicIntersectionBuilder builder = (BasicIntersectionBuilder) 
				RoadDesign.getIntersectionBuilder(BasicIntersectionBuilder.class);

		
		// validate the input to see if an interection can be made
		if(!builder.isValid(approaches, xsecs)){
			return null;
		}
		
		// calculates the directions
		BasicIntersection intersection = new BasicIntersection();
		intersection.directions.add(RoadVector.subtract(
			approaches.get(1), approaches.get(0)).unit2d()
		);
		intersection.directions.add(RoadVector.subtract(
			approaches.get(3), approaches.get(2)).unit2d()
		);
		
		intersection.isLeftTurn = Vector23f.isLeftTurn(
				intersection.directions.get(0).getVector(), 
				intersection.directions.get(1).getVector()
		);
		
		// finds the intersection of center lines
		intersection.interCenterLines=RoadVector.intersect(approaches.get(0), 
					approaches.get(1), approaches.get(2), approaches.get(3));

		// finds the basic point for the turn curves. they are the intersections
		// roads with width
		intersection.inters=builder.findAllInters(intersection.interCenterLines,
				approaches, xsecs);
		
		// finds all the turn pairs
		Vector23f turnPairs[][] = builder.findTurnPairs(approaches);
		
		// find curve points
		for(int i=0;i<4;i++){
			intersection.curves.add(
				ModelGenUtil.translate(builder.findTurnCurve(turnPairs[i]),
					intersection.inters.get(i).getVector()
				)
			);
		}
	
		// this step is necessary because the ends of turn curves may not project
		// on the center line with the same amount, causing inconvinience in the following
		// processing. this step extend the shorter curves so that all curves project
		// on the center line the same.
		intersection.patchTurns();
		return intersection;
	}

	/**
	 * A private constructor only for allocating space for instance variables.
	 */
	private BasicIntersection(){
		directions=new ArrayList<RoadVector>();
		curves=new ArrayList<ArrayList<RoadVector>>();
		legs = new ArrayList<BasicLeg>();
		
		roadEnds=new ArrayList<RoadVector>();
		extensions=new ArrayList<RoadVector>();
		
		legs = new ArrayList<BasicLeg>();
		legTypes=new ArrayList<BasicLegType>();
		guiHandler = new BasicIntersectionGUIHandler(this);
	}
	
	/**
	 * When the turn angle is not 90 degree. From a specific leg, the turn
	 * curves on both side will not project on the center line equally. This 
	 * makes hard to defines when the leg ends and intersection begins 
	 * along the center line. 
	 * <p>
	 * Therefore this program patch the shorter curve with a straight section
	 * so that both turn curves project on the center line equally. See roadEnds.
	 * <p>
	 * When the right turn curve is a less than 90 degree turn. Its turn curve
	 * starts later than the left one. The difference will cause non-uniformity 
	 * of road cross sections. This program uses extensions which add to the road
	 * another road on the right side. See extensions.
	 */
	private void patchTurns() {
		BasicIntersectionBuilder builder = (BasicIntersectionBuilder) 
				RoadDesign.getIntersectionBuilder(BasicIntersectionBuilder.class);

		ArrayList<RoadVector> dirs = builder.findAllDirs(directions);
	
		for(int i=0;i<4;i++){
//			int size1 = curves.get(i).size();
//			RoadVector p1 = curves.get(i).get(size1-1);
//			RoadVector p2 = curves.get((i+1)%4).get(0);
			RoadVector right = curves.get(i).get(0);
			int size1 = curves.get((i+3)%4).size();
			RoadVector left = curves.get((i+3)%4).get(size1-1);
			
			
			float leftProj = RoadVector.dot2d(dirs.get(i), 
							RoadVector.subtract(left, interCenterLines)); 
			float rightProj = RoadVector.dot2d(dirs.get(i), 
							RoadVector.subtract(right, interCenterLines));
			
			getRoadEnds().add(
				RoadVector.add(interCenterLines, 
					RoadVector.multi(dirs.get(i),
							(leftProj>rightProj)?leftProj:rightProj)
					)
			);
			
			extensions.add(
				(leftProj>rightProj)?
					RoadVector.add(interCenterLines, 
							RoadVector.multi(dirs.get(i), 
									rightProj))
					:null
			);
			
			if(i%2==1){
				// this is a leg that belongs to the second road
				if(isLeftTurn()){
					getLegTypes().add((i==1)?BasicLegType.UP_STREAM:BasicLegType.DOWN_STREAM);
				}else{
					getLegTypes().add((i==3)?BasicLegType.UP_STREAM:BasicLegType.DOWN_STREAM);
				}
			}else{
				getLegTypes().add((i==0)?BasicLegType.UP_STREAM:BasicLegType.DOWN_STREAM);
			}
		}
	}


	/**
	 * Returns the pointer to the leg which contains a specific road
	 * @param road
	 * @return leg that contains road
	 */
	public BasicLeg getLeg(BasicRoad road) {
		for(int i=0;i<legs.size();i++){
			BasicLeg leg = legs.get(i);
			if(leg.getRoad()==road){
				return leg;
			}
		}
		return null;
	}

	/**
	 * The return array is [minX, minY, maxX, maxY]
	 */
	@Override
	public float[] getBoundingBox() {
		float minX=Float.MAX_VALUE;
		float minY=Float.MAX_VALUE;
		float maxX=Float.MIN_VALUE;
		float maxY=Float.MIN_VALUE;

		for(RoadVector vec : getRoadEnds()){
			minX = (minX>vec.getX())?vec.getX():minX;
			minY = (minY>vec.getY())?vec.getY():minY;
			maxX = (maxX<vec.getX())?vec.getX():maxX;
			maxY = (maxY<vec.getY())?vec.getY():maxY;
		}
		
		return new float[]{minX, minY, maxX, maxY};
	}	

	public ArrayList<RoadVector> getInnerBorder() {
		return innerBorder;
	}

	public ArrayList<RoadVector> getInnerArea() {
		return innerArea;		
	}
	
	public ArrayList<RoadVector> getShoulderAreas() {
		return shoulderArea;		
	}

//	public RoadVector getInterCenterLines() {
//		return interCenterLines;
//	}

//	public void setInterCenterLines(RoadVector interCenterLines) {
//		this.interCenterLines = interCenterLines;
//	}

//	public ArrayList<RoadVector> getDirections() {
//		return directions;
//	}

//	public void setDirections(ArrayList<RoadVector> directions) {
//		this.directions = directions;
//	}

//	public ArrayList<RoadVector> getInters() {
//		return inters;
//	}

//	public void setInters(ArrayList<RoadVector> inters) {
//		this.inters = inters;
//	}

	protected ArrayList<ArrayList<RoadVector>> getCurves() {
		return curves;
	}

//	public void setCurves(ArrayList<ArrayList<RoadVector>> curves) {
//		this.curves = curves;
//	}

	public ArrayList<RoadVector> getExtensions() {
		return extensions;
	}

//	public void setExtensions(ArrayList<RoadVector> extensions) {
//		this.extensions = extensions;
//	}

	public ArrayList<BasicLegType> getLegTypes() {
		return legTypes;
	}

//	public void setLegTypes(ArrayList<BasicLegType> legTypes) {
//		this.legTypes = legTypes;
//	}

	public ArrayList<RoadVector> getRoadEnds() {
		return roadEnds;
	}

//	public void setRoadEnds(ArrayList<RoadVector> roadEnds) {
//		this.roadEnds = roadEnds;
//	}

	public ArrayList<RoadVector> getShoulderArea() {
		return shoulderArea;
	}

	public void setShoulderArea(ArrayList<RoadVector> shoulderArea) {
		this.shoulderArea = shoulderArea;
	}

	public void setShoulderBorders(
			ArrayList<ArrayList<RoadVector>> shoulderBorders) {
		this.shoulderBorders = shoulderBorders;
	}

	public void setInnerArea(ArrayList<RoadVector> innerArea) {
		this.innerArea = innerArea;
	}

	public void setInnerBorder(ArrayList<RoadVector> innerBorder) {
		this.innerBorder = innerBorder;
	}

	public ArrayList<BasicLeg> getLegs() {
		return legs;
	}
}
