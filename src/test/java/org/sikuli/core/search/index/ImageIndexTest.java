package org.sikuli.core.search.index;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.sikuli.core.search.ImageQuery;
import org.sikuli.core.search.TopMatches;
import org.sikuli.core.search.index.ImageIndexWriter;

public class ImageIndexTest {

	
	@Test
	public void testAPI() throws IOException{
		
		BasicConfigurator.configure();
		
		ImageIndex index = new ImageIndex();
		Config config = new Config();		
		ImageIndexWriter w = new ImageIndexWriter(index, config);
				
		for (int i=1;i<=10;++i){		
			String imageDir = "src/test/resources/imagesets/icons";
			String imageFilename = String.format("%d.png",i);
			File imageFile = new File(imageDir, imageFilename);
			BufferedImage image = ImageIO.read(imageFile);		
			
			ImageLabel imageLabel = new IntegerImageLabel(i);
			LabeledImage labeledImage = new BufferedLabeledImage(image, imageLabel);
			w.add(labeledImage);
		}
		
		ImageIndexSearcher searcher = new ImageIndexSearcher(index);
		for (int i=1;i<=10;++i){		
			String imageDir = "src/test/resources/imagesets/icons";
			String imageFilename = String.format("%d.png",i);
			File imageFile = new File(imageDir, imageFilename);
			BufferedImage image = ImageIO.read(imageFile);
			
			ImageQuery query = new ImageQuery(image);
			List<LabeledImageMatch> matches = searcher.search(query, 1);
			
			System.out.println("label: " + matches.get(0));
			
		}
		
		
	}
}
