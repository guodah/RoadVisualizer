package com.sim.renderer;


import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;


import java.net.URL;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;

import com.jme.app.BaseGame;
import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.FirstPersonHandler;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.joystick.JoystickInput;
import com.jme.light.PointLight;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.Text;
import com.jme.scene.TriMesh;
import com.jme.scene.Spatial.TextureCombineMode;
import com.jme.scene.shape.Box;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jme.util.geom.BufferUtils;
import com.jme.util.geom.Debugger;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.ImageBasedHeightMap;
import com.sim.central.RoadDesign;
import com.sim.io.exporters.basic.BasicHeightMapExporter;
import com.sim.util.CompareUtil;
import com.sim.util.ModelImporter;
 
/**
 * Started Date: Jul 29, 2004<br>
 * Revised Date: Jun 11, 2012<br>
 * <br>
 * 
 * Is used to demonstrate the inner workings of SimpleGame.
 * 
 * <p>
 * 
 * For the RoadDesign project, Dahai Guo revised "initSystem" by adding the code
 * for loading eye point and graphics models.
 * 
 * @author Jack Lindamood and Dahai Guo
 */
public class AutoGenLoader extends BaseGame {
	
	public static final int SHOULDER = 0;
	public static final int MEDIAN = 1;
	public static final int WHITE_MARKER = 3;
	public static final int YELLOW_MARKER = 4;
	public static final int PAVEMENT = 5;
	
	private static final Logger logger = Logger.getLogger(AutoGenLoader.class
			.getName());
 
//	public static String MODEL_PATH="RoadModel\\model20";
//	public static String MESH_FILE;
	
	public static void main(String[] args) throws IOException {
		
		AutoGenLoader app = new AutoGenLoader();
		app.setConfigShowMode(ConfigShowMode.AlwaysShow);
		app.start();
	}
	
	public AutoGenLoader(){
		super();
		setConfigShowMode(ConfigShowMode.AlwaysShow);
	}
	
	/** The camera that we see through. */
	protected Camera cam;
	/** The root of our normal scene graph. */
	protected Node rootNode;
	/** Handles our mouse/keyboard input. */
	protected InputHandler input;
	/** High resolution timer for jME. */
	protected Timer timer;
	/** The root node of our text. */
	protected Node fpsNode;
	/** Displays all the lovely information at the bottom. */
	protected Text fps;
	/** Simply an easy way to get at timer.getTimePerFrame(). */
	protected float tpf;
	/** True if the renderer should display bounds. */
	protected boolean showBounds = false;
 
	/** A wirestate to turn on and off for the rootNode */
	protected WireframeState wireState;
	/** A lightstate to turn on and off for the rootNode */
	protected LightState lightState;
	
	protected TriMesh [] meshes;
 
	/** Location of the font for jME's text at the bottom */
	public static String fontLocation = "com/jme/app/defaultfont.tga";
	
	private BufferedImage heightMap;
	private short [] heightMapRaster;
	
	private float [] heightMapRange;
	private TerrainBlock tb;
 
