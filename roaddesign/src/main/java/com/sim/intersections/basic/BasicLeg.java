package com.sim.intersections.basic;

import java.util.ArrayList;
import java.util.Collections;

import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.geometries.RoadVector;
import com.sim.obj.CrossSection;
import com.sim.roads.basic.BasicRoad;


/**
 * This class defines a leg of a {@link com.sim.intersections.basic.BasicIntersection}.
 * <p>
 * When the intersection is not exactly 90-degree. It is possible that
 * the road of the leg needs to be extended on the side in which drivers
 * enter the intersection.
 * <p>
 * In addition, drivers will pass a white marker before entering the intersection. 
 * 
 * @author Dahai Guo
 *
 */
public class BasicLeg {
	
	/**
	 * The approaching road in this leg.
	 */
	private BasicRoad road;
	
	/**
	 * The extension of center line (could be null)
	 */
	private RoadVector centerLineEx;
	
	/**
	 * up_stream or down_stream
	 */
	private BasicLegType type;
	
	/**
	 * Cross sections of extensions if any.
	 */
	private ArrayList<ArrayList<RoadVector>> extension;
	
	/**
	 * Cross sections of marker if any.
	 */
	private ArrayList<ArrayList<RoadVector>> marker;
	
	/**
	 * Where the leg and intersection interface.
	 */
	protected ArrayList<RoadVector> border;
	
	/**
	 * Builds a leg object.
	 * 
	 * @param road which road the leg is implemented
	 * @param centerLineEx null when the leg has no extension
	 * @param type UP_STREAM OR DOWN_STREAM
	 * @return 
	 */
	public static BasicLeg getLeg(BasicRoad road, RoadVector centerLineEx, 
			BasicLegType type){
		BasicLeg leg = new BasicLeg(road, centerLineEx, type);
		
		// this order matters, do not change it
		leg.findExtension();
		leg.findMarker();
		leg.findBorder();
		return leg;
	}
	
	/** 
	 * Default constructor only for allocating spaces for instance variables.
	 * @param road
	 * @param centerLineEx
	 * @param type
	 */
	private BasicLeg(BasicRoad road, RoadVector centerLineEx, BasicLegType type){
		this.road = road;
		this.centerLineEx = centerLineEx;
		this.type = type;
		this.border = new ArrayList<RoadVector>();
	}
	
	/**
	 * Finds the border between this leg and the intersection.
	 * <p>
	 * Note the sequence of the points need have the intersection to the left.
	 */
	private void findBorder(){
		// the marker borders the intersections
		border.addAll(marker.get(1));
		
		int markerWidth = marker.get(0).size();
		int [] temp = this.findExtXsecIndices();
		int xsecStart = temp[0], xsecEnd = temp[1];
		ArrayList<RoadVector> roadBorder;
		
		// takes into account the other side of the road,
		// which is opposite to the direction of the leg
		int start, end;
		if(type==BasicLegType.UP_STREAM){
			Collections.reverse(border);
			if(extension!=null){
				border.add(marker.get(0).get(0));
			}
			roadBorder = road.getLastXsec();
			start = xsecStart;
			end = road.hasShoulder()?1:0;
		}else{
			if(extension!=null){
				border.add(marker.get(0).get(markerWidth-1));
			}
			roadBorder = road.getFirstXsec();
			start = xsecEnd;
			end = road.hasShoulder()?roadBorder.size()-2:roadBorder.size()-1;
		}
		
		if(start<=end){
			for(int i=start;i<=end;i++){
				border.add(roadBorder.get(i));
			}
		}else{
			for(int i=start;i>=end;i--){
				border.add(roadBorder.get(i));
			}
		}
		
		// the intersection needs to be the left
		Collections.reverse(border);
	}
	
	/**
	 * Finds the the cross sections of the marker that connects the 
	 * leg and the intersection.
	 * <p>
	 * This marker is the interface between the leg and intersection. It is 
	 * needed no matter whether or not the leg has extension.
	 */
	private void findMarker(){
		int index1, index2, xsecStart, xsecEnd;
		ArrayList<ArrayList<RoadVector>> xsecs;
				
		if(centerLineEx==null){
			// if the road is not extended, finds the last two or first two
			// road center line points that need to be extrapolated, depending
			// on the leg is upstream or downstream
			xsecs = road.getXsecPts();
			int size = xsecs.size();
			index1 = (type==BasicLegType.UP_STREAM)?size-2:1;
			index2 = (type==BasicLegType.UP_STREAM)?size-1:0;
			
			// finds the cross section point indices that span
			// median, one side pavement, and shoulder
			int temp[] = findExtXsecIndices(); 
			xsecStart = temp[0];
			xsecEnd = temp[1];
		}else{
			xsecs = extension;
			index1 = 0;
			index2 = 1;
			xsecStart = 0;
			xsecEnd = xsecs.get(0).size()-1;
		}
		
		// marker does not cover shoulder area
		if(road.hasShoulder()){
			if(type==BasicLegType.UP_STREAM){
				xsecEnd--;
			}else{
				xsecStart++;
			}
		}
		
		// extrapolates the road for the marker
		marker = RoadShapeCalc.extendRoad(xsecs, index1, index2, 
				xsecStart, xsecEnd, DesignConsts.MARKER_WIDTH);
	}
	
