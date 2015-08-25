package com.sim.terrain.basic;

import java.util.ArrayList;

import com.sim.central.RoadDesign;
import com.sim.debug.DebugView;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.gui.handler.BasicTerrainGUIHandler;
import com.sim.gui.handler.GUIHandler;
import com.sim.gui.handler.NetworkGUIDrawer;
import com.sim.io.exporters.basic.BasicTerrainExporter;
import com.sim.network.RoadNetwork;
import com.sim.terrain.Terrain;
import com.sim.util.TriangulatorAdapter;



/**
 * The class is based on {@link BasicVectorMap BasicVectorMap}. It triangulates each inner hole. The
 * surrounding terrain is a circle, approximated by a 10 edge polygon. The outer
 * perimeter of each subnetwork is a hole to the surrounding terrain.
 * <p>
 * A BasicTerrain only defines a set of triangles. These triangles make a mesh with a uniform texture. 
 * 
 * @author Dahai Guo
 *
 */
public class BasicTerrain extends Terrain{
	
	/**
	 * Responsible for building the terrain.
	 */
	private BasicTerrainBuilder builder;
	
	/**
	 * Default constructor only for allocating spaces for variables
	 * @param network which network this terrain is made for
	 */
	public BasicTerrain(RoadNetwork network) {
		super(network);
		this.exporter = BasicTerrainExporter.getExporter(this);
		builder = (BasicTerrainBuilder) RoadDesign.getTerrainBuilder(
				BasicTerrainBuilder.class);
		guiHandler = new BasicTerrainGUIHandler(this);
	}

	/**
	 * Responds to the changes in the network.
	 */
	public void update(){
		builder.update(this);
	}

	/**
	 * See {@link com.sim.terrain.Terrain#triangles} 
	 * @param triangles
	 */
	protected void setTriangles(ArrayList<RoadVector> triangles) {
		this.triangles = triangles;		
	}

}
