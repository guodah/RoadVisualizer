package com.sim.core.basic;

/**
 * Includes constants used to build 3D models.
 * 
 * @author Dahai Guo
 *
 */
public class ModelGenConsts {
	
	/**
	 * Shoulder material constant.
	 */
	public static final int SHOULDER = 0;
	
	/**
	 * Meidan material constant.
	 */
	public static final int MEDIAN = 1;
	
	/**
	 * White marker constant.
	 */
	public static final int WHITE_MARKER = 3;
		
	/**
	 * Yellow marker constant.
	 */
	public static final int YELLOW_MARKER = 4;
	
	/**
	 * Road surface material constant.
	 */
	public static final int ROAD_SURFACE = 5;
	
	/**
	 * Solid White Marker (left)
	 */
	public static final int SOLID_WHITE_MARKER_LEFT = 6;
	
	/**
	 * Solid White Marker (right)
	 */
	public static final int SOLID_WHITE_MARKER_RIGHT = 7;
	
	/**
	 * Pavement
	 */
	public static final int PAVEMENT = 8;
	

	public static final int TERRAIN = 9;

	
	/**
	 * This is for wrapping uniform texture.
	 * <p>
	 * The less this value is, the more times the texture will repeat.
	 */
	public static final float TEX_WRAP_SCALE = 5.0f;

	/**
	 * The shortest length of edges, approximating curves
	 */
	public final static float RESOLUTION = 30.0f; 
	
	/**
	 * Is the base height of road to generate.
	 */
	public final static float INIT_ELEVATION = 0.0f;

	/**
	 * Used for floating point calculation 
	 */
	public final static float VERY_SMALL_FLOAT = 0.001f;
	
	/**
	 * Used for loating point calculation
	 */
	public final static float SMALL_FLOAT = 0.1f;
	
	/**
	 * Flags a point not to be related with run out of off
	 */
	public static final int NOT_ANCHOR = 0;
	
	/**
	 * Flags a point to be where run out begins
	 */
	public static final int RUNOUT_BEGINS = 1;

	/**
	 * Flags a point to be where run off begins
	 */
	public static final int RUNOFF_BEGINS = 2;
	
	/**
	 * Flags a point to be where run off ends
	 */
	public static final int RUNOFF_ENDS = 4;
	
	/**
	 * Flags a point where the outside lane is level with 
	 * the inside lane.
	 */
	public static final int LEVEL_OTHER_SIDE = 8;
	
	/**
	 * Defines the relative size of the surrounding terrain.
	 * <p>
	 * If the value is 2, the size of terrain is about twice the size
	 * of the surrounding rectangle of the model.
	 */
	public static final float TERRAIN_SCALE = 2;
}
