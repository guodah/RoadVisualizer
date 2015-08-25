package com.sim.util;

import java.util.ArrayList;

import com.sim.geometries.RoadVector;


public class SearchUtil {
	
	/**
	 * Finds if and where a certain point in on a path in form of 
	 * the sequence of points. 
	 *  
	 * @param path a sequence of points along a road
	 * @param pt 
	 * @return -1 if pt is not on path; otherwise index of which point pt is right 
	 *         after 
	 */
	public static int findInCurve(ArrayList<RoadVector> path, RoadVector pt){
		int cutIndex = -1;
		RoadVector v1 = path.get(0);
		for(int i=0;i<path.size()-1;i++){
			RoadVector v2 = path.get(i+1);
			
			if(RoadVector.isBetween2d(v1, v2, pt)){
				cutIndex = i;
			}
			
			v1 = v2;
		}
		return cutIndex;
	}
}
