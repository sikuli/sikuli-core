package org.sikuli.core.search;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.logging.ImageExplainer.Level;

import com.google.common.collect.Lists;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TemplateMatchUtilitiesTest {
	
	static {
		ImageExplainer.getExplainer(TemplateMatchingUtilities.class).setLevel(Level.ALL);
	}
	
	@Test
	public void testMathWithMultipleROIs() throws IOException{
		
		BufferedImage input = ImageIO.read(getClass().getResource("/xpfolders/screen.png"));
		BufferedImage target = ImageIO.read(getClass().getResource("/xpfolders/targetCPP_301_199_49_198_55_334_302_335_306_475.png"));
		IplImage inputIplImage = ImagePreprocessor.createGrayscale(input);
		IplImage targetIplImage = ImagePreprocessor.createGrayscale(target);
		
		List<Rectangle> rois = Lists.newArrayList();
		rois.add(new Rectangle(240,140,150,300));
		rois.add(new Rectangle(0,0,50,300));
		rois.add(new Rectangle(0,150,200,200));
		TemplateMatchingUtilities.computeTemplateMatchResultMatrixWithMultipleROIs_GrouthTruth(inputIplImage, targetIplImage, rois);
		TemplateMatchingUtilities.computeTemplateMatchResultMatrixWithMultipleROIs(inputIplImage, targetIplImage, rois);
		
		
	}

}
