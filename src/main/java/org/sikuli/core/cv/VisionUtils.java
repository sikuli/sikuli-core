/*******************************************************************************
 * Copyright 2011 sikuli.org
 * Released under the MIT license.
 * 
 * Contributors:
 *     Tom Yeh - initial API and implementation
 ******************************************************************************/
package org.sikuli.core.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.sikuli.core.draw.BlobPainter;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.logging.ImageExplainer.Level;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;


public class VisionUtils {
	 
	
	static public <T> T[] mapOverCols(IplImage input, Class<T> c, Function<CvArr, T> op){
		CvRect roi = cvGetImageROI(input);
		@SuppressWarnings("unchecked")
		T[] vals = (T[]) Array.newInstance(c,roi.width());

		for (int i = roi.x(); i < roi.x() + roi.width(); ++i){
			CvRect colROI = cvRect(i,roi.y(),1, roi.height());
			cvSetImageROI(input, colROI);
			T val = op.apply(input);
			vals[i-roi.x()] = val;
			cvResetImageROI(input);				
		}	
		cvSetImageROI(input, roi);
		return vals;
	}
	
	static public <T> T[] mapOverRows(IplImage input, Class<T> c, Function<CvArr, T> op){

		CvRect roi = cvGetImageROI(input);
		@SuppressWarnings("unchecked")
		T[] vals = (T[]) Array.newInstance(c,roi.height());

		for (int i = roi.y(); i < roi.y() + roi.height(); ++i){
			CvRect rowROI = cvRect(roi.x(),i,roi.width(),1); 
			cvSetImageROI(input, rowROI);
			T val = op.apply(input);
			vals[i-roi.y()] = val;
			cvResetImageROI(input);					
		}	
		cvSetImageROI(input, roi);
		return vals;
	}

	
	// If the input image is blank, namely, the entire image looks like margin,
	// the returned image is an zero size image (both width and height are zero).
	static public BufferedImage getSubimageInsideMargin(BufferedImage image){
		Rectangle r = MarginFinderOld.getRegionInsideMargin(image);
		return image.getSubimage(r.x, r.y, r.width, r.height);
	}
	
	static public Rectangle getRegionInsideMargin(BufferedImage image){
		return MarginFinderOld.getRegionInsideMargin(image);
	}

	
	static public BufferedImage createComponentImage(Component component) {
		Dimension size = component.getSize();
		BufferedImage image = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		component.paint(g2);
		g2.dispose();
		return image;
	}
	
	static public BufferedImage captureScreen(Rectangle rect){
		try {
			return (new Robot()).createScreenCapture(rect);
		} catch (AWTException e) {
		}
		return null;
	}
			
