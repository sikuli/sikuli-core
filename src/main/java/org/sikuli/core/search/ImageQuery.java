package org.sikuli.core.search;

import java.awt.image.BufferedImage;

import org.sikuli.core.cv.ImagePreprocessor;


public class ImageQuery extends Query {
	final private BufferedImage image; 
	public ImageQuery(){
		this.image = null;
	}
	
	public ImageQuery(BufferedImage image) {
		super();
		this.image = image;
	}
	public BufferedImage getImage(){
		return image;
	}
	public int getWidth(){
		return image.getWidth();
	}
	public int getHeight(){
		return image.getHeight();
	}
}