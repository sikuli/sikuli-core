package org.sikuli.core.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.TextMap.TextBlock;
import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_imgproc.*;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;

public class TextMap {
	
	private static final int MIN_CHARACTER_WIDTH = 1;
	private static final int MAX_CHARACTER_WIDTH = 50;
	private static final int MIN_CHARACTER_HEIGHT = 8;
	private static final int MAX_CHARACTER_HEIGHT = 20;

	static class TextBlock {
		public int x;
		public int y;
		public int width;
		public int height;
		public TextBlock(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}		
	}
	
	ImageExplainer explainer = ImageExplainer.getExplainer(TextMap.class);
	private IplImage characterBlockMask;
	private List<TextBlock> blobs;
	
	static public TextMap createFrom(BufferedImage image){
		TextMap m = new TextMap();
		m.init(image);
		return m;
	}
	
	public double computeTextScore(int x, int y, int width, int height){
		int x1 = Math.max(0, x);
		int y1 = Math.max(0, y);
		int w1 = Math.min(characterBlockMask.width()-x1-2,width);
		int h1 = Math.min(characterBlockMask.height()-y1-2,height);
		int w0 = characterBlockMask.width();
		int h0 = characterBlockMask.height();		
		cvSetImageROI(characterBlockMask, cvRect(x1,y1,w1,h1));
		int n = cvCountNonZero(characterBlockMask);
		cvResetImageROI(characterBlockMask);
		return 1.0 * n / (width*height);
	}
	
	
	public Iterable<Rectangle> getCharacterBounds(){
		return Iterables.transform(blobs, new Function<TextBlock, Rectangle>(){
			@Override
			public Rectangle apply(TextBlock b) {
				return new Rectangle(b.x,b.y,b.width,b.height);
			}			
		});
	}
	
	void init(BufferedImage image) {			
		explainer.step(image, "input image");
		
		
		IplImage grayImage = ImagePreprocessor.createGrayscale(image);			
		IplImage foregroundMask = computeForegroundMask(grayImage);
		
		explainer.step(foregroundMask, "foreground mask");
		
		blobs = computeBlobs(foregroundMask);
		
		explainer.step(visualize(foregroundMask.getBufferedImage(), blobs), "extracted blobs");
		
		blobs = rejectOverlyLargeOrSmallBlobs(blobs);
		
		explainer.step(visualize(foregroundMask.getBufferedImage(), blobs), "overly small/large blobs removed");
		
		characterBlockMask = computeCharacterBlockMask(foregroundMask, blobs);

		explainer.step(characterBlockMask.getBufferedImage(), "character block mask");
		
//		blobs = computeBlobs(characterBlockMask.clone());
//
//		explainer.step(visualize(characterBlockMask.getBufferedImage(), blobs), "non-overlapping character blobs");
		
	}	
	
	public BufferedImage getImage(){
		return characterBlockMask.getBufferedImage();
	}
	
	ImageRenderer visualize(BufferedImage input, final List<TextBlock> blobs){
		ImageRenderer m = new PiccoloImageRenderer(input){
			@Override
			protected void addContent(PLayer layer) {
				for (TextBlock b : blobs){					
					PPath p = PPath.createRectangle(b.x, b.y, b.width, b.height);
					p.setStrokePaint(Color.red);
					p.setPaint(null);
					layer.addChild(p);			
				}					
			}				
		};			
		return m;
	}
	
	IplImage computeForegroundMask(IplImage grayImage){			
		IplImage foregroundMask = IplImage.create(cvGetSize(grayImage), 8, 1);			
		IplImage edgeMap = IplImage.create(cvGetSize(grayImage), 8, 1);
		
		cvCanny(grayImage,edgeMap,0.66*50,1.33*50,3);
		IplConvKernel kernel = IplConvKernel.create(3,3,1,1,CV_SHAPE_RECT,null);
		cvDilate(edgeMap,edgeMap,kernel,1);
		kernel.release();
		
		cvAdaptiveThreshold(grayImage,foregroundMask,255,CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY_INV, 5, 1);
		
		cvAnd(foregroundMask, edgeMap, foregroundMask, null);
			
		return foregroundMask;
	}
	
	List<TextBlock> computeBlobs(IplImage binaryImage){
		IplImage clone = binaryImage.clone();
		CvMemStorage storage = CvMemStorage.create();
		CvSeq contour = new CvSeq(null);
		cvFindContours(clone, storage, contour, Loader.sizeof(CvContour.class),
				//CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
				CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);
		List<TextBlock> blobs = Lists.newArrayList(); 

		collectBlobs(contour, blobs);
		return blobs;
	}
	
	void collectBlobs(CvSeq seq, List<TextBlock> blobs){
		while (seq != null && !seq.isNull()) {
			if (seq.elem_size() > 0) {
				CvRect b = cvBoundingRect(seq,0);
				if (b.height() < MAX_CHARACTER_HEIGHT){
					TextBlock blob = new TextBlock(b.x(), b.y(), b.width(), b.height());
					blobs.add(blob);
				} else{					
					collectBlobs(seq.v_next(), blobs);
				}
			}
			seq = seq.h_next();
		}
	}
	
	List<TextBlock> rejectOverlyLargeOrSmallBlobs(List<TextBlock> blobs){
		List<TextBlock> out = Lists.newArrayList();
		for (TextBlock b : blobs){				
			if (b.width <= MAX_CHARACTER_WIDTH && b.height <= MAX_CHARACTER_HEIGHT
					&& b.width >= MIN_CHARACTER_WIDTH && b.height >= MIN_CHARACTER_HEIGHT){
				out.add(b);
			}
		}			
		return out;
	}
	
	IplImage computeCharacterBlockMask(IplImage foregroundMask, List<TextBlock> blobs){			
		IplImage characterBlockMask = IplImage.create(cvGetSize(foregroundMask), 8, 1);
		cvSet(characterBlockMask, cvScalarAll(0), null);			
		for (TextBlock b : blobs){				
			cvSetImageROI(characterBlockMask, cvRect(b.x,b.y,b.width,b.height));
			cvSet(characterBlockMask, cvScalarAll(255), null);
		}
		return characterBlockMask; 
	}
	
	
}