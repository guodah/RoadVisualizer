package com.sim.gui.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.sim.central.RoadDesign;


public class ResetHandler implements ActionListener{
	
	/**
	 * Calls {@link com.sim.central.RoadDesign#reset()} 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RoadDesign.reset();	// clears everything
		RoadDesign.update(); // re-draw
	}
}
