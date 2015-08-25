package com.sim.roads.basic;

import java.awt.Container;



import java.awt.Graphics;

import java.util.*;

import javax.swing.JOptionPane;

import com.sim.central.RoadDesign;
import com.sim.core.basic.IntersectionCalcError;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.curves.CurveParam;
import com.sim.curves.HorizontalCurve;
import com.sim.curves.VerticalCurve;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.gui.handler.BasicRoadGUIHandler;
import com.sim.intersections.*;
import com.sim.intersections.basic.BasicLegType;
import com.sim.io.exporters.basic.BasicRoadExporter;
import com.sim.network.RoadNetwork;
import com.sim.obj.CrossSection;
import com.sim.roads.Road;
import com.sim.util.CopyUtil;

import com.sim.util.GraphicsUtil;
import com.sim.util.SearchUtil;



/**
 * A BasicRoad has uniform cross section. It is created by user's mouse
 * clicks. The first mouse click just starts the road and following ones will
 * in addition asks the user for grade, speed and super elevation at the point.
 * The last triple of (grade, speed, super elevation) will only be used the road
 * grows again. 
 * 
 * @author Dahai Guo
 *
 */
public class BasicRoad extends Road {
	
	/**
	 * Responsible for calculating the geometries of the road.
	 */
	private BasicRoadBuilder builder;
	
	/**
	 * Input by the user. The length of this
	 * list should be one more than grades, speeds, and superEles.
	 * <p>
	 * The user will not be asked to input grade, speed, and superEles
	 * at the first point.
	 */
	private ArrayList<RoadVector> keyPoints;
	
	/**
	 * Grades at keyPoints 
	 */
	private ArrayList<Float> grades;
	
	/**
	 * Speed limits at keyPoints
	 */
	private ArrayList<Float> speeds;
	
	/**
	 * superEles at keyPoints.
	 */
	private ArrayList<Float> superEles;
	
	/**
	 * The cross sectional profile of this road.
	 */
	private CrossSection xsec;

	/**
	 * For each keyPoints, this variable records the cross sections at each center line.
	 */
	protected NavigableMap<Integer, ArrayList<ArrayList<RoadVector>>> xsecs;

	/**
	 * For each keyPoints, curve records the center line points 
	 * at each center line.
	 */
	protected NavigableMap<Integer, ArrayList<RoadVector>> curve;
	
	/**
	 * All the points in instance variable curve.
	 */
	protected ArrayList<RoadVector> curvePts;
	
	/**
	 * All the cross section in instance variable {@link #xsecs}
	 */
	protected ArrayList<ArrayList<RoadVector>> xsecPts;
	
	/**
	 * Possibly a road is adjacent to two intersections, to which this road
	 * is upstream or downstream.
	 */
	protected Map<BasicLegType, Intersection> intersections;
	
	/**
	 * Once a {@link com.sim.roads.basic.BasicRoad} is closed, no input point will be accepted.
	 */
	private boolean closed;

	/**
	 * Disable the road from editting.
	 * @see #closed
	 */
	public void close(){
		if(closed){
			return;
		}
		closed = true;
	}
	
	/**
	 * Only allocates space for a {@link com.sim.roads.basic.BasicRoad}
	 * @param xsec the cross sectional profile of the road. See 
	 * 				{@link #xsec}
	 * @return
	 */
	private static BasicRoad newUniRoad(CrossSection xsec){
		BasicRoad road = new BasicRoad();
		road.keyPoints = new ArrayList<RoadVector>();
		road.curve = new TreeMap<Integer, ArrayList<RoadVector>>();
		road.xsecs = new TreeMap<Integer, ArrayList<ArrayList<RoadVector>>>();
		road.grades = new ArrayList<Float>();
		road.speeds = new ArrayList<Float>();
		road.superEles = new ArrayList<Float>();
		road.intersections = new TreeMap<BasicLegType, Intersection>();
		road.xsec = xsec;
		road.closed = false;
		
		road.guiHandler = new BasicRoadGUIHandler(road);
		road.exporter = BasicRoadExporter.getExporter(road);
		
		road.builder = (BasicRoadBuilder) 
				RoadDesign.getRoadBuilder(BasicRoadBuilder.class);
		
		return road;
	}
	
