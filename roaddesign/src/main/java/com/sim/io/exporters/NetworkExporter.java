package com.sim.io.exporters;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import com.sim.network.RoadNetwork;


public abstract class NetworkExporter {
	protected RoadNetwork network;
	/**
	 * Used to export network model
	 * 
	 * @param modelPath path to the directory where to export network model
	 * @throws FileNotFoundException
	 */
	public abstract void export(String modelPath) 
			throws FileNotFoundException;
}
