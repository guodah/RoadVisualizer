package com.sim.geometries;

import com.sim.core.basic.FlagUtil;
import com.sim.core.basic.ModelGenConsts;
import com.sim.curves.HorizontalCurve;
import com.sim.curves.VerticalCurve;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.util.Duplicatable;


/**
 * Is a Vector23f geometrically, but contains some information about
 * road design.
 * 
 * @author Dahai Guo
 *
 */
public class RoadVector implements Duplicatable{
	private Vector23f vector;

	/**
	 * This is for debug uses. See FlagUtil for all its possible values.
	 */
	public int roadDesignType;
	
	/**
	 * See ModelGenConsts for possible cross section types.
	 * <p>
	 * It is for cross section segment types.
	 */
	public int crossSectionType;
	
	/**
	 * See ModelGenConsts for possible cross section types.
	 * <p>
	 * It is for adding super-elevation after the horizontal and/or
	 * vertical curve is calculated. 
	 */
	private int anchorType;
	
	/**
	 * Which horizontal curve this road vector belongs to. Could be null.
	 */
	public HorizontalCurve hCurve; 

	/**
	 * Which vertical curve this road vector belongs to. Could be null.
	 */
	public VerticalCurve vCurve;
	
//	public boolean byIntersection;
//	public BasicIntersection intersection=null;

//	public boolean intersectionExtension;
	
	/**
	 * Finds where v1--v2 and v3--v4 intersect if they do.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @return
	 */
	public static RoadVector intersect(RoadVector v1, RoadVector v2,
			RoadVector v3, RoadVector v4){
		Vector23f v = Vector23f.intersect(v1.vector, v2.vector, 
				v3.vector, v4.vector);
		if(v==null){
			return null;
		}else{
			return new RoadVector(v);
		}
	}

	/**
	 * Does not validate arguments
	 * @param x
	 * @param y
	 * @param z
	 */
	public RoadVector(float x, float y, float z){
		vector = new Vector23f(x,y,z);
		roadDesignType = FlagUtil.ON_STRAIGHT_SEGMENT;
		anchorType = ModelGenConsts.NOT_ANCHOR;
		hCurve = null;
		vCurve = null;
//		byIntersection = false;
//		intersectionExtension = false;
	}

/*
	public void setByIntersection(boolean inter){
		byIntersection = inter;
	}
*/	
	public RoadVector(Vector23f v){
		this(v.x, v.y, v.z);
	}
	
	public RoadVector(){
		this(0,0,0);
	}
	
	/**
	 * Returns (x,y,z)
	 */
	public String toString(){
		return vector.toString();
	}
		
	public static RoadVector add(RoadVector v1, RoadVector v2){
		return new RoadVector(Vector23f.add(v1.vector, v2.vector));
	}
	
	public static RoadVector subtract(RoadVector v1, RoadVector v2){
		return new RoadVector(Vector23f.subtract(v1.vector, v2.vector));
	}
	
	public static float dot(RoadVector v1, RoadVector v2){
		return Vector23f.dot(v1.vector, v2.vector);
	}

	/**
	 * Returns true when all x, y, and z are all zero.
	 * 
	 * @return
	 */
	public boolean isZero(){
		return vector.isZero();	
	}
	
	/**
	 * Returns true when both x and y are zero.
	 * 
	 * @return
	 */
	public boolean isZero2d(){
		return vector.isZero2d();
	}
	
