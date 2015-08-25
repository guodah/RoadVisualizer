package com.sim.util;

import java.util.*;

import com.sim.geometries.*;


public class ModelGenUtil {
	
	/**
	 * Finds a two dimensional circle that surrounds a two dimensional
	 * array lists of points.
	 * 
	 * @param twoDPoints two dimensional array list of points
	 * @param scale the more the value is, the larger the surrounding circle is
	 * @return
	 */
	public static Circle2f findSurroundCircle2d(
			ArrayList<ArrayList<RoadVector>> twoDPoints,
			float scale
			) {
		float maxX=-1e+10f, maxY=-1e+10f;
		float minX=1e+10f, minY=1e+10f;
		float centerX=0, centerY=0;
		int count = 0;
		for(int i=0;i<twoDPoints.size();i++){
			for(int j=0;j<twoDPoints.get(i).size();j++){
				RoadVector v = twoDPoints.get(i).get(j);
				
				if(v.getX() > maxX){
					maxX = v.getX();
				}

				if(v.getX() < minX){
					minX = v.getX();
				}
				
				if(v.getY() > maxY){
					maxY = v.getY();
				}
				
				if(v.getY() < minY){
					minY = v.getY();
				}
				
				centerX += v.getX();
				centerY += v.getY();
				count++;
			}
		}

		float radius = Math.max(scale*(maxX-minX)/2, 
				scale*(maxY-minY)/2);
		centerX /= count;
		centerY /= count;

		return new Circle2f(new Vector23f(centerX, centerY,0),radius);
	}
	
	/**
	 * Finds a polygon which bounds a sequence of cross sectional profiles.
	 * @param xsecs
	 * @return
	 */
	public static ArrayList<RoadVector> findSurroundPoly(
			ArrayList<ArrayList<RoadVector>> xsecs){
		ArrayList<RoadVector> perimeter1 = new ArrayList<RoadVector>();
        for(int i=0;i<xsecs.size();i++){
        	RoadVector v = xsecs.get(i).get(0);
        	if(i<=1){
        		perimeter1.add(v);
        		continue;
        	}else{
        		int size = perimeter1.size();
        		RoadVector v1 = perimeter1.get(size-1);
        		RoadVector v2 = perimeter1.get(size-2);
        		RoadVector dir1 = RoadVector.subtract(v1, v2);
        		RoadVector dir2 = RoadVector.subtract(v, v1);
        		
        		if(dir1.acuteTurn(dir2)){
        			perimeter1.add(v);
        		}
        	}
        }
        
        int width = xsecs.get(0).size();
        ArrayList<RoadVector> perimeter2 = new ArrayList<RoadVector>(); 
        for(int i=xsecs.size()-1;i>=0;i--){
        	RoadVector v = xsecs.get(i).get(width-1);
         	if(i>=xsecs.size()-2){
        		perimeter2.add(v);
        		continue;
        	}else{
        		int size = perimeter2.size();
        		RoadVector v1 = perimeter2.get(size-1);
        		RoadVector v2 = perimeter2.get(size-2);
        		RoadVector dir1 = RoadVector.subtract(v1, v2);
        		RoadVector dir2 = RoadVector.subtract(v, v1);
        		
        		if(dir1.acuteTurn(dir2)){
        			perimeter2.add(v);
        		}
        	}
        }

        perimeter1.addAll(perimeter2);
        return perimeter1;
	}
	
	public static ArrayList<RoadVector> translate(
			ArrayList<RoadVector> pts, Vector23f base){
		for(int i=0;i<pts.size();i++){
			Vector23f v = pts.get(i).getVector();
			pts.get(i).setX(v.x+base.x);
			pts.get(i).setY(v.y+base.y);
			pts.get(i).setZ(v.z+base.z);
		}
		return pts;
	}
	
	/*
	 * only works in the XY plane
	 */
	public static float[] inTriangle(Vector23f v, Vector23f v0, 
			Vector23f v1, Vector23f v2, float delta){
		float area = Vector23f.cross(Vector23f.subtract(v1, v0).clearZ(), 
				Vector23f.subtract(v2, v0).clearZ()).magnitude()/2;
		
		float area1 = Vector23f.cross(Vector23f.subtract(v, v2).clearZ(), 
				Vector23f.subtract(v1, v2).clearZ()).magnitude()/2;
		float area2 = Vector23f.cross(Vector23f.subtract(v, v0).clearZ(), 
				Vector23f.subtract(v2, v0).clearZ()).magnitude()/2;
		float area3 = Vector23f.cross(Vector23f.subtract(v, v1).clearZ(), 
				Vector23f.subtract(v0, v1).clearZ()).magnitude()/2;
		
		if(CompareUtil.floatCompare(area-area1-area2-area3, 0, delta)){
			return new float[]{area1/area,area2/area,area3/area};
		}else{
			return null;
		}
	}
}
