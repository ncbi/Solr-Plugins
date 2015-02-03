/**
 * 
 */
package com.agi.pubsolr.analysis;

import java.util.List;

/**
 * @author Rafis
 *
 */
public class SplitterBoundReceiver {
	private List<Integer> list;
	
	public SplitterBoundReceiver(List<Integer> list) {
		this.list = list;
	}
	
	public void setList(List<Integer> list) {
		this.list = list;
	}

	public void addBound(int bound) {
		list.add(bound);
	}
	
	public void shift(int offset) {
		for (int i = 0, n = list.size(); i < n; i++) {
			list.set(i, list.get(i) + offset);
		}
	}
}
