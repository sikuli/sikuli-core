package org.sikuli.core.search.index;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.cv.VisionUtils;
import org.sikuli.core.search.ScoredItem;
import org.sikuli.core.search.SearchAlgorithm;
import org.sikuli.core.search.SearchAlgorithmOutput;

import com.google.common.collect.Lists;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

public class ImageIndex {
	
	final private List<IndexRecord> records = Lists.newArrayList();
	void add(LabeledImage labeledImage){
		BufferedImage image = labeledImage.getImage();
		ImageFeature feature = FeatureComputer.compute(image);
		IndexRecord record = new IndexRecord(labeledImage, feature);
		records.add(record);
	}
	
	static class FeatureComputer {
		static public ImageFeature compute(BufferedImage image){
			ImageFeature fs = new ImageFeature();
			fs.rawImage = IplImage.createFrom(image);
			fs.foregroundHeight = image.getHeight();
			fs.foregroundWidth = image.getWidth();					
			fs.numberOfForegroundPixels = cvCountNonZero(VisionUtils.computeForegroundMaskOf(ImagePreprocessor.createGrayscale(image)));
			return fs;
		}
	}

	
	interface NormalizedSimilairty {
		double compute(ImageFeature f1, ImageFeature f2);
	}
		
	
	static NormalizedSimilairty templateMatchSimilarity = new NormalizedSimilairty() {
		@Override
		public double compute(ImageFeature f1, ImageFeature f2) {
			IplImage im1 = f1.rawImage;
			IplImage im2 = f2.rawImage;
			
			int minW = Math.min(im1.width(), im2.width());
			int minH = Math.min(im1.height(), im2.height());
			
			CvRect roi1 = cvRect(im1.width()/2 - minW/2, im1.height()/2 - minH/2, minW, minH);			
			cvSetImageROI(im1, roi1);			
			double score1 = VisionUtils.computeBestTemplateMatchScore(im2, im1);
			cvResetImageROI(im1);
			
			CvRect roi2 = cvRect(im2.width()/2 - minW/2, im2.height()/2 - minH/2, minW, minH);
			cvSetImageROI(im2, roi2);
			//System.out.println("roi2:" + roi2.x() + " " + roi2.y() + " " + roi2.width() + " " + roi2.height());
			double score2 = VisionUtils.computeBestTemplateMatchScore(im1, im2);
			cvResetImageROI(im2);

			return (score1 + score2)/2;
		}
	}; 
	
	static NormalizedSimilairty foregroundAreaSimilarity = new NormalizedSimilairty() {
		@Override
		public double compute(ImageFeature f1, ImageFeature f2) {
			return 1f - 1.0*Math.abs(f1.numberOfForegroundPixels -  f2.numberOfForegroundPixels) / Math.max(f1.numberOfForegroundPixels, f2.numberOfForegroundPixels);
		}
	};
	static NormalizedSimilairty heightSimilarity = new NormalizedSimilairty() {
		@Override
		public double compute(ImageFeature f1, ImageFeature f2) {
			return 1f - 1.0*Math.abs(f1.foregroundHeight -  f2.foregroundHeight) / Math.max(f1.foregroundHeight, f2.foregroundHeight);
		}		
	};	
	static NormalizedSimilairty widthSimilarity = new NormalizedSimilairty() {
		@Override
		public double compute(ImageFeature f1, ImageFeature f2) {
			return 1f - 1.0*Math.abs(f1.foregroundWidth -  f2.foregroundWidth) / Math.max(f1.foregroundWidth, f2.foregroundWidth);
		}		
	};
	static NormalizedSimilairty sizeSimilarityComputer = new NormalizedSimilairty() {
		@Override
		public double compute(ImageFeature f1, ImageFeature f2) {
			double s1 = widthSimilarity.compute(f1, f2);
			double s2 = heightSimilarity.compute(f1, f2);
			return (s1 + s2) / 2;
		}		
	};
	
	
	static  NormalizedSimilairty totalSimilarity = new NormalizedSimilairty() {
		
		@Override
		public double compute(ImageFeature f1,
				ImageFeature f2) {
			
			double s1 = sizeSimilarityComputer.compute(f1, f2);
			double s2 = foregroundAreaSimilarity.compute(f1, f2);
			double s3 = templateMatchSimilarity.compute(f1, f2);
			return (s1 + s2 + s3) / 3;
		}		
	};
	
	static class IndexRecord {		
		
		final private ImageFeature imageFeature;
		final private LabeledImage labeledImage;

		public IndexRecord(LabeledImage labeledImage, ImageFeature imageFeature) {
			super();
			this.labeledImage = labeledImage;
			this.imageFeature = imageFeature;
		}
		
		public ImageFeature getImageFeature() {
			return imageFeature;
		}

		public LabeledImage getLabeledImage() {
			return labeledImage;
		}		
	}
	
	static class ImageFeature {		
		int numberOfForegroundPixels;
		int foregroundWidth;
		int foregroundHeight;		
		IplImage rawImage;		
	}
	
	class ExhausiveLinearScanSearchAlgorithm implements SearchAlgorithm<LabeledImageMatch>{

		//final private Logger logger = Logger.getLogger(ExhausiveLinearScanSearchAlgorithm.class);
		final private BufferedImage queryImage;
		final private NormalizedSimilairty normSimilarityComputer;
		private List<ScoredItem<IndexRecord>> scoredRecords = Lists.newArrayList();
		private Iterator<ScoredItem<IndexRecord>> iterator = null;
		
		public ExhausiveLinearScanSearchAlgorithm(BufferedImage queryImage,
				NormalizedSimilairty normSimilarityComputer) {
			super();
			this.queryImage = queryImage;
			this.normSimilarityComputer = normSimilarityComputer;
		}

		@Override
		public void execute() {
			ImageFeature queryFeature = FeatureComputer.compute(queryImage);				
			for (IndexRecord record : records){				
				ImageFeature dataFeature = record.getImageFeature();							
				double score = normSimilarityComputer.compute(queryFeature, dataFeature);
				//logger.debug("label: " + record.getLabeledImage().getLabel() + ", score: =" + score);
				ScoredItem<IndexRecord> scoredItem = new ScoredItem<IndexRecord>(record, (float) score);
				scoredRecords.add(scoredItem);				
			}			
			Collections.sort(scoredRecords, ScoredItem.getComparator());
			iterator = scoredRecords.iterator();
		}

		@Override
		public LabeledImageMatch fetchNext() {
			if (iterator.hasNext()){				
				ScoredItem<IndexRecord> scoredItem = iterator.next();
				LabeledImageMatch imageDocumentMatch = new LabeledImageMatch(scoredItem.getItem().getLabeledImage());
				//LabeledImageMatch out = new SearchAlgorithmOutput<LabeledImageMatch>(imageDocumentMatch, scoredItem.getScore());
				return imageDocumentMatch;			
			}else{
				return null;
			}
		}
		
	}
	
	public SearchAlgorithm<LabeledImageMatch> getAlgorithm(BufferedImage queryImage){		
		return new ExhausiveLinearScanSearchAlgorithm(queryImage, totalSimilarity);
	}
}