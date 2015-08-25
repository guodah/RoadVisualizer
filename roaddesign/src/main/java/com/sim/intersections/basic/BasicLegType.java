package com.sim.intersections.basic;

public enum BasicLegType{
	UP_STREAM, DOWN_STREAM;
	
	/**
	 * Finds and returns a direction, opposite to the current one.
	 * @return
	 */
	public BasicLegType not(){
		return (this==UP_STREAM)?DOWN_STREAM:UP_STREAM;
	}
}
