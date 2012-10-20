package org.sikuli.core.search.index;


public class ImageIndexWriter {
	
	final private ImageIndex index;

	public ImageIndexWriter(ImageIndex index, Config config){
		this.index = index;
	}	
	public void add(LabeledImage labeledImage){					
		index.add(labeledImage);
	}
	
}
 