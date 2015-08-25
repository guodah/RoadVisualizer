package com.sim.io.exporters;

import java.io.FileNotFoundException;
import java.util.Formatter;

import com.sim.intersections.*;


public abstract class IntersectionExporter {
	/**
	 * Used to export intersection(s). 
	 * 
	 * @param filename file name where to export the intersection model
	 * @throws FileNotFoundException
	 */
	public abstract void export(String filename) throws FileNotFoundException;
}