	/**
	 * Creates a new BasicRoad
	 * 
	 * @param v the first point to creates the road
	 * @param network to which the new road will be added to.
	 * @return
	 */
	public static BasicRoad newUniRoad(RoadVector v, RoadNetwork network){
		
		// ask the user for a cross section
		CrossSection xsec = RoadDesign.getXsec(v.getVector());
		if(xsec==null){
			return null;
		}
		
		// creates the road using xsec
		BasicRoad road = newUniRoad(xsec);
		
		road.increment(v);
		return road;
	}
/*	
	public static BasicRoad findClosedUniRoad(ArrayList<RoadVector> inputPoints,
			ArrayList<Float> grades, ArrayList<Float> speeds,
			ArrayList<Float> superEles, CrossSection xsec){
		BasicRoad road = new BasicRoad();
		road.keyPoints = inputPoints;
		road.grades = grades;
		road.speeds = speeds;
		road.superEles = superEles;
		road.xsec = xsec;
		road.closed = true;
		road.calculate();
		return road;
	}
	
	private void findElevation(){
		RoadKeyCalc.findElevation(keyPoints, grades);
	}
*/
	/**
	 * Calculates the curve and cross sections of the road, described
	 * as follows:
	 * <p>
	 * <ol>
	 * <li> when there is only one keyPoint, the road just contain that point.
	 * <li> when there are more keyPoints, it calls 
	 *      RoadKeyCalc.findKeyCenterLinePoints to figure out center line points,
	 *      taking into account horizontal and/or vertical curves; then it
	 *      finds the cross sectional profile at each center line point; at last
	 *      it adds super elevation information to the cross sectional profiles.
	 * </ol>
	 */
	private void calculate(){
		if(keyPoints.size()==1){
			ArrayList<RoadVector> segment = new ArrayList<RoadVector>();
			
			curve.clear();
			xsecs.clear();
			
			segment.add(keyPoints.get(0));
			curve.put(0, segment);
			findCurvePts();
			findXsecPts();
		}else{
			/*
			curve = RoadShapeCalc.findCenterLinePoints(
					keyPoints, grades, speeds, superEles, xsec);
			
			xsecs = xsec.findCrossSections(curve);
			
			findCurvePts();
			findXsecPts();
			
			RoadShapeCalc.addSuperElevations(
					getCurvePts(), getXsecPts(), xsec);
			*/
			curve = builder.findCenterLinePoints(
					keyPoints, grades, speeds, superEles, xsec);
			xsecs = builder.findCrossSections(curve, xsec);
			curvePts = builder.findCurvePts(curve);
			xsecPts = builder.findXsecPts(xsecs);
			builder.addSuperElevations(curvePts, xsecPts, xsec);
		}
	}

	public boolean isClosed(){
		return closed;
	}

	/**
	 * Gets the cross sectional profile of the road
	 * @return {@link #xsec}
	 */
	public CrossSection getXsec() {
		return xsec;
	}

	/**
	 * Removes the last added key point and re-calculates the road.
	 */
	@Override
	public void decrement() {
		if(keyPoints.size()<=1){
			reset();
			return;
		}else{
			keyPoints.remove(keyPoints.size()-1);
			grades.remove(grades.size()-1);
			speeds.remove(speeds.size()-1);
			superEles.remove(superEles.size()-1);

			calculate();
		}
	}

	/**
	 * Clears all the data in the road.
	 */
	private void reset() {
		keyPoints.clear();
		grades.clear();
		speeds.clear();
		superEles.clear();
		xsecs.clear();
		curve.clear();
		curvePts.clear();
		xsecPts.clear();
	}

