package com.sim.util;


import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import com.sim.core.basic.FlagUtil;
import com.sim.core.basic.ModelGenConsts;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.gui.*;


/**
 * All methods in this class draws things via a Graphics object. In addition, 
 * each method needs the caller to pass a ThreeDEnv object for converting
 * the points in the world coordinates to the screen coordinates.
 * <p>
 * Methods do not check the validity of arguments.
 * 
 * @author Dahai Guo
 *
 */
public class GraphicsUtil {
	
	/**
	 * Draws a list of cross sectional profiles. Each cross sectional profile is 
	 * in form of a list of points with absolute coordinates.
	 * 
	 * @param g
	 * @param lines
	 * @param env
	 */
	public static void drawCrossSections(Graphics g, 
			ArrayList<ArrayList<RoadVector>> lines, com.sim.gui.ThreeDEnv env){
		if(lines==null){
			return;
		}
		for(int i=0;i<lines.size();i++){
			ArrayList<RoadVector> list = lines.get(i);
			drawPolyLine(g,list, env);
		}
	}
	
	/**
	 * Draws a straight line.
	 * @param g
	 * @param p1
	 * @param p2
	 * @param env
	 */
	public static void drawLine(Graphics g, RoadVector p1, RoadVector p2,
			ThreeDEnv env){
		Point _p1 = env.projectScreen(p1.getVector());
		Point _p2 = env.projectScreen(p2.getVector());
		
		g.drawLine((int)_p1.x, (int)_p1.y, (int)_p2.x, (int)_p2.y);
	}
	
	/**
	 * Draws a polyline (unclosed).
	 * 
	 * @param g
	 * @param polyLine
	 * @param env
	 */
	public static void drawPolyLine(Graphics g, ArrayList<RoadVector> polyLine,
			com.sim.gui.ThreeDEnv env){
		
		if(polyLine==null || polyLine.size()<2){
			return;
		}
		
		RoadVector p1 = polyLine.get(0);
		Point _p1 = env.projectScreen(p1.getVector());
		
		for(int i=0;i<polyLine.size()-1;i++){

		//	g.setColor(Color.black);
			RoadVector p2 = polyLine.get(i+1);
			Point _p2 = env.projectScreen(p2.getVector());

			g.drawLine((int)_p1.x, (int)_p1.y, (int)_p2.x, (int)_p2.y);

			g.fillRect((int)_p1.x, (int)_p1.y, 3, 3);

			g.fillRect((int)_p2.x, (int)_p2.y, 3, 3);
			
		//	g.drawString(i+"", _p2.x, _p2.y);
			
			p1 = p2;
			_p1 = _p2;
		}
	}
	
	/**
	 * Draws a polygon. (closed)
	 * 
	 * @param g
	 * @param pts
	 * @param env
	 */
	public static void drawPolygon(Graphics g, ArrayList<RoadVector> pts,
			com.sim.gui.ThreeDEnv env){
		
		if(pts==null){
			return;
		}
		
		drawPolyLine(g, pts, env);
		RoadVector p1 = pts.get(0);
		RoadVector p2 = pts.get(pts.size()-1);
		
		Point _p1 = env.projectScreen(p1.getVector());
		Point _p2 = env.projectScreen(p2.getVector());
		
		g.drawLine(_p1.x, _p1.y, _p2.x, _p2.y);
	}
	
	/**
	 * Draws cross sectional profile to show its graphics model in form of
	 * triangle meshes.
	 * 
	 * @param g
	 * @param crossSections
	 * @param env
	 */
	public static void drawCrossSectionTriangles(Graphics g, 
			ArrayList<ArrayList<RoadVector>> crossSections,
			com.sim.gui.ThreeDEnv env){
		
		if(crossSections==null){
			return;
		}
		
	//	drawPolyLine(g, curvePoints);
		Stroke s = ((Graphics2D)g).getStroke();
	    Stroke drawingStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, 
	    		BasicStroke.JOIN_BEVEL);
	    ((Graphics2D)g).setStroke(drawingStroke);
		drawCrossSections(g, crossSections, env);
		
