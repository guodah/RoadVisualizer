package com.sim.gui;

import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.*;

import com.sim.central.RoadDesign;
import com.sim.obj.CrossSection;


/**
 * Part of JXsecEditor to visualize the cross section being edited.
 * @author Dahai Guo
 *
 */
public class JXsecDrawPanel extends JPanel{
	JXsecEditor parentEditor;
	public static final int MARGIN = 20;
	public static final float MAGNIFIER = 7.0f;
	
	private CrossSection xsec;
	
	public JXsecDrawPanel(JXsecEditor parent){
		parentEditor = parent;
		xsec = parentEditor.readXsec();
	}
	
	protected void paintComponent(Graphics g){
		
		// read from the editor
		parentEditor.readXsec(xsec);
		
		int width = getWidth();
		int height = getHeight();
		
		g.clearRect(0,0,width, height);
		
		Stroke oldStroke = ((Graphics2D)g).getStroke();
	    Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
	    		BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		
		int leftX = width/2;
		int leftY = height/4;
		
		int rightX = leftX;
		int rightY = leftY;
		
		int textY = height*6/8;
		

		
		float zoom = (width-2*MARGIN)/
				(xsec.medianWidth+xsec.shoulderWidth*2.0f+
				xsec.laneWidth*xsec.numOfLanes*2.0f);
		
		int tempX, tempY;
		// draw median
		if(Float.compare(xsec.medianWidth,0)!=0){
			leftX = (int) (leftX-zoom*xsec.medianWidth/2);
			rightX = (int) (rightX+zoom*xsec.medianWidth/2);
			g.drawLine(leftX, leftY, rightX, rightY);
			
			g.drawLine(leftX, leftY, leftX, leftY+5);
			g.drawLine(rightX, rightY, rightX, rightY+5);
			
			rightY += 5;
			leftY += 5;
			
		    ((Graphics2D)g).setStroke(dashed);
		    g.drawLine(leftX, height/8, leftX, height*7/8);
		    g.drawString("Median", (leftX+rightX)/2-17, textY);
		    g.drawLine(rightX, height/8, rightX, height*7/8);
		    ((Graphics2D)g).setStroke(oldStroke);
		}
		
		// draw lanes
		for(int i=0;i<xsec.numOfLanes;i++){
			// draw left lane
			tempX = (int) (leftX - zoom*xsec.laneWidth);
			tempY = (int) (leftY + zoom*xsec.laneWidth*
					(xsec.laneSlope*MAGNIFIER/100));
			g.drawLine(leftX, leftY, tempX, tempY);
			g.drawString("Lane #"+(i+1), (leftX+tempX)/2-20, textY);
			leftX = tempX; leftY = tempY;
			
			// draw right lane
			tempX = (int) (rightX + zoom*xsec.laneWidth);
			tempY = (int) (rightY + zoom*xsec.laneWidth*
					(xsec.laneSlope*MAGNIFIER/100));
			g.drawLine(rightX, rightY, tempX, tempY);
			g.drawString("Lane #"+(i+1), (rightX+tempX)/2-20, textY);
			rightX = tempX; rightY = tempY;
			
		    ((Graphics2D)g).setStroke(dashed);
		    g.drawLine(leftX, height/8, leftX, height*7/8);
		    g.drawLine(rightX, height/8, rightX, height*7/8);
		    ((Graphics2D)g).setStroke(oldStroke);

		}
		
		// draw shoulder
		if(Float.compare(xsec.shoulderWidth, 0.0f)!=0){
			// draw left shoulder
			tempX = (int) (leftX - zoom*xsec.shoulderWidth);
			tempY = (int) (leftY + zoom*xsec.shoulderWidth*
					(xsec.shoulderSlope*MAGNIFIER/100));
			g.drawLine(leftX, leftY, tempX, tempY);
			g.drawString("Shoulder", (leftX+tempX)/2-20, textY);
			leftX = tempX; leftY = tempY;
			
			// draw right shoulder
			tempX = (int) (rightX + zoom*xsec.shoulderWidth);
			tempY = (int) (rightY + zoom*xsec.shoulderWidth*
					(xsec.shoulderSlope*MAGNIFIER/100));
			g.drawLine(rightX, rightY, tempX, tempY);
			g.drawString("Shoulder", (rightX+tempX)/2-20, textY);
			rightX = tempX; rightY = tempY;
			
		}
	}
}
