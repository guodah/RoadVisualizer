package com.sim.roads;

import com.sim.geometries.RoadVector;
import com.sim.gui.handler.*;
import com.sim.io.exporters.RoadExporter;
import com.sim.network.RoadNetwork;
import com.sim.roads.basic.BasicRoad;


/**
 * This class is the super class that includes necessary variables and
 * methods.
 * 
 * @author Dahai Guo
 *
 */
public abstract class Road {
	
	/**
	 * Reference to the exporter class that can save a road object
	 * to the disk or other media.
	 */
	protected RoadExporter exporter;
	
	/**
	 * Reference to the gui handler class that is responsible for displaying 
	 * a road on the screen.
	 */
	protected GUIHandler guiHandler;
	
	/**
	 * Closes the road. 
	 * <p>
	 * Before being closed, the road is editable. After being closed, the road
	 * is no longer editable.
	 */
	public abstract void close();
	
	/**
	 * See method close.
	 * @return
	 */
	public abstract boolean isClosed();
	
	/**
	 * Adds (grows) a new key point to the road. 
	 * @param v
	 * @return true if increment succeeded, false otherwise
	 */
	public abstract boolean increment(RoadVector v);
	
	/**
	 * Undoes the recent added point.
	 */
	public abstract void decrement();

	/**
	 * Creates a new road.
	 * 
	 * @param roadType the type of the road (to date, only 
	 * 			{@link com.sim.roads.basic.BasicRoad} is implemented)
	 * @param v the first key point in the road
	 * @param network
	 * @return
	 */
	public static Road newRoad(RoadTypes roadType, RoadVector v, RoadNetwork network) {

		switch(roadType){
		case BASIC_ROAD:
			return BasicRoad.newUniRoad(v, network);
		default:
			return null;
		}
	}
	
	/**
	 * Returns the handler that is responsible for displaying this road in 
	 * the user interface
	 * @return {@link #guiHandler}
	 */
	public GUIHandler getGUIHandler() {
		return guiHandler;
	}
	
	/**
	 * Where the road is extensible if the road is open; null otherwise.
	 * @return
	 */
	public abstract RoadVector getOpenPt();
	
	/**
	 * The range of the road
	 * @return an array of {minX, minY, maxX, maxY}
	 */
	public abstract float[] getBoundingBox();
	
	/**
	 * Returns the exporter that is able to save the road to the disk.
	 * 
	 * @return {@link #exporter}
	 */
	public RoadExporter getExporter() {
		return exporter;
	}

}
