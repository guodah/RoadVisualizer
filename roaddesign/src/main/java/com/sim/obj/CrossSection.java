package com.sim.obj;

import java.io.File;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.ModelGenConsts;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.geometries.RoadVector;
import com.sim.util.CollectionUtil;




/**
 * As of December 9, 2010. A cross section is defined by the following
 * parameters:
 * <ul>
 * 	<li> numOfLanes (ft) </li>
 * 	<li> laneWidth (ft) </li>
 * 	<li> laneSlope (percent) </li>
 * 	<li> shoulderWidth <ft)</li>
 * 	<li> shoulderSlope (percent) </li>
 * 	<li> medianWidth (ft) </li>
 * </ul>
 * <p>
 * medianWidth being zero means no median.  
 * 
 * @author Dahai Guo
 *
 */
public class CrossSection implements Cloneable{

	public int numOfLanes;
	public float laneWidth; // in feet
	public float laneSlope; // is a percentage number
	public float shoulderWidth; // in feet
	public float shoulderSlope; // is a percentage number
	public float medianWidth; // in feet

/*	
	public CrossSection clone(){
		CrossSection xsec = new CrossSection();
		xsec.numOfLanes = numOfLanes;
		xsec.laneSlope = laneSlope;
		xsec.laneWidth = laneWidth;
		xsec.shoulderSlope = shoulderSlope;
		xsec.shoulderWidth = shoulderWidth;
		xsec.medianWidth = medianWidth;
		return xsec;
	}
*/	
	
	/**
	 * Builds a string in the following format:
	 * <p>numOfLanes = ...
	 * <p>laneWidth = ...
	 * <p>shoulderWidth = ...
	 * <p>shoulderSlope = ...
	 * <p>medianWidth = ... 
	 */
	public String toString(){
		return "numOfLanes = " + numOfLanes +
			" laneWidth = " + laneWidth +
			" laneSlope = " + laneSlope +
			" shoulderWidth = " + shoulderWidth +
			" shoulderSlope = " + shoulderSlope +
			" medianWidth = " + medianWidth;
	}
	
	/**
	 * Reads from an xml file in the following format (example)<p>
	 * 
	 * {@literal <RoadProfile>}<p>
	 * {@literal	<CrossSection>}<p>
	 * {@literal		<numOfLanes> 4 </numOfLanes>}<p>
	 * {@literal		<laneWidth> 12 </laneWidth>}<p>
	 * {@literal		<laneSlope> 2 </laneSlope>}<p>
	 * {@literal		<shoulderWidth> 6 </shoulderWidth>}<p>
	 * {@literal		<shoulderSlope> 4 </shoulderSlope>}<p>
	 * {@literal		<medianWidth> 12 </medianWidth>}<p>
	 * {@literal	</CrossSection>}<p>
	 * {@literal </RoadProfile>}<p>
	 * 
	 * @param xmlFile xml file name
	 * @return a CrossSection object
	 */
	public static CrossSection readFromXML(String xmlFile){
		CrossSection cs = new CrossSection();
		
		try {
			File file = new File(xmlFile);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
		//	System.out.println("Root element " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("CrossSection");

			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);
			    
				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			  
					Element fstElmnt = (Element) fstNode;
					
					NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("numOfLanes");
					Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
					NodeList fstNm = fstNmElmnt.getChildNodes();
				//	 System.out.println("numOfLanes : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.numOfLanes = (int) Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());

					fstNmElmntLst = fstElmnt.getElementsByTagName("laneWidth");
					fstNmElmnt = (Element) fstNmElmntLst.item(0);
					fstNm = fstNmElmnt.getChildNodes();
				//	System.out.println("laneWidth : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.laneWidth = Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());

					fstNmElmntLst = fstElmnt.getElementsByTagName("laneSlope");
					fstNmElmnt = (Element) fstNmElmntLst.item(0);
					fstNm = fstNmElmnt.getChildNodes();
				//	System.out.println("laneSlope : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.laneSlope = Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());

					fstNmElmntLst = fstElmnt.getElementsByTagName("shoulderWidth");
					fstNmElmnt = (Element) fstNmElmntLst.item(0);
					fstNm = fstNmElmnt.getChildNodes();
				//	System.out.println("shoulderWidth : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.shoulderWidth = Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());

					fstNmElmntLst = fstElmnt.getElementsByTagName("shoulderSlope");
					fstNmElmnt = (Element) fstNmElmntLst.item(0);
					fstNm = fstNmElmnt.getChildNodes();
				//	System.out.println("shoulderSlope : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.shoulderSlope = Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());

					fstNmElmntLst = fstElmnt.getElementsByTagName("medianWidth");
					fstNmElmnt = (Element) fstNmElmntLst.item(0);
					fstNm = fstNmElmnt.getChildNodes();
				//	System.out.println("medianWidth : "  + ((Node) fstNm.item(0)).getNodeValue());
					cs.medianWidth = Float.parseFloat(((Node) fstNm.item(0)).getNodeValue());
					
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return cs;
	}
	
