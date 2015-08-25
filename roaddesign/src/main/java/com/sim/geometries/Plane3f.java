package com.sim.geometries;

/**
 * Implements a 3D plane.
 * <p>
 * Mathematically it is described as (X dot normal) + d = 0. 
 * 
 * @author Dahai Guo
 *
 */
public class Plane3f {
	/**
	 * Is a unit vector and the normal of the plane
	 */
	public Vector23f normal; 
	
	/**
	 * Is used in ax+by+cz+d=0;
	 */
	public float d;
	
	/**
	 * Does not validate the arguments.
	 * 
	 * @param normal
	 * @param d
	 */
	public Plane3f(Vector23f normal, float d){
		this.normal = normal;
		this.d = d;
	}
}	
