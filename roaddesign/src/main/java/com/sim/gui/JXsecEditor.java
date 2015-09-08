package com.sim.gui;

import java.awt.*;


import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Formatter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sim.central.Logging;
import com.sim.central.RoadDesign;
import com.sim.core.basic.RoadShapeCalc;
import com.sim.obj.CrossSection;


/**
 * This class lets users edit either a default cross section or an 
 * existing one from an xml file. The user can edit it, save it to xml, and/or
 * confirm it to close the window.
 * 
 * @author Dahai Guo
 *
 */
public class JXsecEditor extends JDialog 
	implements ActionListener, FocusListener{

	public final static int WIDTH = 800;
	public final static int HEIGHT = 300;

	protected JXsecDrawPanel xsecDisplayPanel;
	protected JPanel controlPanel;
	
	protected JComboBox numOfLanesInput;
	protected JComboBox laneWidthInput;
	protected JComboBox laneSlopeInput;
	protected JComboBox shoulderSlopeInput;
	protected JTextField shoulderWidthInput;
	protected JTextField medianWidthInput;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton saveButton;
	private JButton saveAsButton;
	
	private String path = null;
	private Integer grades[] = new Integer[]{0,2,4,6,8,10,12};
	private Integer numOfLanes[] = new Integer[]{1,2,3,4,5,6}; 
	private Integer laneWidth[] = new Integer[]{9,10,11,12,13,14};
	
	public JXsecEditor(JRoadDesignFrame owner, String title){

		this(owner, title, CrossSection.getDefault(), null);		
	}
	
	public JXsecEditor(JRoadDesignFrame owner, String title,
			CrossSection xsec, String path) {
		super(owner, true);
		this.setTitle(title);
		setSize(WIDTH, HEIGHT);
		
		init();
		populate(xsec);		
		this.path = path;
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    int w = getSize().width;
	    int h = getSize().height;
	    int x = (dim.width-w)/2;
	    int y = (dim.height-h)/2;
	    setLocation(x, y);
		setVisible(true);
	}

	private void populate(CrossSection xsec){
		this.laneSlopeInput.setSelectedItem(xsec.laneSlope);
		this.laneWidthInput.setSelectedItem((int)xsec.laneWidth);
		this.medianWidthInput.setText(
				(new Float(xsec.medianWidth).toString()));
		this.numOfLanesInput.setSelectedItem(xsec.numOfLanes);
		this.shoulderSlopeInput.setSelectedItem((int)xsec.shoulderSlope);
		this.shoulderWidthInput.setText(
				(new Float(xsec.shoulderWidth)).toString());		
	}
	
	private void init(){
		setLayout(new BorderLayout());
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(8,2));
		
		numOfLanesInput = new JComboBox(numOfLanes);
		laneWidthInput = new JComboBox(laneWidth);
		laneSlopeInput = new JComboBox(grades);
		shoulderSlopeInput = new JComboBox(grades);
	
		shoulderWidthInput = new JTextField(10);
		medianWidthInput = new JTextField(10);
		
		okButton = new JButton("OK");
		cancelButton = new JButton("CANCEL");
		saveButton = new JButton("SAVE");
		saveAsButton = new JButton("SAVE AS");
		
		numOfLanesInput.addActionListener(this);
		laneWidthInput.addActionListener(this);
		laneSlopeInput.addActionListener(this);
		shoulderSlopeInput.addActionListener(this);
		shoulderWidthInput.addFocusListener(this);
		medianWidthInput.addFocusListener(this);
		saveButton.addActionListener(this);
		saveAsButton.addActionListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		JPanel inputs [] = new JPanel[16];
		
		for(int i=0;i<inputs.length;i++){
			inputs[i] = new JPanel();
			inputs[i].setLayout(new FlowLayout());
		}
				
		inputs[0].add(new JLabel("Num of Lanes"));
		inputs[1].add(numOfLanesInput);
		
		inputs[2].add(new JLabel("Lane Width (ft)"));
		inputs[3].add(laneWidthInput);
		
		inputs[4].add(new JLabel("Lane Slope (%)"));
		inputs[5].add(laneSlopeInput);
		
		inputs[6].add(new JLabel("Shoulder Slope (%)"));
		inputs[7].add(shoulderSlopeInput);
		
		inputs[8].add(new JLabel("Shoulder Width (ft)"));
		inputs[9].add(shoulderWidthInput);
		
		inputs[10].add(new JLabel("Median Width (ft)"));
		inputs[11].add(medianWidthInput);

		inputs[12].add(saveButton);
		inputs[13].add(saveAsButton);
		
		inputs[14].add(okButton);
		inputs[15].add(cancelButton);
		
		for(int i=0;i<inputs.length;i++){
			controlPanel.add(inputs[i]);
		}
		add(controlPanel, BorderLayout.WEST);
		
		populate(CrossSection.getDefault());

		xsecDisplayPanel = new JXsecDrawPanel(this);
		xsecDisplayPanel.setBackground(Color.white);
		add(xsecDisplayPanel, BorderLayout.CENTER);
	}
	
	public static void main(String args[]){
//		JXsecEditor xsecEditor=new JXsecEditor(null, null, "Xsec Editor", 800, 300);
//		xsecEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		xsecEditor.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton){
			try{
				if(e.getSource()==okButton && verifyInput()){
					RoadDesign.drop(readXsec());
					setVisible(false);
				}else if(e.getSource()==cancelButton){
					RoadDesign.drop(null);
					setVisible(false);
				}else if((e.getSource()==saveButton && path==null) ||
						e.getSource()==saveAsButton){
					CrossSection xsec = readXsec();
					JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				    FileNameExtensionFilter filter = new FileNameExtensionFilter(
				            "Cross Section (*.xml)", "xml");
				     fc.setFileFilter(filter);
					int returnVal = fc.showSaveDialog(this);
	
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						path = f.getAbsolutePath();
						xsec.saveXML(path);
					}
				}else{
					readXsec().saveXML(path);
				}
			}catch(NumberFormatException ee){
				RoadDesign.displayError("Please input only non-negative numbers", "Input Error(s)", false);
				
			}
		}else{
			if(xsecDisplayPanel!=null)
				xsecDisplayPanel.repaint();
		}
	}

	public void readXsec(CrossSection xsec){
		xsec.laneSlope = 
			(Integer)(laneSlopeInput.getSelectedItem());
		xsec.laneWidth =
			(Integer)(laneWidthInput.getSelectedItem());
		xsec.medianWidth = new Float(
			medianWidthInput.getText()
		);
		xsec.numOfLanes = 
			(Integer)(numOfLanesInput.getSelectedItem());
		xsec.shoulderSlope =
			(Integer)(shoulderSlopeInput.getSelectedItem());
		xsec.shoulderWidth = new Float(
			shoulderWidthInput.getText()
		);		
	}
	
	public CrossSection readXsec(){
		CrossSection xsec = new CrossSection();
		readXsec(xsec);
		return xsec;
	}
	
	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		
		if(!verifyInput()){
			RoadDesign.displayError("Please input only non-negative numbers", "Input Error(s)", false);
		}else{
			xsecDisplayPanel.repaint();
		}
	}
	
	// verifies user input in road drawing content menu
	private boolean verifyInput(){
		try{ 
			if (Float.parseFloat(shoulderWidthInput.getText()) < 0 || Float.parseFloat(medianWidthInput.getText()) < 0){ // verifies input is a valid float and input is not negative
				return false;
			}
			return true;
		}catch(NumberFormatException e){
			return false;
		}
		
	}
}
