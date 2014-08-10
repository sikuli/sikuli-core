package org.sikuli.core.cv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sikuli.core.draw.BlobPainter;
import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.search.ScoredItem;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;


import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.POffscreenCanvas;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

public class DiffFinder {


	private static final int threshold = 50;
	
	
	// TODO: move this to VisionUtil
	IplImage cloneWithoutAlphaChannel(IplImage bgra){
		
		IplImage bgr = IplImage.create(bgra.width(), bgra.height(), 8, 3);
		IplImage alpha = IplImage.create(bgra.width(), bgra.height(), 8, 1);
		
		//cvSet(rgba, cvScalar(1,2,3,4));

		IplImage[] in = {bgra};
		IplImage[] out = {bgr, alpha}; 
		int from_to[] = { 0,3,  1,0,  2,1,  3,2 };
		cvMixChannels(in, 1, out, 2, from_to, 4);
		
		return bgr;
	}
	
	//
	// Assume beforeImage and afterImage have the same size
	public List<ScoredItem<Rectangle>> diff(BufferedImage beforeImage, BufferedImage afterImage){

		ImageExplainer logger = ImageExplainer.getExplainer(this.getClass());


		IplImage before = IplImage.createFrom(beforeImage);
		IplImage after = IplImage.createFrom(afterImage);
		
		if (after.nChannels() == 4){
			after = cloneWithoutAlphaChannel(after);
		}
		
		if (before.nChannels() == 4){
			before = cloneWithoutAlphaChannel(before);
		}
		
		logger.step(beforeImage, "before");
		logger.step(afterImage, "after");		

		IplImage diff = IplImage.createCompatible(before);
		cvAbsDiff(before, after, diff);

		logger.step(diff, "diff (color)");

		IplImage diffg = IplImage.create(cvGetSize(before),8,1);
		cvCvtColor(diff, diffg, CV_RGB2GRAY);

		logger.step(diffg, "diff (gray)");

		// thresholding
		cvThreshold(diffg, diffg, 50, 255, CV_THRESH_BINARY);			
		logger.step(diffg, "thresholded by " + threshold);

		// find connected components
		final List<CvRect> blobs = VisionUtils.detectBlobs(diffg);
		
		BufferedImage painted = (new BlobPainter(diffg.getBufferedImage(), blobs)).render();
		logger.step(painted, "detected components");


		// filtering. keep only blobs with at least certain size.
		List<CvRect> filtered_blobs = new ArrayList<CvRect>();
		for (CvRect blob : blobs){
			if (blob.width() > 5 && blob.height() > 5){
				filtered_blobs.add(blob);
			}
		}
		
		painted = (new BlobPainter(diffg.getBufferedImage(), blobs)).render();
		logger.step(painted, "detected components (after filtering out small blobs)");
		
		final List<ScoredItem<Rectangle>> returned_list = new ArrayList<ScoredItem<Rectangle>>();
		for (CvRect b : filtered_blobs){
		
			Rectangle r = new Rectangle(b.x(),b.y(),b.width(),b.height());
			
			// count the number of diff pixels in the rectangle
			cvSetImageROI(diffg, b);
			int numPixels = cvCountNonZero(diffg);
			cvResetImageROI(diffg);
		
			// compute the score as the ratio of diff pixels to area
			float score = (float) (1.0 * numPixels / (b.width() * b.height()));
			
			ScoredItem<Rectangle> s = new ScoredItem<Rectangle>(r, score);						
			returned_list.add(s);
		}
		
		
		ImageRenderer viz = new PiccoloImageRenderer(diffg.getBufferedImage()){

			@Override
			protected void addContent(PLayer layer) {
				for (ScoredItem<Rectangle> s : returned_list){
					Rectangle r = s.getItem();
					PPath l = PPath.createRectangle(r.x,r.y,r.width,r.height);								
					l.setStrokePaint(Color.red.darker());
					//l.setTransparency(0);
					l.setPaint(null);
					layer.addChild(l);
					
					PText t = new PText(String.format("%3f",s.getScore()));
					t.setTextPaint(Color.yellow);
					t.setOffset(r.x,r.y);
					t.setScale(1.2);
					layer.addChild(t);
				}
			}
		};
		
		logger.step(viz, "results with scores");
		
		return returned_list;
	}

}
