package com.sim.intersections.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.sim.core.basic.DesignConsts;
import com.sim.core.basic.IntersectionCalcError;
import com.sim.curves.ThreeCenteredCurve;
import com.sim.curves.ThreeCenteredTurnConsts;
import com.sim.geometries.RoadVector;
import com.sim.geometries.Vector23f;
import com.sim.intersections.Directions;
import com.sim.intersections.IntersectionBuilder;
import com.sim.network.RoadNetwork;
import com.sim.obj.CrossSection;
import com.sim.roads.Road;
import com.sim.roads.basic.BasicRoad;
import com.sim.util.CopyUtil;
import com.sim.util.ModelGenUtil;
import com.sim.util.TriangulatorAdapter;




public class BasicIntersectionBuilder extends IntersectionBuilder{
	/**
	 * The minimum ratio that is (road straight section before intersection)
	 * divided by (the road width of the other road being intersected) 
	 */
	public static final float SIDE_CLEARANCE = 4.0f;
	
	/**
	 * When a road is grown by another key point, it may intersect some other roads in 
	 * the 2D space. To date, there are no overpass considered. So all intersecting roads
	 * must be intersect at the same elevation. So when two roads intersect in 2D, but at 
	 * different elevations, the new key point will be rejected.
	 * 
	 * @param road1
	 * @param network
	 * @return null: not intersecting any road<p>
	 * 			an empty map: not all intersecting road are valid<p>
	 * 			none empty map: all BasicRoad that can intersect road1
	 * 							the key is the distance from the second
	 * 							last point in road1 
	 */
	public NavigableMap<Float, BasicRoad> findInterCandidates(
			BasicRoad road1, RoadNetwork network){
		NavigableMap<Float, BasicRoad> result=null;
		
		// get the second last point in road1's center line
		ArrayList<RoadVector> curve = road1.getCurvePts();
		RoadVector v1 = curve.get(curve.size()-2); 
		
		ArrayList<RoadVector> temp = new ArrayList<RoadVector>();
		
		ArrayList<CrossSection> xsecs = new ArrayList<CrossSection>();

		Iterator<Road> roads = network.getRoadSegments(); 
		while(roads.hasNext()){

			Road road2 = roads.next();
			
			if(road1==road2 || !(road2 instanceof BasicRoad)){
				continue;
			}
			
			xsecs.clear();
			xsecs.add(road1.getXsec());
			xsecs.add(((BasicRoad)road2).getXsec());

		
			// find where road1 and road2 intersect by returning the four centerline
			// points (two for road1 and two for road2) which define the intersection
			// in the 2D space.
			//
			// Note when road1 and road2 intersect at multiple places, the size of return
			// list will contain a multiple of four points.
			ArrayList<RoadVector> interPts = findInter2D(road1, (BasicRoad)road2);
			if(interPts!=null){
				result = (result==null)?new TreeMap<Float, BasicRoad>():result;
				while(interPts.size()>=4){

					temp.clear();
					for(int i=0;i<4;i++,temp.add(interPts.remove(0)));
					
					if(isValid(temp,xsecs)){
						
						// when the intersection is valid which means the intersection
						// can be implemented, add road2 to the map given the key which
						// is the distance between the intersection of center lines and
						// the center line point in road1 before growing the new key point
						RoadVector inter = RoadVector.intersect(temp.get(0), 
							temp.get(1),temp.get(2),temp.get(3));
	
						result.put(RoadVector.subtract(v1, inter).
								magnitude2d(), (BasicRoad) road2);
						
					}else{
						// when a certain intersection is not valid, return an empty 
						// container.
						result.clear();
						return result;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Tests if and where road1 and road2 intersect in the 2D space.
	 * <p>
	 * For each intersection, add the four center lines points (two for road1 and
	 * two for road2) which define the intersection to the returned list. 
	 * 
	 * @param road1
	 * @param road2
	 * @return
	 */
	private ArrayList<RoadVector> findInter2D(BasicRoad road1, BasicRoad road2) {
		
		ArrayList<RoadVector> curve1 = road1.getCurvePts();
		ArrayList<RoadVector> curve2 = road2.getCurvePts();
		
		ArrayList<RoadVector> temp = null;

		RoadVector v1 = curve1.get(0);
		for(int i=0;i<curve1.size()-1;i++){
			RoadVector v2 = curve1.get(i+1);
		
			RoadVector _v1 = curve2.get(0);
			RoadVector inter;
			for(int j=0;j<curve2.size()-1;j++){

				RoadVector _v2 = curve2.get(j+1);
				inter = RoadVector.intersect(v1.clearZ(), v2.clearZ(), 
						_v1.clearZ(), _v2.clearZ());
				
				if(inter!=null){
					temp=(temp==null)?new ArrayList<RoadVector>():temp;
					temp.add(_v1);temp.add( _v2);
					temp.add(v1); temp.add(v2);
				}
				
				_v1 = _v2;
			}
			
			v1 = v2;
		}
		
		return temp;
	}

	/**
	 * A BasicIntersection only exists when<p>
	 * <ol>
	 * <li> two roads are intersecting
	 * <li> both roads have zero grade where they intersect
	 * <li> both roads have a same elevation
	 * <li> the road geometries are creating too small a space for
	 *      the intersection. Here a constant, SIDE_CLEARANCE.
	 * </ol>
	 * @param approaches See the same parameter in 
	 * {@link com.sim.intersections.basic.BasicIntersection#findIntersection(ArrayList, ArrayList)}
	 * @return
	 */
	public boolean isValid(
			ArrayList<RoadVector> approaches, ArrayList<CrossSection> xsecs) {
		IntersectionCalcError.clear();
		
		// needs to have four points, the first two define
		// the first approaches, the last two define the second
		if(approaches.size()!=4){
			IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_WRONG_NUM_PTS;
		}else if(xsecs.size()!=2){
			IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_WRONG_NUM_XSECS;
		}

		
		if(!haveZeroGrade(approaches)){
			IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_NON_ZERO_GRADE;
		}
		
//		if(xsecs.get(0).medianWidth < 
//				MIN_MEDIAN_LANE_RATIO*xsecs.get(1).laneWidth){
//			Failures.failure |= Failures.BASIC_INTER_NARROW_MEDIAN;
//		}
		
		float z[] = new float[4];
		for(int i=0;i<4;i++){
			z[i] = approaches.get(i).getZ();
			approaches.get(i).setZ(0.0f);
		}
	
		RoadVector inter = RoadVector.intersect(approaches.get(0), 
				approaches.get(1), approaches.get(2), approaches.get(3));
		if(inter==null){
			IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_NO_INTERSECT;
		}else{
			if(RoadVector.subtract(inter, approaches.get(0)).magnitude2d()<
					xsecs.get(0).findWidth2d()*SIDE_CLEARANCE){
				IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_LESS_CLEARANCE;
			}
			if(RoadVector.subtract(inter, approaches.get(1)).magnitude2d()<
					xsecs.get(0).findWidth2d()*SIDE_CLEARANCE){
				IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_LESS_CLEARANCE;
			}
			if(RoadVector.subtract(inter, approaches.get(2)).magnitude2d()<
					xsecs.get(1).findWidth2d()*SIDE_CLEARANCE){
				IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_LESS_CLEARANCE;
			}
			if(RoadVector.subtract(inter, approaches.get(3)).magnitude2d()<
					xsecs.get(1).findWidth2d()*SIDE_CLEARANCE){
				IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_LESS_CLEARANCE;
			}
		}

		for(int i=0;i<4;i++){
			approaches.get(i).setZ(z[i]);
		}
		
		if(Float.compare(approaches.get(0).getZ(),
				approaches.get(2).getZ())!=0){
			IntersectionCalcError.failure |= IntersectionCalcError.BASIC_INTER_NON_EQUAL_Z;
		}
		
		return (IntersectionCalcError.failure==0);
	}
	
	/**
	 * Checks if the roads for intersection are all at zero grade.
	 * 
	 * @param approaches See the same parameter in 
	 * {@link com.sim.intersections.basic.BasicIntersection#findIntersection(ArrayList, ArrayList)}
	 * @return
	 */
	private boolean haveZeroGrade(
			ArrayList<RoadVector> approaches) {
		
		// see if the two approaches both have zero grade
		float deltaZ1 = approaches.get(0).getZ()-
			approaches.get(1).getZ();
		float deltaZ2 = approaches.get(2).getZ()-
			approaches.get(3).getZ();
		
		if(Float.compare(deltaZ1,0)!=0){
			return false;
		}else if(Float.compare(deltaZ2, 0)!=0){
			return false;
		}

		return true;
	}
	
	/**
	 * Splits two roads given an intersection.
	 * <p>
	 * Note the intersection must be the correct intersection 
	 * between road1 and road2.
	 * <p>
	 * road1 is supposed to remain open after losing some part because of the intersection 
	 * 
	 * @param intersection
	 * @param road1
	 * @param road2
	 * @param network
	 * @return see the description
	 */
	protected BasicRoad splitRoads(BasicIntersection intersection,
			BasicRoad road1, BasicRoad road2, RoadNetwork network) {
		BasicRoad result = null;
		
		/*
		 * For each direction of the intersection, finds its leg
		 * and end point where the intersection begins and leg ends. 
		 */
		for(int i=0;i<intersection.getRoadEnds().size();i++){
			BasicLegType type = intersection.getLegTypes().get(i);
			RoadVector end = intersection.getRoadEnds().get(i);
			
			// roadEnds(0) and roadEnds(2) are for road1 direction
			// roadEnds(1) and roadEnds(3) are for road2 direction
			BasicRoad oldRoad = (i%2==0)?road1:road2;
			
			// cut the road and return the piece before end
			BasicRoad road = oldRoad.cut(end,type);
			
			// the split also affects the intersection on the other side
			BasicIntersection another = (BasicIntersection) 
					oldRoad.getIntersection(type.not());
			
			// add the intersections to the cut road
			road.addIntersection(type.not(), another);
			road.addIntersection(type, intersection);
			
			// replace the road
			if(another!=null)
				another.replace(oldRoad, road);

			// find the leg for the new cut road
			BasicLeg leg = BasicLeg.getLeg(road, 
					intersection.getExtensions().get(i), 
					intersection.getLegTypes().get(i));
			
			// the road on the north direction will remain open 
			if(i==Directions.NORTH.value()){
				result = road;
			}else{
				road.close();
			}
			
			network.addRoad(road);
			intersection.getLegs().add(leg);
		}
		
		// road1 and road2 no longer exist
		network.removeRoad(road1);
		network.removeRoad(road2);
			
		return result;
	}

	/**
	 * Uses {@link com.sim.curves.ThreeCenteredCurve} to find the a list of points, outlines
	 * the turn.
	 * 
	 * @param turnPairs the direction before and after the turn
	 * @return
	 */
	public  ArrayList<RoadVector> findTurnCurve(Vector23f[] turnPairs) {
		float [] radii = ThreeCenteredTurnConsts.findRadii(
				turnPairs[0], turnPairs[1], null);
		float [] offsets = ThreeCenteredTurnConsts.findOffsets(
				turnPairs[0], turnPairs[1], null);

		ThreeCenteredCurve curve = new ThreeCenteredCurve(
			radii, offsets, turnPairs[0], turnPairs[1]
		);
		
		ArrayList<Vector23f> curvePoints = curve.findCurvePoints();
		ArrayList<RoadVector> result = new ArrayList<RoadVector>();
		for(int i=0;i<curvePoints.size();i++){
			result.add(new RoadVector(curvePoints.get(i)));
		}
		return result;
	}

	/**
	 * For each turn (four total), finds the directions before and after the turn.
	 * 
	 * @param approaches See the same parameter in 
	 * {@link com.sim.intersections.basic.BasicIntersection#findIntersection(ArrayList, ArrayList)}
	 * @return
	 */
	public  Vector23f[][] findTurnPairs(
			ArrayList<RoadVector> approaches) {
		
		// find the directions of the two intersecting roads
		Vector23f direction1 = RoadVector.subtract(
				approaches.get(1), approaches.get(0)).
				unit2d().getVector();
		Vector23f direction2 = RoadVector.subtract(
				approaches.get(3), approaches.get(2)).
				unit2d().getVector();

		Vector23f _dir1 = direction1.reverse();
		Vector23f _dir2 = direction2.reverse();;
		
		Vector23f[][] result = new Vector23f[4][2];
		
		if(Vector23f.isLeftTurn(direction1, direction2)){
			result[0][0] = direction1; result[0][1] = _dir2;
			result[1][0] = direction2; result[1][1] = direction1;
			result[2][0] = _dir1; result[2][1] = direction2;
			result[3][0] = _dir2; result[3][1] = _dir1;
		}else{
			result[0][0] = direction1; result[0][1] = direction2;
			result[1][0] = _dir2; result[1][1] = direction1;
			result[2][0] = _dir1; result[2][1] = _dir2;
			result[3][0] = direction2; result[3][1] = _dir1;			
		}
		return result;
	}

	/**
	 * Each element in the return list is the direction of the road, leaving the 
	 * intersection. The order is counter-clockwise. 
	 * <p>
	 * The first one, returned, is the one of the road in the south if you look at
	 * the parameter directions with the first one pointing to the north.
	 * 
	 * @param directions directions of the intersecting road
	 * @return
	 */
	public  ArrayList<RoadVector> findAllDirs(
			ArrayList<RoadVector> directions){
		// find the two directions
		RoadVector direction1 = directions.get(0);
		RoadVector direction2 = directions.get(1);

		ArrayList<RoadVector> dirs = new ArrayList<RoadVector>();
		if(Vector23f.isLeftTurn(direction1.getVector(), 
				direction2.getVector())){
			dirs.add(direction1.reverse());
			dirs.add(direction2.reverse());
			dirs.add(direction1.duplicate());
			dirs.add(direction2.duplicate());
		}else{
			dirs.add(direction1.reverse());
			dirs.add(direction2.duplicate());
			dirs.add(direction1.duplicate());
			dirs.add(direction2.reverse());			
		}
		
		return dirs;
	}

	/**
	 * Finds the intersections of roads with width. 
	 * <p>
	 * The intersections are in counter clockwise order, starting from the one
	 * right to and below the center line intersection.
	 * 
	 * @param interCenterLines intersection of center lines
	 * @param approaches See the same parameter in 
	 * {@link com.sim.intersections.basic.BasicIntersection#findIntersection(ArrayList, ArrayList)}
	 * @param xsecs See the same parameter in 
	 * {@link com.sim.intersections.basic.BasicIntersection#findIntersection(ArrayList, ArrayList)}
	 * @return
	 */
	public  ArrayList<RoadVector> findAllInters(RoadVector interCenterLines,
			ArrayList<RoadVector> approaches, ArrayList<CrossSection> xsecs) {

		// find the two directions
		RoadVector direction1 = RoadVector.subtract(
				approaches.get(1), approaches.get(0)).unit2d();
		RoadVector direction2 = RoadVector.subtract(
					approaches.get(3), approaches.get(2)).unit2d();
		
		float offsets[][];
		if(Vector23f.isLeftTurn(direction1.getVector(), 
				direction2.getVector())){
			 offsets = new float[][]{{-1,-1}, {-1,1}, {1,1}, {1,-1}};
		}else{
			 offsets = new float[][]{{1,-1}, {1,1}, {-1,1}, {-1,-1}};
		}
		
//		float tempX = direction1.getX(), tempY = direction1.getY();
//		direction1.setX(tempY); direction1.setY(-tempX);
//		tempX = direction2.getX(); tempY = direction2.getY();
//		direction2.setX(tempY); direction2.setY(-tempX);
		
		// find xsec widths
				
		float width1 = xsecs.get(0).findWidth2d()/2-
				xsecs.get(0).shoulderWidth;
		float width2 = xsecs.get(1).findWidth2d()/2-
				xsecs.get(1).shoulderWidth;
		
		float cos = RoadVector.dot(direction1.unit2d(), 
				direction2.unit2d());
		float sin = (float) Math.sqrt(1-cos*cos);
		ArrayList<RoadVector> inters = new ArrayList<RoadVector>();
		for(int i=0;i<offsets.length;i++){
			inters.add(RoadVector.add(
				RoadVector.add(interCenterLines, 
					RoadVector.multi(direction2, 
							width1*offsets[i][0]/sin
					)
				),
				RoadVector.multi(direction1, 
					width2*offsets[i][1]/sin
				)
			));
		}
		return inters;
	}

	/**
	 * Finds and triangulates polygons enclosing shoulder.
	 * <p>
	 * Note polygons need to have its inner area to the 
	 * left of the borders
	 */
	public void findShoulders(BasicIntersection intersection) {

		ArrayList<ArrayList<RoadVector>> shoulderBorders = 
				new ArrayList<ArrayList<RoadVector>>();
		int numOfDirections = Directions.values().length;
		ArrayList<RoadVector> shoulderBorder = new ArrayList<RoadVector>();
		ArrayList<RoadVector> temp;
		
		// for each turn path, finds the shoulder polygon
		for(Directions dir : Directions.values()){
			int code = dir.value();
			
			BasicLeg leg1 = intersection.getLegs().get(code);
			BasicLeg leg2 = intersection.getLegs().get((code+1)%numOfDirections);

			// gets the turn path
			temp = CopyUtil.copy(intersection.getCurves().get(code));
			
			// the first and last point in the turn path will be taken care
			// of in the leg.getLeft(Right)ShoulderBorder
			temp.remove(0);
			temp.remove(temp.size()-1);
			
			// to have the inner area to the left
			Collections.reverse(temp);
			
			shoulderBorder.clear();
			shoulderBorder.addAll(leg2.getLeftShoulderBorder());
			shoulderBorder.addAll(temp);
			shoulderBorder.addAll(leg1.getRightShoulderBorder());
			
			shoulderBorders.add(shoulderBorder);
			
			// use trianglator to get the triangle mesh
			if(intersection.getShoulderArea()==null){
				intersection.setShoulderArea(TriangulatorAdapter.triangulate(shoulderBorder));
			}else{
				intersection.getShoulderArea().addAll(TriangulatorAdapter.
						triangulate(shoulderBorder));
			}
		}
		
		intersection.setShoulderBorders(shoulderBorders);
		
	}

	/**
	 * Finds and triangulates the polygon, enclosing the inner area.
	 */
	public void findInnerArea(BasicIntersection intersection) {
		ArrayList<RoadVector> innerBorder = new ArrayList<RoadVector>();
		ArrayList<RoadVector> temp;

		// each direction consists of the border where the leg and intersection
		// meet and the turn path
		for(Directions dir : Directions.values()){
			int code = dir.value();
			
			innerBorder.addAll(intersection.getLegs().get(code).getBorder());
			temp = CopyUtil.copy(intersection.getCurves().get(code));
			temp.remove(0);
			temp.remove(temp.size()-1);
			innerBorder.addAll(temp);
		}
		
		intersection.setInnerArea(TriangulatorAdapter.triangulate(innerBorder));
		intersection.setInnerBorder(innerBorder);
	}

}
