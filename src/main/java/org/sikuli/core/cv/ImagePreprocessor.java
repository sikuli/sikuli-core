/*******************************************************************************
 * Copyright 2011 sikuli.org
 * Released under the MIT license.
 * 
 * Contributors:
 *     Tom Yeh - initial API and implementation
 ******************************************************************************/
package org.sikuli.core.cv;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGRA2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2Lab;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

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
