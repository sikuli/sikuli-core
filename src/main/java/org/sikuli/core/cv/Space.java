package org.sikuli.core.cv;

import static com.googlecode.javacv.cpp.opencv_core.cvAvgSdv;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;

import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;

public class Space {
	
	static private ImageExplainer logger = ImageExplainer.getExplainer(MarginFinderOld.class);
	
	private IplImage gray;
	
	
	public Space(IplImage gray) {
		super();
		this.gray = gray;
	}

	public Rectangle findContentBounds(Rectangle roi){
		
		int h = gray.height();
		int w = gray.width();
		
		int t = findMarginEndPosition(gray, roi.y, roi.y + roi.height, 1, true, roi);
		int b = findMarginEndPosition(gray, roi.y + roi.height-1, roi.y, -1, true, roi);
		int l = findMarginEndPosition(gray, roi.x, roi.x+roi.width, 1, false, roi);
		int r = findMarginEndPosition(gray, roi.x+roi.width-1, roi.x, -1, false, roi);

		if (r<l)
			l = r;
		
		if (b<t)
			b = t;
		
		Rectangle regionInsideMargin = new Rectangle(l,t,r-l+1,b-t+1);		
		ImageRenderer explainationImageRenderer = new ExplainationImageRenderer(gray.getBufferedImage(),regionInsideMargin);
		logger.result(explainationImageRenderer, "margin (t=" + t + ", l=" + l + ", b=" + b + ", r=" + r + ")");
		return regionInsideMargin;
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
	
	static private int findMarginEndPosition(IplImage gray, int startPosition, int endPosition, int step, boolean row, Rectangle roi){
		int h = gray.height();
		int w = gray.width();
		
		//System.out.println("start = " + startPosition + " end = " + endPosition);
		int i = startPosition;
		while ((i + step) != endPosition){
			
			if (row){
				CvRect rowROI = cvRect(roi.x,i,roi.width,1);		
				//System.out.println("rowROI = " + rowROI.x() + ", i = " + i + ", step = " + step);
				cvSetImageROI(gray, rowROI);
			}else{
				CvRect colROI = cvRect(i,roi.y,1,roi.height-1);			
				cvSetImageROI(gray, colROI);
			}
			
			CvScalar mean = new CvScalar();
			CvScalar stdDev = new CvScalar();			
			cvAvgSdv(gray, mean, stdDev, null);
			cvResetImageROI(gray);
			
			if (stdDev.getVal(0) > 10){
				return i;
			}			
			i += step;
		}
		return endPosition;
	}
}