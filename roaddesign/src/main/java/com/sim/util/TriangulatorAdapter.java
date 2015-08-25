package com.sim.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;


import com.jme.math.Vector3f;
import com.jmex.font3d.math.Triangulator;
import com.sim.geometries.RoadVector;

/**
 * This class is used to adapt the interface of the triangulator
 * in jME. Because jME uses a different Vector3f than the one used in 
 * this package.
 * 
 * @author Dahai Guo
 *
 */
public class TriangulatorAdapter{
	
	/**
	 * The triangulator in jME.
	 */
	private Triangulator triangulator;
	
	/**
	 * Internal buffer of input vertices
	 */
	private ArrayList<RoadVector> vertices;
	
	/**
	 * Initializes the jME's triangulator
	 */
	public TriangulatorAdapter(){
		triangulator = new Triangulator();
		vertices = new ArrayList<RoadVector>();
	}
	
	/**
	 * Adds a vertex to the triangulator
	 * @param v
	 */
	public void addVertex(RoadVector v){
		vertices.add(v);
		triangulator.addVertex(new Vector3f(
				v.getX(), v.getY(), v.getZ()));
	}
	
	/**
	 * Adds an edge to the triangulator
	 * @param p1 index of first point
	 * @param p2 index of second point
	 */
	public void addEdge(int p1, int p2){
		triangulator.addEdge(p1, p2);
	}
	
	/**
	 * Calls the jME's triangulate and form a list of vertex, 
	 * in which every three points form a triangle.
	 * 
	 * @return triangle vertices
	 */
	public ArrayList<RoadVector> triangulate(){
		IntBuffer intBuf = triangulator.triangulate();
		ArrayList<RoadVector> result =
	        	new ArrayList<RoadVector>();
		int pos = intBuf.position();
		for(int i=0;i<pos;i++){
			int index = intBuf.get(i);
			result.add(vertices.get(index));
		}
	        
		return result;
	}
	
	/**
	 * Creates a triangulator and empty the internal buffer.
	 */
	public void reset(){
		triangulator = new Triangulator();
		vertices.clear();
	}

	/**
	 * Triangulates a close polygon.
	 * 
	 * @param border
	 * @return
	 */
	public static ArrayList<RoadVector> triangulate(
			ArrayList<RoadVector> border) {
		TriangulatorAdapter triangulator = 
			new TriangulatorAdapter();
		for(RoadVector v : border){
			triangulator.addVertex(v);
		}
		
		for(int i=0;i<border.size();i++){
			triangulator.addEdge(i, (i+1)%border.size());
		}
		
		return triangulator.triangulate();
	}
	
	/**
	 * Returns the list of vertices, already in the triangulator
	 * @return
	 */
	public ArrayList<RoadVector> getVertices(){
		return vertices;
	}
	
	public static void main(String args[]){
		ArrayList<RoadVector> vs = new ArrayList<RoadVector>();
		vs.add(new RoadVector(1264.409f, 524.319f, 0.000f));
		vs.add(new RoadVector(1257.573f, 534.182f, 0.000f));
		vs.add(new RoadVector(1337.385f, 589.497f, 0.000f));
		vs.add(new RoadVector(1339.851f, 591.206f, 0.000f));
		vs.add(new RoadVector(1364.192f, 603.435f, 0.000f));
		vs.add(new RoadVector(1408.897f, 626.053f, 0.000f));
		vs.add(new RoadVector(1437.812f, 633.613f, 0.000f));
		vs.add(new RoadVector(1484.620f, 627.368f, 0.000f));
		vs.add(new RoadVector(1512.145f, 615.454f, 0.000f));
		vs.add(new RoadVector(1639.042f, 540.805f, 0.000f));
		vs.add(new RoadVector(1632.553f, 530.711f, 0.000f));

		Collections.reverse(vs);
		
		TriangulatorAdapter triangulator = 
			new TriangulatorAdapter();
		
		for(RoadVector v : vs){
			triangulator.addVertex(v);
		}
		
		for(int i=0;i<vs.size();i++){
			triangulator.addEdge(i, (i+1)%vs.size());
		}
		
		triangulator.triangulate();
	}

	/**
	 * Returns the number of vertices, already stored in the triangulator.
	 * @return
	 */
	public int getNumOfVertices() {
		triangulator.getVertices().size();
		return 0;
	}
}
