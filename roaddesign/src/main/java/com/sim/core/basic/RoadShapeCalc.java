package com.sim.core.basic;



import java.awt.Color;


import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.*;
import com.jmex.font3d.math.Triangulator;
import com.sim.curves.*;
import com.sim.geometries.*;
import com.sim.obj.CrossSection;
import com.sim.roads.basic.BasicRoad;
import com.sim.terrain.basic.BasicTerrain;
import com.sim.util.CollectionUtil;
import com.sim.util.ModelGenUtil;

//import sim.basic.BasicLegType;

/**
 * Includes methods for calculating road-related data.
 * 
 * Last modified: July 20, 2011
 * 
 * @author Dahai Guo
 *
 */
public class RoadShapeCalc {
	
	/**
	 * This constant indicates that the texture is good for covering
	 * a certain number of feet.
	 * 
	 * The way of texture mapping this application is layered. Each layer is 
	 * specific to a certain material. Each triangle is specified to belong to 
	 * a certain layer. This parameter specifies the length of a layer.
	 */
//	public static final float TEXTURE_LEN = 256.0f;
	
	/**
	 * For how many center line points, an individual mesh can include 
	 * at most.
	 */
//	public static final int MESH_SIZE = 50;
	
	/**
	 * Finds the heights of control points, input by the user. 
	 * 
	 * Users only specify 2D coordinates and slops in the 2D GUI. 
	 * So heights of points need be to calculated.
	 * 
	 * Does not validate the arguments.
	 * 
	 * @param inputPoints input by the user
	 * @param grades the list of grades, in percentage, between two 
	 *  			 consecutive input points
	 */
	public static void findElevation(ArrayList<RoadVector> inputPoints,
			ArrayList<Float> grades){

		float currentElevation = ModelGenConsts.INIT_ELEVATION;

		inputPoints.get(0).setZ(currentElevation);
		for(int i=1;i<inputPoints.size();i++){
			float distance = RoadVector.subtract(inputPoints.get(i-1),
				inputPoints.get(i)).magnitude2d();

			currentElevation = currentElevation+distance*grades.get(i-1)/100;
			inputPoints.get(i).setZ(currentElevation);
		}
	}

	/**
	 * Regardless heights, only finds the parameters of horizontal curves,
	 * given the input points and speeds.
	 * 
	 * See class HorizontalCurve for more details.
	 * 
	 * Does not validate arguments.

	 * @param controlPoints input by user
	 * @param speeds speeds at control points
	 * @param superEles super elevations at control points
	 * @param xsec cross section profile along all the control points
	 * @return
	 */
	public static ArrayList<HorizontalCurve> findHorizontalCurves(
			ArrayList<RoadVector> controlPoints,	ArrayList<Float> speeds,
			ArrayList<Float> superEles, CrossSection xsec){

			ArrayList<HorizontalCurve> result = 
				new ArrayList<HorizontalCurve>();
			
			// each horizontal curve is defined by three consecutive 
			// control points
			for(int i=2;i<controlPoints.size();i++){
				result.add(findHorizontalCurve(
						controlPoints.get(i-2),
						controlPoints.get(i-1),
						controlPoints.get(i),
						(speeds.get(i-2)+speeds.get(i-1))/2,
							superEles.get(i-2),
						xsec
					)
				);
			}
			return result;
	}

	/**
	 * Finds the parameters of horizontal curves, given the input points and 
	 * speeds and horizontal curves.
	 * 
	 * If a vertical curve coincides a horizontal curve, the vertical curve will
	 * be found on the plane which 1) is perpendicular to the XY plane, 2) holds
	 * the same angle with the two directions of the horizontal curves.    
	 * 
	 * See classes HorizontalCurve and VerticalCurve for more details.
	 * 
	 * Does not validate arguments.
	 * 
	 * @param controlPoints input by user
	 * @param speeds speeds at control points
	 * @param hCurves horizontal curves which are defined by control points (2D)
	 * @return
	 */
	public static ArrayList<VerticalCurve> findVerticalCurves(
			ArrayList<RoadVector> controlPoints,	
			ArrayList<Float> speeds,
			ArrayList<HorizontalCurve> hCurves){
		
			ArrayList<VerticalCurve> result = 
				new ArrayList<VerticalCurve>();
			
			// each vertical curve is defined by three consecutive 
			// control points
			for(int i=2;i<controlPoints.size();i++){
				result.add(findVerticalCurve(
					controlPoints.get(i-2),
					controlPoints.get(i-1),
					controlPoints.get(i),
					(speeds.get(i-2)+speeds.get(i-1))/2,
					hCurves.get(i-2))
				);
			}
			return result;
	}
	
	/**
	 * Finds the geometric parameters of a horizontal curve.
	 * 
	 *  Note p1 and p2 are not necessarily where the horizontal curve begins
	 *  and ends respectively. They, with pi, just define where the horizontal
	 *  curve should be.
	 * 
	 * @param p1 first control point
	 * @param pi second control point
	 * @param p2 third control point
	 * @param speed average design speed of two directions: 
	 * 				(p1-->pi) and (pi-->p2)
	 * @return
	 */
	public static HorizontalCurve findHorizontalCurve
	(RoadVector p1, RoadVector pi, 
			RoadVector p2, float speed, float superElevation, 
			CrossSection xsec){

		// v1 = pi - p1
		RoadVector v1 = RoadVector.subtract(pi, p1);
		// v2 = p2 - pi
		RoadVector v2 = RoadVector.subtract(p2, pi);
		
		// return if p1, pi, p2 are co-linear
		if(Float.compare(v1.findDirectionXY(),v2.findDirectionXY())==0 ){
			return null;
		}

		// p1ToPi_2d is |v1| projected on XY plane
		float p1ToPi_2d = v1.magnitude2d();
		// piToP2_2d is |v2| projected on XY plane
		float piToP2_2d = v2.magnitude2d();

		// v1 and v2 are changed to the unit vector on the XY plane
		v1 = v1.unit2d();
		v2 = v2.unit2d();
		
		// theta_2d is the angle of the curve
		float theta_2d = (float) (Math.acos(RoadVector.dot2d(v1,v2)));
		
		// finds where the curve begins (pc) and ends (pt) 
		float minRadius = findMinRadius(speed, superElevation); 
		RoadVector pc = RoadVector.add(p1, RoadVector.multi(v1, // v1 is unit now on XY plane
				(float) (p1ToPi_2d - minRadius*Math.tan(theta_2d/2))));
		RoadVector pt = RoadVector.add(pi, RoadVector.multi(v2, // v2 is unit now on XY plane
				(float) (minRadius*Math.tan(theta_2d/2))));

		// the relative position of p1, p2, and pi cannot implement a horizontal
		// curve with the minimum radius
		if(RoadVector.subtract(pc, pi).magnitude2d()
					>RoadVector.subtract(p1, pi).magnitude2d() ||
				RoadVector.subtract(pt, pi).magnitude2d()
					>RoadVector.subtract(p2, pi).magnitude2d()){
		    throw new IllegalStateException(
		    		"The last three control points cannot implement a horizontal curve\n" +
		    		"according AASHTO requirements\n"+
		    		"You may increase the distances between control points\n"+
		    		"or reduce super elevation"
		    );
		}
		

		// the curve should be able to support run out and run off
		RunOutOff runOutOff = findRunOutOff(speed, 
				superElevation, xsec.numOfLanes);

		// one third of the run off should be before the curve begins
		// see the document for horizontal curves
		float curveLen_2d = minRadius * theta_2d;
		if(0.333*runOutOff.runOffLen > 0.5*curveLen_2d ||
				0.667*runOutOff.runOffLen + runOutOff.runOutLen > 
					RoadVector.subtract(p1, pc).magnitude2d() ||
				0.667*runOutOff.runOffLen + runOutOff.runOutLen > 
					RoadVector.subtract(p2, pt).magnitude2d()){
						
		    throw new IllegalStateException(
		    		"The horizontal curve will be too short to support run out and off\n" +
		    		"according AASHTO requirements.\n"+
		    		"You may increase the speed limits.\n"+
		    		"or reduce super elevation"
		    );
		}

		// finding the center of the arc
		RoadVector center = findHCurveCenter(pc, pi, pt, theta_2d, minRadius);
		
		// packages the horizontal curve for return
		return new HorizontalCurve(theta_2d, minRadius, pc, pi, 
				pt, center, p1, p2, runOutOff, superElevation);

	}
	
	/**
	 * Reads table.
	 * 
	 * @param speed
	 * @return
	 */
	private static float findMinRadius(float speed, 
			float superElevation) {
		return DesignConsts.MinRadiiTable[(int) ((superElevation-4)/2)]
		                     [(int) ((speed-15)/5)];
	}	
	
	/**
	 * Reads table.
	 * 
	 * @param speed
	 * @param superElevation
	 * @param numOfLanes numOfLanes will be set to 2 if it is greater than 2
	 * @return
	 */
	private static RunOutOff findRunOutOff(float speed, float superElevation,
			int numOfLanes) {
		int temp = numOfLanes;
		if(temp>2){
			temp = 2;
		}
		
		return new RunOutOff(
				DesignConsts.RunOutTable[temp-1][(int) ((speed-15)/5)],
				DesignConsts.RunOffTable[temp-1][(int) ((superElevation-4)/2)]
				                          [(int) ((speed-15)/5)]
		);
	}

