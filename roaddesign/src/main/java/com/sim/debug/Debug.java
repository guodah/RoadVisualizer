package com.sim.debug;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.sim.central.RoadDesign;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.gui.FrameComponents;
import com.sim.obj.CrossSection;


public class Debug {
	public static void init(){
		JFrame frame = (JFrame) RoadDesign.findComponent(
				FrameComponents.MAIN_FRAME);
		frame.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				char c = arg0.getKeyChar();
				if(c=='d' || c=='D'){
					DebugView.adhocView2();
				}
			}
			
		});
	}
	
	private static int count=0;
	private static Vector23f pts[] ={
		new Vector23f(-1878, 802, 0),
		new Vector23f(-188, -68, 0),
		new Vector23f(1997, 987, 0),
		new Vector23f(1797, -1093, 0),
		new Vector23f(2787, -1028, 0),
		new Vector23f(-333, 987, 0)
	};
	
	public static Vector23f getPt(){
		return pts[count++];
	}
	
	public static void printInput(ArrayList<RoadVector> inputPoints,
			ArrayList<Float> grades, ArrayList<Float> speeds,
			ArrayList<Float> superEles, CrossSection xsec) {
				
		System.out.println("****** User Input Coordinates ******");
		for(int i=0;i<inputPoints.size();i++){
			System.out.printf("\t%s\n", inputPoints.get(i));
		}
		
		System.out.println("****** Grades at Points ******");
		for(int i=0;i<grades.size();i++){
			System.out.printf("Grade #%d = %f\n", i, grades.get(i));
		}
		
		System.out.println("****** Speeds at Points ******");
		for(int i=0;i<speeds.size();i++){
			System.out.printf("Speed #%d = %f\n", i, speeds.get(i));
		}

		System.out.println("****** SuperElevations at Points ******");
		for(int i=0;i<superEles.size();i++){
			System.out.printf("SuperEles #%d = %f\n", i, superEles.get(i));
		}
		
		System.out.println("****** Cross Section Profile ******");
		System.out.println(xsec);
	}
}
