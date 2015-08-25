package com.sim.random;

import java.nio.IntBuffer;

import com.jme.math.Vector3f;
import com.jmex.font3d.math.Triangulator;

public class TriangulationTest {
	public static void main(String args[]){
		Triangulator triangulator = new Triangulator();
        triangulator.addVertex(new Vector3f(-1.0f, -1.0f, 0.0f));
        triangulator.addVertex(new Vector3f( 1.0f, -1.0f, 0.0f));
        triangulator.addVertex(new Vector3f( 1.0f,  1.0f, 0.0f));
        triangulator.addVertex(new Vector3f(-1.0f,  1.0f, 0.0f));

        triangulator.addVertex(new Vector3f(-0.5f, -0.5f, 0.0f));
        triangulator.addVertex(new Vector3f(-0.5f,  0.5f, 0.0f));
        triangulator.addVertex(new Vector3f( 0.5f,  0.5f, 0.0f));
        triangulator.addVertex(new Vector3f( 0.5f, -0.5f, 0.0f));
                
        triangulator.addEdge(0,1);
        triangulator.addEdge(1,2);
        triangulator.addEdge(2,3);
        triangulator.addEdge(3,0);
/*
        triangulator.addEdge(4,5);
        triangulator.addEdge(5,6);
        triangulator.addEdge(6,7);
        triangulator.addEdge(7,4);
 */
        
        triangulator.addEdge(4,7);
        triangulator.addEdge(7,6);
        triangulator.addEdge(6,5);
        triangulator.addEdge(5,4);

        IntBuffer intBuf = triangulator.triangulate();
        
        int pos = intBuf.position();
        
        System.out.println(intBuf.position());
        
        for(int i=0;i<pos;i+=3){
        	System.out.printf("(%d, %d, %d)\n",
        			intBuf.get(i),intBuf.get(i+1),intBuf.get(i+2));
        }
	}
}
