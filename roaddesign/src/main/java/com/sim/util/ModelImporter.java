package com.sim.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Skybox;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.CullState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import com.sim.central.ModelPathState;
import com.sim.central.RoadDesign;

/**
 * This class contains methods which loads graphics model to be rendered in 
 * the JMonkeyEngine
 * @author Dahai Guo
 *
 */
public class ModelImporter {
	
	public static DisplaySystem display=null;
	
	/**
	 * Loads a graphics model for a road network.
	 * 
	 * @param camParams where to store the camera parameters
	 * @return a list of meshes each of which is mapped to one texture file
	 * @throws IOException
	 */
	public static TriMesh [] loadModel(String modelPath, Vector3f[] camParams)
		throws FileNotFoundException{
		
		// display system is needed for generating texture state 
//		if(display==null)
//			return null;
		
		if(modelPath==null || ModelPathState.checkModelPathBasic(modelPath)
				!=ModelPathState.OK){
			throw new FileNotFoundException("The model in "+modelPath+" is not correct");
		}
		
		// gets the path to the control file which is an xml
		String mesh_file = RoadDesign.getMeshFilePath();

		// gets the file that stores where the start view is saved
		String start_view_file = String.format("%s\\%s", 
				RoadDesign.getModelPath(), 
				RoadDesign.getStartViewPath());
		
		// loads the camera parameters
		if(camParams!=null){
			loadViewPoint(camParams, new Scanner(new File(start_view_file)));
		}
		
		// loads information of texture files and texture regions
		Map[] maps = ModelXMLReader.loadModelXML(mesh_file);
		
		// prepares the textures, usable to jme
		Map<Integer, TextureState> images = 
				loadTextures(maps[0]);
		Map<Integer, ArrayList<Float>> regions =
				(Map<Integer, ArrayList<Float>>)maps[1];

		// finds the path to where the model is saved
		ArrayList<String> modelFiles = loadModelFilePaths(modelPath);
		ArrayList<TriMesh> model = new ArrayList<TriMesh>();
		
		// prepares textured meshes
		for(String path : modelFiles){
			model.addAll(loadMeshes(images, regions, path));
		}
		
		TriMesh[] result = new TriMesh[model.size()];
		return model.toArray(result);
	}
		
	/**
	 * Loads the meshes in a single file. 
	 * 
	 * @param images the texture images
	 * @param regions the texture coordinates
	 * @param path path to the file, containing the meshes
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Collection<? extends TriMesh> loadMeshes(
			Map<Integer, TextureState> images,
			Map<Integer, ArrayList<Float>> regions, String path) 
					throws FileNotFoundException {
		
		Scanner scan = new Scanner(new File(path));
		
		ArrayList<TriMesh> meshes = new ArrayList<TriMesh>();
		int numOfMeshes = scan.nextInt();
		for(int i=0;i<numOfMeshes;i++){
			meshes.add(loadMesh(scan, regions, images));
		}
		return meshes;
	}

	/**
	 * Compiles the list of file names where meshes are stored.
	 * @return
	 * @throws FileNotFoundException
	 */
	private static ArrayList<String> loadModelFilePaths(String modelPath) 
			throws FileNotFoundException {
		ArrayList<String> filenames = new ArrayList<String>();
		
		// gets the directory file, each line stores a file name where
		// a road model is stored.
		String model_dir = String.format("%s\\%s", 
				modelPath, RoadDesign.getModelDir());
		
		Scanner scan = new Scanner(new File(model_dir));
		while(scan.hasNext()){
			String temp = scan.nextLine();
			if(temp.length()>0){
				filenames.add(temp);
			}
		}
		
		return filenames;
	}

	/**
	 * Loads the textures.
	 * 
	 * @param map maps region types (int) to texture file names
	 * @return
	 * @throws IOException
	 */
	private static Map<Integer, TextureState> loadTextures(Map map) 
			throws FileNotFoundException {
		Map<Integer, String> filenames = (Map<Integer, String>)map;
		Map<Integer,  TextureState> images = new HashMap<Integer,  TextureState>();
		String texturePath = RoadDesign.getTexturePath();
		for(Integer i : filenames.keySet()){
			String path = String.format("%s\\%s", 
					texturePath, filenames.get(i));
			
			images.put(i, loadTexture(path));
		}
		return images;
	}

	public static void loadViewPoint(Vector3f camParams[], Scanner scan){
		for(int i=0;i<camParams.length;i++){
			camParams[i].x = scan.nextFloat();
			camParams[i].y = scan.nextFloat();
			camParams[i].z = scan.nextFloat();
		}
	}
	