	/**
	 * Grows the road given another key point. <p>
	 * 
	 * @param v the new key point
	 * @return true if increment succeeded, false otherwise
	 */
	@Override
	public boolean increment(RoadVector v) {
		if(keyPoints.size()==0){
			keyPoints.add(v);
			return true;
		}else{
			
			// reads the grade, speed, and super elevation from the user
			Map<CurveParam, Float> params = RoadDesign.getCurveParam(v.getVector());
			if(params==null){
				return false;
			}else{
				keyPoints.add(v);
				this.speeds.add(params.get(CurveParam.SPEED));
				this.grades.add(params.get(CurveParam.GRADE));
				this.superEles.add(params.get(CurveParam.SUPERELEVATION));
				
				// re-calculates the road
				calculate();
				return true;
			}
			
		}
	}

	/**
	 * Returns where the road can be grown.
	 * @return extension point of the road if it exists; null otherwise.
	 */
	@Override
	public RoadVector getOpenPt() {
		if(closed){
			return null;
		}
		
		if(keyPoints.size()==0){
			return null;
		}else if(keyPoints.size()==1){
			return keyPoints.get(0);
		}else{
			// when the road has more than two key points, returns
			// the last center line point.
			ArrayList<RoadVector> segment = curve.lastEntry().getValue(); 
			return segment.get(segment.size()-1);
		}
	}
	
	/**
	 * Returns all the cross sections at center line points
	 * @return {@link #xsecs}
	 */
	public ArrayList<ArrayList<RoadVector>> getXsecPts(){
		return xsecPts;
	}
	
	/**
	 * Returns a list of all the center line points
	 * @return
	 */
	public ArrayList<RoadVector> getCurvePts(){
		return curvePts;
	}
	
	/**
	 * Puts the cross sections in {@link #xsecs} in {@link #xsecPts}
	 */
	private void findXsecPts(){
		
		if(xsecs==null || xsecs.size()==0){
			xsecPts = null;
			return;
		}
		
		if(xsecPts==null){
			xsecPts = new ArrayList<ArrayList<RoadVector>>();
		}else{
			xsecPts.clear();
		}
		
		Integer key = (xsecs.size()==0)?null:xsecs.firstKey();
		while(key!=null){
			xsecPts.addAll(xsecs.get(key));
			key = xsecs.higherKey(key);
		}
	}
	
	/**
	 * Puts the center line points in {@link #curve} in {@link #curvePts}
	 */	
	private void findCurvePts(){
		
		if(curve==null || curve.size()==0){
			curvePts = null;
			return;
		}
		
		if(curvePts==null){
			curvePts = new ArrayList<RoadVector>();
		}else{
			curvePts.clear();
		}
		
		Integer key = (curve.size()==0)?null:curve.firstKey();
		while(key!=null){
			curvePts.addAll(curve.get(key));
			key = curve.higherKey(key);
		}
	}

	public boolean hasShoulder() {
		return Float.compare(xsec.shoulderWidth,0)!=0;
	}
	
	public boolean hasMedian(){
		return Float.compare(xsec.medianWidth,0)!=0;
	}

	/**
	 * Returns the cross section at the first center line point
	 * @return the first cross section if it exists, null otherwise 
	 */
	public ArrayList<RoadVector> getFirstXsec(){
		if(xsecPts!=null)
			return xsecPts.get(0);
		else
			return null;
	}
	
	/**
	 * Returns the cross section at the last center line point
	 * @return the last cross section if it exists, null otherwise 
	 */	
	public ArrayList<RoadVector> getLastXsec(){
		if(xsecPts!=null)
			return xsecPts.get(xsecPts.size()-1);
		else
			return null;
	}