	/**
	 * Finds the cross sections of road extension if there is a one.
	 * <p>
	 * The extension is the extrapolation of the last two points of the 
	 * last two or first two road center line points, depending on this leg
	 * is an upstream or downstream of an intersection.
	 */
	private void findExtension(){
		// centerLineEx is passed in the constructor
		// if it is null, no extension is needed
		if(centerLineEx==null){
			extension = null;
			return;
		}
		
		ArrayList<RoadVector> curve = road.getCurvePts();
		int size = curve.size();
		
		// if the leg is an upstream, the last two points will be used
		// to extrapolate; other the first two points are used.
		int v1 = (type==BasicLegType.UP_STREAM)?(size-2):(1);
		int v2 = (type==BasicLegType.UP_STREAM)?(size-1):(0);
		
		// finds the sections of cross sections that need to 
		// be extrapolated
		int [] temp = findExtXsecIndices();
		int xsecStart = temp[0];
		int xsecEnd = temp[1];

		// extrapolating
		extension = RoadShapeCalc.extendRoad(
				road, v1, v2, xsecStart, xsecEnd, centerLineEx);
	}

	/**
	 * Finds the range of cross section points that need to be 
	 * extended/extrapolated.
	 * <p>
	 * When extending a road, it may be the case in which not all the points
	 * need to be extended. Only a side of the road which is followed by
	 * an obtuse turn angle.
	 * <p>
	 * The function of this method depends on BasicRoad in which the cross section
	 * consists of the following section (from left to right)<p>
	 * <ol>
	 * <li> shoulder (optional)
	 * <li> solid marker
	 * <li> pavement
	 * <li> solid marker 
	 * <li> median (optional)
	 * <li> solid marker 
	 * <li> pavement
	 * <li> slid marker
	 * <li> shoulder <optional>
	 * </ol>
	 * 
	 * @return start and end indices of cross section points
	 */
	private int [] findExtXsecIndices(){
		
		// if this leg is upstream, the cross section to be extrapolated
		// is the right side of road; it is left side otherwise
		// no matter which side is extrapolated, the start and end indices differ
		// by 6 if shoulder and median both exist
		int xsecStart, xsecEnd;
		if(type==BasicLegType.UP_STREAM){
			xsecEnd = road.getXsecPts().get(0).size()-1;
			xsecStart = xsecEnd - 6;
		}else{
			xsecStart = 0;
			xsecEnd = xsecStart + 6;
		}
		
		// fixes the case if shoulder or median does not exist
		if(!road.hasShoulder()){
			if(type==BasicLegType.UP_STREAM)
				xsecStart++;
			else
				xsecEnd--;
		}
			
		if(!road.hasMedian()){
			if(type==BasicLegType.UP_STREAM)
				xsecStart++;
			else
				xsecEnd--;
		}
		
		return new int[]{xsecStart, xsecEnd};
	}
	
	/**
	 * Finds the border in the left shoulder area.
	 * <p>
	 * Is used to figure out the graphics model for the shoulder area
	 * of the intersection.
	 * @return the sequence of points that outline how the road shoulder
	 *         borders the intersection border
	 */
	public ArrayList<RoadVector> getLeftShoulderBorder(){
		if(!road.hasShoulder()){
			return null;
		}
		
		ArrayList<RoadVector> shoulder = new ArrayList<RoadVector>();
				
		if(type==BasicLegType.UP_STREAM){
			ArrayList<RoadVector> xsec = road.getLastXsec();
			shoulder.add(xsec.get(0));
			shoulder.add(xsec.get(1));
		}else{
			ArrayList<RoadVector> xsec = road.getFirstXsec();
			shoulder.add(xsec.get(xsec.size()-1));
			shoulder.add(xsec.get(xsec.size()-2));
		}
		
		return shoulder;
	}
	
