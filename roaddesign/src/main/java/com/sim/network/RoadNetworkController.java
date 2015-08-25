package com.sim.network;

import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.util.*;

import com.sim.central.Logging;
import com.sim.central.RoadDesign;
import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;
import com.sim.gui.ViewMode;
import com.sim.gui.handler.NetworkGUIDrawer;
import com.sim.io.exporters.NetworkExporter;
import com.sim.io.exporters.basic.BasicNetworkExporter;
import com.sim.network.update.*;
import com.sim.roads.Road;


/**
 * This class is the interface to a road network. An instance of this class
 * is able to pass user actions to an existing road network.
 * 
 * @author Dahai Guo
 *
 */
public class RoadNetworkController {
	/**
	 * The handle through which the network is drawn
	 */
	private NetworkGUIDrawer networkGuiHandler;
	
	/**
	 * The directory of handles for dealing with requests to update 
	 * the road network
	 */
	private Map<NetworkUpdateType, NetworkUpdateHandler> handlers;
	
	/**
	 * The history updates that are completed.
	 */
	private Stack<NetworkUpdate> updates;
	
	/**
	 * The export for exporting the network 
	 */
	private NetworkExporter exporter;
	
	/**
	 * The road network interfaced with the controller
	 */
	private RoadNetwork network;
	
	/**
	 * Default constructor only for allocating spaces for variables.
	 */
	private RoadNetworkController(){
		handlers = new HashMap<NetworkUpdateType, NetworkUpdateHandler>();
		network = new RoadNetwork();
		updates = new Stack<NetworkUpdate>();
	}
	
	/**
	 * Registers a handler with respect to a update type
	 * @param updateType
	 * @param handler
	 */
	public void addHandler(NetworkUpdateType updateType, 
			NetworkUpdateHandler handler){
		handlers.put(updateType, handler);
	}
	
	/**
	 * Finds a handler to handles update request.
	 * <p>
	 * If ,  
	 * @param update update request
	 * @return false if no handler for the request is found or request was
	 *         handled successfully, true otherwise.  
	 */
	public boolean handle(NetworkUpdate update){
		NetworkUpdateHandler handler = handlers.get(update.getType());
		if(handler==null)
			return false;
		if(handler.handle(update, network)){
			updates.push(update);
			RoadDesign.update();
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Undoes the last update which is complete
	 */
	public void cancel(){
		if(updates.empty())
			return;
		NetworkUpdate update = updates.pop();
		NetworkUpdateHandler handler = handlers.get(update);
		handler.cancel();
		RoadDesign.update();
	}

	/**
	 * Exports the road network to the disk.
	 * 
	 * @param modelPath directory path where to export
	 * @throws FileNotFoundException
	 */
	public void export(String modelPath) 
			throws FileNotFoundException{
		exporter.export(modelPath);
	}
	
	/**
	 * Removes every road, intersection and terrain in the road network.
	 */
	public void reset(){
		network.removeAll();
	}

	/**
	 * Builds a controller that has been added a set of handlers.<p>
	 * See NetworkUpdateType.
	 * <p>
	 * The controller is able to handle two types of request<p>
	 * <ol>
	 * <li> {@link com.sim.network.update.NetworkUpdateType#ROAD_POINT_ADDITION}
	 * <li> {@link com.sim.network.update.NetworkUpdateType#ROAD_CLOSE}
	 * </ol>
	 * @return
	 */
	public static RoadNetworkController findController() {
		Logging.getLogger().info("Building road network controller");
		RoadNetworkController control =
			new RoadNetworkController();
		control.addHandler(NetworkUpdateType.ROAD_POINT_ADDITION, 
				new RoadAddHandler());
		control.addHandler(NetworkUpdateType.ROAD_CLOSE, 
				new RoadCloseHandler());
		control.networkGuiHandler = new NetworkGUIDrawer(control.network);
		control.exporter = new BasicNetworkExporter(control.network);
		Logging.getLogger().info("Building road network controller...Succeed");
		return control;
	}

	/**
	 * Draws the road network
	 * @see com.sim.gui.ViewMode
	 * @param g
	 * @param env
	 * @param viewMode
	 */
	public void draw(Graphics g, ThreeDEnv env, ViewMode viewMode) {
		networkGuiHandler.draw(g, env, viewMode);
	}

	/**
	 * Sets whether or not the terrain is shown in the interface with the user.
	 * @param show
	 */
	public void showTerrain(boolean show) {
		networkGuiHandler.showTerrain(show);
	}

	/**
	 * Queries the road network for where the current growing (edited) road
	 * should continue growing. 
	 * @return
	 */
	public RoadVector getOpenPt() {
		Road road = network.findUnclosedRoad();
		if(road==null){
			return null;
		}else{
			return road.getOpenPt();
		}
	}

	public RoadNetwork getNetwork() {
		return network;
	}
}
