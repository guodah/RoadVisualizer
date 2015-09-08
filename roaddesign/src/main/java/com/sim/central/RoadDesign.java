package com.sim.central;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;


import com.jme.app.AbstractGame.ConfigShowMode;
import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.FlagUtil;
import com.sim.core.basic.ModelGenConsts;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.curves.CurveParam;
import com.sim.debug.Debug;
import com.sim.debug.DebugView;
import com.sim.debug.components.DebugMesh;
import com.sim.debug.components.DebugPolyLine;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.gui.FrameComponents;
import com.sim.gui.JCurveDialog;
import com.sim.gui.JRoadDesignFrame;
import com.sim.gui.JXsecChooser;
import com.sim.gui.ThreeDEnv;
import com.sim.gui.ViewMode;
import com.sim.gui.WorkMode;
import com.sim.gui.handler.NetworkGUIDrawer;
import com.sim.intersections.Intersection;
import com.sim.intersections.IntersectionBuilder;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.network.RoadNetwork;
import com.sim.network.RoadNetworkController;
import com.sim.network.update.NetworkUpdate;
import com.sim.obj.CrossSection;
import com.sim.random.RoadUtil;
import com.sim.renderer.AutoGenLoader;
import com.sim.roads.Road;
import com.sim.roads.RoadBuilder;
import com.sim.roads.basic.BasicRoad;
import com.sim.terrain.*;
import com.sim.util.ArgumentList;
import com.sim.util.ModelExporterUtils;

/**
 * This class centralizes the communication in the road design process. It mainly
 * connects three components:<p>
 * <ol>
 * <li> A logic component. The interface with that component is 
 * {@link com.sim.network.RoadNetworkController}.
 * <li> An editable interface where the user can edit the road network. 
 * See {@link com.sim.gui.FrameComponents FrameComponents}
 * <li> A game engine, used to render the road network. See 
 * {@link com.sim.renderer.AutoGenLoader AutoGenLoader}
 * </ol>
 *  <p>
 *  This class also loads properties from the disk. The property names are recorded
 *  as instance variables in this class as follows
 *  <ol>
 *  <li> {@link #TITLE}
 *  <li> {@link #TEXTURE_DIRECTORY}
 *  <li> {@link #TEXTURE_DIR}
 *  <li> {@link #MODEL_PATH}
 *  <li> {@link #INIT_SCALE}
 *  <li> {@link #MODEL_DIR}
 *  <li> {@link #START_VIEW_PATH}
 *  <li> {@link RoadDesign#LOG_FILE_NAME}
 *  </ol>
 *  
 * @author Dahai Guo
 * 
 */
public class RoadDesign {
	
	/**
	 * flag of execution flag
	 */
	public static boolean DEBUG = false;
	
	/**
	 * Title of the application window
	 */
	public static final String TITLE = "title";
	
	/**
	 * Path to file which stores the texture information for meshes
	 */
	public static final String TEXTURE_DIRECTORY = "texture_directory";
	
	/**
	 * Directory to where texture files are saved
	 */
	public static final String TEXTURE_DIR = "texture_dir";
	
	/**
	 * Directory to where the current model is saved. When the application
	 * starts to run, this property does not exist. The user needs sets it.
	 */
	public static final String MODEL_PATH = "model_path";
	
	/**
	 * How the real size of the network compares what it shows 
	 * on the design panel.
	 */
	public static final String INIT_SCALE = "init_scale";
	
	/**
	 * In {@link #MODEL_DIR MODEL_DIR}, there should exists a file which
	 * records all the files in MODEL_DIR.
	 */
	public static final String MODEL_DIR = "model_dir";
	
	/**
	 * Where the view point is when the road model is rendered.
	 */
	public static final String START_VIEW_PATH = "start_view";
	
	/**
	 * Filename for the heightmap
	 */
	public static final String HEIGHT_MAP_FILE = "heightmap_name";
	
