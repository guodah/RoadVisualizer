package com.sim.random;




import java.nio.IntBuffer;
import java.util.*;
//import com.jme.math.Vector3f;
import com.jme.math.Vector3f;
import com.jmex.font3d.math.Triangulator;

public class RoadUtil {
	

	public static void main(String args[]){
		Triangulator triangulator = new Triangulator();
        triangulator.addVertex(new Vector3f(-1.0f, -1.0f, 0.0f));
        triangulator.addVertex(new Vector3f( 1.0f, -1.0f, 0.0f));
        triangulator.addVertex(new Vector3f( 1.0f,  1.0f, 0.0f));
        triangulator.addVertex(new Vector3f(-1.0f,  1.0f, 0.0f));
        
        triangulator.addEdge(0,1);
        triangulator.addEdge(1,2);
        triangulator.addEdge(2,3);
        triangulator.addEdge(3,0);

        IntBuffer intBuf = triangulator.triangulate();
        int pos = intBuf.position();
        
        for(int i=0;i<pos;i+=3){
        	System.out.printf("%d -- (%d, %d, %d)\n",
        			i/3, intBuf.get(i),intBuf.get(i+1),intBuf.get(i+2));
        }

	}
	
}
