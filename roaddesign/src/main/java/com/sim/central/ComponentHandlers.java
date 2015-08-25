package com.sim.central;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.sim.gui.FrameComponents;
import com.sim.gui.ViewMode;
import com.sim.gui.handler.MainMouseHandler;
import com.sim.gui.handler.RenderHandler;
import com.sim.gui.handler.ResetHandler;
import com.sim.gui.handler.SaveHandler;
import com.sim.roads.RoadTypes;


/**
 * This class implements a directory of event handlers for 
 * {@link com.sim.gui.JRoadDesignFrame} 
 * @see com.sim.gui.FrameComponents com.sim.gui.JRoadDesignFrame
 * @author Dahai Guo
 *
 */
public class ComponentHandlers {
	
	private Map<FrameComponents, EventListener> handlers;
	
	public static ComponentHandlers createHandlers(){
		
		ComponentHandlers _handlers = new ComponentHandlers();
		
		_handlers.handlers = new HashMap<FrameComponents, EventListener>();

		ActionListener handler = new ResetHandler();
		_handlers.handlers.put(FrameComponents.MENU_FILE_NEW, handler);
		_handlers.handlers.put(FrameComponents.TOOLBAR_NEW,	handler);

		handler = new SaveHandler();
		_handlers.handlers.put(FrameComponents.MENU_FILE_SAVE_MODEL, handler);
		_handlers.handlers.put(FrameComponents.MENU_FILE_SAVE_MODEL_AS, handler);
		_handlers.handlers.put(FrameComponents.TOOLBAR_SAVE_MODEL, handler);
		
		handler = new RenderHandler();
		_handlers.handlers.put(FrameComponents.MENU_OPERATIONS_RENDER, handler);
		_handlers.handlers.put(FrameComponents.TOOLBAR_RENDER, handler);
		
		_handlers.handlers.put(FrameComponents.MENU_FILE_EXIT, 
			new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					System.exit(1);
			}
		});
		
		_handlers.handlers.put(FrameComponents.TOOLBAR_TERRAIN_CHECK, 
			new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					RoadDesign.showTerrain(((JCheckBox)e.getSource()).isSelected());
					RoadDesign.update();
				}
		});

		_handlers.handlers.put(FrameComponents.TOOLBAR_VIEW_MODE_BOX, 
			new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					JComboBox box = (JComboBox)e.getSource();
					RoadDesign.setViewMode(
							(ViewMode) box.getSelectedItem());
				}
			
		});
		
		_handlers.handlers.put(FrameComponents.MAIN_DRAW_PANEL, 
			new MainMouseHandler());
		
		return _handlers;
	}

	public EventListener findHandler(FrameComponents componentName) {
		return handlers.get(componentName);
	}
}