	/**
	 * Width of the height map
	 */
	public static final String HEIGHT_MAP_WIDTH = "heightmap_width";
	
	/**
	 * Height of the height map
	 */
	public static final String HEIGHT_MAP_HEIGHT = "heightmap_height";

	/**
	 * The file that contains the scale of the height map
	 */
	public static final String HEIGHT_MAP_SCALE_FILE = "heightmap_scale_file";

	/**
	 * The log file name.
	 */
	public static final String LOG_FILE_NAME = "log_file"; 
	
	/**
	 * The interface to the logical data which, at the top level, is 
	 * represented as a {@link com.sim.network.RoadNetwork RoadNetwork} object.
	 */
	private static RoadNetworkController networkControl;
	
	/**
	 * When the user is editing the road network, the value of this variable
	 * decides what form the network appears. See 
	 * {@link com.sim.gui.ViewMode ViewMode}.
	 */
	private static ViewMode viewMode;
	
	/**
	 * When the user is editing the road network, the value of this variable
	 * decides the way in which the user is editing and/or evaluating the network.
	 */
	private static WorkMode workMode;
	
	/**
	 * The three dimensional view parameters associated with how users edits and/or
	 * evaluating the network.
	 */
	private static Map<WorkMode, ThreeDEnv> envs;
	
	/**
	 * A temporary place in which the user inputs are cached.
	 */
	private static Stack<Object> dropBox = new Stack<Object>();
	
	/**
	 * A central place where the builders for network components are registered.
	 */
	private static Builders builders;
	
	/**
	 * GUI frame which is the highest level GUI component.
	 */
	private static JRoadDesignFrame frame;

	/**
	 * Offline global properties.
	 */
	protected static Properties properties;

	/**
	 * Starts the application. <p>
	 * It fails when it could not find the properties file.
	 * 
	 * @param propertiesPath where the properties are saved
	 * @throws IOException thrown when the properties files is missing
	 */
	public static void start(String propertiesPath) {
		try{
			loadProperties(propertiesPath);
		}catch(IOException e){
			RoadDesign.displayError("RoadDesign failed to start", 
					"The properties file is missing.");
			System.exit(1);
		}

		if(!Logging.init()){
			int userInput = JOptionPane.showConfirmDialog(null, 
					"Logging did not start correctly. "+
					"Do you want to continue?", 
					"Logging Error", 
					JOptionPane.OK_CANCEL_OPTION, 
					JOptionPane.WARNING_MESSAGE);
			
			if(userInput!=JOptionPane.OK_OPTION){
				System.exit(1);
			}
		}

		Logging.getLogger().info("Starting RoadDesign");
		
		// build the application frame
		frame=JRoadDesignFrame.buildDesignFrame(getProperty(TITLE));

		// install the event handlers for the components 
		// in the application frame
		installHandlers();
		
		// install the builders for the components in the road network
		installBuilders();

		// finds the interface to the road network
		networkControl = RoadNetworkController.findController();
		
		// global linear buffer for communication
//		dropBox = new Stack<Object>();
		
		viewMode = ViewMode.CENTER_LINE;
		workMode = WorkMode.NORMAL;		
		
		create3DEnv();
		getFrame().setVisible(true);
		Logging.getLogger().info("RoadDesign started");
	}
	
	/**
	 * Finds 3D parameters for the user interface. <p>
	 * Note this is not related to the game engine. 
	 */
	private static void create3DEnv() {
		int width = getFrame().getWidth();
		int height = getFrame().getHeight();
		envs = new HashMap<WorkMode, ThreeDEnv>();

		for(WorkMode mode : WorkMode.values()){
			ThreeDEnv env = ThreeDEnv.findDefaultEnv(
					width, height, 
					new Float(getProperty(INIT_SCALE)));
			envs.put(mode, env);
		}
	}

