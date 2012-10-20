package org.sikuli.core.cv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;

import static com.googlecode.javacv.cpp.opencv_core.*;

public class MarginFinderOld {
	
	static private ImageExplainer logger = ImageExplainer.getExplainer(MarginFinderOld.class);
	
	static public Rectangle getRegionInsideMargin(IplImage gray){
		
		int h = gray.height();
		int w = gray.width();
		
		int t = findMarginEndPosition(gray, 0, h, 1, true);
		int b = findMarginEndPosition(gray, h, 0, -1, true);
		int l = findMarginEndPosition(gray, 0, w, 1, false);
		int r = findMarginEndPosition(gray, w, 0, -1, false);

		if (r<l)
			l = r;
		
		if (b<t)
			b = t;
		
		Rectangle regionInsideMargin = new Rectangle(l,t,r-l,b-t);		
		ImageRenderer explainationImageRenderer = new ExplainationImageRenderer(gray.getBufferedImage(),regionInsideMargin);
		logger.result(explainationImageRenderer, "margin (t=" + t + ", l=" + l + ", b=" + b + ", r=" + r + ")");
		return regionInsideMargin;
	}
	
	static public Rectangle getRegionInsideMargin(BufferedImage input){
		IplImage gray = ImagePreprocessor.createGrayscale(input);
		return getRegionInsideMargin(gray);		
	}
	
	static class ExplainationImageRenderer extends PiccoloImageRenderer 
		implements ImageRenderer{

		final private Rectangle r;
		public ExplainationImageRenderer(BufferedImage input, Rectangle r) {
			super(input);
			this.r = r;
		}

		@Override
		protected void addContent(PLayer layer) {
			PPath p = PPath.createRectangle(r.x,r.y,r.width,r.height);
			p.setPaint(null);
			p.setStrokePaint(Color.red);
			p.setStroke(new BasicStroke(1f));
			layer.addChild(p);			
		}
		
	}
	
	static private int findMarginEndPosition(IplImage gray, int startPosition, int endPosition, int step, boolean row){
		int h = gray.height();
		int w = gray.width();
		
		int i = startPosition;
		while ((i + step) != endPosition){
			i += step;

			if (row){
				CvRect rowROI = cvRect(0,i,w,1);			
				cvSetImageROI(gray, rowROI);
			}else{
				CvRect colROI = cvRect(i,0,1,h);			
				cvSetImageROI(gray, colROI);
			}
			
			CvScalar mean = new CvScalar();
			CvScalar stdDev = new CvScalar();			
			cvAvgSdv(gray, mean, stdDev, null);
			cvResetImageROI(gray);
			
			if (stdDev.getVal(0) > 10){
				return i;
			}			
		}
		return endPosition;
	}
}