	/**
	 * Finds the center of a horizontal curve without considering z value
	 * 
	 * @param pc
	 * @param pi
	 * @param pt
	 * @param theta
	 * @param minRadius
	 * @return
	 */
	private static RoadVector findHCurveCenter(RoadVector pc, RoadVector pi, RoadVector pt,
			float theta, float minRadius) {
		
		RoadVector v1_2d = RoadVector.subtract(pi, pc).clearZ();
		RoadVector v2_2d = RoadVector.subtract(pt, pi).clearZ();
		
		RoadVector chord_2d = RoadVector.add(v1_2d, v2_2d);
		RoadVector halfChord_2d = RoadVector.multi(chord_2d, 0.5f);
		RoadVector centerOfChord_2d = RoadVector.add(pc.clearZ(), halfChord_2d);
		
		float piToCenter_2d = (float) (minRadius/Math.cos(theta/2));
		RoadVector piToCenterDir_2d = RoadVector.subtract(
				centerOfChord_2d, pi.clearZ()).unit2d();
		RoadVector center = RoadVector.add(pi.clearZ(), 
				RoadVector.multi(piToCenterDir_2d, piToCenter_2d));
		return center;
	}

	
	/**
	 * Finds the vertical curve on the plane which 1) is perpendicular to 
	 * the XY plane, 2) forms the same angle between two directions :
	 * (p1-->pvi) and (pvi-->p2).
	 * 
	 * The parameters are for the vertical curve, being projected on the plane
	 * which forms the same angle between two directions (p1-->pvi) and
	 * (pvi-->p2)
	 * 
	 * Important: pi and pvi are the same in X and Y.
	 * 
	 * @param p1
	 * @param pvi
	 * @param p2
	 * @param speed
	 * @param hCurve
	 * @return
	 */
	private static VerticalCurve findVerticalCurve(RoadVector p1, 
			RoadVector pvi, RoadVector p2, float speed, HorizontalCurve hCurve){


		if(Float.compare(p1.getZ(), p2.getZ())==0 && 
				Float.compare(p1.getZ(), pvi.getZ())==0){
			return null;
		}
		
		VerticalCurve curve = new VerticalCurve();
		curve.p1 = p1;
		curve.p2 = p2;
		curve.pvi = pvi;
		
		// v1_2d = pvi - p1 (2d)
		RoadVector v1_2d = RoadVector.subtract(pvi, p1).clearZ();
		// v2_2d = p2 - pvi (2d)
		RoadVector v2_2d = RoadVector.subtract(p2, pvi).clearZ();
		
		// finds the projection plane in which vertical curve is found
		float theta1 = v1_2d.findDirectionXY();
		float theta2 = v2_2d.findDirectionXY();
		float theta = (theta1+theta2)/2;
		RoadVector projectVector2d = new RoadVector((float)Math.cos(theta),
				(float)Math.sin(theta),0.0f);                        
		
		// finds the angle between the projection plane and two directions of 
		// the horizontal curve
		float angleV1PV = (float)Math.acos(RoadVector.dot2d(v1_2d, projectVector2d)/
				v1_2d.magnitude2d()*projectVector2d.magnitude2d()); 
		float angleV2PV = (float)Math.acos(RoadVector.dot2d(v2_2d, projectVector2d)/
				v2_2d.magnitude2d()*projectVector2d.magnitude2d());
		if(angleV1PV>Math.PI/2){
			angleV1PV = (float) (Math.PI - angleV1PV);
		}
		
		if(angleV2PV>Math.PI/2){
			angleV2PV = (float) (Math.PI - angleV2PV);
		}
		
		// grade of the first direction (p1-->pvi)
		float G1 = (float)((pvi.getZ()-p1.getZ())/(v1_2d.magnitude2d()
				*Math.cos(angleV1PV))); 
		// grade of the first direction (pvi-->p2)
		float G2 = (float)((p2.getZ()-pvi.getZ())/(v2_2d.magnitude2d()
				*Math.cos(angleV2PV))); 

		// finds the length of the vertical curve, being projected on the 
		// XY plane which means the distance from pvc to pvt
		float A = G2-G1;
		float curveProjectedLen = 0.0f;
		
		// see the document for vertical curves
		if(G1>G2){
		   curve.type = VerticalCurve.CREST;
		   curveProjectedLen = DesignConsts.CrestCurveK[(int) ((speed-15)/5)] * (-A) * 100;
		}else{
			curve.type = VerticalCurve.SAG;
			curveProjectedLen = A * 100;
		}

		if(hCurve!=null){

			// if a horizontal curve coincides, needs to figure out
			// this vertical curve is within the horizontal curve or embeds
			// the horizontal curve

			float hCurveLen = hCurve.theta*hCurve.minRadius; 
			float chord = (float)(2*Math.sin(hCurve.theta/2)*hCurve.minRadius);
			
			if(curveProjectedLen<chord){
				// PVC and PVT are on horizontal curve
				curve.inHCurve = true;
				
				// finds half the angle over which the section of the vertical curve
				// on the horizontal curve spans
				float angleCurveProjLenDiv2 = (float) Math.asin(
						(curveProjectedLen/2)/hCurve.minRadius);
				
				// finds the direction from the center to pc of the 
				// horizontal curve. The beginning angle of the horizontal
				// curve
				float angleCenterPC = RoadVector.subtract(hCurve.pc, 
						hCurve.center).findDirectionXY();
				float angleCenterPT = RoadVector.subtract(hCurve.pt, 
						hCurve.center).findDirectionXY();
				
				int clockWise = Vector23f.clockWise2d(angleCenterPC, 
						angleCenterPT)?-1:1;
				
				// finds the beginning and end angles on the horizontal curve
				// on which the vertical curve spans
				float angle1 = angleCenterPC + (hCurve.theta/2 
					- angleCurveProjLenDiv2)*clockWise;
				float angle2 = angleCenterPC + (hCurve.theta/2 
					+ angleCurveProjLenDiv2)*clockWise;
				
				curve.pvc = new RoadVector(
						(float)(hCurve.center.getX()+hCurve.minRadius
								*Math.cos(angle1)),
						(float)(hCurve.center.getY()+hCurve.minRadius
								*Math.sin(angle1)),
						0.0f
				);
				
				curve.pvc.setZ(findPVCEle(curve,hCurve, p1, pvi, p2));

				
				curve.pvt = new RoadVector(
						(float)(hCurve.center.getX()+hCurve.minRadius
								*Math.cos(angle2)),
						(float)(hCurve.center.getY()+hCurve.minRadius
								*Math.sin(angle2)),
						0.0f
				);
				
				// see the document of vertical curves
				curve.a = (G2-G1)/(2*curveProjectedLen);
				curve.b = G1;
				curve.c = curve.pvc.getZ();
			   
				return curve;			
			}else{
				curve.inHCurve = false;
			}
		}
		
		// recall: v1_2d = pvi-p1 and v2_2d = p2-pvi both in XY plane
		if(curveProjectedLen/2 > v1_2d.magnitude2d()*Math.cos(angleV1PV) ||
				curveProjectedLen/2 > v2_2d.magnitude2d()*Math.cos(angleV2PV)){
		    throw new IllegalStateException(
		    		"The last three control points cannot implement a vertical curve\n" +
		    		"according ASHTO requirements\n"+
		    		"You may increase the distances between control points.\n"
		    );
		}
		
		curve.pvc = RoadVector.add(p1.clearZ(),
				RoadVector.multi(v1_2d.unit2d(), (float) (v1_2d.magnitude2d()-
						(curveProjectedLen/2)/Math.cos(angleV1PV))));
		curve.pvc.setZ(findPVCEle(curve,hCurve, p1, pvi, p2));

		curve.pvt = RoadVector.add(pvi.clearZ(), RoadVector.multi(v2_2d.unit2d(), 
				(float) ((curveProjectedLen/2)/Math.cos(angleV2PV))));
		
		curve.a = (G2-G1)/(2*curveProjectedLen);
		curve.b = G1;
		curve.c = curve.pvc.getZ();
			   
		return curve;
	}
	
	/**
	 * Finds the height of pvc in a vertical curve.
	 * 
	 * The calculation is different when the vertical curve is in horizontal
	 * curve or embeds a horizontal curve. 
	 * 
	 * When it is in a horizontal curve, pvc.z is in the middle of pc.z and 
	 * pvi.z. Their relationship is linear interpolation on the chord of the 
	 * horizontal curve.
	 * 
	 * When it is not, the linear interpolation is on the direction (p1-->pvi)
	 * 
	 * This method also finds hCurve.pc.z if the vertical curve is within 
	 * a horizontal curve. (Implementation note: this may not be necessary.)
	 * 
	 * @param vCurve
	 * @param hCurve
	 * @param p1
	 * @param pvi
	 * @param p2
	 * @return
	 */
	private static float findPVCEle(VerticalCurve vCurve, 
			HorizontalCurve hCurve, RoadVector p1, RoadVector pvi, RoadVector p2) {
		
		if(vCurve.inHCurve){
			RoadVector chord = RoadVector.subtract(hCurve.pt, hCurve.pc).clearZ();
			RoadVector pcToPvc = RoadVector.subtract(vCurve.pvc, hCurve.pc).clearZ();
			
			float projOnChord = (float) RoadVector.dot2d(pcToPvc, chord.unit2d());

			// finds the elevation of pc of the horizontal 
			float p1ToPc = RoadVector.subtract(hCurve.pc, p1).magnitude2d();
			float p1ToPi = RoadVector.subtract(hCurve.pi, p1).magnitude2d();
			hCurve.pc.setZ((pvi.getZ()-p1.getZ())*(p1ToPc/p1ToPi)+p1.getZ());
			
			// linear interpolation on the chord
			float pvcEle = (pvi.getZ()-hCurve.pc.getZ()) * 
				(projOnChord/(chord.magnitude2d()/2)) + hCurve.pc.getZ(); 
 
			return pvcEle;
		}else{
			
			// linear interpolation on the direction (p1-->pvi)
			float p1ToPvc = RoadVector.subtract(vCurve.pvc, p1).magnitude2d();
			float p1ToPvi = RoadVector.subtract(vCurve.pvi, p1).magnitude2d();
			
			return (pvi.getZ()-p1.getZ())*(p1ToPvc/p1ToPvi)+p1.getZ(); 
		}
	}

	/**
	 * Sees if a sequence of points in a road intersect itself.
	 * 
	 * @param keyPoints
	 * @return
	 */
	private static ArrayList<RoadVector> intersectRoadSegments2D
			(ArrayList<RoadVector> keyPoints){
		ArrayList<RoadVector> intersections = null;
		
		for(int i=0;i<keyPoints.size()-2;i++){
			RoadVector r1 = keyPoints.get(i);
			RoadVector r2 = keyPoints.get(i+1);
			
			for(int j=i+2;j<keyPoints.size()-1;j++){
				RoadVector r3 = keyPoints.get(j);
				RoadVector r4 = keyPoints.get(j+1);
				
				RoadVector inter = RoadVector.intersect(r1, r2, r3, r4);
				if(inter==null)
					continue;
				
				intersections = (intersections==null)?
						new ArrayList<RoadVector>():intersections;
				intersections.add(inter);
			}
		}
		
		return intersections;
	}
	
	/**
	 * Is the highest level method, called to found center line points given
	 * user input poitns, grades, and speeds
	 * 
	 * 
	 * @param keyPoints input by users
	 * @param grades grades between control points
	 * @param speeds speed limits at control points
	 * @param superEles super elevations
	 * @param xsec cross section profile, shared by all control points
	 * @return a list of center line points
	 */
	public static NavigableMap<Integer, ArrayList<RoadVector>> findCenterLinePoints(
			ArrayList<RoadVector> keyPoints, ArrayList<Float> grades,
			ArrayList<Float> speeds, ArrayList<Float> superEles,
			CrossSection xsec){
		
		// verifies that the points sequence in keyPoints does not intersect
		if(intersectRoadSegments2D(keyPoints)!=null){
			throw new IllegalStateException(
					"So far, a road is not allowed to intersect itself."
			);
		}
		
		// finds the elevations of control points
		findElevation(keyPoints, grades);
		
		// finds the parameters of vertical and horizontal curves
		ArrayList<HorizontalCurve> hCurves = 
			findHorizontalCurves(keyPoints, speeds, superEles, xsec);
		ArrayList<VerticalCurve> vCurves = 
			findVerticalCurves(keyPoints, speeds, hCurves);
		
		NavigableMap<Integer,ArrayList<RoadVector>> result = 
				new TreeMap<Integer,ArrayList<RoadVector>>();
		
		// add the first point
		ArrayList<RoadVector> temp = new ArrayList<RoadVector>();
		temp.add(keyPoints.get(0));
		result.put(0, temp);
		
		// add curve points
		for(int i=2;i<keyPoints.size();i++){
			result.put(i-1, findKeyCenterLinePoints(keyPoints.get(i-2),
					keyPoints.get(i-1), keyPoints.get(i), 
					hCurves.get(i-2), vCurves.get(i-2), xsec)
			);
		}
		
		// add the last point
		temp = new ArrayList<RoadVector>();
		temp.add(keyPoints.get(keyPoints.size()-1));
		result.put(keyPoints.size()-1, temp);
		
		return result;
	}

	/**
	 * Given three key points (user inputs), finds the center line points
	 * considering horizontal and/or vertical curves.
	 * 
	 * @param p1 beginning point of the curve
	 * @param pi intersecting point of the curve
	 * @param p2 end point of the curve
	 * @param grade grade at pi
	 * @param speed speed limit at pi
	 * @param superEle super elevation at pi
	 * @param xsec cross section of the entire road
	 * @return
	 */
	public static ArrayList<RoadVector> findCenterLinePoints(
			RoadVector p1, RoadVector pi, RoadVector p2, 
			float grade, float speed, float superEle,
			CrossSection xsec){
		HorizontalCurve hCurve = findHorizontalCurve(
				p1, pi, p2, speed, superEle, xsec);
		VerticalCurve vCurve = findVerticalCurve(
				p1, pi, p2, speed, hCurve);
		
		ArrayList<RoadVector> result = 
			findKeyCenterLinePoints(p1, pi, p2, hCurve, vCurve, xsec);
		result.add(0, p1);
		result.add(p2);
		return result;
	}
	
	/**
	 * If the horizontal is not null, finds the curve points for the horizontal
	 * curve first. Then if the vertical curve exists, finds the section in 
	 * which the vertical curve exist, change the z values for the points in 
	 * this section.
	 * 
	 * @param p1 
	 * @param pi where the two legs of the curve meets
	 * @param p2
	 * @param hCurve horizontal curve
	 * @param vCurve vertical curve
	 * @param xsec cross section at p1, pi, and p2
	 * @return
	 */
	private static ArrayList<RoadVector> findKeyCenterLinePoints(
			RoadVector p1, RoadVector pi, RoadVector p2,
			HorizontalCurve hCurve,	VerticalCurve vCurve,
			CrossSection xsec) {
		
		if(vCurve==null && hCurve==null){
			return null; // both curves are null
		}else if(vCurve!=null && hCurve==null){
			return findVCurvePoints(vCurve);
		}else if(vCurve==null && hCurve!=null){
			return findHCurvePoints(hCurve, xsec);
		}else if(vCurve.inHCurve==true){
			// both curves exist and the vertical curve is within the horizontal curve
			ArrayList<RoadVector> result = findHCurvePoints(hCurve, xsec);
			findZOnVCurve(result, vCurve);
			return result;
		}else{
			// both curves exist and the vertical embeds the horizontal curve
			ArrayList<RoadVector> result = findHCurvePoints(hCurve, xsec);
			findEnclosingVCurve(result, vCurve);
			return result;
		}
	}

