package com.sim.io.exporters.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;

import com.sim.io.exporters.RoadExporter;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.ModelExporterUtils;


public class BasicRoadExporter extends RoadExporter{

	private BasicRoad road;
	
	/**
	 * Builds an exporter for a specific basic road.
	 * @param road which road to export
	 * @return
	 */
	public static BasicRoadExporter getExporter(BasicRoad road){
		BasicRoadExporter exporter = new BasicRoadExporter(road);
		return exporter;
	}
	
	private BasicRoadExporter(BasicRoad road){
		this.road = road;
	}
	
	/**
	 * Uses {@link com.sim.util.ModelExporterUtils#saveRoadMesh(java.util.ArrayList, com.sim.obj.CrossSection, Formatter)} 
	 * to export the road.
	 */
	@Override
	public void export(String path) throws FileNotFoundException {
		Formatter format = new Formatter(path);
		// saving # of meshes
		format.format("%d\n", road.getXsecPts().get(0).size()-1);
		
		ModelExporterUtils.saveRoadMesh(road.getXsecPts(), 
				road.getXsec(), format);
		format.close();
	}

}
