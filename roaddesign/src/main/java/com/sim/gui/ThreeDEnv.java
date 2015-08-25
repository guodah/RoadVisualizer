package com.sim.gui;

import java.awt.Point;
import java.awt.geom.Point2D;

import com.sim.geometries.Plane3f;
import com.sim.geometries.Ray3f;
import com.sim.geometries.Vector23f;

/**
 * A three-D environment which is able to map screen coordinates to the world
 * coordinates or the other direction.
 * 
 * @author Dahai Guo
 *
 */
public class ThreeDEnv {
	private Vector23f eye;
	
	private final float viewPlaneFromEye;
	private Plane3f viewPlane;

	private final Plane3f ground;		
	private final float INIT_SCALE;
	
	private final float width;
	private final float height;
	
	private ThreeDEnv(float width, float height, float init_scale) {
		this.INIT_SCALE = init_scale;
		viewPlaneFromEye = (width>height)?width:height;
		
		this.width = width;
		this.height = height;
	
		// ground is at z=0
		ground = new Plane3f(new Vector23f(0,0,1),0);
	}

	public static ThreeDEnv findDefaultEnv(
			float width, float height, float scale){
		ThreeDEnv env = new ThreeDEnv(width, height, scale);
		env.defaultCaliber(width, height);
		return env;
	}
	
	public void moveEye(float deltaX, float deltaY, float deltaZ){
		eye.x -= deltaX;
		eye.y -= deltaY;
		
		if(eye.z-deltaZ>0){
			eye.z -= deltaZ;
		}

		findViewPlane();
	}
	
	public void defaultCaliber(float width, float height){
	
		eye = new Vector23f(width/2,
				height/2,INIT_SCALE*viewPlaneFromEye);

		findViewPlane();
	}

	
	private void findViewPlane(){
		if(viewPlane==null){
			viewPlane = new Plane3f(new Vector23f(0,0,1),0);
		}
		viewPlane.d = eye.z - viewPlaneFromEye;
	}
	
	public Point projectScreen(Vector23f p){
		Ray3f eye2P = new Ray3f(eye, p);
		Vector23f proj = eye2P.intersect(viewPlane);
		
		proj.x -= (eye.x - width/2);
		proj.y -= (eye.y - height/2);

		return new Point((int)proj.x,(int)proj.y);  
	}

	public Vector23f projectGroud(Point2D.Float _p){
		Vector23f p = new Vector23f(_p.x + (eye.x - width/2), 
				_p.y + (eye.y - height/2), viewPlane.d);
		Ray3f eye2P = new Ray3f(eye, p);
		Vector23f proj = eye2P.intersect(ground);

		return proj;  		
	}

	public void moveEyeTo(float x, float y) {
		eye.x = x;
		eye.y = y;
	}

	public void fitRange(float width, float height) {
		float z1 = this.viewPlaneFromEye*(width/this.width);
		float z2 = this.viewPlaneFromEye*(height/this.height);
		eye.z = (z1>z2)?z1:z2;
	}
}