	/**
	 * Is called when a horizontal curve is within a vertical curve.
	 * 
	 * Before this call, the horizontal curve must have been found a list of points
	 * for display. Only the 2D coordinates of these points are found. This 
	 * method will find the z values for these points. Additionally, it will add 
	 * and append points for the vertical curve before and after the horizontal
	 * curve begins and ends.
	 *  
	 * @param hCurvePoints
	 * @param vCurve
	 */
	private static void findEnclosingVCurve(ArrayList<RoadVector> hCurvePoints,
			VerticalCurve vCurve) {
		
		// 1. see if vertical curve begins/end after/before run out begins/ends
		RoadVector p1ToPvc = RoadVector.subtract(vCurve.pvc, vCurve.p1);
		RoadVector p1ToRunOut = RoadVector.subtract(hCurvePoints.get(0), 
				vCurve.p1); // hCurvePoints.get(0) is where run out begins
				
		// 2. if yes, call findEmbeddedVCurve and return
		if(p1ToPvc.magnitude2d()>p1ToRunOut.magnitude2d()){
			findZOnVCurve(hCurvePoints, vCurve);
			return;
		}

		// 3. if no, discretize the segment between
		//    a) pvc --- runout begins
				
		ArrayList<RoadVector> pvcToRunOutPoints = new ArrayList<RoadVector>(); 
		RoadVector pvcToRunOut = RoadVector.subtract(hCurvePoints.get(0), 
			vCurve.pvc);
		int numOfPoints = (int) (pvcToRunOut.magnitude()/
				ModelGenConsts.RESOLUTION);
		RoadVector pvcToRunOutUnit = pvcToRunOut.unit2d();
		RoadVector temp = vCurve.pvc;
		for(int i=1;i<=numOfPoints;i++){
			temp = RoadVector.add(temp, 
					RoadVector.multi(pvcToRunOutUnit, 
							ModelGenConsts.RESOLUTION));
			
			FlagUtil.regRdDesType(temp, FlagUtil.ON_VERTICAL_CURVE);

			pvcToRunOutPoints.add(temp);
		}
		
		//    b) runout ends --- pvt
		ArrayList<RoadVector> runOutToPvtPoints = new ArrayList<RoadVector>(); 
		
		float pvcToPvt2d = RoadVector.subtract(vCurve.pvt, vCurve.pvc).
			magnitude2d();
		vCurve.pvt.setZ(vCurve.a*pvcToPvt2d*pvcToPvt2d+vCurve.b*pvcToPvt2d+
			vCurve.c);

		RoadVector runOutToPvtUnit = RoadVector.subtract(vCurve.pvt,
				hCurvePoints.get(hCurvePoints.size()-1)).unit2d();
		
		temp = hCurvePoints.get(hCurvePoints.size()-1);
		for(int i=1;i<=numOfPoints;i++){
			temp = RoadVector.add(temp, 
					RoadVector.multi(runOutToPvtUnit, 
							ModelGenConsts.RESOLUTION));
			runOutToPvtPoints.add(temp);
			FlagUtil.regRdDesType(temp, FlagUtil.ON_HORIZONTAL_CURVE);
		}
				
		hCurvePoints.addAll(0, pvcToRunOutPoints);
		hCurvePoints.add(0, vCurve.pvc);
		hCurvePoints.addAll(runOutToPvtPoints);
		hCurvePoints.add(vCurve.pvt);
		
		// 4. finds z values for vertical curve
		findZOnVCurve(hCurvePoints, vCurve);
		
		return;
	}

	/**
	 * Finds elevation of for points on a vertical curve.
	 * 
	 * Given a list of display points for the coinciding horizontal curve,
	 * first it finds the points, closest to pvt and pvc. 
	 * 
	 * This will form a range. in the given list of display points. For each 
	 * point p in this range, finds the projection of (pvc->p) on the plane 
	 * where the vertical curve was found. See findVerticalCurve.
	 * 
	 * @param hCurvePoints
	 * @param vCurve
	 */
	private static void findZOnVCurve(
			ArrayList<RoadVector> hCurvePoints, VerticalCurve vCurve) {

	//	System.out.println("findZOnVCurve");
		
		// finding which points on hCurvePoints are closest to 
		// pvc and pvt on vCurve.
		int pvcIndex=0, pvtIndex=0;
		float minPvcDistance=Float.MAX_VALUE, 
			minPvtDistance=Float.MAX_VALUE; 
		for(int i=0;i<hCurvePoints.size();i++){
			RoadVector p = hCurvePoints.get(i);
			float pvcDist = RoadVector.subtract(p, vCurve.pvc).magnitude2d();
			float pvtDist = RoadVector.subtract(p, vCurve.pvt).magnitude2d();
			
			if(pvcDist<minPvcDistance){
				minPvcDistance = pvcDist;
				pvcIndex = i;
			}
			
			if(pvtDist<minPvtDistance){
				minPvtDistance = pvtDist;
				pvtIndex = i;
			}			
		}
		
		// needs to update the z value from pvcIndex to pvtIndex
		RoadVector pvcToPvtUnit = RoadVector.subtract(vCurve.pvt, 
				vCurve.pvc).unit2d();
		for(int i=pvcIndex;i<=pvtIndex;i++){
			RoadVector arcPoint = hCurvePoints.get(i);
			RoadVector pvcToArcPoint = RoadVector.subtract(arcPoint, vCurve.pvc);
			float projOnL = RoadVector.dot2d(pvcToArcPoint, pvcToPvtUnit);
			arcPoint.setZ(vCurve.a*projOnL*projOnL + vCurve.b*projOnL + vCurve.c);
			
			FlagUtil.regRdDesType(arcPoint, FlagUtil.ON_VERTICAL_CURVE);
		}
	}

	/**
	 * Finds a list of display points for a vertical curve.
	 * 
	 * This vertical curve is not coincided with a horizontal curve
	 * 
	 * @param vCurve
	 * @return
	 */
	private static ArrayList<RoadVector> findVCurvePoints(
			VerticalCurve vCurve) {
		
	//	System.out.println("findVCurvePoints");
		
		// 1. find z value for pvt 
		RoadVector pviToP2 = RoadVector.subtract(vCurve.p2, vCurve.pvi);
		RoadVector pviToPvt = RoadVector.subtract(vCurve.pvt, vCurve.pvi);
		
		float pvtEle = vCurve.pvi.getZ() + 
			(vCurve.p2.getZ() - vCurve.pvi.getZ()) *
			(pviToPvt.magnitude2d()/pviToP2.magnitude2d());
		vCurve.pvt.setZ(pvtEle);
		
		// 2. find z values for parabola curve
		RoadVector pvcToPvtUnit = RoadVector.subtract(vCurve.pvt, vCurve.pvc).unit2d();
		int numOfPoints = (int) (RoadVector.subtract(vCurve.pvt, vCurve.pvc).
			magnitude2d() / ModelGenConsts.RESOLUTION);
		ArrayList<RoadVector> result = new ArrayList<RoadVector>();
		float distance = 0;
		for(int i=1;i<=numOfPoints;i++){
			distance += ModelGenConsts.RESOLUTION;
			float z = vCurve.a*distance*distance + vCurve.b*distance + vCurve.c;
			RoadVector temp = RoadVector.add(vCurve.pvc.clearZ(),
					RoadVector.multi(pvcToPvtUnit, distance));
			
			temp = new RoadVector(temp.getX(),temp.getY(),z);
			FlagUtil.regRdDesType(temp, FlagUtil.ON_VERTICAL_CURVE);
			result.add(new RoadVector(temp.getX(),temp.getY(),z));
		}
		
		result.add(0, vCurve.pvc);
		result.add(vCurve.pvt);
		return result;
	}

