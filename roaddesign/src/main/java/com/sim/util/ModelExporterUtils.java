package com.sim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Scanner;

import com.sim.central.ModelPathState;
import com.sim.central.RoadDesign;
import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.ModelGenConsts;
import com.sim.geometries.RoadVector;
import com.sim.intersections.Intersection;
import com.sim.obj.CrossSection;
import com.sim.roads.basic.BasicRoad;


public class ModelExporterUtils {
	
	/**
	 * A collection of strips is a list of point sequence (same length). 
	 * Given two strips, each two pairs of points (two on each strip) define
	 * two triangles. This method returns another list of points, each 
	 * three of which make a triangle.
	 * 
	 * @param strips
	 * @return
	 */
	public static ArrayList<RoadVector> convertStripToMesh(
			ArrayList<ArrayList<RoadVector>> strips){
		ArrayList<RoadVector> mesh = new ArrayList<RoadVector>();
		for(int i=0;i<strips.size()-1;i++){
			for(int j=0;j<strips.get(i).size()-1;j++){
				RoadVector v11 = strips.get(i).get(j);
				RoadVector v12 = strips.get(i).get(j+1);
				RoadVector v21 = strips.get(i+1).get(j);
				RoadVector v22 = strips.get(i+1).get(j+1);
				
				mesh.add(v11); mesh.add(v12); mesh.add(v21);
				mesh.add(v12); mesh.add(v22); mesh.add(v21);
			}
		}
		
		return mesh;
	}
	
	
	/**
	 * Triangulates a strip and saves the mesh.
	 *   
	 * @param format
	 * @param strips
	 * @param type
	 * @param textureSize implies that the texture needs to be square
	 */
	public static void saveUniTextureStrips(Formatter format, 
			ArrayList<ArrayList<RoadVector>> strips, int type, float textureSize){
		ArrayList<RoadVector> mesh =  convertStripToMesh(strips);
						
		saveUniTextureMesh(format, mesh, type, textureSize);
	}
	
	/**
	 * Given a mesh, saves it with a uniform texture map. 
	 * 
	 * @param format
	 * @param mesh each three points of which make a triangle
	 * @param type texture type
	 * @param textureSize
	 */
	public static void saveUniTextureMesh(Formatter format, 
			ArrayList<RoadVector> mesh, int type, float textureSize){
		RoadVector [] box = findBoundingBox(mesh);
		float minX = box[0].getX();
		float minY = box[0].getY();
		
		// saves the texture type
		format.format("%d\n", type);
	
		// saves number of points and all the points
		format.format("%d\n", mesh.size());
		for(RoadVector vec : mesh){
			format.format("%.3f\t%.3f\t%.3f\n", 
					vec.getX(),vec.getY(),vec.getZ());
		}
		
		// saves the number of triangles in the mesh
		format.format("%d\n", mesh.size()/3);
		
		// for each triangle, saves three lines, each of which is 
		// point_index(see above) texture_coordinate_u texture_coordinate_v
		for(int i=0;i<mesh.size()/3;i++){
			for(int j=0;j<3;j++){
				float deltaX = mesh.get(3*i+j).getX()-minX;
				float deltaY = mesh.get(3*i+j).getY()-minY;
				
				format.format("%d\t%.3f\t%.3f\n", 
					3*i+j, deltaX/textureSize, deltaY/textureSize);
			}
		}
	}

	/**
	 * Finds the bounding rectangle of a triangle mesh.
	 * 
	 * @param mesh each three points of which make a triangle
	 * @return
	 */
	private static RoadVector[] findBoundingBox(ArrayList<RoadVector> mesh) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		
		for(RoadVector vec : mesh){
			float x = vec.getX();
			float y = vec.getY();
			
			minX = (x<minX)?x:minX;
			minY = (y<minY)?y:minY;
			maxX = (x>maxX)?x:maxX;
			maxY = (y>maxY)?y:maxY;
		}
		
		return new RoadVector[]{
			new RoadVector(minX,minY,0),
			new RoadVector(maxX,maxY,0),
		};
	}
	
