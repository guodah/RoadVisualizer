package com.sim.io.exporters.basic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;

import javax.imageio.ImageIO;

import com.jme.math.Triangle;
import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;
import com.sim.central.Logging;
import com.sim.central.RoadDesign;
import com.sim.geometries.Vector23f;
import com.sim.io.exporters.HeightMapExporter;
import com.sim.network.RoadNetwork;
import com.sim.util.ModelGenUtil;
import com.sim.util.ModelImporter;

public class BasicHeightMapExporter extends HeightMapExporter{
	
	public static int MINX = 0;
	public static int MAXX = 1;
	public static int MINY = 2;
	public static int MAXY = 3;
	public static int MINZ = 4;
	public static int MAXZ = 5;
	
	public BasicHeightMapExporter(RoadNetwork network){
		this.network = network;
	}

	/**
	 * Calculates and exports the height map of the network.<p>
	 * This method needs load an already saved model from the disk and
	 * then calculate the heightmap.
	 */
	@Override
	public void export(String modelPath) throws FileNotFoundException {
		ArrayList<Triangle> triangles = loadTriangles(modelPath);
		float range [] = findRange(triangles);
		BufferedImage image = findHeightMap(triangles, range);
		
		String heightMapFile = RoadDesign.getProperty(RoadDesign.HEIGHT_MAP_FILE);
		String path = String.format("%s\\%s", modelPath,
				heightMapFile);
		int index = heightMapFile.indexOf('.');
		String extension = heightMapFile.substring(index+1, heightMapFile.length());
		try {
			if(ImageIO.write(image, extension, new File(path))){
				Logging.getLogger().info("Height map saved successfully");
			}else{
				Logging.getLogger().severe("Height map saved unsuccessfully");
			}

		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				throw (FileNotFoundException)e;
			}
		}
		
