package org.sikuli.core.cv;

public class BooleanArraySegment {		
	public boolean value;
	public int position;
	public int size;
	
	public BooleanArraySegment(){		
	}
	
	public BooleanArraySegment(BooleanArraySegment seg) {
		this.value = seg.value;
		this.position = seg.position;
		this.size = seg.size;
	}

	public String toString(){
		return "(" + position + "->" + (position + size - 1) + ": " + size + ") ";
	}
}