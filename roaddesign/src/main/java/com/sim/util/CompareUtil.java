package com.sim.util;

/**
 * Compares floating point numbers.
 * @author Dahai Guo
 *
 */
public class CompareUtil {
	
	/**
	 * A small number to take into account floating-point errors
	 */
	public static final float SMALL_FLOAT = 0.05f;
	
	public static boolean floatCompare(float f1, float f2){
		return floatCompare(f1, f2, SMALL_FLOAT);
	}

	public static boolean floatCompare(float f1, float f2, float delta){
		float d = Math.abs(f1-f2);
		return (d<delta);
	}
	
	public static boolean isZero(float f){
		return Math.abs(f)<SMALL_FLOAT;
	}
	
	public static boolean isBetween(float f, float f1, float f2){
		return (f>=f1 && f<=f2) || (f>=f2 && f<=f1);
	}
}
