package com.sim.intersections;


import java.util.*;

import com.sim.geometries.Vector23f;
import com.sim.gui.handler.GUIHandler;
import com.sim.io.exporters.IntersectionExporter;
import com.sim.roads.Road;


public abstract class Intersection implements Cloneable{
	/**
	 * This type intersection requires the two approaches to 
	 * have 
	 * <ul>
	 * <li> zero grade
	 * <li> the same elevation
	 * <li> no either horizontal or vertical alignment
	 * <li> (both) median, wider than a lane width
	 * </ul>
	 * 
	 * The only parameter is the radii. It considers passenger cars. 
	 */
	public final static String BASIC_TYPE = "basic";
	
	protected IntersectionExporter exporter;
	protected GUIHandler guiHandler;
//	protected Map<Road, Road> directions; // Key: upstream, down: downstream
//	protected abstract void findIntersectionModel();
	
	public GUIHandler getGUIHandler() {
		return guiHandler;
	}

	public abstract float[] getBoundingBox();

	public IntersectionExporter getExporter() {
		return exporter;
	}
}
