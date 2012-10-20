package org.sikuli.ocr;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.search.ImageQuery;
import org.sikuli.core.search.ImageSearcher;
import org.sikuli.core.search.RegionMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jgoodies.looks.Fonts;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;

public class FontModelLearner {

	static private ImageExplainer explainer = ImageExplainer.getExplainer(FontModelLearner.class);
	static private Logger logger = LoggerFactory.getLogger(FontModelLearner.class);

	static private BufferedImage crop(BufferedImage src, int x, int y, int width, int height)
	{
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = dest.getGraphics();
		g.drawImage(src, 0, 0, width, height, x, y, x + width, y + height, null);
		g.dispose();
		return dest;
	}

//
//	static private BufferedImage trim(BufferedImage image){
//		IplImage gray = ImagePreprocessor.createGrayscale(image);	
//		XYDecomposer d = new XYDecomposer();
//		DefaultXYDecompositionStrategy strategy = new DefaultXYDecompositionStrategy(){
//
//			@Override
//			public int getMaxLevel() {
//				return 1;
//			}
//
//			@Override
//			public int getMinSeperatorSize() {
//				return 1;
//			}
//
//			@Override
//			public int getMinStdDev() {
//				return 1;
//			}
//		};
//		Block root = d.decompose(gray, strategy);
//		Rectangle r = root.getChild(0).getBounds();
//		BufferedImage decomposed = root.toImage(image);
//		//logger.step(decomposed, "decomposition result");
//		return crop(image,r.x,r.y,r.width,r.height);
//	}

	static class FontModelCandidate {
		final private FontModel model;
		final private double score;

		public FontModelCandidate(FontModel model, double score) {
			super();
			this.model = model;
			this.score = score;
		}

		public FontModel getModel() {
			return model;
		}
	}


//	static double evaluateFontModel(final BufferedImage trainingImage, String label, FontModel fontModel){
//		TextImageRenderer renderer = new TextImageRenderer();		
//		final BufferedImage modelImage = renderer.render(label,fontModel);
//		//		final BufferedImage modelImage = trim(modelImage1);
//		//		logger.step(modelImage,"font model being evaluated");
//
//		// side by side comparison
//		PiccoloImageRenderer a = new PiccoloImageRenderer(500,200){
//			@Override
//			protected void addContent(PLayer layer) {
//				PImage image1 = new PImage(trainingImage);
//				layer.addChild(image1);
//
//				PImage image2 = new PImage(modelImage);
//				image2.setOffset(0, trainingImage.getHeight());
//				layer.addChild(image2);
//			}
//		};		
//		//logger.step(a, "training (top) vs model (bottom)");
//
//		//final BufferedImage modelImage = trim(modelImage);
//
//		if (modelImage.getWidth() > trainingImage.getWidth() ||
//				modelImage.getHeight() > trainingImage.getHeight())
//			return 0;
//
//		ImageSearcher s = new ImageSearcher(trainingImage);
//		ImageQuery q = new ImageQuery(modelImage);
//		List<RegionMatch> matches = s.search(q, null, 1);
//		final RegionMatch m = matches.get(0);
//
//
//
//		a = new PiccoloImageRenderer(500,200){
//			@Override
//			protected void addContent(PLayer layer) {
//				PImage image1 = new PImage(trainingImage);			
//				layer.addChild(image1);
//
//				PImage image2 = new PImage(modelImage);
//				image2.setOffset(m.getX(), trainingImage.getHeight());
//				layer.addChild(image2);
//			}
//		};		
//		explainer.step(a, String.format("Score: %1.4f (name = %s, size = %d, tracking = %1.2f)", m.getScore(), 
//				fontModel.getName(), fontModel.getSize(), fontModel.getTracking()));
//		//logger.step(a, "aligned horizontally");
//
//
//		//		PiccoloImageAnnotator a = new PiccoloImageAnnotator(500,200){
//		//			@Override
//		//			protected void addAnnotations(PLayer layer) {
//		//				PImage renderedTextImageNode = new PImage(modelImage);
//		//				renderedTextImageNode.setTransparency(0.5f);
//		//				//renderedTextImageNode.setOffset(offsetx, offsety);
//		//
//		//				PImage tImage = new PImage(trimmedTrainingImage);
//		//				tImage.setOffset(offsetx,offsety);
//		//				layer.addChild(tImage);
//		//
//		//				layer.addChild(renderedTextImageNode);
//		//
//		//				renderedTextImageNode = new PImage(fitModelImage);
//		//				renderedTextImageNode.setTransparency(0.5f);
//		//				renderedTextImageNode.setOffset(0, offsety+50);
//		//				layer.addChild(renderedTextImageNode);
//		//
//		//
//		//				PPath p = PPath.createRectangle(0, 0, 
//		//						fitModelImage.getWidth(), fitModelImage.getHeight());
//		//				p.setStrokePaint(Color.red);
//		//				p.setPaint(null);
//		//				layer.addChild(p);
//		//			}				
//		//		};
//		//		//
//		//		logger.debug(a.render(),"i = " + i);
//
//
//		return m.getScore();
//	}


//	private static FontModel estimateFontModel(BufferedImage trainingImage, String label, int baseFontSize){
//		TextImageRenderer renderer = new TextImageRenderer();
//		FontModel fontModel = new FontModel();
//		fontModel.setSize(baseFontSize);
//
//		BufferedImage modelImage = renderer.render(label,fontModel);
//		//		logger.step(modelImage,"base model image");
//
//		final BufferedImage trimmedTrainingImage = trim(trainingImage);
//		final BufferedImage trimmedModelImage = trim(modelImage);
//
//		//		logger.step(trimmedTrainingImage,"trimmed training image");
//		//		logger.step(trimmedModelImage,"trimmed training image");
//
//		Dimension tightSizeOfTrainingImage = new Dimension(trimmedTrainingImage.getWidth(),trimmedTrainingImage.getHeight());
//		Dimension tightSizeOfModelImage = new Dimension(trimmedModelImage.getWidth(),trimmedModelImage.getHeight());
//
//		double scalex = 1.0 * tightSizeOfTrainingImage.width / tightSizeOfModelImage.width;
//		double scaley = 1.0 * tightSizeOfTrainingImage.height / tightSizeOfModelImage.height;
//
//		fontModel.setScaleX(scalex);
//		fontModel.setScaleY(scaley);
//
//		return fontModel;
//	}



	
	public static FontModel learn(final BufferedImage trainingImage, String text) {
		logger.trace("learning started");

		final List<BufferedImage> imageList = Lists.newArrayList();
		
		List<FontModel> candidates = Lists.newArrayList();
		
		List<Font> fonts = FontLibrary.getFonts();
		double[] sizes = {14,13,12,11,10,9};
		double[] trackings = {0, -0.01, -0.02, -0.03};
		for (Font font : fonts){
			for (double size : sizes){
				for (double tracking : trackings){
					FontModel model = new FontModel();
					model.setFont(font);
					model.setSize(size);
					model.setTracking(tracking);
					candidates.add(model);		
				}
			}
		}
		logger.trace("parameters generated");
		
		for (FontModel candidate : candidates){
			BufferedImage modelImage = candidate.toImage(text);
			imageList.add(modelImage);
		}
		logger.trace("individual model images generated");
		
		BufferedImage tallestImage = Collections.max(imageList, new Comparator<BufferedImage>(){
			@Override
			public int compare(BufferedImage a, BufferedImage b) {
				return a.getHeight() - b.getHeight();
			}			
		});
		
		BufferedImage widestImage = Collections.max(imageList, new Comparator<BufferedImage>(){
			@Override
			public int compare(BufferedImage a, BufferedImage b) {
				return a.getWidth() - b.getWidth();
			}			
		});


		int n = imageList.size();
		final int dy = tallestImage.getHeight()+2;
		int w = widestImage.getWidth()+10;
		PiccoloImageRenderer a = new PiccoloImageRenderer(w,n*dy){
			@Override
			protected void addContent(PLayer layer) {
				for (int i=0; i < imageList.size(); ++i){
					BufferedImage image = imageList.get(i);
					PImage pImage = new PImage(image);
					pImage.setOffset(5, i*dy);
					layer.addChild(pImage);
				}
			}
		};	
		BufferedImage tiledModelImage = a.render();
		explainer.step(tiledModelImage, "tiledModelImage");
		logger.trace("tiled model image generated");

		ImageSearcher s = new ImageSearcher(tiledModelImage);
		ImageQuery q = new ImageQuery(trainingImage);
		List<RegionMatch> matches = s.search(q, null, 1);
		final RegionMatch m = matches.get(0);

		int index = (int) Math.round(1.0 * (m.y + 1) / dy);
		
		logger.trace("learning stopped. top score = " + m.getScore() + " index = " + index + " m.y = " + m.y);
		
		FontModel bestModel = candidates.get(index);
		return bestModel;
		
	}


}