	/**
	 * Cuts the road at a certain point which must be on the existing center line.
	 * @param cutter where the road will be cut
	 * @param legType the type of the leg that contains the road
	 * @return the cutt-off road if the operation succeeds; null otherwise
	 */
	public BasicRoad cut(RoadVector cutter, BasicLegType legType) {
		
		// finds where the road will be cut
		int cutIndex=SearchUtil.findInCurve(keyPoints, cutter);
		if((cutIndex) < 0)
			return null;
			
		// creates the new road
		BasicRoad road = BasicRoad.newUniRoad(this.xsec);
		
		// copies the key points, grades, speeds, supereles to the new road
		int start=0, end=0;
		start = (legType==BasicLegType.UP_STREAM)?0:cutIndex+1;
		end = (legType==BasicLegType.UP_STREAM)? cutIndex : keyPoints.size()-1; 
		
		road.keyPoints = CopyUtil.copy(keyPoints, start, end);
		road.grades = CopyUtil.copy(grades, 
				(legType==BasicLegType.UP_STREAM)?0:start-1, end-1);
		road.speeds = CopyUtil.copy(speeds, 
				(legType==BasicLegType.UP_STREAM)?0:start-1, end-1);
		road.superEles = CopyUtil.copy(superEles, 
				(legType==BasicLegType.UP_STREAM)?0:start-1, end-1);

		// adds the cutter which is likely not to have existed in the road's
		// center line
		if(legType==BasicLegType.UP_STREAM){
			road.keyPoints.add(cutter);
			// finds the grade, speed, and superEles at the cutter
			if(road.grades.size()!=0){
				CopyUtil.duplicateTail(road.grades);
				CopyUtil.duplicateTail(road.speeds);
				CopyUtil.duplicateTail(road.superEles);
			}else{
				road.grades.add(grades.get(0));
				road.speeds.add(speeds.get(0));
				road.superEles.add(superEles.get(0));
			}
		}else{
			road.keyPoints.add(0,cutter);
		}
		
		road.calculate();
		return road;
	}
	
	/**
	 * Adds an intersection given a type. <p>
	 * 
	 * @param type downstream or upstream
	 * @param intersection 
	 */
	public void addIntersection(BasicLegType type, Intersection intersection){
		intersections.put(type, intersection);
	}
	
	/**
	 * Returns the intersection of a certain type
	 * @param type downstream or upstream
	 * @return the corresponding intersection if it exists; null otherwise.
	 */
	public Intersection getIntersection(BasicLegType type){
		return intersections.get(type);
	}

	/**
	 * Finds and returns how many intersections this road is adjacent to.
	 * @return
	 */
	public int getNumOfIntersections() {
		Set<BasicLegType> keys = intersections.keySet();
		int count = 0;
		for(BasicLegType type : keys){
			if(intersections.get(type)!=null){
				count++;
			}
		}
		return count;
	}

	@Override
	public float[] getBoundingBox() {
		float minX=Float.MAX_VALUE;
		float minY=Float.MAX_VALUE;
		float maxX=Float.MIN_VALUE;
		float maxY=Float.MIN_VALUE;

		if(curvePts!=null){
			for(RoadVector vec : curvePts){
				minX = (minX>vec.getX())?vec.getX():minX;
				minY = (minY>vec.getY())?vec.getY():minY;
				maxX = (maxX<vec.getX())?vec.getX():maxX;
				maxY = (maxY<vec.getY())?vec.getY():maxY;
			}
		}
		return new float[]{minX, minY, maxX, maxY};
	}

	/**
	 * Finds the perimeter of the road which has the road area to the right.
	 * @return
	 */
	public ArrayList<RoadVector> getPerimeter() {
			
		if(xsecPts==null){
			return null;
		}
		
		ArrayList<RoadVector> perimeter = new ArrayList<RoadVector>();
		
		int xsecWidth = xsecPts.get(0).size();
		int roadLen = xsecPts.size();
		
		for(int i=0;i<xsecPts.size();i++){
			perimeter.add(xsecPts.get(i).get(0));
		}
		
		for(int i=1;i<xsecWidth-1;i++){
			perimeter.add(xsecPts.get(roadLen-1).get(i));
		}
		
		for(int i=roadLen-1;i>=0;i--){
			perimeter.add(xsecPts.get(i).get(xsecWidth-1));
		}

		for(int i=xsecWidth-2;i>=1;i--){
			perimeter.add(xsecPts.get(0).get(i));
		}
		return perimeter;
	}

}

