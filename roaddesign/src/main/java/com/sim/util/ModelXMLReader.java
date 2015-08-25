package com.sim.util;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelXMLReader {
	
	public static void main(String args[]){
		Map [] maps = loadModelXML("mesh_files_global\\meshOP.xml");
		Map<Integer, String> images = maps[0];
		Set<Integer> keys = images.keySet();
		for(Integer i : keys){
			System.out.printf("%d\t%s\n", i, images.get(i));
		}
		
		System.out.println();
		
		Map<Integer, ArrayList<Float>> regions = maps[1];
		keys = regions.keySet();
		for(Integer i : keys){
			ArrayList<Float> coords = regions.get(i);
			System.out.printf("%d\t%.3f\t%.3f\t%.3f\t%.3f\n", i, 
					coords.get(0),coords.get(1),
					coords.get(2),coords.get(3));
		}
	}
	
	// the following constant should be saved in a file (warning)
	public static final String TEXTURE_DIRECTORY = "TextureDirectory";
	public static final String REGIONS = "regions";
	public static final String REGION_CODE = "Code";
	public static final String VERTICES = "Vertices";
	public static final String TEXTURE = "Texture";
	public static final String FILENAME = "Filename";
	public static final String REGION = "Region";
	public static final String TERRAIN = "Terrain";
	public static final String REGION_NAME = "Name";
	public static final String BOTTOM_LEFT_X = "BottomLeftX";
	public static final String BOTTOM_LEFT_Y = "BottomLeftY";
	public static final String TOP_RIGHT_X = "TopRightX";
	public static final String TOP_RIGHT_Y = "TopRightY";
	public static final String TERRAIN_VERTICES = "TerrainVertices";
	public static final String TERRAIN_TEXTURE = "TerrainTexture";
	public static final String INTERSECTIONS = "Intersections";
	
	public static final int REGION_TEXTURE_FILE = 0;
	public static final int REGION_BOTTOM_LEFT_X = 0;
	public static final int REGION_BOTTOM_LEFT_Y = 1;
	public static final int REGION_TOP_RIGHT_X = 2;
	public static final int REGION_TOP_RIGHT_Y = 3;
	
	public static final int TERRAIN_VERTICES_FILE = 0;
	public static final int TERRAIN_TEXTURE_FILE = 1;
	
	/**
	 * Loads information of texture maps.
	 * 
	 * @param filename should be two maps. The first maps region types to 
	 *                 texture file names. The second maps regions types to
	 *                 texture coordinates.
	 * @return
	 */
	public static Map[] loadModelXML(String filename){
		
		File file = new File(filename);
		
		// for return, see the documentation of this method
		Map<Integer, String> images = 
			new HashMap<Integer, String>();
		Map<Integer, ArrayList<Float>> regions =
			new HashMap<Integer, ArrayList<Float>>();
				
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			
			Element docEle = doc.getDocumentElement();
			
			NodeList texList = docEle.getElementsByTagName(TEXTURE);
			
			for(int i=0;i<texList.getLength();i++){
				Node node = texList.item(i);
				if(!node.getParentNode().getNodeName().equals(TEXTURE_DIRECTORY)){
					continue;
				}
				loadTexture(images, regions, (Element)node);
			}
						
		}catch(Exception e){
			e.printStackTrace();
		}
		return new Map[]{images, regions};
	}
	
	/**
	 * Loads information of texture maps.
	 * 
	 * @param images map that maps region types to texture file names
	 * @param regions map that maps regions types to texture coordinates
	 * @param ele xml element
	 */
	private static void loadTexture(Map<Integer, String> images, 
			Map<Integer, ArrayList<Float>> regions, Element ele){
		String texFileName = getTagValue(FILENAME, ele);
		NodeList regionList = ele.getElementsByTagName(REGION);
		for(int j=0;j<regionList.getLength();j++){
			Node tempNode = regionList.item(j);

			ArrayList<Float> region = new ArrayList<Float>();
			int regionCode = 
				Integer.parseInt(getTagValue(REGION_CODE, (Element) tempNode));
			float bottomLeftX = 
				Integer.parseInt(getTagValue(BOTTOM_LEFT_X, (Element) tempNode));
			float bottomLeftY = 
				Integer.parseInt(getTagValue(BOTTOM_LEFT_Y, (Element) tempNode));
			float topRightX = 
				Integer.parseInt(getTagValue(TOP_RIGHT_X, (Element) tempNode));
			float topRightY = 
				Integer.parseInt(getTagValue(TOP_RIGHT_Y, (Element) tempNode));

			region.add(bottomLeftX); region.add(bottomLeftY);
			region.add(topRightX); region.add(topRightY);
			
			regions.put(regionCode, region);
			images.put(regionCode, texFileName);
		}
	}
		
	/**
	 * Given a tag name, loads the corresponding value in an xml element.
	 * @param sTag
	 * @param eElement
	 * @return
	 */
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue().replaceAll(" ", "");
	  }
}
