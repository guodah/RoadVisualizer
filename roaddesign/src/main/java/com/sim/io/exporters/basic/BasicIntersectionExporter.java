package com.sim.io.exporters.basic;

import java.io.File;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;

import com.sim.core.basic.ModelGenConsts;
import com.sim.geometries.RoadVector;
import com.sim.intersections.basic.BasicIntersection;
import com.sim.intersections.basic.BasicLeg;
import com.sim.io.exporters.IntersectionExporter;
import com.sim.util.ModelExporterUtils;


/**
 * This class uses methods defined in {@link com.sim.util.ModelExporterUtils} 
 * to export an object of {@link com.sim.intersections.basic.BasicIntersection}.
 * @author Dahai Guo
 *
 */
public class BasicIntersectionExporter 
	extends IntersectionExporter{

	/**
	 * the intersection to export
	 */
	private BasicIntersection intersection;
	
	/**
	 * Builds an basic intersection exporter.
	 * 
	 * @param intersection reference to which intersection to export.
	 * @return
	 */
	public static BasicIntersectionExporter 
		getExporter(BasicIntersection intersection){
		BasicIntersectionExporter ex = 
				new BasicIntersectionExporter(intersection);
		return ex;
	}
	
	private BasicIntersectionExporter(BasicIntersection intersection){
		this.intersection = intersection;
	}
	
	/**
	 * Exports the intersection model. 
	 * <p>
	 * See {@link #findNumOfMeshes()} to find what meshes will be exported.
	 * <p>
	 * It uses {@link com.sim.util.ModelExporterUtils#saveRoadMesh(java.util.ArrayList, com.sim.obj.CrossSection, Formatter)}
	 * and {@link com.sim.util.ModelExporterUtils#saveUniTextureMesh(Formatter, java.util.ArrayList, int, float)}.  
	 */
	@Override
	public void export(String path) throws FileNotFoundException {
		Formatter format = new Formatter(path);
		
		int numOfMeshes = findNumOfMeshes();
		
		format.format("%d\n", numOfMeshes);
		
		// exporting shoulder and inner area
//		BasicIntersectionModel model = intersection.getModel();
		ModelExporterUtils.saveUniTextureMesh(format, intersection.getInnerArea(), 
				ModelGenConsts.PAVEMENT, ModelGenConsts.TEX_WRAP_SCALE);
		ModelExporterUtils.saveUniTextureMesh(format, intersection.getShoulderAreas(), 
				ModelGenConsts.SHOULDER, ModelGenConsts.TEX_WRAP_SCALE);
		
		// exporting extensions and markers
		ArrayList<BasicLeg> legs = intersection.getLegs();
		if(legs!=null){
			ArrayList<RoadVector> markers = 
					new ArrayList<RoadVector>();
			for(BasicLeg leg : legs){
				ArrayList<ArrayList<RoadVector>> extension = 
						leg.getExtension();
				ModelExporterUtils.saveRoadMesh(extension, 
						leg.getRoad().getXsec(), format);
				markers.addAll(ModelExporterUtils.
						convertStripToMesh(leg.getMarker()));
			}
			
			// exports the white markers that border the intersection
			// and the legs
			ModelExporterUtils.saveUniTextureMesh(format, markers, 
					ModelGenConsts.WHITE_MARKER, ModelGenConsts.TEX_WRAP_SCALE);
		}
		format.close();
	}

	/**
	 * The following components are built into meshes<p>
	 * <ol>
	 * <li> shoulder 
	 * <li> inner area (without pavement marker)
	 * <li> white markers of all four legs (makers that border 
	 *      the intersection and the leg)
	 * <li> meshes in extensions (each cross section segment is a mesh)
	 * </ol>
	 * 
	 * @return
	 */
	private int findNumOfMeshes() {
		int numOfMeshes = 0;
		
	//	BasicIntersectionModel model = intersection.getModel();
		if(intersection!=null){
			numOfMeshes += (intersection.getInnerArea()!=null)?1:0;
			numOfMeshes += (intersection.getShoulderAreas()!=null)?1:0;
		}
		
		ArrayList<BasicLeg> legs = intersection.getLegs();
		if(legs!=null){
			numOfMeshes += 1; // white markers
		}
		for(BasicLeg leg : legs){
			ArrayList<ArrayList<RoadVector>> extension = leg.getExtension();
			if(extension!=null)
				numOfMeshes += extension.get(0).size()-1;
		}
		
		return numOfMeshes;
	}
}
