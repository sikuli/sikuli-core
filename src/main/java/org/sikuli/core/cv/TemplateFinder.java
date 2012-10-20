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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;

public class TemplateFinder {

	static final float MIM_TARGET_DIMENSION  = 6.0f;
	static final float MIM_TARGET_DIMENSION_FINDALL  = 24.0f;
	static final float MIM_TARGET_DIMENSION_FINDALLSIMILAR  = 32.0f;


	static final float REMATCH_THRESHOLD  = 0.995f;

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	private IplImage createGrayScaleIplImage(BufferedImage colorImage){
		IplImage colorIplImage = IplImage.createFrom(colorImage);
		IplImage grayIplImage = IplImage.create(cvGetSize(colorIplImage), 8, 1);
		cvCvtColor(colorIplImage, grayIplImage, CV_RGB2GRAY);
		colorIplImage.release();
		return grayIplImage;
	}

	public FindResult findFirstGoodMatch(BufferedImage inputImage, BufferedImage targetImage){
		FindResult[] results = findSimilarMatches(inputImage, targetImage, 1, 0.995);
		if (results.length > 0){
			return results[0];
		}else{
			return null;
		}
	}

	public FindResult[] findSimilarMatches(BufferedImage inputImage, BufferedImage targetImage, int k, double minSimilarity){

		if (inputImage.getWidth() < targetImage.getWidth() ||
				inputImage.getHeight() < targetImage.getHeight())
			return new FindResult[0];

		minSimilarity = Math.min(0.999, minSimilarity);

		IplImage targetGray = createGrayScaleIplImage(targetImage);
		IplImage inputGray = createGrayScaleIplImage(inputImage);

		FindResult[] r =  findSimilarMatches(inputGray, targetGray, k, minSimilarity);

		inputGray.release();
		targetGray.release();      
		return r;
	}


	public FindResult[] findMatches(BufferedImage inputImage, BufferedImage targetImage, int k){

		if (inputImage.getWidth() < targetImage.getWidth() ||
				inputImage.getHeight() < targetImage.getHeight())
			return new FindResult[0];


		IplImage inputGray = createGrayScaleIplImage(inputImage);
		IplImage targetGray = createGrayScaleIplImage(targetImage);

		FindResult[] r = findMatches(inputGray, targetGray, k);

		inputGray.release();
		targetGray.release();      
		return r;
	}

	public FindResult findFirstMatch(BufferedImage inputImage, BufferedImage targetImage){

		if (inputImage.getWidth() < targetImage.getWidth() ||
				inputImage.getHeight() < targetImage.getHeight())
			return null;

		IplImage inputGray = createGrayScaleIplImage(inputImage);
		IplImage targetGray = createGrayScaleIplImage(targetImage);

		FindResult r = findFirstMatch1(inputGray, targetGray);

		inputGray.release();	
		targetGray.release();      
		return r;
	}

	private FindResult[] findSimilarMatches(IplImage input, IplImage target, int k, double minSimilarity){

		double factor = Math.min(target.height() * 1.0 / MIM_TARGET_DIMENSION_FINDALLSIMILAR, 
				target.width() * 1.0 / MIM_TARGET_DIMENSION_FINDALLSIMILAR);

		DownsampleTemplateFinder tm = new DownsampleTemplateFinder();

		//FindResult[] topResults = tm.findTopKMatches(input, target, factor, k);
		FindResult[] topResults = tm.findTopKMatches(input, target, factor, k);

		//         FindResult top = topResults[0];       
		//         if (top.score < REMATCH_THRESHOLD){
		//            topResults = tm.findTopKMatches(input, target, factor*0.25, k);
		//         }
		//
		//         top = topResults[0];       
		//         if (top.score < REMATCH_THRESHOLD){
		//            topResults = tm.findTopKMatches(input, target, 1.0f, k);
		//         }


		ArrayList<FindResult> filteredResults = new ArrayList<FindResult>();
		for (FindResult r : topResults){
			if (r.score >= minSimilarity){
				filteredResults.add(r);
			}
		}

		return filteredResults.toArray(new FindResult[0]);
	}

