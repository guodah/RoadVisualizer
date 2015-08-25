package com.sim.gui;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

import com.sim.central.RoadDesign;
import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.FlagUtil;
import com.sim.core.basic.ModelGenConsts;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.curves.HorizontalCurve;
import com.sim.curves.VerticalCurve;
import com.sim.geometries.Geom3f;
import com.sim.geometries.Plane3f;
import com.sim.geometries.Ray3f;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.intersections.Intersection;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.roads.Road;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.GraphicsUtil;


/**
 * This panel is for viewing set of 3D points, whose span on the z axis
 * is much less than the one on either x or y axis.
 *  
 * @author Owner
 *
 */
public class JRoadDesignPanel extends JPanel{
	
	protected void paintComponent(Graphics g){
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		RoadDesign.draw(g);
	}
}