class FontLibrary {
	
	static private List<Font> loadCustomFonts(String[] fontNames){
		List<Font> fonts = Lists.newArrayList();
		for (String fontName : fontNames){
			InputStream is = TextImageRenderer.class.getResourceAsStream(fontName);
			Font font = null;
			try {
				font = Font.createFont(Font.TRUETYPE_FONT, is);
				fonts.add(font);
			} catch (FontFormatException e) {
			} catch (IOException e) {
			}
		}
		return fonts;
	}
	
	static List<Font> precreatedFonts = Lists.newArrayList();	
	static List<Font> getFonts(){
		return precreatedFonts;
	}
	
	static {
//		String[] fontNames = {"TAHOMA.TTF","SEGOEUI.TTF","MICROSS.TTF"};
//		String[] fontNames = {"MICROSS.TTF"};
		String[] fontNames = {};
		precreatedFonts = loadCustomFonts(fontNames);
//		precreatedFonts.add(new Font("sansserif", 0, 0));
		precreatedFonts.add(Fonts.WINDOWS_XP_96DPI_DEFAULT_GUI);
		precreatedFonts.add(Fonts.WINDOWS_XP_120DPI_DEFAULT_GUI);
//		precreatedFonts.add(Fonts.SEGOE_UI_12PT);
//		precreatedFonts.add(Fonts.SEGOE_UI_13PT);
//		precreatedFonts.add(Fonts.TAHOMA_11PT);
//		precreatedFonts.add(Fonts.TAHOMA_13PT);
//		precreatedFonts.add(new Font("sansserif", 0, 0));

		//		precreatedFonts.add(new Font("serif", 0, 0));
//		precreatedFonts.add(new Font("monospaced", 0, 0));	
	}
}