	/**
	 * Finds a list of display points for a horizontal curve.
	 * 
	 * This list of points include: 
	 * <ul>
	 * <li>pc
	 * <li>runout points
	 * <li>runoff points
	 * <li>arc points
	 * <li>runoff points
	 * <li>runout points
	 * <li>pt
	 * 
	 * Note that the finding of horizontal curve parameters are for
	 * the projected horizontal curve on the XY plane.
	 * 
	 * @param hCurve
	 * @return
	 */
	private static ArrayList<RoadVector> findHCurvePoints(
			HorizontalCurve hCurve, CrossSection xsec) {

		// 0. restore grades
		float grade1 = (hCurve.pi.getZ()-hCurve.p1.getZ())/
			RoadVector.subtract(hCurve.pi, hCurve.p1).magnitude2d();
		float grade2 = (hCurve.p2.getZ()-hCurve.pi.getZ())/
			RoadVector.subtract(hCurve.p2, hCurve.pi).magnitude2d();

		
		// 1. find z value for pc
		RoadVector p1ToPi = RoadVector.subtract(hCurve.pi, hCurve.p1);
		RoadVector p1ToPc = RoadVector.subtract(hCurve.pc, hCurve.p1);
		
		hCurve.pc.setZ(grade1 * p1ToPc.magnitude2d() + hCurve.p1.getZ());
		
		
		// 2. find the start of runout and its elevation
		float p1ToRunOut2d = (float) (p1ToPc.magnitude2d() - hCurve.runOutOff.runOutLen -
			hCurve.runOutOff.runOffLen * 0.667);
		
		RoadVector runOutBegins = RoadVector.add(hCurve.p1, 
				RoadVector.multi(p1ToPi.unit2d(), p1ToRunOut2d));
		runOutBegins.setZ(p1ToRunOut2d*grade1 + hCurve.p1.getZ());
		runOutBegins.setAchorType(ModelGenConsts.RUNOUT_BEGINS);
		
		FlagUtil.regRdDesType(runOutBegins, FlagUtil.ON_RUNOUT_SECTION);
		
		// 3. find the sequence of 3d points between the start of run out
		//    and pc 
		float distance = 0;
		int numOfKeyPoints = (int) ((hCurve.runOutOff.runOutLen + 
				0.667 * hCurve.runOutOff.runOffLen) / 
					ModelGenConsts.RESOLUTION);
		ArrayList<RoadVector> runOutOff_1 = new ArrayList<RoadVector>();
		RoadVector temp = runOutBegins;
		boolean runOffFound = false;
		for(int i=0;i<numOfKeyPoints;i++){
			RoadVector next = RoadVector.add(temp, 
					RoadVector.multi(p1ToPi.unit2d(), 
							ModelGenConsts.RESOLUTION));
			next.setZ(temp.getZ() + grade1 * ModelGenConsts.RESOLUTION);
			
			distance += ModelGenConsts.RESOLUTION;

			if(!runOffFound){
				FlagUtil.regRdDesType(next, FlagUtil.ON_RUNOUT_SECTION);
			}else{
				FlagUtil.regRdDesType(next, FlagUtil.ON_RUNOFF_SECTION);
			}
			
			if(!runOffFound && Float.compare(Math.abs(distance- 
					hCurve.runOutOff.runOutLen), 
					ModelGenConsts.VERY_SMALL_FLOAT)<0){
				
				next.setAchorType(ModelGenConsts.RUNOFF_BEGINS);
				runOffFound = true;
			}else if(!runOffFound && Float.compare(distance, 
					hCurve.runOutOff.runOutLen)>0){
				
				RoadVector v = RoadVector.add(runOutBegins, 
						RoadVector.multi(p1ToPi.unit2d(), 
								hCurve.runOutOff.runOutLen)
				);
				v.setZ(grade1 * hCurve.runOutOff.runOutLen + runOutBegins.getZ());;
				v.setAchorType(ModelGenConsts.RUNOFF_BEGINS);
				
				FlagUtil.regRdDesType(v, FlagUtil.ON_RUNOFF_SECTION);
				
				runOutOff_1.add(v);
				runOffFound = true;
			}
			
			runOutOff_1.add(next);
			temp = next;
		}
		
		// 4. find z values of pt
		RoadVector piToP2 = RoadVector.subtract(hCurve.p2, hCurve.pi);
		RoadVector piToPt = RoadVector.subtract(hCurve.pt, hCurve.pi);
		
		hCurve.pt.setZ(grade2 * piToPt.magnitude2d() + hCurve.pi.getZ());;
		
		// 5. find the 2d points on the arc curve
		numOfKeyPoints = (int) ((hCurve.theta * hCurve.minRadius) 
				/ ModelGenConsts.RESOLUTION);
		ArrayList<RoadVector> arcPoints = new ArrayList<RoadVector>();
		float unitAngle = ModelGenConsts.RESOLUTION / hCurve.minRadius;
		float beginAngle = RoadVector.subtract(hCurve.pc, hCurve.center).
			findDirectionXY();
		float endAngle = RoadVector.subtract(hCurve.pt, hCurve.center).
			findDirectionXY();
		
		if(Vector23f.clockWise2d(beginAngle, endAngle)){
			unitAngle *= -1;
		}
		
		float runOffEndsAngleFirst = (hCurve.runOutOff.runOffLen*.333f)
			/hCurve.minRadius;
		
		RoadVector p2ToPi = RoadVector.subtract(hCurve.pi, hCurve.p2);
		float runOffEndsAngleLast = hCurve.theta - 
				(hCurve.runOutOff.runOffLen*.333f)/hCurve.minRadius;

		runOffFound = false;
		boolean runOffFound2 = false;
		for(int i=1;i<=numOfKeyPoints;i++){
			float x = (float) (hCurve.center.getX() + Math.cos(beginAngle+i*unitAngle)
				*hCurve.minRadius);
			float y = (float) (hCurve.center.getY() + Math.sin(beginAngle+i*unitAngle)
				*hCurve.minRadius);

			RoadVector v = new RoadVector(x,y,0);
			
			FlagUtil.regRdDesType(v, FlagUtil.ON_HORIZONTAL_CURVE);
			
			if(!runOffFound && Float.compare(Math.abs(Math.abs(i*unitAngle)- 
					runOffEndsAngleFirst), ModelGenConsts.VERY_SMALL_FLOAT)<0){
				v.setAchorType(ModelGenConsts.RUNOFF_ENDS);
				runOffFound = true;
			}else if(!runOffFound && Float.compare(Math.abs(i*unitAngle), 
					runOffEndsAngleFirst)>0){
				x = (float) (hCurve.center.getX() + 
						Math.cos(beginAngle+runOffEndsAngleFirst
								*(unitAngle>0?1:-1)
						) *
						hCurve.minRadius);
				y = (float) (hCurve.center.getY() + 
						Math.sin(beginAngle+runOffEndsAngleFirst
								*(unitAngle>0?1:-1)
						) * 
						hCurve.minRadius);
				RoadVector t = new RoadVector(x,y,0);
				t.setAchorType(ModelGenConsts.RUNOFF_ENDS);
				
				FlagUtil.regRdDesType(t, FlagUtil.ON_HORIZONTAL_CURVE);
				
				arcPoints.add(t);
				runOffFound = true;
			}
			
			if(!runOffFound2 && Float.compare(Math.abs(Math.abs(i*unitAngle)- 
					runOffEndsAngleLast), ModelGenConsts.VERY_SMALL_FLOAT)<0){
				v.setAchorType(ModelGenConsts.RUNOFF_ENDS);
				runOffFound2 = true;
			}else if(!runOffFound2 && Float.compare(Math.abs(i*unitAngle), 
					runOffEndsAngleLast)>0){
				x = (float) (hCurve.center.getX() + 
						Math.cos(beginAngle+runOffEndsAngleLast
								*(unitAngle>0?1:-1)
						) *
						hCurve.minRadius);
				y = (float) (hCurve.center.getY() + 
						Math.sin(beginAngle+runOffEndsAngleLast
								*(unitAngle>0?1:-1)
						) * 
						hCurve.minRadius);
				RoadVector t = new RoadVector(x,y,0);
				t.setAchorType(ModelGenConsts.RUNOFF_ENDS);
				arcPoints.add(t);
				runOffFound2 = true;
			}
			
			arcPoints.add(v);
		}
		
		// 6. find z values of the arc points
		RoadVector pcToArcPoint = null;
		RoadVector chord = RoadVector.subtract(hCurve.pt, hCurve.pc); 
		float chordLen = chord.magnitude2d();
		RoadVector chordUnit = chord.unit2d();
		for(int i=0;i<arcPoints.size();i++){
			RoadVector arcPoint = arcPoints.get(i);
			pcToArcPoint = RoadVector.subtract(arcPoint, hCurve.pc);
			float projOnChord = RoadVector.dot2d(chordUnit, pcToArcPoint);
			if(projOnChord>(chordLen/2)){
				arcPoint.setZ(hCurve.pi.getZ() + (hCurve.pt.getZ() - hCurve.pi.getZ()) *
					((projOnChord - chordLen/2) / (chordLen/2)));
			}else{
				arcPoint.setZ(hCurve.pc.getZ() + (hCurve.pi.getZ() - hCurve.pc.getZ()) *
					(projOnChord / (chordLen/2)));;
			}
		}
		
		// 7. find the end of the run out and its elevation
		RoadVector p2ToPt = RoadVector.subtract(hCurve.pt, hCurve.p2);
		
		float p2ToRunOut2d = (float) (p2ToPt.magnitude2d() - hCurve.runOutOff.runOutLen -
			hCurve.runOutOff.runOffLen * 0.667);
		
		// runOutBegins2 is 3d now
		RoadVector runOutBegins2 = RoadVector.add(hCurve.p2, 
				RoadVector.multi(p2ToPt.unit2d(), p2ToRunOut2d));
		runOutBegins2.setZ(hCurve.p2.getZ() + p2ToRunOut2d *(-grade2));
		
		FlagUtil.regRdDesType(runOutBegins2, FlagUtil.ON_RUNOUT_SECTION);
		
		runOutBegins2.setAchorType(ModelGenConsts.RUNOUT_BEGINS);
				
		// 8. find the sequence of points between pt and the end of run out
		numOfKeyPoints = (int) ((hCurve.runOutOff.runOutLen + 
				0.667 * hCurve.runOutOff.runOffLen) / 
					ModelGenConsts.RESOLUTION);
		ArrayList<RoadVector> runOutOff_2 = new ArrayList<RoadVector>();
		temp = runOutBegins2;
		runOffFound = false;
		distance = 0;
		for(int i=1;i<=numOfKeyPoints;i++){
			RoadVector next = RoadVector.add(temp, 
					RoadVector.multi(p2ToPt.unit2d(), 
							ModelGenConsts.RESOLUTION));
			next.setZ(temp.getZ() + ModelGenConsts.RESOLUTION * (-grade2));
	
			distance += ModelGenConsts.RESOLUTION;
			
			if(!runOffFound){
				FlagUtil.regRdDesType(next, FlagUtil.ON_RUNOUT_SECTION);
			}else{
				FlagUtil.regRdDesType(next, FlagUtil.ON_RUNOFF_SECTION);
			}

			if(!runOffFound && Float.compare(Math.abs(distance- 
					hCurve.runOutOff.runOutLen), 
					ModelGenConsts.VERY_SMALL_FLOAT)<0){
				
				next.setAchorType(ModelGenConsts.RUNOFF_BEGINS);
				runOffFound = true;
			}else if(!runOffFound && Float.compare(distance, 
					hCurve.runOutOff.runOutLen)>0){
				
				RoadVector v = RoadVector.add(runOutBegins2, 
						RoadVector.multi(p2ToPt.unit2d(), 
								hCurve.runOutOff.runOutLen)
				);
				v.setZ((-grade2) * hCurve.runOutOff.runOutLen + runOutBegins2.getZ());
				v.setAchorType(ModelGenConsts.RUNOFF_BEGINS);
				
				FlagUtil.regRdDesType(v, FlagUtil.ON_RUNOFF_SECTION);
				
				runOutOff_2.add(0,v);
				runOffFound = true;
			}
		
			runOutOff_2.add(0,next);
			temp = next;
		}

		// 9. runOutBegins + runOutOff_1 + pc + arcPoints + pt 
		//         + runOutOff_2 + runOutEnds
		ArrayList<RoadVector> result = new ArrayList<RoadVector>();
		result.add(runOutBegins);
		result.addAll(runOutOff_1);
		result.add(hCurve.pc);
		result.addAll(arcPoints);
		result.add(hCurve.pt);
		result.addAll(runOutOff_2);
		result.add(runOutBegins2);
		
		// 10. find key point where the inner side starts to lower
		//     if the lane slope is already no less than the super
		//     elevation, this is not necessary
		if(Float.compare(xsec.laneSlope, hCurve.superElevation)<0){
			distance = 0;
			
			// not until "begin" distance is passed after the runout begins
			// the inner side will start lowering.
			float begin = hCurve.runOutOff.runOutLen + 
				(xsec.laneSlope/hCurve.superElevation)*
				hCurve.runOutOff.runOffLen;

			RoadVector v1 = result.get(0), v2;
			for(int i=1;i<result.size()-1;i++){
				v2 = result.get(i);
				distance += RoadVector.subtract(v2, v1).magnitude();
				
				if(Float.compare(Math.abs(distance-begin), 
						ModelGenConsts.VERY_SMALL_FLOAT)<0){
					v2.setAchorType(ModelGenConsts.LEVEL_OTHER_SIDE);
					break;
				}else if(distance > begin){
					RoadVector v = RoadVector.add(v1, 
							RoadVector.multi(
								RoadVector.subtract(v2, v1).unit(), 
							distance - begin
						)
					);
					result.add(i, v);
					v.setAchorType(ModelGenConsts.LEVEL_OTHER_SIDE);
					break;
				}
				v1 = v2;
			}

			v1 = result.get(result.size()-1);
			distance = 0;
			for(int i=result.size()-2;i>=0;i--){
				v2 = result.get(i);
				distance += RoadVector.subtract(v1, v2).magnitude();
								
				if(Float.compare(Math.abs(distance-begin), 
						ModelGenConsts.VERY_SMALL_FLOAT)<0){
					v2.setAchorType(ModelGenConsts.LEVEL_OTHER_SIDE);
					break;
				}else if(distance > begin){
					RoadVector v = RoadVector.add(v1, 
							RoadVector.multi(
								RoadVector.subtract(v2, v1).unit(), 
							distance - begin
						)
					);
					result.add(i+1, v);
					v.setAchorType(ModelGenConsts.LEVEL_OTHER_SIDE);
					break;
				}
				v1 = v2;
			}
		}
		
		// 11. registering hCurve at hCurve points
		int count=0;
		for(RoadVector v: result){
			v.hCurve = hCurve;
		//	System.out.printf("***%d***: %s\n",count++, v);
		}

		// 12. snapping close points
		for(int i=0;i<result.size()-1;){
			RoadVector v1 = result.get(i);
			RoadVector v2 = result.get(i+1);
			
			distance = RoadVector.subtract(v1, v2).magnitude2d();
			if(distance<DesignConsts.DELTA){
				if(v1.getAchorType()==0){
					result.remove(i);
				}else if(v2.getAchorType()==0){
					result.remove(i+1);
				}
			}else{
				i++;
			}
		}
		
		return result;
	}
	
