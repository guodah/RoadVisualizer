package com.sim.network.update;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.sim.central.RoadDesign;
import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.network.RoadNetwork;
import com.sim.obj.CrossSection;
import com.sim.roads.Road;
import com.sim.roads.RoadTypes;
import com.sim.roads.basic.BasicRoad;



public class RoadAddHandler extends NetworkUpdateHandler {

	public void cancel() {
		throw new RuntimeException("Unimplemented");
	}

	/**
	 * Grows the unclosed road by another key point.
	 * @return true if the update is handled successfully, false otherwise
	 */
	public boolean handle(NetworkUpdate _update, RoadNetwork network) {
		
		RoadAddUpdate update = (RoadAddUpdate)_update;
		boolean result = true;
	//	System.out.println(update.getNewPt());
		// find the unclosed road (there can only be
		// one unclosed road)
		Road unclosedRoad = network.findUnclosedRoad();
		
		// increment the unclosed road if any; otherwise
		// create a new road
		if(unclosedRoad!=null){
			try{
				if(unclosedRoad.increment(new RoadVector(update.getNewPt()))){
				
					BasicIntersection intersection;
					do{
						intersection = 
								BasicIntersection.findNearestIntersections(
										(BasicRoad)unclosedRoad, network);
						if(intersection!=null){
							network.addIntersection(intersection);
							unclosedRoad = network.findUnclosedRoad();
						}
					}while(intersection!=null);
				}else{
					result = false;;
				}
			}catch(RuntimeException e){
				unclosedRoad.decrement();
				RoadDesign.displayError(e.toString(), "Point addition failed");
				result = false;
			}
		}else{
			Road road = Road.newRoad(RoadTypes.BASIC_ROAD,new RoadVector(
					update.getNewPt()), network); 
			
			if(road!=null){
				network.addRoad(road);
			}else{
				result = false;
			}
		}
		network.updateTerrain();
		return result;
	}

}
