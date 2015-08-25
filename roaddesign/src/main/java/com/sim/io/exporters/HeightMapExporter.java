package com.sim.io.exporters;

import java.io.FileNotFoundException;

import com.sim.network.RoadNetwork;

public abstract class HeightMapExporter {
	protected RoadNetwork network;
	public abstract void export(String modelPath)
			throws FileNotFoundException;
}
