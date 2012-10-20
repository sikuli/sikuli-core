package org.sikuli.core.search.index;

public class IntegerImageLabel implements ImageLabel {

	final private int i;
	
	public IntegerImageLabel(int i) {
		super();
		this.i = i;
	}

	@Override
	public String getName() {
		return "Integer";
	}

	@Override
	public String getStringValue() {
		return ""+i;
	}
	
	public String toString(){
		return ""+i;
	}
		
}