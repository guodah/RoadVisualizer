package com.sim.debug.components;

import java.awt.Graphics;
import java.util.ArrayList;

import com.sim.geometries.BoundBox;
import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;
import com.sim.util.GraphicsUtil;


public class DebugXsec extends DebugComponent{
	ArrayList<ArrayList<RoadVector>> xsecs;
	
	public DebugXsec(ArrayList<ArrayList<RoadVector>> xsecs, String name){
		this.xsecs = xsecs;
		centroid = findCentroid();
		this.name = name;
	}
	
	public void draw(Graphics g, ThreeDEnv env){
		GraphicsUtil.drawCrossSections(g, xsecs, env);
		GraphicsUtil.drawString(g, xsecs.get(0).get(0).getVector(), name, env);
	}

	@Override
	protected RoadVector findCentroid() {
		float x, y, z;
		int size = 0;
		x=y=z=0;
		for(int i=0;i<xsecs.size();i++){
			for(int j=0;j<xsecs.get(i).size();j++){
				x += xsecs.get(i).get(j).getX();
				y += xsecs.get(i).get(j).getY();
				z += xsecs.get(i).get(j).getZ();
			}
			size += xsecs.get(i).size();
		}
		
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
		for(ArrayList<RoadVector> xsec : xsecs){
			for(RoadVector v : xsec){
				if(v.getY()>max){
					max=v.getY();
				}
				if(v.getY()<min){
					min=v.getY();
				}
			}
		}
		box.maxY=max;box.minY=min;
		
		min=Float.MAX_VALUE; max=Float.MIN_VALUE;
		for(ArrayList<RoadVector> xsec : xsecs){
			for(RoadVector v : xsec){
				if(v.getX()>max){
					max=v.getX();
				}
				if(v.getX()<min){
					min=v.getX();
				}
			}
		}
		box.maxX=max;box.minX=min;
		
		return box;
	}
}
