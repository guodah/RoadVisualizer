package com.sim.roads.basic;

import java.util.ArrayList;
import java.util.NavigableMap;

import com.sim.core.basic.RoadShapeCalc;
import com.sim.geometries.RoadVector;
import com.sim.obj.CrossSection;
import com.sim.roads.RoadBuilder;


public class BasicRoadBuilder extends RoadBuilder{

	/**
	 * It calls {@link com.sim.core.basic.RoadShapeCalc#findCenterLinePoints(ArrayList, ArrayList, ArrayList, ArrayList, CrossSection)}
	 * to find the center line points.
	 * <p>
	 * The center line points are grouped, in a hash map, according to the key points.
	 * 
	 * @param keyPoints 
	 * @param grades
	 * @param speeds
	 * @param superEles
	 * @param xsec
	 * @return a hash map where the key is the index of key point and
	 *         the value is its corresponding center line points
	 */
	public NavigableMap<Integer, ArrayList<RoadVector>> findCenterLinePoints(
			ArrayList<RoadVector> keyPoints, ArrayList<Float> grades,
			ArrayList<Float> speeds, ArrayList<Float> superEles,
			CrossSection xsec){
		return RoadShapeCalc.findCenterLinePoints(
				keyPoints, grades, speeds, superEles, xsec);
	}
	
	/**
	 * It calls {@link com.sim.core.basic.RoadShapeCalc#findCrossSections(ArrayList, CrossSection)}
	 * to find the cross section at center line points of a road.
	 * <p>
	 * The center line points are grouped, in a hash map, according to the key points.
	 * 
	 * @param centerLine all center line points
	 * @param xsec the cross sectional profile of the road
	 * @return a hash map where the key is the index of key point and
	 *         the value is its corresponding cross sections
	 */
	public NavigableMap<Integer, ArrayList<ArrayList<RoadVector>>> findCrossSections(
			NavigableMap<Integer, ArrayList<RoadVector>> centerLine,
			CrossSection xsec
			){
		return RoadShapeCalc.findCrossSections(centerLine, xsec);
	}
	
	/**
	 * Puts the cross sections in 
	 * {@link BasicRoad#xsecs} in {@link BasicRoad#xsecPts}
	 */
	public ArrayList<ArrayList<RoadVector>> findXsecPts(
			NavigableMap<Integer, ArrayList<ArrayList<RoadVector>>> xsecs){
		
		ArrayList<ArrayList<RoadVector>> xsecPts = null;
		if(xsecs==null || xsecs.size()==0){
			return xsecPts;
		}
		
		xsecPts = new ArrayList<ArrayList<RoadVector>>();
		
		Integer key = (xsecs.size()==0)?null:xsecs.firstKey();
		while(key!=null){
			xsecPts.addAll(xsecs.get(key));
			key = xsecs.higherKey(key);
		}
		return xsecPts;
	}
	
	/**
	 * Puts the center line points in {@link BasicRoad#curve} in 
	 * {@link BasicRoad#curvePts}
	 */	
	public ArrayList<RoadVector> findCurvePts(
			NavigableMap<Integer, ArrayList<RoadVector>> curve){
		ArrayList<RoadVector> curvePts = null; 
		if(curve==null || curve.size()==0){
			return null;
		}
		
		curvePts = new ArrayList<RoadVector>();
		
		Integer key = (curve.size()==0)?null:curve.firstKey();
		while(key!=null){
			curvePts.addAll(curve.get(key));
			key = curve.higherKey(key);
		}
		return curvePts;
	}

	/**
	 * Recalculates the road geometry, taking into account the super-elevation requirement.
	 * 
	 * @param roadPoints center line points
	 * @param crossSections cross sections at the center line points
	 * @param xsec the cross sectional profile of the road
	 */
	public void addSuperElevations(ArrayList<RoadVector> roadPoints,
			ArrayList<ArrayList<RoadVector>> crossSections, CrossSection xsec) {
		RoadShapeCalc.addSuperElevations(roadPoints, crossSections, xsec);
	}
}
