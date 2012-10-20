package org.sikuli.core.search.index;

import java.awt.image.BufferedImage;

public interface LabeledImage {
	public BufferedImage getImage();
	public void setLabel(ImageLabel label);
	public ImageLabel getLabel();
}