	/**
	 *  Installs the builder for the components in the road network
	 */
	private static void installBuilders(){
		Logging.getLogger().info("Installing builders for network components...");
		builders = Builders.installBuilders();
		Logging.getLogger().info("Installing builders for network components...Succeed");
	}

	/**
	 * Finds the actual road builder object.
	 * 
	 * @param c specifies which type of road builder wanted
	 * @return the actual road builder object
	 */
	public static RoadBuilder getRoadBuilder(Class<? extends RoadBuilder> c){
		return builders.findRoadBuilder(c);
	}
	
	/**
	 * Finds the actual intersection builder object.
	 * 
	 * @param c specifies which type of intersection builder wanted
	 * @return the actual intersection builder object
	 */
	public static IntersectionBuilder getIntersectionBuilder(
			Class<? extends IntersectionBuilder>c){
		return builders.findIntersectionBuilder(c);
	}

	/**
	 * Finds the actual terrain builder object.
	 * 
	 * @param c specifies which type of terrain builder wanted
	 * @return the actual terrain builder object
	 */
	public static TerrainBuilder getTerrainBuilder(
			Class<? extends TerrainBuilder>c){
		return builders.findTerrainBuilder(c);
	}
	
	/**
	 * Finds and installs the event handlers for the component in the 
	 * application frame.
	 */
	private static void installHandlers() {
		Logging.getLogger().info("Installing event handlers "+
				"for application frame components...");
		ComponentHandlers componentHandlers = ComponentHandlers.createHandlers();
		for(FrameComponents componentName : FrameComponents.values()){
			Component component = 
				getFrame().findComponent(componentName);
			EventListener handler = 
				componentHandlers.findHandler(componentName);
			
			if(component instanceof AbstractButton){
				((AbstractButton)component).
					addActionListener((ActionListener) handler);
			}else if(component instanceof JComboBox){
				((JComboBox)component).
					addActionListener((ActionListener) handler);
			}else if(component instanceof JPanel){
				((JPanel)component).addMouseListener(
						(MouseListener) handler);
				((JPanel)component).addMouseMotionListener(
						(MouseMotionListener) handler);
				((JPanel)component).addMouseWheelListener(
						(MouseWheelListener) handler);
			}
		}
		Logging.getLogger().info("Installing event handlers for "+
				"application frame components...Succeed");

	}

	private static void loadProperties(String propertiesPath) 
		throws IOException {
		properties = new Properties();
		properties.load(new FileInputStream(propertiesPath));
	}

	/**
	 * Returns the value of a property. 
	 * @see com.sim.central.RoadDesign
	 * @param var property name
	 * @return property value
	 */
	public static String getProperty(String var){
		if(properties==null)
			return null;
		else
			return properties.getProperty(var);
	}
	
	/**
	 * @return the property value of {@link #MODEL_DIR MODEL_DIR}
	 */
	public static String getModelDir(){
		return getProperty(MODEL_DIR);
	}
	
	/**
	 * Sets a property value given a property name
	 * @param prop property name
	 * @param value property value
	 */
	public static void setProperty(String prop, String value){
		properties.setProperty(prop, value);
	}
	
	/**
	 * Removes a property value given a property name
	 * @param prop property name
	 */
	public static void removeProperty(String prop){
		properties.remove(prop);
	}
	
	/**
	 * Finds a component in the application frame.
	 * @see com.sim.gui.FrameComponents
	 * @param componentName 
	 * @return the component object
	 */
	public static Component findComponent(FrameComponents componentName){
		return getFrame().findComponent(componentName);
	}

	/**
	 * Displays an error message in a pop-up window.
	 * @param error error msg
	 * @param title title of the window
	 */
	public static void displayError(String error, String title){
		displayError(error, title, true);
	}

	/**
	 * Displays an error message in a pop-up window.
	 * @param error error msg
	 * @param title title of the window
	 * @param log whether or not this message will be logged
	 */
	public static void displayError(String error, String title, boolean log){
		
		JOptionPane.showMessageDialog(getFrame(), 
				error, title, JOptionPane.ERROR_MESSAGE);
		if(log)
			Logging.getLogger().severe("ERROR: "+error);
	}
	