	/**
	 * This is called every frame in BaseGame.start()
	 * 
	 * @param interpolation
	 *            unused in this implementation
	 * @see com.jme.app.AbstractGame#update(float interpolation)
	 */
	protected final void update(float interpolation) {
		/** Recalculate the framerate. */
		timer.update();
		/** Update tpf to time per frame according to the Timer. */
		tpf = timer.getTimePerFrame();
		/** Check for key/mouse updates. */
		input.update(tpf);
		/** Send the fps to our fps bar at the bottom. */
		fps.print("FPS: " + (int) timer.getFrameRate());
		/** Call simpleUpdate in any derived classes of SimpleGame. */
		simpleUpdate();
 
		// updating the sky box
	    sb.setLocalTranslation(cam.getLocation());
	    sb.updateGeometricState(0, true);
		
		/** Update controllers/render states/transforms/bounds for rootNode. */
		rootNode.updateGeometricState(tpf, true);
 
		/** If toggle_wire is a valid command (via key T), change wirestates. */
		if (KeyBindingManager.getKeyBindingManager().isValidCommand(
				"toggle_wire", false)) {
			wireState.setEnabled(!wireState.isEnabled());
			rootNode.updateRenderState();
		}
		/** If toggle_lights is a valid command (via key L), change lightstate. */
		if (KeyBindingManager.getKeyBindingManager().isValidCommand(
				"toggle_lights", false)) {
			lightState.setEnabled(!lightState.isEnabled());
			rootNode.updateRenderState();
		}
		/** If toggle_bounds is a valid command (via key B), change bounds. */
		if (KeyBindingManager.getKeyBindingManager().isValidCommand(
				"toggle_bounds", false)) {
			showBounds = !showBounds;
		}
		/** If camera_out is a valid command (via key C), show camera location. */
		if (KeyBindingManager.getKeyBindingManager().isValidCommand(
				"camera_out", false)) {
			logger.info("Camera at: "
					+ display.getRenderer().getCamera().getLocation());
		}
 
		updateCamera();
				
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("exit",
				false)) {
			finish();
		}
 
	}
 
	/**
	 * Given the camera's position in XY plane, finds its elevation.
	 */
	private void updateCamera(){
		float x = cam.getLocation().x;
		float y = cam.getLocation().y;
		float z = getElevation(heightMap, x, y, this.heightMapRange);
		cam.getLocation().z = z + 2.0f;
		cam.update();
	}
	
//	private float preEle = Float.NaN;
//	private int preRow = Integer.MAX_VALUE;
//	private int preCol = Integer.MAX_VALUE;
//	private static long counter = 0;
	
	/**
	 * Given the camera's position in XY plane, finds its elevation in a height map.
	 * 
	 * @param heightMap the height map image
	 * @param x x coordinate of the camera
	 * @param y y coordinate of the camera
	 * @param range (minx, maxx, miny, maxy, minz, maxz) of the road network
	 * @return
	 */
	private float getElevation(BufferedImage heightMap, float x, float y, float[] range){
		float minX = range[BasicHeightMapExporter.MINX];
		float maxX = range[BasicHeightMapExporter.MAXX];
		float minY = range[BasicHeightMapExporter.MINY];
		float maxY = range[BasicHeightMapExporter.MAXY];
		float minZ = range[BasicHeightMapExporter.MINZ];
		float maxZ = range[BasicHeightMapExporter.MAXZ];
		float width = maxX - minX;
		float height = maxY - minY;
		float thickness = maxZ - minZ;
		int zero = 65535; // used to zero out the upper half of the 32-bit integer
				
		int col, row;
		col = (int) (((x-minX)/width)*heightMap.getWidth());
		row = (int) (((y-minY)/height)*heightMap.getHeight());

		col = (col>=heightMap.getWidth())?heightMap.getWidth()-1:col;
		row = (row>=heightMap.getHeight())?heightMap.getHeight()-1:row;

		int gray = zero & heightMapRaster[row*heightMap.getWidth()+col];
		float eleBL = (float) (minZ + (gray/65535.0)*thickness);

		gray = zero & heightMapRaster[row*heightMap.getWidth()+col+1];
		float eleBR = (float) (minZ + (gray/65535.0)*thickness);

		gray = zero & heightMapRaster[(row+1)*heightMap.getWidth()+col];
		float eleTL = (float) (minZ + (gray/65535.0)*thickness);

		gray = zero & heightMapRaster[(row+1)*heightMap.getWidth()+col+1];
		float eleTR = (float) (minZ + (gray/65535.0)*thickness);

		float u = ((x-minX)/width)*heightMap.getWidth() - col;
		float v = ((y-minY)/height)*heightMap.getHeight() - row;

		float ele = 0;
		if(u>v){
			ele = (1-u)*eleBL + (u-v)*eleBR + v*eleTR;
		}else{
			ele = (1-v)*eleBL + (v-u)*eleTL + u*eleTR;
		}
		return ele;
	}
