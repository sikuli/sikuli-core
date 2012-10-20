package org.sikuli.core.search.index;

import java.awt.image.BufferedImage;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BufferedLabeledImage implements LabeledImage {

	public BufferedLabeledImage(BufferedImage image, ImageLabel imageLabel) {
		super();
		this.image = image;
		this.label = imageLabel;
	}

	final private BufferedImage image;
	private ImageLabel label;
	
	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public void setLabel(ImageLabel label) {
		this.label = label;	
	}

	@Override
	public ImageLabel getLabel() {
		return label;
	}

	
}