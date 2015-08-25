package com.sim.curves;

import com.sim.geometries.Vector23f;
import com.sim.obj.VehicleType;

/**
 * ASHTO Exhibit 9-20
 * @author Dahai Guo
 *
 */
public class ThreeCenteredTurnConsts {
	
	/**
	 * Min turn angle
	 */
	public static float MIN_ANGLE = 30.0f;
	
	/**
	 * Max turn angle
	 */
	public static float MAX_ANGLE = 150.0f;
	
	/**
	 *  uses vehicle type WB-67
	 */
	public static final float radii[][] ={
		{460, 175, 460},// 30
		{460, 175, 460},// 45
		{400, 100, 400},// 60
		{420, 75, 420},// 75
		{440, 65, 440},// 90
		{500, 50, 500},// 105
		{550, 45, 550},// 120
		{550, 45, 550},// 135
		{550, 45, 550},// 150
	};
	
	/**
	 *  uses vehicle type WB-67
	 */
	public static final float offsets[][] ={
		{4,4},//30
		{4,4},//45
		{8,8},//60
		{10,10},//75
		{10,10},//90
		{13,13},//105
		{15,15},//120
		{16,16},//135
		{19,19}//150
	};

	/**
	 * Given two directions, finds the turn angle and gets the index for
	 * querying offsets and radii
	 * @param direction1
	 * @param direction2
	 * @return
	 */
	private static int findIndex(Vector23f direction1, Vector23f direction2){
		float ANGLE = (float) (180*Math.acos(
				Vector23f.dot2d(direction1, direction2))/Math.PI);
				
		if(ANGLE<MIN_ANGLE){
			ANGLE=MIN_ANGLE;
		}else if(ANGLE>MAX_ANGLE){
			ANGLE=MAX_ANGLE;
		}
		
		return (int) ((ANGLE-MIN_ANGLE)/15);		
	}
	
	/**
	 * Given two directions and the vehicle type, gets the radii of 
	 * the three centered curve.
	 * 
	 * @param direction1
	 * @param direction2
	 * @param veh
	 * @return
	 */
	public static float[] findRadii(Vector23f direction1, 
			Vector23f direction2, VehicleType veh){
		
		int index = findIndex(direction1, direction2);
		
		return radii[index];
	}
	
	/**
	 * Given two directions and the vehicle type, gets the offsets of 
	 * the three centered curve.
	 * 
	 * @param direction1
	 * @param direction2
	 * @param veh
	 * @return
	 */
	public static float[] findOffsets(Vector23f direction1, 
			Vector23f direction2, VehicleType veh){
		int index = findIndex(direction1, direction2);
		
		return offsets[index];		
	}
}