//		float alpha = 0.2f;
//		if(!Float.isNaN(preEle))
//			ele = ele*(1-alpha)+alpha*preEle;
		
//		float ele = (1-v)*((1-u)*eleBL+u*eleBR) + v*((1-u)*eleTL+u*eleTR);
		
//		if(counter%20==0){
//			System.out.printf("x=%.3f, y=%.3f, col=%d, row=%d, BL=%.3f, BR=%.3f, TL=%.3f, TR=%.3f, u=%.3f, v=%.3f preEle =%.3f ele=%.3f\n",
//					x, y, col, row, eleBL, eleBR, eleTL, eleTR, u, v, preEle, ele);			
//			counter=0;
//		}else{
//			counter++;
//		}
		
/*		
		if(CompareUtil.floatCompare(ele-preEle, 0)){ 
		//			row!=preRow || col!=preCol){
			System.out.printf("x=%.3f, y=%.3f, col=%d, row=%d, BL=%.3f, BR=%.3f, TL=%.3f, TR=%.3f, u=%.3f, v=%.3f preEle =%.3f ele=%.3f\n",
					x, y, col, row, eleBL, eleBR, eleTL, eleTR, u, v, preEle, ele);			
		}

		preRow = row;
		preCol = col;
		preEle = ele;
		return ele;
	}
*/	
	public void closeDisplaySystem(){
		super.quit();
//		if(!display.isClosing()){
//			display.close();
//		}
	}
	
	/**
	 * This is called every frame in BaseGame.start(), after update()
	 * 
	 * @param interpolation
	 *            unused in this implementation
	 * @see com.jme.app.AbstractGame#render(float interpolation)
	 */
	protected final void render(float interpolation) {
		/** Clears the previously rendered information. */
		display.getRenderer().clearBuffers();
		/** Draw the rootNode and all its children. */
		display.getRenderer().draw(rootNode);
				
		/**
		 * If showing bounds, draw rootNode's bounds, and the bounds of all its
		 * children.
		 */
		if (showBounds)
			Debugger.drawBounds(rootNode, display.getRenderer());
		/** Draw the fps node to show the fancy information at the bottom. */
		display.getRenderer().draw(fpsNode);
		/** Call simpleRender() in any derived classes. */
		simpleRender();
	}
 
	/** A sky box for our scene. */
	Skybox sb;
	/**
	 * Creates display, sets up camera, and binds keys. Called in
	 * BaseGame.start() directly after the dialog box.
	 * 
	 * @see com.jme.app.AbstractGame#initSystem()
	 */
	protected final void initSystem() {
		try {
			/**
			 * Get a DisplaySystem acording to the renderer selected in the
			 * startup box.
			 */
			TextureManager.clearCache();
			DisplaySystem.resetSystemProvider();
			display = DisplaySystem.getDisplaySystem(settings.getRenderer());
			/** Create a window with the startup box's information. */
			display.createWindow(settings.getWidth(), settings.getHeight(),
					settings.getDepth(), settings.getFrequency(), settings
							.isFullscreen());
			/**
			 * Create a camera specific to the DisplaySystem that works with the
			 * display's width and height
			 */
			cam = display.getRenderer().createCamera(display.getWidth(),
					display.getHeight());
 
		} catch (JmeException e) {
			/**
			 * If the displaysystem can't be initialized correctly, exit
			 * instantly.
			 */
			logger.log(Level.SEVERE, "Could not create displaySystem", e);
			System.exit(1);
		}
 
		/** Set a black background. */
		display.getRenderer().setBackgroundColor(ColorRGBA.gray.clone());
 
		
		/** set up the model IO (Written by Dahai Guo) **/
//		AutoGenIO.display = display;
		ModelImporter.display = display;
		
		Vector3f [] camParams = new Vector3f[4];
		camParams[0] = new Vector3f(); // loc
		camParams[1] = new Vector3f(); // dir
		camParams[2] = new Vector3f(); // up
		camParams[3] = new Vector3f(); // left
		
		/** Load the mesh to center the eye point (Written by Dahai Guo) **/
		try {
			String modelPath = RoadDesign.getModelPath();
			meshes = ModelImporter.loadModel(modelPath, camParams);
			heightMap = ModelImporter.loadHeightMap(modelPath);
			heightMapRange = ModelImporter.loadHeightMapRange(modelPath);
			heightMapRaster = (short[]) heightMap.getData().getDataElements(0, 0, 
					heightMap.getWidth(), heightMap.getHeight(), null);
			
//			System.out.printf("The heightmap length is %d\n", heightMapRaster.length);
//			System.out.printf("(256,256): %d\n", heightMapRaster[512*256+256]);
//			System.out.printf("(257,256): %d\n", heightMapRaster[512*256+257]);
//			System.out.printf("(256,257): %d\n", heightMapRaster[512*257+256]);
//			System.out.printf("(257,257): %d\n", heightMapRaster[512*257+257]);
			
			BufferedImage im = ImageIO.read(new File("model//height.png")); 
	        ImageBasedHeightMap ib=new ImageBasedHeightMap(im);
	        tb = new TerrainBlock("heightmap", ib.getSize(),
	        	new Vector3f(
	        		(heightMapRange[1]-heightMapRange[0])/im.getWidth(),
	        		(heightMapRange[5]-heightMapRange[4])/65535,
	        		(heightMapRange[3]-heightMapRange[2])/im.getHeight()
	        	),
	        	ib.getHeightMap(),
	        	new Vector3f(0,0,0)
	        );
	        

			
		} catch (IOException e) {
			throw new IllegalStateException("Model files not complete!");
		}

		sb = ModelImporter.setupSky();
//		sb.setLocalTranslation(centroid.x, centroid.y, 0);
		
		/** Set up how our camera sees. */
//		cam.setFrustumPerspective(45.0f, (float) display.getWidth()
//				/ (float) display.getHeight(), 1, 1000);
		cam.setFrustumPerspective(60.0f, (float) display.getWidth()
		/ (float) display.getHeight(), 1, 5000);
		
		/** Move our camera to a correct place and orientation. */
		cam.setFrame(camParams[0], camParams[1], camParams[2], camParams[3]);
//		cam.setFrame(loc, left, up, dir);
		/** Signal that we've changed our camera's location/frustum. */
		cam.update();
		/** Assign the camera to this renderer. */
		display.getRenderer().setCamera(cam);
 
		/** Create a basic input controller. */
		FirstPersonHandler firstPersonHandler = new FirstPersonHandler(cam);
		/** Signal to all key inputs they should work 10x faster. */
		firstPersonHandler.getKeyboardLookHandler().setActionSpeed(70f);
		firstPersonHandler.getMouseLookHandler().setActionSpeed(0.2f);
		input = firstPersonHandler;
 
		/** Get a high resolution timer for FPS updates. */
		timer = Timer.getTimer();
 
		/** Sets the title of our display. */
		display.setTitle("SimpleGame");
 
		/** Assign key T to action "toggle_wire". */
		KeyBindingManager.getKeyBindingManager().set("toggle_wire",
				KeyInput.KEY_T);
		/** Assign key L to action "toggle_lights". */
		KeyBindingManager.getKeyBindingManager().set("toggle_lights",
				KeyInput.KEY_L);
		/** Assign key B to action "toggle_bounds". */
		KeyBindingManager.getKeyBindingManager().set("toggle_bounds",
				KeyInput.KEY_B);
		/** Assign key C to action "camera_out". */
		KeyBindingManager.getKeyBindingManager().set("camera_out",
				KeyInput.KEY_C);
		KeyBindingManager.getKeyBindingManager().set("exit",
				KeyInput.KEY_ESCAPE);
	}
 
	/**
	 * Creates rootNode, lighting, statistic text, and other basic render
	 * states. Called in BaseGame.start() after initSystem().
	 * 
	 * @see com.jme.app.AbstractGame#initGame()
	 */
	protected final void initGame() {
		/** Create rootNode */
		rootNode = new Node("rootNode");
 
		/**
		 * Create a wirestate to toggle on and off. Starts disabled with default
		 * width of 1 pixel.
		 */
		wireState = display.getRenderer().createWireframeState();
		wireState.setEnabled(false);
		rootNode.setRenderState(wireState);
 
		/**
		 * Create a ZBuffer to display pixels closest to the camera above
		 * farther ones.
		 */
		ZBufferState buf = display.getRenderer().createZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
 
		rootNode.setRenderState(buf);
 
		// -- FPS DISPLAY
		// First setup blend state
		/**
		 * This allows correct blending of text and what is already rendered
		 * below it
		 */
		BlendState as1 = display.getRenderer().createBlendState();
		as1.setBlendEnabled(true);
		as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		as1.setDestinationFunction(BlendState.DestinationFunction.One);
		as1.setTestEnabled(true);
		as1.setTestFunction(BlendState.TestFunction.GreaterThan);
		as1.setEnabled(true);
 
		// Now setup font texture
		TextureState font = display.getRenderer().createTextureState();
		/** The texture is loaded from fontLocation */
		font.setTexture(TextureManager.loadTexture(SimpleGame.class
				.getClassLoader().getResource(fontLocation),
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		font.setEnabled(true);
 
		// Then our font Text object.
		/** This is what will actually have the text at the bottom. */
		fps = Text.createDefaultTextLabel("FPS label", "");
		fps.setCullHint(Spatial.CullHint.Never);
		fps.setTextureCombineMode(TextureCombineMode.Replace);
 
		// Finally, a stand alone node (not attached to root on purpose)
		fpsNode = new Node("FPS node");
		fpsNode.attachChild(fps);
		fpsNode.setRenderState(font);
		fpsNode.setRenderState(as1);
		fpsNode.setCullHint(Spatial.CullHint.Never);
 
		// ---- LIGHTS
		/** Set up a basic, default light. */
		PointLight light = new PointLight();
		light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
		light.setLocation(new Vector3f(100, 100, 100));
		light.setEnabled(true);
 
		/** Attach the light to a lightState and the lightState to rootNode. */
		lightState = display.getRenderer().createLightState();
		lightState.setEnabled(false);
		lightState.attach(light);
		rootNode.setRenderState(lightState);
 
		/** Let derived classes initialize. */
		simpleInitGame();
 
		/**
		 * Update geometric and rendering information for both the rootNode and
		 * fpsNode.
		 */
		rootNode.updateGeometricState(0.0f, true);
		rootNode.updateRenderState();
		fpsNode.updateGeometricState(0.0f, true);
		fpsNode.updateRenderState();
	}
	
	protected void simpleInitGame()  {
		for(TriMesh mesh: meshes){
			rootNode.attachChild(mesh);
		}
				
		// Attach the skybox to our root node, and force the rootnode to show
		// so that the skybox will always show
		rootNode.attachChild(sb);
		rootNode.setCullHint(Spatial.CullHint.Never);

	}
 
	/**
	 * Can be defined in derived classes for custom updating. Called every frame
	 * in update.
	 */
	protected void simpleUpdate() {
	}
 
	/**
	 * Can be defined in derived classes for custom rendering. Called every
	 * frame in render.
	 */
	protected void simpleRender() {
	}
 
	/**
	 * unused
	 * 
	 * @see com.jme.app.AbstractGame#reinit()
	 */
	protected void reinit() {
	}
 
	/**
	 * Cleans up the keyboard.
	 * 
	 * @see com.jme.app.AbstractGame#cleanup()
	 */
	protected void cleanup() {
		logger.info("Cleaning up resources.");
		KeyInput.destroyIfInitalized();
		MouseInput.destroyIfInitalized();
		JoystickInput.destroyIfInitalized();
	}
}