package org.sikuli.core.search.algorithm;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.cv.VisionUtils;
import org.sikuli.core.search.RegionMatch;
import org.sikuli.core.search.SearchAlgorithm;
import org.sikuli.core.search.TemplateMatchingUtilities;
import org.sikuli.core.search.TemplateMatchingUtilities.TemplateMatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

public class SearchByTextureAndColorAtOriginalResolution implements SearchAlgorithm<RegionMatch>{

	
	static private Logger logger = LoggerFactory.getLogger(SearchByTextureAndColorAtOriginalResolution.class);
	
	private IplImage query;
	private IplImage input;		

	private CvScalar avergerColorOfTheQueryImage;
	private IplImage resultMatrix = null;

	public SearchByTextureAndColorAtOriginalResolution(IplImage input, IplImage query){
		this.input = input;
		this.query = query;
	}

	@Override
	public void execute(){
		this.resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(
				ImagePreprocessor.createGrayscale(input), 
				ImagePreprocessor.createGrayscale(query));
		// compute average color of the query image		
		logger.trace("channels:" + query.nChannels());
		logger.trace("alpha channel:" + query.alphaChannel());
		logger.trace("bt:" + query.getBufferedImageType());
		if (query.nChannels()==4){
			// When the input image has an alpha channel, the color space is abgr 
			// and must be converted to bgr in order to compare its color to captured screen data
			query = VisionUtils.cloneWithoutAlphaChannel(query); 
		}
		if (input.nChannels()==4){
			// When the input image has an alpha channel, the color space is abgr 
			// and must be converted to bgr in order to compare its color to captured screen data
			input = VisionUtils.cloneWithoutAlphaChannel(input); 
		}
		CvScalar avg = cvAvg(query, null);
		this.avergerColorOfTheQueryImage = avg;
	}


	static private double calculateL1Distance(CvScalar a, CvScalar b){
		////System.out.println();	
		logger.trace(toString(a) + "<->" + toString(b));
		double d = 0;
		for (int i = 0; i < 3; i++){
			d += Math.abs(a.getVal(i) - b.getVal(i));
		}
		return d;
	}

	static private String toString(CvScalar a){
		String s = "[";
		for (int i = 0; i < 4; i++){
			s = s + String.format("%3.0f ",a.getVal(i));
		}
		s = s + "]";
		return s;
	}
	
	// return the raw distance score 
	private double calculateColorDifferenceBetweenMatchedRegionAndTarget(Rectangle m){
		// compute the average color of the found match
		cvSetImageROI(input, cvRect(m.x,m.y,m.width,m.height));			
		CvScalar averageColorOfTheMatchedRegion = cvAvg(input, null);			
		double diff = calculateL1Distance(avergerColorOfTheQueryImage, averageColorOfTheMatchedRegion);
		cvResetImageROI(input);						
		return diff;
	}
	
	// return a score between 0 and 1
	private double calculateColorMatchScore(Rectangle r){
		double rawScore =  calculateColorDifferenceBetweenMatchedRegionAndTarget(r);
		return (255 - Math.min(rawScore,255))/(255);
	}
	
	static class ColorRegionMatch extends RegionMatch{
		double colorScore;
		double textureScore;

		public ColorRegionMatch(Rectangle r){
			super(r.getBounds());
		}

		public double getScore(){
			double s1 = textureScore;
			double s2 = colorScore;
//			double s = (s1+s2)/2;
			double s;
			if (s2 > 0.85){
				s = s1;
			}else{
				s = 0;
			}
//			double s = Math.min(s1, s2);
			return s;
		}
		
		public String toString(){
			return " x = " + x + ", y = " + y + ", textureScore = " + String.format("%1.3f", textureScore)
					+ ", colorScore = " + String.format("%1.3f", colorScore);

		}
	}


	private ColorRegionMatch fetchedMatch = null; 
	private LinkedList<ColorRegionMatch> prefetchedCandidates = new LinkedList<ColorRegionMatch>();
	private int MAX_NUMBER_TO_PREFETCH = 10;
	
	
	private ColorRegionMatch fetchNextColorRegionMath(){
		TemplateMatchResult result = TemplateMatchingUtilities.fetchNextBestMatch(resultMatrix, query);
		
		ColorRegionMatch newMatch = new ColorRegionMatch(result.getBounds());
		newMatch.colorScore = calculateColorMatchScore(result.getBounds());			
		newMatch.textureScore = result.score;
		return newMatch;
	}
	
	private void prefetch(){
		
		// if there's a previously fetched match that was not added to the list
		if (fetchedMatch != null){
			prefetchedCandidates.add(fetchedMatch);
			fetchedMatch = null;
		}
		
		while (true){

			// get previous match
			ColorRegionMatch previousMatch;
			if (prefetchedCandidates.isEmpty()){
				previousMatch = null;
			}else{
				previousMatch = prefetchedCandidates.getLast();
			}

			// get new match
			ColorRegionMatch newMatch = fetchNextColorRegionMath();
			logger.trace("prefecth (" + prefetchedCandidates.size() + ")" + newMatch);
 
			double dropInTextureSimilarity = previousMatch == null ? 0 : 
					(1.0 - newMatch.textureScore / previousMatch.textureScore);
			
			boolean isDropInTextureSimilaritySingificant =  dropInTextureSimilarity > 0.15;
								
			//System.out.println("Drop:" + String.format("%1.2f",dropInTextureSimilarity));
			
			
			if (isDropInTextureSimilaritySingificant){		
				previousMatch = newMatch;
				break;
			}else{
				prefetchedCandidates.add(newMatch);					
			}

			
			boolean hasPrefetchedEnough = prefetchedCandidates.size() >= MAX_NUMBER_TO_PREFETCH;
			if (hasPrefetchedEnough){
				break;
			}
		}
		
		Collections.sort(prefetchedCandidates, new Comparator<ColorRegionMatch>(){
			@Override
			public int compare(ColorRegionMatch a, ColorRegionMatch b) {
				return Doubles.compare(b.getScore(),a.getScore());
			}				
		});
	}

	@Override
	public RegionMatch fetchNext(){

		if (prefetchedCandidates.isEmpty()){
			prefetch();
		}

		ColorRegionMatch colorRegionMatch = prefetchedCandidates.poll();			
		return colorRegionMatch;
	}
}