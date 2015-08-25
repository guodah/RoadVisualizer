package com.sim.util;


import java.util.*;

import com.sim.geometries.*;


public class CopyUtil {
	/**
	 * Copies from an original list given two indices inclusive to a new list. 
	 *  
	 * @param list 
	 * @param start
	 * @param end
	 * @return null if list is null; an empty when the parameters are invalid
	 */
	public static <T> ArrayList<T> copy(ArrayList<T> list, int start, int end){
		ArrayList<T> newList = new ArrayList<T>();
		
		if(list==null){
			return null;
		}
		
		if(start>end || start>=list.size() || end>=list.size()){
			return newList;
		}
		
		for(int i=start;i<=end;i++){
			newList.add(list.get(i));
		}
		return newList;
	}
	
	/**
	 * Re-insert the head element at the front of the list
	 * @param list
	 */
	public static <T> void duplicateHead(ArrayList<T> list){
		if(list==null || list.size()==0){
			return;
		}
		list.add(0, list.get(0));
	}
	
	/**
	 * Re-insert the tail element at the end of the list
	 * @param list
	 */
	public static <T> void duplicateTail(ArrayList<T> list){
		if(list==null || list.size()==0){
			return;
		}
		list.add(list.get(list.size()-1));
	}

	/**
	 * Duplicates the entire array list. (shallow copy)
	 * @param arrayList
	 * @return
	 */
	public static <T> ArrayList<T> copy(ArrayList<T> arrayList) {
		return copy(arrayList, 0, arrayList.size()-1);
	}
}
