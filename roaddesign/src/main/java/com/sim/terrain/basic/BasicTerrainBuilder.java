package com.sim.terrain.basic;

import java.util.ArrayList;

import com.sim.central.Logging;
import com.sim.geometries.RoadVector;
import com.sim.network.RoadNetwork;
import com.sim.terrain.TerrainBuilder;
import com.sim.util.TriangulatorAdapter;


public class BasicTerrainBuilder extends TerrainBuilder{
	/**
	 * Given the bounding box of road network, this scale factor defines
	 * how large the surrounding terrain is.
	 */
	protected static final float TERRAIN_SCALE = 1.5f;

	/**
	 * The number of lines used to approximate the circle of
	 * surrounding terrain.
	 */
	protected final static int NUM_OF_EDGES_FOR_TERRAIN = 10;

	/**
	 * Re-calculates the terrain
	 * @param terrain
	 */
	protected void update(BasicTerrain terrain){
		BasicVectorMap vMap = BasicVectorMap.findVectorMap(terrain.getNetwork());
		terrain.clear();
		if(vMap!=null)	
			terrain.setTriangles(triangulate(vMap, terrain.getNetwork()));

	}
	
	/**
	 * Triangulates to get triangle meshes
	 * @param vMap see {@link com.sim.terrain.basic.BasicVectorMap}
	 * @param network which network to build terrain for
	 */
	private ArrayList<RoadVector> triangulate(BasicVectorMap vMap, 
			RoadNetwork network) {
		
		ArrayList<RoadVector> triangles = new ArrayList<RoadVector>();
		triangulateOuterArea(vMap, triangles, network);
		triangulateHoles(vMap, triangles);
		
		return triangles;
	}

	/**
	 * Triangulates each inner hole
	 * @param vMap 
	 * @param triangles 
	 */
	private void triangulateHoles(BasicVectorMap vMap, ArrayList<RoadVector> triangles) {
		
		ArrayList<ArrayList<RoadVector>> holes = vMap.getInnerPerimeter();
		if(holes==null)
			return;
		for(ArrayList<RoadVector> perimeter : holes){
			TriangulatorAdapter triangulator = new TriangulatorAdapter();
			
			for(int i=0;i<perimeter.size();i++){
				triangulator.addVertex(perimeter.get(i));
			}
			for(int i=0;i<perimeter.size();i++){
				if(i!=perimeter.size()-1)
					triangulator.addEdge(i, i+1);
				else
					triangulator.addEdge(i, 0);
			}
						
			triangles.addAll(triangulator.triangulate());
		}
	}

	/**
	 * Finds and triangulates the surrounding terrain.
	 * <p>
	 * Each outer loop is a hole from the perspective of the surrounding terrain. 
	 * The surrounding terrain is a circle, approximated by 10 edge polygon.
	 * @param vMap 
	 * @param triangles 
	 * @param network 
	 */
	private void triangulateOuterArea(BasicVectorMap vMap, 
			ArrayList<RoadVector> triangles, RoadNetwork network) {
		TriangulatorAdapter triangulator = new TriangulatorAdapter();
		
		float [] range = network.getXYBoundingBox();
		float centerX = (range[2]+range[0])/2;
		float centerY = (range[3]+range[1])/2;
		float width = range[2]-range[0];
		float height = range[3]-range[1];
		
	//	Logging.getLogger().info(String.format("Model size is (%d,%d)",
	//			(int)width,(int)height));
		
		float radius = (width>height)?width/2:height/2;
		radius *= TERRAIN_SCALE;
		
		if(width<0 || height<0){
			return;
		}
		
		float unitAngle = (float) (2*Math.PI/NUM_OF_EDGES_FOR_TERRAIN);
		for(int i=0;i<NUM_OF_EDGES_FOR_TERRAIN;i++){
			RoadVector v = new RoadVector(
				centerX+(float)Math.cos(unitAngle*i)*radius,
				centerY+(float)Math.sin(unitAngle*i)*radius,
				0
			);
			triangulator.addVertex(v);
		}
				
		ArrayList<ArrayList<RoadVector>> perimeters = vMap.getOuterPerimeter();
		int count = NUM_OF_EDGES_FOR_TERRAIN;
		if(perimeters!=null){
			for(ArrayList<RoadVector> perimeter : perimeters){
				for(RoadVector vec : perimeter){
					triangulator.addVertex(vec);
				}
			}
		}
		for(int i=0;i<NUM_OF_EDGES_FOR_TERRAIN;i++){
			if(i!=NUM_OF_EDGES_FOR_TERRAIN-1)
				triangulator.addEdge(i, i+1);
			else
				triangulator.addEdge(i, 0);
		}
		
		if(perimeters!=null){
			for(ArrayList<RoadVector> perimeter : perimeters){
				for(int i=0;i<perimeter.size();i++){
					if(i!=perimeter.size()-1)
						triangulator.addEdge(count+i, count+i+1);
					else
						triangulator.addEdge(count+i, count);
				}
				count+=perimeter.size();
			}
		}
				
		triangles.addAll(triangulator.triangulate());
	}

}
