package com.sim.util;

import java.util.*;

public class CollectionUtil {
	
	/**
	 * Counts how many elements are stored in a map. If a specific
	 * element is also a collection, counts the elements recursively. 
	 * 
	 * @param map
	 * @return
	 */
	public static <K,V> int count(Map<K,V> map){
		Set<K> keySet = map.keySet();
		int num = 0;
		for(K key : keySet){
			V value = map.get(key);
			if(value instanceof Collection<?>){
				num+=count((Collection)value);
			}else{
				num++;
			}
		}
		return num;
	}
	
	/**
	 * Counts how many elements are stored in a collection. If a specific
	 * element is also a collection, counts the elements recursively. 
	 * 
	 * @param collection
	 * @return
	 */
	public static <T> int count(Collection<T> collection){
		int num = 0;
		Iterator<T> iterator = collection.iterator();
		while(iterator.hasNext()){
			Object obj = iterator.next();
			if(obj instanceof Collection){
				num += count((Collection<T>)obj);
			}else{
				num++;
			}
		}
		return num;
	}
	
	/**
	 * The first parameter is a map of array lists and keys are comparable. Considering
	 * the master list which is the union of all the array lists, this method returns
	 * the element given the index in the master list.
	 * 
	 * @param map
	 * @param index
	 * @return
	 */
	public static <V, K> V get(NavigableMap<K, ArrayList<V>> map, int index){
		K key = map.firstKey();
		int current = 0;
		while(key!=null){
			ArrayList<V> list = map.get(key);
			if(current+list.size()>index){
				return list.get(index-current);
			}else{
				current+=list.size();
				key = map.higherKey(key);
			}
		}
		return null;
	}
}
