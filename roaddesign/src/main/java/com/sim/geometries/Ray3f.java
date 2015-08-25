package com.sim.geometries;


/**
 * Implements 3D ray.
 * <p>
 * Mathematically, it is described as p=orig+direction*t
 * 
 * @author Dahai Guo
 *
 */
public class Ray3f {
	/**
	 * Beginning of the ray
	 */
	public Vector23f orig;
	
	/**
	 * Is the unit vector and the direction of the ray
	 */
	public Vector23f direction;
	
	/**
	 * Does not validate arguments.
	 * 
	 * @param orig
	 * @param p2
	 */
	public Ray3f(Vector23f orig, Vector23f p2){
		this.orig = orig;
		direction = (Vector23f.subtract(p2, orig)).unit();
	}
	
	/**
	 * Does not validate the argument
	 * @param orig
	 */
	public Ray3f(Vector23f orig){
		this.orig = orig;
	}
	
	/**
	 * Finds the intersection of the ray and a plane.
	 * 
	 * Does not consider the case in which the ray and the plane are parallel
	 * 
	 * @param p
	 * @return
	 */
	public Vector23f intersect(Plane3f p){

		float t = (p.d-Vector23f.dot(p.normal, orig))/
			Vector23f.dot(p.normal, direction);
		return Vector23f.add(orig, 
				Vector23f.multi(direction, t));
	}
	
	/**
	 * Finds the intersection of the "this" ray and another.
	 * 
	 * Considers the cases: 1) two rays are not co-planer 2) do not intersect 
	 * 	3) neither case 1 nor 2
	 * 
	 * @param r
	 * @return
	 */
	public Vector23f intersect(Ray3f r){
		// http://mathforum.org/library/drmath/view/62814.html
		Vector23f p1 = orig;
		Vector23f p2 = r.orig;
		Vector23f v1 = direction;
		Vector23f v2 = r.direction;
		
		Vector23f v1Xv2 = Vector23f.cross(v1, v2);
		Vector23f p12P2 = Vector23f.subtract(p2, p1);
		Vector23f p12P2Xv2 = Vector23f.cross(p12P2, v2);
		
		if(!v1Xv2.isZero() || !v1Xv2.isParallelTo(p12P2Xv2)){
			return null;
		}else{
			float a = 0;
			if(Float.compare(v1Xv2.x, 0)!=0){
				a = p12P2Xv2.x / v1Xv2.x;
			}else if(Float.compare(v1Xv2.y, 0)!=0){
				a = p12P2Xv2.y / v1Xv2.y;
			}else if(Float.compare(v1Xv2.z, 0)!=0){
				a = p12P2Xv2.z / v1Xv2.z;
			}
			
			return Vector23f.add(orig, Vector23f.multi(direction, a));
		}
	}
}
