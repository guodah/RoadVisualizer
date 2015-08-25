package com.sim.curves;

import com.sim.geometries.*;

/**
 * Encapsulates parameters for a vertical curve.
 * <p>
 * p1, p2 and pvi are from the user. Another parameter from user is
 * speed which is not listed in this class.
 * <p>
 * A vertical curve is possible to be coincide with a horizontal curve.
 * inHCurve is a flag, indicating this situation. If this is the case,
 * pvi for the vertical curve must be the same with pi for the horizontal
 * curve in XY plane.
 *  
 * @author Owner
 *
 */
public class VerticalCurve {
	public static final int CREST = 0;
	public static final int SAG = 1;
	
	/**
	 * Either CREST or SAG.
	 */
	public int type;
	
	/**
	 * Where the vertical curve begins
	 */
	public RoadVector pvc;
	
	/**
	 * Where the two legs intersect
	 */
	public RoadVector pvi;
	
	/**
	 * Where the vertical curve ends
	 */
	public RoadVector pvt;
	
	/**
	 * Parabolic curve coefficient
	 */
	public float a;
	
	/**
	 * Parabolic curve coefficient
	 */
	public float b;
	
	/**
	 * Parabolic curve coefficient
	 */
	public float c;
	
	/**
	 * Where or not this vertical curve coincide with 
	 * a horizontal curve
	 */
	public boolean inHCurve;
	
	/** 
	 * p1-->pvi defines the first leg
	 */
	public RoadVector p1;
	
	/**
	 * pvi-->p2 defines the second leg
	 */
	public RoadVector p2;
	
	/**
	 * Dummy constructor
	 */
	public VerticalCurve(){
		
	}
	
	/**
	 * This constructor is rigid in that it requires all the parameters to 
	 * be set.
	 * 
	 * @param type
	 * @param pvc
	 * @param pvi
	 * @param pvt
	 * @param a
	 * @param b
	 * @param c
	 * @param inHCurve
	 * @param p1
	 * @param p2
	 */
	public VerticalCurve(int type, RoadVector pvc, RoadVector pvi, RoadVector pvt, 
			float a, float b, float c, boolean inHCurve,
			RoadVector p1, RoadVector p2){
		this.type = type;
		this.pvc = pvc;
		this.pvi = pvi;
		this.pvt = pvt;
		this.a = a;
		this.b = b;
		this.c = c;
		this.inHCurve = inHCurve;
		this.p1 = p1;
		this.p2 = p2;
	}
}
