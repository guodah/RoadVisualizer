package com.sim.geometries;

import java.awt.Color;

import com.sim.curves.HorizontalCurve;
import com.sim.curves.VerticalCurve;
import com.sim.util.CompareUtil;
import com.sim.util.Duplicatable;



/**
 * This class is used to represent 2D or 3D vectors.
 * <p>
 * Its capability is three dimensions. But it has methods to 
 * zero or ignore the z field to realize 2D vector operations. 
 * 
 * @author Dahai Guo
 *
 */
public class Vector23f implements Geom3f, Duplicatable {
	
	public float x;
	public float y;
	public float z;
	
//	public int roadDesignType = DebugUtil.ON_STRAIGHT_SEGMENT;
//	public int crossSectionType;
//	private int anchorType = ModelGenConsts.NOT_ANCHOR;
//	public HorizontalCurve hCurve = null; 
//	public VerticalCurve vCurve = null;
		
	
	/**
	 * Does not validate arguments
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector23f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Negates x, y, z.
	 * @return
	 */
	public Vector23f reverse(){
		return new Vector23f(-x,-y,-z);
	}
	
	/**
	 * Returns "(x,y,z)"
	 */
	public String toString(){
		return String.format("(%.3f, %.3f, %.3f)",x,y,z);
	}
	
	public static Vector23f add(Vector23f v1, Vector23f v2){
		return new Vector23f(v1.x+v2.x,
				v1.y+v2.y, v1.z+v2.z);
	}
	
	public static Vector23f subtract(Vector23f v1, Vector23f v2){
		return new Vector23f(v1.x-v2.x,
				v1.y-v2.y, v1.z-v2.z);
	}
	
	public static float dot(Vector23f v1, Vector23f v2){
		return v1.x*v2.x+v1.y*v2.y+v1.z*v2.z;
	}

	/**
	 * Sees if v is on the edge formed by v1 and v2
	 * 
	 * @param v1
	 * @param v2
	 * @param v
	 * @return
	 */
	public static boolean isBetween2d(Vector23f v1, Vector23f v2, Vector23f v){
		Vector23f v1ToV = Vector23f.subtract(v, v1);
		Vector23f v1ToV2 = Vector23f.subtract(v2, v1);
		
		float dot = Vector23f.dot2d(v1ToV.unit2d(), v1ToV2.unit2d());
		if(!CompareUtil.floatCompare(dot, 1.0f)){
			return false;
		}else{
			return v1ToV.magnitude2d()<v1ToV2.magnitude2d();
		}
	}
	
	/**
	 * Returns true when all x, y, and z are all zero.
	 * 
	 * @return
	 */
	public boolean isZero(){
		return Float.compare(x, 0)==0 && Float.compare(y, 0)==0 &&
			Float.compare(z, 0)==0;	
	}
	
	/**
	 * Sees if v1->v2 to v2->v3 forms a left turn.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return
	 */
	public static boolean isLeftTurn(Vector23f v1, Vector23f v2, Vector23f v3){
		
		Vector23f v1ToV2 = Vector23f.subtract(v2, v1);
		Vector23f v2ToV3 = Vector23f.subtract(v3, v2);
		Vector23f cross = Vector23f.cross(v1ToV2, v2ToV3);
		
		return Float.compare(cross.z, 0)>0;
	}
	
	/**
	 * Sees if the transition between direction1 and direction2 is a left turn.
	 * @param direction1
	 * @param direction2
	 * @return
	 */
	public static boolean isLeftTurn(Vector23f direction1, Vector23f direction2){
		Vector23f cross = Vector23f.cross(direction1, direction2);
		
		return Float.compare(cross.z, 0)>0;
	}
	
	/**
	 * Returns true when both x and y are zero.
	 * 
	 * @return
	 */
	public boolean isZero2d(){
		return Float.compare(x, 0)==0 && Float.compare(y, 0)==0;
	}
	
	/**
	 * Returns true when two vectors are same in both magnitude and direction
	 * @param v
	 * @return
	 */
	public boolean equals(Vector23f v){
		return Float.compare(x, v.x)==0 && Float.compare(y, v.y)==0
			&& Float.compare(z, v.z)==0;
	}
	
	/**
	 * Sees if this is parallel to a given vector.
	 * @param v
	 * @return
	 */
	public boolean isParallelTo(Vector23f v){
		if(isZero()){
			return v.isZero();
		}else{

			Vector23f thisUnit = unit();
			Vector23f vUnit = v.unit();
			
			return CompareUtil.floatCompare(thisUnit.x, vUnit.x) 
				&& CompareUtil.floatCompare(thisUnit.y, vUnit.y) 
				&& CompareUtil.floatCompare(thisUnit.z, vUnit.z);
		}
	}
	