	/**
	 * For each center line point, finds its cross section in form of 
	 * a list profile points of cross section.
	 * 
	 * The profile points are absolute coordinates. 
	 * 
	 * @param centerLine a list of center line coordinates
	 * @param cs cross sectional profile
	 * @return
	 */
	public static ArrayList<ArrayList<RoadVector>> 
		findCrossSections(ArrayList<RoadVector> centerLine, CrossSection cs){
		ArrayList<ArrayList<RoadVector>> crossSections = 
				new ArrayList<ArrayList<RoadVector>>();
		
		for(int i=0;i<centerLine.size()-1;i++){			
			RoadVector v0 = (i!=0)?centerLine.get(i-1):null;
			RoadVector v1 = centerLine.get(i);
			RoadVector v2 = centerLine.get(i+1);
						
			// finds the direction of the current road segment
			float direction;
			if(v0!=null){
				float direction1 = RoadVector.subtract(v2, v1).findDirectionXY();
				float direction2 = RoadVector.subtract(v1, v0).findDirectionXY();
				if( (direction1 > 1.5*Math.PI && direction2 < 0.5*Math.PI) ||
						(direction2 > 1.5*Math.PI && direction1 < 0.5*Math.PI)){
					direction = (float) ((direction1+direction2)/2 - Math.PI);
				}else{
					direction = (direction1+direction2)/2;
				}
			}else{
				direction = RoadVector.subtract(v2, v1).findDirectionXY();
			}
			
			// finds the left and right direction at this 
			// center line point
			float leftDir = (float) (direction + Math.PI/2);
			float rightDir = (float) (direction - Math.PI/2);
			
			// finds the unit vectors for the left and right directions
			RoadVector left2d = new RoadVector((float)Math.cos(leftDir), 
					(float)Math.sin(leftDir),0);
			RoadVector right2d = new RoadVector((float)Math.cos(rightDir), 
					(float)Math.sin(rightDir),0);
			
			// finds the cross section at centerLine.get(i)
			crossSections.add(findCrossSection(v1,left2d, right2d, cs));
			
			if(i==centerLine.size()-2){
				// finds the cross section of the last point
				crossSections.add(findCrossSection(v2,left2d, right2d, cs));
			}
			
		}
		
		return crossSections;
	}
	
	/**
	 * Given a center line point, finds the profile points of its cross section.
	 * 
	 * Note: the median height is assumed to be zero
	 * 
	 * @param v center line point
	 * @param left left direction (unit vector)
	 * @param right right direction (unit vector)
	 * @param cs cross section 
	 * @return
	 */
	public static ArrayList<RoadVector> findCrossSection(RoadVector v,
			RoadVector left, RoadVector right, CrossSection cs) {
		ArrayList<RoadVector> crossSec = new ArrayList<RoadVector>();
		
		// initially, the cross section is considered zero width
		// therefore both left and right most points are v which
		// is the input center line point
		RoadVector leftMost = v;
		RoadVector rightMost = v;
		
		/* add points for median */
		if(Float.compare(cs.medianWidth, 0)!=0){
			leftMost = incrementXsec(crossSec, leftMost, true, left, 
					0, cs.medianWidth/2, ModelGenConsts.MEDIAN);

			rightMost = incrementXsec(crossSec, rightMost, false, right,
					0, cs.medianWidth/2, ModelGenConsts.ROAD_SURFACE);
		}
		
		// the median is sandwiched by MARKER_GAP/2 pavement and
		// MARKER_WIDTH yellow marker
		leftMost = incrementXsec(crossSec, leftMost, true, left,
				0, DesignConsts.MARKER_GAP/2, ModelGenConsts.ROAD_SURFACE);
	
		leftMost = incrementXsec(crossSec, leftMost, true, left,
				0, DesignConsts.MARKER_WIDTH, ModelGenConsts.YELLOW_MARKER);
		
		rightMost = incrementXsec(crossSec, rightMost, false, right,
				0, DesignConsts.MARKER_GAP/2, ModelGenConsts.YELLOW_MARKER);
		
		rightMost = incrementXsec(crossSec, rightMost, false, right,
				0, DesignConsts.MARKER_WIDTH, ModelGenConsts.ROAD_SURFACE);
		
		// because around the median are there lanes and shoulders which 
		// may have slopes, so their profile points can be relatively 
		// different from the center line point. 
		float leftZ = v.getZ();
		float rightZ = v.getZ();
		
		/* add points for lanes */
		
		// each lane actually means a segment of pavement followed by 
		// a white marker.
		for(int i=0;i<cs.numOfLanes;i++){
			
			leftMost = incrementXsec(crossSec, leftMost, true, left,
					-cs.laneSlope, cs.laneWidth, ModelGenConsts.ROAD_SURFACE);
	
			leftMost = incrementXsec(crossSec, leftMost, true, left,
					-cs.laneSlope, DesignConsts.MARKER_WIDTH, ModelGenConsts.WHITE_MARKER);

			rightMost = incrementXsec(crossSec, rightMost, false, right,
					-cs.laneSlope, cs.laneWidth, ModelGenConsts.WHITE_MARKER);
			
			rightMost = incrementXsec(crossSec, rightMost, false, right,
					-cs.laneSlope, DesignConsts.MARKER_WIDTH, ModelGenConsts.ROAD_SURFACE);
		}
		
		// add a little paved region before shoulder
		leftMost = incrementXsec(crossSec, leftMost, true, left,
				-cs.laneSlope, DesignConsts.MARKER_WIDTH, ModelGenConsts.ROAD_SURFACE);

		rightMost = incrementXsec(crossSec, rightMost, false, right,
				-cs.laneSlope, DesignConsts.MARKER_WIDTH, ModelGenConsts.SHOULDER);
		
		/* add points for shoulders */
		if(Float.compare(cs.shoulderWidth, 0)!=0){
			leftMost = incrementXsec(crossSec, leftMost, true, left,
					-cs.shoulderSlope, cs.shoulderWidth, ModelGenConsts.SHOULDER);

			rightMost = incrementXsec(crossSec, rightMost, false, right,
					-cs.shoulderSlope, cs.shoulderWidth, ModelGenConsts.SHOULDER);
		}
		
		return crossSec;
	}
	
	
	
	/**
	 * Revises the center line point list so that it is possible to cut the 
	 * center line point list into segments, each of which has a length being
	 * equal to segLen.
	 * 
	 * This was developed to facilitate texture mapping (wrapping mode). In detail,
	 * it makes sure that the texture coordinates of each triangle do not wrap textures.
	 * This is necessary when a single texture is formed, containing multiple materials.
	 * 
	 * Note this function should not be called when the road graphics model consists of
	 * multiple meshes, each of which uses its own texture file.
	 * 
	 * @param center center line points
	 * @param segLen 
	 */
	/*
	public static void splitCenterLineTexture(ArrayList<RoadVector> center, float segLen){
		float currentLen = 0;
		
		ArrayList<RoadVector> localList = new ArrayList<RoadVector>();
		RoadVector v1 = center.get(0);
		localList.add(v1);
		
		for(int i=0;i<center.size()-1;){
			
			RoadVector v2 = center.get(i+1);
			float distancev1_v2 = RoadVector.subtract(v2, v1).magnitude();
					
			if(Math.abs((currentLen+distancev1_v2)-segLen)>
				ModelGenConsts.VERY_SMALL_FLOAT 
					&& (currentLen+distancev1_v2)<segLen){
				currentLen += distancev1_v2;
				v1 = v2;
				localList.add(v2);
				i++;
			}else if(Math.abs((currentLen+distancev1_v2)-segLen)>
				ModelGenConsts.VERY_SMALL_FLOAT
					&& (currentLen+distancev1_v2)>segLen){
				RoadVector direction = RoadVector.subtract(v2, v1).unit();
				RoadVector newPoint = RoadVector.add(v1, 
						RoadVector.multi(direction, segLen-currentLen));
				
				FlagUtil.regRdDesType(newPoint, 
						FlagUtil.ON_HORIZONTAL_CURVE);
				
				if(v1.hCurve == v2.hCurve){
					newPoint.hCurve = v1.hCurve;
				}else{
					newPoint.hCurve = null;
				}
				
				currentLen = 0;
				
				localList.add(newPoint);

				newPoint.crossSectionType = v1.crossSectionType;
				v1 = newPoint;
			}else{
				currentLen = 0;
				v1 = v2;
				localList.add(v2);
				i++;
			}	
		}
		
		center.clear();
		center.addAll(localList);
	}
*/		

	
	/**
	 * See whether or not the curve, defined by p1, pi, and p2, overlaps the 
	 * previous curve which ends at lastValidPoint2d
	 * 
	 * @param p1
	 * @param pi
	 * @param p2
	 * @param speed
	 * @param lastValidPoint2d
	 * @return the last point of the curve by p1, pi, and p2
	 */
	/*
	private static RoadVector isValidCurve(RoadVector p1, RoadVector pi, 
			RoadVector p2, float speed, float superElevation, 
			RoadVector lastValidPoint2d,
			CrossSection xsec){
				
		HorizontalCurve hCurve = 
			RoadShapeCalc.findHorizontalCurve(
					p1, pi, p2, speed, superElevation, xsec);
		
		VerticalCurve vCurve =
			RoadShapeCalc.findVerticalCurve(
					p1, pi, p2, speed, hCurve);
				
		float d1, d2=0.0f, d3=0.0f;
		d2 = (hCurve!=null)?RoadVector.subtract(hCurve.pt, pi).magnitude2d()+
				hCurve.runOutOff.runOutLen + 0.667f * hCurve.runOutOff.runOffLen
				:0.0f;
		d3 = (vCurve!=null)?RoadVector.subtract(vCurve.pvc, pi).magnitude2d():0.0f;
		

		if(lastValidPoint2d!=null){
			d1 = RoadVector.subtract(lastValidPoint2d, pi).
				magnitude2d();
				
			if(hCurve!=null){
				if(d2>=d1){
					throw new IllegalStateException(
							"This curve is too close to the previous one."
					);
				}
			}
				
			if(vCurve!=null){
				if(d3>=d1){
					throw new IllegalStateException(
							"This curve is too close to the previous one."
					);
				}
			}
		}
			
		// updating lastValidPoint
		float d = (d2>d3)?d2:d3;
		lastValidPoint2d = RoadVector.add(pi, 
				RoadVector.multi(RoadVector.subtract(p2, pi).unit2d(),d));
		
		return lastValidPoint2d;
	}
	*/
	
	/**
	 * Removes points in a list of points so that no two points are less than
	 * a certain threshold away.
	 * 
	 * If a point is anchored, it cannot be removed.
	 * 
	 * @param curvePoints
	 * @param minDist
	 */
	/*
	public static void snapCurvePoints(ArrayList<RoadVector> curvePoints,
			float minDist){
		RoadVector p1 = curvePoints.get(0), p2;
		for(int i=1;i<curvePoints.size();){
			p2 = curvePoints.get(i);
			float dist = RoadVector.subtract(p1, p2).magnitude();
			
			if(dist<minDist){
				if(p2.getAchorType()==ModelGenConsts.NOT_ANCHOR &&
						i!=(curvePoints.size()-1)){
					curvePoints.remove(i);
				}else{
					curvePoints.remove(i-1);
					p1 = p2;
				}
			}else{
				i++;
				p1 = p2;
			}
		}
	}
	*/

