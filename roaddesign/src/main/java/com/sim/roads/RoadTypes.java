package com.sim.roads;

public enum RoadTypes {
	/**
	 * @see com.sim.roads.basic.BasicRoad
	 */
	BASIC_ROAD (0);
	
	private int value;
	private RoadTypes(int value){
		this.value = value;
	}
}
