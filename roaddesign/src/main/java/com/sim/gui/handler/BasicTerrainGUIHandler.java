package com.sim.gui.handler;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import com.sim.geometries.RoadVector;
import com.sim.gui.ThreeDEnv;
import com.sim.gui.ViewMode;
import com.sim.terrain.basic.BasicTerrain;
import com.sim.util.GraphicsUtil;


/**
 * This class is able to draw a basic terrain in one of 
 * the three modes: <p>
 * <ol>
 * <li> draw the triangles of graphics model
 * <li> draw the graphics model with color
 * </ol>
 * 
 * @author Dahai Guo
 *
 */
public class BasicTerrainGUIHandler implements GUIHandler{

	private BasicTerrain terrain;
	public BasicTerrainGUIHandler(BasicTerrain terrain){
		this.terrain = terrain;
	}
	@Override
	public void draw(Graphics g, ThreeDEnv env, ViewMode mode) {
		if(mode==ViewMode.CENTER_LINE){
			return;
		}
		ArrayList<RoadVector> triangles = terrain.getTriangles();
		RoadVector _triangles[] = new RoadVector[triangles.size()];
		triangles.toArray(_triangles);
		if(mode==ViewMode.TRIANGLE){
			GraphicsUtil.drawTriangles(g, _triangles, env);
		}else{
			Color c = g.getColor();
			g.setColor(Color.yellow);
			GraphicsUtil.fillTriangles(g, _triangles, c, env);
		}
	}
}
