package com.sim.gui.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.sim.central.RoadDesign;


public class RenderHandler implements ActionListener{
	/**
	 * Calls {@link com.sim.central.RoadDesign#render()}
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		RoadDesign.render(); // loads the game engine
	}
}
