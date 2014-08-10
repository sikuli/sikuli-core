/*******************************************************************************
 * Copyright 2011 sikuli.org
 * Released under the MIT license.
 * 
 * Contributors:
 *     Tom Yeh - initial API and implementation
 ******************************************************************************/
package org.sikuli.core.cv;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class ImagePreprocessor {
	
	public static IplImage createLab(BufferedImage input){
		IplImage color = IplImage.createFrom(input);
		IplImage rgb = IplImage.create(cvGetSize(color), 8, 3);
		cvCvtColor(color,rgb,CV_BGRA2RGB);
		IplImage lab = IplImage.createCompatible(rgb);        
		cvCvtColor(rgb, lab, CV_RGB2Lab );
		return lab;
	}

	public static IplImage createHSV(BufferedImage input){
		IplImage color = IplImage.createFrom(input);
		IplImage rgb = IplImage.create(cvGetSize(color), 8, 3);
		cvCvtColor(color,rgb,CV_BGRA2RGB);
		IplImage hsv = IplImage.createCompatible(rgb);        
		cvCvtColor(rgb, hsv, CV_RGB2HSV );
		return hsv;
	}

	public static IplImage createGrayscale(BufferedImage input) {
		// covert to grayscale at Java level
		// something is not right at JavaCV if a ARGB image is given
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		BufferedImage gray1 = op.filter(input,null);
		return VisionUtils.createGrayImageFrom(IplImage.createFrom(gray1));		
	}
	
	public static IplImage createGrayscale(IplImage input) {
		return VisionUtils.createGrayImageFrom(input);		
	}
}
