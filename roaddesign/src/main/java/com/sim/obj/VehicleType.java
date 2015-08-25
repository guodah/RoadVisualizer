package com.sim.obj;

/**
 * ASHTO Exhibit 9-20
 * @author Dahai Guo
 *
 */
public enum VehicleType {
	P(0), 
	SU(1), 
	WB_40(2), 
	WB_50(3), 
	WB_62(4), 
	WB_67(5),
	WB_100T(6), 
	WB_109D(7);
	
	private final int typeNum;
	
	VehicleType(int v){
		typeNum = v;
	}
	
	public int getType(){
		return typeNum;
	}
	
	public static void main(String args[]){
	//	System.out.println(P.getType());
	}
}
