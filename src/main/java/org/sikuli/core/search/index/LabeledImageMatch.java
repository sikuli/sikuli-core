package org.sikuli.core.search.index;

import org.sikuli.core.search.Match;

public class LabeledImageMatch extends Match {
	final private LabeledImage labeledImage;
	public LabeledImageMatch(LabeledImage image) {
		super();
		this.labeledImage = image;
	}
	public LabeledImage getLabeledImage(){
		return labeledImage;
	}
	
	public String toString(){
		return "[" + labeledImage.getLabel().getStringValue() + "]";
	}
}
