package org.sikuli.core.search;

import static com.googlecode.javacv.cpp.opencv_core.*;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import org.sikuli.core.cv.VisionUtils;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.search.TemplateMatchingUtilities.TemplateMatchResult;

import com.google.common.collect.Lists;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;

public class SearchAlgorithmFactory {

	static private ImageExplainer logger = ImageExplainer.getExplainer(SearchAlgorithmFactory.class);

	static class SearchWithIgnoreMask implements SearchAlgorithm<RegionMatch> {			

		private static final int NUM_LOCAL_PATCHES = 4;
		private static final int NUM_VOTES_PER_PATCH = 3;
		private static final int VOTE_INCREMENT = 50;

		// This field will be set by execute() to be returned by fetchNext()
		private RegionMatch bestMatch;

		final private IplImage ignoreMask;
		final private IplImage inputGray;
		final private IplImage queryGray;
		public SearchWithIgnoreMask(IplImage inputGray, IplImage queryGray, IplImage ignoreMask){
			this.inputGray = inputGray;
			this.queryGray = queryGray;
			this.ignoreMask = ignoreMask;
		}

		@Override
		public void execute(){

			List<Rectangle> sampleRectangles = sampleSalientLocalRectangles(queryGray, ignoreMask);			
			IplImage voteMatrix = vote(inputGray, queryGray, sampleRectangles);

			final double min[] = new double[1];
			final double max[] = new double[1];
			CvPoint minPoint = new CvPoint(2);
			CvPoint maxPoint = new CvPoint(2);
			cvMinMaxLoc(voteMatrix, min, max, minPoint, maxPoint, null);

			final Rectangle b = new Rectangle(maxPoint.x(), maxPoint.y(), queryGray.width(), queryGray.height());
			PiccoloImageRenderer anno = new  PiccoloImageRenderer(inputGray.getBufferedImage()){
				@Override
				protected void addContent(PLayer layer) {
					PPath p = PPath.createRectangle(b.x,b.y,b.width,b.height);
					p.setStroke(new BasicStroke(2));
					p.setStrokePaint(Color.red);
					p.setPaint(null);
					layer.addChild(p);
				}			
			};
			if (max[0] < NUM_LOCAL_PATCHES){
				logger.step(anno.render(), "best location (not a match)");
			}else{
				logger.step(anno.render(), "best location");
			}


			double score = (double) max[0] / (NUM_LOCAL_PATCHES*VOTE_INCREMENT);
			RegionMatch m = new RegionMatch(b);
			m.setScore(score);
		}	

		static private IplImage vote(IplImage input, IplImage query, List<Rectangle> sampleRectangles){

			final List<Rectangle> ms = Lists.newArrayList();
			//


			IplImage voteMatrix = IplImage.create(cvGetSize(input), 8, 1);
			cvSet(voteMatrix, cvScalarAll(0));

			for (Rectangle r : sampleRectangles){

				IplImage featureImage = IplImage.create(cvSize(r.width,r.height), 8, 1);			
				cvSetImageROI(query, cvRect(r.x,r.y,r.width,r.height));
				cvCopy(query, featureImage);

				IplImage rm = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(input, featureImage);

				for (int i = 0; i < NUM_VOTES_PER_PATCH; ++i){
					TemplateMatchResult b = TemplateMatchingUtilities.fetchNextBestMatch(rm, featureImage);
					ms.add(b);

					int ox = b.x - r.x;
					int oy = b.y - r.y;

					if (ox<0 || oy<0 || ox>voteMatrix.width()-1 || oy>voteMatrix.height()-1){
						continue;
					}				

					CvScalar currentCount = cvGet2D(voteMatrix, oy, ox);
					double val = currentCount.getVal(0);
					val = val + VOTE_INCREMENT;
					cvSet2D(voteMatrix, oy, ox, cvScalarAll(val));
				}

				cvResetImageROI(query);
			}


			PiccoloImageRenderer anno = new  PiccoloImageRenderer(input.getBufferedImage()){
				@Override
				protected void addContent(PLayer layer) {
					for (Rectangle m : ms){
						PPath p = PPath.createRectangle(m.x,m.y,m.width,m.height);
						p.setStroke(new BasicStroke(2));
						p.setStrokePaint(Color.red);
						p.setPaint(null);
						layer.addChild(p);
					}
				}			
			};
			logger.step(anno.render(), "local features found in the input image");

			return voteMatrix;		
		}

		static private List<Rectangle> sampleSalientLocalRectangles(IplImage gray, IplImage ignoreMask){
			//
			IplImage foreground = VisionUtils.computeForegroundMaskOf(gray);
			logger.step(foreground, "foreground");

			IplImage keepMask = IplImage.createCompatible(ignoreMask);
			cvNot(ignoreMask, keepMask);

			IplImage effectiveForeground = foreground;
			cvAnd(foreground, keepMask, effectiveForeground, null);
			logger.step(foreground, "effective foreground");

			IplImage block = IplImage.create(cvSize(25,25), 8, 1);
			cvSet(block, cvScalarAll(255));

			IplImage resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix1(effectiveForeground, block);

			final List<Rectangle> rs = Lists.newArrayList();
			for (int i = 0; i < NUM_LOCAL_PATCHES; ++i){
				TemplateMatchResult fetchNextBestMatch = TemplateMatchingUtilities.fetchNextBestMatch1(resultMatrix, block);
				rs.add(fetchNextBestMatch);
			}

			PiccoloImageRenderer anno = new  PiccoloImageRenderer(gray.getBufferedImage()){
				@Override
				protected void addContent(PLayer layer) {
					for (Rectangle m : rs){
						PPath p = PPath.createRectangle(m.x,m.y,m.width,m.height);
						p.setStroke(new BasicStroke(2));
						p.setStrokePaint(Color.red);
						p.setPaint(null);
						layer.addChild(p);
					}
				}			
			};
			logger.step(anno.render(), "");//sampled locations");

			return rs;
		}

		@Override
		public RegionMatch fetchNext() {
			return bestMatch;
		}

	}

	static class SearchByGrayscaleAtOriginalResolution implements SearchAlgorithm<RegionMatch>{

		IplImage resultMatrix = null;
		IplImage query;
		IplImage input;

		SearchByGrayscaleAtOriginalResolution(IplImage input, IplImage query){
			this.input = input;
			this.query = query;
		}

		@Override
		public void execute(){
			this.resultMatrix = TemplateMatchingUtilities.computeTemplateMatchResultMatrix(input, query);	
		}

		@Override
		public RegionMatch fetchNext(){
			final TemplateMatchResult result = TemplateMatchingUtilities.fetchNextBestMatch(resultMatrix, query);
			RegionMatch m = new RegionMatch(result);
			m.setScore(result.score);
			//System.out.println("x = " + m.getX() + ", y = " + m.getY() + ", score = " + m.getScore());
			return m;
		}
	}

}