		exportHeightMapScale(modelPath, range);
	}

	/**
	 * Loads triangles from the graphics model from the disk.
	 * 
	 * @param modelPath where the model is saved
	 * @return the list of the triangles
	 * @throws FileNotFoundException
	 */
	private ArrayList<Triangle> loadTriangles(String modelPath) 
			throws FileNotFoundException{
		TriMesh[] model = ModelImporter.loadModel(modelPath, null);
		
		// gets all the triangles out
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		for(TriMesh mesh : model){
			Triangle[] ts = mesh.getMeshAsTriangles(null);
			for(Triangle t : ts){
				triangles.add(t);
			}
		}
		Collections.shuffle(triangles);
		return triangles;
	}

	/**
	 * Finds the range of the graphics model.
	 * 
	 * @param triangles the triangles that comprise a graphics model of the road network
	 * @return {minx, maxx, miny, maxy, minz, maxz}
	 */
	private float [] findRange(ArrayList<Triangle> triangles){
		// find the range
		float [] range = new float[6];
		range[MINX]=range[MINY]=range[MINZ]=Float.MAX_VALUE;
		range[MAXX]=range[MAXY]=range[MAXZ]=Float.MIN_VALUE;
		Vector3f vs[]=new Vector3f[3];
		for(int i=0;i<triangles.size();i++){
			Triangle t = triangles.get(i);
			vs[0] = t.get(0);
			vs[1] = t.get(1);
			vs[2] = t.get(2);
		
			for(int j=0;j<3;j++){
				range[MINX] = Math.min(range[MINX], vs[j].x);
				range[MAXX] = Math.max(range[MAXX], vs[j].x);
				
				range[MINY] = Math.min(range[MINY], vs[j].y);
				range[MAXY] = Math.max(range[MAXY], vs[j].y);

				range[MINZ] = Math.min(range[MINZ], vs[j].z);
				range[MAXZ] = Math.max(range[MAXZ], vs[j].z);
			}
		}
		return range;
	}
	
	/**
	 * Finds the height map into an image which is gray level and 16-bit.
	 * 
	 * @param triangles triangles the triangles that comprise a graphics model of the road network 
	 * @param range
	 * @return
	 */
	private BufferedImage findHeightMap(ArrayList<Triangle> triangles, float[] range){

		int heightMapWidth = Integer.parseInt(RoadDesign.getProperty(
				RoadDesign.HEIGHT_MAP_WIDTH));
		int heightMapHeight = Integer.parseInt(RoadDesign.getProperty(
				RoadDesign.HEIGHT_MAP_HEIGHT));

		// allocates the height image
		BufferedImage image = new BufferedImage(
				heightMapWidth, heightMapHeight,
				BufferedImage.TYPE_USHORT_GRAY);
				//BufferedImage.TYPE_INT_RGB);
		short[] data = new short[heightMapWidth*heightMapHeight];
		
		float [][] temp = new float[heightMapWidth][heightMapHeight];
		for(int i=0;i<image.getWidth();i++){
			for(int j=0;j<image.getHeight();j++){
				float[] worldCoor = mapToWorld(i,j,image.getWidth(),image.getHeight(),
						range);
				temp[i][j] = getHeight(worldCoor,triangles);

				float tempf = (temp[i][j]-range[MINZ])/
						(range[MAXZ]-range[MINZ]);
				short gray = (short) ((tempf)*65535);
				gray = (short) ((gray>65535)?65535:gray);
				data[j*image.getHeight()+i] = gray;
			}
		}
		
		System.out.printf("(%d,%d): %d\n",338,338,data[338*512+338]);
		
		image.getRaster().setDataElements(0, 0, image.getWidth(), image.getHeight(), data);
		return image;
	}
	
	/**
	 * Saves the ranges of the graphics model of the road network, including
	 * width, height, and thick.
	 * 
	 * @param modelPath
	 * @param range
	 * @param minZ
	 * @param maxZ
	 * @throws FileNotFoundException
	 */
	private void exportHeightMapScale(String modelPath, float range[]) 
			throws FileNotFoundException {
		String heightMapScaleFile = RoadDesign.getProperty(
				RoadDesign.HEIGHT_MAP_SCALE_FILE);
		
		String filePath = String.format("%s\\%s", modelPath, heightMapScaleFile); 
		Formatter format = new Formatter(new File(filePath));
		format.format("%.3f\t%.3f\n", range[0], range[1]);
		format.format("%.3f\t%.3f\n", range[2], range[3]);
		format.format("%.3f\t%.3f\n", range[4], range[5]);
		format.close();
	}

	/**
	 * Given an (image) coordinate in the heightmap, finds its 2D coordinate
	 * in the road network.
	 * 
	 * @param imageX
	 * @param imageY
	 * @param heightMapWidth
	 * @param heightMapHeight
	 * @param range
	 * @return
	 */
	private float[] mapToWorld(int imageX, int imageY, int heightMapWidth, 
			int heightMapHeight, float [] range){
		float x = (float) ((imageX*1.0/heightMapWidth)*
				(range[MAXX]-range[MINX])+range[MINX]);
		float y = (float) ((imageY*1.0/heightMapHeight)*
				(range[MAXY]-range[MINY])+range[MINY]);
		return new float[]{x,y};
	}
	
	/**
	 * Given a 2D coordinate in the road network, finds its elevation.
	 * 
	 * @param pos 2D coordinate in the road network
	 * @param triangles all the triangles in the road network's graphics model
	 * @return
	 */
	private float getHeight(float[] pos, ArrayList<Triangle> triangles){
		
		Vector23f v = new Vector23f(pos[0],pos[1],0);
		
		for(Triangle triangle : triangles){
			Vector3f _v0 = triangle.get(0);
			Vector3f _v1 = triangle.get(1);
			Vector3f _v2 = triangle.get(2);
			
			Vector23f v0 = new Vector23f(_v0.x,_v0.y,0);
			Vector23f v1 = new Vector23f(_v1.x,_v1.y,0);
			Vector23f v2 = new Vector23f(_v2.x,_v2.y,0);
			
			float[] percentages = ModelGenUtil.inTriangle(v, 
					v0,v1,v2,1.0f);
			
			if(percentages==null)
				continue;
			
			v0.z = _v0.z;
			v1.z = _v1.z;
			v2.z = _v2.z;
			
			Vector23f N = Vector23f.cross(
					Vector23f.subtract(v1, v0), 
					Vector23f.subtract(v2, v0));
			
			return v0.z + (N.x*(v.x-v0.x)+N.y*(v.y-v0.y))/(-N.z);
		}
		
		return 0;
	}

}
