package com.sim.io.exporters;

import java.io.FileNotFoundException;

import com.sim.terrain.Terrain;


public abstract class TerrainExporter {
	/**
	 * Used to export terrain(s). 
	 * 
	 * @param filename file name where to export the terrain model
	 * @throws FileNotFoundException
	 */
	public abstract void export(String filename) throws FileNotFoundException;
}
