package org.sikuli.core.cv;

import java.awt.image.BufferedImage;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

public class ImageMask {
	
	private ImageMask(){		
	}
	private IplImage mask;
	
	public void add(int x, int y, int width, int height){
		cvSetImageROI(mask, cvRect(x,y,width,height));
		cvSet(mask, cvScalarAll(255));
		cvResetImageROI(mask);
	}
	
	public void remove(int x, int y, int width, int height){
		cvSetImageROI(mask, cvRect(x,y,width,height));
		cvSet(mask, cvScalarAll(0));
		cvResetImageROI(mask);		
	}
	
	public static ImageMask create(int w, int h){
		ImageMask m = new ImageMask();
		m.mask = IplImage.create(cvSize(w, h), 8, 1);
		return m;		
	}
	
	public static ImageMask createFrom(IplImage image){
		return create(image.width(), image.height());
	}
	
	public static ImageMask createFrom(BufferedImage image){
		return create(image.getWidth(), image.getHeight());
	}

	public BufferedImage createMaskedImage(BufferedImage input){		
		IplImage image = IplImage.createFrom(input);
		IplImage maskedImage = IplImage.createCompatible(image);
		cvSet(maskedImage, cvScalarAll(0), null);
		cvCopy(image, maskedImage, mask);
		return maskedImage.getBufferedImage();
	}
}