	private FindResult[] findMatches(IplImage input, IplImage target, int k){

		double factor = Math.min(target.height() * 1.0 / MIM_TARGET_DIMENSION_FINDALL, 
				target.width() * 1.0 / MIM_TARGET_DIMENSION_FINDALL);

		DownsampleTemplateFinder tm = new DownsampleTemplateFinder();

		FindResult[] topResults = tm.findTopKMatches(input, target, factor, k);

		FindResult top = topResults[0];

		if (top.score < REMATCH_THRESHOLD){
			topResults = tm.findTopKMatches(input, target, factor*0.75, k);
		}

		return topResults;
	}


	private FindResult findFirstMatch1(IplImage input, IplImage target){

		double factor = Math.min(target.height() * 1.0 / MIM_TARGET_DIMENSION, 
				target.width() * 1.0 / MIM_TARGET_DIMENSION);

		DownsampleTemplateFinder tm = new DownsampleTemplateFinder();

		FindResult top = tm.findTopMatch(input, target, factor);

		if (top.score < REMATCH_THRESHOLD){
			top = tm.findTopMatch(input, target, factor*0.75);
		}

		if (top.score < REMATCH_THRESHOLD){
			top = tm.findTopMatch(input, target, factor/2);
		}

		if (top.score < REMATCH_THRESHOLD){
			top = tm.findTopMatch(input, target, factor/4);
		}
		return top;
	}
}


class BaseTemplateFinder {


	//   static ArrayList<FindResult> findTopKMatches(BufferedImage screenImage, BufferedImage targetImage, double min_similarity){
	//      
	//      return null;
	//   }
	//
	//   static FindResult findTopMatchFast(BufferedImage screenImage, BufferedImage targetImage, double min_similarity){
	//      FindResult r = new FindResult();
	//      r.x = 10;
	//      r.y = 10;
	//      r.width = 10;
	//      r.height = 10;
	//      return r;
	//   }


	public static IplImage computeTemplateMatchResultMatrix(IplImage input, IplImage target){

		int iwidth,iheight;
		if (input.roi() != null){
			iwidth = input.roi().width() - target.width() + 1;
			iheight = input.roi().height() - target.height() + 1;      
		}else{
			iwidth = input.width() - target.width() + 1;
			iheight = input.height() - target.height() + 1;
		}

		IplImage map = IplImage.create(cvSize(iwidth,iheight), 32, 1);      
		opencv_imgproc.cvMatchTemplate(input, target, map, CV_TM_CCOEFF_NORMED);
		return map;
	}


	static FindResult findGroundTruthTopMatch(BufferedImage screenImage, BufferedImage targetImage, double min_similarity){

		IplImage inputColor = IplImage.createFrom(screenImage);            
		IplImage targetColor = IplImage.createFrom(targetImage);

		IplImage inputGray = IplImage.create(cvGetSize(inputColor), 8, 1);
		IplImage targetGray = IplImage.create(cvGetSize(targetColor), 8, 1);
		cvCvtColor(inputColor, inputGray, CV_RGB2GRAY);
		cvCvtColor(targetColor, targetGray, CV_RGB2GRAY);

		IplImage map = computeTemplateMatchResultMatrix(inputGray, targetGray);

		double min[] = new double[1];
		double max[] = new double[1];
		CvPoint minPoint = new CvPoint(2);
		CvPoint maxPoint = new CvPoint(2);

		opencv_core.cvMinMaxLoc(map, min, max, minPoint, maxPoint, null);

		cvReleaseImage(map);
		cvReleaseImage(inputColor);
		cvReleaseImage(targetColor);
		cvReleaseImage(inputGray);
		cvReleaseImage(targetColor);

		//      if (max[0] < min_similarity){
		//         return null;
		//         
		//      }else{

		FindResult result = new FindResult();
		result.x = maxPoint.x();
		result.y = maxPoint.y();
		result.width = targetImage.getWidth();
		result.height = targetImage.getHeight();
		result.score = max[0];

		return result;
		//  }
	}

}