	    drawingStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
	    		BasicStroke.JOIN_BEVEL);
	    ((Graphics2D)g).setStroke(drawingStroke);
	    
		for(int i=0;i<crossSections.size()-1;i++){
			ArrayList<RoadVector> cs1 = crossSections.get(i);
			ArrayList<RoadVector> cs2 = crossSections.get(i+1);
			
			int loopCount = cs1.size();
			for(int j=0;j<loopCount;j++){
				RoadVector p1 = cs1.get(j);
				RoadVector p2 = cs2.get(j);
				Point _p1 = env.projectScreen(p1.getVector());
				Point _p2 = env.projectScreen(p2.getVector());
				
				g.drawLine((int)_p1.x, (int)_p1.y, 
						(int)_p2.x, (int)_p2.y);
			}			
		}
		

	    drawingStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
	    		BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
	    ((Graphics2D)g).setStroke(drawingStroke);
		for(int i=0;i<crossSections.size()-1;i++){
			ArrayList<RoadVector> cs1 = crossSections.get(i);
			ArrayList<RoadVector> cs2 = crossSections.get(i+1);
				
			int loopCount = cs1.size();
			for(int j=0;j<loopCount-1;j++){
				RoadVector p1 = cs1.get(j);
				RoadVector p2 = cs2.get(j+1);
				Point _p1 = env.projectScreen(p1.getVector());
				Point _p2 = env.projectScreen(p2.getVector());
				
				g.drawLine((int)_p1.x, (int)_p1.y, 
						(int)_p2.x, (int)_p2.y);				
			}
		}

		((Graphics2D)g).setStroke(s);
	}

	/**
	 * Draws a BasicTerrain's graphics model.
	 * 
	 * @param g
	 * @param terrain
	 * @param env
	 */
	public static void drawBasicTerrain(Graphics g, ArrayList<RoadVector> terrain, 
			com.sim.gui.ThreeDEnv env) {
		
		if(terrain==null){
			return;
		}
		
		for(int i=0;i<terrain.size();i+=3){
			RoadVector v1 = terrain.get(i);
			RoadVector v2 = terrain.get(i+1);
			RoadVector v3 = terrain.get(i+2);
			g.setColor(Color.red);
			drawTriangle(g, v1, v2, v3, env);
			g.setColor(Color.black);
		}
	}

	/**
	 * Draws a triangle.
	 * 
	 * @param g
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param env
	 */
	public static void drawTriangle(Graphics g, 
			RoadVector v1, RoadVector v2, RoadVector v3,
			com.sim.gui.ThreeDEnv env) {
		int x[] = new int[3];
		int y[] = new int[3];
		
		Point p1 = env.projectScreen(v1.getVector());
		Point p2 = env.projectScreen(v2.getVector());
		Point p3 = env.projectScreen(v3.getVector());
		
		x[0]=(int) p1.x; x[1]=(int) p2.x; x[2]=(int) p3.x;
		y[0]=(int) p1.y; y[1]=(int) p2.y; y[2]=(int) p3.y;
		
		for(int i=0;i<3;i++){
			g.drawRect(x[i], y[i], 3, 3);
		}
		
		g.drawPolygon(x, y, 3);	
	}

	/**
	 * Fills triangles.
	 * 
	 * @param g
	 * @param triangles every three points make a triangle. 
	 * @param c
	 * @param env
	 */
	public static void fillTriangles(Graphics g, RoadVector[] triangles, Color c, 
			com.sim.gui.ThreeDEnv env){
		if(triangles==null){
			return;
		}
		for(int i=0;i<triangles.length/3;i++){
			fillTriangle(g, triangles[3*i],triangles[3*i+1],
					triangles[3*i+2], c, env);		
		}
	}

	/**
	 * Draws triangles.
	 * 
	 * @param g
	 * @param triangles every three points make a triangle. 
	 * @param env
	 */
	public static void drawTriangles(Graphics g, RoadVector[] triangles, 
			com.sim.gui.ThreeDEnv env){
		
		if(triangles==null){
			return;
		}
		for(int i=0;i<triangles.length/3;i++){
			drawTriangle(g, triangles[3*i],triangles[3*i+1],
					triangles[3*i+2], env);		
		}
	}
	
	/**
	 * Fills triangles.
	 * 
	 * @param g
	 * @param crossSections the cross sections along the road center line 
	 * @param env
	 */
	public static void fillTriangles(Graphics g, ArrayList<ArrayList<RoadVector>> crossSections,
			Color color, com.sim.gui.ThreeDEnv env) {
		
		if(crossSections==null){
			return;
		}
		
		for(int i=0;i<crossSections.size()-1;i++){
			ArrayList<RoadVector> list1 = crossSections.get(i);
			ArrayList<RoadVector> list2 = crossSections.get(i+1);
			
			for(int j=0;j<list1.size()-1;j++){
				RoadVector v11 = list1.get(j);
				RoadVector v12 = list1.get(j+1);
				RoadVector v21 = list2.get(j);
				RoadVector v22 = list2.get(j+1);
				
				int type = v11.crossSectionType;

				fillTriangle(g, v11, v12, v21, color, env);
				fillTriangle(g, v12, v22, v21, color, env);
			}
		}		
	}
	
	/**
	 * Fills triangles in a graphics model for cross sectional profiles.
	 * @param g
	 * @param crossSections
	 * @param env
	 */
	public static void fillTriangles(Graphics g, ArrayList<ArrayList<RoadVector>> crossSections,
			com.sim.gui.ThreeDEnv env) {

		if(crossSections==null){
			return;
		}
				
		for(int i=0;i<crossSections.size()-1;i++){
			ArrayList<RoadVector> list1 = crossSections.get(i);
			ArrayList<RoadVector> list2 = crossSections.get(i+1);
			
			for(int j=0;j<list1.size()-1;j++){
				RoadVector v11 = list1.get(j);
				RoadVector v12 = list1.get(j+1);
				RoadVector v21 = list2.get(j);
				RoadVector v22 = list2.get(j+1);
				
				int type = v11.crossSectionType;
				

				fillTriangle(g, v11, v12, v21, type, env);
				fillTriangle(g, v12, v22, v21, type, env);
			}
		}
	}

	/**
	 * Writes a string at a specific location.
	 * 
	 * @param g
	 * @param v
	 * @param msg
	 * @param env
	 */
	public static void drawString(Graphics g, Vector23f v, String msg, com.sim.gui.ThreeDEnv env){
		Point p = env.projectScreen(v);
		
		g.drawString(msg, (int)p.x, (int)p.y);
	}

	/**
	 * Fills a triangle.
	 * 
	 * @param g
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param color
	 * @param env
	 */
	public static void fillTriangle(Graphics g, RoadVector v1, RoadVector v2, 
			RoadVector v3, Color color, com.sim.gui.ThreeDEnv env){
		Color old = g.getColor();
		g.setColor(color);
		
		int x[] = new int[3];
		int y[] = new int[3];
		
		Point p1 = env.projectScreen(v1.getVector());
		Point p2 = env.projectScreen(v2.getVector());
		Point p3 = env.projectScreen(v3.getVector());
		
		x[0]=(int) p1.x; x[1]=(int) p2.x; x[2]=(int) p3.x;
		y[0]=(int) p1.y; y[1]=(int) p2.y; y[2]=(int) p3.y;
		g.fillPolygon(x, y, 3);		
		
		g.setColor(old);
	}
	
	/**
	 * Fills a triangle. The color in represented by the cross section type. See
	 * ModelGenConsts.
	 * 
	 * @param g
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param crossSectionType
	 * @param env
	 */
	public static void fillTriangle(Graphics g, RoadVector v1, RoadVector v2, 
			RoadVector v3, int crossSectionType, com.sim.gui.ThreeDEnv env){
		
		Color color = null;
		
		switch(crossSectionType){
		case ModelGenConsts.MEDIAN:
			color = Color.green;
			break;
		case ModelGenConsts.ROAD_SURFACE:
			color = new Color(102,51,0);
			break;
		case ModelGenConsts.SHOULDER:
			color = new Color(0,200,0);
			break;
		case ModelGenConsts.WHITE_MARKER:
			color = Color.WHITE;
			break;
		case ModelGenConsts.YELLOW_MARKER:
			color = Color.yellow;
			break;
		}

		fillTriangle(g, v1, v2, v3, color, env);
	}

}