	/**
	 * Finds the border in the right shoulder area.
	 * <p>
	 * Is used to figure out the graphics model for the shoulder area
	 * of the intersection.
	 * 
	 * @return the sequence of points that outline how the road shoulder
	 *         borders the intersection border
	 */
	public ArrayList<RoadVector> getRightShoulderBorder(){
		if(!road.hasShoulder()){
			return null;
		}
		ArrayList<RoadVector> shoulder = new ArrayList<RoadVector>();
		
		// on the right side, needs to take into account the marker where
		// the road meets the intersection.
		if(type==BasicLegType.UP_STREAM){
			int markerWidth = marker.get(1).size();
			shoulder.add(marker.get(1).get(markerWidth-1));
			
			ArrayList<RoadVector> xsec = (this.extension==null)?
					road.getLastXsec():extension.get(1);
			shoulder.add(xsec.get(xsec.size()-2));
			shoulder.add(xsec.get(xsec.size()-1));
		}else{
			shoulder.add(marker.get(1).get(0));
			
			ArrayList<RoadVector> xsec = (this.extension==null)?
					road.getFirstXsec():extension.get(1);
			shoulder.add(xsec.get(1));
			shoulder.add(xsec.get(0));
		}
		return shoulder;		
	}

	/**
	 * See {@link #extension}
	 * @return {@link #extension}
	 */
	public ArrayList<ArrayList<RoadVector>> getExtension() {
		return this.extension;
	}

	/**
	 * See {@link #marker}
	 * @return {@link #marker}
	 */
	public ArrayList<ArrayList<RoadVector>> getMarker() {
		return this.marker;
	}

	/**
	 * Returns the border between the leg and intersection.
	 * @return {@link #border}
	 */
	public ArrayList<RoadVector> getBorder() {
		return this.border;
	}

	/**
	 * Gets the road this leg object encompasses.
	 * @return {@link #road}
	 */
	public BasicRoad getRoad() {
		return road;
	}

	/**
	 * Sets the road which this leg encompasses
	 * @param road
	 */
	public void setRoad(BasicRoad road) {
		this.road = road;
	}

	public BasicLegType getType() {
		return type;
	}
	
	/**
	 * Finds the left border between the leg and surrounding terrain.
	 * <p>
	 * The "left" is relatively to the direction of the leg, see
	 * {@link #type}.
	 * 
	 * @return the sequence of points that outlines the border.
	 */
	public ArrayList<RoadVector> getLeftBorder(){
		int xsecPos = (type==BasicLegType.UP_STREAM)?0
				: road.getLastXsec().size()-1;
		ArrayList<ArrayList<RoadVector>> xsecs = road.getXsecPts();
		int begin = (type==BasicLegType.UP_STREAM)?0
				: xsecs.size()-1;
		int end  = (type==BasicLegType.DOWN_STREAM)?0
				: xsecs.size()-1;
		
		ArrayList<RoadVector> border = new ArrayList<RoadVector>();
		
		
		for(int i=(begin<=end)?begin:end;i<=((end>=begin)?end:begin);i+=1){
			border.add(xsecs.get(i).get(xsecPos));
		}
		
		if(begin>=end){
			Collections.reverse(border);
		}
		
		return border;
	}
	
	/**
	 * Finds the right border between the leg and surrounding terrain
	 * The "left" is relatively to the direction of the leg, see
	 * {@link #type}.
	 * 
	 * @return the sequence of points that outlines the border.
	 */
	public ArrayList<RoadVector> getRightBorder(){
		int xsecPos = (type==BasicLegType.DOWN_STREAM)?0
				: road.getLastXsec().size()-1;
		ArrayList<ArrayList<RoadVector>> xsecs = road.getXsecPts();
		int begin = (type==BasicLegType.UP_STREAM)?0
				: xsecs.size()-1;
		int end  = (type==BasicLegType.DOWN_STREAM)?0
				: xsecs.size()-1;
		
		ArrayList<RoadVector> border = new ArrayList<RoadVector>();
		for(int i=(begin<=end)?begin:end;i<=((end>=begin)?end:begin);i+=1){
			border.add(xsecs.get(i).get(xsecPos));
		}
		
		if(begin>=end){
			Collections.reverse(border);
		}
		
		if(this.extension!=null){
			xsecPos = (type==BasicLegType.DOWN_STREAM)?0
					: extension.get(0).size()-1;
			border.add(extension.get(1).get(xsecPos));
		}
				
		return border;
	}
}