	/**
	 * Saves the starting view point when the graphics model is first loaded.
	 * 
	 * The view point is the first ceter line point. Additionally, it saves
	 * gaze, left, and up vectors for the game engine to initialize.
	 * 
	 * @param crossSections
	 * @param centerLine
	 * @param format
	 */
	/*
	private static void saveStartView(ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine,	Formatter format){
		RoadVector eye = centerLine.get(0).duplicate();
		eye.setZ(eye.getZ()+3);

		// finds the starting gaze direction
		RoadVector gaze = RoadVector.subtract(centerLine.get(1), 
				centerLine.get(0)).unit();
		
		// finds the left direction
		RoadVector left = RoadVector.subtract(crossSections.get(0).get(0), 
				centerLine.get(0)).unit2d();
		
		// finds the up direction
		RoadVector up = RoadVector.cross(gaze, left);
		
		// save eype point and (left, up, and gaze) to file
		// (left, up and gaze) is required by the game engine
		format.format("%.3f\t%.3f\t%.3f\n", eye.getX(), eye.getY(), eye.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", left.getX(), left.getY(), left.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", up.getX(), up.getY(), up.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", gaze.getX(), gaze.getY(), gaze.getZ());
	}
	*/
	/**
	 * Saves the triangles, created by the user.
	 * 
	 * This method will also save an initial eye point so that it will
	 * start driving from the beginning of the road
	 * 
	 * Note how dotted white markers are realized need to be improved.

	 * @param crossSections cross section profiles 
	 * 				at all the center line points
	 * @param centerLine 
	 * @param format file access
	 * @param textLim longitudal texture size
	 * @param meshSize max num of center line points in a mesh
	 */
	/*
	public static void saveRoadTriangles(ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine,	Formatter format, 
			float textLim, int meshSize) {
		// finds the starting eye point
		saveStartView(crossSections, centerLine, format);
		
		saveMeshes(crossSections, centerLine, format, 
				textLim, meshSize);
		
	}
	*/
	/**
	 * Saves meshes to disk. 
	 * 
	 * @param crossSections cross sections at center line points, containing 
	 * 		material information
	 * @param centerLine center line points that needs to be consistent with
	 * 		crossSections
	 * @param format disk access
	 * @param textLim texture size
	 * @param meshSize number of center line points included in one mesh
	 */
	/*
	private static void saveMeshes(
			ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, Formatter format, float textLim,
			int meshSize) {
		
		// find white marker indices
		ArrayList<Integer> whiteMarkerIndices = new ArrayList<Integer>();
		for(int i=0;i<crossSections.get(0).size();i++){
			if(crossSections.get(0).get(i).crossSectionType == 
				ModelGenConsts.WHITE_MARKER){
				whiteMarkerIndices.add(i);
			}
		}
		
		// find the max and min indices for dotted white makers
		// the fist and last white markers will not be dotted.
		int minWhiteIndex = -1, maxWhiteIndex = -1;
		if(whiteMarkerIndices.size()>2){
			whiteMarkerIndices.remove(0); 
			whiteMarkerIndices.remove(whiteMarkerIndices.size()-1);
			minWhiteIndex = whiteMarkerIndices.get(0);
			maxWhiteIndex = whiteMarkerIndices.get(whiteMarkerIndices.size()-1);
		}

		// find how many meshes are needed
		int numOfMeshes = (crossSections.size()-1)/meshSize;
		if((crossSections.size()-1)%meshSize!=0)
			numOfMeshes++;
		
		// save the number of meshes, jME seems to have
		// limit on number of triangles for individual meshes
		format.format("%d\n", numOfMeshes);
		
		float distance = 0.0f;
		
		// build individual mesh 
		for(int k=0;k<numOfMeshes;k++){
	
			distance = saveMesh(crossSections, centerLine, format, 
					textLim, meshSize, k*meshSize, 
					minWhiteIndex, maxWhiteIndex, whiteMarkerIndices, distance);
		}

	}
	 */
	/**
	 * Saves one mesh
	 * 
	 * @param crossSections cross sections at center line points, containing 
	 * 		material information
	 * @param centerLine center line points that needs to be consistent with
	 * 		crossSections
	 * @param format disk access
	 * @param textLim texture size
	 * @param meshSize number of center line points included in one mesh
	 * @param startXsec starting index of the center line point for this mesh
	 * 
	 * @param minWhiteIndex left border white marker index 
	 * @param maxWhiteIndex right border white marker index
	 * @param whiteMarkerIndices other dotted white marker
	 * @param distance distance on the center line that has been processed
	 * @return
	 */
	/*
	private static float saveMesh(ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, Formatter format, float textLim,
			int meshSize, int startXsec, int minWhiteIndex, int maxWhiteIndex,
			ArrayList<Integer> whiteMarkerIndices, float distance) {

		float longitudeLen1 = distance;
		float longitudeLen2 = distance;
		RoadVector center1 = centerLine.get(0);

		int numOfCrocs = 0;
		if(startXsec+meshSize<crossSections.size()){
			numOfCrocs = meshSize;
		}else{
			// this is the last mesh
			numOfCrocs = (crossSections.size()-1)-startXsec;
		}
		
		// save the number of vertices, used by this mesh
		format.format("%d\n", (numOfCrocs+1)*
				crossSections.get(0).size());
		
		// save all the vertices, used in this mesh
		for(int i=0;i<numOfCrocs+1;i++){
			for(int j=0;j<crossSections.get(0).size();j++){
				RoadVector v = crossSections.
					get(startXsec+i).get(j);
				format.format("%.3f\t%.3f\t%.3f\n", v.getX(),v.getY(),v.getZ());
			}
		}
		
		// save the number of triangles
		format.format("%d\n", 2*numOfCrocs*(crossSections.get(0).size()-1));		
		
		// for each cross section cs1, together with the next one cs2,
		// these two cross sections correspond each other by having the same
		// number of vertices. Easily speaking, two cross sections form a sequence
		// of rectangles. Each such rectangle is split into two triangles.
		for(int i=0;i<numOfCrocs;i++){				
			// find the two consecutive cross sections
						
			// find the next center line point (center2)
			// [center1 was found outside the loop]
			RoadVector center2 = centerLine.get(startXsec+i+1);

			// the distance between center1 and center2
			longitudeLen2 += RoadVector.subtract(center2, center1).magnitude();
			
			// longitudeLen2 just hit a multiple of textLim
			// it may be slightly greater or less than that
			// to avoid floating-point error, it is set to be a multiple
			if(Math.abs(longitudeLen2/textLim-Math.round(longitudeLen2/textLim))<
					ModelGenConsts.VERY_SMALL_FLOAT){					
				longitudeLen2 = textLim*(Math.round(longitudeLen2/textLim));
			}

			buildStrip(crossSections, centerLine, format,
					startXsec+i, i, minWhiteIndex, maxWhiteIndex,
					longitudeLen1, longitudeLen2, textLim);

			longitudeLen1 = longitudeLen2;
			center1 = center2;
		}
		return longitudeLen2;
	}
	*/
	/**
	 * Builds a strip of triangles that is between two cross sectional
	 * profile at two center line points.
	 * 
	 * @param crossSections cross sections at center line points, containing 
	 * 		material information
	 * @param centerLine center line points that needs to be consistent with
	 * 		crossSections
	 * @param format disk access
	 * 
	 * @param absPos the index of first center line point in the 
	 * 		entire center line
	 * @param relativePos the index of first center line point within
	 * 		the current mesh
	 * 
	 * @param minWhiteIndex left border white marker index 
	 * @param maxWhiteIndex right border white marker index 
	 * @param longitudeLen1 the distance between the first center line point
	 * 		from the beginning of the center line
	 * @param longitudeLen2 the distance between the first center line point
	 * 		from the beginning of the center line
	 * @param textLim texture size
	 */
	/*
	private static void buildStrip(ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, Formatter format,
			int absPos, int relativePos, int minWhiteIndex, int maxWhiteIndex,
			float longitudeLen1, float longitudeLen2, float textLim){
		float texCoordX[] = new float[3];
		float texCoordY[] = new float[3];
		int vs[] = new int[3];
		int b[] = new int[3];

		ArrayList<RoadVector> list1 = crossSections.get(absPos);
		ArrayList<RoadVector> list2 = crossSections.get(absPos+1);

		// find the left most points of the two cross section.
		// they will be used to find texture coordinates
		RoadVector left1 = list1.get(0), right1 = list1.get(list1.size()-1);
		RoadVector left2 = list2.get(0), right2 = list2.get(list2.size()-1);

		// finding triangles, comprising the strip between the two 
		// cross sections
		for(int j=0;j<list1.size()-1;j++){
			RoadVector v11 = list1.get(j);
			RoadVector v12 = list1.get(j+1);
			RoadVector v21 = list2.get(j);
			RoadVector v22 = list2.get(j+1);
			
			int type = v11.crossSectionType;
				
			// this may be awkard now, it tries to realize dotted markers
			// note each center line point has recorded whether or not its 
			// lane divider areas should be white markers or pavement
			// 
			// so when a cross section point's type is white_maker. it actually means
			// lane dividor. In such a case, the type of centerline point will be used.
			if(v11.crossSectionType == ModelGenConsts.WHITE_MARKER &&
				 (j>=minWhiteIndex && j<=maxWhiteIndex)){
					type = centerLine.get(absPos).crossSectionType;
			}
			
			//*** finding the two triangles comprising the current rectangle ***
			
			// finds the indices of the vertices
			vs[0]=relativePos*list1.size()+j; vs[1]=vs[0]+1; 
			vs[2]=(relativePos+1)*list1.size()+j;
			
			// find the x texture coordinates 
			// the base is the left most point  
			texCoordX[0] = RoadVector.subtract(v11, left1).magnitude2d()/
				RoadVector.subtract(right1, left1).magnitude2d();
			texCoordX[1] = RoadVector.subtract(v12, left1).magnitude2d()/
				RoadVector.subtract(right1, left1).magnitude2d();
			texCoordX[2] = RoadVector.subtract(v21, left2).magnitude2d()/
				RoadVector.subtract(right2, left2).magnitude2d();
			
			// find the y texture coordinates
			// the base is where the current mesh begins longitudely
			b[0] = (int)(longitudeLen1/textLim);
			b[1] = (int)(longitudeLen1/textLim);
			b[2] = (int)(longitudeLen2/textLim); 
			texCoordY[0] = longitudeLen1/textLim - b[0];
			texCoordY[1] = longitudeLen1/textLim - b[1];
			texCoordY[2] = longitudeLen2/textLim - b[2];
		
			validateTexCoordY(texCoordY,b);
			saveTriangle(format, vs, texCoordX, texCoordY, type);
			
			// finds the indices of the vertices
			vs[0]=relativePos*list1.size()+j+1; 
			vs[1]=(relativePos+1)*list1.size()+j+1; 
			vs[2]=vs[1]-1;

			// find the x texture coordinates 
			// the base is the left most point  
			texCoordX[0] = RoadVector.subtract(v12, left1).magnitude2d()/
				RoadVector.subtract(right1, left1).magnitude2d();
			texCoordX[1] = RoadVector.subtract(v22, left2).magnitude2d()/
				RoadVector.subtract(right2, left2).magnitude2d();
			texCoordX[2] = RoadVector.subtract(v21, left2).magnitude2d()/
				RoadVector.subtract(right2, left2).magnitude2d();

			// find the y texture coordinates
			// the base is where the current mesh begins longitudely
			b[0] = (int)(longitudeLen1/textLim); 
			b[1] = (int)(longitudeLen2/textLim);
			b[2] = (int)(longitudeLen2/textLim);
			texCoordY[0] = longitudeLen1/textLim - b[0];
			texCoordY[1] = longitudeLen2/textLim - b[1];
			texCoordY[2] = longitudeLen2/textLim - b[2];
			
			validateTexCoordY(texCoordY,b);

			saveTriangle(format, vs, texCoordX, texCoordY, type);
		}

	}
	*/
	/**
	 * Saves an individual triangle.
	 * 
	 * The texture coordinates (texCoordX, texCoordY) are relative to
	 * the top-left corner of the texture area. That top-left corner's
	 * coordinates are stored in the xml file for the model
	 * 
	 * @param format file access
	 * @param vs vertex indices for building the triangle
	 * @param texCoordX  
	 * @param texCoordY
	 * @param type texture type
	 */
	/*
	private static void saveTriangle(Formatter format, int[] vs,
			float[] texCoordX, float[] texCoordY, int type) {

		format.format("%d\n", type);
		for(int i=0;i<3;i++){
			format.format("%d\t%.5f\t%.5f\n",
					vs[i], texCoordX[i], texCoordY[i]);
		}
		format.format("\n");
	}
	*/
	/**
	 * Validates y texture coordinates at triangle points. 
	 * 
	 * This method is necessary when part of the three triangle points hit
	 * the border of a texture. If it's slightly greater than 1.0, it will 
	 * screw texture map, so it has to be set down to 1.0.
	 * 
	 * @param y texture coordinates
	 * @param b ceiling(distance of points/texture size)
	 */
	private static void validateTexCoordY(float [] y, int [] b){
		if(b[0]==b[1] && b[0]==b[2]){
			return;
		}
		
		if((b[0]>b[1] && b[0]>b[2]) && y[0]<ModelGenConsts.SMALL_FLOAT){
			y[0] = 1.0f;
		}
		
		if((b[1]>b[0] && b[1]>b[2]) && y[1]<ModelGenConsts.SMALL_FLOAT){
			y[1] = 1.0f;
		}

		if((b[2]>b[0] && b[2]>b[1]) && y[2]<ModelGenConsts.SMALL_FLOAT){
			y[2] = 1.0f;
		}

		if((b[0]<b[1] && b[0]<b[2]) && y[0]>(1.0f-ModelGenConsts.SMALL_FLOAT)){
			y[1] = y[2] = 1.0f;
		}
		
		if((b[1]<b[0] && b[1]<b[2]) && y[1]>(1.0f-ModelGenConsts.SMALL_FLOAT)){
			y[0] = y[2] = 1.0f;
		}

		if((b[2]<b[0] && b[2]<b[1]) && y[2]>(1.0f-ModelGenConsts.SMALL_FLOAT)){
			y[0] = y[1] = 1.0f;
		}
	}

	
	/**
	 * Saves the surrounding terrain triangles.
	 * 
	 * @param terrain a list of terrain triangle
	 * @param format file access
	 */
	/*
	public static void saveTerrainTriangles(
			ArrayList<RoadVector> terrain, Formatter format) {
		
		int numOfTriangles = terrain.size()/3;
		format.format("%d\n", numOfTriangles);
		for(int i=0;i<numOfTriangles;i++){
			RoadVector v1, v2, v3;
			v1 = terrain.get(3*i+2);
			v2 = terrain.get(3*i+1);
			v3 = terrain.get(3*i);
			
			format.format("%.3f\t%.3f\t%.3f\n", 
					v1.getX(),v1.getY(),v1.getZ());
			format.format("%.3f\t%.3f\t%.3f\n", 
					v2.getX(),v2.getY(),v2.getZ());
			format.format("%.3f\t%.3f\t%.3f\n", 
					v3.getX(),v3.getY(),v3.getZ());
		}
		
	}
	*/

