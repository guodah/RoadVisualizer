package com.sim.central;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This class includes four states of the directory where the model is saved. 
 * The model path is the value of property 
 * {@link com.sim.central.RoadDesign#MODEL_PATH RoadDesign.MODEL_PATH}. 
 * 
 * @author Dahai Guo
 *
 */
public enum ModelPathState {
	/**
	 * The files in the model path is consistent. The renderer should be able to
	 * render it. 
	 * @see com.sim.renderer.AutoGenLoader
	 */
	OK, 
	
	/**
	 * The files in the model path is not consistent. The renderer will not be able to
	 * render it. 
	 * @see com.sim.renderer.AutoGenLoader
	 */
	INCONSISTENT, 
	
	/**
	 * The model path points to an empty directory.
	 */
	EMPTY, 
	
	/**
	 * The model path points to a non-existing directory.
	 */
	NON_EXIST;
	
	/**
	 * Checks the state of the directory where the model is supposed to 
	 * be saved.
	 * <p>
	 * Note this method only works for the model path structure created by
	 * {@link com.sim.io.exporters.basic.BasicIntersectionExporter}
	 * 
	 * @param modelPath value of property {@link com.sim.central.RoadDesign#MODEL_PATH}
	 * @return
	 */
	public static ModelPathState checkModelPathBasic(String modelPath){
		
		File modelDir = new File(modelPath);
		if(!modelDir.exists() || !modelDir.isDirectory()){
			return ModelPathState.NON_EXIST;
		}

		File files[] = modelDir.listFiles();
		if(files==null || files.length==0){
			return ModelPathState.EMPTY;
		}
		
		// lists all file names in model path
		String filenames[] = new String[files.length];
		for(int i=0;i<files.length;i++){
			filenames[i] = files[i].getAbsolutePath();
		}
		Arrays.sort(filenames);

		String fileSep = ""+File.separatorChar;
		if(!modelPath.endsWith(fileSep)){
			modelPath = modelPath + fileSep;
		}
		
		int match = 0;
		String catalog = modelPath + RoadDesign.getProperty(
				RoadDesign.MODEL_DIR);
		String startView = modelPath + RoadDesign.getProperty(
				RoadDesign.START_VIEW_PATH);
		
		// check if the model path contains a model directory and a file that
		// contains the start view point when the model is rendered
		int index = Arrays.binarySearch(filenames, catalog);
		match += (index>=0)?1:0;
		index = Arrays.binarySearch(filenames, startView);
		match += (index>=0)?1:0;
		
		if(match!=2){
			return ModelPathState.INCONSISTENT;
		}
		
		Scanner scan = null;
		try {
			 scan = new Scanner(new File(catalog));
		} catch (FileNotFoundException e) {
			return ModelPathState.INCONSISTENT;
		}
		
		// see if all the files, listed in the model directory all exist
		while(scan.hasNext()){
			String filename = scan.nextLine();
			
			index = Arrays.binarySearch(filenames, filename);
			if(index<0){
				scan.close();
				return ModelPathState.INCONSISTENT;
			}
		}
		scan.close();
		return ModelPathState.OK;
	}

}
