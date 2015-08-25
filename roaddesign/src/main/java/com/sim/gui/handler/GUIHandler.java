package com.sim.gui.handler;


import java.awt.Graphics;

import com.sim.gui.ThreeDEnv;
import com.sim.gui.ViewMode;


public interface GUIHandler {
	void draw(Graphics g, com.sim.gui.ThreeDEnv threeDEnv, ViewMode viewMode);
}
