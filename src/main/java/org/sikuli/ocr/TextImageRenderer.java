package org.sikuli.ocr;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.piccolo.nodes.PText;

public class TextImageRenderer {
	
	static BufferedImage render(String text, Font font, double size, double tracking){
		Font f = font.deriveFont((float)size);
		Map textAttributes = new HashMap();
		textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		textAttributes.put(TextAttribute.TRACKING, tracking);
		textAttributes.put(TextAttribute.FONT, f);
		textAttributes.put(TextAttribute.SIZE, size);
		f = f.deriveFont(textAttributes);		
		PText p = new PText(text);
		p.setFont(f);
		return (BufferedImage) p.toImage();
	}
}