	/**
	 * Returns true when two vectors are same in both magnitude and direction
	 * @param v
	 * @return
	 */
	public boolean equals(RoadVector v){
		if(!vector.equals(v.vector)){
			return false;
		}
		
		return (this.anchorType==v.anchorType &&
				this.vCurve==v.vCurve && this.hCurve==v.hCurve &&
				this.crossSectionType==v.crossSectionType);
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public boolean isParallelTo(RoadVector v){
		return vector.isParallelTo(v.vector);
	}
	
	public float magnitude(){
		return vector.magnitude();
	}
	
	/**
	 * Calculates the magnitude of the projection the XY plane.
	 * 
	 * @return
	 */
	public float magnitude2d(){
		return vector.magnitude2d();
	}
	
	public RoadVector unit(){
		Vector23f v = vector.unit();
		RoadVector rv = duplicate();
		rv.vector = v;
		return rv;
	}

	/**
	 * Finds the direction of the projection of the vector on the XY plane.
	 * @return
	 */
	public RoadVector unit2d(){
		Vector23f v = vector.unit2d();
		RoadVector rv = duplicate();
		rv.vector = v;
		return rv;
	}

	/**
	 * Returns another vector whose magnitude is multiplied by f.
	 *   
	 * @param v1
	 * @param f
	 * @return
	 */
	public static RoadVector multi(RoadVector v1, float f) {
		Vector23f v = Vector23f.multi(v1.vector, f);
		RoadVector rv = v1.duplicate();
		rv.vector = v;
		return rv;
	}
	
	public static RoadVector cross(RoadVector v1, RoadVector v2){
		Vector23f v = Vector23f.cross(v1.vector, v2.vector);
		RoadVector rv = v1.duplicate();
		rv.vector = v;
		return rv;
	}
	

	/**
	 * Using a pre-calculated matrix and center to rotate a point. 
	 * 
	 * @param p
	 * @param matrix
	 * @param center
	 */
	public static void rotate(RoadVector p, Vector23f matrix[], Vector23f center){
		Vector23f.rotate(p.vector, matrix, center);
	}

	/**
	 * Finds the dot produce of the projections of two vectors
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float dot2d(RoadVector v1, RoadVector v2) {
		return Vector23f.dot2d(v1.vector, v2.vector);
	}
		
	public static boolean isBetween2d(RoadVector v1, 
			RoadVector v2, RoadVector v){
		return Vector23f.isBetween2d(v1.getVector(), 
				v2.getVector(), v.getVector());
	}
	
	public RoadVector clearZ(){
		RoadVector rv = duplicate();
		rv.vector.z = 0;
		return rv;
	}

	public RoadVector clearY(){
		RoadVector rv = duplicate();
		rv.vector.y = 0;
		return rv;
	}

	public RoadVector clearX(){
		RoadVector rv = duplicate();
		rv.vector.x = 0;
		return rv;
	}
	
	/**
	 * Returns the direction in range [0,2*PI] in the XY plane.
	 * @return
	 */
	public float findDirectionXY(){
		return vector.findDirectionXY();
	}
		
	public RoadVector duplicate(){
		RoadVector rv = new RoadVector(vector);
		
		rv.crossSectionType = this.crossSectionType;
		rv.roadDesignType = this.roadDesignType;
		rv.anchorType = this.anchorType;
		rv.hCurve = this.hCurve;
		rv.vCurve = this.vCurve;
//		rv.intersectionExtension = this.intersectionExtension;
		return rv; 
	}

	public void setAchorType(int anchorType) {
		this.anchorType |= anchorType;
	}

	public int getAchorType() {
		return anchorType;
	}
	
	/**
	 * See if this road vector is an anchorType.
	 * 
	 * @param anchorType
	 * @return
	 */
	public boolean isAnchorSet(int anchorType){
		if((this.anchorType&anchorType)!=0){
			return true;
		}else{
			return false;
		}
	}

	public float getX(){
		return vector.x;
	}
	
	public void setX(float x){
		this.vector.x = x;
	}

	public float getY(){
		return vector.y;
	}
	
	public void setY(float y){
		this.vector.y = y;
	}

	public float getZ(){
		return vector.z;
	}
	
	public void setZ(float z){
		this.vector.z = z;
	}

	public Vector23f getVector() {
		return vector;
	}

	public boolean acuteTurn(RoadVector v){
		return vector.acuteTurn(v.vector);
	}

	public RoadVector reverse() {
		return new RoadVector(vector.reverse());
	}
/*
	public void setIntersection(BasicIntersection inter) {
		intersection = inter;
	}
	
	public BasicIntersection getIntersection(){
		return intersection;
	}
*/	
}
