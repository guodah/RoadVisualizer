package com.sim.debug.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import com.sim.geometries.BoundBox;
import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;


public abstract class DebugComponent {
	public abstract void draw(Graphics g, ThreeDEnv env);
	public RoadVector getCentroid(){
		return centroid;
	}
	protected abstract RoadVector findCentroid();
	protected RoadVector centroid;
	
	public static RoadVector findCentroid(ArrayList<DebugComponent> components){
		float x, y, z, size;
		x=y=z=0;
		size = components.size();
		
		for(DebugComponent c : components){
			RoadVector centroid = c.getCentroid();
			x += centroid.getX();
			y += centroid.getY();
			z += centroid.getZ();
		}
		
		return new RoadVector(x/size, y/size, z/size);
	}
	
	public abstract BoundBox findBoundBox();
	
	protected String name;
	public abstract String getName();
	public static float findHeight(ArrayList<DebugComponent> components) {
		float max=Float.MIN_VALUE, min=Float.MAX_VALUE;
		for(DebugComponent c : components){
			BoundBox box = c.findBoundBox();
			max = (max>box.maxY)?max:box.maxY;
			min = (min<box.minY)?min:box.minY;
		}
		return max-min;
	}
	
	public static float findWidth(ArrayList<DebugComponent> components) {
		float max=Float.MIN_VALUE, min=Float.MAX_VALUE;
		for(DebugComponent c : components){
			BoundBox box = c.findBoundBox();
			max = (max>box.maxX)?max:box.maxX;
			min = (min<box.minX)?min:box.minX;
		}
		return max-min;
	}
}
