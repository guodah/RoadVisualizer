package com.sim.curves;

import java.awt.Dimension;

import java.awt.Toolkit;
import java.util.*;

import javax.swing.JFrame;

import com.sim.core.basic.ModelGenConsts;
import com.sim.geometries.Vector23f;

/**
 * Assuming the intersection of the directions is at (0,0), this class
 * is able to calculate the sequence of points that outline a three-centered
 * curve.
 * <p>
 * A three-centered curve contains three co-tangent arcs which need to be defined 
 * by three corresponding radii. The two offsets are how much the middle curve moves
 * perpendicular to the directions.
 * 
 * @author Dahai Guo
 *
 */
public class ThreeCenteredCurve {
	public float [] radii = new float[3];
	public float [] offsets = new float[2];
	public Vector23f direction1; // needs to be unit2d
	public Vector23f direction2; // needs to be unit2d

	/**
	 * Without validation of parameters.
	 * 
	 * @param radii
	 * @param direction1
	 * @param direction2
	 */
	public ThreeCenteredCurve(float [] radii, float [] offsets, 
			Vector23f direction1, Vector23f direction2){
		this.radii[0] = radii[0];
		this.radii[1] = radii[1];
		this.radii[2] = radii[2];
		this.offsets[0] = offsets[0];
		this.offsets[1] = offsets[1];
		this.direction1 = direction1.unit2d();
		this.direction2 = direction2.unit2d();
	}
	
	
	public ArrayList<Vector23f> findCurvePoints(){
		// find angle of the beginning arc
		float theta0 = (float) Math.acos((radii[0]-offsets[0])/radii[0]);
		
		// find angle of the ending arc
		float theta2 = (float) Math.acos((radii[2]-offsets[1])/radii[2]);
		
		// find angle of the middle arc 
	 	float theta1 = findMidAngle(theta0, theta2);
	 	
	 	// find the beginning and end points of the turn
	 	ArrayList<Vector23f> pts = findTurnPts(
	 			new float[]{theta0, theta1, theta2});
	 	float sides[] = findSides(pts);
	 	
	 	ArrayList<Vector23f> curvePts = findCurvePoints(
	 			new float[]{theta0, theta1, theta2}, sides);
	 	
	// 	test(curvePts);
	 	
	 	return curvePts;
	}

	private ArrayList<Vector23f> findCurvePoints(float[] theta, float[] sides) {
		ArrayList<Vector23f> result = new ArrayList<Vector23f>();
	//	result.addAll(debugPts);
		
		// find the first point;
		Vector23f first = Vector23f.add(
			new Vector23f(0,0,0), 
			Vector23f.multi(
				direction1, 
				(-1)*sides[0]
			)
		);
		
		Vector23f second = Vector23f.add(
			new Vector23f(0,0,0), 
			Vector23f.multi(
				direction2, 
				sides[1]
			)
		);
		
		float dis = Vector23f.subtract(first, second).magnitude2d();
	//	System.out.printf("DEBUG: dis = %f\n", dis);
		
		// find the first center angle
		float centerAngle = (float)(direction1.findDirectionXY()+Math.PI/2);

		// find curve points
		for(int i=0;i<theta.length;i++){
			float unitAngle = ModelGenConsts.RESOLUTION/radii[i];
			int n = (int)(theta[i]/unitAngle);

			// find the center
			Vector23f center = Vector23f.add(first, 
				Vector23f.multi(
					new Vector23f((float)(Math.cos(centerAngle-Math.PI)),
						(float)(Math.sin(centerAngle-Math.PI)),
						0.0f
					),
					radii[i]
				)      
			);
			
		//	result.add(center);
			
			for(int j=0;j<n;j++){
				Vector23f pts = Vector23f.add(center, 
					new Vector23f(
						(float)Math.cos(centerAngle-j*unitAngle)*radii[i],
						(float)Math.sin(centerAngle-j*unitAngle)*radii[i],
						0.0f
					)
				);
				result.add(pts);
				
				if(j==0){
		//			result.add(center);
		//			result.add(pts);
				}
			}
			first = Vector23f.add(center, 
				new Vector23f(
						(float)Math.cos(centerAngle-theta[i])*radii[i],
						(float)Math.sin(centerAngle-theta[i])*radii[i],
						0.0f
				)
			); 
		//	result.add(first);
			
			centerAngle = Vector23f.subtract(first, center).findDirectionXY();
		}
		
	//	result.add(first);
		result.add(second);
		return result;
	}

