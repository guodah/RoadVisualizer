package com.sim.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import com.sim.central.RoadDesign;
import com.sim.curves.CurveParam;


/**
 * This class implements a dialog that asks the user for grade,
 * speed limit, and super-elevation at a given key point.
 * <p>
 * The users can call askForCurveParam to ask the dialog to be visualized, 
 * then the result, if accepted, will be put in a temporary buffer, maintained
 * by RoadDesign.
 * 
 * @author Dahai Guo
 *
 */
public class JCurveDialog extends JDialog
	implements ActionListener{
	private JComboBox<String> gradeCombo;
	private JCheckBox gradeRisingCheck;
	private JComboBox<String> speedCombo;
	private JComboBox<String> superEleCombo;
	private JButton okButton;
	private JButton cancelButton;
	private int speed, grade, superEle;

	public JCurveDialog(JFrame owner, String title, int x, int y){
		super(owner, true);
		setTitle(title);
		
		gradeCombo = new JComboBox<String>(new String[]{
			"0", "2", "4", "6", "8", "10", "12"	
		});
		gradeRisingCheck = new JCheckBox("", true);
		speedCombo = new JComboBox<String>(new String[]{
			"15", "20", "25", "30", "35", "40", "45",
			"50", "55", "60", "65", "70", "75", "80"
		});
		speedCombo.setSelectedIndex(4);
		superEleCombo = new JComboBox<String>(new String[]{
			"4", "6", "8"	
		});
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5,2));
		
		panel.add(new JLabel("Speed(mph): "));
		panel.add(speedCombo);
		
		panel.add(new JLabel("Grades(%): "));
		panel.add(gradeCombo);
		
		panel.add(new Label("Super Elevation(%): "));
		panel.add(superEleCombo);
		
		panel.add(new JLabel("Rising Curve"));
		panel.add(gradeRisingCheck);

		panel.add(okButton);
		panel.add(cancelButton);
		
		getContentPane().add(panel);
		pack();
		
		setResizable(false);
	//	setLocationRelativeTo(owner);
		setLocation(x,y);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if(evt.getSource() == okButton){
			grade = Integer.parseInt((String) 
					gradeCombo.getSelectedItem());
			if(!gradeRisingCheck.isSelected()){
				grade = -grade;
			}
			speed = Integer.parseInt((String)
					speedCombo.getSelectedItem());
			
			superEle = Integer.parseInt((String)
					superEleCombo.getSelectedItem());
			
			Map<CurveParam, Float> params =
					new HashMap<CurveParam, Float>();
			params.put(CurveParam.SPEED, (float) speed);
			params.put(CurveParam.GRADE, (float) grade);
			params.put(CurveParam.SUPERELEVATION, (float) superEle);
			
			RoadDesign.drop(params); // dropping to the temporary buffer
		}else if(evt.getSource() == cancelButton){
			RoadDesign.drop(null);
		}
		setVisible(false);
	}

	/**
	 * Visualizes the dialog
	 * @param frame
	 * @param title
	 * @param x location of the dialog
	 * @param y location of the dialog
	 */
	public static void askForCurveParam(JRoadDesignFrame frame, 
			String title, int x, int y) {
		JCurveDialog dialog = new JCurveDialog(frame, title, x, y);
	}
}
