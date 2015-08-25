package com.sim.terrain;

import java.util.ArrayList;
import com.sim.geometries.RoadVector;
import com.sim.gui.handler.*;
import com.sim.io.exporters.TerrainExporter;
import com.sim.network.RoadNetwork;


/**
 * This class is the super class of all terrain classes.
 * 
 * @author Dahai Guo
 *
 */
public abstract class Terrain {
	/**
	 * Reference to the gui handler class that is responsible for displaying 
	 * a terrain object on the screen.
	 */
	protected GUIHandler guiHandler;

	/**
	 * Reference to the exporter class that can save a terrain object
	 * to the disk or other media.
	 */
	protected TerrainExporter exporter;
	
	/**
	 * Which road network this terrain is made for
	 */
	protected RoadNetwork network;
	
	/**
	 * Its graphics model.
	 * <p>
	 * Every three points make a triangle.
	 */
	protected ArrayList<RoadVector> triangles;
	
	/**
	 * Default constructor only for allocating spaces for the variable.
	 * @param network See {@link #network}
	 */
	public Terrain(RoadNetwork network){
		this.network = network;
		triangles = new ArrayList<RoadVector>();		
	}
	
	/**
	 * Should be called when the road network is updated so the
	 * terrain needs to be updated
	 */
	public abstract void update();
	
	/**
	 * Returns the mesh
	 * @return {@link #triangles}
	 */
	public ArrayList<RoadVector> getTriangles(){
		return triangles;
	}

	/**
	 * Returns the object that is able to draw this terrain in the user interface
	 * @return {@link #guiHandler}
	 */
	public GUIHandler getGUIHandler(){
		return this.guiHandler;
	}

	/**
	 * Returns the object, being able to export this terrain to the disk
	 * @return {@link #exporter}
	 */
	public TerrainExporter getExporter() {
		return exporter;
	}

	/**
	 * Returns which network this terrain is made for
	 * @return {@link #network}
	 */
	public RoadNetwork getNetwork() {
		return network;
	}

	/**
	 * Empty the graphics model, so clearing the terrain.
	 */
	public void clear() {
		triangles.clear();
	}
	
	/**
	 * Finds the range of the terrain.
	 * <p>
	 * Note, in most cases, the road network is surrounded by a terrain.
	 * @return an array {minX, minY, maxX, maxY}
	 */
	public float [] getBoundingBox(){
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		
		ArrayList<RoadVector> triangles = this.getTriangles();
		
		for(int i=0;i<triangles.size();i++){
			RoadVector v = triangles.get(i);
			
			minX = Math.min(minX, v.getX());
			minY = Math.min(minY, v.getY());
			maxX = Math.max(maxX, v.getX());
			maxY = Math.max(maxY, v.getY());
		}
		
		return new float[]{minX,minY,maxX,maxY};
	}
}