	/**
	 * Invokes a game engine (so far it is jME) to render the road network.
	 */
	public static void render() {
		String modelPath = getProperty(MODEL_PATH);
		if(modelPath == null){
			return;
		}
		
		ModelPathState state = ModelPathState.checkModelPathBasic(modelPath);
		if(state!=ModelPathState.OK){
			RoadDesign.displayError("The files in "+modelPath+" is not consistent", 
					"Data Error");
			Logging.getLogger().severe("Renderer failed: "+
					"The files in "+modelPath+" is not consistent");
			return;
		}
		
		
		Thread t = new Thread(new Runnable(){
		
			@Override
			public void run() {
	
				try{
					Logging.getLogger().info("Starting the renderer...");
					AutoGenLoader app = 
							new AutoGenLoader(); // Create Object
				
					app.setConfigShowMode(ConfigShowMode.AlwaysShow);
					//app.setConfigShowMode(ConfigShowMode.NeverShow);
					app.start(); // Start the program
				
					app.closeDisplaySystem();
					Logging.getLogger().info("The renderer completes successfully");
				}catch(IllegalStateException e){
					Logging.getLogger().severe(
							"The renderer could not display the model!");
					displayError(e.toString(), "Could not display the model!");
				}
			}			
		});
		t.start();
	}

	/**
	 * Passes an update to the road network to the network controller
	 * @param update the user initiated update
	 * @return true if the update is successfully handled, false otherwise
	 */
	public static boolean handleNetworkUpdate(NetworkUpdate update){
		return networkControl.handle(update);
	}
	
	/**
	 * Draws the road network.
	 * @param g the graphics object in the application frame
	 */
	public static void draw(Graphics g){
		networkControl.draw(g, envs.get(workMode), viewMode);
		
		RoadVector pt = networkControl.getOpenPt();
		
		Point p1 = (pt!=null)?envs.get(workMode).
				projectScreen(pt.getVector()):null;
		Point p2 = findComponent(FrameComponents.MAIN_DRAW_PANEL).
				getMousePosition();
		
		if(p1!=null && p2!=null){
			Color c = g.getColor();
			g.setColor(Color.black);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			g.setColor(c);
		}
	}
	
	/**
	 * Clears all data in the network.
	 */
	public static void reset() {
		networkControl.reset();
		removeProperty(MODEL_PATH);
		Logging.getLogger().info("Road Network Reset");
	}

	/**
	 * Enables or disables the showing of the terrrin in the application frame
	 * @param show
	 */
	public static void showTerrain(boolean show) {
		networkControl.showTerrain(show);
	}
	
	
	public static void setViewMode(ViewMode mode){
		viewMode = mode;		
	}

	/**
	 * Maps screen coordinates to the group (real world) coordinates.
	 * @param x
	 * @param y
	 * @return
	 */
	public static Vector23f mapToGround(int x, int y) {
		Point2D.Float _p = new Point2D.Float(x, y);
		return envs.get(workMode).projectGroud(_p);
	}

	/**
	 * Drops an object to a global buffer whose principle is last-in-first-out
	 * @param obj
	 */
	public static void drop(Object obj) {
		dropBox.push(obj);
	}

	public static void setWorkMode(WorkMode mode) {
		workMode = mode;
	}

	/**
	 * Handles the user action to the change the view point of the application frame.
	 * <p>
	 * The user inputs the offset of the eye point.
	 * @param deltaX
	 * @param deltaY
	 * @param deltaZ
	 */
	public static void handleEnvUpdate(float deltaX, float deltaY, float deltaZ) {
		ThreeDEnv env = envs.get(workMode);
		env.moveEye(deltaX, deltaY, deltaZ);
	}
	
