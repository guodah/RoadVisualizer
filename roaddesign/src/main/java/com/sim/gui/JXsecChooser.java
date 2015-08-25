package com.sim.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sim.obj.CrossSection;


/**
 * This class gives the user choices on how to start editing a cross section.
 * The first choice is to have a default a cross section. The other is to load
 * an existing one from an xml file. Once the choice is made by the user, an
 * instance of JXsecEditor will be created to let the user edit it. 
 * 
 * @author Dahai Guo
 *
 */
public class JXsecChooser extends JDialog
	implements ActionListener{
	
	JRadioButton loadRadioButton;
	JRadioButton createRadioButton;
	JButton okButton;
	JButton cancelButton;
	JRoadDesignFrame owner;
	
	public JXsecChooser(JRoadDesignFrame owner, String title, 
			int x, int y){
		super(owner, true);
		setTitle(title);
		setSize(300, 100);
		
		init(x, y);
	}
	
	private void init(int x, int y){
		createRadioButton = new JRadioButton("Customize a default");
		createRadioButton.setSelected(true);
		loadRadioButton = new JRadioButton("Load from file");
		loadRadioButton.setSelected(false);
		okButton = new JButton("OK");
		cancelButton = new JButton("CANCEL");
		
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		setLayout(new BorderLayout());
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(loadRadioButton);
		bg.add(createRadioButton);
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new FlowLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		radioPanel.add(createRadioButton);
		radioPanel.add(loadRadioButton);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		add(radioPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		setLocation(x,y);	
	}
	
	public static void main(String args[]){
		JXsecChooser xsecChooser=new JXsecChooser(null, "Xsec Chooser", 
				200, 100);
		xsecChooser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		xsecChooser.setResizable(false);
		xsecChooser.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		if(e.getSource()==cancelButton){

		}else{
			CrossSection xsec;
			if(loadRadioButton.isSelected()){
				JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			            "Cross Section (*.xml)", "xml");
			     fc.setFileFilter(filter);

				int returnVal = fc.showOpenDialog(this);

				if(returnVal==JFileChooser.APPROVE_OPTION){
					File f = fc.getSelectedFile();
					String path = f.getAbsolutePath();

					xsec = CrossSection.readFromXML(path);
					
					JXsecEditor xsecEditor = new JXsecEditor(
							owner, "Xsec Editor", xsec, path);
				}
			}else{
				JXsecEditor xsecEditor = new JXsecEditor(
						owner, "Xsec Editor");
			}
		}
	}

	/**
	 * Visualizes a JXsecChooser to let user input parameters.
	 * 
	 * @param x horizontal coordinate of the dialog
	 * @param y vertical coordinate of the dialog
	 */
	public static void askForXsec(int x, int y) {
		JXsecChooser xsecChooser=new JXsecChooser(null, "Xsec Chooser", 
				x, y);
		
		xsecChooser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		xsecChooser.setResizable(false);
		xsecChooser.setVisible(true);
	}
}
