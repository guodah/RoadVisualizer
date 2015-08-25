package com.sim.intersections;

public enum Directions{
	SOUTH (0), EAST(1), NORTH(2), WEST(3);
	
	private int value;
	private Directions(int value){
		this.value = value;
	}
	
	public int value(){
		return value;
	}

}