	/**
	 * Saves the current cross section to an xml file.
	 * <p>
	 * See {@link #readFromXML(String)} for the format of xml.
	 * 
	 * @param filename needs to be an xml file
	 */
	public void saveXML(String filename) {
		 
	    try{
	 
		  DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
		  //root elements
		  Document doc = docBuilder.newDocument();
		  Element rootElement = doc.createElement("RoadProfile");
		  doc.appendChild(rootElement);
	 
		  //staff elements
		  Element staff = doc.createElement("CrossSection");
		  rootElement.appendChild(staff);
	 
		  Element numOfLanesEle = doc.createElement("numOfLanes");
		  numOfLanesEle.appendChild(doc.createTextNode(
				  (new Integer(numOfLanes)).toString()));
		  staff.appendChild(numOfLanesEle);
	 
		  Element laneWidthEle = doc.createElement("laneWidth");
		  laneWidthEle.appendChild(doc.createTextNode(
				  (new Float(laneWidth)).toString()));
		  staff.appendChild(laneWidthEle);

		  Element laneSlopeEle = doc.createElement("laneSlope");
		  laneSlopeEle.appendChild(doc.createTextNode(
				  (new Float(laneSlope)).toString()));
		  staff.appendChild(laneSlopeEle);

		  Element shoulderWidthEle = doc.createElement("shoulderWidth");
		  shoulderWidthEle.appendChild(doc.createTextNode(
				  (new Float(shoulderWidth)).toString()));
		  staff.appendChild(shoulderWidthEle);
		  
		  Element shoulderSlopeEle = doc.createElement("shoulderSlope");
		  shoulderSlopeEle.appendChild(doc.createTextNode(
				  (new Float(shoulderSlope)).toString()));
		  staff.appendChild(shoulderSlopeEle);
		  
		  Element medianWidthEle = doc.createElement("medianWidth");
		  medianWidthEle.appendChild(doc.createTextNode(
				  (new Float(medianWidth)).toString()));
		  staff.appendChild(medianWidthEle);
		  
		  //write the content into xml file
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  DOMSource source = new DOMSource(doc);
		  StreamResult result =  new StreamResult(new File(filename));
		  transformer.transform(source, result);
	 
	//	  System.out.println("Done");
	 
	     }catch(ParserConfigurationException pce){
		  pce.printStackTrace();
	     }catch(TransformerException tfe){
		 tfe.printStackTrace();
	     }
	 } 

	/**
	 * Calculates the span of the cross-sectional profile.
	 * @return
	 */
	public float findWidth2d(){
		
		
		ArrayList<RoadVector> pts = RoadShapeCalc.findCrossSectionOp(
			new RoadVector(0,0,0),
			new RoadVector(1,0,0),
			new RoadVector(-1,0,0),
			this
		);
		
		return RoadVector.subtract(
				pts.get(0), pts.get(pts.size()-1)).
				magnitude2d();
	}
	
	/**
	 * This needs to be changed later to read the default cross section
	 * from a file.
	 * 
	 * @return
	 */
	public static CrossSection getDefault(){
		CrossSection xsec = new CrossSection();
		xsec.numOfLanes = 1;//2;
		xsec.laneSlope = 2;
		xsec.laneWidth = 12;
		xsec.medianWidth = 20;//0;//20;
		xsec.shoulderSlope = 2;
		xsec.shoulderWidth = 10;//0;//10;
		return xsec;
	}
	
	public static void main(String args[]){
	//	CrossSection cs = readFromXML("TempCrossSection.xml");
	//	cs.saveXML("test.xml");
	//	System.out.println(cs);
	}
	
}
