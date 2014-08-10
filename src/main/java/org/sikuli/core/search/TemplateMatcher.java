package org.sikuli.core.search;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.search.internal.TemplateMatchingUtilities;
import org.sikuli.core.search.internal.TemplateMatchingUtilities.TemplateMatchResult;

import com.google.common.collect.Lists;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;


public class TemplateMatcher {

	public static class Result {

		public int x;
		public int y;
		public int width;
		public int height;	
		private double score;	

		public Result(Rectangle r) {
			super();
			x = r.x;
			y = r.y;
			width = r.width;
			height = r.height;
		}

		public Rectangle getBounds() {
			return new Rectangle(x,y,width,height);
		}

		public int getX(){
			return x;
		}

		public int getY(){
			return y;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public Point getLocation() {
			return getBounds().getLocation();
		}
		
		public double getScore(){
			return score;
		}
		public void setScore(double score) {
			this.score = score;
		}
		
	}	

	public static List<Result> findMatchesByGrayscaleAtOriginalResolution(BufferedImage input, BufferedImage target, int limit, double minScore){
		IplImage input1 = ImagePreprocessor.createGrayscale(input);
		IplImage target1 = ImagePreprocessor.createGrayscale(target);
		IplImage resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(input1, target1);
		List<Result> result = fetchMatches(resultMatrix, target1, limit, minScore);
		input1.release();
		target1.release();
		resultMatrix.release();
		return result;
	}

	// Experimental
	public static List<Result> findMatchesByGrayscaleAtOriginalResolutionWithROIs(
			BufferedImage input, BufferedImage target, int limit, double minScore, List<Rectangle> rois){
		IplImage input1 = ImagePreprocessor.createGrayscale(input);
		IplImage target1 = ImagePreprocessor.createGrayscale(target);				
		IplImage resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrixWithMultipleROIs(input1, target1, rois);
		return fetchMatches(resultMatrix, target1, limit, minScore);
	}


	static private List<Result> fetchMatches(IplImage resultMatrix, IplImage target, int limit, double minScore){
		List<Result> matches = Lists.newArrayList();		
		while(matches.size() < limit){
			TemplateMatchResult result = TemplateMatchingUtilities.fetchNextBestMatch(resultMatrix, target);
			Result m = new Result(result);
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
