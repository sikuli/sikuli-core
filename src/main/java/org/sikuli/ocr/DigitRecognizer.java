package org.sikuli.ocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.sikuli.core.cv.TextMap;
import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.search.ImageQuery;
import org.sikuli.core.search.ImageSearcher;
import org.sikuli.core.search.RegionMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;

public class DigitRecognizer {

	final static private ImageExplainer explainer = ImageExplainer.getExplainer(DigitRecognizer.class);
	final static private Logger logger = LoggerFactory.getLogger(DigitRecognizer.class);

	// Digit search parameters
	static final int HORIZONTAL_SPLIT_THRESHOLD = 12;
	static final double DIGIT_MATCH_MIN_SCORE = 0.65;

	// Digit template parameters
	static final int dy=20;
	static final int dx=12;
	static final int margin=5;

	static ImageSearcher digitImageSearcher = new ImageSearcher(generateDigitTemplateImage());

	private DigitRecognizer(){		
	}
	
	static private Integer convertLocationToDigit(int x, int y){
		int i = Math.round((x - margin) / dx);
		int px = (x - margin) % dx;
		int py = (y - margin) % dy;
		if (px < 3 && py > 3)
			return null;
		else
			return i;
	}
	static private BufferedImage generateDigitTemplateImage(){
		final List<Font> fonts = Lists.newArrayList();
		fonts.add(new Font("sansserif",0,0));
		fonts.add(new Font("serif",0,0));
		fonts.add(new Font("monaco",0,0));
		PiccoloImageRenderer a = new PiccoloImageRenderer(130,dy*7*fonts.size()+20){

			@Override
			protected void addContent(PLayer layer) {
				int x = margin;
				int y = margin;
				for (Font font : fonts){
					for (int size = 9; size <= 15; ++ size){
						for (int i=0;i<=9;i++){		
							BufferedImage digitImage = TextImageRenderer.render(""+i, font, size, 0);
							PImage pi = new PImage(digitImage);
							pi.setOffset(x,y);
							layer.addChild(pi);
							x += dx;						
						}
						y += dy;
						x = margin;
					}
				}
			}			
		};
		explainer.step(a, "generated digit template images");
		return a.render();
	}
	

	static public List<RecognizedDigit> recognize(BufferedImage inputImage){

		List<RecognizedDigit> recognizedDigits = Lists.newArrayList(); 

		TextMap tm = TextMap.createFrom(inputImage);
		for (Rectangle r : tm.getCharacterBounds()){
			recognizeDigit(inputImage, r, digitImageSearcher, recognizedDigits);
		}		
		
		explainer.step(visualize(inputImage, recognizedDigits), "recognized digits");
		
		return recognizedDigits;
	}
	
	static private ImageRenderer visualize(BufferedImage inputImage, final List<RecognizedDigit> recognizedDigits){
		return new PiccoloImageRenderer(inputImage){
			@Override
			protected void addContent(PLayer layer) {
				for (RecognizedDigit r : recognizedDigits){				
					//Rectangle r = md.bounds;
					PText t = new PText(""+r.digit);
					t.setOffset(r.x, r.y+r.height);
					t.setScale(0.7f);
					t.setTextPaint(Color.red);
					layer.addChild(t);					
				}
			}			
		};
	}

	static private void recognizeDigit(BufferedImage inputImage, Rectangle r, ImageSearcher digitImageSearcher, 
			List<RecognizedDigit> recognizedDigits){
		if (r.width == 0 || r.height <= 3)
			return;

		BufferedImage charImage = inputImage.getSubimage(r.x, r.y, r.width, r.height);		
		ImageQuery q = new ImageQuery(charImage);
		List<RegionMatch> matches = digitImageSearcher.search(q,null,1);

		RegionMatch m = matches.get(0);
		double score = m.getScore();
		Integer i = convertLocationToDigit(m.x,m.y);

		logger.trace("[" + i + "] (" + m.x + "," + m.y + ") score: " + score);

		if (score > DIGIT_MATCH_MIN_SCORE && i != null){
			RecognizedDigit md = new RecognizedDigit();
			md.x = r.x;
			md.y = r.y;
			md.width = r.width;
			md.height = r.height;
			md.digit = Integer.toString(i).charAt(0);
			recognizedDigits.add(md);
		}else{

			if (r.width > HORIZONTAL_SPLIT_THRESHOLD){				
				Rectangle r1 = new Rectangle(r.x,r.y,r.width/2,r.height);
				Rectangle r2 = new Rectangle(r.x + r.width/2,r.y,r.width/2,r.height);
				recognizeDigit(inputImage, r1, digitImageSearcher, recognizedDigits);
				recognizeDigit(inputImage, r2, digitImageSearcher, recognizedDigits);				
			}

		}
	}
}