	/**
	 * Changes cross sections to approcximate super elevation.
	 * 
	 * Pre-condition: anchors must be placed along the center line
	 * to flag runout begins, runoff begins, and runoff ends.
	 * 
	 * This method has some hardcoded numbers which ain't good.
	 * 
	 * @param roadPoints center line points
	 * @param crossSections cross sectional profile points 
	 * @param xsec cross section
	 */
	public static void addSuperElevations(ArrayList<RoadVector> roadPoints,
			ArrayList<ArrayList<RoadVector>> crossSections, CrossSection xsec) {

		// 1. find the range of road point indices
		int numOfXsecPoints = crossSections.get(0).size();
		int leftStart, leftEnd, rightStart, rightEnd;
		// if shoulder exists, it is not part of super elevation
		// this assumes shoulders are the farthest sections
		if(Float.compare(xsec.shoulderWidth, 0)!=0){
			leftEnd = 1;
			rightEnd = numOfXsecPoints - 2;

		}else{
			leftEnd = 0;
			rightEnd = numOfXsecPoints - 1;

		}

		// the middle of a cross section profile is 
		// - marker - pavement - [ median ] - pavement - marker 
		if(Float.compare(xsec.medianWidth, 0)!=0){
			leftStart = numOfXsecPoints/2 - 3;
			rightStart = numOfXsecPoints/2 + 2;			
		}else{
			leftStart = numOfXsecPoints/2 - 2;
			rightStart = numOfXsecPoints/2 + 1;			
		}
		
		// 2. find horizontal curves
		int curveStart=-1, curveEnd=-1;
		int searchStarts = 0;
		HorizontalCurve hCurve;
		// 2.a find where the next horizontal curve starts and ends
		while(curveEnd<roadPoints.size()){
			hCurve = null;
			/*
			 * Each super-elevation (two total) change is marked by 
			 * the following points
			 * 1. RUNOUT_BEGINS
			 * 2. RUNOFF_BEGINS
			 * 3. RUNOFF_ENDS
			 */
			int outerIndices[] = new int[6];
			
			/*
			 * Where should the inner side should beginning changing
			 * to approximate the super elevation  
			 */
			int innerIndices[] = new int[2];
			for(int i=searchStarts;i<roadPoints.size();i++){
				if(hCurve==null && roadPoints.get(i).hCurve!=null){
					curveStart = i;
					hCurve = roadPoints.get(i).hCurve;
				}else if(hCurve!=null && (roadPoints.get(i).hCurve!=hCurve 
						|| i==roadPoints.size()-1)){
					curveEnd = i-1;
					searchStarts = i;
					break;
				}
			}
			
			if(hCurve==null){
				break; // failed to find a horizontal curve
			}else{
				int count1 = 0, count2 = 0;;
				for(int i=curveStart;i<=curveEnd;i++){
					RoadVector v = roadPoints.get(i);
					if(v.isAnchorSet(ModelGenConsts.RUNOFF_BEGINS) ||
							v.isAnchorSet(ModelGenConsts.RUNOUT_BEGINS) ||
							v.isAnchorSet(ModelGenConsts.RUNOFF_ENDS)){
						
						outerIndices[count1++] = i;
					}
					
					if(v.isAnchorSet(ModelGenConsts.LEVEL_OTHER_SIDE)){
						innerIndices[count2++] = i;
					}
				}
			}
			
			// 2.b figure out the direction of the curve
			int upStart, upEnd, downStart, downEnd;
			if(leftTurn(hCurve)){
				upStart = rightStart;
				upEnd = rightEnd;
				downStart = leftStart;
				downEnd = leftEnd;
			}else{
				upStart = leftStart;
				upEnd = leftEnd;
				downStart = rightStart;
				downEnd = rightEnd;			
			}
			
			// 2.c do runout to flatten up to zero
			changeLaneSlopeWithIndices(outerIndices[0], outerIndices[1],
					upStart, upEnd, -xsec.laneSlope, 0.0f,
					roadPoints, crossSections);

			changeLaneSlopeWithIndices(outerIndices[5], outerIndices[4],
					upStart, upEnd, -xsec.laneSlope, 0.0f,
					roadPoints, crossSections);

			// 2.d do runoff 
			changeLaneSlopeWithIndices(outerIndices[1], outerIndices[2],
					upStart, upEnd, 0.0f, hCurve.superElevation,
					roadPoints, crossSections);

			changeLaneSlopeWithIndices(outerIndices[4], outerIndices[3],
					upStart, upEnd, 0.0f, hCurve.superElevation,
					roadPoints, crossSections);

			changeLaneSlopeWithIndices(outerIndices[2], outerIndices[3],
					upStart, upEnd, hCurve.superElevation, hCurve.superElevation,
					roadPoints, crossSections);	
			
			// 2.e do runoff to the inner side
			if(Float.compare(xsec.laneSlope, hCurve.superElevation)<0){
				changeLaneSlopeWithIndices(innerIndices[0], outerIndices[2],
						downStart, downEnd, 
						-xsec.laneSlope, -hCurve.superElevation,
						roadPoints, crossSections);

				changeLaneSlopeWithIndices(innerIndices[1], outerIndices[3],
						downStart, downEnd, 
						-xsec.laneSlope, -hCurve.superElevation,
						roadPoints, crossSections);
				
				changeLaneSlopeWithIndices(outerIndices[2], outerIndices[3],
						downStart, downEnd, 
						-hCurve.superElevation, -hCurve.superElevation,
						roadPoints, crossSections);	

			}
			
		}
	}
	
	/**
	 * Changes some cross section profiles to approximate the super-elevations.
	 * 
	 * The raise/drop of super elevation is linear.
	 * 
	 * @param curveStart starting index along the center line 
	 * @param curveEnd end index along the center line
	 * @param xsecStart starting index along cross section profile
	 * @param xsecEnd end index along cross section profile
	 * @param baseSlope 
	 * @param newSlope
	 * @param roadPoints center line points
	 * @param crossSections cross section profile points
	 */
	private static void changeLaneSlopeWithIndices(int curveStart, int curveEnd, 
			int xsecStart, int xsecEnd, float baseSlope, float newSlope, 
			ArrayList<RoadVector> roadPoints,
			ArrayList<ArrayList<RoadVector>> crossSections){
		
		float distance = 0;
		RoadVector v1, v2;
		
		int direction = (curveEnd-curveStart)/Math.abs(curveEnd-curveStart);
		
		v1 = roadPoints.get(curveStart);
		for(int i=1;i<=Math.abs(curveEnd-curveStart);i++){
			v2 = roadPoints.get(curveStart+i*direction);

			distance += RoadVector.subtract(v1, v2).magnitude();
			
			v1 = v2;
		}
		
		float changeRate = (newSlope-baseSlope)/distance;
		
		distance = 0;
		v1 = roadPoints.get(curveStart);
		for(int i=1;i<=Math.abs(curveEnd-curveStart);i++){
			v2 = roadPoints.get(curveStart+i*direction);
			
			distance += RoadVector.subtract(v2, v1).magnitude();
			
			RoadVector first = crossSections.get(curveStart+i*direction).
				get(xsecStart);
			for(int k=0;k<Math.abs(xsecEnd-xsecStart)+1;k++){
				RoadVector v;
				if(xsecEnd>xsecStart)
					v = crossSections.get(curveStart+i*direction).get(xsecStart+k);
				else 
					v = crossSections.get(curveStart+i*direction).get(xsecStart-k);
				
				float mag = RoadVector.subtract(v, first).magnitude2d(); 
				float dz = mag * (baseSlope + changeRate*distance)/100;
				v.setZ(roadPoints.get(curveStart+i*direction).getZ()+dz);
			}
			
			v1 = v2;
		}
		
	}

	/**
	 * Analyzes a horizontal curve and returns if the curve turns left or right
	 * along p1 --> pi --> p2
	 * @param hCurve
	 * @return
	 */
	private static boolean leftTurn(HorizontalCurve hCurve){
		RoadVector p1 = hCurve.p1;
		RoadVector pi = hCurve.pi;
		RoadVector p2 = hCurve.p2;
		
		RoadVector p1TOpi = RoadVector.subtract(pi, p1);
		RoadVector piTOp2 = RoadVector.subtract(p2, pi);
		RoadVector cross = RoadVector.cross(p1TOpi, piTOp2);
		
		return Float.compare(cross.getZ(), 0)>0;
	}
	
	/**
	 * Finds a circular surrounding terrain to surround the road.
	 * 
	 * Assumes the road "network" has no holes.
	 * 
	 * @param xsecs all the cross section profiles
	 * @return a list of triangles, comprising the terrain
	 */
	/*
	public static ArrayList<RoadVector> findSimpleTerrain(
			ArrayList<ArrayList<RoadVector>> xsecs){
		
		// step 0. initializes the triangulator
		TriangulatorAdapter triangulator = new TriangulatorAdapter();
		
		// step 1. adds the points for the surround circle which is also the
		// outer loop
		Circle2f surroundCircle2d =
			ModelGenUtil.findSurroundCircle2d(xsecs, 
					ModelGenConsts.TERRAIN_SCALE);

		float unitAngle = (float) (2*Math.PI/
				BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN);
		for(int i=0;i<BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN;i++){
			RoadVector v = new RoadVector(
					surroundCircle2d.center.x + 
						(float)Math.cos(unitAngle*i)*surroundCircle2d.radius,
					surroundCircle2d.center.y + 
						(float)Math.sin(unitAngle*i)*surroundCircle2d.radius,
					0.0f);
			triangulator.addVertex(v);
		}
		
		// step 2. adds the points for the inner hole which is 
		// the polygon outlining the road
		ArrayList<RoadVector> perimeter = 
			ModelGenUtil.findSurroundPoly(xsecs);
		for(int i=0;i<perimeter.size();i++){
			triangulator.addVertex(perimeter.get(i));
		}
		
        // step 3. adds edges for the outer loop
        for(int i=0;i<BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN;i++){
        	if(i!=BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN-1)
        		triangulator.addEdge(i,i+1);
        	else
        		triangulator.addEdge(i,0);
        }
        
        // step 4. adds edges for the inner hole
        for(int i=0;i<perimeter.size();i++){
        	if(i!=perimeter.size()-1)
        		triangulator.addEdge(BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN+i,
        				BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN+i+1);
        	else{
        		triangulator.addEdge(
        				BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN+i,
        				BasicTerrain.NUM_OF_EDGES_FOR_TERRAIN);
        	}
        }
                
        // step 5. triangulates the polygons
        ArrayList<RoadVector> result = triangulator.triangulate();
                
        return result;
	}
	 */