class ExactColorFinder extends BaseTemplateFinder {
	FindResult findTopMatch(BufferedImage inputImage, BufferedImage targetImage){

		IplImage inputColor = IplImage.createFrom(inputImage);            
		IplImage targetColor = IplImage.createFrom(targetImage);
		//inputColor.

		System.out.println("channels = " + inputColor.nChannels());
		System.out.println("channels = " + targetColor.nChannels());

		IplImage inputGray = IplImage.create(cvGetSize(inputColor), 8, 3);
		IplImage targetGray = IplImage.create(cvGetSize(targetColor), 8, 3);

		//      cvSetImageCOI(inputColor,2);
		//      cvSetImageCOI(inputGray,1);      
		//      cvCopy(inputColor,inputGray);
		//
		//      cvSetImageCOI(inputColor,3);
		//      cvSetImageCOI(inputGray,2);      
		//      cvCopy(inputColor,inputGray);
		//      
		//      cvSetImageCOI(inputColor,4);
		//      cvSetImageCOI(inputGray,3);      

		cvCopy(inputColor,inputGray);

		cvCopy(targetColor,targetGray);

		//      cvSetImageCOI(targetColor,2);
		//      cvSetImageCOI(targetGray,1);      
		//      cvCopy(targetColor,targetGray);

		//      cvSetImageCOI(targetColor,3);
		//      cvSetImageCOI(targetGray,2);      
		//      cvCopy(targetColor,targetGray);
		//
		//      cvSetImageCOI(targetColor,4);
		//      cvSetImageCOI(targetGray,3);      
		//      cvCopy(targetColor,targetGray);



		cvSetImageCOI(inputColor,0);
		cvSetImageCOI(inputGray,0);      

		//      cvSetImageCOI(targetColor,0);
		//      cvSetImageCOI(targetGray,0);      

		//cvCopy(targetColor,targetGray);
		//cvCvtColor(inputColor, inputGray, CV_RGBA2RGB);
		//cvCvtColor(targetColor, targetGray, CV_RGBA2RGB);

		FindResult r = findTopMatch(inputGray, targetGray);

		//      cvReleaseImage(inputColor);
		//      cvReleaseImage(targetColor);
		//      cvReleaseImage(inputGray);
		//      cvReleaseImage(targetGray);

		return r;
	}

	FindResult findTopMatch(IplImage input, IplImage target){

		IplImage matchResultMatrix = computeTemplateMatchResultMatrix(input, target);
		MatchFetcher fetcher = new MatchFetcher(matchResultMatrix, target);      
		return fetcher.fetchNextMatch();
	}

}

class DownsampleTemplateFinder extends BaseTemplateFinder {

	static Logger logger = LoggerFactory.getLogger(DownsampleTemplateFinder.class);

	private CvSize getSmallerSize(IplImage image, double ratio){
		return cvSize((int)(1.0*image.width()/ratio), (int)(1.0*image.height()/ratio));      
	}

	IplImage createSmallerImage(IplImage image, double ratio){

		if (ratio == 1.0){
			return image;
		}else{
			CvSize smallerSize = getSmallerSize(image, ratio);
			IplImage smallerImage = IplImage.create(smallerSize, 8, 1);
			cvResize(image, smallerImage, CV_INTER_LINEAR);
			return smallerImage;
		}      
	}

