package com.sim.curves;

import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;

/**
 * Encapsulates parameters for a horizontal curve.
 * <p>
 * p1, p2, and pi are input from the user. Another user input is
 * the speed limit which is not part of this class. Given user inputs,
 * other parameters found according to the standards.
 * <p>
 * 
 * @author Dahai Guo
 *
 */
public class HorizontalCurve {

	/**
	 * Where the curve begins 
	 */
	public RoadVector pc;
	
	/**
	 * Where the curve ends
	 */
	public RoadVector pt;
	
	/**
	 * Where the two legs intersect
	 */
	public RoadVector pi;
		
	/**
	 * p1-->pi defines the first leg of the curve
	 */
	public RoadVector p1;
	
	/**
	 * pi-->p2 defines the second leg of the curve
	 */
	public RoadVector p2;
	
	/**
	 * The angle between p1-->pi and pi-->p2
	 */
	public float theta;
	
	/**
	 * The required radius 
	 */
	public float minRadius;
	
	/**
	 * The center of the arc, making the horizontal curve.
	 */
	public RoadVector center;
	
	/**
	 * @see com.sim.curves.RunOutOff
	 */
	public RunOutOff runOutOff;
	
	/**
	 * Super-elevation in percent.
	 */
	public float superElevation;
	
	/**
	 * Initializes a horizontal curve.
	 * <p>
	 * This constructor is rigid in that it requires all the parameters to have
	 * been found.
	 * <p>
	 * @param theta
	 * @param minRadius
	 * @param pc
	 * @param pi
	 * @param pt
	 * @param center
	 * @param p1
	 * @param p2
	 * @param runOutOff
	 * @param superElevation
	 */
	public HorizontalCurve(float theta, float minRadius, RoadVector pc,
			RoadVector pi, RoadVector pt, RoadVector center, 
			RoadVector p1, RoadVector p2, RunOutOff runOutOff,
			float superElevation) {
		this.theta = theta;
		this.minRadius = minRadius;
		this.pc = pc;
		this.pi = pi;
		this.pt = pt;
		this.center = center;
		this.p1 = p1;
		this.p2 = p2;
		this.runOutOff = runOutOff;
		this.superElevation = superElevation;
	}
	
	/**
	 * Dummy constructor.
	 */
	public HorizontalCurve(){
		
	}
}
