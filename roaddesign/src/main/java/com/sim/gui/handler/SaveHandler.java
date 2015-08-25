package com.sim.gui.handler;

import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;

import com.sim.central.Logging;
import com.sim.central.ModelPathState;
import com.sim.central.RoadDesign;
import com.sim.gui.FrameComponents;
import com.sim.util.ModelExporterUtils;

/**
 * actionPerformed will be called when the user selects to click the save button
 * or the save/as menu item from the menu. There may exist several possibilities:
 * <p>
 * <ol>
 * <li> The user selects a valid directory which is empty
 * <li> The user selects a valid directory which is not empty. Then
 *      the user needs to either choose again or confirm to override any
 *      model, already saved in there.
 * <li> The user selects to quit at any time
 * </ol>
 * @author Dahai Guo
 *
 */
public class SaveHandler implements ActionListener{
	
	public void actionPerformed(ActionEvent e) {
		JComponent component = (JComponent) e.getSource();
		
		if(component==RoadDesign.findComponent(
				FrameComponents.MENU_FILE_SAVE_MODEL_AS)){
			// save_as asks to remove the existing model path
			// to find a new one
			RoadDesign.removeProperty(RoadDesign.MODEL_PATH);
		}
		try {
			String modelPath = RoadDesign.getProperty(RoadDesign.MODEL_PATH);
			
			// modelPath property does not exist, need to ask the user
			while(modelPath==null){
				modelPath = askModelPath();
				if(modelPath==null){
					// the user decided to exit
					return;
				}
				
				ModelPathState state = ModelPathState.
						checkModelPathBasic(modelPath); 
				
				if(state==ModelPathState.NON_EXIST){
					modelPath=null;
					RoadDesign.displayError("The path you selected does not exist", 
							"Non-existent directory...");
				}
				
				if(state!=ModelPathState.EMPTY){
					int userInput = RoadDesign.confirm(modelPath+
							" is not empty!\n"+
							"Do you want to override these files.", 
							"Saving model");
					switch(userInput){
					case JOptionPane.CANCEL_OPTION:
						return; // the user decides to exit
					case JOptionPane.YES_OPTION:
						break; // the user decides to override
					case JOptionPane.NO_OPTION:
						modelPath = null; // user decides to choose again
						break;
					}
				}
			}
			
			RoadDesign.getNetworkControl().export(modelPath);
			RoadDesign.setProperty(RoadDesign.MODEL_PATH, modelPath);
			
			Logging.getLogger().info(String.format(
				"Model saved to %s", modelPath
			));
		} catch (FileNotFoundException e1) {
			RoadDesign.displayError(e1.toString(), 
					"Saving Not Completed");
		}
	}
	
	private String askModelPath() throws FileNotFoundException{
		String modelPath = null; 
		JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(RoadDesign.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			modelPath = f.getAbsolutePath();
		}
		return modelPath;
	}
	
	
}