	private float[] findSides(ArrayList<Vector23f> pts) {
		Vector23f third = Vector23f.subtract(pts.get(1), pts.get(0));
		
	//	System.out.printf("DEBUG: %f\n", third.magnitude2d());
		
		float alpha0 = (float)Math.acos(Vector23f.dot(third.unit2d(), direction1));
		float alpha1 = (float)Math.acos(Vector23f.dot(third.unit2d(), direction2));
		float alpha2 = (float) (Math.PI - 
				Math.acos(Vector23f.dot(direction1, direction2)));
	//	System.out.printf("alpha0=%f, alpha1=%f, alpha2=%f\n", alpha0, alpha1, alpha2);
		
		float [] result = new float[2];
		result[0] = (float) (third.magnitude2d()*
				Math.sin(alpha1)/Math.sin(alpha2));
		result[1] = (float) (third.magnitude2d()*
				Math.sin(alpha0)/Math.sin(alpha2));
		return result;
	}

	ArrayList<Vector23f> debugPts = new ArrayList<Vector23f>();
	
	private ArrayList<Vector23f> findTurnPts(float[] theta) {
		float angle = 0;
		float tangentLens[] = new float[3];
		for(int i=0;i<3;i++){
			tangentLens[i] = (float) (radii[i]*Math.tan(theta[i]/2)); 
		}
		
		Vector23f tangentDirs[] = new Vector23f[4];
		tangentDirs[0] = direction1.duplicate();
		for(int i=1;i<=3;i++){
			angle=tangentDirs[i-1].findDirectionXY()-theta[i-1];
			tangentDirs[i] = new Vector23f((float)Math.cos(angle), 
				(float)Math.sin(angle),0);
		}
		
		// assumes the first arc starts at (0,0)
		Vector23f pts[] = new Vector23f[5];
		pts[0] = new Vector23f(0,0,0);
	//	pts[0] = new Vector3f(-99.928f, -79.943f, 0.000f);
		for(int i=1;i<=4;i++){
			float len = 0;
			switch(i){
			case 1:
				len = tangentLens[0];
				break;
			case 4:
				len = tangentLens[2];
				break;
			case 2:
				len = tangentLens[0] + tangentLens[1];
				break;
			case 3:
				len = tangentLens[1] + tangentLens[2];
				break;
			}
			pts[i] = Vector23f.add(pts[i-1], 
					Vector23f.multi(tangentDirs[i-1], len));
		}
		
		ArrayList<Vector23f> result = new ArrayList<Vector23f>();
		result.add(pts[0]);
		result.add(pts[4]);
		
		return result;
	}

	private float findMidAngle(float theta0, float theta2) {
		float dot = Vector23f.dot(direction1, direction2)/
				(direction1.magnitude()*direction2.magnitude());
		float angle = (float) Math.acos(dot);
		return angle - theta0 - theta2;
		/*
		float tangent0 = (float) (direction1.findDirectionXY()-theta0);
		float tangent2 = (float) (direction2.reverse().
				findDirectionXY()+theta2);
		
		Vector3f tangent0Vec = new Vector3f((float)Math.cos(tangent0), 
				(float)Math.sin(tangent0), 0);
		Vector3f tangent2Vec = new Vector3f((float)Math.cos(tangent2), 
				(float)Math.sin(tangent2), 0);
	//	System.out.printf("tangentOVec=%s, tangent2Vec=%s\n", tangent0Vec, tangent2Vec);
		
		return (float)(Math.PI-
				Math.acos(Vector3f.dot2d(tangent0Vec, tangent2Vec)));
		*/
	}
	
	public static void main(String args[]){
	}
}
