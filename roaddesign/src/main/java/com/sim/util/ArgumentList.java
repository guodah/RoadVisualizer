package com.sim.util;

import com.sim.central.RoadDesign;

public class ArgumentList {
	public static String DEBUG = "-d";
	public static String PROPERTIES = "-p";
	
	/**
	 * So far the user can input up to two parameters: {@link #DEBUG} and
	 * {@link #PROPERTIES}.
	 * 
	 * @param args the user's arguments from main(String[] args)
	 * @return the property file name
	 */
	public static String parseArgs(String [] args){
		String propertyFileName = null;
		for(int i=0;i<args.length;i++){
			String arg = args[i];
			if(arg.equals(DEBUG)){
				RoadDesign.DEBUG = true;
			}else if(arg.equals(PROPERTIES)){
				if(i>=args.length-1){
					break;
				}else{
					if(args[i+1].toLowerCase().
							indexOf(".properties")<0){
						break;
					}else{
						propertyFileName = args[i+1];
						i++;
					}
				}
			}
		}
		return propertyFileName;
	}

}
