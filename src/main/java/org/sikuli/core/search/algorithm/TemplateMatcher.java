package org.sikuli.core.search.algorithm;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.search.RegionMatch;
import org.sikuli.core.search.TemplateMatchingUtilities;
import org.sikuli.core.search.TemplateMatchingUtilities.TemplateMatchResult;

import com.google.common.collect.Lists;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TemplateMatcher {
	
	public static List<RegionMatch> findMatchesByGrayscaleAtOriginalResolution(BufferedImage input, BufferedImage target, int limit, double minScore){
		IplImage input1 = ImagePreprocessor.createGrayscale(input);
		IplImage target1 = ImagePreprocessor.createGrayscale(target);
		IplImage resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(input1, target1);
		List<RegionMatch> result = fetchMatches(resultMatrix, target1, limit, minScore);
		input1.release();
		target1.release();
		resultMatrix.release();
		return result;
	}
	
	// Experimental
	public static List<RegionMatch> findMatchesByGrayscaleAtOriginalResolutionWithROIs(
			BufferedImage input, BufferedImage target, int limit, double minScore, List<Rectangle> rois){
		IplImage input1 = ImagePreprocessor.createGrayscale(input);
		IplImage target1 = ImagePreprocessor.createGrayscale(target);				
		IplImage resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrixWithMultipleROIs(input1, target1, rois);
		return fetchMatches(resultMatrix, target1, limit, minScore);
	}
	
	
	static private List<RegionMatch> fetchMatches(IplImage resultMatrix, IplImage target, int limit, double minScore){
		List<RegionMatch> matches = Lists.newArrayList();		
		while(matches.size() < limit){
			TemplateMatchResult result = TemplateMatchingUtilities.fetchNextBestMatch(resultMatrix, target);
			RegionMatch m = new RegionMatch(result);
			m.setScore(result.score);
			if (m.getScore() >= minScore){
				matches.add(m);
			}else{
				break;
			}
		}
		return matches;
	}

}
