package com.sim.network.update;

import java.util.Map;

import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.network.RoadNetwork;
import com.sim.obj.*;
import com.sim.roads.*;

/**
 * This update request is created when the user left-click on the application
 * frame. Either a new road will be created or the unclosed road will be 
 * grown.
 * @author Dahai Guo
 *
 */
public class RoadAddUpdate extends NetworkUpdate{
	
	/**
	 * The first point of a new road or a point that
	 * will be grown to an existing road
	 */
	private Vector23f newPoint;

	public RoadAddUpdate(Vector23f newPoint) {
		this.newPoint = newPoint; 
	}

	public NetworkUpdateType getType() {
		return NetworkUpdateType.ROAD_POINT_ADDITION;
	}

	/**
	 * See {@link #newPoint}
	 * @return
	 */
	public Vector23f getNewPt() {
		return newPoint;
	}	
}
