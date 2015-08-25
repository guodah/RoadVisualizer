package com.sim.central;

import java.util.EventListener;

import java.util.HashMap;
import java.util.Map;

import com.sim.core.basic.*;
import com.sim.gui.FrameComponents;
import com.sim.intersections.IntersectionBuilder;
import com.sim.intersections.basic.BasicIntersectionBuilder;
import com.sim.roads.RoadBuilder;
import com.sim.roads.basic.BasicRoadBuilder;
import com.sim.terrain.TerrainBuilder;
import com.sim.terrain.basic.BasicTerrainBuilder;

/**
 * This class implements a directory of builders for network components.
 *  
 * @author Dahai Guo
 *
 */
public class Builders {
	private Map<Class<? extends RoadBuilder>, RoadBuilder> roadBuilders;
	private Map<Class<? extends IntersectionBuilder>, 
		IntersectionBuilder> intersectionBuilders;
	private Map<Class<? extends TerrainBuilder>, 
		TerrainBuilder> terrainBuilders;
	
	public static Builders installBuilders(){
		Builders _builders = new Builders();
		
		_builders.roadBuilders = new HashMap
				<Class<? extends RoadBuilder>, RoadBuilder>();
		
		_builders.roadBuilders.put(BasicRoadBuilder.class, 
				new BasicRoadBuilder());

		_builders.intersectionBuilders = new HashMap
				<Class<? extends IntersectionBuilder>, IntersectionBuilder>();
		
		_builders.intersectionBuilders.put(BasicIntersectionBuilder.class, 
				new BasicIntersectionBuilder());

		_builders.terrainBuilders = new HashMap
				<Class<? extends TerrainBuilder>, TerrainBuilder>();
		
		_builders.terrainBuilders.put(BasicTerrainBuilder.class, 
				new BasicTerrainBuilder());
		return _builders;
	}

	public RoadBuilder findRoadBuilder(Class<? extends RoadBuilder> c) {
		return roadBuilders.get(c);
	}

	public IntersectionBuilder findIntersectionBuilder(Class<? extends IntersectionBuilder> c) {
		return intersectionBuilders.get(c);
	}

	public TerrainBuilder findTerrainBuilder(Class<? extends TerrainBuilder> c) {
		return terrainBuilders.get(c);
	}
}
