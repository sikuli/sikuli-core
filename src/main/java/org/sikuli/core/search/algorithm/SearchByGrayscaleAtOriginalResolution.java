package org.sikuli.core.search.algorithm;

import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.search.Match;
import org.sikuli.core.search.RegionMatch;
import org.sikuli.core.search.SearchAlgorithm;
import org.sikuli.core.search.TemplateMatchingUtilities;
import org.sikuli.core.search.TemplateMatchingUtilities.TemplateMatchResult;

import com.google.common.collect.Lists;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

abstract class GrayscaleTemplateMatchAlgorithm {	
	abstract public void execute();
	abstract public RegionMatch fetchNext();

	IplImage query;
	IplImage input;

	public GrayscaleTemplateMatchAlgorithm(IplImage input, IplImage query){
		this.input = input;
		this.query = query;
	}

	public GrayscaleTemplateMatchAlgorithm(BufferedImage input, BufferedImage query){
		this.input = ImagePreprocessor.createGrayscale(input);
		this.query = ImagePreprocessor.createGrayscale(query);
	}

}

public class SearchByGrayscaleAtOriginalResolution extends  GrayscaleTemplateMatchAlgorithm{

	public SearchByGrayscaleAtOriginalResolution(BufferedImage input, BufferedImage query) {
		super(input, query);
	}

	IplImage resultMatrix = null;


	//		public SearchByGrayscaleAtOriginalResolution(BufferedImage input, BufferedImage query){
	//			this.input = ImagePreprocessor.createGrayscale(input);
	//			this.query = ImagePreprocessor.createGrayscale(query);
	//		}


	//		static public SearchByGrayscaleAtOriginalResolution create(BufferedImage input, BufferedImage query){
	//			return new SearchByGrayscaleAtOriginalResolution(IplImage.createFrom(input),IplImage.createFrom(query));
	//		}

	@Override
	public void execute(){
		this.resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(input, query);	
	}

	@Override
	public RegionMatch fetchNext(){
		final TemplateMatchResult result = TemplateMatchingUtilities.fetchNextBestMatch(resultMatrix, query);
		RegionMatch m = new RegionMatch(result);
		m.setScore(result.score);
		return m;
	}

	public List<RegionMatch> fetchAll(int limit, double minScore) {
		final List<RegionMatch> matches = Lists.newArrayList();		
		while(matches.size() < limit){
			RegionMatch m = fetchNext();
			if (m.getScore() >= minScore){
				matches.add(m);
			}else{
				break;
			}
		}
		return matches;			
	}
}