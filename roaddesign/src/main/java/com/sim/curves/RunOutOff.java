package com.sim.curves;

/**
 * Contains run-out and run-off information.
 * <p>
 * For a super-elevation to be implemented gradually, minimum 
 * distances are defined for the outer lanes to be leveled (run-out)
 * and be raised to super-elevation (run-off).
 *
 *
 * @author Dahai Guo
 *
 */
public class RunOutOff {
	public float runOutLen;
	public float runOffLen;
	
	public RunOutOff(float runOutLen, float runOffLen){
		this.runOffLen = runOffLen;
		this.runOutLen = runOutLen;
	}
}
