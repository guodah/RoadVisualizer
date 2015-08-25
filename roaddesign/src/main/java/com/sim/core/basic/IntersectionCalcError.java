package com.sim.core.basic;

/**
 * Is a global error flag, like errno in Linux, indicating errors when
 * calculating the geometry of an intersection
 * 
 * @see com.sim.intersections.basic.BasicIntersection 
 * 		com.sim.intersections.basic.BasicIntersectionBuilder
 *  
 * @author Dahai Guo
 *
 */
public class IntersectionCalcError {
	public static int failure = 0;
	
	/**
	 * The calculation of a {@link com.sim.intersections.basic.BasicIntersection }
	 * needs to be defined by four point. 
	 */
	public static final int BASIC_INTER_WRONG_NUM_PTS = 1;
	
	/**
	 * The calculation of a {@link com.sim.intersections.basic.BasicIntersection} 
	 * needs to be defined by two cross sections of two intersecting roads.
	 */
	public static final int BASIC_INTER_WRONG_NUM_XSECS = 2;
	
	/**
	 * The calculation of a {@link com.sim.intersections.basic.BasicIntersection} 
	 * needs the two intersecting roads to have zero grade.
	 */
	public static final int BASIC_INTER_NON_ZERO_GRADE = 4;
	
	/**
	 * The calculation of a BasicIntersection needs the median to be somewhat 
	 * wider than a lane width. See InterKeyCalc.MIN_MEDIAN_LANE_RATIO
	 */
	public static final int BASIC_INTER_NARROW_MEDIAN = 8;
	
	/**
	 * The calculation of a {@link com.sim.intersections.basic.BasicIntersection} 
	 * needs the two intersecting roads to have the same elevation.
	 */
	public static final int BASIC_INTER_NON_EQUAL_Z = 16;
	
	/**
	 * The two supposedly intersecting roads do not intersect.
	 */
	public static final int BASIC_INTER_NO_INTERSECT = 32;
	
	/**
	 * The two roads for calculating a {@link com.sim.intersections.basic.BasicIntersection} 
	 * need to have significant straight sections before the intersection.
	 */
	public static final int BASIC_INTER_LESS_CLEARANCE = 64;
	
	public static void clear() {
		failure = 0;		
	}
}