	private ArrayList<FindResult> findCandidatesAtCoarseLevel(IplImage input, IplImage target, double factor, int n){
		IplImage coarseInput = createSmallerImage(input, factor);
		IplImage coarseTarget = createSmallerImage(target, factor);
		IplImage coarseMatchResultMatrix = computeTemplateMatchResultMatrix(coarseInput, coarseTarget);
		//
		//logger.debug("factor=" + factor);
		MatchFetcher coarseMatcher = new MatchFetcher(coarseMatchResultMatrix, coarseTarget);

		ArrayList<FindResult> candidateResults = new ArrayList<FindResult>();      
		for (int i=0;i<n;++i){
			FindResult m = coarseMatcher.fetchNextMatch();
			//logger.debug("coarse match: " + m);

			int x = (int) (1.0*m.x*factor);
			int y = (int) (1.0*m.y*factor);

			// compute the parameter to define the neighborhood rectangle
			int d = (int) (factor + 1);
			int x0 = Math.max(x-d,0);
			int y0 = Math.max(y-d,0);
			int x1 = Math.min(x+d+target.width(), input.width());
			int y1 = Math.min(y+d+target.height(), input.height());
			CvRect roi = cvRect(x0,y0,x1-x0,y1-y0);

			cvSetImageROI(input, roi);         
			IplImage fineMatchResultMatrix = computeTemplateMatchResultMatrix(input, target);
			cvResetImageROI(input);         

			MatchFetcher fineMatcher = new MatchFetcher(fineMatchResultMatrix, target);
			FindResult candiateMatch = fineMatcher.fetchNextMatch();
			candiateMatch.x += roi.x();
			candiateMatch.y += roi.y();
			//logger.debug("fine match: " + candiateMatch); 
			candidateResults.add(candiateMatch);         

			fineMatchResultMatrix.release();
		}

		Collections.sort(candidateResults, Collections.reverseOrder());
		//      for (FindResult c : candidateResults){
		//         logger.debug("score :" + c.score);
		//      }

		coarseMatchResultMatrix.release();
		coarseInput.release();
		coarseTarget.release();

		Collections.sort(candidateResults, Collections.reverseOrder());

		return candidateResults;
	}


	FindResult[] findTopKMatches(IplImage input, IplImage target, double factor, int k){
		ArrayList<FindResult> candidateResults = findCandidatesAtCoarseLevel(input, target, factor, 10+k);      
		FindResult[] topKCandidates = new FindResult[k];
		for (int i=0; i < k; ++i){         
			topKCandidates[i] = candidateResults.get(i);         
		}
		return topKCandidates;
	}


	// input must be grayscale images
	FindResult findTopMatch(IplImage input, IplImage target, double factor){
		ArrayList<FindResult> candidateResults;
		candidateResults = findCandidatesAtCoarseLevel(input, target, factor, 10);
		return candidateResults.get(0);
	}

}

class MatchFetcher {
	IplImage resultMatrix;
	IplImage target;

	MatchFetcher(IplImage resultMatrix, IplImage target){
		this.resultMatrix = resultMatrix;
		this.target = target;
	}

	FindResult fetchNextMatch(){
		double min[] = new double[1];
		double max[] = new double[1];
		CvPoint minPoint = new CvPoint(2);
		CvPoint maxPoint = new CvPoint(2);


		opencv_core.cvMinMaxLoc(resultMatrix, min, max, minPoint, maxPoint, null);

		double detectionScore = max[0];
		CvPoint detectionLoc = maxPoint;

		FindResult r = new FindResult();
		r.x = detectionLoc.x();
		r.y = detectionLoc.y();
		r.width = target.width();
		r.height = target.height();
		r.score = detectionScore;

		// Suppress returned match
		int xmargin = target.width()/3;
		int ymargin = target.height()/3;

		int x = detectionLoc.x();
		int y = detectionLoc.y();

		int x0 = Math.max(x-xmargin,0);
		int y0 = Math.max(y-ymargin,0);
		int x1 = Math.min(x+xmargin,resultMatrix.width());  // no need to blank right and bottom
		int y1 = Math.min(y+ymargin,resultMatrix.height());

		cvRectangle(resultMatrix, cvPoint(x0, y0), cvPoint(x1-1, y1-1), 
				cvRealScalar(0.0), CV_FILLED, 8,0);

		return r;
	}
}


