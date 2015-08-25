package com.sim.network.update;

import com.sim.geometries.Vector23f;
import com.sim.network.RoadNetwork;
import com.sim.roads.Road;

/**
 * An update request, generated when the user left-click to close the growth
 * of a new road.
 * @author Dahai Guo
 *
 */
public class RoadCloseHandler extends NetworkUpdateHandler{

	public void cancel(){
		throw new RuntimeException("Unimplemented");
	}

	/**
	 * Closes the open road in the network.
	 * @return true if the update is handled successfully, false otherwise
	 */
	@Override
	public boolean handle(NetworkUpdate _update, RoadNetwork network) {
		RoadCloseUpdate update = (RoadCloseUpdate)_update;
		// find the unclosed road (there can only be
		// one unclosed road)
		Road unclosedRoad = network.findUnclosedRoad();
		
		// increment the unclosed road if any; otherwise
		// create a new road
		if(unclosedRoad!=null){
			unclosedRoad.close();
			return true;
		}else{
			return false;
		}
	}
}