	static public List<CvRect> detectBlobs(IplImage input){
		IplImage clone = input.clone();
		CvMemStorage storage = CvMemStorage.create();
		CvSeq contour = new CvSeq(null);
		cvFindContours(clone, storage, contour, Loader.sizeof(CvContour.class),
				CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

		//vlog(crossMask);

		List<CvRect> rects = Lists.newArrayList(); 

		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				CvRect boundingRect = cvBoundingRect(contour,0);
				rects.add(boundingRect);
			}
			contour = contour.h_next();
		}
		return rects;
	}
	
	static public BufferedImage createImageFrom(Component component){
		Dimension size = component.getPreferredSize();
		if (size.width == 0 || size.height == 0){
			size = component.getSize();
		}
		BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();     
		component.paint(g2);
		g2.dispose();
		return image;
	}
	
	static public void negate(IplImage src, IplImage dest){
		cvSubRS(src, cvScalarAll(255), dest, null);
	}
	
	static public IplImage createGrayImageFrom(IplImage input){		
		//System.out.println("nChannels:" + input.nChannels());
		
		if (input.nChannels() == 3){
			IplImage gray = IplImage.create(cvGetSize(input), 8, 1);
			cvCvtColor(input, gray, CV_BGR2GRAY);
			return gray;
		}else if (input.nChannels() == 4){
			IplImage gray = IplImage.create(cvGetSize(input), 8, 1);
			cvSetImageCOI(input,3);
			cvCopy(input, gray);//cvCvtColor(input, gray, CV_BGR2GRAY);
			cvSetImageCOI(input,0);
			return gray;		
		}else if (input.nChannels() == 2){		
			IplImage gray = IplImage.create(cvGetSize(input), 8, 1);
			IplImage alpha = IplImage.create(cvGetSize(input), 8, 1);
			IplImage white = IplImage.create(cvGetSize(input), 8, 1);
			cvSet(white, CvScalar.WHITE);
			cvSetImageCOI(input,1);
			cvCopy(input, gray);			
			cvSetImageCOI(input,2);
			cvCopy(input, alpha);
			cvCopy(gray,white,alpha);
			return white;
		}else {
			return input;
		}
	}

	static public IplImage computeForegroundMaskOf(IplImage inputImage){

		IplImage grayImage = createGrayImageFrom(inputImage);
		IplImage foregroundMask = IplImage.create(cvGetSize(grayImage), 8, 1);

		cvCanny(grayImage,foregroundMask,0.66*50,1.33*50,3);  

		//cvAdaptiveThreshold(foregroundMask,foregroundMask,255,CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY_INV, 5, 1);

		IplConvKernel kernel = IplConvKernel.create(3,3,1,1,CV_SHAPE_RECT,null);
		cvDilate(foregroundMask,foregroundMask,kernel,2);
		//cvErode(foregroundMask,foregroundMask,kernel,2);
		kernel.release();
		return foregroundMask;
	}
	
	static public IplImage computeEdges(IplImage inputImage){
		IplImage grayImage = createGrayImageFrom(inputImage);
		IplImage foregroundMask = IplImage.create(cvGetSize(grayImage), 8, 1);
		cvCanny(grayImage,foregroundMask,0.66*50,1.33*50,3);  
		//cvAdaptiveThreshold(foregroundMask,foregroundMask,255,CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY_INV, 5, 1);
		return foregroundMask;
	}

	static public IplImage getForeground(IplImage inputImage){
		IplImage grayImage = createGrayImageFrom(inputImage);
		IplImage foregroundMask = IplImage.create(cvGetSize(grayImage), 8, 1);
		IplImage foreground = IplImage.create(cvGetSize(inputImage), 8, 3);
		
		cvCanny(grayImage,foregroundMask,0.66*50,1.33*50,3);
		IplConvKernel kernel = IplConvKernel.create(3,3,1,1,CV_SHAPE_RECT,null);
		cvDilate(foregroundMask,foregroundMask,kernel,1);
		//cvErode(foregroundMask,foregroundMask,kernel,1);
		kernel.release();

		cvSet(foreground, cvScalarAll(255));
		cvCopy(inputImage, foreground, foregroundMask);
		  
		//cvAdaptiveThreshold(foregroundMask,foregroundMask,255,CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY_INV, 5, 1);
		return foreground;
	}

	
	
	static public double computeBestTemplateMatchScore(IplImage input, IplImage template){
		TemplateMatchHelper th = new TemplateMatchHelper(CV_TM_CCOEFF_NORMED);		
//		cvSetImageROI(im1, roi);
		th.match(input, template);
		FindResult fetchResult = th.fetchResult();
		double score = fetchResult.score;
//		cvResetImageROI(im1);
		return score;
	}
	
	static public IplImage computeWeighingMask(int w, int h){
		IplImage mask = IplImage.create(cvSize(w,h), 8, 1);
		cvSet(mask, cvScalarAll(255), null);
		//cvSet1D(mask, )


		cvSet2D(mask, w/2, h/2, cvScalarAll(0));
		
		IplImage dist = IplImage.create(cvSize(w,h), 32, 1);		
		cvDistTransform(mask, dist, CV_DIST_L1, 3, null, null, 0);
		
		IplImage out = IplImage.createCompatible(mask);
		cvConvertScaleAbs(dist, out, -1, 0);
		
		
		cvSubRS(out, cvScalarAll(255), out, null);
		
		try {
			ImageIO.write(out.getBufferedImage(), "png", new File("dist.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mask;
	}

	
	static public BufferedImage paintBlobsOnImage(BufferedImage image, List<CvRect> blobs){
		return (new BlobPainter(image,blobs)).render();
	}
	
	

		

	
	static public void extractCharacterBlocks(BufferedImage image){
	
		TextMap e = new TextMap();
		e.init(image);
	}
	
	public static IplImage cloneWithoutAlphaChannel(IplImage bgra){
		
		IplImage bgr = IplImage.create(bgra.width(), bgra.height(), 8, 3);
		IplImage alpha = IplImage.create(bgra.width(), bgra.height(), 8, 1);
		
		//cvSet(rgba, cvScalar(1,2,3,4));

		IplImage[] in = {bgra};
		IplImage[] out = {bgr, alpha}; 
		int from_to[] = { 0,3,  1,0,  2,1,  3,2 };
		cvMixChannels(in, 1, out, 2, from_to, 4);
		
		return bgr;
	}
	
	static ImageExplainer explainer = ImageExplainer.getExplainer(VisionUtils.class);
	
	public static void main(String[] args) throws IOException {
		//BasicConfigurator.configure();
		ImageExplainer.getExplainer(TextMap.class).setLevel(Level.ALL);
		

		BufferedImage image = ImageIO.read(new File("screen.png"));
		//extractCharacterBlocks(image);
		
		TextMap m = TextMap.createFrom(image);
		m.computeTextScore(10, 10, 200 ,200);
	}
	
}