//	public static final int DEFAULT_TEX_SIDE = 512;
	
	/**
	 * Saves the starting view point when the graphics model is first loaded.
	 * <p>
	 * The view point is the first center line point. Additionally, it saves
	 * gaze, left, and up vectors for the game engine to initialize.
	 * 
	 * @param crossSections
	 * @param centerLine
	 * @param format
	 */
	public static void saveStartView(ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine,	Formatter format){
		RoadVector eye = centerLine.get(0).duplicate();
		eye.setZ(eye.getZ()+3);

		// finds the starting gaze direction
		RoadVector gaze = RoadVector.subtract(centerLine.get(1), 
				centerLine.get(0)).unit();
		
		// finds the left direction
		RoadVector left = RoadVector.subtract(crossSections.get(0).get(0), 
				centerLine.get(0)).unit2d();
		
		// finds the up direction
		RoadVector up = RoadVector.cross(gaze, left);
		
		// save eye point and (left, up, and gaze) to file
		// (left, up and gaze) is required by the game engine
		format.format("%.3f\t%.3f\t%.3f\n", eye.getX(), eye.getY(), eye.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", left.getX(), left.getY(), left.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", up.getX(), up.getY(), up.getZ());
		format.format("%.3f\t%.3f\t%.3f\n", gaze.getX(), gaze.getY(), gaze.getZ());
	}
	
	/**
	 * Saves road meshes to disk. 
	 * <p>
	 * There have been the following mesh types:
	 * <ul>
	 * <li> Median
	 * <li> Each solid marker (white)
	 * <li> Each solid marker (yellow)
	 * <li> Pavement with dashed markers
	 * <li> Shoulder
	 * </ul>
	 * <p>
	 * Given two cross sectional profiles aligned and an index, a sequence of 
	 * rectangles can be gotten. This sequence of rectangles should be one of above 
	 * objects.
	 *   
	 * @param crossSections cross section profile at center line points, containing 
	 * 		material information
	 * @param cs the CrossSection object that contains information like lane width, etc.
	 * @param format disk access
	 */
	public static void saveRoadMesh(ArrayList<ArrayList<RoadVector>> crossSections, 
			CrossSection cs, Formatter format) {
		
		if(crossSections==null){
			return;
		}
		
		/**
		 * gets the center line from the sequence of cross sectional profiles.
		 */
		ArrayList<RoadVector> centerLine = new ArrayList<RoadVector>();
		for(int i=0;i<crossSections.size();i++){
			int width = crossSections.get(i).size();
			centerLine.add(crossSections.get(i).get(width/2));
		}
		
		/**
		 * Saves individual road objects. The number of objects is decided by the 
		 * length of a cross sectional profile.
		 */
		for(int i=0;i<crossSections.get(0).size()-1;i++){
			int type = crossSections.get(0).get(i).crossSectionType;
			format.format("%d\n", type);
			switch(type){
			case ModelGenConsts.MEDIAN:
			case ModelGenConsts.SHOULDER:
				exportWrapTexUnlimited(format, crossSections, centerLine, i, 
						ModelGenConsts.TEX_WRAP_SCALE, type);
				break;
			case ModelGenConsts.ROAD_SURFACE:
				exportWrapTexLimitedU(format, crossSections, centerLine, i,
						DesignConsts.DASH_WHITE_MARKER_LEN+
							DesignConsts.DASH_WHITE_MARKER_SPACE,
						cs.laneWidth+DesignConsts.MARKER_WIDTH, type);
				break;
			case ModelGenConsts.SOLID_WHITE_MARKER_LEFT:
			case ModelGenConsts.SOLID_WHITE_MARKER_RIGHT:
				exportWrapTexVOnly(format, crossSections, centerLine, i, 
						DesignConsts.DASH_WHITE_MARKER_LEN+
						DesignConsts.DASH_WHITE_MARKER_SPACE, type);
				break;
			}
		}
	}

	/**
	 * Exports a cross sectional segment along a center line. The texture only 
	 * repeats in the v direction (longitude direction).
	 * 
	 * @param format disk access
	 * @param crossSections a sequence of cross sectional profile
	 * @param centerLine
	 * @param i points to which segment in the cross sectional profile
	 * @param longDist the height of the texture. The less this value is,
	 *                 the more the texture repeats
	 * @param type type of texture
	 */
	private static void exportWrapTexVOnly(Formatter format,
			ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, int i, float longDist, int type) {

		float width = RoadVector.subtract(crossSections.get(0).get(i),
				crossSections.get(0).get(i+1)).magnitude2d();
		
		exportWrapTex(format,crossSections,centerLine, i,
				width, longDist, 1, type);
	}

	/**
	 * Exports a cross sectional segment along a center line. Points u texture
	 * coordinates are not multiplied with any wrap factor.
	 * 
	 * @param format disk access
	 * @param crossSections a sequence of cross sectional profile
	 * @param centerLine
	 * @param i points to which segment in the cross sectional profile
	 * @param longStep the height of the texture. The less this value is,
	 *                 the more the texture repeats
	 * @param texWidthScale decides how much the texture repeats in the 
	 *                      u direction. the greater this value is, the more
	 *                      the texture repeats. when it is, the texture
	 *                      does not repeat in the u direction.
	 * @param type
	 */
	private static void exportWrapTexLimitedU(Formatter format,
			ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, int i, float longStep, 
			float texWidthScale, int type) {
		exportWrapTex(format, crossSections, centerLine, i,
				texWidthScale, longStep, 1, type);
	}

	/**
	 * Exports a cross sectional segment along a center line. How the texture is
	 * mapped is not critical. This method is for cross sectional segments, such as
	 * median and shoulder. 
	 *  
	 * @param format disk access
	 * @param crossSections a sequence of cross sectional profile
	 * @param centerLine
	 * @param i points to which segment in the cross sectional profile
	 * @param texWrapScale decides how much the texture is repeated. the more 
	 *                     the value is, the texture repeats more in the u 
	 *                     direction and less in the v direction.
	 * @param type
	 */
	private static void exportWrapTexUnlimited(Formatter format,
			ArrayList<ArrayList<RoadVector>> crossSections,
			ArrayList<RoadVector> centerLine, int i, float texWrapScale,
			int type) {
		exportWrapTex(format,crossSections,centerLine,i,
				texWrapScale, texWrapScale, 1, type);
	}

	/**
	 * Exports a cross sectional segment along a center line. 
	 * 
	 * @param format disk access
	 * @param crossSections a sequence of cross sectional profile
	 * @param centerLine
	 * @param i points to which segment in the cross sectional profile
	 * @param texWidth decides how much the texture is repeated in the u direction.
	 * 					the more the value is, the texture repeats more in the u 
	 *                     direction.
	 * @param longStep decides how much the texture is repeated in the v direction. 
	 * 						the more the value is, the texture repeats less in the v 
	 * 						direction.
	 * @param texWrapScale
	 * @param type
	 */
	private static void exportWrapTex(Formatter format,
			ArrayList<ArrayList<RoadVector>> crossSections, 
			ArrayList<RoadVector> centerLine, int i,
			float texWidth, float longStep, float texWrapScale, int type) {
	
		// saves all the vertices first, then they will be referenced when
		// saving triangles
		saveAllVertices(format,crossSections,i);
		
		// saves the number of triangles
		format.format("%d\n", (centerLine.size()-1)*2);
		
		int vs[] = new int[3];
		float texCoordX [] = new float[3];
		float texCoordY [] = new float[3];
		// finds the texture width. When texWrapScale is one, the texture width
		// is exactly the width of the cross sectional segment.
		texWidth = texWrapScale*RoadVector.subtract(
				crossSections.get(0).get(i), 
				crossSections.get(0).get(i+1)).magnitude2d()/
				texWidth;
		float longDist1 = 0;
		RoadVector p1 = centerLine.get(0);
		
		// At each center line, the cross sectional segment is a rectangle which can be 
		// represented as two triangles.
		for(int k=1;k<centerLine.size();k++){
			RoadVector p2 = centerLine.get(k);
			float longDist2 = longDist1 + 
				RoadVector.subtract(p1, p2).magnitude();
			
			// find the first triangle
			vs[0]=(k-1)*2; 
			vs[1]=(k-1)*2+1; 
			vs[2]=k*2+1;
						
			texCoordX[0]=0; 
			texCoordX[1]=texWidth; 
			texCoordX[2]=texWidth;
			texCoordY[0]=texWrapScale*longDist1/longStep;
			texCoordY[1]=texWrapScale*longDist1/longStep;
			texCoordY[2]=texWrapScale*longDist2/longStep;
			
			saveTriangle(format,vs,texCoordX,texCoordY,type);
			
			// find the second triangle
			vs[0]=(k-1)*2; 
			vs[1]=k*2+1; 
			vs[2]=k*2;
			
			texCoordX[0]=0; 
			texCoordX[1]=texWidth; 
			texCoordX[2]=0;
			texCoordY[0]=texWrapScale*longDist1/longStep;
			texCoordY[1]=texWrapScale*longDist2/longStep;
			texCoordY[2]=texWrapScale*longDist2/longStep;
			
			saveTriangle(format,vs,texCoordX,texCoordY,type);
			
			longDist1 = longDist2;
			p1 = p2;
		}
	}
	
	/**
	 * Saves all the vertices in a cross sectional segment along a road.
	 * 
	 * @param format disk access
	 * @param crossSections a sequence of cross sectional profiles
	 * @param i points to which segment in the cross sectional profile
	 */
	private static void saveAllVertices(Formatter format,
			ArrayList<ArrayList<RoadVector>> crossSections, int i) {
		format.format("%d\n", 2*crossSections.size());
		for(int j=0;j<crossSections.size();j++){
//			format.format("%s\n", crossSections.get(j).get(i));
//			format.format("%s\n", crossSections.get(j).get(i+1));
			format.format("%.3f\t%.3f\t%.3f\n",
					crossSections.get(j).get(i).getX(),
					crossSections.get(j).get(i).getY(),
					crossSections.get(j).get(i).getZ());
			format.format("%.3f\t%.3f\t%.3f\n",
					crossSections.get(j).get(i+1).getX(),
					crossSections.get(j).get(i+1).getY(),
					crossSections.get(j).get(i+1).getZ());
		}
	}

	/**
	 * Saves an individual triangle.
	 * 
	 * The texture coordinates (texCoordX, texCoordY) are relative to
	 * the top-left corner of the texture area. That top-left corner's
	 * coordinates are stored in the xml file for the model
	 * 
	 * @param format file access
	 * @param vs vertex indices for building the triangle
	 * @param texCoordX  
	 * @param texCoordY
	 * @param type texture type
	 */
	private static void saveTriangle(Formatter format, int[] vs,
			float[] texCoordX, float[] texCoordY, int type) {

		for(int i=0;i<3;i++){
			format.format("%d\t%.5f\t%.5f\n",
					vs[i], texCoordX[i], texCoordY[i]);
		}
		format.format("\n");
	}

	
	/**
	 * Saves the surrounding terrain triangles.
	 * 
	 * @param terrain a list of terrain triangles
	 * @param format file access
	 */
	/*
	public static void saveUniTexTriangles(
			ArrayList<RoadVector> terrain, Formatter format) {
		
		int numOfTriangles = terrain.size()/3;
		format.format("%d\n", numOfTriangles);
		for(int i=0;i<numOfTriangles;i++){
			RoadVector v1, v2, v3;
			v1 = terrain.get(3*i+2);
			v2 = terrain.get(3*i+1);
			v3 = terrain.get(3*i);
			
			format.format("%.3f\t%.3f\t%.3f\n", 
					v1.getX(),v1.getY(),v1.getZ());
			format.format("%.3f\t%.3f\t%.3f\n", 
					v2.getX(),v2.getY(),v2.getZ());
			format.format("%.3f\t%.3f\t%.3f\n", 
					v3.getX(),v3.getY(),v3.getZ());
		}
		
	}

*/
}
