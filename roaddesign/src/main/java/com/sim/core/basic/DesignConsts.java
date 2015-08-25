package com.sim.core.basic;

/**
 * Includes constants for roadway design. 
 * <p>
 * This class summarizes much less constants than what AASHTO includes. 
 * While these constants have not been examined against AASHTO, they should
 * make sense in ordinary cases.
 * <p>  
 * In the future, this class may be revised to be a "hub" which references
 * to other classes, each one of which includes constants in specific categories.
 * <p>
 * Note: all units are foot.
 * 
 * @author Dahai Guo
 *
 */
public class DesignConsts {
	/**
	 * The length of each dashed white marker
	 */
	public static final float DASH_WHITE_MARKER_LEN = 6.0f;
	
	/**
	 * The distance between two dashed white makers
	 */
	public static final float DASH_WHITE_MARKER_SPACE = 18.0f;

	/**
	 * This constant is used to take care of floating point
	 * error or some boundary conditions. 
	 */
	public static final float DELTA = 3.0f;
	
	/**
	 * The width of all kinds of markers
	 */
	public static final float MARKER_WIDTH = (float) (8.0/12.0); 
	
	/**
	 * The distance between two parallel markers.  
	 */
	public static final float MARKER_GAP = (float) (8.0/12.0);

	/**
	 * This array is queried by (speedlimit-15). The minimum 
	 * speed is 15 mph. 
	 * <p>
	 * It is used to calculate the distant of a crest vertical curve.
	 * <p>
	 * Size of array is 14.
	 */
	public static float CrestCurveK [] = {
		3.0f, 7.0f, 12.0f, 19.0f, 29.0f, 44.0f, 61.0f,
		84.0f, 114.0f, 151.0f, 193.0f, 247.0f, 312.0f, 384.0f
	};
	
	/**
	 * This array is queried by (super_elevation-4)/2 and (speed-15)/5.
	 * <p>
	 * The minimum super elevation is 4 percent and minimum speed is 15.
	 * <p>
	 * Size of array is 3X10.
	 */
	public static float MinRadiiTable[][]={
			{
				70.0f, 125.0f, 205.0f, 300.0f, 420.0f,
				565.0f, 730.0f, 930.0f, 1190.0f, 1505.0f
			},
			{
				65.0f, 115.0f, 185.0f, 275.0f, 380.0f,
				510.0f, 660.0f, 835.0f, 1065.0f, 1340.0f, 
				1660.0f, 2050.0f, 2510.0f, 3060.0f
			},
			{
				60.0f, 105.0f, 170.0f, 250.0f, 350.0f,
				465.0f, 500.0f, 760.0f, 965.0f, 1205.0f,
				1485.0f, 1820.0f, 2215.0f, 2675.0f
			}
	};
	
	/**
	 * The array is queried by (number_of_lanes-1), (superelevation-4)/2,
	 * (speed-15)/5.
	 * <p>
	 * If the number of lanes is greater than 2, it is set to 2. The minimum
	 * superelevation is 4, and minimum speed is 15.
	 * <p>
	 * Runoff distance is the distance along which the outside lane is
	 * flatened. Two thirds of this distance is prior to where the horizontal
	 * curve starts (PC). 
	 * <p>
	 * Size of array is 2X3X10.
	 */
	public static final float RunOffTable[][][]={
			{
				{
					61.0f, 65.0f, 69.0f, 73.0f, 77.0f, 83.0f, 89.0f, 
					96.0f, 102.0f, 107.0f
				},
				{
					92.0f, 97.0f , 103.0f, 109.0f, 116.0f, 124.0f, 133.0f,
					144.0f, 153.0f, 160.0f, 168.0f, 180.0f, 189.0f, 206.0f						
				},
				{
					123.0f, 130.0f, 137.0f, 146.0f, 155.0f, 165.0f, 178.0f, 
					192.0f, 204.0f, 213.0f, 224.0f, 240.0f, 252.0f, 275.0f
				}
			},
			{
				{
					92.0f, 97.0f, 103.0f, 109.0f, 116.0f, 124.0f, 138.0f, 
					144.0f, 153.0f, 160.0f 
				},
				{
					138.0f, 146.0f, 154.0f, 164.0f, 174.0f, 186.0f, 200.0f,
					216.0f, 230.0f, 240.0f, 252.0f, 270.0f, 284.0f, 308.0f
				},
				{
					184.0f, 194.0f, 206.0f, 219.0f, 232.0f, 248.0f, 266.0f, 
					288.0f, 307.0f, 320.0f, 336.0f, 360.0f, 379.0f, 412.0f
				}
			}
	};
	
	/**
	 * This array is queried by (number_of_lanes-1) and (speed-15)/5.
	 * <p>
	 * If the number of lanes is greater than 2, it is set to 2. The minimum
	 * super-elevation is 4, and minimum speed is 15.
	 * <p>
	 * Size of array is 2X14.
	 */
	public static float RunOutTable[][]={
			{
				31.0f, 32.0f, 34.0f, 36.0f, 39.0f, 41.0f, 44.0f, 48.0f, 
				51.0f, 53.0f, 58.0f, 60.0f, 63.0f, 69.0f
			},
			{
				46.0f, 49.0f, 51.0f, 55.0f, 58.0f, 62.0f, 67.0f, 72.0f,
				77.0f, 80.0f, 84.0f, 90.0f, 95.0f, 103.0f
			}
	};


}