	/**
	 * Extends a road along its center line.
	 * <p>
	 * It works by first selecting two center line points (their indices index1 and index2).
	 * Then it defines which part of the cross sectional profile will be extended 
	 * (xsecStart and xsecEnd). At last, it gives an amount of extension, which is to
	 * extends the segment defined by (index1-->index2) by the amount.
	 * 
	 * @param road
	 * @param index1
	 * @param index2
	 * @param xsecStart
	 * @param xsecEnd
	 * @param ext
	 * @return
	 */
	public static ArrayList<ArrayList<RoadVector>> extendRoad(
			BasicRoad road, int index1, int index2, int xsecStart, 
			int xsecEnd, float ext) {
		return extendRoad(road.getXsecPts(), index1, 
				index2, xsecStart, xsecEnd, ext);
	}

	/**
	 * Extends a road along its center line.
	 * <p>
	 * It works by first selecting two center line points (their indices index1 and index2).
	 * Then it defines which part of the cross sectional profile will be extended 
	 * (xsecStart and xsecEnd). At last, it gives a point, which is to
	 * extends the segment defined by (index1-->index2) to that point.
	 * 
	 * @param road
	 * @param index1
	 * @param index2
	 * @param xsecStart
	 * @param xsecEnd
	 * @param extPt to where to extend the road
	 * @return
	 */
	public static ArrayList<ArrayList<RoadVector>> extendRoad(
			BasicRoad road, int index1, int index2, int xsecStart, 
			int xsecEnd, RoadVector extPt) {
		
		RoadVector centerPt2 = road.getCurvePts().get(index2);
		float ext = RoadVector.subtract(extPt, centerPt2).magnitude2d();
		
		return extendRoad(road, index1, index2, xsecStart, xsecEnd, ext);
	}

	/**
	 * Extends a road along its center line.
	 * <p>
	 * It works by first selecting two center line points (their indices index1 and index2).
	 * Then it defines which part of the cross sectional profile will be extended 
	 * (xsecStart and xsecEnd). At last, it gives an amount of extension, which is to
	 * extends the segment defined by (index1-->index2) by the amount.
	 * 
	 * @param xsecs cross sectional profiles at center lines
	 * @param index1 
	 * @param index2
	 * @param xsecStart
	 * @param xsecEnd
	 * @param ext
	 * @return
	 */
	public static ArrayList<ArrayList<RoadVector>> extendRoad(
			ArrayList<ArrayList<RoadVector>> xsecs, int index1, int index2, 
			int xsecStart,	int xsecEnd, float ext) {
		ArrayList<RoadVector> xsec1 = xsecs.get(index1);
		ArrayList<RoadVector> xsec2 = xsecs.get(index2);
		
		RoadVector xsecPt1 = xsecs.get(index1).get(0);
		RoadVector xsecPt2 = xsecs.get(index2).get(0);
		
		float ratio = 1 + Math.abs(	ext/
				RoadVector.subtract(xsecPt1, xsecPt2).magnitude2d()
		);
		
		ArrayList<ArrayList<RoadVector>> extensions = 
			new ArrayList<ArrayList<RoadVector>>();
		
		ArrayList<RoadVector> _xsec1 = new ArrayList<RoadVector>();
		for(int i=xsecStart; i<=xsecEnd; i++){
			_xsec1.add(xsec2.get(i).duplicate());
		}
		extensions.add(_xsec1);
		
		ArrayList<RoadVector> _xsec2 = new ArrayList<RoadVector>();
		for(int i=xsecStart; i<=xsecEnd; i++){
			RoadVector v = xsec1.get(i).duplicate();
			RoadVector v1 = xsec1.get(i);
			RoadVector v2 = xsec2.get(i);
			
			v.setX(v.getX()+(v2.getX()-v1.getX())*ratio);
			v.setY(v.getY()+(v2.getY()-v1.getY())*ratio);
			v.setZ(v.getZ()+(v2.getZ()-v1.getZ())*ratio);
			_xsec2.add(v);
		}
		extensions.add(_xsec2);

		return extensions;

	}
	
	/**
	 * For each road center line point, finds its lateral cross-sectional profile.
	 * <p>
	 * It is in essence to "draw" the current cross section profile at individual
	 * center line point.
	 * 
	 * @param centerLine a map where each element is a horizontal and/or vertical 
	 * 			curve.
	 * @return
	 */
	public static NavigableMap<Integer, ArrayList<ArrayList<RoadVector>>> 
			findCrossSections(
					NavigableMap<Integer, ArrayList<RoadVector>> centerLine,
					CrossSection xsec
			){
		NavigableMap<Integer, ArrayList<ArrayList<RoadVector>>> allXsecs = 
			new TreeMap<Integer, ArrayList<ArrayList<RoadVector>>>();
	
		Integer key = centerLine.firstKey();
		ArrayList<RoadVector> segment = centerLine.get(key);
		RoadVector v0=null, v1=null, v2=null;
	
		int count = 0;
		while(key!=null){	
			
			ArrayList<ArrayList<RoadVector>> segmentXsecs = 
					new ArrayList<ArrayList<RoadVector>>();
			v1 = segment.get(0);
			for(int i=0;i<segment.size();i++){
				v2 = CollectionUtil.get(centerLine,count+i+1);
											
				segmentXsecs.add(findCrossSectionMid(v0,v1,v2, xsec));
				
				v0 = v1;
				v1 = v2;
			}			
			
			allXsecs.put(key, segmentXsecs);
			
			count+=segment.size();
			
			key = centerLine.higherKey(key);
			segment = (key==null)?null:centerLine.get(key);
		}
		
		return allXsecs;

	}
	
	/**
	 * Given three center line points, finds the longitude direction at the middle 
	 * point and "draw" the cross sectional profile at the middle point.
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @param xsec 
	 * @return
	 */
	private static ArrayList<RoadVector> findCrossSectionMid(RoadVector v0, 
			RoadVector v1, RoadVector v2, CrossSection xsec){
		// finds the direction of the current road segment
		float direction;
		if(v0!=null && v2!=null){
			float direction1 = RoadVector.subtract(v2, v1).findDirectionXY();
			float direction2 = RoadVector.subtract(v1, v0).findDirectionXY();
			if( (direction1 > 1.5*Math.PI && direction2 < 0.5*Math.PI) ||
					(direction2 > 1.5*Math.PI && direction1 < 0.5*Math.PI)){
				direction = (float) ((direction1+direction2)/2 - Math.PI);
			}else{
				direction = (direction1+direction2)/2;
			}
		}else if(v0==null){
			direction = RoadVector.subtract(v2, v1).findDirectionXY();
		}else{
			direction = RoadVector.subtract(v1, v0).findDirectionXY();
		}
		
		// finds the left and right direction at this 
		// center line point
		float leftDir = (float) (direction + Math.PI/2);
		float rightDir = (float) (direction - Math.PI/2);
		
		// finds the unit vectors for the left and right directions
		RoadVector left2d = new RoadVector((float)Math.cos(leftDir), 
				(float)Math.sin(leftDir),0);
		RoadVector right2d = new RoadVector((float)Math.cos(rightDir), 
				(float)Math.sin(rightDir),0);

		return findCrossSectionOp(v1, left2d, right2d, xsec);
	}
	
	/**
	 * Given a center line point and the left and right direction at this point, 
	 * "draws" the cross sectional profile at the this point.
	 * 
	 * @param v
	 * @param left
	 * @param right
	 * @param xsec 
	 * @return
	 */
	public static ArrayList<RoadVector> findCrossSectionOp(RoadVector v,
			RoadVector left, RoadVector right, CrossSection xsec
			){
		ArrayList<RoadVector> crossSec = new ArrayList<RoadVector>();
		
		// initially, the cross section is considered zero width
		// therefore both left and right most points are v which
		// is the input center line point
		RoadVector leftMost = v;
		RoadVector rightMost = v;
		
		/* add points for median */
		if(Float.compare(xsec.medianWidth, 0)!=0){
			leftMost = incrementXsec(crossSec, leftMost, true, left, 
					0, xsec.medianWidth/2, ModelGenConsts.MEDIAN);

			rightMost = incrementXsec(crossSec, rightMost, false, right,
					0, xsec.medianWidth/2, ModelGenConsts.SOLID_WHITE_MARKER_RIGHT);
		}else{
			incrementXsec(crossSec, v, false, left,
					0, 0, ModelGenConsts.SOLID_WHITE_MARKER_RIGHT);
		}
		
		// the median is sandwiched by MARKER_GAP/2 pavement and
		// MARKER_WIDTH yellow marker
		leftMost = incrementXsec(crossSec, leftMost, true, left, 0, 
				2*DesignConsts.MARKER_WIDTH, 
				ModelGenConsts.SOLID_WHITE_MARKER_LEFT);
		
		rightMost = incrementXsec(crossSec, rightMost, false, right, 0, 
				2*DesignConsts.MARKER_WIDTH, 
				ModelGenConsts.ROAD_SURFACE);
		
		/* add points for lanes */
		
		leftMost = incrementXsec(crossSec, leftMost, true, left, -xsec.laneSlope, 
				xsec.numOfLanes*xsec.laneWidth + (xsec.numOfLanes-1)*DesignConsts.MARKER_WIDTH, 
				ModelGenConsts.ROAD_SURFACE);
		
		rightMost = incrementXsec(crossSec, rightMost, false, right, -xsec.laneSlope, 
				xsec.numOfLanes*xsec.laneWidth + (xsec.numOfLanes-1)*DesignConsts.MARKER_WIDTH,
				ModelGenConsts.SOLID_WHITE_MARKER_LEFT);
		
		// add a little paved region before shoulder
		leftMost = incrementXsec(crossSec, leftMost, true, left,
				-xsec.laneSlope, 2*DesignConsts.MARKER_WIDTH, 
				ModelGenConsts.SOLID_WHITE_MARKER_RIGHT);

		rightMost = incrementXsec(crossSec, rightMost, false, right,
				-xsec.laneSlope, 2*DesignConsts.MARKER_WIDTH, 
				ModelGenConsts.SHOULDER);
		
		/* add points for shoulders */
		if(Float.compare(xsec.shoulderWidth, 0)!=0){
			leftMost = incrementXsec(crossSec, leftMost, true, left,
					-xsec.shoulderSlope, xsec.shoulderWidth, ModelGenConsts.SHOULDER);

			rightMost = incrementXsec(crossSec, rightMost, false, right,
					-xsec.shoulderSlope, xsec.shoulderWidth, ModelGenConsts.SHOULDER);
		}
		
		return crossSec;
	}
	
	/**
	 * Adds one point to the cross section point list.
	 * 
	 * @param crosSec cross section point list
	 * @param previous last point based on which a new point will be found
	 * @param left if added to the left
	 * @param direction2d 
	 * @param slope
	 * @param width
	 * @param type the type of the material to the right of the new point
	 * @return
	 */
	private static RoadVector incrementXsec(ArrayList<RoadVector> crosSec, RoadVector previous, 
			boolean left, RoadVector direction2d, float slope, 
			float width, int type){
		RoadVector next = RoadVector.add(previous, RoadVector.multi(direction2d, width));
		next.setZ(previous.getZ() + width*slope/100);
		next.crossSectionType = type;
		if(left){
			crosSec.add(0, next);
		}else{
			crosSec.add(next);
		}
		return next;
	}
	
	
	/**
	 * Adds one point to the cross section point list.
	 * 
	 * @param crosSec cross section point list
	 * @param previous last point based on which a new point will be found
	 * @param left if added to the left
	 * @param direction2d 
	 * @param slope
	 * @param width
	 * @param type the type of the material to the right of the new point
	 * @return
	 */
	/*
	private static RoadVector incrementXsec(ArrayList<RoadVector> crosSec, RoadVector previous, 
			boolean left, RoadVector direction2d, float slope, 
			float width, int type){
		RoadVector next = RoadVector.add(previous, RoadVector.multi(direction2d, width));
		next.setZ(previous.getZ() + width*slope/100);
		next.crossSectionType = type;
		if(left){
			crosSec.add(0, next);
		}else{
			crosSec.add(next);
		}
		return next;
	}
	*/

}


