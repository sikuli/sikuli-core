package org.sikuli.core.search;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.sikuli.core.logging.ImageExplainer;

public class SearchableTest {

	@Test
	public void testBasicAPI() throws IOException{
//		BufferedImage screenImage = ImageIO.read(new File("src/test/resources/macdesktop/screen.png"));
//		BufferedImage queryImage = ImageIO.read(new File("src/test/resources/macdesktop/target1_898_213.png"));
		
		BufferedImage screenImage = ImageIO.read(new File("src/test/resources/macdesktopdark/screen.png"));
		BufferedImage queryImage = ImageIO.read(new File("src/test/resources/macdesktopdark/targetFolder_1194_65_1196_178_1196_287.png"));

		ImageExplainer.getExplainer(ImageSearcher.class).setLevel(ImageExplainer.Level.STEP);

		
		ImageSearcher searcher = new ImageSearcher(screenImage);
		ImageQuery query = new ImageQuery(queryImage);		
		RegionFilter filter = new RegionFilter();
		filter.include(new Rectangle(0,0,1400,150));
		
		List<RegionMatch> topMatches = searcher.search(query, filter, 5);		
		for (RegionMatch m : topMatches){			
			System.out.println("x = " + m.getX() + ", y = " + m.getY() + ", score = " + m.getScore());		
		}
	
	}
	
	@Test
	public void testSearchWithTransparentImageQuery() throws IOException{
		
		ImageExplainer.getExplainer(MaskedImageQuery.class).setLevel(ImageExplainer.Level.STEP);
		ImageExplainer.getExplainer(SearchAlgorithmFactory.class).setLevel(ImageExplainer.Level.STEP);
		
		BufferedImage screenImage = ImageIO.read(new File("src/test/resources/mask/screen2.png"));
		BufferedImage targetImage = ImageIO.read(new File("src/test/resources/mask/target.png"));
		BufferedImage ignoreImage = ImageIO.read(new File("src/test/resources/mask/ignore.png"));
		
		//MaskedImageQuery.createMaskFromSubimage(screenImage, targetImage, ignoreImage);
		
		ImageSearcher searcher = new ImageSearcher(screenImage);
		MaskedImageQuery query = new MaskedImageQuery(targetImage);
		query.ignore(ignoreImage);
				
		List<RegionMatch> topMatches = searcher.search(query, 1);
		for (RegionMatch m : topMatches){	
			System.out.println("x = " + m.getX() + ", y = " + m.getY() + ", score = " + m.getScore());		
		}
	}
}