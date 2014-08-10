package org.sikuli.core.search;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sikuli.core.cv.ImagePreprocessor;
import org.sikuli.core.draw.ImageRenderer;
import org.sikuli.core.draw.PiccoloImageRenderer;
import org.sikuli.core.logging.ImageExplainer;
import org.sikuli.core.search.algorithm.SearchByTextureAndColorAtOriginalResolution;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

public class ImageSearcher extends Searcher<RegionMatch> {
	
	static private ImageExplainer logger = ImageExplainer.getExplainer(ImageSearcher.class);
	
	final private BufferedImage image;
	public ImageSearcher(BufferedImage image){
		this.image = image;
	}
	
	private SearchAlgorithm<RegionMatch> getAlgorithmFor(ImageQuery query){
		return new SearchAlgorithmFactory.SearchByGrayscaleAtOriginalResolution(
				ImagePreprocessor.createGrayscale(getImage()),
				ImagePreprocessor.createGrayscale(query.getImage()));
	}		
	
	protected void explain(final Query query, final List<RegionMatch> os){
		ImageRenderer r = new PiccoloImageRenderer(getImage()){
			@Override
			protected void addContent(PLayer layer) {				
				for (RegionMatch o : os){	
					Rectangle r = o.getBounds();				
					PPath p = PPath.createRectangle(r.x, r.y, r.width, r.height);
					p.setStrokePaint(Color.red);
					p.setStroke(new BasicStroke(3.0f));
					p.setPaint(null);
					layer.addChild(p);
					
					PText s = new PText(String.format("%3f", o.getScore()));
					s.setOffset(r.x,r.y-15);
					layer.addChild(s);
					
					 if (query instanceof ImageQuery){
						 BufferedImage queryImage = ((ImageQuery) query).getImage();
						 PImage img = new PImage(queryImage);
							img.setOffset(r.x, r.y + r.height + 2);
							img.setTransparency(0.5f);
							layer.addChild(img);
					 }					 
				}				
			}
		};		
		logger.result(r, "search result");
	}
	
	
	protected SearchAlgorithm<RegionMatch> getAlgorithm(Query query){		
		// TODO: Fix this ugly algorithm mapping code
		SearchAlgorithm<RegionMatch> alg = null;
		if (query.getClass() == ColorImageQuery.class){			
			ColorImageQuery imageQuery = (ColorImageQuery) query;
			alg = new SearchByTextureAndColorAtOriginalResolution(
					IplImage.createFrom(getImage()), 
					IplImage.createFrom(imageQuery.getImage()));
		}else if (query instanceof ImageQuery){
			alg = getAlgorithmFor((ImageQuery)query);
		}
		return alg;
	}

	public BufferedImage getImage() {
		return image;
	}
	
}