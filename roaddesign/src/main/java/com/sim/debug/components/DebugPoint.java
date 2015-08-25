package com.sim.debug.components;

import java.awt.Graphics;

import com.sim.geometries.BoundBox;
import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;
import com.sim.util.GraphicsUtil;


public class DebugPoint extends DebugComponent{
	RoadVector v;

	public DebugPoint(RoadVector v, String name){
		this.v = v;
		this.centroid = v;
		this.name = name;
	}
	
	@Override
	public void draw(Graphics g, ThreeDEnv env) {
		java.awt.Point p = env.projectScreen(v.getVector());
		g.fillRect(p.x-1, p.y-1, 3, 3);
		GraphicsUtil.drawString(g, v.getVector(), name, env);
	}

	@Override
	protected RoadVector findCentroid() {
		return v;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BoundBox findBoundBox() {
		BoundBox box = new BoundBox();
		box.maxX=v.getX(); box.minX=v.getX();
		box.maxY=v.getY(); box.minY=v.getY();
		return box;
	}

	
	
}
