package org.sikuli.core.search;

import java.util.Comparator;

public class ScoredItem<T> {
	
	public ScoredItem(T item, double d) {
		this.score = d;
		this.item = item;
	}

	final private double score;
	final private T item;	
	
	public double getScore(){
		return score;
	}
	
	public T getItem(){
		return item;
	}
	
	public String toString(){
		return "[score = " + score + ": " + item + "]";
	}
	
	static public Comparator<ScoredItem> getComparator(){
		return new Comparator<ScoredItem>(){

			@Override
			public int compare(ScoredItem o1, ScoredItem o2) {
				if (o1.score < o2.score){
					return 1;
				}else if (o1.score > o2.score){
					return -1;
				}else{
					return 0;
				}
			}
		};
	}
}
