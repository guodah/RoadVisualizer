package com.sim;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sim.central.RoadDesign;

public class RoadDesignTest {

	@Test
	public void testRoadDesign() {
		RoadDesign.start("RoadDesign.properties");
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
