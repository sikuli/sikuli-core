/*******************************************************************************
 * Copyright 2011 sikuli.org
 * Released under the MIT license.
 * 
 * Contributors:
 *     Tom Yeh - initial API and implementation
 ******************************************************************************/
package org.sikuli.core.cv;

import static com.googlecode.javacv.cpp.opencv_core.CV_FILLED;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRealScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_TM_SQDIFF;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_TM_SQDIFF_NORMED;

import java.awt.Rectangle;
import java.util.List;


import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TemplateMatchHelper {

	IplImage resultMatrix;
	IplImage target;
	private int method;
	
	
	public TemplateMatchHelper(int method){
		this.method = method;
	}

	public void match(IplImage input, IplImage target){
		int tWidth = target.roi() != null ? target.roi().width() : target.width();
		int tHeight = target.roi() != null ? target.roi().height() : target.height();
		int iWidth = input.roi() != null ? input.roi().width() : input.width();
		int iHeight = input.roi() != null ? input.roi().height() : input.height();
		
		
		int rwidth = iWidth - tWidth + 1;
		int rheight = iHeight - tHeight + 1;      

		//System.out.println("rwidth:" + rwidth + " rheight:" + rheight);
		
		this.target = target;
		resultMatrix = IplImage.create(cvSize(rwidth, rheight), 32, 1);      
		opencv_imgproc.cvMatchTemplate(input, target, resultMatrix, method);
	}
	
//	public void match(IplImage input, IplImage target, List<Rectangle> rois){
//		int tWidth = target.roi() != null ? target.roi().width() : target.width();
//		int tHeight = target.roi() != null ? target.roi().height() : target.height();
//		int iWidth = input.roi() != null ? input.roi().width() : input.width();
//		int iHeight = input.roi() != null ? input.roi().height() : input.height();
//		
//		
//		resultMatrix = IplImage.create(cvSize(rwidth, rheight), 32, 1);
//		
//		int rwidth = iWidth - tWidth + 1;
//		int rheight = iHeight - tHeight + 1;      
//
//		//System.out.println("rwidth:" + rwidth + " rheight:" + rheight);
//		
//		this.target = target;
//		resultMatrix = IplImage.create(cvSize(rwidth, rheight), 32, 1);      
//		opencv_imgproc.cvMatchTemplate(input, target, resultMatrix, method);
//	}

	public FindResult fetchResult(){
		double min[] = new double[1];
		double max[] = new double[1];
		CvPoint minPoint = new CvPoint(2);
		CvPoint maxPoint = new CvPoint(2);


		opencv_core.cvMinMaxLoc(resultMatrix, min, max, minPoint, maxPoint, null);

		double detectionScore;
		CvPoint detectionLoc;
		
		if (method == CV_TM_SQDIFF || method == CV_TM_SQDIFF_NORMED){
			detectionScore = min[0];
			detectionLoc = minPoint;
		}else{			
			detectionScore = max[0];
			detectionLoc = maxPoint;
		}

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
