package com.sim.io.exporters.basic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;

import javax.imageio.ImageIO;

import com.jme.math.Triangle;
import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;
import com.sim.central.RoadDesign;
import com.sim.geometries.Vector23f;
import com.sim.intersections.Intersection;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.io.exporters.IntersectionExporter;
import com.sim.io.exporters.NetworkExporter;
import com.sim.io.exporters.RoadExporter;
import com.sim.io.exporters.TerrainExporter;
import com.sim.network.RoadNetwork;
import com.sim.roads.Road;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.ModelExporterUtils;
import com.sim.util.ModelGenUtil;
import com.sim.util.ModelImporter;


public class BasicNetworkExporter extends NetworkExporter{

	public BasicNetworkExporter(RoadNetwork network){
		this.network = network;
	}
	
	/**
	 * Exports a BasicNetwork to a file.
	 * <p>
	 * Things to export include
	 * <ol>
	 * <li> a start view when the model is loaded in the game engine 
	 * <li>
	 * </ol>
	 * @param modelPath the directory to where the model will be exported to 
	 */
	@Override
	public void export(String modelPath) 
			throws FileNotFoundException {
		File path = new File(modelPath);
		if(!path.exists()){
			RoadDesign.removeProperty(RoadDesign.MODEL_PATH);
			throw new FileNotFoundException(String.format(
					"%s does not exist.", modelPath));			
		}else if(!path.isDirectory()){
			RoadDesign.removeProperty(RoadDesign.MODEL_PATH);
			throw new FileNotFoundException(String.format(
				"%s is not a directory.", modelPath));
		}
	
		Iterator<Road> roads = network.getRoadSegments();
		BasicRoad road = null;
		if(roads.hasNext()){
			road = (BasicRoad) roads.next();
		}else{
			return;
		}
		
		ArrayList<String> filenames = new ArrayList<String>();
		
		String filename = String.format("%s\\%s", modelPath,
				RoadDesign.getStartViewPath());
		
		Formatter format = new Formatter(filename);
		
		// saves a start view which is at where the first road starts
		ModelExporterUtils.saveStartView(
				road.getXsecPts(), road.getCurvePts(), format);
		format.close();
		
		// exports roads
		roads = network.getRoadSegments();
		int i = 0;
		while(roads.hasNext()){
			road = (BasicRoad) roads.next();
			RoadExporter ex = road.getExporter();
			filename = String.format("%s\\road%d.out", modelPath,i++);
			ex.export(filename);
			filenames.add(filename);
		}
		
		Iterator<Intersection> intersections = network.getIntersections();
		i = 0;
		// exports intersection
		while(intersections.hasNext()){
			Intersection intersection = intersections.next();
			IntersectionExporter ex = intersection.getExporter();
			filename = String.format("%s\\intersection%d.out", modelPath,i++);
			ex.export(filename);
			filenames.add(filename);
		}

		// exports terrain
		TerrainExporter ex = network.getTerrain().getExporter();
		filename = String.format("%s\\terrain.out", modelPath); 
		ex.export(filename);
		filenames.add(filename);
		
		// exports all the created files
		String modelDirPath = String.format("%s\\%s", 
			modelPath, RoadDesign.getModelDir());
		format = new Formatter(modelDirPath);
		for(String file : filenames){
			format.format("%s\n", file);
		}
		format.close();
		
		// loads the model that was just exporter for calculating the
		// heightmap
		(new BasicHeightMapExporter(network)).export(modelPath);
	}
}