	/**
	 * Reads the cross section from the user.
	 * @param v where the user initiated the request for a cross section profile.
	 * @return
	 */
	public static CrossSection getXsec(Vector23f v){
		Point p = envs.get(workMode).projectScreen(v);
		JXsecChooser.askForXsec(
			getFrame().getLocation().x+p.x, 
			getFrame().getLocation().y+p.y);
		CrossSection xsec = (!dropBox.isEmpty())
				?(CrossSection)dropBox.pop():null;
		if(xsec!=null){
			Logging.getLogger().info(String.format("XSEC: %s", 	xsec));
		}
		return xsec;
	}

	public static Object popDropBox(){
		return dropBox.empty()?null:dropBox.pop();
	}
	
	/**
	 * Reads curve point parameters from the user when 
	 * he/she adds another curve point.
	 * @param v where the user initiated the request for curve point parameters
	 * @return
	 */
	public static Map<CurveParam, Float> getCurveParam(Vector23f v) {
		Point p = envs.get(workMode).projectScreen(v);
		JCurveDialog.askForCurveParam(
			getFrame(),"Curve Parameters", getFrame().getLocation().x+p.x, 
			getFrame().getLocation().y+p.y);
		Map<CurveParam, Float> result = (Map<CurveParam, Float>) dropBox.pop();
		if(result!=null){
			String params = "";
			for(CurveParam param: result.keySet()){
				params += String.format("%s=%.3f ",
						param,result.get(param));
			}
			Logging.getLogger().info("CURVE_PARAM: "+params);
		}
		return result;
	}
	
	/**
	 * Called when the road network is updated to update the 
	 * display in the application frame.
	 */
	public static void update(){
		
		RoadNetwork network = networkControl.getNetwork();
		
		JPanel panel = (JPanel) findComponent(FrameComponents.MAIN_DRAW_PANEL);
		panel.repaint();
	}
	
	public static void main(String args[]) {
		
		String propertyFileName = ArgumentList.parseArgs(args);
			
		if(propertyFileName==null){
			propertyFileName = "RoadDesign.properties";
		}
		RoadDesign.start(propertyFileName);
	}

	/**
	 * Returns the path to the file which saves the start viewpoint in the renderer
	 * @return the value of {@link #START_VIEW_PATH START_VIEW_PATH}
	 */
	public static String getStartViewPath() {
		return getProperty(START_VIEW_PATH);
	}

	/**
	 * Returns the path to the mesh file which saves the texture info for the meshes
	 * @return the value of {@link #TEXTURE_DIRECTORY MESH_FILE}
	 */
	public static String getMeshFilePath() {
		return getProperty(TEXTURE_DIRECTORY);
	}

	/**
	 * Returns the path to where the model is saved
	 * @return the value of {@link #MODEL_PATH MODEL_PATH}
	 */
	public static String getModelPath() {
		return getProperty(MODEL_PATH);
	}

	/**
	 * Returns the path to where textures are saved
	 * @return the value of {@link #TEXTURE_DIR TEXTURE_DIR}
	 */
	public static String getTexturePath() {
		return getProperty(TEXTURE_DIR);
	}

	/**
	 * Returns the object for the application frame
	 * @return the application frame
	 */
	public static JRoadDesignFrame getFrame() {
		return frame;
	}

	public static RoadNetworkController getNetworkControl() {
		return networkControl;
	}

	/**
	 * Displays an (YES,NO,CANCEL} confirm window.
	 * 
	 * @param msg
	 * @param title
	 * @return
	 */
	public static int confirm(String msg, String title) {
		int userInput = JOptionPane.showConfirmDialog(null, 
				msg,title, 
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.WARNING_MESSAGE);
		if(userInput!=JOptionPane.CANCEL_OPTION){
			Logging.getLogger().info("USER_CHOICE: "+
					((userInput==JOptionPane.YES_OPTION)?
							"YES ":"NO ")+
					msg
			);
		}
		return userInput;
	}
}