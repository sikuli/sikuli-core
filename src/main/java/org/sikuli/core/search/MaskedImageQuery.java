package org.sikuli.core.search;

import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.logging.ImageExplainer;

import com.google.common.collect.Lists;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

public class MaskedImageQuery extends ImageQuery {

	final static private ImageExplainer logger = ImageExplainer.getExplainer(MaskedImageQuery.class);
	final private List<BufferedImage> subimagesToIgnore = Lists.newArrayList();

	public MaskedImageQuery(BufferedImage queryImage) {
		super(queryImage);
	}
	
	public void ignore(BufferedImage subimageToIgnore){
		subimagesToIgnore.add(subimageToIgnore);
	}
	
	
	public SearchAlgorithm<RegionMatch> createSearchAlgorithm(IplImage inputImage){
		IplImage queryGray = ImagePreprocessor.createGrayscale(getImage());
		return new SearchAlgorithmFactory.SearchWithIgnoreMask(inputImage, queryGray, createMask());	
	}
	
	IplImage createMask(){
	
		BufferedImage queryImage = getImage();
		
		IplImage ignoreMask = IplImage.create(cvSize(queryImage.getWidth(),queryImage.getHeight()), 8, 1);
		cvSet(ignoreMask, cvScalarAll(0));		

		for (BufferedImage subimage : subimagesToIgnore){
			ImageSearcher searcher = new ImageSearcher(queryImage);
			ImageQuery query = new ImageQuery(subimage);
			List<RegionMatch> topMatches = searcher.search(query, null, 1);	
			final RegionMatch m = topMatches.get(0);			

			cvRectangle(ignoreMask, cvPoint(m.getX(),m.getY()), cvPoint(m.getX()+m.getWidth(),m.getY()+m.getHeight()), 
					cvScalarAll(255), CV_FILLED, 8,0);
		}
		
		logger.step(ignoreMask, "ignore mask");
		
		return ignoreMask;
	}
}