	public float magnitude(){
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
	
	/**
	 * Calculates the magnitude of the projection the XY plane.
	 * 
	 * @return
	 */
	public float magnitude2d(){
		return (float) Math.sqrt(x*x+y*y);
	}
		
	public Vector23f unit(){
		if(isZero()){
			return new Vector23f(0,0,0);
		}
		float mag = magnitude();
		return new Vector23f(x/mag, y/mag, z/mag);
	}

	/**
	 * Finds the direction of the projection of the vector on the XY plane.
	 * @return
	 */
	public Vector23f unit2d(){
		float mag = magnitude2d();
		return new Vector23f(x/mag, y/mag, 0.0f);
	}

	/**
	 * Returns another vector whose magnitude is multiplied by f.
	 *   
	 * @param v1
	 * @param f
	 * @return
	 */
	public static Vector23f multi(Vector23f v1, float f) {
		return new Vector23f(v1.x*f, v1.y*f, v1.z*f);
	}
	
	public static Vector23f cross(Vector23f v1, Vector23f v2){
		return new Vector23f(
				v1.y*v2.z-v2.y*v1.z,
				v2.x*v1.z-v1.x*v2.z,
				v1.x*v2.y-v2.x*v1.y
		);
	}
	
	/**
	 * Finds the rotation matrix with an axis and angle in degrees.
	 * 
	 * @param axis
	 * @param degrees
	 * @return
	 */
	public static Vector23f[] findRotateMatrix(Vector23f axis, float degrees){
		float c = (float) Math.cos(degrees*Math.PI/180);
		float s = (float) Math.sin(degrees*Math.PI/180);
		
		Vector23f result [] = new Vector23f[3];
		result[0] = new Vector23f(
				axis.x*axis.x+(1-axis.x*axis.x)*c,
				axis.x*axis.y*(1-c)-axis.z*s,
				axis.x*axis.z*(1-c)+axis.y*s
		);
		
		result[1] = new Vector23f(
				axis.x*axis.y*(1-c)+axis.z*s,
				axis.y*axis.y+(1-axis.y*axis.y)*c,
				axis.x*axis.z*(1-c)-axis.x*s
		);

		result[2] = new Vector23f(
				axis.x*axis.z*(1-c)-axis.y*s,
				axis.y*axis.z*(1-c)+axis.x*s,
				axis.z*axis.z+(1-axis.z*axis.z)*c
		);
		return result;
	}

	/**
	 * Using a pre-calculated matrix and center to rotate a point. 
	 * 
	 * @param p
	 * @param matrix
	 * @param center
	 */
	public static void rotate(Vector23f p, Vector23f matrix[], Vector23f center){
		p.x = p.x - center.x;
		p.y = p.y - center.y;
		p.z = p.z - center.z;
		
		float x = Vector23f.dot(p, matrix[0]);
		float y = Vector23f.dot(p, matrix[1]);
		float z = Vector23f.dot(p, matrix[2]);
		
		p.x = x + center.x;
		p.y = y + center.y;
		p.z = z + center.z;
	}

	/**
	 * Finds the dot produce of the projections of two vectors
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float dot2d(Vector23f v1, Vector23f v2) {
		return v1.x*v2.x+v1.y*v2.y;
	}
		
	public Vector23f clearZ(){
		return new Vector23f(x,y,0.0f);
	}

	public Vector23f clearY(){
		return new Vector23f(x,0.0f,z);
	}

	public Vector23f clearX(){
		return new Vector23f(0.0f,y,z);
	}
	
	/**
	 * Returns the direction in range [0,2*PI] in the XY plane.
	 * @return
	 */
	public float findDirectionXY(){
		float tempX = (Float.compare(0.0f, Math.abs(x))==0)?0:x;
		float tempY = (Float.compare(0.0f, Math.abs(y))==0)?0:y;
		float angle = (float) Math.atan(tempY/tempX);
		if(Float.compare(tempX,0)>=0 && Float.compare(tempY,0)<0){
			angle += Math.PI*2;
		}else if(Float.compare(tempX,0)<0){
			angle += Math.PI;
		}
		return angle;
	}
	
	/**
	 * Returns true if the span from angle1 to angle2 is clock wise.
	 * 
	 * @param angle1 in [0, 2*PI)
	 * @param angle2 in [0, 2*PI)
	 * @return
	 */
	public static boolean clockWise2d(float angle1, float angle2){
		if(Math.abs(angle2-angle1)<Math.PI && angle2<angle1){
			return true;
		}else if(Math.abs(angle2-angle1)>Math.PI && angle2>angle1){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean clockWise2d(Vector23f v1, Vector23f v2){
		float angle1 = v1.findDirectionXY();
		float angle2 = v2.findDirectionXY();
		return clockWise2d(angle1, angle2);
	}
	
	/**
	 * Sees if this vector and a given vector form a acute turn
	 * @param v
	 * @return
	 */
	public boolean acuteTurn(Vector23f v){
		float dot = Vector23f.dot(this, v);
		return dot>0;
	}
	
	public Vector23f duplicate(){
		Vector23f v= new Vector23f(x,y,z);
		return v; 
	}

	/**
	 * Finds the intersection between pt1--pt2 and pt3--pt4.
	 * 
	 * @param pt1
	 * @param pt2
	 * @param pt3
	 * @param pt4
	 * @return
	 */
	public static Vector23f intersect(Vector23f pt1, Vector23f pt2,
			Vector23f pt3, Vector23f pt4){
		// http://mathforum.org/library/drmath/view/62814.html
		Vector23f p1 = pt1;
		Vector23f p2 = pt3;
		Vector23f v1 = Vector23f.subtract(pt2, pt1).unit();
		Vector23f v2 = Vector23f.subtract(pt4, pt3).unit();
		
		Vector23f v1Xv2 = Vector23f.cross(v1, v2);
		Vector23f p12P2 = Vector23f.subtract(p2, p1);
		Vector23f p12P2Xv2 = Vector23f.cross(p12P2, v2);
		
		if(v1Xv2.isZero() || !v1Xv2.isParallelTo(p12P2Xv2)){
			return null;
		}else{
			float a = 0;
			if(Math.abs(v1Xv2.x)>=Math.abs(v1Xv2.y) && 
					Math.abs(v1Xv2.x)>=Math.abs(v1Xv2.z)){
				a = p12P2Xv2.x / v1Xv2.x;
			}else if(Math.abs(v1Xv2.y)>=Math.abs(v1Xv2.x) && 
					Math.abs(v1Xv2.y)>=Math.abs(v1Xv2.z)){
				a = p12P2Xv2.y / v1Xv2.y;
			}else if(Math.abs(v1Xv2.z)>=Math.abs(v1Xv2.y) && 
					Math.abs(v1Xv2.z)>=Math.abs(v1Xv2.x)){
				a = p12P2Xv2.z / v1Xv2.z;
			}
			
			Vector23f p = Vector23f.add(p1, Vector23f.multi(v1, a));
			boolean onPt12 = false, onPt34=false;
			if(Math.abs(v1.x)>=Math.abs(v1.y) && 
					Math.abs(v1.x)>=Math.abs(v1.z) &&
					CompareUtil.isBetween(p.x, pt1.x, pt2.x)){
				onPt12 = true;
			}else if(Math.abs(v1.y)>=Math.abs(v1.x) && 
					Math.abs(v1.y)>=Math.abs(v1.z) &&
					CompareUtil.isBetween(p.y, pt1.y, pt2.y)){
				onPt12 = true;
			}else if(Math.abs(v1.z)>=Math.abs(v1.x) && 
					Math.abs(v1.z)>=Math.abs(v1.y)  &&
					CompareUtil.isBetween(p.z, pt1.z, pt2.z)){
				onPt12 = true;
			}

			if(Math.abs(v2.x)>=Math.abs(v2.y) && 
					Math.abs(v2.x)>=Math.abs(v2.z) &&
					CompareUtil.isBetween(p.x, pt3.x, pt4.x)){
				onPt34 = true;
			}else if(Math.abs(v2.y)>=Math.abs(v2.x) && 
					Math.abs(v2.y)>=Math.abs(v2.z) &&
					CompareUtil.isBetween(p.y, pt3.y, pt4.y)){
				onPt34 = true;
			}else if(Math.abs(v2.z)>=Math.abs(v2.x) && 
					Math.abs(v2.z)>=Math.abs(v2.y) &&
					CompareUtil.isBetween(p.z, pt3.z, pt4.z)){
				onPt34 = true;
			}
			
			return (onPt12 && onPt34)?p:null;
		}
	}
}
