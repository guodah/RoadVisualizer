package com.sim.gui;


//import sim.basic.BasicOuterPath;
//import sim.basic.BasicVectorMap;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

import com.sim.debug.DebugView;
import com.sim.debug.components.DebugMesh;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.network.RoadNetwork;
import com.sim.roads.basic.BasicRoad;

/**
 * This class arranges the graphical user interface for the entire application.
 * 
 * @author Dahai Guo
 *
 */
@SuppressWarnings("serial")
public class JRoadDesignFrame extends JFrame{

	public static final double WINDOW_WIDTH = 0.95;
	public static final double WINDOW_HEIGHT = 0.8;
	
	private HashMap<FrameComponents, Component> components;
	
	public static JRoadDesignFrame buildDesignFrame(String title){
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		JRoadDesignFrame frame=new JRoadDesignFrame(title, 
				(int)(WINDOW_WIDTH*dim.width), 
				(int)(WINDOW_HEIGHT*dim.height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
	
	public JRoadDesignFrame(String title, int width, int height){
		super(title);
		setSize(width, height);
		
		components = new HashMap<FrameComponents, Component>();

		JMenuBar menu = createMenuBar();
		this.setJMenuBar(menu);
		this.setFocusable(true);

		components.put(FrameComponents.MAIN_FRAME,this);
		
		JRoadDesignPanel drawPanel = new JRoadDesignPanel();
		components.put(FrameComponents.MAIN_DRAW_PANEL, drawPanel);
		
		add(createToolBar(), BorderLayout.NORTH);
		add(drawPanel, BorderLayout.CENTER);
	}
	
	public JToolBar createToolBar(){
		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(new EtchedBorder());
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
				
	    JButton action = new JButton(new ImageIcon("icons/new_32.png"));
	    action.setToolTipText("Create a new road network");
	    toolBar.add(action);
	    components.put(FrameComponents.TOOLBAR_NEW, action);
	    
	    action = new JButton(new ImageIcon("icons/save_32.png"));
	    toolBar.setToolTipText("Save the graphics model");
	    toolBar.add(action);
	    components.put(FrameComponents.TOOLBAR_SAVE_MODEL, action);
	    
	    action = new JButton(new ImageIcon("icons/render.png"));
	    action.setToolTipText("Render the road");
	    toolBar.add(action);
	    components.put(FrameComponents.TOOLBAR_RENDER, action);
	    
	    toolBar.addSeparator(new Dimension(10,10));
	    
	    JComboBox<ViewMode> viewModeBox = new JComboBox<ViewMode>(ViewMode.values());
	    toolBar.add(new JLabel("View Mode"));
	    toolBar.add(viewModeBox);
	    components.put(FrameComponents.TOOLBAR_VIEW_MODE_BOX, viewModeBox);
	    
	    JCheckBox terrainBox = new JCheckBox("Show Terrain");
	    toolBar.add(terrainBox);
	    components.put(FrameComponents.TOOLBAR_TERRAIN_CHECK, terrainBox);
	    	    
		return toolBar;
	}

	public JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenuItem item;
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		
		item = new JMenuItem("New...", KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		fileMenu.add(item);
		components.put(FrameComponents.MENU_FILE_NEW, item);

		item = new JMenuItem("Save", KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		fileMenu.add(item);
		components.put(FrameComponents.MENU_FILE_SAVE_MODEL, item);

		item = new JMenuItem("Save As", KeyEvent.VK_A);
		fileMenu.add(item);
		components.put(FrameComponents.MENU_FILE_SAVE_MODEL_AS, item);

		item = new JMenuItem("Exit", KeyEvent.VK_E);
		fileMenu.add(item);
		components.put(FrameComponents.MENU_FILE_EXIT, item);
		
		JMenu operationMenu = new JMenu("Operations");
		operationMenu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(operationMenu);

		item = new JMenuItem("Render", KeyEvent.VK_R);
		operationMenu.add(item);
		components.put(FrameComponents.MENU_OPERATIONS_RENDER, item);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		helpMenu.setEnabled(false);
		menuBar.add(helpMenu);
		
		return menuBar;
	}

	public Component findComponent(FrameComponents componentName){
		return components.get(componentName);
	}	
	
	public static void main(String args[]){
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		JRoadDesignFrame frame=new JRoadDesignFrame("Title", 
				(int)(WINDOW_WIDTH*dim.width), 
				(int)(WINDOW_HEIGHT*dim.height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
