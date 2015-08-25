package com.sim.debug.components;

import java.awt.Graphics;
import java.util.ArrayList;

import com.sim.geometries.BoundBox;
import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;
import com.sim.util.GraphicsUtil;


public class DebugPolyLine extends DebugComponent{
	ArrayList<RoadVector> line;

	public DebugPolyLine(ArrayList<RoadVector> pts, String name) {
		line = pts;
		centroid = findCentroid();
		this.name = name;
	}

	@Override
	public void draw(Graphics g, ThreeDEnv env) {
		GraphicsUtil.drawPolyLine(g, line, env);
		GraphicsUtil.drawString(g, line.get(0).getVector(), name, env);
	}

	@Override
	protected RoadVector findCentroid() {
		float x, y, z;
		x=y=z=0;
		for(int i=0;i<line.size();i++){
			x += line.get(i).getX();
			y += line.get(i).getY();
			z += line.get(i).getZ();
		}
		int size = line.size();
		return new RoadVector(x/size, y/size, z/size);

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BoundBox findBoundBox() {
		BoundBox box = new BoundBox();
		float min=Float.MAX_VALUE, max=Float.MIN_VALUE;
		for(RoadVector v : line){
			if(v.getY()>max){
				max=v.getY();
			}
			if(v.getY()<min){
				min=v.getY();
			}
		}
		box.maxY=max;box.minY=min;
		
		min=Float.MAX_VALUE; max=Float.MIN_VALUE;
		for(RoadVector v : line){
			if(v.getX()>max){
				max=v.getX();
			}
			if(v.getX()<min){
				min=v.getX();
			}
		}
		box.maxX=max;box.minX=min;
		return box;
	}
}
