package com.sim.central;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



public class Logging {
	private static Logger logger = null;
	
	public static boolean init(){
		FileHandler fh = null;
		SimpleFormatter formatterTxt = new SimpleFormatter();
		try {
			 fh = new FileHandler(
					RoadDesign.getProperty(RoadDesign.LOG_FILE_NAME));
				
			 fh.setFormatter(formatterTxt);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		logger = Logger.getLogger("sim");
		logger.setLevel(Level.ALL);
		logger.addHandler(fh);
		
		return true;
	}
	
	public static Logger getLogger(){
		return logger;
	}
}
