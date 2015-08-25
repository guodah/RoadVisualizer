package com.sim.network.update;

/**
 * This class defines what users can create a new or do to an existing
 * road network. 
 * 
 * @author Dahai Guo
 *
 */
public enum NetworkUpdateType {
	/**
	 * Adds a point to create a new or add to an existing road.
	 * <p>
	 * This update is caused by a left mouse-click.
	 * <p>
	 * The new point extend the road with another triplet of information
	 * (grade, speed_limit, super-elevation). This new point is referred to 
	 * key point in this project. 
	 * <p>
	 * To date, the new key point is not allowed to cause a loop road. In
	 * other words, the new key point is not allowed to cause the road
	 * to intersect itself.
	 */
	ROAD_POINT_ADDITION, 
	
	/**
	 * Closes the road.
	 * <p>
	 * The user can only draw one road at a time. The editable road is referred
	 * to as "open road" in this project. When the user right-clicks on the screen,
	 * the road will be closed. Then the road is no longer editable. 
	 */
	ROAD_CLOSE
}
