package com.sim.core.basic;


import java.awt.Color;
import java.util.ArrayList;

import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.obj.CrossSection;


/**
 * Includes flags for debugging.
 * 
 * Last Modified: July 20, 2011
 * @author Dahai Guo
 *
 */
public class FlagUtil {
	
	public static final boolean ROAD_CALC_DEBUG = true;
	public static final boolean GUI_DEBUG = false;
	
	/**
	 * To flag a center line point to be on a straight segment.
	 */
	public static final int ON_STRAIGHT_SEGMENT = 0;
	
	/**
	 * To flag a center line point to be on a vertical curve.
	 */
	public static final int ON_VERTICAL_CURVE = 1;
	
	/**
	 * To flag a center line point to be on a runout section.
	 */
	public static final int ON_RUNOUT_SECTION = 2;
	
	/**
	 * To flag a center line point to be on a runoff section.
	 */
	public static final int ON_RUNOFF_SECTION = 4;
	
	/**
	 * To flag a center line point to be on a horizontal curve.
	 */
	public static final int ON_HORIZONTAL_CURVE = 8;	
	
	/**
	 * In GUI, color center line according to center line point's flag.
	 */
	public static final Color STRAIGHT_SEGMENT_COLOR = Color.BLACK;
	
	/**
	 * In GUI, color center line according to center line point's flag.
	 */
	public static final Color VERTICAL_CURVE_COLOR = Color.RED;
	
	/**
	 * In GUI, color center line according to center line point's flag.
	 */
	public static final Color RUNOUT_SECTION_COLOR = Color.YELLOW;
	
	/**
	 * In GUI, color center line according to center line point's flag.
	 */
	public static final Color RUNOFF_SECTION_COLOR = Color.BLUE;
	
	/**
	 * In GUI, color center line according to center line point's flag.
	 */
	public static final Color HORIZONTAL_CURVE_COLOR = Color.GREEN;

	/**
	 * Designate a point on a road center line as a certain type.
	 * 
	 * @param v
	 * @param type
	 */
	public static void regRdDesType(RoadVector v, int type){
		if(ROAD_CALC_DEBUG){
			v.roadDesignType |= type;
		}
	}

/*	
	public static Color RUNOUTOFF = Color.BLUE;
	public static Color ARC = Color.RED;
	public static Color V_RUNOUTOFF = RUNOUTOFF.darker();
	public static Color V_ARC = ARC.darker();
	public static Color V_CURVE = Color.BLACK;
*/	
}
