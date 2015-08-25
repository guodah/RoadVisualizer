package com.sim.io.exporters.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;

import com.sim.core.basic.ModelGenConsts;
import com.sim.io.exporters.TerrainExporter;
import com.sim.terrain.basic.BasicTerrain;
import com.sim.util.ModelExporterUtils;


/**
 * This class is able to exports a BasicTerrain.
 * 
 * @author Dahai Guo
 *
 */
public class BasicTerrainExporter extends TerrainExporter{

	private BasicTerrain terrain;
	
	private BasicTerrainExporter(BasicTerrain terrain){
		this.terrain = terrain;
	}

	/**
	 * Builds an export for a basic terrain.
	 * @param terrain which terrain object to export
	 * @return
	 */
	public static BasicTerrainExporter getExporter(BasicTerrain terrain){
		return new BasicTerrainExporter(terrain);
	}
	
	/**
	 * Uses {@link com.sim.util.ModelExporterUtils#saveUniTextureMesh(Formatter, java.util.ArrayList, int, float)} 
	 * to export the road.
	 */
	@Override
	public void export(String path) throws FileNotFoundException {
		Formatter format = new Formatter(path);
		
		format.format("1\n");
		ModelExporterUtils.saveUniTextureMesh(format, 
				terrain.getTriangles(), ModelGenConsts.TERRAIN, 
				ModelGenConsts.TEX_WRAP_SCALE);
		
		format.close();
	}

}
