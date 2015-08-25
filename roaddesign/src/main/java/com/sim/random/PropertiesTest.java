package com.sim.random;

import java.io.*;
import java.util.*;

public class PropertiesTest {
	public static void main(String args[]){
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream("RoadDesign.properties"));
		    System.out.println(properties.getProperty("mesh_file"));
		} catch (IOException e) {
			
		}
	}
}