	/**
	 * Reads a mesh from a file.
	 * 
	 * @param scan disk access
	 * @param texRegions map that maps texture regions (int) to texture coordinates
	 * @param textures map that maps texture regions (int) to jme compatible textures
	 * @return
	 */
	private static TriMesh loadMesh(Scanner scan,
			Map<Integer, ArrayList<Float>> texRegions, 
			Map<Integer, TextureState> textures) {
		
		// find the texture info
		int type = scan.nextInt();
		TextureState ts = textures.get(type);
		float bottomLeftX = texRegions.get(type).
			get(ModelXMLReader.REGION_BOTTOM_LEFT_X);
		float bottomLeftY = texRegions.get(type).
			get(ModelXMLReader.REGION_BOTTOM_LEFT_Y);
		float topRightX =  texRegions.get(type).
			get(ModelXMLReader.REGION_TOP_RIGHT_X);
		float topRightY =  texRegions.get(type).
			get(ModelXMLReader.REGION_TOP_RIGHT_Y);
		int texWidth = 0;
		int texHeight = 0;
		
		if(ts!=null){
			texWidth = ts.getTexture().getImage().getWidth();
			texHeight = ts.getTexture().getImage().getHeight();
		}
		
		// load vertices
		int numOfVertices = scan.nextInt();
		Vector3f [] _vertices = new Vector3f[numOfVertices];
		for(int i=0;i<numOfVertices;i++){
			float x, y, z;
			x = scan.nextFloat();
			y = scan.nextFloat();
			z = scan.nextFloat();
			
			_vertices[i] = new Vector3f(x,y,z);
		}
		
		// load vertex indices and texture coords
		int numOfTriangles = scan.nextInt();
//		System.out.println(numOfTriangles);
		int [] verIndices = new int[numOfTriangles*3];
		Vector3f vertices[] = new Vector3f[numOfTriangles*3];
		Vector2f [] texCoords = new Vector2f[numOfTriangles*3];
//		ColorRGBA colors [] = new ColorRGBA[numOfTriangles*3];
		
		// loading individual triangles
		for(int i=0;i<numOfTriangles;i++){
			
			for(int j=0;j<3;j++){
				int index = scan.nextInt();
				float x = scan.nextFloat();
				float y = scan.nextFloat();
				
				vertices[3*i+j] = new Vector3f(
						_vertices[index].x, 
						_vertices[index].y, 
						_vertices[index].z
				);
				
				verIndices[3*i+j] = 3*i+j;
				
				if(ts!=null){
					texCoords[3*i+j] = new Vector2f(
							(bottomLeftX + x * (topRightX-bottomLeftX))/texWidth,
							(bottomLeftY + y * (topRightY-bottomLeftY))/texHeight
					);
				}
			}
		}
		
		TriMesh m = new TriMesh();

		m.reconstruct(BufferUtils.createFloatBuffer(vertices), 
				null, 
				null, /* no color */
		//		BufferUtils.createFloatBuffer(colors), /* with color */
		 		TexCoords.makeNew(texCoords), /* with texture */
		//		null, /* no texture */
				BufferUtils.createIntBuffer(verIndices));
		if(ts!=null)
			m.setRenderState(ts);
		
		return m;
	}

	
	public static void main(String args[]) throws IOException{
	}
	
	
	/**
	 * Builds the texture state.
	 * 
	 * @param image the image object
	 * @return the texture object
	 * @throws DataFormatException
	 */
	private static TextureState loadTexture(Image image){

		if(display==null){
			return null;
		}
		// Get my TextureState
		TextureState ts = display.getRenderer().createTextureState();
 
		// Get my Texture
		Texture t = TextureManager.loadTexture(image,
	//			Texture.MinificationFilter.BilinearNearestMipMap,
	//			Texture.MagnificationFilter.NearestNeighbor,
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.NearestNeighbor,
				false);
 
		// Set a wrap for my texture so it repeats
		t.setWrap(Texture.WrapMode.Repeat);
 
		// Set the texture to the TextureState
		ts.setTexture(t);
		
		return ts;
	}


	/**
	 * Loads a image file and builds a texture object, usable in jME 
	 * @param imagePath 
	 * @return the texture object if successful; null otherwise
	 */
	private static TextureState loadTexture(String imagePath){
		try {
			return loadTexture(ImageIO.read(new File(imagePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/** 
	 * Builds a (2000*2000*2000) sky box for our scene.  
	 * 
	 * */
	public static Skybox setupSky() {
		Skybox sb = new Skybox("skybox", 2000, 2000, 2000);

		String path = RoadDesign.getTexturePath();
		
		sb.setTexture(Skybox.Face.North, TextureManager.loadTexture(
				path+"\\north.jpg", 
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.setTexture(Skybox.Face.West, TextureManager.loadTexture(
				path+"\\west.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.setTexture(Skybox.Face.South, TextureManager.loadTexture(
				path+"\\south.jpg", 
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.setTexture(Skybox.Face.East, TextureManager.loadTexture(
				path+"\\east.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.setTexture(Skybox.Face.Up, TextureManager.loadTexture(
				path+"\\top.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.setTexture(Skybox.Face.Down, TextureManager.loadTexture(
				path+"\\bottom.jpg", 
				Texture.MinificationFilter.BilinearNearestMipMap,
				Texture.MagnificationFilter.Bilinear));
		sb.preloadTextures();
		sb.rotateUpTo(new com.jme.math.Vector3f(0,0,1));

		CullState cullState = display.getRenderer().createCullState();
		cullState.setCullFace(CullState.Face.None);
		cullState.setEnabled(true);
		sb.setRenderState(cullState);
 
		sb.updateRenderState();
		
		return sb;
	}
	
	public static BufferedImage loadHeightMap(String modelPath) 
			throws IOException{
		String heightMap = RoadDesign.getProperty(
				RoadDesign.HEIGHT_MAP_FILE);
		String filename = String.format("%s\\%s", modelPath, heightMap);
		
		BufferedImage im = ImageIO.read(new File(filename));
		return im;
	}
	
	public static float [] loadHeightMapRange(String modelPath) 
			throws FileNotFoundException{
		String heightMapScaleFile = RoadDesign.getProperty(
				RoadDesign.HEIGHT_MAP_SCALE_FILE);
		String filename = String.format("%s\\%s", modelPath, heightMapScaleFile);
		Scanner scan = new Scanner(new File(filename));
		
		float [] result = new float[6];
		for(int i=0;i<result.length;i++){
			result[i] = scan.nextFloat();
		}
		return result;
	}

}
